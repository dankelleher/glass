import org.seethrough.glass.MirrorClient

class GlassGrailsPlugin {
	def groupId = "org.seethrough.glass"
	
    def version = "0.1"
    def grailsVersion = "2.2 > *"

    def pluginExcludes = [
        "grails-app/views/error.gsp"
    ]

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


	def dependsOn = [
		"plugin-config": "0.1.8",
		"quartz" : "1.0-RC8"
		]

    def doWithWebDescriptor = { xml ->
    }

    def doWithSpring = {
    }

    def doWithDynamicMethods = { ctx ->
    }

    def doWithApplicationContext = { applicationContext ->
		def config = application.mergedConfig.grails.plugin.glass
		MirrorClient.APP_NAME = config.appname
		MirrorClient.IMAGE_URL = config.imageurl
    }

    def onChange = { event ->
    }

    def onConfigChange = { event ->
		def config = application.mergedConfig.grails.plugin.glass
		MirrorClient.APP_NAME = config.appname
		MirrorClient.IMAGE_URL = config.imageurl
    }

    def onShutdown = { event ->
    }
}
