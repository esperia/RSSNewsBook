package com.esperia09.rssnewsbook.compat;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

import java.net.MalformedURLException;
import java.text.ParseException;

/**
 * Created by esperia on 2016/06/18.
 */
public interface ICraftBookPageManager {
    void clear();
    int size();
    RssMeta getRssMeta() throws ParseException;

    void add(BaseComponent... components);
}
