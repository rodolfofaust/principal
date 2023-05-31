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

public class GeraMovFinLancamentoAcerto implements AcaoRotinaJava {

	@Override
	public void doAction(ContextoAcao contextoAcao) throws Exception {
		// TODO Auto-generated method stub
		Registro regPai = contextoAcao.getLinhaPai();
		Registro[] registros = contextoAcao.getLinhas();
		// BigDecimal sentidoPgto;

		for (Registro registro : registros) {

			BigDecimal vlrAdiantamento = (BigDecimal) registro.getCampo("VLRADIANTAMENTO");
			BigDecimal vlrUtilizado = (BigDecimal) registro.getCampo("VLRUTILIZADO");
			Timestamp dataVenci = (Timestamp) registro.getCampo("DATAVENCIMENTO"); // Data Vencimento:

			if (vlrUtilizado == null)
				throw new Exception("Favor preencher o Valor Utilizado.");

			if (dataVenci == null)
				throw new Exception("Favor preencher a Data de Vencimento.");

			if (vlrAdiantamento.compareTo(vlrUtilizado) == -1) {
				// valor adiantado é menor do que o utilizado. Gera uma mov financeira de débito
				// para reembolsar o funcionario
				criaMovimentacaoFinanceira(registro, regPai, vlrAdiantamento, vlrUtilizado, new BigDecimal(-1));
			} else if (vlrAdiantamento.compareTo(vlrUtilizado) == 1) {
				// valor adiantado é maio do que o utilizado. Gera uma mov financeira como
				// receita no financeiro (TGFFIN). O funcionario devolve dinheiro.
				criaMovimentacaoFinanceira(registro, regPai, vlrAdiantamento, vlrUtilizado, new BigDecimal(1));
			}
			contextoAcao.setMensagemRetorno("Movimentação Financeria realizada com sucesso!");
		}
	}

	private void criaMovimentacaoFinanceira(Registro registro, Registro regPai, BigDecimal vlrAdiantamento,
			BigDecimal vlrUtilizado, BigDecimal sentidoPgto) throws Exception {
		// tela mestre
		BigDecimal codParc = (BigDecimal) regPai.getCampo("CODPARC");
		// Timestamp dataRetorno = (Timestamp) regPai.getCampo("DATARETORNO");
		BigDecimal codlancAcerto = (BigDecimal) regPai.getCampo("CODLANCACERTO");

		// tela detalhe
		BigDecimal numFinAdViag = (BigDecimal) registro.getCampo("CODFINLANC"); // Nro Financeiro Adiantamento Viagem:
		BigDecimal codFinLanc = (BigDecimal) registro.getCampo("CODFINLANC"); // Nro Financeiro Adiantamento Viagem:
		BigDecimal sequencia = (BigDecimal) registro.getCampo("SEQUENCIA"); // Nro Financeiro Adiantamento Viagem:
		BigDecimal codCtaBcoInt = (BigDecimal) registro.getCampo("CODCTABCOINT"); // Conta bancária
		BigDecimal codNat = (BigDecimal) registro.getCampo("CODNAT"); // Natureza
		BigDecimal codTipOper = (BigDecimal) registro.getCampo("CODTIPOPER"); // Tipo Operação
		BigDecimal codCencus = (BigDecimal) registro.getCampo("CODCENCUS"); // Centro de Custo
		BigDecimal codTipTit = (BigDecimal) registro.getCampo("CODTIPTIT"); // Tipo de Título
		Timestamp dataAdViag = (Timestamp) registro.getCampo("DATAPAGTO"); // Data Adiantamento:
		Timestamp dataBaixa = (Timestamp) registro.getCampo("DATABAIXAFIN"); // Data Baixa:
		Timestamp dataVenci = (Timestamp) registro.getCampo("DATAVENCIMENTO"); // Data Vencimento:

		String observacao = (String) ((registro.getCampo("OBSERVACAO") != null) ? (registro.getCampo("OBSERVACAO"))
				: "");
		BigDecimal vlrAcerto = vlrAdiantamento.subtract(vlrUtilizado);
		vlrAcerto = vlrAcerto.compareTo(BigDecimal.ZERO) == -1 ? vlrAcerto.negate() : vlrAcerto;

		// variaveis sistema e banco
		BigDecimal codusu = (BigDecimal) JapeSessionContext.getProperty("usuario_logado");
		BigDecimal codEmp = NativeSql.getBigDecimal("CODEMP", "TSIUSU", "CODUSU = ?", codusu);
		BigDecimal codBco = NativeSql.getBigDecimal("CODBCO", "TSICTA", "CODCTABCOINT = ?", codCtaBcoInt);

		// Busca a tabela a ser inserida, com base na instância
		EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
		DynamicVO financeiroVO = (DynamicVO) dwfEntityFacade.getDefaultValueObjectInstance("Financeiro");

		// inclui os valores desejados nos campos
		financeiroVO.setProperty("CODPARC", codParc);
		financeiroVO.setProperty("DTVENC", dataVenci);
		financeiroVO.setProperty("DTNEG", dataAdViag);
		financeiroVO.setProperty("CODNAT", codNat);
		financeiroVO.setProperty("CODTIPOPER", codTipOper);
		financeiroVO.setProperty("VLRDESDOB", vlrAcerto);
		financeiroVO.setProperty("RECDESP", sentidoPgto);
		financeiroVO.setProperty("CODTIPTIT", codTipTit);
		financeiroVO.setProperty("CODCENCUS", codCencus);
		financeiroVO.setProperty("DHBAIXA", dataBaixa);
		financeiroVO.setProperty("HISTORICO", "***ACERTO DE ADIANTAMENTO DE VIAGEM*** " + observacao);
		financeiroVO.setProperty("CODBCO", codBco);
		financeiroVO.setProperty("CODEMP", codEmp);
		financeiroVO.setProperty("CODUSU", codusu);
		financeiroVO.setProperty("CODCTABCOINT", codCtaBcoInt);
		financeiroVO.setProperty("NUMNOTA", numFinAdViag);

		// realiza o insert
		dwfEntityFacade.createEntity("Financeiro", (EntityVO) financeiroVO);

		// captura a chave primaria criada após o insert
		BigDecimal nufin = (BigDecimal) financeiroVO.getProperty("NUFIN");
		atualizaFinanLancamentoAcerto(nufin, dataBaixa, codFinLanc, sequencia, codlancAcerto, vlrUtilizado, dataVenci);
	}

	private void atualizaFinanLancamentoAcerto(BigDecimal nufin, Timestamp dataBaixa, BigDecimal codFinLanc,
			BigDecimal sequencia, BigDecimal codlancAcerto, BigDecimal vlrUtilizado, Timestamp dataVenci)
			throws Exception {
		// Busca o registro pela PK
		EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
		PersistentLocalEntity finanAdiaViaEntity = dwfFacade.findEntityByPrimaryKey("AD_FINANLANCADIANVI",
				new Object[] { codlancAcerto, codFinLanc, sequencia });
		DynamicVO finanAdiaViaVO = (DynamicVO) finanAdiaViaEntity.getValueObject();

		// setar propriedades à serem atualizadas
		finanAdiaViaVO.setProperty("NUMFIN", nufin);
		finanAdiaViaVO.setProperty("DATABAIXAFIN", dataBaixa);
		finanAdiaViaVO.setProperty("VLRUTILIZADO", vlrUtilizado);
		finanAdiaViaVO.setProperty("DATAVENCIMENTO", dataVenci);

		// realiza o update
		finanAdiaViaEntity.setValueObject((EntityVO) finanAdiaViaVO);

	}

}
