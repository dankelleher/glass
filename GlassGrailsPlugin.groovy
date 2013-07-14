import org.seethrough.glass.MirrorClient

class GlassGrailsPlugin {
	def groupId = "org.seethrough.glass"
	
    // the plugin version
    def version = "0.1"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.2 > *"
    // resources that are excluded from plugin packaging
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
        // TODO Implement additions to web.xml (optional), this event occurs before
    }

    def doWithSpring = {
    }

    def doWithDynamicMethods = { ctx ->
    }

    def doWithApplicationContext = { applicationContext ->
        // TODO Implement post initialization spring config (optional)
		def config = application.mergedConfig.grails.plugin.glass
		MirrorClient.APP_NAME = config.appname
		MirrorClient.IMAGE_URL = config.imageurl
    }

    def onChange = { event ->
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    def onConfigChange = { event ->
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
		println "Config changed"
		def config = application.mergedConfig.grails.plugin.glass
		MirrorClient.APP_NAME = config.appname
		MirrorClient.IMAGE_URL = config.imageurl
    }

    def onShutdown = { event ->
        // TODO Implement code that is executed when the application shuts down (optional)
    }
}
