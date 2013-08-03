package org.seethrough.glass

import static org.junit.Assert.*

import grails.test.mixin.*
import grails.test.mixin.support.*
import org.junit.*

import com.google.api.services.mirror.model.TimelineItem;


@TestFor(NotifyService)
class NotifyServiceTests {
	static final String CARD_ID = "Card ID"
	static final String IMAGE_FILE = "test/unit/cat.png"
	
	def user
	def card
	
	def attachmentIds = []

	@Before
    void setUp() {
        user = new User()
		card = new TimelineItem()
		
		// stub out mirror service
		service.mirrorService = [
			getAttachmentIds : { user, cardId -> attachmentIds},
			getAttachmentInputStream : { user, cardId, attachmentId -> getImageStream() }
			]
    }
	
	def getImageStream() {
		return new FileInputStream(IMAGE_FILE)
	}

	@Test
    void testGetAttachedImagesForCardWithNoAttachmentsReturnsEmptyList() {
        assert service.getAttachedImages(user, CARD_ID) == []
    }
	
	@Test
	void testGetAttachedImageForCardWithAttachment() {
		def attachmentId = "Attachment ID"		
		attachmentIds << attachmentId
		
		def images = service.getAttachedImages(user, CARD_ID)
		
		assert images.size() == 1
		
		images[0].with {
			assert width == 500
			assert height == 333
		}
	}
}
