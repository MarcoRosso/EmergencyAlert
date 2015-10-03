package com.marco.emergencyalert;



import com.google.gson.Gson;


public class JsonUtils {
	public User parseUserFromJson(String jsonData){
		Gson gson = new Gson();
		User user = gson.fromJson(jsonData, User.class);
		return user;
		
	}
}
