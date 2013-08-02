package org.seethrough.glass

import static javax.mail.Flags.Flag.*

import javax.mail.Flags
import javax.mail.Folder
import javax.mail.Session
import javax.mail.Store
import javax.mail.search.FlagTerm

import org.springframework.beans.factory.InitializingBean

class MailService implements InitializingBean {
	static transactional = false

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
		if (!config.username) return
		
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
