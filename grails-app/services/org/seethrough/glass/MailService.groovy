package org.seethrough.glass

import javax.mail.*
import javax.mail.search.*
import java.util.Properties
import java.security.*
import org.springframework.beans.factory.InitializingBean

import static javax.mail.Flags.Flag.*

class MailService implements InitializingBean {
	def transactional = false
	
	def grailsApplication
	
	def messageHandlerService	// implemented in the main project 
	
	def config

	void afterPropertiesSet() {
		readConfig()
	}
	
	void readConfig() {
		config = grailsApplication.mergedConfig.grails.plugin.glass
	}

	def readMail() {	
		def session = Session.getDefaultInstance(new Properties(), null)
		def store = session.getStore("imaps")
		def inbox

		try {
			inbox = openInbox(store)

			def messages = unseenMails(inbox)
			messages.each { msg ->
				handleMessage(msg)
				msg.setFlag(SEEN, true)
			}
		} catch (Exception e) {
			log.error e
			throw e
		} finally {
			inbox?.close(true)
			store.close()
		}

	}

	private handleMessage(msg) {
		def msgMap = extractContents(msg)
		messageHandlerService?.email(msgMap)
	}

	private Folder openInbox(Store store) {
		store.connect(
				config.host,
				config.username,
				config.password)
		def inbox = store.getFolder("Inbox")
		inbox.open(Folder.READ_WRITE)
		return inbox
	}
	
	def unseenMails(inbox) { 
		inbox.search(new FlagTerm(new Flags(SEEN), false))
	}
	
	def extractContents(msg) {
		def from = msg.from[0].getAddress()
		def user = User.findByUsername(from)
		
		[from: user, subject: msg.subject, body: msg.contentStream.getText().trim()]
	}
}
