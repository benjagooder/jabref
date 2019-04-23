package org.jabref.logic.importer.fileformat.endnote;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthorTest {

    Author sut = new Author();

    @Test
    void givenGenTechName_whenSetCorpName_thenGetGenTech() {
        String testName = "GenTech";
        sut.setCorpName(testName);
        String sutCorpName = sut.getCorpName();
        assertEquals(testName, sutCorpName);
    }

    @Test
    void givenJerryName_whenSetFirstName_thenGetJerry() {
        String testName = "Harry";
        sut.setFirstName(testName);
        String sutFirstName = sut.getFirstName();
        assertEquals(testName, sutFirstName);
    }

    @Test
    void givenJPInitials_whenSetInitials_thenGetJP() {
        String testInitials = "JP";
        sut.setInitials(testInitials);
        String sutInitials = sut.getInitials();
        assertEquals(testInitials, sutInitials);
    }

    @Test
    void givenHendersonName_whenSetLastName_thenGetHenderson() {
        String testName = "Henderson";
        sut.setLastName(testName);
        String sutLastName = sut.getLastName();
        assertEquals(testName, sutLastName);
    }

    @Test
    void givenA_whenSetMiddleInitial_thenGetA() {
        String testName = "Harry";
        sut.setMiddleInitial(testName);
        String sutMiddleInitial = sut.getMiddleInitial();
        assertEquals(testName, sutMiddleInitial);
    }

    @Test
    void givenScientistRole_whenSetRole_thenGetScientist() {
        String testName = "Scientist";
        sut.setRole(testName);
        String sutRole = sut.getRole();
        assertEquals(testName, sutRole);
    }

    @Test
    void givenHelloSalutation_whenSetSalutation_thenGetHello() {
        String testName = "Hello";
        sut.setSalutation(testName);
        String sutSalutation = sut.getSalutation();
        assertEquals(testName, sutSalutation);
    }

    @Test
    void givenCPSuffix_whenSetSuffix_thenGetCP() {
        String testSuffix = "CP";
        sut.setSuffix(testSuffix);
        String sutSuffix = sut.getSuffix();
        assertEquals(testSuffix, sutSuffix);
    }

    @Test
    void givenMrTitle_whenSetTitle_thenGetMr() {
        String testSuffix = "CP";
        sut.setSuffix(testSuffix);
        String sutSuffix = sut.getSuffix();
        assertEquals(testSuffix, sutSuffix);
    }

    @Test
    void givenPurpleColorStyle_whenSetStyle_thenGetPurpleColorStyle() {
        Style testStyle = new Style();
        String testColor = "Purple";
        testStyle.setColor(testColor);
        sut.setStyle(testStyle);
        Style sutStyle = sut.getStyle();
        String sutStyleColor = sutStyle.getColor();
        assertEquals(testStyle, sutStyle);
        assertEquals(testColor, sutStyleColor);
    }
    
}
