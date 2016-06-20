package com.esperia09.rssnewsbook.compat.v1_10_R1;

import com.esperia09.rssnewsbook.Consts;
import com.esperia09.rssnewsbook.compat.AbstractCraftBookPageManager;
import com.esperia09.rssnewsbook.compat.ICraftBookPageManager;
import com.esperia09.rssnewsbook.compat.RssMeta;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.server.v1_10_R1.IChatBaseComponent;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftMetaBook;
import org.bukkit.inventory.meta.BookMeta;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.List;

/**
 * Created by esperia on 2016/06/19.
 */
public class CraftBookPageManager extends AbstractCraftBookPageManager {
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
    public int size() {
        return pages.size();
    }

    @Override
    public RssMeta getRssMeta() throws ParseException {
        int size = pages.size();
        if (size == 0) {
            throw new ParseException("The book has not page.", -1);
        }
        // Get the last page meta data.
        IChatBaseComponent cmp = pages.get(size - 1);
        String text = cmp.toPlainText();

        return parseText(text);
    }

    @Override
    public void add(BaseComponent... components) {
        IChatBaseComponent page = IChatBaseComponent.ChatSerializer.a(ComponentSerializer.toString(components));
        pages.add(page);
    }
}
