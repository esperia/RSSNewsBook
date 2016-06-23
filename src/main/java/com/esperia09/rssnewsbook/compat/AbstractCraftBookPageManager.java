package com.esperia09.rssnewsbook.compat;

import com.esperia09.rssnewsbook.Consts;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;

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
        int textCount = 0;
        String pluginName = split[0];
        String url = split[1];
        String lastUpdate = split[2];
        RssMeta rssMeta = new RssMeta();

        // Check format
        if (!Consts.PLUGIN_NAME.equals(pluginName)) {
            throw new ParseException("Illegal Plugin name.", 0);
        }
        textCount += pluginName.length() + 1;

        // Time
        SimpleDateFormat fmt = new SimpleDateFormat(Consts.FMT_ISO8601);
        try {
            rssMeta.lastUpdate = fmt.parse(lastUpdate);
        } catch (UnsupportedOperationException e) {
            throw new ParseException("Unsupported format. " + e.getMessage(), textCount);
        } catch (IllegalArgumentException e) {
            throw new ParseException("Illegal format. " + e.getMessage(), textCount);
        }
        textCount += lastUpdate.length() + 1;

        // URL
        try {
            rssMeta.url = new URL(url);
        } catch (MalformedURLException e) {
            throw new ParseException(e.getMessage(), textCount);
        }

        return rssMeta;
    }
}
