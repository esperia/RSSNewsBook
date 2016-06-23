package com.esperia09.rssnewsbook.compat;

import com.esperia09.rssnewsbook.Consts;
import net.md_5.bungee.api.chat.TextComponent;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by esperia on 2016/06/20.
 */
public class RssMeta {
    public URL url;
    public Date lastUpdate;

    public TextComponent toComponent() {
        String result = Consts.PLUGIN_NAME;

        result += '\n' + url.toString();

        if (lastUpdate != null) {
            SimpleDateFormat fmt = new SimpleDateFormat(Consts.FMT_ISO8601);
            String dateString = fmt.format(lastUpdate);
            result += '\n' + dateString;
        }

        TextComponent cmp = new TextComponent(result);
        return cmp;
    }
}
