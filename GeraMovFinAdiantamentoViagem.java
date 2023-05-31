package br.com.sankhya.ctba.adiantamentoViagens;

import java.math.BigDecimal;
import java.sql.Timestamp;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.bmp.PersistentLocalEntity;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.util.JapeSessionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

public class GeraMovFinAdiantamentoViagem implements AcaoRotinaJava {

	@Override
	public void doAction(ContextoAcao contextoAcao) throws Exception {
		// TODO Auto-generated method stub
		Registro regPai = contextoAcao.getLinhaPai();
		Registro[] registros = contextoAcao.getLinhas();

		for (Registro registro : registros) {
			criaMovimentacaoFinanceira(registro, regPai);
			contextoAcao.setMensagemRetorno("Movimentação Financeria realizada com sucesso!");
		}
	}

	private void criaMovimentacaoFinanceira(Registro registro, Registro regPai) throws Exception {
		// tela mestre
		BigDecimal codParc = (BigDecimal) regPai.getCampo("CODPARC");
		Timestamp dataRetorno = (Timestamp) regPai.getCampo("DATARETORNO");
		BigDecimal numAdViagem = (BigDecimal) regPai.getCampo("NUADIANTVIAGEM");

		// tela detalhe
		BigDecimal numFinAdViag = (BigDecimal) registro.getCampo("NUMFINADVIAGEM"); // Nro Financeiro Adiantamento
																					// Viagem:
		BigDecimal sequencia = (BigDecimal) registro.getCampo("SEQUENCIA"); // Nro Financeiro Adiantamento Viagem:
		BigDecimal codCtaBcoInt = (BigDecimal) registro.getCampo("CODCTABCOINT"); // Conta bancária
		BigDecimal codNat = (BigDecimal) registro.getCampo("CODNAT"); // Natureza
		BigDecimal codTipOper = (BigDecimal) registro.getCampo("CODTIPOPER"); // Tipo Operação
		BigDecimal vlrAdiato = (BigDecimal) registro.getCampo("VLRADIANTAMENTO"); // Valor Adiantamento:
		BigDecimal codCencus = (BigDecimal) registro.getCampo("CODCENCUS"); // Centro de Custo
		BigDecimal codTipTit = (BigDecimal) registro.getCampo("CODTIPTIT"); // Tipo de Título
		Timestamp dataPagto = (Timestamp) registro.getCampo("DATAPAGTO"); // Data Pagto:
		String observacao = (String) registro.getCampo("OBSERVACAO"); // Tipo de Título
		String sentidoPgto = (String) registro.getCampo("SENTIDOPAGTO"); // Receita/Despesa:

		// variaveis sistema e banco
		BigDecimal codusu = (BigDecimal) JapeSessionContext.getProperty("usuario_logado");
		Timestamp dhatual = (Timestamp) JapeSessionContext.getProperty("dh_atual");
		BigDecimal codEmp = NativeSql.getBigDecimal("CODEMP", "TSIUSU", "CODUSU = ?", codusu);
		BigDecimal codBco = NativeSql.getBigDecimal("CODBCO", "TSICTA", "CODCTABCOINT = ?", codCtaBcoInt);

		// Busca a tabela a ser inserida, com base na instância
		EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
		DynamicVO financeiroVO = (DynamicVO) dwfEntityFacade.getDefaultValueObjectInstance("Financeiro");

		// inclui os valores desejados nos campos
		financeiroVO.setProperty("CODPARC", codParc);
		financeiroVO.setProperty("DTVENC", dataRetorno);
		financeiroVO.setProperty("DTNEG", dataPagto);
		financeiroVO.setProperty("CODNAT", codNat);
		financeiroVO.setProperty("CODTIPOPER", codTipOper);
		financeiroVO.setProperty("VLRDESDOB", vlrAdiato);

		if (sentidoPgto.compareTo("C") == 0) {
			financeiroVO.setProperty("RECDESP", new BigDecimal(1));
		} else {
			financeiroVO.setProperty("RECDESP", new BigDecimal(-1));
		}
		// financeiroVO.setProperty("RECDESP", sentidoPgto);
		financeiroVO.setProperty("CODTIPTIT", codTipTit);
		financeiroVO.setProperty("CODCENCUS", codCencus);
		// financeiroVO.setProperty("DHBAIXA", dataPagto);
		financeiroVO.setProperty("HISTORICO", "***ADIANTAMENTO DE VIAGEM*** " + observacao);
		financeiroVO.setProperty("CODBCO", codBco);
		financeiroVO.setProperty("CODEMP", codEmp);
		financeiroVO.setProperty("CODUSU", codusu);
		financeiroVO.setProperty("CODCTABCOINT", codCtaBcoInt);
		financeiroVO.setProperty("NUMNOTA", numFinAdViag);

		// realiza o insert
		dwfEntityFacade.createEntity("Financeiro", (EntityVO) financeiroVO);

		// captura a chave primaria criada após o insert
		BigDecimal nufin = (BigDecimal) financeiroVO.getProperty("NUFIN");
		atualizaFinanAdiantamentoViagem(nufin, dhatual, numFinAdViag, sequencia, numAdViagem);
	}

	private void atualizaFinanAdiantamentoViagem(BigDecimal nufin, Timestamp dhatual, BigDecimal numFinAdViag,
			BigDecimal sequencia, BigDecimal numAdViagem) throws Exception {
		// Busca o registro pela PK
		EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
		PersistentLocalEntity finanAdiaViaEntity = dwfFacade.findEntityByPrimaryKey("AD_FINADIANTAVIAGEM",
				new Object[] { numAdViagem, numFinAdViag, sequencia });
		DynamicVO finanAdiaViaVO = (DynamicVO) finanAdiaViaEntity.getValueObject();

		// setar propriedades à serem atualizadas
		finanAdiaViaVO.setProperty("NUFIN", nufin);
		finanAdiaViaVO.setProperty("DATABAIXAFIN", dhatual);

		// realiza o update
		finanAdiaViaEntity.setValueObject((EntityVO) finanAdiaViaVO);
	}

	/*
	 * private Collection<DynamicVO> getEnvParcbyPerfil(BigDecimal numFinAdViag)
	 * throws Exception { JapeWrapper envParDAO =
	 * JapeFactory.dao("AD_FINADIANTAVIAGEM"); return
	 * envParDAO.find("this.NUMFINADVIAGEM = ?", numFinAdViag); }
	 */

}
