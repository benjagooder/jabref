package org.jabref.logic.preferences;

import org.junit.Before;
import org.junit.jupiter.api.Test;

import java.sql.Time;

import static org.junit.jupiter.api.Assertions.*;

class TimestampPreferencesTest {

    TimestampPreferences sut = new TimestampPreferences(true, false, "Time", "Test", false);

    @Test
    public void givenTimeStamp_whenTimeStampTime_thenReturnTime() {
        assertEquals("Time", sut.getTimestampField());
    }

    @Test
    public void givenFormate_whenFormatTest_thenReturnTest() {
        assertEquals("Test", sut.getTimestampFormat());
    }
}
