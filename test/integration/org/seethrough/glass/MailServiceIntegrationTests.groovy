package org.seethrough.glass

import static org.junit.Assert.*

import org.junit.Before
import org.junit.Test

class MailServiceIntegrationTests {
	def mailService

    @Before
    void setUp() {
		// config for tests
		def config = new ConfigSlurper().parse(new File('test.cfg').toURI().toURL())
		mailService.config.host = "imap.gmail.com"
		mailService.config.username = config.username
		mailService.config.password = config.password
    }

    @Test
    void testConnectToGMail() {
        mailService.readMail()
    }
}
