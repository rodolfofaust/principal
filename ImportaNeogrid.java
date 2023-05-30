package br.com.sankhya.ctba.neogrid;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
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


public class ImportaNeogrid implements AcaoRotinaJava {

	static final String SSH_PATH_IMPORTADOS = "/home/neogrid/NeoGridClient/documents/in";
	static final String SSH_PATH_RETORNOS = "/home/neogrid/NeoGridClient/documents/in";
	static final String SSH_PATH_SEPARACAO = "/Pedidos Separação/";

	static final String REPOSITORIO_RECEBER = "neogrid_in/";
	static final String REPOSITORIO_OK = "neogrid_in/NEOGRID_OK/";
	static final String REPOSITORIO_ERRO = "neogrid_in/NEOGRID_ERRO/";

	static final Logger logger = Logger.getLogger(ImportaNeogrid.class.getName());


	@SuppressWarnings("unchecked")
	public void doAction(ContextoAcao contexto) throws Exception {
		// TODO Auto-generated method stub
		logger.log(Level.INFO, "[ImportaNeogrid] ----- DOACTION - INICIO -----");

		//VARIAVEIS
		String nomeArquivo = "", erroMsg = "";
		boolean temEstoque = Boolean.FALSE, semErro = Boolean.TRUE;
		BigDecimal nunota = BigDecimal.ZERO;

		// Listas
		List<BigDecimal> listNotas = new ArrayList<>();
		List<List<StringBuilder>> arrayListFtp = new ArrayList<>();

		//INICIO PROCESSO
		try {

			this.renomearArquivo(ImpNeogridHelper.getRepositorio() + REPOSITORIO_RECEBER);

			BigDecimal codTipOper = (BigDecimal) MGECoreParameter.getParameter("TOPVENDAARQUTXT");
			DynamicVO topVO = ImpNeogridHelper.getTipoOperacao(codTipOper);

			boolean validaEstoque = topVO.asString("VALEST").equals("S");

			if (!validaEstoque) 
				temEstoque = true;


			arrayListFtp = this.buscarArquivoDir(ImpNeogridHelper.getRepositorio() + REPOSITORIO_RECEBER, contexto);
			for (List<StringBuilder> sbList: arrayListFtp) {
				List<StringBuilder> listFtp = sbList;

				for (StringBuilder sb: listFtp) {
					logger.log(Level.INFO, " ############ Novo arquivo ############");

					//REINICIANDO AS VARIÁVEIS
					BigDecimal codParc = null, codVend = null;
					semErro = Boolean.TRUE;
					erroMsg = "";
					String  tipo_registro = "", ciffob = "";
					String 	numero_pedido_comprador = "", cnpj_comprador = "";

					BigDecimal valor_total_ipi = BigDecimal.ZERO,
							valor_total_pedido = BigDecimal.ZERO,
							valor_encargos_total = BigDecimal.ZERO,
							valor_encargos_frete = BigDecimal.ZERO,
							valor_desconto_total = BigDecimal.ZERO, 
							valor_encargos_seguro = BigDecimal.ZERO,
							valor_desconto_promocional = BigDecimal.ZERO, 
							valor_encargos_financeiros = BigDecimal.ZERO,
							valor_desconto_comercial_pedido = BigDecimal.ZERO,
							valor_desconto_financeiro_pedido = BigDecimal.ZERO; 


					List<String> listItens = new ArrayList<>();
					Scanner scan = new Scanner(sb.toString()); 
					while (scan.hasNextLine()) {
						logger.log(Level.INFO, "******IMPORTADOR - INICIANDO LINHA/HELPER******");
						String linha = scan.nextLine();

						if (semErro) {
							tipo_registro = linha.substring(0,2); // 2 - 0 - 1

							//NOME ARQUIVO
							if (tipo_registro.contains(";")) { 
								nomeArquivo = linha.substring(1, linha.length());
							}

							logger.log(Level.INFO, "nomeArquivo: "+nomeArquivo);

							//HEADER
							if (tipo_registro.equals("01")) { 
								logger.log(Level.INFO, "******HEADER - INICIO*******");
								numero_pedido_comprador = linha.substring(8,28).trim();
								cnpj_comprador = linha.substring(180,194).trim();

								//codParc = NativeSql.getBigDecimal("CODPARC", "TGFPAR", "CGC_CPF = ? ", new Object[] { cnpj_comprador });

								Collection<DynamicVO> parcList = EntityFacadeFactory.getDWFFacade().findByDynamicFinderAsVO(new FinderWrapper(
										"Parceiro", "this.CGC_CPF = ? ", new Object[] { cnpj_comprador }));

								logger.log(Level.INFO, "parcList.size(): "+parcList.size());

								if (parcList.size() > 0) {
									DynamicVO parcVO = parcList.iterator().next();
									codParc = parcVO.asBigDecimal("CODPARC");
									codVend = parcVO.asBigDecimal("CODVEND");
									ciffob = parcVO.asString("AD_CIFFOB");
								} else {
									semErro = false;
									erroMsg = erroMsg+"\nParceiro não encontrado. CNPJ: "+cnpj_comprador;
								}

								logger.log(Level.INFO, " CNPJ: "+cnpj_comprador+", CODPARC"+codParc+", CODVEND"+codVend+", AD_CIFFOB"+ciffob);
								logger.log(Level.INFO, "******HEADER - FIM*******");
							}

							//DESCONTOS_ABATIMENTO_ENCARGOS
							if (tipo_registro.equals("03")) { 
								logger.log(Level.INFO, "******DESCONTOS_ABATIMENTO_ENCARGOS - INICIO*******");
								//descontos
								valor_desconto_promocional = new BigDecimal(linha.substring(47,62).trim());
								valor_desconto_total 	   = BigDecimal.ZERO;
								valor_desconto_financeiro_pedido = new BigDecimal(linha.substring(7,22).trim());
								valor_desconto_comercial_pedido  = new BigDecimal(linha.substring(27,42).trim());

								//totais
								valor_desconto_total = valor_desconto_total.add(valor_desconto_promocional.add(valor_desconto_financeiro_pedido.add(valor_desconto_comercial_pedido))); 
								valor_encargos_total = valor_encargos_total.add(valor_encargos_financeiros.add(valor_encargos_frete.add(valor_encargos_seguro))); 

								logger.log(Level.INFO, "******DESCONTOS_ABATIMENTO_ENCARGOS - FIM*******");
							} 

							//TRAILLER
							if (tipo_registro.equals("09")) {
								logger.log(Level.INFO, "******TRAILLER - INICIO*******");

								valor_total_pedido = new BigDecimal(linha.substring(107,122));
								valor_total_ipi = new BigDecimal(linha.substring(17,32));

								logger.log(Level.INFO, "******TRAILLER - FIM*******");
							}	

							//ITEM_PEDIDO
							if (tipo_registro.equals("04")) {
								BigDecimal codProd = ImpNeogridHelper.getProduto(linha, codParc);
								BigDecimal qtde_pedida = BigDecimal.ZERO; 
								StringBuilder sbqtde_pedida = new StringBuilder(linha.substring(99,114).trim());
								qtde_pedida = new BigDecimal(sbqtde_pedida.insert(linha.substring(99,114).trim().length()-2, '.').toString());

								if (codProd == null) {
									semErro = Boolean.FALSE;
									erroMsg = erroMsg+"\nErro: PRODUTO NÃO EXISTE. CODROD: "+codProd;
									logger.log(Level.INFO, erroMsg);
								}

								if (codProd != null) {
									if (validaEstoque) {
										//qtd = NativeSql.getBigDecimal("COUNT(*)", "TGFPRO", "CODPROD = ? ", new Object[] { codProd });
										temEstoque = ImpNeogridHelper.validaEstoqueProduto(codProd, qtde_pedida);

										if (!temEstoque) {
											semErro = temEstoque;
											erroMsg = erroMsg+"\nErro: PRODUTO SEM ESTOQUE DISPONIVEL. CODPROD: "+codProd;
											logger.log(Level.INFO, erroMsg);
										}
									}
									if (temEstoque)
										listItens.add(linha);
								}
								logger.log(Level.INFO, "O CODPROD: "+codProd+", qtde_pedida: "+qtde_pedida+", temEstoque: "+temEstoque);
							}

							/*
							if (tipo_registro.equals("02")) { //CONDICOES_PAGAMENTO
								logger.log(Level.INFO, "******CONDICOES_PAGAMENTO - INICIO*******");

								logger.log(Level.INFO, "******CONDICOES_PAGAMENTO - FIM*******");
							} else if (tipo_registro.equals("05")) { //GRADE
								//getItensPedido();
							} else if (tipo_registro.equals("06")) { //CROSS_DOCKING
								//getItensPedido();
							}

							 */

							tipo_registro = "";
						}//IF semErro
					}//while scan

					scan.close();

					logger.log(Level.INFO, "[doAction] VARIAVEIS: "
							+", codParc = "+codParc
							+", semErro = "+semErro
							+", cnpj_comprador = "+cnpj_comprador
							+", valor_desconto_total = "+valor_desconto_total
							+", valor_total_ipi = "+valor_total_ipi
							+", valor_total_pedido = "+valor_total_pedido
							+", numero_pedido_comprador = "+numero_pedido_comprador
							+", nomeArquivo = "+nomeArquivo
							+", qtde Produtos = "+listItens.size()
							);

					if (listItens == null || listItens.isEmpty()) {
						semErro = Boolean.FALSE;
						erroMsg = erroMsg+"\nErro: SEM PRODUTO NA LISTA. ";
						logger.log(Level.INFO, erroMsg);
					}

					File file = new File(nomeArquivo);
					if (semErro && codParc != null) {
						nunota = ImpNeogridHelper.criaMovimentoPortalVendas(valor_desconto_total, valor_total_ipi, valor_total_pedido, 
								numero_pedido_comprador, codParc, nomeArquivo, ciffob, codVend);

						for (String linhaiTens: listItens) {
							getItensPedido(linhaiTens, nunota, codParc);	
						}

						listNotas.add(nunota);
						ImpNeogridHelper.insertLog(nunota, BigDecimal.ZERO, nomeArquivo, "Arquivo processado com sucesso! ");
						//this.renomearArquivoOK(ImpNeogridHelper.getRepositorio() + REPOSITORIO_RECEBER + nomeArquivo);
						this.renomearArquivoOK(file);

					} else {

						erroMsg = "Arquivo Nao Processado. Erro: "+erroMsg;
						logger.log(Level.INFO, "******* DEU ERRO *******" +erroMsg );
						ImpNeogridHelper.insertLog(nunota, BigDecimal.ZERO, nomeArquivo, erroMsg);
						//this.renomearArquivoErro(ImpNeogridHelper.getRepositorio() + REPOSITORIO_RECEBER + nomeArquivo);
						this.renomearArquivoErro(file);
					}
				}
			}
		} catch (Exception ex) {
			erroMsg = "Erro na leitura do arquiv. Erro: " + ex.getCause();
			logger.log(Level.INFO, "CATCH/EXCEPTION -> erroMsg: "+erroMsg);

			ImpNeogridHelper.insertLog(nunota, BigDecimal.ZERO, nomeArquivo, erroMsg);
			File file = new File(nomeArquivo);

			this.renomearArquivoErro(file);
			//contexto.info(dateFormat.format(new Date()) + " [exportavelRomaneioRecebimento] Exception: " + ex + "\n");
		}
		if (listNotas.isEmpty()) {
			contexto.setMensagemRetorno("Nenhuma nota foi gerada.");			
		} else {
			contexto.setMensagemRetorno("Rotina executada com sucesso!" + "<br>Notas Geradas: " + listNotas.toString());			
		}

	}

	private void getItensPedido(String linha, BigDecimal nunota, BigDecimal codParc) throws Exception {
		// TODO Auto-generated method stub
		logger.log(Level.INFO, "******getItensPedido - INICIO*******");
		//ITEM_PEDIDO

		BigDecimal 	referencia_produto = BigDecimal.ZERO, 
				numero_sequencial = BigDecimal.ZERO, 
				qtde_pedida = BigDecimal.ZERO, 
				preco_bruto_unitario = BigDecimal.ZERO, 
				valor_unitario_ipi = BigDecimal.ZERO, 
				valor_unitario_desc_comercial = BigDecimal.ZERO, 
				percentual_ipi_produto = BigDecimal.ZERO,
				percentual_desc_comercial = BigDecimal.ZERO;

		referencia_produto = ImpNeogridHelper.getProduto(linha, codParc);

		//codigo_barras 	  	 = new BigDecimal(linha.substring(17,31).trim());
		numero_sequencial 	 = new BigDecimal(linha.substring(2,6).trim());

		StringBuilder sbqtde_pedida = new StringBuilder(linha.substring(99,114).trim());
		qtde_pedida 		 = new BigDecimal(sbqtde_pedida.insert(linha.substring(99,114).trim().length()-2, '.').toString());

		StringBuilder sbpreco_bruto_unitario = new StringBuilder(linha.substring(182,197).trim());
		preco_bruto_unitario 		 = new BigDecimal(sbpreco_bruto_unitario.insert(linha.substring(182,197).trim().length()-2, '.').toString());

		StringBuilder sbvalor_unitario_ipi = new StringBuilder(linha.substring(240,255).trim());
		valor_unitario_ipi 		 = new BigDecimal(sbvalor_unitario_ipi.insert(linha.substring(240,255).trim().length()-2, '.').toString());		

		StringBuilder sbvalor_unitario_desc_comercial = new StringBuilder(linha.substring(220,235).trim());
		valor_unitario_desc_comercial 		 = new BigDecimal(sbvalor_unitario_desc_comercial.insert(linha.substring(220,235).trim().length()-2, '.').toString());		
		//valor_unitario_desc_comercial  = new BigDecimal(linha.substring(220,235).trim());		

		percentual_ipi_produto = new BigDecimal(linha.substring(255,260).trim());		

		logger.log(Level.INFO, "[getItensPedido] VARIAVEIS: "
				+", NUNOTA = "+nunota
				+", REFERENCIA_PRODUTO = "+referencia_produto
				+", NUMERO_SEQUENCIAL = "+numero_sequencial
				+", QTDE_PEDIDA = "+qtde_pedida
				+", PRECO_BRUTO_UNITARIO = "+preco_bruto_unitario
				+", VALOR_UNITARIO_IPI = "+valor_unitario_ipi
				+", VALOR_UNITARIO_DESC_COMERCIAL = "+valor_unitario_desc_comercial
				+", PERCENTUAL_IPI_PRODUTO = "+percentual_ipi_produto
				+", PERCENTUAL_DESC_COMERCIAL = "+percentual_desc_comercial
				);

		ImpNeogridHelper.criaItensNaNota(numero_sequencial, qtde_pedida, preco_bruto_unitario, valor_unitario_desc_comercial, percentual_desc_comercial,
				percentual_ipi_produto, valor_unitario_ipi, nunota, referencia_produto);


		logger.log(Level.INFO, "******getItensPedido - FIM*******");
	}


	private void renomearArquivoOK(File file) throws Exception {
		logger.log(Level.INFO, "[renomearArquivoOK] ******** INICIO ********");
		logger.log(Level.INFO, "[renomearArquivoOK] BEFORE **** FILE NAME: "+ file.toString());

		String newNome = ImpNeogridHelper.getRepositorio() + REPOSITORIO_OK + file.getName();
		if (newNome.contains("-PROCESSANDO.TXT") || newNome.contains("-PROCESSANDO.txt")) {
			File newFile = new File(newNome.replace("-PROCESSANDO.txt", "-OK.txt").replace("-PROCESSANDO.TXT", "-OK.TXT"));
			file.renameTo(newFile);
		} else {
			File newFile = new File(newNome.replace(".txt", "-OK.txt").replace(".TXT", "-OK.TXT"));
			file.renameTo(newFile);			
		}

		logger.log(Level.INFO, "[renomearArquivoOK] AFTER **** FILE NAME: "+ file.toString());		
		logger.log(Level.INFO, "[renomearArquivoOK] ******** FIM ********");
	}	

	private void renomearArquivoErro(File file) throws Exception {
		logger.log(Level.INFO, "[renomearArquivoErro] ******** INICIO ********");
		logger.log(Level.INFO, "[renomearArquivoErro] BEFORE **** FILE NAME: "+ file.toString());

		String newNome = ImpNeogridHelper.getRepositorio() + REPOSITORIO_ERRO + file.getName();
		if (newNome.contains("-PROCESSANDO.TXT") || newNome.contains("-PROCESSANDO.txt")) {
			File newFile = new File(newNome.replace("-PROCESSANDO.txt", "-OK.txt").replace("-PROCESSANDO.TXT", "-OK.TXT"));
			file.renameTo(newFile);
		} else {
			File newFile = new File(newNome.replace(".txt", "-ERRO.txt").replace(".TXT", "-ERRO.TXT"));
			file.renameTo(newFile);			
		}

		logger.log(Level.INFO, "[renomearArquivoErro] AFTER **** FILE NAME: "+ file.toString());
		logger.log(Level.INFO, "[renomearArquivoErro] ******** FIM ********");
	}


	private void renomearArquivo(String path) throws Exception {
		logger.log(Level.INFO, "[renomearArquivo] path 1: "+ path);

		String novoNome = "";

		File folder = new File(path);
		for (File file : folder.listFiles()) {

			if (!file.isDirectory()) {
				logger.log(Level.INFO, "[renomearArquivo] File name OLD: "+ path + file.getName());

				if (file.getName().contains("-PROCESSANDO")) {
					File fileNew = new File(path + file.getName().replace("-PROCESSANDO.TXT", ".txt").replace("-PROCESSANDO.txt", ".txt"));
					file.renameTo(fileNew);
					novoNome = fileNew.toString();
				}

				if (file.getName().contains("-PROCESSANDO_2")) {
					File fileNew = new File(path + file.getName().replace("-PROCESSANDO_2.TXT", ".txt").replace("-PROCESSANDO.txt", ".txt"));
					file.renameTo(fileNew);
					novoNome = fileNew.toString();
				}

				if (file.getName().contains(".TXT")) {
					File fileNew = new File(path + file.getName().replace(".TXT", ".txt"));
					file.renameTo(fileNew);
					novoNome = fileNew.toString();
				}
			}
			logger.log(Level.INFO, "[renomearArquivo] File name NEW: "+novoNome );
		}

	}	


	private List<List<StringBuilder>> buscarArquivoDir(String path, ContextoAcao contexto) throws Exception {
		logger.log(Level.INFO, "[buscarArquivoDir] path 1: "+ path);

		List<StringBuilder> list = new ArrayList<>();
		List<List<StringBuilder>> arraylist = new ArrayList<>();

		File folder = new File(path);
		logger.log(Level.INFO, "[buscarArquivoDir] folder.listFiles(): "+ folder.listFiles().toString());

		for (File file : folder.listFiles()) {
			logger.log(Level.INFO, "[doAction] file.isDirectory: "+file.isDirectory());

			if (!file.isDirectory()) {
				logger.log(Level.INFO, "[buscarArquivoDir] File name 2: "+ path + file.getName());

				if (!file.getName().contains("-OK") && !file.getName().contains("-ERRO")) {
					logger.log(Level.INFO, "[buscarArquivoDir] File name 2: "+ path + file.getName());
					BufferedReader reader = new BufferedReader(new FileReader (path + file.getName()));
					StringBuilder sb = new StringBuilder();
					String inline = "";
					int a = 0;
					logger.log(Level.INFO, "[buscarArquivoDir] READER: "+reader.toString());

					while ((inline = reader.readLine()) != null) {
						if (a == 0)
							sb.append(";" + file.toString() + "\n");
						sb.append(inline + "\n");
						a++;
					}
					logger.log(Level.INFO, "[buscarArquivoDir] SB: "+ sb.toString());

					list.add(sb);
					reader.close();

					arraylist.add(list);
					list = new ArrayList<>();
				}
			}
		}
		logger.log(Level.INFO, "[buscarArquivoDir] File list size: "+ arraylist.size());
		return arraylist;
	}	

	/*	
	private void renomearArquivoErro(String path) throws Exception {
		logger.log(Level.INFO, "[renomearArquivoErro] ******** INICIO ********");

		String fileError = null;
		if (path.contains("-PROCESSANDO.txt")) {
			fileError = path.replace("-PROCESSANDO.txt", "-ERRO.txt").replace("-PROCESSANDO.TXT", "-ERRO.TXT");	
		} else {
			fileError = path.replace(".txt", "-ERRO.txt").replace(".TXT", "-ERRO.TXT");
		}

		File fileEr = new File(fileError);
		File file = new File(path);

		logger.log(Level.INFO, "[renomearArquivoErro] fileError: {0}", fileError);
		logger.log(Level.INFO, "[renomearArquivoErro] fileEr1: {0}", fileEr.toString());
		logger.log(Level.INFO, "[renomearArquivoErro] file2: {0}"  , file.toString());
		logger.log(Level.INFO, "[renomearArquivoErro] file3: {0}"  , file.getName());
		logger.log(Level.INFO, "[renomearArquivoErro] path: {0}"  , path);

		if (file.exists()) {
			logger.log(Level.INFO, "[renomearArquivoErro] file EXISTS");
			if (!fileEr.exists()) {
				logger.log(Level.INFO, "[renomearArquivoErro] fileEr NAO EXISTS");
				if (!file.getName().contains("-ERRO.txt")) {
					File newFile = null;

					if (path.contains("-PROCESSANDO.txt") || (path.contains("-PROCESSANDO.TXT"))) {
						newFile = new File(path.replace("-PROCESSANDO.txt", "-ERRO.txt").replace("-PROCESSANDO.TXT", "-ERRO.TXT"));	
					} else {
						newFile = new File(path.replace(".txt", "-ERRO.txt").replace(".TXT", "-ERRO.TXT"));
					}
					logger.log(Level.INFO, "[renomearArquivoErro] file4: {0}", path +" -> "+ newFile.getName());
					file.renameTo(newFile);
				}
			} else {
				logger.log(Level.INFO, "[renomearArquivoErro] fileEr NAO EXISTS: "+fileEr.toString());
			}
		} else {
			logger.log(Level.INFO, "[renomearArquivoErro] file NAO EXISTS: "+file.toString());
			//file.renameTo(fileEr);

			String nomeFile = ImpNeogridHelper.getRepositorio() + REPOSITORIO_ERRO + fileEr.getName();
			logger.log(Level.INFO, "[renomearArquivoErro] nomeFile 5: {0}"  , nomeFile);
			File namefileEr = new File(nomeFile);

			logger.log(Level.INFO, "[renomearArquivoErro] namefileEr: {0}"  , namefileEr.toString());

			file.renameTo(namefileEr);
			//file.createNewFile();
		}

		logger.log(Level.INFO, "[renomearArquivoErro] File 6: {0}", file.toString());
		logger.log(Level.INFO, "[renomearArquivoErro] ******** FIM ********");
	}




	private void renomearArquivo(String path, File file) throws Exception {
		logger.log(Level.INFO, "[renomearArquivo] File name OLD: {0}", path + file.getName());

		String fileName = file.getName();
		File newFile = null;

		if (!file.getName().contains("-PROCESSANDO.TXT")) {
			newFile = new File(path + fileName.toUpperCase()
			.replace(" - ", "-")
			.replace(" ", "-")
			.replace(".txt", "-PROCESSANDO.txt")
			.replace(".TXT", "-PROCESSANDO.TXT"));
			if (!newFile.exists()) {
				file.renameTo(newFile);
			} else {
				newFile = new File(path + fileName.toUpperCase()
				.replace(" - ", "-")
				.replace(" ", "-")			
				.replace(".txt", "-ARQUIVO-DUPLICADO-ERRO.txt")
				.replace(".TXT", "-ARQUIVO-DUPLICADO-ERRO.TXT"));
				file.renameTo(newFile);
			}
		} else {
			newFile = new File(path + fileName.toUpperCase());
		}

		logger.log(Level.INFO, "[renomearArquivo] File name NEW: {0}",  path + newFile.getName());
	}

	 */
}
