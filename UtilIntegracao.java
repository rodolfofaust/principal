package br.com.sankhya.ctba.integracaoapi;


import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collection;
import java.util.InputMismatchException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sankhya.util.StringUtils;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.bmp.PersistentLocalEntity;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.core.JapeSession.SessionHandle;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.util.JapeSessionContext;
import br.com.sankhya.jape.util.ProcedureCaller;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.modelcore.util.MGECoreParameter;

/*
@author: Rodolfo Faust - Sankhya Curitiba
@date: 02/12/2022

Objetivo: Classes utilitarias
Versões: 
	1.0 - Implementacao das rotinas API Integracao CRM Index FLorestal - RD Station	 
 */

public class UtilIntegracao {
	final static String  token ="63851621c5e07d0016e05752";
	static final Logger logger = Logger.getLogger(UtilIntegracao.class.getName());
	public UtilIntegracao() {

	}

	public static void ProcessarDeal(Deal deal, Vendedor vendedor, Product prod, BigDecimal numOSOld, String token2,
			List<Contact> contact, Organization org) 	 throws Exception {

		logger.log(Level.INFO, deal.toString());
		logger.log(Level.INFO, vendedor.toString());
		logger.log(Level.INFO, prod.toString());
		logger.log(Level.INFO, org.toString());
		logger.log(Level.INFO, numOSOld.toString());
		
		logger.log(Level.INFO, "[ProcessarDeal]***************************************** INICIO *****************************************");
		logger.log(Level.INFO, "[ProcessarDeal] Cliente: " + org.getIdClient()   + " - nameClient: " + org.getNameClient());
		logger.log(Level.INFO, "[ProcessarDeal] idDeal: " + deal.getIdDeal()  + " naDeal: " + deal.getNameDeal() );
		logger.log(Level.INFO, "[ProcessarDeal] Vendedor: " +  vendedor.getIdVend() + " - nameVend: " + vendedor.getNameVend() );
		logger.log(Level.INFO,	"[ProcessarDeal]***************************************** INICIO *****************************************");
		
		BigDecimal codCenCus= null;
		if ((prod.getIdProd() != null) && (!prod.getIdProd().equals(""))) {
			codCenCus = NativeSql.getBigDecimal("AD_CODCENCUS", "TGFPRO", "AD_CODPRODCRM = ? ",	new Object[] { prod.getIdProd()  });
		}
		codCenCus = (codCenCus != null ? codCenCus : BigDecimal.ZERO);

		  vendedor = buscarVendedor(vendedor );


		if (numOSOld.intValue() > 0) {
			atualizarProspect(deal, vendedor, contact, org, numOSOld, codCenCus);

		} else {
			if (deal.getDeal_stage().equals("N") || deal.getDeal_stage().equals("DDEEB")) {
				
				insereProspect( contact, org, deal, token,  codCenCus, vendedor);
			}
		}

		logger.log(Level.INFO, "[ProcessarDeal] FIM");
	}

	private static void insereProspect(List<Contact> contact, Organization org, Deal deal, String token2,
			BigDecimal codCenCus, Vendedor vendedor) throws Exception {
		
		
	logger.log(Level.INFO, "[insereProspect] ");
		
		BigDecimal codParc;
		BigDecimal codPap;
		String cnpj;
		cnpj = ImportClientesAPI.organizations(token, org.getNameClient()  );
		logger.log(Level.INFO, "[insereProspect] cnpj " + cnpj);

		if (cnpj == null) {
			String mensagem = "Não foi possível inserir o prospect, CNPJ não encontrado no cadastro do RD Stations, favor verificar! \n Nome: " +  org.getNameClient()  + " Cliente: " + org.idClient   + " \n idDeal: " + "\n nameDeal: " + deal.getNameDeal() + "\n idDeal: " + deal.getIdDeal() +  " \n CNPJ: " + cnpj;
			logger.log(Level.INFO, "[ProcessarDeal] " + mensagem);
			salvaLogIntegracao(mensagem, "");
			return;
		}
		
		codParc = buscarParceiro( org.getIdClient()  , cnpj, org.getNameClient() ,deal.getNameDeal()  , deal.getIdDeal() );

		// cnpj -> verificar se ja tem esse CNPJ cadastrado, se sim, só atualiza a
		// negociação, não cria um novo Prospect...
		// buscar cnpj da tgfpar se não tiver no CRM RD Station
		String solucao = "";
		solucao = ImportActivities.activities(token, deal.getIdDeal());
		solucao += ImportTasks.tasks(token, deal.getIdDeal()) ;
		codPap = existeParcProspect( org.getIdClient() );
		if (codPap == null) {

//			if (telContato != null) {
//				telContato = ajustaNumero(telContato);
//			}

			codPap = insertParcProspect(deal, org, cnpj, vendedor.getCodvend());
			
		}
		insertContatoProspect(codPap, contact);
		insertOrdemServico(codPap, codParc, solucao, codCenCus, deal,  vendedor);
		
		
		
	}

	private static void atualizarProspect(Deal deal, Vendedor vendedor, List<Contact> contact, Organization org, BigDecimal numOSOld, BigDecimal codCenCus) throws Exception {
		
	logger.log(Level.INFO, "[atualizarProspect] ");
		
		BigDecimal codParc;
		BigDecimal codPap;
		if (deal.getNameDeal().length() > 40) {
			deal.setNameDeal(deal.getNameDeal().substring(0, 40));
		}
		codParc = NativeSql.getBigDecimal("CODPARC", "TGFPAR", "AD_CODPARCCRM = ?  ",	new Object[] {org.getIdClient() });
		
		if (codParc == null) {
			String mensagem = "Parceiro não encontrado no cadastro do Sankhya, favor verificar! \n Nome: " + org.getNameClient()+ " Cliente: " + org.getIdClient() + "\n nameDeal: " + deal.getNameDeal()    + " \n idDeal: " +  deal.getIdDeal()  ;
			logger.log(Level.INFO, "[ProcessarDeal] " + mensagem);
			salvaLogIntegracao(mensagem, "");
			return;
		}

		updateOrdemServico(numOSOld,  codParc, vendedor, deal);
		
		String nome = "'%" +  org.getNameClient() + "%'";
		codPap = NativeSql.getBigDecimal("CODPAP", "TCSPAP", "AD_CODPARCCRM = ? OR NOMEPAP LIKE ? ", new Object[] { org.getIdClient(), nome });
		
		if (codPap != null) {
			updateContato(codPap, contact);
		} else {
			String mensagem = " CodPap não encontrado na tabela TCSPAP, favor verificar! \n Nome: " + org.getNameClient()
					+ " idDeal: " + org.getIdClient();
			logger.log(Level.INFO, "[ProcessarDeal] " + mensagem);
			salvaLogIntegracao(mensagem, "");
		}
	}

	private static void updateOrdemServico(BigDecimal numOSOld, BigDecimal codParc, Vendedor vendedor, Deal deal) throws Exception {
		logger.log(Level.INFO, "[updateOrdemServico] INICIO");
		logger.log(Level.INFO, "[updateOrdemServico] CODPAP: " + numOSOld + "\nDEAL_STAGE: " + deal.getDeal_stage() );
		Boolean flagAtualizar = false;

		// Busca o registro pela PK
		EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
		PersistentLocalEntity ordServEntity = dwfFacade.findEntityByPrimaryKey("OrdemServico",
				new Object[] { numOSOld }); // TCSOSE - Negociacao
		DynamicVO ordServVO = (DynamicVO) ordServEntity.getValueObject();

		if ( deal.getDeal_stage().equals("C")) {
			ordServVO.setProperty("SITUACAO", "F");
			flagAtualizar = true;
		}
		if (ordServVO.asBigDecimal("CODVEND") != vendedor.getCodvend()) {
			ordServVO.setProperty("CODVEND", vendedor.getCodvend());
			flagAtualizar = true;
		}
		if (!ordServVO.asString("IDENTIFICADOR").equals(deal.getNameDeal())) {
			ordServVO.setProperty("IDENTIFICADOR", deal.getNameDeal());
			flagAtualizar = true;
		}

		if (ordServVO.asBigDecimal("CODPARC") != codParc) {
			ordServVO.setProperty("CODPARC", codParc);
			flagAtualizar = true;
		}

		if (flagAtualizar) {
			ordServEntity.setValueObject((EntityVO) ordServVO);
		}

		if (!deal.getDeal_stage().equals("C")) {
			logger.log(Level.INFO, "[updateOrdemServico]  ATUALIZA");

			BigDecimal numOS = NativeSql.getBigDecimal("NUMOS", "TCSOSE", "AD_IDDEALCRM = ? ", new Object[] {deal.getIdDeal()});
	
			String solucao = "";
			solucao = ImportActivities.activities(token, deal.getIdDeal());
			solucao += ImportTasks.tasks(token, deal.getIdDeal());
			if (!ExisteSubOs(numOS, deal.getRatingDeal() , StringUtils.convertToBigDecimal( deal.getVlrTotDeal()) , deal.getDeal_stage() , solucao)) {
				Timestamp dhatual = (Timestamp) JapeSessionContext.getProperty("dh_atual");
				insertSubOS(numOS, vendedor.getCodusu(), deal.getRatingDeal() , dhatual,  StringUtils.convertToBigDecimal(deal.getVlrTotDeal()), deal.getDeal_stage(), solucao);
			}
		}


		logger.log(Level.INFO, "[updateOrdemServico] FIM");
		
	}

	private static void updateContato(BigDecimal codPap, List<Contact> cont) throws Exception {
		
		if(!existeContato(codPap, cont)) {
			insertContatoProspect(codPap, cont);
		}
	}

	private static boolean existeContato(BigDecimal codPap, List<Contact> cont) throws Exception {
		logger.log(Level.INFO, "[existeContato] INICIO ");

		String title = null, name = null, email = null, telefone = null, celular = null;

		for (Contact contato : cont) {
			title = contato.getTitle() == null ? null : contato.getTitle();
			name = contato.getName() == null ? null : contato.getName();
			email = contato.getEmail() == null ? null : contato.getEmail();

			List<Phone> phones = contato.getPhones();
			for (Phone phone : phones) {
				if (phone.getType() != null && phone.getType().equals("cellphone")) {
					celular = phone.getPhone();
				} else if (phone.getType() != null && phone.getType().equals("work")) {
					telefone = phone.getPhone();
				}
			}
			
			logger.log(Level.INFO, "[existeContato] CODPAP = " + codPap + " NOMECONTATO = " + name + " CARGO = " + title + " EMAIL = " + email + " TELEFONE = " + telefone + " CELULAR = " + celular );

			Boolean existeContact = validarContato(codPap , name , title , email , telefone , celular);

			if(!existeContact) {
				logger.log(Level.INFO, "[existeContato] empty");
				BigDecimal qtd = NativeSql.getBigDecimal("nvl(count(*),0)", "TCSCTT", "CODPAP = ? ",new Object[] { codPap });
				
				if (qtd.intValue() > 0) {
					JapeFactory.dao("ContatoProspect").deleteByCriteria(" CODPAP = ? ", new Object[] { codPap });
					logger.log(Level.INFO, "[existeContato] contato  mudou e foi apagado para inserir novamente");
					return false;
				} else {
					logger.log(Level.INFO, "[existeContato] nao existe contato, vai ser inserido ");
					return false;
				}
			}
		}
		return false;
	}

	private static Boolean validarContato(BigDecimal codPap, String name, String title, String email, String telefone,
			String celular) throws Exception {
		JdbcWrapper jdbc = null;

		try {
			jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();

			NativeSql sql = new NativeSql(jdbc);
			sql.setNamedParameter("CODPAP", codPap);
			sql.setNamedParameter("NOMECONTATO", name);
			sql.setNamedParameter("CARGO", title);
			sql.setNamedParameter("EMAIL", email);
			sql.setNamedParameter("TELEFONE", telefone);
			sql.setNamedParameter("CELULAR", celular);

			sql.appendSql(" SELECT  NVL(COUNT(*),0) AS QTD");
			sql.appendSql("   FROM  TCSCTT");
			sql.appendSql("  WHERE CODPAP = :CODPAP AND NOMECONTATO = :NOMECONTATO  AND CARGO = :CARGO  AND EMAIL = :EMAIL  AND TELEFONE = :TELEFONE  AND CELULAR = :CELULAR  ");

			ResultSet rs = sql.executeQuery();

			while (rs.next()) {
				BigDecimal qtd= rs.getBigDecimal("QTD");
				if(qtd.intValue() > 0) {
					return true;
				}else {
					return false;
				}
			}
		} finally {
			jdbc.closeSession();
		}
		return null;
	}

	
	private static BigDecimal existeParcProspect(String idClient) throws Exception {
		logger.log(Level.INFO, "[existeParcProspect] INICIO");
		
		BigDecimal codPap = NativeSql.getBigDecimal("NVL(CODPAP,0)", "TCSPAP", "AD_CODPARCCRM = ?  ", new Object[] {idClient});
		
		logger.log(Level.INFO, "[existeParcProspect] FIM codPap: " + codPap);
		return codPap;
	}
	public static Vendedor buscarVendedor(Vendedor vendedor) throws Exception {
		logger.log(Level.INFO, "[buscarVendedor] INICIO idVend: " + vendedor.getIdVend() );

		String 	nameVend = "'%" + vendedor.getNameVend() + "%'" ;
		
			JdbcWrapper jdbc = null;

			try {
				jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();

				NativeSql sql = new NativeSql(jdbc);
				sql.setNamedParameter("AD_CODVENDCRM", vendedor.getIdVend() );
				sql.setNamedParameter("APELIDO", nameVend);

				sql.appendSql(" SELECT CODVEND, CODUSU, AD_CODVENDCRM ");
				sql.appendSql(" FROM TGFVEN ");
				sql.appendSql(" WHERE AD_CODVENDCRM = :AD_CODVENDCRM  ");
				sql.appendSql("    OR APELIDO LIKE :APELIDO  ");

				System.out.println("QUERY: " +sql.toString());
				
				ResultSet rs = sql.executeQuery();

				if (rs.next()) {
					logger.log(Level.INFO, "[buscarVendedor] FIM CODVEND: " +rs.getBigDecimal("CODVEND") + " codusu: " + rs.getBigDecimal("CODUSU"));
				
					vendedor.setCodusu( rs.getBigDecimal("CODUSU"));
					vendedor.setCodvend(rs.getBigDecimal("CODVEND"));
					
					if(rs.getString("AD_CODVENDCRM") == null) {
						JapeWrapper cabDAO = JapeFactory.dao("Vendedor");
						cabDAO.prepareToUpdateByPK(rs.getBigDecimal("CODVEND")).set("AD_CODVENDCRM", vendedor.getIdVend() ).update();
					}
					logger.log(Level.INFO, "[buscarVendedor] FIM codVend: " + rs.getBigDecimal("CODVEND"));
					return vendedor;
				}else {
					logger.log(Level.INFO, "[buscarVendedor] inserindo nameVend: " + nameVend + " idVend: " + vendedor.getIdVend());
					if (nameVend.length() > 15)
						nameVend = nameVend.substring(0,15);
					
					EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
					DynamicVO venVO;

					venVO = (DynamicVO) dwfEntityFacade.getDefaultValueObjectInstance("Vendedor"); //TGFVEN
					venVO.setProperty("APELIDO", nameVend);
					venVO.setProperty("AD_CODVENDCRM", vendedor.getIdVend());

					dwfEntityFacade.createEntity("Vendedor", (EntityVO) venVO);

					BigDecimal codVend = (BigDecimal) venVO.getProperty("CODVEND");
					
					vendedor.setCodusu(new BigDecimal(0));
					vendedor.setCodvend(codVend);
					
					
					logger.log(Level.INFO, "[buscarVendedor] FIM codVend: " + codVend);
					return vendedor;
				}

			} finally {
				jdbc.closeSession();
			}

	}

	public static BigDecimal criaParceiro(String idClient, String nameClient, String cnpj) throws Exception {
		logger.log(Level.INFO, "[criaParceiro] INICIO");
		logger.log(Level.INFO, "[criaParceiro] idParceiro: " + idClient + " nameClient: " + nameClient + " cnpj: " + cnpj);
		BigDecimal codparc = BigDecimal.ZERO;
		Timestamp dhatual = (Timestamp) JapeSessionContext.getProperty("dh_atual");

		//Busca a tabela a ser inserida, com base na instância
		EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
		DynamicVO parcVO;

		parcVO = (DynamicVO) dwfEntityFacade.getDefaultValueObjectInstance("Parceiro");

		//inclui os valores desejados nos campos
		parcVO.setProperty("NOMEPARC", nameClient);
		parcVO.setProperty("RAZAOSOCIAL", nameClient);
		parcVO.setProperty("AD_CODPARCCRM", idClient);
		parcVO.setProperty("TIPPESSOA", "J");
		parcVO.setProperty("CLIENTE", "S");
		parcVO.setProperty("DTCAD", dhatual);
		parcVO.setProperty("DTALTER", dhatual);
		parcVO.setProperty("CLASSIFICMS", "C");
		parcVO.setProperty("CGC_CPF", cnpj);
		parcVO.setProperty("CODCID", new BigDecimal(1516));
		parcVO.setProperty("AD_PROSPECT", "S");

		//realiza o insert
		dwfEntityFacade.createEntity("Parceiro", (EntityVO) parcVO);

		//captura a chave primaria criada após o insert	        		
		codparc = (BigDecimal) parcVO.getProperty("CODPARC");
		logger.log(Level.INFO, "[criaParceiro] FIM");
		return codparc;
	}

	private static BigDecimal insertParcProspect(Deal deal, Organization org, String cnpj, BigDecimal codVend 
			) throws Exception {

		logger.log(Level.INFO, "[insertParcProspect] INICIO");
		String nameClient = null;
		if (org.getNameClient().length() > 40)
			 nameClient = org.getNameClient().substring(0,40);

		//Busca a tabela a ser inserida, com base na instância
		EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
		DynamicVO parcProspectVO;

		parcProspectVO = (DynamicVO) dwfEntityFacade.getDefaultValueObjectInstance("ParceiroProspect"); //TCSPAP
		//inclui os valores desejados nos campos
		parcProspectVO.setProperty("NOMEPAP", nameClient);
		parcProspectVO.setProperty("RAZAOSOCIAL", nameClient);
		parcProspectVO.setProperty("CODVEND", codVend);
		parcProspectVO.setProperty("CGC_CPF", cnpj); //CNPJ
		parcProspectVO.setProperty("ENDERECO", null);
		parcProspectVO.setProperty("NUMEND", null);
		parcProspectVO.setProperty("COMPLEMENTO", null);
		parcProspectVO.setProperty("NOMEBAI", null);
		parcProspectVO.setProperty("NOMECID", null);
		parcProspectVO.setProperty("CODUF", null);
		parcProspectVO.setProperty("TIPPESSOA", "J");
		parcProspectVO.setProperty("EMAIL", org.getEmailUser());
		parcProspectVO.setProperty("NATURALIDADE", BigDecimal.ZERO);
		parcProspectVO.setProperty("ISPROPOSTACARTAO", "N");
		parcProspectVO.setProperty("SITCADSEFAZ", "0");
		parcProspectVO.setProperty("AD_CODPARCCRM", org.getIdClient());

		//realiza o insert
		dwfEntityFacade.createEntity("ParceiroProspect", (EntityVO) parcProspectVO);

		//captura a chave primaria criada após o insert	        		
		BigDecimal codPap = (BigDecimal) parcProspectVO.getProperty("CODPAP");
		
		logger.log(Level.INFO, "[insertParcProspect] FIM");
		return codPap;
		
	}

	public static void insertContatoProspect(BigDecimal codPap, List<Contact> cont) throws Exception {
		logger.log(Level.INFO, "[insertContatoProspect] INICIO");
		logger.log(Level.INFO, "[insertContatoProspect] CODPAP: "+codPap);

		for (Contact contato : cont) {
		
			if (contato.getTitle() != null && contato.getTitle().length() > 20)
				contato.setTitle(contato.getTitle().substring(0,20));
	
			if (contato.getName() != null && contato.getName().length() > 40) {
				contato.setName(contato.getName().substring(0,40));
			}
			String celular = null;
			String telefone = null;
			
			BigDecimal zero = new BigDecimal(0);
			BigDecimal codContato = NativeSql.getBigDecimal("NVL(MAX(CODCONTATO),0)", "TCSCTT", "CODCONTATO >= ?  ", new Object[] {zero});
			
			logger.log(Level.INFO, "[insertContatoProspect] codContato: "+codContato);
			
			 List<Phone> phones = contato.getPhones();
			    for (Phone phone : phones) {
			        if(phone.getType() != null && phone.getType().equals("cellphone") ) {
			        	String tel = phone.getPhone().replace("+", "").trim();
			        	if(tel.length() <= 13) {
			        		celular = tel;
			        	}else {
			        		String mensagem = "Nome: " + contato.getName() + "Cargo: " + contato.getTitle() + " Telefone: " + tel +" Telefone com mais que 13 caracteres, favor verificar!";
							salvaLogIntegracao(mensagem, "");
			        	}
			        }else if(phone.getType() != null && phone.getType().equals("work")) {
			        	String tel = phone.getPhone().replace("+", "").trim();
			        	if(tel.length() <= 13) {
			        		telefone = tel;
			        	}else {
							String mensagem = "Nome: " + contato.getName() + "Cargo: " + contato.getTitle() + " Telefone: " + tel +" Telefone com mais que 13 caracteres, favor verificar!";
							salvaLogIntegracao(mensagem, "");
			        	}
			        }
			    }			

			    inserirContato(codPap, contato.getName(),contato.getTitle(), contato.getEmail(),  celular, telefone );
		}
		logger.log(Level.INFO, "[insertContatoProspect] FIM");
	}


	private static void inserirContato(BigDecimal codPap, String name, String title, String email,
			String celular, String telefone) throws Exception {
		logger.log(Level.INFO, "[inserirContato] INICIO");
		logger.log(Level.INFO, "[inserirContato] codPap: " + " codPap: " + " name: " + name + " title: " + title + " email: " + email +
				" celular: " + celular +" telefone: " + telefone);
		
		
		JdbcWrapper jdbc = null;

			ProcedureCaller caller = new ProcedureCaller("AD_INCL_CONTATO");
			caller.addInputParameter(codPap);
			caller.addInputParameter(name);
			caller.addInputParameter(title);
			caller.addInputParameter(email);
			caller.addInputParameter(celular);
			caller.addInputParameter(telefone);

			try {
				jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
				jdbc.openSession();

				caller.execute(jdbc.getConnection());

			} finally {
				jdbc.closeSession();
			}
			
			logger.log(Level.INFO, "[inserirContato] FIM");
	}

	public static void insertOrdemServico(BigDecimal codPap, BigDecimal codParc, String solucao, BigDecimal  codCenCus, Deal deal,  Vendedor vendedor) throws Exception {
		logger.log(Level.INFO, "[insertOrdemServico] INICIO");

		//String situacao = deal_stage.equals("C") ? "F" : "P";
		Timestamp dhatual = (Timestamp) JapeSessionContext.getProperty("dh_atual");
		String nameDeal = null; 
		if (deal.getNameDeal().length() > 50) {
			nameDeal = deal.getNameDeal().substring(0,50);
		}
		

		//Busca a tabela a ser inserida, com base na instância
		EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
		DynamicVO osVO;

		osVO = (DynamicVO) dwfEntityFacade.getDefaultValueObjectInstance("OrdemServico"); //TCSOSE - Negociacao
		//inclui os valores desejados nos campos
		osVO.setProperty("CODPAP", codPap);
		osVO.setProperty("SITUACAO", "P");
		osVO.setProperty("TIPO", "P");
		osVO.setProperty("CODCONTATO", BigDecimal.ONE);
		osVO.setProperty("CODTPN", BigDecimal.ONE);
		osVO.setProperty("CODCENCUS", codCenCus);
		osVO.setProperty("DHCHAMADA", dhatual);
		osVO.setProperty("CODVEND", vendedor.getCodvend());
		osVO.setProperty("CODPARC", codParc);
		osVO.setProperty("IDENTIFICADOR", nameDeal);
		osVO.setProperty("AD_IDDEALCRM", deal.getIdDeal());
		//contProspectVO.setProperty("STATUSNEG", new BigDecimal(4));

		//realiza o insert
		dwfEntityFacade.createEntity("OrdemServico", (EntityVO) osVO);

		//captura a chave primaria criada após o insert	        		
		BigDecimal numOS = (BigDecimal) osVO.getProperty("NUMOS");
		insertSubOS(numOS, vendedor.getCodusu() ,  deal.getRatingDeal(), dhatual, StringUtils.convertToBigDecimal(deal.getVlrTotDeal()) , deal.getDeal_stage(), solucao);

		logger.log(Level.INFO, "[insertOrdemServico] FIM");
	}



	private static boolean ExisteSubOs(BigDecimal numOS, BigDecimal ratingDeal, BigDecimal vlrTot,
			String deal_stage, String solucao) throws Exception {
		
		logger.log(Level.INFO, "[ExisteSubOs]  INICIO");
		
		BigDecimal stage = null;
		
		if (deal_stage.equals("DDEEB")) {
			stage = BigDecimal.ONE;
		}
		else {
			stage = new BigDecimal(2);
		}

		logger.log(Level.INFO, "[ExisteSubOs]  NUMOS = " + numOS + " CODEPV =  " + ratingDeal   + " VLRCOBRADO = " + vlrTot +  " CODOCOROS = " + stage + " solucao: " + solucao);

		
		String nomeInstancia = "ItemOrdemServico"; //Tabela: TCSITE - Item Negociacao
		EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();

		FinderWrapper finderUpd = new FinderWrapper(nomeInstancia,
				"NUMOS = ? AND CODEPV = ? AND VLRCOBRADO = ? AND CODOCOROS = ? AND SOLUCAO = ?");

		// Insere os argumentos caso existam
		finderUpd.setFinderArguments(new Object[] { numOS, ratingDeal, vlrTot, stage, solucao });

		// Realiza a busca na tabela pelos critÃ©rios
		Collection<PersistentLocalEntity> libCollection = dwfFacade.findByDynamicFinder(nomeInstancia, finderUpd);
		// Itera entre os registos encontrados
		if (libCollection.isEmpty()) {
			logger.log(Level.INFO, "[ExisteSubOs] false  FIM");
			return false;
		} else {
			logger.log(Level.INFO, "[ExisteSubOs] true FIM");
			return true;
		}
		
	}

	public static void insertSubOS(BigDecimal numOS, BigDecimal codUsu, BigDecimal RatingDeal, Timestamp dhatual, BigDecimal vlrTot, String deal_stage, String solucao) throws Exception {
		logger.log(Level.INFO, "[insertSubOS] INICIO");
		logger.log(Level.INFO, "[insertSubOS] NUMOS: "+numOS + " codUsu: " + codUsu + " RatingDeal: " + " dhatual: " + dhatual + " vlrTot: " + vlrTot + " deal_stage: " + deal_stage);

		if (codUsu == null) codUsu = BigDecimal.ZERO;
		
		
		BigDecimal sequencia = NativeSql.getBigDecimal("MAX(NUMITEM)", "TCSITE", "NUMOS = ? ", new Object[] { numOS }) ;
		
		sequencia = (sequencia != null ? sequencia.add(new BigDecimal(1)) : BigDecimal.ONE);

		 BigDecimal stage = null;
		 
		//Busca a tabela a ser inserida, com base na instância
		EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
		DynamicVO subOsVO;

		if (deal_stage.equals("DDEEB"))
			stage = BigDecimal.ONE;
		else
			stage = new BigDecimal(2);
		
		//criar CODSERV = 1001	PROSPECTCODSERV  e CODPROD = 1000  PROSPECTCODPROD padão, e configurar por preferencia.
		
		BigDecimal codServ = (BigDecimal) MGECoreParameter.getParameter("PROSPECTCODSERV");
		BigDecimal codProd = (BigDecimal) MGECoreParameter.getParameter("PROSPECTCODPROD");
		
		logger.log(Level.INFO, "[insertSubOS] codServ: "+codServ + " codProd: " + codProd );
		
		if (solucao.length() > 4000)
			solucao = solucao.substring(0, 3999);
		
		try {
			subOsVO = (DynamicVO) dwfEntityFacade.getDefaultValueObjectInstance("ItemOrdemServico"); //TCSITE - Item Negociacao
	
			//inclui os valores desejados nos campos
			subOsVO.setProperty("NUMOS", numOS);
			subOsVO.setProperty("NUMITEM", sequencia);
			subOsVO.setProperty("CODSERV", codServ);
			subOsVO.setProperty("CODPROD", codProd);
			subOsVO.setProperty("CODEPV", RatingDeal);
			subOsVO.setProperty("EXECUTANTE", new BigDecimal(5));
			subOsVO.setProperty("CODUSU", new BigDecimal(5));
			subOsVO.setProperty("DHPREVISTA", dhatual);
			subOsVO.setProperty("TEMPPREVISTO", tempPrevisto());
			subOsVO.setProperty("DTPREVFECHAMENTO", dtPrevFechamento());
			subOsVO.setProperty("VLRCOBRADO", vlrTot);
			subOsVO.setProperty("CODOCOROS", stage);
			subOsVO.setProperty("LIBERADO", "N");
			subOsVO.setProperty("CODSIT", BigDecimal.ZERO);
			subOsVO.setProperty("SOLUCAO", solucao);
	
			//realiza o insert
			dwfEntityFacade.createEntity("ItemOrdemServico", (EntityVO) subOsVO);
		}catch (Exception e) {
			System.out.println(e.getMessage());
		}
		logger.log(Level.INFO, "[insertSubOS] FIM");
	}

	private static BigDecimal buscarParceiro(String idClient, String cnpj, String nameClient, String nameDeal,
			String idDeal) throws Exception {
		logger.log(Level.INFO, "[buscarParceiro] idClient " + idClient + " cnpj: " + cnpj + " nameClient: " + nameClient + " nameDeal: ");

		BigDecimal codParc =null;
		SessionHandle hnd = null;

		try {
			hnd = JapeSession.open();

			JapeWrapper configDao = JapeFactory.dao(DynamicEntityNames.PARCEIRO);
			DynamicVO searchConfig = configDao.findOne("AD_CODPARCCRM = ? OR CGC_CPF = ? ", idClient, cnpj);

			if (searchConfig == null) {
				if (isCNPJ(cnpj)) {
					codParc = criaParceiro(idClient, cnpj, nameClient);
					logger.log(Level.INFO, "[buscarParceiro] codParc " + codParc );
				} else {

					String mensagem = "Código CNPJ inválido, favor verificar!\n Nome: " + nameClient + " Cliente: "
							+ idClient + "\n nameDeal: " + nameDeal + "\n idDeal: " + idDeal + "\n CNPJ: " + cnpj;
					logger.log(Level.INFO, "[ProcessarDeal] " + mensagem);
					
					salvaLogIntegracao(mensagem, "");
				}

			} else {
				logger.log(Level.INFO, "[buscarParceiro] searchConfig " + searchConfig );
				if(searchConfig.asString("AD_CODPARCCRM") == null)
					configDao.prepareToUpdate(searchConfig).set("AD_CODPARCCRM", idClient).update();

			}
		} catch (Exception e) {
			String mensagem = "Código CNPJ inválido, favor verificar!\n Nome: " + nameClient + " Cliente: " + idClient
					+ "\n nameDeal: " + nameDeal + "\n idDeal: " + idDeal + "\n CNPJ: " + cnpj;
			logger.log(Level.INFO, "[ProcessarDeal] " + mensagem + e.toString());
			salvaLogIntegracao(mensagem, "");
		} finally {
			JapeSession.close(hnd);
		}
		return codParc;

	}
	public static Timestamp tempPrevisto() throws Exception {
		Calendar cal = Calendar.getInstance();
		// TEMPPREVISTO -> sempre gerar com data atual e 18:00PM
		//mudar o horário para 18:00:00.000
		cal.set(Calendar.HOUR_OF_DAY, 18);
		cal.set(Calendar.MINUTE, 00);
		cal.set(Calendar.SECOND, 00);
		cal.set(Calendar.MILLISECOND, 000);

		Timestamp dataHora = new Timestamp(cal.getTimeInMillis());
		return dataHora;
	}

	public static Timestamp dtPrevFechamento() throws Exception {
		Calendar cal = Calendar.getInstance();
		// DTPREVFECHAMENTO -> data da criação (DHPREVISTA) + 5 dias
		cal.add(Calendar.DAY_OF_MONTH, +5);

		Timestamp dataHora = new Timestamp(cal.getTimeInMillis());
		return dataHora;
	}


	public static String ajustaNumero(String texto) throws Exception {
		logger.log(Level.INFO, "[ajustaNumero] INICIO");

		String numeroStr = "";
		for ( int i = 0; i < texto.length(); i++ ) {
			if (Character.isDigit(texto.charAt(i)))
				numeroStr = numeroStr+texto.charAt(i); //concatena somente o que for numero
		}
		numeroStr = numeroStr.trim();
		logger.log(Level.INFO, "[ajustaNumero] FIM");

		return numeroStr;
	}

	public static void salvaLogIntegracao(String msg, String json) throws Exception {
		//Busca a tabela a ser inserida, com base na instância
		EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
		Timestamp dhatual = (Timestamp) JapeSessionContext.getProperty("dh_atual");

		String mensagem = null;
		if (msg.length() > 99) {
			mensagem = msg.substring(0,99);
		}else {
			mensagem = msg;
		}

		DynamicVO logVO = (DynamicVO) dwfEntityFacade.getDefaultValueObjectInstance("AD_LOGINTEGRACAOCRM");

		//inclui os valores desejados nos campos
		logVO.setProperty("DATAIMPORT", dhatual);
		logVO.setProperty("MSGCLOB", msg.toCharArray());
		logVO.setProperty("MENSAGEM", mensagem);
		logVO.setProperty("NOMEJSON", json);


		//realiza o insert
		dwfEntityFacade.createEntity("AD_LOGINTEGRACAOCRM", (EntityVO) logVO);
	}
	public static boolean isCNPJ(String CNPJ) {
		
		if(CNPJ == null)
			return false;
		
		logger.log(Level.INFO, "[isCNPJ] INICIO");
		// considera-se erro CNPJ's formados por uma sequencia de numeros iguais
		if (CNPJ.equals("00000000000000") || CNPJ.equals("11111111111111") || CNPJ.equals("22222222222222") || CNPJ.equals("33333333333333") || CNPJ.equals("44444444444444") || CNPJ.equals("55555555555555") || CNPJ.equals("66666666666666") || CNPJ.equals("77777777777777") || CNPJ.equals("88888888888888") || CNPJ.equals("99999999999999") || (CNPJ.length() != 14))
			return (false);

		char dig13, dig14;
		int sm, i, r, num, peso;

		// "try" - protege o código para eventuais erros de conversao de tipo (int)
		try {
			// Calculo do 1o. Digito Verificador
			sm = 0;
			peso = 2;
			for (i = 11; i >= 0; i--) {
				// converte o i-ésimo caractere do CNPJ em um número:
				// por exemplo, transforma o caractere '0' no inteiro 0
				// (48 eh a posição de '0' na tabela ASCII)
				num = (int) (CNPJ.charAt(i) - 48);
				sm = sm + (num * peso);
				peso = peso + 1;
				if (peso == 10)
					peso = 2;
			}

			r = sm % 11;
			if ((r == 0) || (r == 1))
				dig13 = '0';
			else
				dig13 = (char) ((11 - r) + 48);

			// Calculo do 2o. Digito Verificador
			sm = 0;
			peso = 2;
			for (i = 12; i >= 0; i--) {
				num = (int) (CNPJ.charAt(i) - 48);
				sm = sm + (num * peso);
				peso = peso + 1;
				if (peso == 10)
					peso = 2;
			}

			r = sm % 11;
			if ((r == 0) || (r == 1))
				dig14 = '0';
			else
				dig14 = (char) ((11 - r) + 48);

			// Verifica se os dígitos calculados conferem com os dígitos informados.
			if ((dig13 == CNPJ.charAt(12)) && (dig14 == CNPJ.charAt(13))) {
				logger.log(Level.INFO, "[isCNPJ] TRUE FIM");
				return (true);
			}
			else {
				logger.log(Level.INFO, "[isCNPJ] FALSE FIM");
				return (false);
			}
		} catch (InputMismatchException erro) {
			return (false);
		}
	}

}
