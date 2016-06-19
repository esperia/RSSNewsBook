package com.esperia09.rssnewsbook.compat;

import org.bukkit.inventory.meta.BookMeta;

/**
 * Created by esperia on 2016/06/18.
 */
public interface ICompat {
    ICraftBookPageManager createCraftBookPageManager(BookMeta bookMeta) throws NoSuchFieldException, IllegalAccessException;
}
