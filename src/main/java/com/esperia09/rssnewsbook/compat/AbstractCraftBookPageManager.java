package com.esperia09.rssnewsbook.compat;

import com.esperia09.rssnewsbook.Consts;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by esperia on 2016/06/20.
 */
public abstract class AbstractCraftBookPageManager implements ICraftBookPageManager {

    protected RssMeta parseText(String text) throws ParseException {
        if (text == null || "".equals(text)) {
            throw new ParseException("This is empty meta text.", 0);
        }

        String[] split = text.split("\n");
        if (split.length != 3) {
            throw new ParseException("Illegal Length.", 0);
        }

        // validates
        // FIXME
        String pluginName = split[0];
        String lastUpdate = split[1];
        String url = split[2];
        if (!Consts.PLUGIN_NAME.equals(pluginName)) {
            throw new ParseException("Illegal Plugin name.", 0);
        }
        DateTimeFormatter fmt = ISODateTimeFormat.dateTimeNoMillis();
        DateTime dateTime = fmt.parseDateTime(lastUpdate);
        if (!Consts.PLUGIN_NAME.equals(pluginName)) {
            throw new ParseException("Illegal Plugin name.", 0);
        }

        // Set some params
        RssMeta rssMeta = new RssMeta();
        try {
            rssMeta.url = new URL(text);
        } catch (MalformedURLException e) {
            throw new ParseException(e.getMessage(), 0);
        }

        return rssMeta;
    }
}
