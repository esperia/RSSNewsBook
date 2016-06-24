package com.esperia09.rssnewsbook;

import com.esperia09.rssnewsbook.compat.ICraftBookPageManager;
import com.esperia09.rssnewsbook.compat.CompatManager;
import com.esperia09.rssnewsbook.compat.RssMeta;
import com.esperia09.rssnewsbook.data.config.ConfigKeys;
import com.esperia09.rssnewsbook.data.config.YamlConfig;
import com.esperia09.rssnewsbook.data.config.YamlConfigNews;
import com.esperia09.rssnewsbook.data.db.Connector;
import com.esperia09.rssnewsbook.rest.Api;
import com.esperia09.rssnewsbook.rss.Feed;
import com.esperia09.rssnewsbook.rss.FeedMessage;
import com.esperia09.rssnewsbook.utils.TextUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;

public class RSSNewsBookPlugin extends JavaPlugin {
    private CompatManager cm;
//    private MyListener listener = new MyListener();
    private YamlConfig ymlConfig;
    private YamlConfig ymlNews;

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

        // Load configs
        ymlConfig = new YamlConfig(this, "config.yml");
        ymlNews = new YamlConfig(this, "news.yml");

//        // connect to database
//        try {
//            Connector.getInstance().connect(this);
//        } catch (SQLException e) {
//            this.getLogger().info("Connot connect to database: " + e.getMessage());
//            this.getLogger().info("Using in-memory cache.");
//        }

//        getServer().getPluginManager().registerEvents(listener, this);
    }

    @Override
    public void onDisable() {
        super.onDisable();

//        HandlerList.unregisterAll(listener);

//        try {
//            Connector.getInstance().disconnect(this);
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            final Player player = (Player) sender;

            if (command.getName().startsWith(Consts.Commands.rssnews.name())) {
                if (args.length == 0) {
                    return false;
                }
                String subCommand = args[0];
                String[] poppedArgs = Arrays.copyOfRange(args, 1, args.length);
                if ("convert".equals(subCommand)) {
                    convert(player, poppedArgs);
                } else if ("update".equals(subCommand)) {
                    update(player, poppedArgs);
                } else if ("list".equals(subCommand)) {
                    if (!player.hasPermission(Permissions.LIST)) {
                        player.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                        return true;
                    }

                    final List<YamlConfigNews> newsList = YamlConfigNews.fromMapList(ymlNews.getConfig().getMapList("news"));

                    ArrayList<String> messages = new ArrayList<>();
                    messages.add("Follow the newsIds:");
                    for (YamlConfigNews news : newsList) {
                        messages.add(String.format(Locale.US, "%1$s: %2$s",
                                ChatColor.AQUA + news.getNewsId(),
                                ChatColor.RESET + news.getUrl()));
                    }
                    player.sendMessage(messages.toArray(new String[messages.size()]));
                } else if ("add".equals(subCommand)) {
                    add(player, poppedArgs);
                } else {
                    return false;
                }

                return true;
            }
        } else {
            System.out.println("The command must be run from player.");
        }

//        Server server = Bukkit.getServer();
//        Collection<? extends Player> onlinePlayers = server.getOnlinePlayers();
        return super.onCommand(sender, command, label, args);
    }

    private boolean convert(Player player, String[] args) {
        if (!player.hasPermission(Permissions.CONVERT)) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
            return true;
        }
        if (args.length < 1) {
            // TODO Help
            player.sendMessage(ChatColor.RED + "Require [name]");
            return true;
        }

        String name = args[0];

        // Check name existing.
        final List<YamlConfigNews> newsList = YamlConfigNews.fromMapList(ymlNews.getConfig().getMapList("news"));
        YamlConfigNews existNews = null;
        for (YamlConfigNews news : newsList) {
            if (name.equals(news.getNewsId())) {
                existNews = news;
                break;
            }
        }
        if (existNews == null) {
            player.sendMessage(ChatColor.RED + "The newsId is not registered.");
            return false;
        }

        // Convert

        return update(player, existNews);
    }

    private URL addingUrl = null;

    /**
     * Add URL to config.yml.
     *
     * @param player
     * @param args
     * @return
     */
    private boolean add(final Player player, String[] args) {
        if (!player.hasPermission(Permissions.ADMIN_NEWS_ADD)) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
            return true;
        }
        if (addingUrl != null) {
            player.sendMessage(ChatColor.RED + "still fetching. Please wait a moment...");
            return true;
        }
        if (args.length < 2) {
            // TODO: More Helps
            player.sendMessage(ChatColor.RED + "Require [newsId] [url]");
            return true;
        }

        final String name = args[0];
        final String url = args[1];

        final URL fetchUrl;
        try {
            fetchUrl = new URL(url);
        } catch (MalformedURLException e) {
            player.sendMessage(ChatColor.RED + "This is not a URL. Please check your argument.");
            return true;
        }
        addingUrl = fetchUrl;

        // check the name already exists
        YamlConfigNews existNews = null;
        final List<YamlConfigNews> newsList = YamlConfigNews.fromMapList(ymlNews.getConfig().getMapList("news"));
        for (YamlConfigNews news : newsList) {
            if (name.equals(news.getNewsId())) {
                existNews = news;
                break;
            }
        }

        final YamlConfigNews news;
        if (existNews == null) {
            news = new YamlConfigNews(name, url);
            newsList.add(news);
        } else {
            news = existNews;
            news.setUrl(url);
        }

        // Try fetch
        Api.getInstance().reqReadRss(this, fetchUrl.toString(), new Api.ApiCallback<Feed>() {
            @Override
            public void onFinish(Feed data, Throwable tr) {
                if (tr != null) {
                    player.sendMessage(ChatColor.RED + String.format(Locale.US, "Cannot got news. (%1$s)", tr.getMessage()));
                    addingUrl = null;
                    return;
                }
                // -> succeeded: register params to config.yml
                ymlNews.getConfig().set("news", YamlConfigNews.toMapList(newsList));
                try {
                    ymlNews.save();
                } catch (IOException e) {
                    player.sendMessage(ChatColor.RED + String.format(Locale.US, "Cannot save news. (%1$s)", e.getMessage()));
                }
                addingUrl = null;
            }
        });

        return true;
    }

    /**
     * @param player
     * @param args
     * @return
     */
    private boolean update(final Player player, String[] args) {
        if (!player.hasPermission(Permissions.UPDATE)) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
            return true;
        }

        return update(player, (YamlConfigNews) null);
    }

    private boolean update(final Player player, YamlConfigNews news) {
        PlayerInventory inventory = player.getInventory();

        final ItemStack writtenBookInMainHand = inventory.getItemInMainHand();
        if (writtenBookInMainHand == null) {
            // TODO テストしても通らない。このコード要らないかも
            player.sendMessage(ChatColor.RED + "You have one's hand free.");
            return true;
        }
        if (writtenBookInMainHand.getData().getItemType() != Material.WRITTEN_BOOK) {
            player.sendMessage(ChatColor.RED + String.format("You must hold %1$s.", Material.WRITTEN_BOOK.name()));
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
        final URL url;
        if (news != null) {
            try {
                url = new URL(news.getUrl());
            } catch (MalformedURLException e) {
                player.sendMessage(ChatColor.RED + String.format(Locale.US, "This is not a URL. Please check your argument. (%1$s)", e.getMessage()));
                return true;
            }
        } else {
            final RssMeta rssMeta;
            try {
                rssMeta = cbpMgr.getRssMeta();
            } catch (ParseException e) {
                // Not found meta. (maybe not RSSNewsBook yet) Fetch new RSS data.
                player.sendMessage(ChatColor.RED + String.format(Locale.US, "This is not a URL. Please check your argument. (%1$s)", e.getMessage()));
                return true;
            }

            // Is this expired?
            long expireTime = rssMeta.lastUpdate.getTime() + (getConfig().getInt(ConfigKeys.NEWS_EXPIRE) * 1000L);
            if (expireTime < System.currentTimeMillis()) {
                Date dateTime = new Date(expireTime);
                player.sendMessage(ChatColor.RED + String.format(Locale.US, "Cannot update until %1$s", dateTime.toString()));
                return true;
            }

            url = rssMeta.url;
        }

        // Request to Web
        player.sendMessage("Fetching...");
        Api.getInstance().reqReadRss(this, url.toString(), new Api.ApiCallback<Feed>() {
            @Override
            public void onFinish(Feed data, Throwable tr) {
                if (tr != null) {
                    player.sendMessage(ChatColor.RED + String.format(Locale.US, "Cannot got news. (%1$s)", tr.getMessage()));
                    return;
                }

                // refresh the pages
                cbpMgr.clear();

                for (FeedMessage msg : data.getMessages()) {
                    // make a title
                    final String titleStr = (msg.getTitle() != null) ? msg.getTitle() : "[No Title]";
                    final TextComponent title;
                    if (!TextUtils.isEmpty(msg.getLink())) {
                        title = new TextComponent(ChatColor.BOLD + "" + ChatColor.UNDERLINE + titleStr);
                        title.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, msg.getLink()));
                        title.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Open news link").create()));
                    } else {
                        title = new TextComponent(ChatColor.BOLD + titleStr);
                    }

                    // make a description
                    TextComponent description = new TextComponent('\n' + msg.getDescription());

                    //add the page to the list of pages
                    cbpMgr.add(title, description);
                }

                // save url
                RssMeta rssMeta = new RssMeta();
                rssMeta.url = url;
                rssMeta.lastUpdate = new Date(System.currentTimeMillis());
                cbpMgr.add(rssMeta.toComponent());

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
