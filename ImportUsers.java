package br.com.sankhya.ctba.integracaoapi;

import java.util.ArrayList;
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


public class ImportUsers {
	static final Logger logger = Logger.getLogger(ImportUsers.class.getName());

	static String token = "63851621c5e07d0016e05752";

	public static void main(String[] args) throws Exception {
		String id = "5f593719faba6300130f0d55";
		String name = Users(token, id); 
		System.out.println(name);
	}

	public static String Users(String token, String id) throws Exception {
		logger.log(Level.INFO, "[Users] INICIO");
			
			OkHttpClient client = new OkHttpClient();
			OkHttpClient.Builder builder = new OkHttpClient.Builder();
			client = builder.build();

			Request request = getJson(token); 
//			logger.log(Level.INFO, "[request] "+request);

			try (Response response = client.newCall(request).execute()) {
				if (!response.isSuccessful()) {

					throw new Exception("Falha no JSON: " + response.message());
				} else {
					//resposta = response.message();
					String res = response.body().string(); 
					JsonParser parser = new JsonParser();
					JsonObject jsobj = parser.parse(res).getAsJsonObject();

					List<User> user = new ArrayList<>();
					user = getUsers(res, request);
					
					return  User.getUserNameById(user, id) ;
				}
				
			} catch (Exception ex) {
		
				if (ex.getMessage() != null) {
					UtilIntegracao.salvaLogIntegracao("[ERROR]: "+ex.getMessage(), "Users");
					throw new Exception("Falha no JSON.");
				}
			} 

		logger.log(Level.INFO, "[Users] FIM");
		return null;
	}

	public static Request getJson(String token) throws Exception {
//		logger.log(Level.INFO, "[getJson]  ----------------------- INICIO ----------------------- ");
		
		Request request = new Request.Builder()
				.url("https://crm.rdstation.com/api/v1/users?token="+token)
				.get()
				.addHeader("accept", "application/json")
				.addHeader("content-type", "application/json") 
				.build();
		
		//https://crm.rdstation.com/api/v1/users?token=63851621c5e07d0016e05752
//		logger.log(Level.INFO, "[getJson]  ----------------------- FIM ----------------------- ");
		return request;
	}

	@SuppressWarnings("unused")
	private static List<User> getUsers(String usuario, Request request) throws Exception {
		logger.log(Level.INFO, "[getUsers]  ----------------------- INICIO ----------------------- ");

		List<User> user = new ArrayList<>();
		
		JsonParser parser = new JsonParser();
		JsonObject jsobj = parser.parse(usuario).getAsJsonObject();
		JsonArray posts = jsobj.getAsJsonArray("users");

		for (JsonElement post : posts) {
			String id = post.getAsJsonObject().get("id").getAsString();
			String name 		= post.getAsJsonObject().get("name").getAsString();
			user.add(new User(id, name));
		}
		
		
		
		
		
		
		
		/*
		
		
		JsonObject jsonObject = JsonParser.parseString(usuario).getAsJsonObject();

		if (jsonObject.has("users")) {
			JsonArray Users = jsonObject.getAsJsonArray("users");
			for (JsonElement activity : Users) {
				JsonObject activityObject = activity.getAsJsonObject();
				String id = activityObject.get("id").getAsString();
				String name = activityObject.get("name").getAsString();
				user.add(new User(id, name));
			}
		}
		*/
		return user;


	}
}
