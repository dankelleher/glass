package org.seethrough.glass

class MailJob {
	static triggers = {
	  simple repeatInterval: 15000l // execute job once in 15 seconds
	}
	
	def mailService

	def execute() {
		mailService.readMail()
	}
}