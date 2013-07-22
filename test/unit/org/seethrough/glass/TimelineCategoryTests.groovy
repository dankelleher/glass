package org.seethrough.glass

import static org.junit.Assert.*
import static org.seethrough.glass.TimelineAction.*

import org.junit.Before
import org.junit.Test

import grails.test.mixin.Mock
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin

import com.google.api.services.mirror.model.TimelineItem

@TestMixin(GrailsUnitTestMixin)
class TimelineCategoryTests {
	
	static final String ACTION = "actionname"
	static final String ACTION_DISPLAYNAME = "ActionName"

	TimelineItem timelineItem
	
	@Before
	void setUp() {
		TimelineItem.metaClass.mixin TimelineCategory
		TimelineCategory.LINK_GENERATOR = [resource : {params -> "http://dummy.link.to/static/${params.dir}/${params.file}"}]
		
		timelineItem = new TimelineItem()
	}
	
	@Test
	void testAddCustomItem() {		
		timelineItem.addCustomAction(ACTION_DISPLAYNAME)
		
		def action = timelineItem.menuItems[0]
		
		assert action.id == ACTION
		assert action.values[0].displayName == ACTION_DISPLAYNAME
		assert action.values[0].iconUrl == "http://dummy.link.to/static/images/actions/${ACTION}.png"
	}
	
	@Test
	void testAddCustomItemWithSpaces() {
		def displayNameWithSpaces = "Action Name"
		
		timelineItem.addCustomAction(displayNameWithSpaces)
		
		def action = timelineItem.menuItems[0]
		
		assert action.id == ACTION
		assert action.values[0].displayName == displayNameWithSpaces
		assert action.values[0].iconUrl == "http://dummy.link.to/static/images/actions/${ACTION}.png"
	}
	
	@Test
	void testAddBuiltInItem() {
		timelineItem.addAction(TOGGLE_PINNED)
		
		def action = timelineItem.menuItems[0]
		
		assert action.action == TOGGLE_PINNED.toString()
	}
	
	@Test
	void testAddMultipleItems() {
		timelineItem.with {
			addCustomAction(ACTION_DISPLAYNAME)
			addAction(REPLY)
			addAction(DELETE)
			
			assert menuItems[0].id == ACTION
			assert menuItems[1].action == REPLY.toString()
			assert menuItems[2].action == DELETE.toString()
		}
	}
}
