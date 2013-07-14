package org.seethrough.glass

import com.google.api.client.auth.oauth2.CredentialStore
import com.google.api.client.auth.oauth2.Credential
import grails.validation.ValidationException

import org.apache.commons.logging.LogFactory;
import org.springframework.validation.Errors

/**
 * Copyright: Daniel Kelleher Date: 02.06.13 Time: 23:40
 */
class GORMCredentialStore implements CredentialStore {
	private static final LOG = LogFactory.getLog(this)
	
    @Override
    boolean load(String userId, Credential credential) {
        User user = User.get(userId)

        if (!user) return false

        UserCredential storedCredential = user.credential

        if (!storedCredential) return false

        credential.accessToken = storedCredential.accessToken
        credential.refreshToken = storedCredential.refreshToken
        credential.expirationTimeMilliseconds = storedCredential.expirationTimeMilliseconds

        return true
    }

    @Override
    void store(String userId, Credential credential) {
        User user = User.get(userId)

        if (!user) throw new NullPointerException("No such user with ID $userId")

        def cred = UserCredential.findOrCreateWhere(user: user)
		cred.with {
                accessToken = credential.accessToken
                refreshToken = credential.refreshToken
                expirationTimeMilliseconds = credential.expirationTimeMilliseconds
				save()
        }

        if (cred.hasErrors()) throw new ValidationException("Errors in credential: ${cred.errors}", cred.errors)
    }

    @Override
    void delete(String userId, Credential credential) {
        User user = User.get(userId)

        if (user) {
            user.credential.delete()
        }
    }
}
