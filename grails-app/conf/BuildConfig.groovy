grails.project.work.dir = 'target'

grails.project.groupId = "org.seethrough.glass"

grails.project.dependency.resolution = {

	inherits 'global'
	log 'warn'
	
	repositories {
		grailsCentral()
		mavenLocal()
		mavenCentral()
	}

	dependencies {
		compile 'com.google.api-client:google-api-client:1.14.1-beta'
		compile 'com.google.http-client:google-http-client:1.14.1-beta'
		compile 'com.google.http-client:google-http-client-jackson:1.14.1-beta'
		compile 'com.google.apis:google-api-services-mirror:v1-rev2-1.14.1-beta'
		compile 'com.google.apis:google-api-services-oauth2:v1-rev33-1.14.1-beta'
		//compile 'com.google.oauth-client:google-oauth-client:1.14.1-beta'

		compile 'org.codehaus.jackson:jackson-core-asl:1.9.11'
		compile 'org.codehaus.jackson:jackson-mapper-asl:1.9.11'
		//compile 'commons-codec:commons-codec:1.7'
		//compile 'commons-logging:commons-logging:1.1.1'
		compile 'com.google.guava:guava:14.0.1'
		compile 'com.sun.mail:javax.mail:1.5.0'
	}

	plugins {
		test ":hibernate:$grailsVersion", {
			export = false
		}

		compile ":plugin-config:0.1.8"
		compile ":quartz:1.0-RC9"

		build ':release:2.2.1', ':rest-client-builder:1.0.3', {
			export = false
		}
	}
}
