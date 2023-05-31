package br.com.sankhya.ctba.adiantamentoViagens;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Collection;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.bmp.PersistentLocalEntity;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

public class LancamentoAcertoAdiantViagem implements EventoProgramavelJava {

	@Override
	public void afterDelete(PersistenceEvent arg0) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterInsert(PersistenceEvent event) throws Exception {
		// TODO Auto-generated method stub
		DynamicVO finAcertoVO = (DynamicVO) event.getVo();
		BigDecimal codAdiantamento = finAcertoVO.asBigDecimal("CODADIANTEMANTO");
		BigDecimal codLancamentoAcerto = finAcertoVO.asBigDecimal("CODLANCACERTO");
		Collection<DynamicVO> finAdtoViagemVOs = buscaFinanceiroAdtoViagem(codAdiantamento);

		// criaAcertoFinanceiro(finAdtoViVO, codLancamentoAcerto);
		if (finAdtoViagemVOs.isEmpty()) {
			throw new Exception("Não foram encontrados lançamentos financeiros para esse Adiantamento de Viagem.");
		}

		for (DynamicVO finAdtoViagemVO : finAdtoViagemVOs) {
			criaAcertoFinanceiro(finAdtoViagemVO, codLancamentoAcerto);
		}
	}

	@Override
	public void afterUpdate(PersistenceEvent arg0) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeCommit(TransactionContext arg0) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeDelete(PersistenceEvent arg0) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeInsert(PersistenceEvent event) throws Exception {
		// TODO Auto-generated method stub
		DynamicVO lancAcertoVO = (DynamicVO) event.getVo();
		DynamicVO adiaVO = buscaAdiantamentoViagem(lancAcertoVO.asBigDecimal("CODADIANTEMANTO"));

		BigDecimal codParc = adiaVO.asBigDecimal("CODPARC");
		BigDecimal codCid = adiaVO.asBigDecimal("CODCID");
		BigDecimal qtdPessoas = adiaVO.asBigDecimal("QTDPESSOAS");
		Timestamp dataRetorno = adiaVO.asTimestamp("DATARETORNO");
		String tpTransporte = adiaVO.asString("TIPOTRANSPORTE");
		String observacao = adiaVO.asString("OBSERVACAO");

		lancAcertoVO.setProperty("CODPARC", codParc);
		lancAcertoVO.setProperty("CODCID", codCid); // destino
		lancAcertoVO.setProperty("QTDPESSOA", qtdPessoas);
		lancAcertoVO.setProperty("DATARETORNO", dataRetorno);
		lancAcertoVO.setProperty("TIPOTRANSPORTE", tpTransporte);
		lancAcertoVO.setProperty("OBSERVACAO", observacao);
	}

	@Override
	public void beforeUpdate(PersistenceEvent arg0) throws Exception {
		// TODO Auto-generated method stub

	}

	private DynamicVO buscaAdiantamentoViagem(BigDecimal numAdiantViagem) throws Exception {
		EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
		PersistentLocalEntity cabNotaEntity = dwfFacade.findEntityByPrimaryKey("AD_ADIANTAMENTOVIAGEM",
				new Object[] { numAdiantViagem });

		DynamicVO adiaVO = (DynamicVO) cabNotaEntity.getValueObject();

		if (adiaVO.getValueObjectID().isEmpty()) {
			throw new Exception("Não foram encontrados lançamentos financeiros para esse Adiantamento de Viagem.");
		}

		return adiaVO;
	}

	private Collection<DynamicVO> buscaFinanceiroAdtoViagem(BigDecimal numAdiantViagem) throws Exception {
		JapeWrapper envParDAO = JapeFactory.dao("AD_FINADIANTAVIAGEM");
		return envParDAO.find("this.NUADIANTVIAGEM = ?", numAdiantViagem);
	}

	public void criaAcertoFinanceiro(DynamicVO adiaVO, BigDecimal codLancamentoAcerto) throws Exception {
		// Busca a tabela a ser inserida
		EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
		DynamicVO finAcertoVO = (DynamicVO) dwfEntityFacade.getDefaultValueObjectInstance("AD_FINANLANCADIANVI");

		// inclui os valores desejados nos campos
		finAcertoVO.setProperty("SEQUENCIA", adiaVO.asBigDecimal("SEQUENCIA"));
		finAcertoVO.setProperty("CODLANCACERTO", codLancamentoAcerto);
		finAcertoVO.setProperty("CODCTABCOINT", adiaVO.asBigDecimal("CODCTABCOINT"));
		finAcertoVO.setProperty("CODNAT", adiaVO.asBigDecimal("CODNAT"));
		finAcertoVO.setProperty("CODCENCUS", adiaVO.asBigDecimal("CODCENCUS"));
		finAcertoVO.setProperty("CODTIPOPER", adiaVO.asBigDecimal("CODTIPOPER"));
		finAcertoVO.setProperty("SENTIDOPAGTO", adiaVO.asString("SENTIDOPAGTO"));
		finAcertoVO.setProperty("OBSERVACAO", adiaVO.asString("OBSERVACAO"));
		finAcertoVO.setProperty("DATAPAGTO", adiaVO.asTimestamp("DATAPAGTO"));
		finAcertoVO.setProperty("VLRADIANTAMENTO", adiaVO.asBigDecimal("VLRADIANTAMENTO"));
		finAcertoVO.setProperty("CODFINADVIAGEM", adiaVO.asBigDecimal("NUMFINADVIAGEM"));
		finAcertoVO.setProperty("CODTIPTIT", adiaVO.asBigDecimal("CODTIPTIT"));

		// realiza o insert
		dwfEntityFacade.createEntity("AD_FINANLANCADIANVI", (EntityVO) finAcertoVO);
	}

}
