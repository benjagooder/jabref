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

    @Test
    public void givenUser_whenGetUserMethod_thenCorrectUserResults() {
        SharedDatabasePreferences sut = new SharedDatabasePreferences();
        sut.setUser("Benjamin");
        String result = sut.getUser().toString();
        assertEquals("Optional[Benjamin]", result);
    }

}
