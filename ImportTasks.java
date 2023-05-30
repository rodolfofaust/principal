package br.com.sankhya.ctba.integracaoapi;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ImportTasks {
	static final Logger logger = Logger.getLogger(ImportTasks.class.getName());

	static String token = "63851621c5e07d0016e05752";

	public static void main(String[] args) throws Exception {
		String tarefa = null;
		String deal_id = "63ebe3f0b46277000cc13d37";
		tarefa = tasks(token, deal_id );
		
		System.out.println(tarefa); 
	}

	public static String tasks(String token, String deal_id) throws Exception {
		logger.log(Level.INFO, "[tasks] INICIO");
		String tarefa = "", cabecalho = "\n******************  TAREFAS ******************\n", result= null ;
		
		OkHttpClient client = new OkHttpClient();
		OkHttpClient.Builder builder = new OkHttpClient.Builder();
		client = builder.build();

		Request request = getJson(token, deal_id);

		try (Response response = client.newCall(request).execute()) {
			if (!response.isSuccessful()) {
				throw new Exception("Falha no JSON: " + response.message());
			} else {
				// resposta = response.message();
				String res = response.body().string();
				JsonParser parser = new JsonParser();
				JsonObject jsobj = parser.parse(res).getAsJsonObject();

				result = gettasks(res, request);
				if(result != null && result != "") {
					tarefa = cabecalho + result;
				}
				
				return tarefa;
				
			}
		} catch (Exception ex) {
			if (ex.getMessage() != null) {
//				UtilIntegracao.salvaLogIntegracao("[ERROR]: " + ex.getMessage(), "tasks");
				throw new Exception("Falha no JSON.");
			}
		} 

		logger.log(Level.INFO, "[tasks] FIM");
		return tarefa;
	}

	public static Request getJson(String token, String deal_id) throws Exception {
	
		Request request = new Request.Builder()
				.url("https://crm.rdstation.com/api/v1/tasks?token="+token+"&deal_id="+deal_id)
				.get()
				.addHeader("accept", "application/json")
				.addHeader("content-type", "application/json") 
				.build();
		
		
//		Request request = new Request.Builder()
//				.url("https://crm.rdstation.com/api/v1/tasks?token=63851621c5e07d0016e05752&deal_id=63ea39b3df9ffa001d597a72")
//				.get()
//				.addHeader("accept", "application/json")
//				.addHeader("content-type", "application/json") 
//				.build();
		
		return request;
	}

	private static String gettasks(String tar, Request request) throws Exception {
		logger.log(Level.INFO, "[gettasks]  ----------------------- INICIO ----------------------- ");
		logger.log(Level.INFO, "[gettasks]  request: " + request);
		JsonParser parser = new JsonParser();
		JsonObject jsobj = parser.parse(tar).getAsJsonObject();
		List<Task> tasks = new ArrayList<>();
		
		if (jsobj.has("tasks")) {
			JsonArray posts = jsobj.getAsJsonArray("tasks");
			
			if (posts != null) {	
		        for (JsonElement element : posts) {
		            JsonObject taskJsonObject = element.getAsJsonObject();
		            Task task = new Task();

		            if (taskJsonObject.has("subject")) {
		            	if (!taskJsonObject.get("subject").isJsonNull()) {
		            		task.setSubject(taskJsonObject.get("subject").getAsString());
		            	}
		            }
		            if (taskJsonObject.has("notes")) {
		            	if (!taskJsonObject.get("notes").isJsonNull()) {
		            		task.setNotes(taskJsonObject.get("notes").getAsString());
		            	}
		            }
		            if (taskJsonObject.has("created_at")) {
		            	if (!taskJsonObject.get("created_at").isJsonNull()) {
		            		task.setCreatedAt(taskJsonObject.get("created_at").getAsString());
		            	}
		            }
	//	            task.setType(taskJsonObject.get("type").getAsString());
	//	            task.setHour(taskJsonObject.get("hour").getAsString());
	//	            task.setMarkup(taskJsonObject.get("markup").getAsString());
	//	            task.setDone(taskJsonObject.get("done").getAsBoolean());
	//	            task.setUserIds(parseStringList(taskJsonObject.getAsJsonArray("user_ids")));
		            
	//	            task.setDealId(taskJsonObject.get("deal_id").getAsString());
		            
	//	            if(!taskJsonObject.get("done_date").isJsonNull())
	//	            	task.setDoneDate(taskJsonObject.get("done_date").getAsString());
		         
		            
	//	            task.setDate(taskJsonObject.get("date").getAsString());
	
	/*	            JsonObject dealJsonObject = taskJsonObject.getAsJsonObject("deal");
		            Deal deal = new Deal();
		            deal.setId(dealJsonObject.get("id").getAsString());
		            deal.setName(dealJsonObject.get("name").getAsString());
		            if(!dealJsonObject.get("hold").isJsonNull())
		            	deal.setHold(dealJsonObject.get("hold").getAsString());
		            deal.setRating(dealJsonObject.get("rating").getAsInt());
		            task.setDeal(deal);
	*/	            
		        	List<User> user = new ArrayList<>();
		            JsonArray users = taskJsonObject.getAsJsonArray("users");
		            
		            if(users != null) {
			            for (JsonElement u : users) {
			            	if(!u.getAsJsonObject().get("id").isJsonNull() && !u.getAsJsonObject().get("name").isJsonNull()) {
			            	
			            		String id = u.getAsJsonObject().get("id").getAsString();
			            		String name = u.getAsJsonObject().get("name").getAsString();
			            		user.add(new User(id, name));
			            	}
			            }
			            task.setUsers(user);
			            tasks.add(task);
		            }
		        }	
			}
		}
	        String tarefa = "";
	        for (Task a : tasks) {
	        	String dataCriacao = convertStringToTimestamp(a.getCreatedAt(), "yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
//	        	System.out.println("Assunto: " + a.getSubject() + "\nDescrição: " + a.getNotes() +"\nData Criação: " + dataCriacao );
	        	tarefa += "Assunto: " + a.getSubject() + "\nDescrição: " + a.getNotes() +"\nData Criação: " + dataCriacao ;
	        	
	        	List<User> u1 = a.getUsers();
	        	int n= 1;
				for (User usuario : u1) {
//					System.out.println("\nResponsável"+"(" + n + "): " + usuario.getName() );
					tarefa += "\nResponsável"+"(" + n + "): " + usuario.getName() ;
					n += 1 ; 
				}
//				System.out.println("\n");
				tarefa += "\n\n";
			}
	        
	    	logger.log(Level.INFO, "[gettasks]  ----------------------- FIM ----------------------- ");    
	        return tarefa;
	}
	
	public static String convertStringToTimestamp(String strDate, String formato) {
		try {
			DateFormat formatter = new SimpleDateFormat(formato);
			// you can change format of date
			Date date = formatter.parse(strDate);
			Timestamp timeStampDate = new Timestamp(date.getTime());
			
			SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
			String formattedDate = format.format(timeStampDate);

			return formattedDate;
		} catch (ParseException e) {
			System.out.println("Exception :" + e);
			return null;
		}
	}
	
	 /*       private static List<String> parseStringList(JsonArray jsonArray) {
	            List<String> list = new ArrayList<>();
	            for (JsonElement element : jsonArray) {
	                list.add(element.getAsString());
	            }
	            return list;
	        }
	*/
}
