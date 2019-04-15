package org.jabref.logic.shared.prefs;

import org.junit.jupiter.api.Test;
import static org.junit.Assert.assertEquals;

public class SharedDatabasePreferencesTest {

    @Test
    public void givenPortName_whenGetPortNameMethod_thenReturnCorrectName() {
        SharedDatabasePreferences sut = new SharedDatabasePreferences();
        sut.setName("Donaldson");
        String result = sut.getName().toString();
        assertEquals("Optional[Donaldson]", result);
    }
// Trivial change
    @Test
    public void givenUser_whenGetUserMethod_thenReturnCorrectUserResults() {
        SharedDatabasePreferences sut = new SharedDatabasePreferences();
        sut.setUser("Benjamin");
        String result = sut.getUser().toString();
        assertEquals("Optional[Benjamin]", result);
    }

    @Test
    public void givenType_whenGetTypeMethod_thenReturnCorrectType() {
        SharedDatabasePreferences sut = new SharedDatabasePreferences();
        sut.setType("theBestType");
        String result = sut.getType().toString();
        assertEquals("Optional[theBestType]", result);
    }

    @Test
    public void givenPort_whenGetPortMethod_thenReturnCorrectPort() {
        SharedDatabasePreferences sut = new SharedDatabasePreferences();
        sut.setPort("theBestPort");
        String result = sut.getPort().toString();
        assertEquals("Optional[theBestPort]", result);
    }

}
