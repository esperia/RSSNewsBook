package com.esperia09.rssnewsbook;

import static org.junit.Assert.*;

import com.esperia09.rssnewsbook.rest.Api;
import com.esperia09.rssnewsbook.rest.Updater;
import com.esperia09.rssnewsbook.rss.Feed;
import com.esperia09.rssnewsbook.rss.FeedMessage;
import com.esperia09.rssnewsbook.rss.RSSFeedParser;
import com.esperia09.rssnewsbook.utils.YamlUtilsForTest;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by esperia on 2016/06/05.
 */
@RunWith(JUnit4.class)
public class RSSParseTest {

    @Test
    public void updateCheck() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
//        String url = "http://localhost:3000/rssnewsbook.xml";
        YamlConfiguration config = YamlConfiguration.loadConfiguration(YamlUtilsForTest.getFile("plugin.yml"));
        String version = config.getString("version");

        // Parse version
        Method m = Updater.class.getDeclaredMethod("getNewestVersionIfReleased", String.class);
        m.setAccessible(true);
        String newVersion = (String) m.invoke(null, version);

        assertNull(newVersion);
    }

    @Test
    public void versionAlgoTest() {
        assertTrue(Updater.isNewVersion("0.1.0", "0.1.1"));
        assertTrue(Updater.isNewVersion("0.1.0", "0.1.11"));
        assertTrue(Updater.isNewVersion("0.1.0", "0.2.0"));
        assertTrue(Updater.isNewVersion("0.1.0", "1.0.0"));
        assertTrue(Updater.isNewVersion("0.1.0", "1.0.1"));
    }

//    @Test
//    public void convertDateISO8601() {
//        DateTime dateTime = new DateTime(1469071850000L);
//
//        DateTimeFormatter fmt = ISODateTimeFormat.dateTimeNoMillis();
//        String dateString = fmt.print(dateTime);
//        assertEquals("2016-07-21T12:30:50+09:00", dateString);
//
//        DateTime back = fmt.parseDateTime(dateString);
//        assertEquals(back.getMillis(), dateTime.getMillis());
//    }

}
