// configuration for plugin testing - will not be included in the plugin zip

log4j = {
    // Example of changing the log pattern for the default console
    // appender:
    //
    //appenders {
    //    console name:'stdout', layout:pattern(conversionPattern: '%c{2} %m%n')
    //}

    error  'org.codehaus.groovy.glass.web.servlet',  //  controllers
           'org.codehaus.groovy.glass.web.pages', //  GSP
           'org.codehaus.groovy.glass.web.sitemesh', //  layouts
           'org.codehaus.groovy.glass.web.mapping.filter', // URL mapping
           'org.codehaus.groovy.glass.web.mapping', // URL mapping
           'org.codehaus.groovy.glass.commons', // core / classloading
           'org.codehaus.groovy.glass.plugins', // plugins
           'org.codehaus.groovy.glass.orm.hibernate', // hibernate integration
           'org.springframework',
           'org.hibernate',
           'net.sf.ehcache.hibernate'

    warn   'org.mortbay.log'
}

