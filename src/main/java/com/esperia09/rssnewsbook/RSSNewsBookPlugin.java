package com.esperia09.rssnewsbook;

import com.esperia09.rssnewsbook.rest.Api;
import com.esperia09.rssnewsbook.rss.Feed;
import com.esperia09.rssnewsbook.rss.FeedMessage;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.server.v1_9_R1.IChatBaseComponent;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_9_R1.inventory.CraftMetaBook;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.List;

public class RSSNewsBookPlugin extends JavaPlugin {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            final Player player = (Player) sender;

            if (command.getName().startsWith(Consts.Commands.torssnews.name())) {
                PlayerInventory inventory = player.getInventory();

                final ItemStack writtenBookInMainHand = inventory.getItemInMainHand();
                if (writtenBookInMainHand == null) {
                    // TODO テストしても通らない。このコード要らないかも
                    player.sendMessage("手に何も持っていません！");
                    return false;
                }
                if (writtenBookInMainHand.getData().getItemType() != Material.WRITTEN_BOOK) {
                    player.sendMessage(Material.WRITTEN_BOOK.name() + "を手に持つ必要があります。");
                    return false;
                }
                //create the book
                final BookMeta writtenBookMeta = (BookMeta) writtenBookInMainHand.getItemMeta();

                Api.getInstance().reqReadRss(this, new Api.ApiCallback<Feed>() {
                    @Override
                    public void onFinish(Feed data, Throwable tr) {
                        if (data == null) {
                            if (tr == null) {
                                throw new IllegalStateException("This is Plugin's bug!!");
                            }
                            player.sendMessage("Cannot got news. (" + tr.getMessage() + ")");
                            return;
                        }
                        List<IChatBaseComponent> pages;

                        //get the pages
                        try {
                            Field pagesField = CraftMetaBook.class.getDeclaredField("pages");
                            pages = (List<IChatBaseComponent>) pagesField.get(writtenBookMeta);
                        } catch (NoSuchFieldException e) {
                            player.sendMessage(e.getMessage());
                            e.printStackTrace();
                            return;
                        } catch (IllegalAccessException e) {
                            player.sendMessage(e.getMessage());
                            e.printStackTrace();
                            return;
                        }

                        pages.clear();
                        for (FeedMessage msg : data.getMessages()) {
                            // make a title
                            final String titleStr = (msg.getTitle() != null) ? msg.getTitle() : "[No Title]";
                            TextComponent title = new TextComponent(titleStr);
                            if (msg.getLink() != null) {
                                title.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, msg.getLink()));
                            }
                            title.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Open news link").create()));

                            // make a description
                            TextComponent description = new TextComponent('\n' + msg.getDescription());

                            //add the page to the list of pages
                            IChatBaseComponent page = IChatBaseComponent.ChatSerializer.a(ComponentSerializer.toString(title, description));
                            pages.add(page);
                        }

                        //set the title and author of this book
                        if (data.getTitle() != null) {
                            writtenBookMeta.setTitle(data.getTitle());
                        } else {
                            writtenBookMeta.setTitle("[No Title]");
                        }
                        writtenBookMeta.setAuthor("" + data.getCopyright());

                        //update the ItemStack with this new meta
                        writtenBookInMainHand.setItemMeta(writtenBookMeta);
                    }
                });

                return true;
            }
        }

//        Server server = Bukkit.getServer();
//        Collection<? extends Player> onlinePlayers = server.getOnlinePlayers();
        return super.onCommand(sender, command, label, args);
    }
}
