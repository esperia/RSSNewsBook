package com.esperia09.rssnewsbook;

import com.esperia09.rssnewsbook.compat.ICraftBookPageManager;
import com.esperia09.rssnewsbook.compat.CompatManager;
import com.esperia09.rssnewsbook.compat.RssMeta;
import com.esperia09.rssnewsbook.data.config.MyConfig;
import com.esperia09.rssnewsbook.data.db.Connector;
import com.esperia09.rssnewsbook.rest.Api;
import com.esperia09.rssnewsbook.rss.Feed;
import com.esperia09.rssnewsbook.rss.FeedMessage;
import com.esperia09.rssnewsbook.utils.TextUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.text.ParseException;

public class RSSNewsBookPlugin extends JavaPlugin {
    private CompatManager cm;
    private MyConfig config = new MyConfig();
    private MyListener listener = new MyListener();

    @Override
    public void onEnable() {
        super.onEnable();
        try {
            CompatManager cm = new CompatManager(this);

            this.cm = cm;
        } catch (final Exception e) {
            this.getLogger().severe("Could not find support for this CraftBukkit version.");
            this.getLogger().info("Check for updates at URL HERE");
            e.printStackTrace();
            this.setEnabled(false);
            return;
        }

        config.init(this);

        // connect to database
        try {
            Connector.getInstance().connect(this);
        } catch (SQLException e) {
            this.getLogger().info("Connot connect to database: " + e.getMessage());
            this.getLogger().info("Using in-memory cache.");
        }

        getServer().getPluginManager().registerEvents(listener, this);
    }

    @Override
    public void onDisable() {
        super.onDisable();

        HandlerList.unregisterAll(listener);

        try {
            Connector.getInstance().disconnect(this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            final Player player = (Player) sender;

            if (command.getName().startsWith(Consts.Commands.rssnews.name())) {
                PlayerInventory inventory = player.getInventory();

                if (!player.hasPermission(Permissions.USE)) {
                    player.sendMessage(TextUtils.FC_RED + "You do not have permission to use this command!");
                    return true;
                }

                final ItemStack writtenBookInMainHand = inventory.getItemInMainHand();
                if (writtenBookInMainHand == null) {
                    // TODO テストしても通らない。このコード要らないかも
                    player.sendMessage("You have one's hand free.");
                    return true;
                }
                if (writtenBookInMainHand.getData().getItemType() != Material.WRITTEN_BOOK) {
                    player.sendMessage(String.format("You must hold %1$s.", Material.WRITTEN_BOOK.name()));
                    return true;
                }

                // Wrap in manager to book
                final BookMeta writtenBookMeta = (BookMeta) writtenBookInMainHand.getItemMeta();
                final ICraftBookPageManager cbpMgr;
                try {
                    cbpMgr = cm.getCompat().createCraftBookPageManager(writtenBookMeta);
                } catch (IllegalAccessException | NoSuchFieldException e) {
                    player.sendMessage(e.getMessage());
                    e.printStackTrace();
                    return true;
                }

                // Obtain url for fetch.
                final URL fetchUrl;
                // TODO: Require URL argument.
                if (args.length > 0) {
                    String argUrl = args[0];

                    // FIXME: The Test url
//                    fetchUrl = "http://rss.rssad.jp/rss/itm/2.0/netlab.xml";
                    try {
                        fetchUrl = new URL(argUrl);
                    } catch (MalformedURLException e) {
                        player.sendMessage("This is not a URL. Please check your argument.");
                        return true;
                    }
                } else {
                    final RssMeta rssMeta;
                    try {
                        rssMeta = cbpMgr.getRssMeta();
                    } catch (ParseException e) {
                        // Not found meta. (maybe not RSSNewsBook yet) Fetch new RSS data.
                        player.sendMessage("This is not a URL. Please check your argument.");
                        return true;
                    }
                    fetchUrl = rssMeta.url;
                }

                // TODO: Is this expired?

                // Request to Web
                player.sendMessage("Fetching...");
                Api.getInstance().reqReadRss(this, fetchUrl.toString(), new Api.ApiCallback<Feed>() {
                    @Override
                    public void onFinish(Feed data, Throwable tr) {
                        if (tr != null) {
                            player.sendMessage("Cannot got news. (" + tr.getMessage() + ")");
                            return;
                        }

                        // refresh the pages
                        cbpMgr.clear();

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
                            cbpMgr.add(title, description);
                        }

                        // save url
                        TextComponent urlTextCmp = new TextComponent(fetchUrl.toString());
                        cbpMgr.add(urlTextCmp);

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
        } else {
            System.out.println("The command must be run from player.");
        }

//        Server server = Bukkit.getServer();
//        Collection<? extends Player> onlinePlayers = server.getOnlinePlayers();
        return super.onCommand(sender, command, label, args);
    }
}
