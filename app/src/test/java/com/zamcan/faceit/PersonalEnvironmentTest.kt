package com.zamcan.faceit
import com.zamcan.faceit.data.PersonalEnvironment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
class PersonalEnvironmentTest {
    @Test fun defaultCompositionIsStoneGuardian() {
        val v=PersonalEnvironment()
        assertEquals("Football",v.background); assertEquals("Lion sculpture",v.mainObject)
        assertEquals("Natural stone",v.material); assertEquals("Subtle",v.motion)
        assertTrue(v.widgetsEnabled); assertTrue(v.iconsEnabled)
    }
    @Test fun environmentJsonRoundTrips() {
        val v=PersonalEnvironment(dialerStyle="Natural Stone",resourceProfile="Essential")
        assertEquals(v,PersonalEnvironment.fromJson(v.toJson()))
    }
}
