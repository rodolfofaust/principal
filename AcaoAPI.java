package br.com.sankhya.ctba.integracaoapi;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.cuckoo.core.ScheduledAction;
import org.cuckoo.core.ScheduledActionContext;

import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.TimeUtils;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.bmp.PersistentLocalEntity;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.util.JapeSessionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.dwfdata.vo.tsi.UsuarioVO;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.ws.ServiceContext;

public class AcaoAPI implements ScheduledAction {
	static final DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
	static final Logger logger = Logger.getLogger(AcaoAPI.class.getName());	

	@Override
	public void onTime(ScheduledActionContext contexto) {
		logger.log(Level.INFO, "[onTime] INICIO");
		contexto.info(dateFormat.format(new Date()) + " [onTime] INICIO");
		
		String token = "63851621c5e07d0016e05752";
		
		JapeSession.SessionHandle hnd = null;

		final AuthenticationInfo authInfo = new AuthenticationInfo("SUP", BigDecimalUtil.ZERO_VALUE,
				BigDecimalUtil.ZERO_VALUE, new Integer(Integer.MAX_VALUE));
		authInfo.makeCurrent();

		final ServiceContext sctx = new ServiceContext(null);
		sctx.setAutentication(authInfo);
		sctx.makeCurrent();

		try {
			hnd = JapeSession.open();
			hnd.setCanTimeout(false);
			hnd.setPriorityLevel(JapeSession.LOW_PRIORITY); 

			EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
			UsuarioVO usuVO = (UsuarioVO) ((DynamicVO) dwf.findEntityByPrimaryKeyAsVO(DynamicEntityNames.USUARIO,
					authInfo.getUserID())).wrapInterface(UsuarioVO.class);

			JapeSessionContext.putProperty("usuario_logado", authInfo.getUserID());
			JapeSessionContext.putProperty("emp_usu_logado", usuVO.getCODEMP());
			JapeSessionContext.putProperty("dh_atual", new Timestamp(System.currentTimeMillis()));
			JapeSessionContext.putProperty("d_atual", new Timestamp(TimeUtils.getToday()));
			JapeSessionContext.putProperty("usuarioVO", usuVO);
			JapeSessionContext.putProperty("authInfo", authInfo);
			
			ImportDealAPI.deals(token);
			
		} catch (Throwable ex) {
			contexto.info(dateFormat.format(new Date()) + " [onTime] Falha na execução: " + ex.toString() + "\n");
			logger.log(Level.INFO, "[onTime] {0}", ex);
			ex.printStackTrace();
		} finally {
			
			try {
				Timestamp dhatual = (Timestamp) JapeSessionContext.getProperty("dh_atual");
				
				EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
				PersistentLocalEntity parcelaDestEntity = dwfFacade.findEntityByPrimaryKey("AD_CTRLEXECUCAOCRM",
						new Object[] { new BigDecimal(1)  });
				DynamicVO parcelaVO = (DynamicVO) parcelaDestEntity.getValueObject();
				
				parcelaVO.setProperty("DTULTIMAEXECUCAO", dhatual);
				
				parcelaDestEntity.setValueObject((EntityVO) parcelaVO);
				
				UtilIntegracao.salvaLogIntegracao("Rotina executada com sucesso.", "deals");
			} catch (Exception e) {
				logger.log(Level.INFO, "[onTime] " +  e.getMessage());
				contexto.info(dateFormat.format(new Date()) + " [onTime] \n" + e.getMessage());
			}
			
			JapeSession.close(hnd);
			sctx.unregistry();
			AuthenticationInfo.unregistry();
		
			logger.log(Level.INFO, "[onTime] Rotina executada com sucesso!");
			contexto.info(dateFormat.format(new Date()) + " [onTime] Rotina executada com sucesso!\n");
		}
	}
}
