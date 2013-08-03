package tellajoke



import grails.test.mixin.*
import org.junit.*

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(JokeService)
class JokeServiceTests {

    void testConnectionFailureToAPIReturnsConnectionFailureMessage() {
		service.endpoint = "http://0.0.0.0"
		def result = service.getJoke()
		
        assert result.startsWith("Error")
    }
	
	void testRetrieveJoke() {
		def result = service.getJoke()
		
		assert result != null
		assert !result.startsWith("Error")
		//assert result.isFunny() // TODO
	}
}
