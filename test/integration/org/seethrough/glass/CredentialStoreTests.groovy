package org.seethrough.glass

import static org.junit.Assert.*

import org.junit.Before
import org.junit.Test

import com.google.api.client.auth.oauth2.BearerToken
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.auth.oauth2.CredentialStore
import com.google.api.client.http.BasicAuthentication
import com.google.api.client.http.GenericUrl
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson.JacksonFactory
import com.google.api.client.testing.http.MockHttpTransport

class CredentialStoreTests {
	static JsonFactory JSON_FACTORY = new JacksonFactory()
	static String ACCESS_TOKEN = "abc"
	static String NEW_ACCESS_TOKEN = "def"
	static final GenericUrl TOKEN_SERVER_URL = new GenericUrl("http://example.com/token")
	private static final String CLIENT_ID = "id"
	private static final String CLIENT_SECRET = "secret"
	private static final String REFRESH_TOKEN = "refreshToken"
	private static final long EXPIRES_IN = 3600

	CredentialStore credentialStore
	User user

    @Before
    void setUp() {
        credentialStore = new GORMCredentialStore()
		user = new User(id: "id", username: "bob")
		user.id = "id"
		user.save()
    }

    @Test
    void testSaveCredentials() {
		def cred = createCredentialWithAccessToken(ACCESS_TOKEN)
        credentialStore.store("id", cred)

		user.refresh()

		def storedCredential = user.credential

		assert ACCESS_TOKEN == storedCredential.accessToken
		assert REFRESH_TOKEN == storedCredential.refreshToken
		assert EXPIRES_IN == storedCredential.expirationTimeMilliseconds
    }

	@Test
	void testUpdateCredentials() {
		def cred = createCredentialWithAccessToken(ACCESS_TOKEN)
		credentialStore.store("id", cred)

		cred = createCredentialWithAccessToken(NEW_ACCESS_TOKEN)
		credentialStore.store("id", cred)

		//user.refresh()
		user = User.get(user.id)

		def storedCredential = user.credential

		assert NEW_ACCESS_TOKEN == storedCredential.accessToken
		assert REFRESH_TOKEN == storedCredential.refreshToken
		assert EXPIRES_IN == storedCredential.expirationTimeMilliseconds
	}

	private Credential createCredentialWithAccessToken(accessToken) {
		Credential access = new Credential.Builder(
			BearerToken.queryParameterAccessMethod()).setTransport(new MockHttpTransport())
			.setJsonFactory(JSON_FACTORY)
			.setTokenServerUrl(TOKEN_SERVER_URL)
			.setClientAuthentication(new BasicAuthentication(CLIENT_ID, CLIENT_SECRET))
			.build()
			.setAccessToken(accessToken)
			.setRefreshToken(REFRESH_TOKEN)
			.setExpirationTimeMilliseconds(EXPIRES_IN)
		return access
	  }
}
