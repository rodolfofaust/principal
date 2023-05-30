package br.com.sankhya.ctba.integracaoapi;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.bmp.PersistentLocalEntity;
import br.com.sankhya.jape.util.JapeSessionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;



public class BtnImportarOportunidades implements AcaoRotinaJava {
	static final Logger logger = Logger.getLogger(BtnImportarOportunidades.class.getName());
	
	public void doAction(ContextoAcao contextoAcao) throws Exception {
		logger.log(Level.INFO, "[onTime] -------------- Start -------------");
		String token = "63851621c5e07d0016e05752";
		String erro = null;
	
		try {
			ImportDealAPI.deals(token);
			
		} catch (Exception ex) {
			erro = ex.getMessage();
		} finally {
			if (erro != null) {
				contextoAcao.setMensagemRetorno("<b>Erro na Importação!</b>"+"\n"+ erro);
			} else { 
				
				Timestamp dhatual = (Timestamp) JapeSessionContext.getProperty("dh_atual");
				
				EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
				PersistentLocalEntity parcelaDestEntity = dwfFacade.findEntityByPrimaryKey("AD_CTRLEXECUCAOCRM",
						new Object[] { new BigDecimal(1)  });
				DynamicVO parcelaVO = (DynamicVO) parcelaDestEntity.getValueObject();
				
				parcelaVO.setProperty("DTULTIMAEXECUCAO", dhatual);
				
				parcelaDestEntity.setValueObject((EntityVO) parcelaVO);
				
				logger.log(Level.INFO, "[BtnImportarOportunidades] Ultima Exceução: " + dhatual +" Rotina executada com sucesso.");
				UtilIntegracao.salvaLogIntegracao("Rotina executada com sucesso.", "deals");
				contextoAcao.setMensagemRetorno("<b>Importado com Sucesso !</b>");
			}	
		}
		
	}

}
