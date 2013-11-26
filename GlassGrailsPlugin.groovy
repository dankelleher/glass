import org.seethrough.glass.TimelineCategory

import com.google.api.services.mirror.model.TimelineItem

class GlassGrailsPlugin {
	//def groupId = "org.seethrough.glass"
	
	def version = "0.4"
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

	def documentation = "https://github.com/dankelleher/glass"
	def organization = [name: 'SeeThrough Development', url: 'http://seethrough.dyndns.org']

	def license = "APACHE"
	def issueManagement = [system: 'GITHUB', url: 'https://github.com/dankelleher/glass/issues']
	def scm = [url: 'https://github.com/dankelleher/glass']

	def doWithDynamicMethods = {
		TimelineItem.metaClass.mixin TimelineCategory
	}
	
	def doWithApplicationContext = { applicationContext ->
		TimelineCategory.LINK_GENERATOR = applicationContext.getBean("grailsLinkGenerator")
	}
}
