package com.esperia09.rssnewsbook.compat.v1_10_R1;

import com.esperia09.rssnewsbook.compat.ICompat;
import com.esperia09.rssnewsbook.compat.ICraftBookPageManager;
import org.bukkit.inventory.meta.BookMeta;

public class Compat implements ICompat {
    @Override
    public ICraftBookPageManager createCraftBookPageManager(BookMeta bookMeta) throws NoSuchFieldException, IllegalAccessException {
        return new CraftBookPageManager(bookMeta);
    }
}
