package com.esperia09.rssnewsbook.compat.v1_9_R1;

import com.esperia09.rssnewsbook.compat.ICraftBookPageManager;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.server.v1_9_R1.IChatBaseComponent;
import org.bukkit.craftbukkit.v1_9_R1.inventory.CraftMetaBook;
import org.bukkit.inventory.meta.BookMeta;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by esperia on 2016/06/19.
 */
public class CraftBookPageManager implements ICraftBookPageManager {
    private final List<IChatBaseComponent> pages;

    public CraftBookPageManager(BookMeta bookMeta) throws NoSuchFieldException, IllegalAccessException {
        Field pagesField = CraftMetaBook.class.getDeclaredField("pages");
        pages = (List<IChatBaseComponent>) pagesField.get(bookMeta);
    }

    @Override
    public void clear() {
        pages.clear();
    }

    @Override
    public void add(BaseComponent... components) {
        IChatBaseComponent page = IChatBaseComponent.ChatSerializer.a(ComponentSerializer.toString(components));
        pages.add(page);
    }
}
