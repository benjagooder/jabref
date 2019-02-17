package org.jabref.logic.formatter.casechanger;

import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

public class TitleTest {

    @Test
    public void GivenTitle_WhenTitleHello_ThenParse () {
        Title sut = new Title("Hello");
    }

    @Test
    public void GivenTitle_WhenTitleHelloWorld_ThenReturnHello(){
        Title sut = new Title("Hello World");
        sut.getFirstWord();
    }

    @Test
    public void GivenTitleGetFirstWord_WhenTitleEmpty_ThenReturnOptEmpty(){
        Title sut = new Title("");
        Optional<Word> w = Optional.empty();
        assertEquals(w, sut.getFirstWord());
    }

    @Test
    public void GivenTitle_WhenTitleHelloWorld_ThenReturnWorld(){
        Title sut = new Title("Hello World");
        sut.getLastWord();
    }

    @Test
    public void GivenTitleGetLastWord_WhenTitleEmpty_ThenReturnOptEmpty(){
        Title sut = new Title("");
        Optional<Word> w = Optional.empty();
        assertEquals(w, sut.getLastWord());
    }
}
