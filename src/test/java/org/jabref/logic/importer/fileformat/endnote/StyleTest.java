package org.jabref.logic.importer.fileformat.endnote;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StyleTest {

    Style sut = new Style();

    @Test
    void givenColorPurple_whenSetColor_thenColorIsPurple() {
        String newColor = "purple";
        sut.setColor(newColor);
        String styleColor = sut.getColor();
        assertEquals(newColor,styleColor);
    }

    @Test
    void givenTomCruiseFace_whenSetFace_thenGetTomCruiseFace() {
        String newFace = "Tom Cruise";
        sut.setFace(newFace);
        String sutFace = sut.getFace();
        assertEquals(newFace, sutFace);
    }

    @Test
    void givenNullFace_whenSetFace_thenGetNormal() {
        String nullFace = null;
        sut.setFace(nullFace);
        String sutFace = sut.getFace();
        String testString = "normal";
        assertEquals(testString, sutFace);
    }

    @Test
    void givenHelveticaFont_whenSetFont_thenGetHelvetica() {
        String newFont = "helvetica";
        sut.setFont(newFont);
        String sutFont = sut.getFont();
        assertEquals(newFont, sutFont);
    }

    @Test
    void givenNullFont_whenSetFont_thenGetDefault() {
        String nullFont = null;
        sut.setFont(nullFont);
        String sutFont = sut.getFont();
        String testFont = "default";
        assertEquals(testFont, sutFont);
    }

    @Test
    void givenBigSize_whenSetSize_thenGetBig() {
        String newSize = "big";
        sut.setSize(newSize);
        String sutSize = sut.getSize();
        assertEquals(newSize, sutSize);
    }

    @Test
    void givenSupremeValue_whenSetValue_thenGetSupreme() {
        String newValue = "supreme";
        sut.setvalue(newValue);
        String sutValue = sut.getvalue();
        assertEquals(newValue, sutValue);
    }
}
