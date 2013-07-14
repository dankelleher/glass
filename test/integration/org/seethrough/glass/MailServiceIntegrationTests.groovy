package org.seethrough.glass

import static org.junit.Assert.*
import grails.test.mixin.TestFor;

import org.junit.*

class MailServiceIntegrationTests {
	def mailService

    @Before
    void setUp() {
		// config for tests	// TODO remove before publication
		def config = new ConfigSlurper().parse(new File('test.cfg').toURI().toURL())
		mailService.config.host = "imap.gmail.com"
		mailService.config.username = config.username
		mailService.config.password = config.password
    }

    @After
    void tearDown() {
        // Tear down logic here
    }

    @Test
    void testConnectToGMail() {
        mailService.readMail()
    }
}
