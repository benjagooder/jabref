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
    void setColor() {

    }

    @Test
    void getFace() {
    }

    @Test
    void setFace() {
    }

    @Test
    void getFont() {
    }

    @Test
    void setFont() {
    }

    @Test
    void getSize() {
    }

    @Test
    void setSize() {
    }

    @Test
    void getvalue() {
    }

    @Test
    void setvalue() {
    }
}
