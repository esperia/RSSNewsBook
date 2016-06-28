package com.esperia09.rssnewsbook.compat;

import com.esperia09.rssnewsbook.Consts;
import com.esperia09.rssnewsbook.utils.MyDate;
import net.md_5.bungee.api.chat.TextComponent;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by esperia on 2016/06/20.
 */
public class RssMeta {
    public String newsId;
    public Date lastUpdate;

    public TextComponent toComponent() {
        String result = Consts.PLUGIN_NAME;

        result += '\n' + newsId;

        if (lastUpdate != null) {
            SimpleDateFormat fmt = new SimpleDateFormat(MyDate.FMT_ISO8601);
            String dateString = fmt.format(lastUpdate);
            result += '\n' + dateString;
        }

        TextComponent cmp = new TextComponent(result);
        return cmp;
    }
}
