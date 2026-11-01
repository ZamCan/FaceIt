package com.zamcan.faceit.messaging

import org.junit.Assert.assertEquals
import org.junit.Test

class SmsSegmentEstimatorTest {
    @Test fun emptyMessageUsesZeroSegments() {
        val result = SmsSegmentEstimator.estimate("")
        assertEquals(0, result.segments)
        assertEquals(0, result.units)
    }

    @Test fun gsmBasicBoundaryIs160Septets() {
        assertEquals(1, SmsSegmentEstimator.estimate("a".repeat(160)).segments)
        val result = SmsSegmentEstimator.estimate("a".repeat(161))
        assertEquals(2, result.segments)
        assertEquals(SmsSegmentEstimator.Encoding.GSM_7, result.encoding)
    }

    @Test fun extensionCharactersConsumeTwoSeptets() {
        val result = SmsSegmentEstimator.estimate("^")
        assertEquals(2, result.units)
        assertEquals(SmsSegmentEstimator.Encoding.GSM_7, result.encoding)
    }

    @Test fun concatenatedGsmUses153SeptetsPerSegmentAfterSinglePartLimit() {
        assertEquals(2, SmsSegmentEstimator.estimate("a".repeat(306)).segments)
        assertEquals(3, SmsSegmentEstimator.estimate("a".repeat(307)).segments)
    }

    @Test fun unicodeSwitchesWholeMessageToUcs2() {
        val result = SmsSegmentEstimator.estimate("hello🙂")
        assertEquals(SmsSegmentEstimator.Encoding.UCS_2, result.encoding)
        assertEquals(7, result.units) // five ASCII chars plus a UTF-16 surrogate pair
        assertEquals(1, result.segments)
    }

    @Test fun concatenatedUcs2Uses67CodeUnitsPerSegment() {
        assertEquals(1, SmsSegmentEstimator.estimate("あ".repeat(70)).segments)
        assertEquals(2, SmsSegmentEstimator.estimate("あ".repeat(71)).segments)
    }
}
