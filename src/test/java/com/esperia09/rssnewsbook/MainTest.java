package com.esperia09.rssnewsbook;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Assert;
import org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by esperia on 2016/06/05.
 */
@RunWith(JUnit4.class)
public class MainTest {

    @Test
    public void convertDate() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US);

        Date date = new Date(1469071850000L);
        String format = sdf.format(date);
        Assert.assertEquals("2016-07-21T12:30:50+0900", format);

        Date back = sdf.parse(format);
        Assert.assertEquals(back.getTime(), date.getTime());
    }

    @Test
    public void convertDateISO8601() {
        DateTime dateTime = new DateTime(1469071850000L);

        DateTimeFormatter fmt = ISODateTimeFormat.dateTimeNoMillis();
        String dateString = fmt.print(dateTime);
        Assert.assertEquals("2016-07-21T12:30:50+09:00", dateString);

        DateTime back = fmt.parseDateTime(dateString);
        Assert.assertEquals(back.getMillis(), dateTime.getMillis());
    }
}
