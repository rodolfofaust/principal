package br.com.sankhya.ctba.neogrid;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.cuckoo.core.ScheduledAction;
import org.cuckoo.core.ScheduledActionContext;

import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.TimeUtils;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.util.JapeSessionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.dwfdata.vo.tsi.UsuarioVO;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.ws.ServiceContext;

public class AcaoAgendadaLeitorArqEDI implements ScheduledAction {
	
	static final BigDecimal EVENTO_LIB = new BigDecimal(65);
	static final String TIPOMOV = "COMPRA";
	static final DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
	static final DateFormat dateFormatEstoque = new SimpleDateFormat("dd/MM/yyyy");

	static final Logger logger = Logger.getLogger(AcaoAgendadaLeitorArqEDI.class.getName());
	
	@SuppressWarnings("removal")
	@Override
	public void onTime(ScheduledActionContext contexto) {
		// TODO Auto-generated method stub
		Timestamp dhatual = (Timestamp) JapeSessionContext.getProperty("dh_atual");
		logger.log(Level.INFO, "[onTime] -------------- Start -------------");
		contexto.info(dhatual + " [EventoAgendadoLiberaCarga] -------------- Start -------------\n");
		
		
        JapeSession.SessionHandle hnd = null;
		final AuthenticationInfo authInfo = new AuthenticationInfo("SUP", BigDecimalUtil.ZERO_VALUE, BigDecimalUtil.ZERO_VALUE, new Integer(Integer.MAX_VALUE));
        authInfo.makeCurrent();
        final ServiceContext sctx = new ServiceContext(null);
        sctx.setAutentication(authInfo);
        sctx.makeCurrent();
        try {
            hnd = JapeSession.open();
            hnd.setCanTimeout(false);
            hnd.setPriorityLevel(JapeSession.LOW_PRIORITY); // em casos de deadlock, esta sessão cai.
            EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
            UsuarioVO usuVO = (UsuarioVO) ((DynamicVO) dwf.findEntityByPrimaryKeyAsVO(DynamicEntityNames.USUARIO, authInfo.getUserID())).wrapInterface(UsuarioVO.class);
            JapeSessionContext.putProperty("usuario_logado", authInfo.getUserID());
            JapeSessionContext.putProperty("emp_usu_logado", usuVO.getCODEMP());
            JapeSessionContext.putProperty("dh_atual", new Timestamp(System.currentTimeMillis()));
            JapeSessionContext.putProperty("d_atual", new Timestamp(TimeUtils.getToday()));
            JapeSessionContext.putProperty("usuarioVO", usuVO);
            JapeSessionContext.putProperty("authInfo", authInfo);		
		
            //this.getNotaNaoLiberada();
            
            //COMPRA
           /// this.leitorArqNeogrid(contexto);
            
        } catch (Throwable ex) {
        	contexto.info(dhatual + " [onTime] Falha na execução: " + ex.toString() + "\n");
        	logger.log(Level.SEVERE, null, ex);
        } finally {
            JapeSession.close(hnd);
            sctx.unregistry();
            AuthenticationInfo.unregistry();
            logger.log(Level.INFO, "[onTime] Rotina executada com sucesso");
            contexto.info(dhatual + " [onTime] Rotina executada com sucesso\n");
        }  
        
	contexto.info(dhatual + " [EventoAgendadoLiberaCarga] -------------- End -------------\n");		
	logger.log(Level.INFO, "[onTime] -------------- End -------------");		
	}
/*
	private void leitorArqNeogrid(ScheduledActionContext contexto) throws Exception {
		logger.log(Level.INFO, "[exportavelRomaneioRecebimento] START");
		contexto.info(dateFormat.format(new Date()) + " [exportavelRomaneioRecebimento] START\n");
		
		DateFormat dtFormat = new SimpleDateFormat("dd/MM/yyyy");
		/////////////////////////////////////////////////////////////////////
//		List<StringBuilder> listFtp = this.buscarArquivoFTP(FTP_PATH_RETORNOS, contexto);	
//		List<StringBuilder> listFtp = this.buscarArquivo();
		//List<List<StringBuilder>> arrayListFtp = this.buscarArquivoDir(this.getRepositorio() + REPOSITORIO_RECEBER, contexto);
		/////////////////////////////////////////////////////////////////////			
		logger.log(Level.INFO, "[exportavelRomaneioRecebimento] Lista de arquivos: {0}", arrayListFtp.size());
		contexto.info(dateFormat.format(new Date()) + " [exportavelRomaneioRecebimento] Lista de arquivos: " + arrayListFtp.size() + "\n");
		
		for (List<StringBuilder> sbList: arrayListFtp) {
			List<StringBuilder> listFtp = sbList;
			List<ItemArquivo> listArq = new ArrayList<>();
			List<List<ItemArquivo>> listListArq = new ArrayList<>();			
			logger.log(Level.INFO, "[exportavelRomaneioSeparacao] ############ Novo arquivo ############");
			
			for (StringBuilder sb: listFtp) {
				String filePath = "";
				BigDecimal nuNotaTemp = BigDecimal.ZERO; 
				Scanner scan = new Scanner(sb.toString()); 
				while (scan.hasNextLine()) { 
					String linha = scan.nextLine(); 
					linha = linha.replace(";;", ";0;");
					if (linha.startsWith(";")) {
						linha = "0" + linha;
					}
					if (linha.endsWith(";")) {
						linha = linha + "0";
					}
					
					logger.log(Level.INFO, "[exportavelRomaneioRecebimento] linha: {0}", linha);
					contexto.info(dateFormat.format(new Date()) + " [exportavelRomaneioRecebimento] linha: " + linha + "\n");
					if (linha.startsWith("0;/home")) {
						continue;
					}
					
					try {
						String[] produto = linha.split(";");
						String cnpj = null;
						if (!produto[0].equals("0")) { 	
							cnpj = produto[0].trim();
						}
						
						//Alterado 23/092021 para NUMNOTA - Solicitação do GP Felipe G. de Oliveira
						BigDecimal nuNota = new BigDecimal(produto[1]);
						
						String codBarra = null;
						if (!produto[2].equals("0")) { 	
							codBarra = produto[2].trim();
						}
						
						BigDecimal codProd = null;
						if (!produto[3].equals("0")) {
							if (produto[3].contains("-")) {
								String[] str = produto[3].split("-");
								produto[3] = str[0];
							}
							codProd = new BigDecimal(produto[3]);
						}
						
						BigDecimal pesoLiq = null;
						if (!produto[4].equals("0")) {
							pesoLiq = new BigDecimal(produto[4].replace(",", "."));
						}
	
						Timestamp dtFabricacao = null;
						if (!produto[5].equals("0")) {
							Date date = dtFormat.parse(produto[5]);
							dtFabricacao = new Timestamp(date.getTime());
						}
						
						Timestamp dtValidade = null;
						if (!produto[6].equals("0")) {
							Date date = dtFormat.parse(produto[6]);
							dtValidade = new Timestamp(date.getTime());
						}
						
						String rgEmbalagem = null;
						if (!produto[7].equals("0")) { 
							rgEmbalagem = produto[7].trim();
						}
						
						if (!produto[produto.length - 1].equals("0")) { 
							filePath = produto[produto.length - 1].trim();
						}					
						
						if ((nuNotaTemp.compareTo(BigDecimal.ZERO) != 0) && (nuNotaTemp.compareTo(nuNota)) != 0) {
							listListArq.add(listArq);
							listArq = new ArrayList<>();
						}
						
						//ItemArquivo arq = new ItemArquivo(cnpj, nuNota, null, codBarra, codProd, pesoLiq, dtFabricacao, dtValidade, rgEmbalagem, filePath, null);
						ItemArquivo arq = new ItemArquivo(cnpj, nuNota, null, codBarra, codProd, pesoLiq, dtFabricacao, dtValidade, rgEmbalagem, filePath);
						listArq.add(arq);						
						nuNotaTemp = nuNota;
					} catch (Exception ex) {
						String erro = "Erro na leitura do arquivo: " + filePath + "\n" + ex.getCause();
						this.insertLog(null, nuNotaTemp, erro);
						logger.log(Level.SEVERE, null, ex);
						contexto.info(dateFormat.format(new Date()) + " [exportavelRomaneioRecebimento] Exception: " + ex + "\n");
					}
				}
				listListArq.add(listArq);
			}
			
			this.criarEntrada(listListArq);
		}
		logger.log(Level.INFO, "[exportavelRomaneioRecebimento] END");
		contexto.info(dateFormat.format(new Date()) + " [exportavelRomaneioRecebimento] END\n");
	}
	
	private List<List<StringBuilder>> buscarArquivoDir(String path, ScheduledActionContext contexto) throws Exception {
		logger.log(Level.INFO, "[buscarArquivoDir] path: {0}", path);
		contexto.info(dateFormat.format(new Date()) + " [buscarArquivoDir] path: " + path + "\n");
		List<StringBuilder> list = new ArrayList<>();
		List<List<StringBuilder>> arraylist = new ArrayList<>();

		File folder = new File(path);
		for (File file : folder.listFiles()) {
			if (!file.isDirectory()) {
				if (!file.getName().contains("-OK") && !file.getName().contains("-PROCESSANDO") && !file.getName().contains("-ERRO")) {
					logger.log(Level.INFO, "[buscarArquivoDir] File name: {0}", path + file.getName());
					contexto.info(dateFormat.format(new Date()) + " [buscarArquivoDir] File name: " + path + file.getName() + "\n");

					BufferedReader reader = new BufferedReader(new FileReader (path + file.getName()));
	
					StringBuilder sb = new StringBuilder();
					String inline = "";
					while ((inline = reader.readLine()) != null) {
						sb.append(inline + ";" + path + file.getName()
						.toUpperCase()
						.replace(" - ", "-")
						.replace(" ", "-")
						.replace(".txt", "-PROCESSANDO.txt") 
						.replace(".TXT", "-PROCESSANDO.TXT") + "\n");
					}
					list.add(sb);
					reader.close();
					
					this.renomearArquivo(path, file);
					list = this.ordenarLista(list);
					
					arraylist.add(list);
					list = new ArrayList<>();
				}
			}
		}
		logger.log(Level.INFO, "[buscarArquivoDir] File list size: {0}", arraylist.size());
		return arraylist;
	}	
	*/
}
