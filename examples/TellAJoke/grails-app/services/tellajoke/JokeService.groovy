package tellajoke

import groovyx.net.http.RESTClient

class JokeService {

	String endpoint = "http://api.icndb.com/jokes/"
	
	// connects to the RESTful joke api and retrieves the joke text
    def getJoke() {
		def rest = new RESTClient(endpoint)

		try {
			def result = rest.get(path: "random")
			return result.getData()?.value?.joke	// calling result.data doesn't work
		} catch (ex) {
			log.error("Error retrieving joke", ex)
			return "Error: Joke server has been Norrisified"
		}
    }
}
