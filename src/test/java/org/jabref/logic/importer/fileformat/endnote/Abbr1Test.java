package org.jabref.logic.importer.fileformat.endnote;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Abbr1Test {

    @Test
    void givenStyle_whenGetStyle_thenGetStyle() {
        Abbr1 sut = new Abbr1();
        Style newStyle = new Style();
        sut.setStyle(newStyle);
        Style sutStyle = sut.getStyle();
        assertEquals(newStyle, sutStyle);
    }
}
