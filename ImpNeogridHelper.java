package br.com.sankhya.ctba.neogrid;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.EntityPrimaryKey;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.util.JapeSessionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.jape.vo.PrePersistEntityState;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.comercial.BarramentoRegra;
import br.com.sankhya.modelcore.comercial.centrais.CACHelper;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.modelcore.util.MGECoreParameter;

/*
@author: Rodolfo Faust - Sankhya Curitiba
@date: 04/05/2023

Objetivo: Classes helper 
Versões: 
	1.0 - importacao Neogrid 
	Cliente: FontLife

 */


public class ImpNeogridHelper {
	static final Logger logger = Logger.getLogger(ImpNeogridHelper.class.getName());

	public ImpNeogridHelper() {

	}

	public static DynamicVO getTipoOperacao(BigDecimal codTipOper) throws Exception {
		logger.log(Level.INFO, "[getTipoOperacao] codTipOper: "+codTipOper);

		JapeWrapper topDAO = JapeFactory.dao("TipoOperacao");
		DynamicVO topVO = (DynamicVO) topDAO.findOne("this.CODTIPOPER = ? AND this.DHALTER = (SELECT MAX(DHALTER) FROM TGFTOP WHERE CODTIPOPER = ?) ",
				new Object[] { codTipOper, codTipOper });

		return topVO;
	}

	public static BigDecimal getProduto(String linha, BigDecimal codParc) throws Exception {
		logger.log(Level.INFO, "[getProduto] CODPARC: "+codParc+", LINHA: "+linha);

		BigDecimal codProd = null, qtd = null;
		if ((!linha.substring(71,91).trim().equals(""))) {
			if (!linha.substring(71,91).trim().matches("[[0-9]+]*")) {
				codProd = converteEmBigDecimal(linha.substring(71,91).trim()); // exclui caracteres nao numericos
			} else {
				codProd = new BigDecimal(linha.substring(71,91).trim()); //codprod
			}
		}

		if (codProd != null) {
			qtd = NativeSql.getBigDecimal("COUNT(*)", "TGFPRO", "CODPROD = ? ", new Object[] { codProd });			
		}

		if (codProd == null || qtd == null || qtd.intValue() == 0) {
			logger.log(Level.INFO, "[getProduto] Não existe o codprod "+codProd+". Fazer consulta pelo EAN/Referencia.");
			codProd = ImpNeogridHelper.getProdutoEAN(linha.substring(17,31).trim(), codParc); //referencia (EAN)
		}

		logger.log(Level.INFO, "[getProduto] CODPROD: "+codProd);
		return codProd;
	}

	private static BigDecimal converteEmBigDecimal(String ref_prod) throws Exception {
		String numeroStr = "";
		BigDecimal numeroBig = BigDecimal.ZERO;

		for ( int i = 0; i < ref_prod.length(); i++ ) {

			logger.log(Level.INFO, "i :" +i);
			// verifica se o char não é um dígito
			if (Character.isDigit(ref_prod.charAt(i))) {
				numeroStr = numeroStr+ref_prod.charAt(i); //concatena somente o que for numero
			}
		}

		if (!numeroStr.equals("") || numeroStr != null)
			numeroBig = new BigDecimal(numeroStr.trim());

		return numeroBig;
	}


	public static BigDecimal getProdutoEAN(String ref_prod, BigDecimal codParc) throws Exception {
		logger.log(Level.INFO, "******getProdutoEAN - INICIO*******");

		BigDecimal codProd = BigDecimal.ZERO;
		JdbcWrapper jdbc = null;
		try {
			jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
			NativeSql sql = new NativeSql(jdbc);
			sql.setNamedParameter("CODIGOEAN", ref_prod);
			sql.setNamedParameter("PARCEIRO", codParc);

			sql.appendSql(" SELECT PRO.CODPROD" );
			sql.appendSql(" FROM TGFTAB TAB " );
			sql.appendSql(" JOIN TGFEXC EXC ON EXC.NUTAB   = TAB.NUTAB" );
			sql.appendSql(" JOIN TGFPRO PRO ON PRO.CODPROD = EXC.CODPROD" );
			sql.appendSql(" JOIN TGFPAR PAR ON PAR.CODTAB  = TAB.CODTAB" );
			sql.appendSql(" WHERE PAR.CODPARC  = :PARCEIRO" );
			sql.appendSql(" AND PRO.REFERENCIA = :CODIGOEAN" );
			sql.appendSql(" AND TAB.DTVIGOR = (SELECT MAX(TAB2.DTVIGOR) FROM TGFTAB TAB2 WHERE TAB.NUTAB = TAB2.NUTAB)" );


			ResultSet rs = sql.executeQuery();

			if (rs.next()) 
				codProd = rs.getBigDecimal("CODPROD");					
		} finally {
			jdbc.closeSession();
		}

		logger.log(Level.INFO, "CODPROD: "+codProd+"\n******getProdutoEAN - FIM*******");

		return codProd;
	}


	public static boolean validaEstoqueProduto(BigDecimal codProd, BigDecimal qtdneg) throws Exception {
		logger.log(Level.INFO, "[validaEstoqueProduto] codProd: "+codProd);
		BigDecimal estoque = BigDecimal.ZERO;

		JdbcWrapper jdbc = null;
		try {
			jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
			NativeSql sql = new NativeSql(jdbc);
			sql.setNamedParameter("PRODUTO", codProd);

			sql.appendSql(" SELECT ROUND(ESTOQUE - RESERVADO,2) AS DISPONIVEL " );
			sql.appendSql(" FROM TGFEST E " );
			sql.appendSql(" WHERE E.CODPROD = :PRODUTO" );
			sql.appendSql(" AND E.CODLOCAL = (SELECT P.CODLOCALPADRAO FROM TGFPRO P WHERE P.CODPROD = :PRODUTO)" );
			logger.log(Level.INFO, "[validaEstoqueProduto] sqlString: {0}", new Object[] { sql.toString() });

			ResultSet rs = sql.executeQuery();

			if (rs.next()) {
				estoque = rs.getBigDecimal("DISPONIVEL");
			}

		} finally {
			jdbc.closeSession();
		}

		boolean valida = estoque.compareTo(qtdneg) > -1 ? true : false;
		logger.log(Level.INFO, "[validaEstoqueProduto] valida: "+valida+", estoque: "+estoque+", qtdneg: "+qtdneg);
		return valida;
	}

	public static void insertLog(BigDecimal nuNota, BigDecimal numNota, String nomeArquivo, String log) throws Exception {
		Timestamp dtalteracao = (Timestamp) JapeSessionContext.getProperty("dh_atual");

		//Busca a tabela a ser inserida, com base na instância
		EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
		DynamicVO logVO;

		logVO = (DynamicVO) dwfEntityFacade.getDefaultValueObjectInstance("AD_LOGIMPORTACAO");
		//inclui os valores desejados nos campos
		logVO.setProperty("NUNOTA", nuNota);
		logVO.setProperty("NUMNOTA", numNota);
		logVO.setProperty("LOG", log.trim());
		logVO.setProperty("DTALTERACAO", dtalteracao);
		logVO.setProperty("TIPOMOV", "VENDA");
		logVO.setProperty("ARQUIVO", nomeArquivo);

		//realiza o insert
		dwfEntityFacade.createEntity("AD_LOGIMPORTACAO", (EntityVO) logVO);

		//captura a chave primaria criada após o insert	        		
		BigDecimal result = (BigDecimal) logVO.getProperty("CODLOG");

		logger.log(Level.INFO, "[insertLog] nuNota: {0}, numNota: {1}, log: {2}, dtalteracao: {3}, codlog: {4}", 
				new Object[] { nuNota, numNota, log, dtalteracao, result });
	}

	public static String getRepositorio() throws Exception {
		String dir = (String) MGECoreParameter.getParameter("FREPBASEFOLDER"); // /home/baggio/

		if (dir == null) {
			dir = ".sw_file_repository";
		} else {
			dir = dir.replace("\n", "");
		}

		if (dir.charAt(dir.length() - 1) != '/') {
			dir = dir + "/";
		}
		logger.log(Level.INFO, "[getRepositorio] dir: {0}", dir); // /home/baggio/
		return dir;
	}


	public static BigDecimal criaMovimentoPortalVendas(BigDecimal valor_desconto_total, BigDecimal valor_total_ipi, 
			BigDecimal valor_total_pedido, String numero_pedido_comprador, BigDecimal codParc, String nomeArquivo, String ciffob, BigDecimal codVend) throws Exception {

		logger.log(Level.INFO, "******criaMovimentoPortalVendas - INICIO*******");

		//VARIAVEIS
		BigDecimal codusu = (BigDecimal) JapeSessionContext.getProperty("usuario_logado");
		BigDecimal codTipOper = (BigDecimal) MGECoreParameter.getParameter("TOPVENDAARQUTXT");
		Timestamp dhatual  = (Timestamp) JapeSessionContext.getProperty("dh_atual");

		DynamicVO topVO = ImpNeogridHelper.getTipoOperacao(codTipOper);

		logger.log(Level.INFO, "valor_desconto_total: "+valor_desconto_total
				+ ", valor_total_ipi: "+valor_total_ipi
				+ ", valor_total_pedido: "+valor_total_pedido
				+ ", numero_pedido_comprador: "+numero_pedido_comprador
				+ ", TOP: "+codTipOper
				+ ", codParc: "+codParc);


		EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
		AuthenticationInfo authInfo = AuthenticationInfo.getCurrent();
		CACHelper cacHelper = new CACHelper();

		DynamicVO cabVOnova = (DynamicVO) dwfEntityFacade.getDefaultValueObjectInstance("CabecalhoNota");

		cabVOnova.setProperty("CODTIPOPER", codTipOper);
		cabVOnova.setProperty("DTNEG", dhatual);
		//cabVOnova.setProperty("DHTIPOPER", getDhAlter(codTipOper));
		cabVOnova.setProperty("DHTIPOPER", topVO.asTimestamp("DHALTER"));
		cabVOnova.setProperty("CODCENCUS", topVO.asBigDecimal("AD_CODCENCUS"));
		cabVOnova.setProperty("CODNAT", topVO.asBigDecimal("AD_CODNAT"));
		cabVOnova.setProperty("VLRNOTA", valor_total_pedido);
		cabVOnova.setProperty("CODUSU", codusu);
		cabVOnova.setProperty("CODEMP", BigDecimal.ONE); //BAGGIO & BAGGIO LTDA
		cabVOnova.setProperty("CODPARC", codParc);
		cabVOnova.setProperty("COMISSAO", BigDecimal.ZERO);
		cabVOnova.setProperty("VLRDESCTOT", valor_desconto_total);
		cabVOnova.setProperty("VLRIPI", valor_total_ipi	);
		cabVOnova.setProperty("PENDENTE", "P");
		cabVOnova.setProperty("APROVADO", "S");
		cabVOnova.setProperty("STATUSNOTA", "L");
		cabVOnova.setProperty("NUMPEDIDO2", numero_pedido_comprador);
		cabVOnova.setProperty("TIPMOV", "P");
		cabVOnova.setProperty("CIF_FOB", ciffob);
		cabVOnova.setProperty("CODVEND", codVend);
		cabVOnova.setProperty("OBSERVACAO", "PEDIDO DE COMPRA - "+nomeArquivo);

		PrePersistEntityState cabState = PrePersistEntityState.build(dwfEntityFacade, "CabecalhoNota", cabVOnova);
		BarramentoRegra bRegrasCab = cacHelper.incluirAlterarCabecalho(authInfo, cabState);
		Collection<EntityPrimaryKey> pk = bRegrasCab.getDadosBarramento().getPksEnvolvidas();

		if (pk.isEmpty())
			throw new MGEModelException("Não foi possível gerar o movimento no portal");
		BigDecimal nunota = (BigDecimal) pk.iterator().next().getValues()[0];

		logger.log(Level.INFO, "******criaMovimentoPortalVendas - FIM*******");

		return nunota;
	}

	public static void criaItensNaNota(BigDecimal numero_sequencial, BigDecimal qtde_pedida, BigDecimal preco_bruto_unitario,
			BigDecimal valor_unitario_desc_comercial, BigDecimal percentual_desc_comercial, BigDecimal percentual_ipi_produto, BigDecimal valor_unitario_ipi,
			BigDecimal nunota, BigDecimal codProd) throws Exception {

		logger.log(Level.INFO, "******criaItensNaNota - INICIO*******");

		//VARIAVEIS
		String codVol = NativeSql.getString("CODVOL", "TGFPRO", "CODPROD = ? ", new Object[] { codProd });
		BigDecimal codusu  = (BigDecimal) JapeSessionContext.getProperty("usuario_logado");
		Timestamp dhatual  = (Timestamp) JapeSessionContext.getProperty("dh_atual");

		logger.log(Level.INFO, "numero_sequencial: "+numero_sequencial
				+ ", qtde_pedida: "+qtde_pedida
				+ ", preco_bruto_unitario: "+preco_bruto_unitario
				+ ", valor_unitario_desc_comercial: "+valor_unitario_desc_comercial
				+ ", percentual_desc_comercial: "+percentual_desc_comercial
				+ ", percentual_ipi_produto: "+percentual_ipi_produto
				+ ", valor_unitario_ipi: "+valor_unitario_ipi
				+ ", referencia_produto: "+codProd
				+ ", codVol: "+codVol
				+ ", nunota: "+nunota);

		//Busca a tabela a ser inserida, com base na instância
		EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
		DynamicVO iteVO;

		iteVO = (DynamicVO) dwfEntityFacade.getDefaultValueObjectInstance("ItemNota");

		//inclui os valores desejados nos campos
		iteVO.setProperty("SEQUENCIA", numero_sequencial);
		iteVO.setProperty("CODEMP", BigDecimal.ONE); //BAGGIO & BAGGIO LTDA
		iteVO.setProperty("CODPROD", codProd);
		iteVO.setProperty("QTDNEG", qtde_pedida.setScale(2));
		iteVO.setProperty("VLRUNIT", preco_bruto_unitario.setScale(2));
		iteVO.setProperty("VLRTOT", preco_bruto_unitario.multiply(qtde_pedida).setScale(2));
		iteVO.setProperty("VLRDESC", valor_unitario_desc_comercial.setScale(2));
		iteVO.setProperty("PERCDESC", percentual_desc_comercial.setScale(2));
		iteVO.setProperty("CODVOL", codVol);
		iteVO.setProperty("VLRIPI", valor_unitario_ipi.setScale(2));
		iteVO.setProperty("CODUSU", codusu);
		iteVO.setProperty("DTALTER", dhatual);
		iteVO.setProperty("ALIQIPI", percentual_ipi_produto.setScale(2));

		AuthenticationInfo authInfo = AuthenticationInfo.getCurrent();
		CACHelper helper = new CACHelper();
		Collection<PrePersistEntityState> itensStateColl = new ArrayList<PrePersistEntityState>();

		itensStateColl.add(PrePersistEntityState.build(dwfEntityFacade, "ItemNota", iteVO));
		JapeSessionContext.putProperty("br.com.sankhya.com.CentralCompraVenda", Boolean.valueOf(true));
		helper.incluirAlterarItem(nunota, authInfo, itensStateColl, false);
		JapeSessionContext.putProperty("br.com.sankhya.com.CentralCompraVenda", Boolean.valueOf(false));

		logger.log(Level.INFO, "******criaItensNaNota - FIM*******");
	}

}
