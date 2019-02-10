package org.jabref.gui;

import javafx.stage.Stage;
import org.jabref.model.entry.BibEntry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
//trying to find how this is linked to BibTexKey Duplicate Check throws NPE Bug#4614
class DuplicateResolverDialogTest {

    @Test
    void givenUninitializedStage_whenInitializing_thenNoError() {
        Stage sut;
    }

    @Test
    void givenNewBibEntry_whenInitializing_thenNoError() {
        BibEntry sut = new BibEntry();

    }

    @Test
    void givenNewDuplicateResolverTypeIMPORT_CHECK_whenInitializing_thenEqualsIMPORT_CHECK() {
        DuplicateResolverDialog.DuplicateResolverType sut = DuplicateResolverDialog.DuplicateResolverType.IMPORT_CHECK;
        assertEquals(DuplicateResolverDialog.DuplicateResolverType.IMPORT_CHECK,sut);
    }

    @Test
    void givenNewDuplicateResolverResultKEEP_LEFT_whenInitializing_thenEqualsIMPORT_CHECK() {
        DuplicateResolverDialog.DuplicateResolverResult sut = DuplicateResolverDialog.DuplicateResolverResult.KEEP_LEFT;
        assertEquals(DuplicateResolverDialog.DuplicateResolverResult.KEEP_LEFT,sut);
    }

}
