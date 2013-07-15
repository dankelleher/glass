import org.seethrough.glass.MirrorClient

class GlassGrailsPlugin {
	def groupId = "org.seethrough.glass"
	
	def version = "0.1"
	def grailsVersion = "2.0 > *"

	def title = "Glass Plugin"
	def author = "Daniel Kelleher"
	def authorEmail = "dankelleher@yahoo.com"
	def description = '''\
The Grails Glass Plugin allows you to quickly develop Grails apps for the upcoming Google Glass platform, via the Mirror API. 
The plugin takes care of the following common tasks: 
OAuth2 user authentication with Google
Posting and adding contacts to a Glass timeline
Subscribing to notifications from Glass, triggered by user actions.
'''

	def documentation = "http://glass.org/plugin/glass"
	def organization = [name: 'SeeThrough Development', url: 'http://seethrough.dyndns.org']

	def license = "APACHE"
	def issueManagement = [system: 'GITHUB', url: 'https://github.com/dankelleher/glass/issues']
	def scm = [url: 'https://github.com/dankelleher/glass']

	def doWithApplicationContext = { applicationContext ->
		def config = application.mergedConfig.grails.plugin.glass
		MirrorClient.APP_NAME = config.appname
		MirrorClient.IMAGE_URL = config.imageurl
	}

	def onConfigChange = { event ->
		def config = application.mergedConfig.grails.plugin.glass
		MirrorClient.APP_NAME = config.appname
		MirrorClient.IMAGE_URL = config.imageurl
	}
}
