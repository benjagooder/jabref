package org.jabref.logic.shared.security;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.NoSuchPaddingException;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PasswordTest {

    @Test
    public void GivenCharArrayThenCreatePasswordObject() throws NoSuchPaddingException, NoSuchAlgorithmException {
        char[] arr = {'a', 'b', 'c'};
        Password sut = new Password(arr, "test");
    }

    @Test
    public void GivenStringPhraseWhenPhraseHelloThenEncrypt() throws GeneralSecurityException, UnsupportedEncodingException {
        String result = "fugW1ePfHofGE6+kYzvhsg==";
        Password sut = new Password("hello", "test");
        assertEquals(sut.encrypt(), result);
    }

    @Test
    public void GivenBlankStringThenDecrypt() throws GeneralSecurityException, UnsupportedEncodingException {
        Password sut = new Password("", "");
        sut.encrypt();
        assertEquals(sut.decrypt(), "");
    }
}
