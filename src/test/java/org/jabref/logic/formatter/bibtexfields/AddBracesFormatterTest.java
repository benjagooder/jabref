package org.jabref.logic.formatter.bibtexfields;

import org.jabref.logic.l10n.Language;
import org.jabref.logic.l10n.Localization;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests in addition to the general tests from {@link org.jabref.logic.formatter.FormatterTest}
 */
class AddBracesFormatterTest {

    private AddBracesFormatter formatter;

    @BeforeEach
    public void setUp() {
        formatter = new AddBracesFormatter();
    }

    @Test
    public void formatAddsSingleEnclosingBraces() {
        assertEquals("{test}", formatter.format("test"));
    }

    @Test
    public void formatKeepsUnmatchedBracesAtBeginning() {
        assertEquals("{test", formatter.format("{test"));
    }

    @Test
    public void formatKeepsUnmatchedBracesAtEnd() {
        assertEquals("test}", formatter.format("test}"));
    }

    @Test
    public void formatKeepsShortString() {
        assertEquals("t", formatter.format("t"));
    }

    @Test
    public void formatKeepsEmptyString() {
        assertEquals("", formatter.format(""));
    }

    @Test
    public void formatKeepsDoubleEnclosingBraces() {
        assertEquals("{{test}}", formatter.format("{{test}}"));
    }

    @Test
    public void formatKeepsTripleEnclosingBraces() {
        assertEquals("{{{test}}}", formatter.format("{{{test}}}"));
    }

    @Test
    public void formatKeepsNonMatchingBraces() {
        assertEquals("{A} and {B}", formatter.format("{A} and {B}"));
    }

    @Test
    public void formatKeepsOnlyMatchingBraces() {
        assertEquals("{{A} and {B}}", formatter.format("{{A} and {B}}"));
    }

    @Test
    public void formatDoesNotRemoveBracesInBrokenString() {
        // We opt here for a conservative approach although one could argue that "A} and {B}" is also a valid return
        assertEquals("{A} and {B}}", formatter.format("{A} and {B}}"));
    }

    @Test
    public void formatExample() {
        assertEquals("{In CDMA}", formatter.format(formatter.getExampleInput()));
    }

    @Test
    public void givenLocalizationLanguageSetToEnglish_whenGetNameMethod_thenAddEnclosingBracesReturned() {
        Localization.setLanguage(Language.English);
        assertEquals("Add enclosing braces", formatter.getName());
    }

    @Test
    public void givenLocalizationLanguageSetToEnglish_whenGetDescriptionMethod_thenAddBracesMsgReturned() {
        Localization.setLanguage(Language.English);
        assertEquals("Add braces encapsulating the complete field content.", formatter.getDescription());
    }

    @Test
    public void whenGetKeyMethod_thenAdd_BracesReturned() {
        assertEquals("add_braces", formatter.getKey());
    }

    @Test
    public void whenGetExampleInputMethod_thenInCDMAWithSpaceReturned() {
        assertEquals("In CDMA", formatter.getExampleInput());
    }
}
