package karsai.laszlo.bringcloser;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import karsai.laszlo.bringcloser.utils.ApplicationUtils;

public class ApplicationUtilsDateConversionUnitTest {

    /*@Ignore
    @Test
    public void testGetUTCDateAndTime() {
        String input = "20180726184900";
        String result = ApplicationUtils.getUTCDateAndTime(null, input);
        String demandedResult = "20180726164900";
        Assert.assertEquals(result, demandedResult);
    }

    @Ignore
    @Test
    public void testGetLocalDateAndTimeToDisplay() {
        String input = "20180726164900";
        String result = ApplicationUtils.getLocalDateAndTimeToDisplay(null, input);
        String demandedResult = "2018 Jul 26 18:49";
        Assert.assertEquals(result, demandedResult);
    }

    @Ignore
    @Test
    public void testGetDateAndTime() {
        Date date = ApplicationUtils.getDateAndTime("20180726164900");
        Assert.assertNotNull(date);
    }*/

    @Test
    public void testTimeWentBy() {
        Map<String, Integer> demandedMap = new HashMap<>();
        demandedMap.put(ApplicationUtils.PDF_DAY_ID, 5);
        demandedMap.put(ApplicationUtils.PDF_HOUR_ID, 1);
        demandedMap.put(ApplicationUtils.PDF_MINUTE_ID, 1);
        demandedMap.put(ApplicationUtils.PDF_SEC_ID, 1);
        Map<String, Integer> resultMap = ApplicationUtils.getTimeWentByInfo(
                "20180606190514",
                "20180601180413"
        );
        boolean isOk
                = demandedMap.get(ApplicationUtils.PDF_DAY_ID)
                .equals(resultMap.get(ApplicationUtils.PDF_DAY_ID)) &&
                demandedMap.get(ApplicationUtils.PDF_HOUR_ID)
                        .equals(resultMap.get(ApplicationUtils.PDF_HOUR_ID)) &&
                demandedMap.get(ApplicationUtils.PDF_MINUTE_ID)
                        .equals(resultMap.get(ApplicationUtils.PDF_MINUTE_ID)) &&
                demandedMap.get(ApplicationUtils.PDF_SEC_ID)
                        .equals(resultMap.get(ApplicationUtils.PDF_SEC_ID));
        Assert.assertTrue(isOk);
    }
}
