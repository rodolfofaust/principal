package br.com.sankhya.ctba.integracaoapi;


import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.bmp.PersistentLocalEntity;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.util.JapeSessionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import jdk.nashorn.internal.ir.TryNode;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class ImportDealAPI {
	static final Logger logger = Logger.getLogger(ImportDealAPI.class.getName());

	static String token = "63851621c5e07d0016e05752";

	public static void main(String[] args) throws Exception {
		deals(token);
	}

	public static void deals(String token) throws Exception {
		logger.log(Level.INFO, "[deals] INICIO");
		boolean hasMore = true;

		int p = 1;
		
		while (hasMore) {
			OkHttpClient client = new OkHttpClient();
			OkHttpClient.Builder builder = new OkHttpClient.Builder();
			client = builder.build();

			Request request = getJson(p, token);
			
//			logger.log(Level.INFO, "[request] "+request);

			try (Response response = client.newCall(request).execute()) {
				if (!response.isSuccessful()) {

					throw new Exception("Falha no JSON: " + response.message());
				} else {
					//resposta = response.message();
					String res = response.body().string(); 
					JsonParser parser = new JsonParser();
					JsonObject jsobj = parser.parse(res).getAsJsonObject();
					hasMore = jsobj.get("has_more").getAsString().equals("true");

					logger.log(Level.INFO, "[hasMore] "+hasMore+"/"+"página: "+p);
					logger.log(Level.INFO, "[hasMore] request:  "+request);
					getOportunidades(res, request);
					p++;
				}

				
			
			} catch (Exception ex) {
				hasMore = false;
				if (ex.getMessage() != null) {
					UtilIntegracao.salvaLogIntegracao("[ERROR]: "+ex.getMessage(), "deals");
					throw new Exception("Falha no JSON.");
				}
			} finally {
				

			}
		}

		logger.log(Level.INFO, "[deals] FIM");
	}

	public static Request getJson(int p, String token) throws Exception {
		logger.log(Level.INFO, "[getJson]  ----------------------- INICIO ----------------------- page: " + p);
		Request request = new Request.Builder()
				.url("https://crm.rdstation.com/api/v1/deals?token="+token+"&page="+p)
				.get()
				.addHeader("accept", "application/json")
				.addHeader("content-type", "application/json") 
				.build();
		System.out.println("page: " + p);
		
//		Request request = new Request.Builder()
//				.url("https://crm.rdstation.com/api/v1/deals?token=63851621c5e07d0016e05752&organization=63ebe3f0b46277000cc13d39")
//				.get()
//				.addHeader("accept", "application/json")
//				.addHeader("content-type", "application/json") 
//				.build();
		
		
		
		//https://crm.rdstation.com/api/v1/deals?token=63851621c5e07d0016e05752&Parametros=&lng=pt-BR
//		logger.log(Level.INFO, "[getJson]  ----------------------- FIM ----------------------- ");
		return request;
	}
	public static Timestamp convertStringToTimestamp(String strDate) throws ParseException {
		System.out.println("strDate: " + strDate); 
		
		OffsetDateTime odt = OffsetDateTime.parse(strDate);
		long timestamp = odt.toInstant().toEpochMilli();
		Timestamp ts = new Timestamp(timestamp);
		return ts;
	}
	@SuppressWarnings("unused")
	private static void getOportunidades(String deals, Request request) throws Exception {
		logger.log(Level.INFO, "[getOportunidades]  ----------------------- INICIO ----------------------- ");
		
		JsonParser parser = new JsonParser();
		JsonObject jsobj = parser.parse(deals).getAsJsonObject();
		JsonArray posts = jsobj.getAsJsonArray("deals");

		for (JsonElement post : posts) {

			JsonObject postObject = post.getAsJsonObject();

			
			List<Contact> contact = new ArrayList<>();
			
			Deal deal = new Deal();
			Organization org = new Organization();
			Vendedor vendedor = new Vendedor();
			Product prod = new Product();
			
			try {
				
				JsonElement dealStageElement = post.getAsJsonObject().getAsJsonObject("deal_stage").get("nickname");
				JsonElement updatedAtElement = post.getAsJsonObject().getAsJsonObject("deal_stage").get("updated_at");

				if(dealStageElement != null && !dealStageElement.isJsonNull() ) {
					 deal.setDeal_stage( dealStageElement.getAsString()); 
				}
				if( updatedAtElement != null && !updatedAtElement.isJsonNull()) {
					deal.setUpdated_atDeal_stageStr(updatedAtElement.getAsString());
				} 
				

				JsonElement idElement = post.getAsJsonObject().get("id");
				JsonElement nameElement = post.getAsJsonObject().get("name");
				JsonElement vlrTotDealElement = post.getAsJsonObject().get("amount_total");
				JsonElement vlrUniDealElement = post.getAsJsonObject().get("amount_unique");
				JsonElement vlrMesDealElement = post.getAsJsonObject().get("amount_monthly");
				JsonElement updatedAtElement1 = post.getAsJsonObject().get("updated_at");
				JsonElement ratingElement = post.getAsJsonObject().get("rating");

				if (idElement != null && !idElement.isJsonNull()) {
					deal.setIdDeal(idElement.getAsString());	
				}
				if (nameElement != null && !nameElement.isJsonNull()) {
					deal.setNameDeal(nameElement.getAsString()); 
				}
				if (vlrTotDealElement != null && !vlrTotDealElement.isJsonNull()) {
					deal.setVlrTotDeal(vlrTotDealElement.getAsString());	
				}
				if (vlrUniDealElement != null && !vlrUniDealElement.isJsonNull()) {
					deal.setVlrUniDeal(vlrUniDealElement.getAsString()); 
				}
				if (vlrMesDealElement != null && !vlrMesDealElement.isJsonNull()) {
					deal.setVlrMesDeal(vlrMesDealElement.getAsString());	
				}
				if (updatedAtElement1 != null && !updatedAtElement1.isJsonNull()) {
					deal.setUpdated_atStr(updatedAtElement1.getAsString());	
				}
				if (ratingElement != null && !ratingElement.isJsonNull()) {
					deal.setRatingDeal(ratingElement.getAsBigDecimal());	
				}

				deal.setUpdated_at(convertStringToTimestamp(deal.getUpdated_atStr())); 

				if (postObject.has("organization")) {
					JsonObject organizationObj = post.getAsJsonObject().getAsJsonObject("organization");

					JsonElement idClientElement = organizationObj.get("id");
					JsonElement nameClientElement = organizationObj.get("name");
					JsonElement idUserElement = organizationObj.getAsJsonObject("user").get("id");
					JsonElement nameUserElement = organizationObj.getAsJsonObject("user").get("name");
					JsonElement emailUserElement = organizationObj.getAsJsonObject("user").get("email");

					if (idClientElement != null && !idClientElement.isJsonNull()) {
						org.setIdClient(idClientElement.getAsString());  
					}

					if (nameClientElement != null && !nameClientElement.isJsonNull()) {
						org.setNameClient(nameClientElement.getAsString().trim()); 
					}

					if (idUserElement != null && !idUserElement.isJsonNull()) {
						org.setIdUser(idUserElement.getAsString()); 

					}
					if (nameUserElement != null && !nameUserElement.isJsonNull()) {
						org.setNameUser(nameUserElement.getAsString()); 

					}
					if (emailUserElement != null && !emailUserElement.isJsonNull()) {
						org.setEmailUser(emailUserElement.getAsString()); 

					}

				}
				
//			if(!idClient.equals("63ebe3f0b46277000cc13d39")) {
//				logger.log(Level.INFO, "[getOportunidades]  return idClient: " + idClient);
//				return;
//			}else {
//				logger.log(Level.INFO, "[getOportunidades] deals: " + deals);
//				logger.log(Level.INFO, "[getOportunidades]  nameDeal: " + nameDeal);
//			}
			

				if (postObject.has("user")) {
					JsonObject userObj = post.getAsJsonObject().getAsJsonObject("user");

					JsonElement idVendElement = userObj.get("id");
					JsonElement nameVendElement = userObj.get("name");
					JsonElement emailVendElement = userObj.get("email");
					JsonElement nNameVendElement = userObj.get("nickname");

					if (idVendElement != null && !idVendElement.isJsonNull()) {
						vendedor.setIdVend(idVendElement.getAsString());	
					}
					if (nameVendElement != null && !nameVendElement.isJsonNull()) {
						vendedor.setNameVend(nameVendElement.getAsString());
					}
					if (emailVendElement != null && !emailVendElement.isJsonNull()) {
						vendedor.setEmailVend(emailVendElement.getAsString());
					}
					if (nNameVendElement != null && !nNameVendElement.isJsonNull()) {
						vendedor.setnNameVend(nNameVendElement.getAsString()); 
					}
				}
			
				JsonArray contactsJson = post.getAsJsonObject().getAsJsonArray("contacts");
				for (JsonElement contactElement : contactsJson) {
				    String title = null;
				    String name = null;
				    String emaill = null;
				    if (contactElement.getAsJsonObject().has("name") && !contactElement.getAsJsonObject().get("name").isJsonNull()) {
				        name = contactElement.getAsJsonObject().get("name").getAsString();
				    }

				    if (contactElement.getAsJsonObject().has("title") && !contactElement.getAsJsonObject().get("title").isJsonNull()) {
				        title = contactElement.getAsJsonObject().get("title").getAsString();
				    }
				    if (contactElement.getAsJsonObject().has("emails") && !contactElement.getAsJsonObject().getAsJsonArray("emails").isJsonNull()) {
				        JsonArray emails = contactElement.getAsJsonObject().getAsJsonArray("emails");
				        for (JsonElement email : emails) {
				            if (!email.getAsJsonObject().get("email").isJsonNull()) {
				                emaill = email.getAsJsonObject().get("email").getAsString();
				            }
				        }
				    }

				    List<Phone> ph = new ArrayList<>();
				    if (contactElement.getAsJsonObject().has("phones") && !contactElement.getAsJsonObject().getAsJsonArray("phones").isJsonNull()) {
				        JsonArray phones = contactElement.getAsJsonObject().getAsJsonArray("phones");
				        for (JsonElement phone : phones) {
				            String phonee = null;
				            String type = null;
				            if (!phone.getAsJsonObject().get("phone").isJsonNull()) {
				                phonee = phone.getAsJsonObject().get("phone").getAsString();
				            }

				            if (!phone.getAsJsonObject().get("type").isJsonNull()) {
				                type = phone.getAsJsonObject().get("type").getAsString();
				            }
				            ph.add(new Phone(phonee, type));

				        }
				    }
				    contact.add(new Contact(name, title, emaill, ph));
				}
				
				JsonArray produtos = post.getAsJsonObject().getAsJsonArray("deal_products");
				if (produtos != null) {
				    for (JsonElement produto : produtos) {
				        if (produto != null && !produto.isJsonNull() && produto.getAsJsonObject().has("id")) {
				        	
				        	if (produto.getAsJsonObject().has("id")) {
				        		prod.setIdProd(produto.getAsJsonObject().get("id").getAsString());  
				        	}
				        	if (produto.getAsJsonObject().has("name")) {
				        		prod.setNameProd(produto.getAsJsonObject().get("name").getAsString()); 
				        	}
				        	if (produto.getAsJsonObject().has("amount")) {
				        		prod.setQtdNeg(produto.getAsJsonObject().get("amount").getAsString());
				        	}
				        	if (produto.getAsJsonObject().has("discount_type")) {
				        		prod.setTipoDesc(produto.getAsJsonObject().get("discount_type").getAsString());
				        	}
				        	if (produto.getAsJsonObject().has("price")) {
				        		prod.setVlrProd(produto.getAsJsonObject().get("price").getAsString());
				        	}
				        	if (produto.getAsJsonObject().has("discount")) {
				        		prod.setVlrDesc(produto.getAsJsonObject().get("discount").getAsString()); 
				        	}
				            if (!produto.getAsJsonObject().get("total").isJsonNull()) {
				            	prod.setVlrTot(produto.getAsJsonObject().get("total").getAsBigDecimal());
				            }
				            if (produto.getAsJsonObject().get("updated_at") != null) {
				            	prod.setUpdated_at(produto.getAsJsonObject().get("updated_at").getAsString());
				            	prod.setUpdated_atProduct(convertStringToTimestamp(prod.getUpdated_at())); ;
				            }
				            
				        }
				    }
				}
				
			} catch (ParseException  e) {
				System.out.println("***********************  ERRO ****************************");
				System.out.println("***********************  ERRO ****************************");
				System.out.println(e.getMessage());
				System.out.println("***********************  ERRO ****************************");
			}
			
			
			System.out.println("***************************************************");
			System.out.println("");
			
			/*
			 * Validação da Ultima Execução
			 * 
			*/	
				
		
			Timestamp ultimaExecucao = NativeSql.getTimestamp("DTULTIMAEXECUCAO", "AD_CTRLEXECUCAOCRM", "SEQ = 1");
			logger.log(Level.INFO, "[getOportunidades]  ultimaExceucao   : " + ultimaExecucao 
					     + "\n                          updated_atDeal   : " + deal.getUpdated_at()
					     + "\n                          updated_atProduct: " + prod.getUpdated_atProduct()   );
		
			int ultimoDeal = deal.getUpdated_at().compareTo(ultimaExecucao);
			int ultimoProduct = -1;
			if(prod.getUpdated_atProduct() != null ) {
				ultimoProduct =prod.getUpdated_atProduct().compareTo(ultimaExecucao);
			}
			
			logger.log(Level.INFO, "[getOportunidades] ultimoDeal  " + ultimoDeal + " ultimoProduct  " + ultimoProduct);
			
			if(ultimoDeal >= 0  ||  ultimoProduct >= 0) {
				
				BigDecimal numOSOld  = buscarOrdemServico(deal.getIdDeal());
				
				if(numOSOld != null) {
					if (!deal.getDeal_stage().equals("L")) {
						UtilIntegracao.ProcessarDeal(deal, vendedor, prod,  numOSOld, token, contact, org);
					}
				}
			}
			 
		}
		logger.log(Level.INFO, "[getOportunidades]  ----------------------- FIM ----------------------- ");
	}

	private static BigDecimal buscarOrdemServico(String idDeal) throws Exception {
		logger.log(Level.INFO, "[buscarOrdemServico]  ----------------------- Inicio ----------------------- ");
		
		/*
		 * Buscar Situação da Ordem de Servico
		 * Se Estiver Aberta (P) - continuar o processamento
		 * Se Estiver Fechada (F) - buscar próximo registro
		*/
		String nomeInstancia = "OrdemServico"; //Tabela: TCSOSE
		EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();

		Collection<DynamicVO> notasList = dwfFacade.findByDynamicFinderAsVO(
				new FinderWrapper(nomeInstancia, "this.AD_IDDEALCRM = ? ", new Object[] { idDeal }));

		if (notasList.isEmpty()) {
			logger.log(Level.INFO, "[buscarOrdemServico]  NAO ENCONTRADA para o ID: " + idDeal);
			return null;
		}else {
			DynamicVO funVO = notasList.iterator().next();
			
			if( funVO.asString("SITUACAO") == null ||  funVO.asString("SITUACAO").equals("P")) {
				logger.log(Level.INFO, "[buscarOrdemServico]  OS: " + funVO.asBigDecimal("NUMOS") + " Situacao: " +  funVO.asString("SITUACAO"));
				return funVO.asBigDecimal("NUMOS");
			}
		}
		logger.log(Level.INFO, "[buscarOrdemServico]  ----------------------- Fim ----------------------- ");
		return null;
			
	}
	
}

