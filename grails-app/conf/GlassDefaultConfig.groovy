grails {
	plugin {
		glass {
			host = "imap.gmail.com"
			username = "a valid gmail address"
			password = "the gmail password"
			appname = "Default Glass App"
			imageUrl = ""
			oauth {
				clientid = "a google oauth2 client id"
				clientsecret = "an oauth2 client secret"   // TODO encrypt or store in a secure key-store
			}
		}
	}
}