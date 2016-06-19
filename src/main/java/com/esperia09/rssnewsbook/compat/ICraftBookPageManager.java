package com.esperia09.rssnewsbook.compat;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * Created by esperia on 2016/06/18.
 */
public interface ICraftBookPageManager {
    void clear();

    void add(BaseComponent... components);
}
