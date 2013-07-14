grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"

grails.project.groupId = "org.seethrough.glass"

grails.project.dependency.resolution = {
	// inherit Grails' default dependencies
	inherits("global") {
		// uncomment to disable ehcache
		// excludes 'ehcache'
	}
	log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
	legacyResolve true //temp TODO use while testing with in-place plugins // whether to do a secondary resolve on plugin installation, not advised and here for backwards compatibility
	repositories {
		grailsCentral()
		mavenCentral()
		// uncomment the below to enable remote dependency resolution
		// from public Maven repositories
		//mavenLocal()
		//mavenRepo "http://snapshots.repository.codehaus.org"
		//mavenRepo "http://repository.codehaus.org"
		//mavenRepo "http://download.java.net/maven/2/"
		//mavenRepo "http://repository.jboss.com/maven2/"
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
		compile 'com.google.guava:guava:14.0.1'
		compile 'com.sun.mail:javax.mail:1.5.0'
	}

	plugins {
		runtime ":hibernate:$grailsVersion"
		build(":tomcat:$grailsVersion",
				":release:2.2.1",
				":rest-client-builder:1.0.3") {
					export = false
				}
		compile ":plugin-config:0.1.8"
		compile ":quartz:1.0-RC8"
	}
}
