package com.zamcan.faceit.data

import org.junit.Assert.*
import org.junit.Test

class SavedCompositionCodecTest {
    @Test fun snapshotsPreserveAllLayersAndMetadataAcrossReload() {
        val environment = PersonalEnvironment(name = "My scene", selectedArtworkFile = "background.png",
            selectedObjectFile = "object.webp", material = "Marble", nameEnabled = true,
            nameText = "Amina", useProceduralBackground = true)
        val items = listOf(SavedComposition("one", environment, 123L),
            SavedComposition("two", environment.copy(name = "Copy", material = "Glass"), 456L))
        assertEquals(items, SavedCompositionCodec.decode(SavedCompositionCodec.encode(items)))
    }
    @Test fun emptyLibraryRoundTrips() {
        assertEquals(emptyList<SavedComposition>(), SavedCompositionCodec.decode("[]"))
    }
    @Test(expected = Exception::class) fun corruptLibraryIsReportedInsteadOfDiscarded() {
        SavedCompositionCodec.decode("{broken")
    }
    @Test(expected = IllegalArgumentException::class) fun duplicateIdsCannotOverwriteAnotherSnapshot() {
        val item = SavedComposition("same", PersonalEnvironment(), 123)
        SavedCompositionCodec.decode(SavedCompositionCodec.encode(listOf(item, item)))
    }
}

