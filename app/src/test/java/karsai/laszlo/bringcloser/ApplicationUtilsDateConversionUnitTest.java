package karsai.laszlo.bringcloser;

import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

import karsai.laszlo.bringcloser.utils.ApplicationUtils;

public class ApplicationUtilsDateConversionUnitTest {

    @Test
    public void testGetUTCDateAndTime() {
        String input = "20180726184900";
        String result = ApplicationUtils.getUTCDateAndTime(null, input);
        String demandedResult = "20180726164900";
        Assert.assertEquals(result, demandedResult);
    }

    @Test
    public void testGetLocalDateAndTimeToDisplay() {
        String input = "20180726164900";
        String result = ApplicationUtils.getLocalDateAndTimeToDisplay(null, input);
        String demandedResult = "2018 Jul 26 18:49";
        Assert.assertEquals(result, demandedResult);
    }

    @Test
    public void testGetDateAndTime() {
        Date date = ApplicationUtils.getDateAndTime("20180726164900");
        Assert.assertNotNull(date);
    }
}
