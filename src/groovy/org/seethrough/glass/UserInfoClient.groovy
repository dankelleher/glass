package org.seethrough.glass

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.json.jackson.JacksonFactory
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpTransport

import com.google.api.services.oauth2.Oauth2
import com.google.api.services.oauth2.model.Userinfo

class UserInfoClient {
	static Userinfo getUserinfo(Credential credential) {
		HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport()
		Oauth2 oauth2 = new Oauth2.Builder(transport, new JacksonFactory(), credential)
			.setApplicationName("Cricket Glass")
			.build()
		return oauth2.userinfo().get().execute();
	}
	
	static String getUsername(Credential credential) {
		Userinfo userinfo = getUserinfo(credential)
		userinfo.email
	}
}
