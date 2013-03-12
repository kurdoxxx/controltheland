package com.btxtech.game;

import com.btxtech.game.services.AbstractServiceTest;
import junit.framework.Assert;
import org.junit.ComparisonFailure;
import org.junit.Test;

/**
 * User: beat
 * Date: 26.01.13
 * Time: 16:20
 */

public class TestTest {

    @Test
    public void assertStringIgnoreWhitespace() {
        AbstractServiceTest.assertStringIgnoreWhitespace("  hallo.du xxxx\n\rcccc", "hallo.duxxxxcccc");
        AbstractServiceTest.assertStringIgnoreWhitespace("\tx.{}.XX\naaa.bbb", "x.{   }.XX\taaa.bbb");
        String expected = "Adcell.user.track({\n" +
                "    'pid':'3111',\n" +
                "    'eventid':'3820',\n" +
                "    'bid':'hallohallo',\n" +
                "    'referenz':'1'\n" +
                "});";
        String actual = "Adcell.user.track({\n" +
                "'pid':'3111'," +
                "'eventid':'3820'," +
                "'bid':'hallohallo'," +
                "'referenz':'1'" +
                "});";
        AbstractServiceTest.assertStringIgnoreWhitespace(expected, actual);
    }

    @Test
    public void assertStringIgnoreWhitespaceFail() {
        String expected = "Adcell.user.track({\n" +
                "    'pid':'3110',\n" +
                "    'eventid':'3820',\n" +
                "    'bid':'hallohallo',\n" +
                "    'referenz':'1'\n" +
                "});";
        String actual = "Adcell.user.track({\n" +
                "'pid':'3111'," +
                "'eventid':'3820'," +
                "'bid':'hallohallo'," +
                "'referenz':'1'" +
                "});";
        try {
            AbstractServiceTest.assertStringIgnoreWhitespace(expected, actual);
            Assert.fail("ComparisonFailure expected");
        } catch (ComparisonFailure e) {

        }
    }
}