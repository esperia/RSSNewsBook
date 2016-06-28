package com.esperia09.rssnewsbook;

import com.esperia09.rssnewsbook.compat.CompatManager;
import com.esperia09.rssnewsbook.compat.ICraftBookPageManager;
import com.esperia09.rssnewsbook.compat.RssMeta;
import com.esperia09.rssnewsbook.data.config.ConfigKeys;
import com.esperia09.rssnewsbook.data.config.YamlConfig;
import com.esperia09.rssnewsbook.data.config.YamlConfigNews;
import com.esperia09.rssnewsbook.data.storage.MyFileUtils;
import com.esperia09.rssnewsbook.rest.Api;
import com.esperia09.rssnewsbook.rss.Feed;
import com.esperia09.rssnewsbook.rss.FeedMessage;
import com.esperia09.rssnewsbook.rss.RSSFeedParser;
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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
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
            this.cm = new CompatManager(this);
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

        // Check update
        if (!ymlConfig.getConfig().isSet(ConfigKeys.UPDATER)) {
            ymlConfig.getConfig().set(ConfigKeys.UPDATER, true);
            try {
                ymlConfig.save();
            } catch (IOException e) {
                this.getLogger().severe(String.format(Locale.US, "Cannot save config.yml (%1$s)", e.getMessage()));
                e.printStackTrace();
            }
        }
        if (ymlConfig.getConfig().getBoolean(ConfigKeys.UPDATER)) {
            final String currentPluginVersion = getDescription().getVersion();
            Api.getInstance().reqCanUpdateVersion(this, currentPluginVersion, new Api.ApiCallback<String>() {
                @Override
                public void onFinish(String newVersion, Throwable tr) {
                    if (tr != null) {
                        getLogger().info(String.format(Locale.US,
                                "Failed check update: (%1$s) ", tr.getMessage()));
                    } else {
                        if (newVersion != null) {
                            getLogger().info(String.format(Locale.US,
                                    "New version available!! > v%1$s < (currently:v%2$s)", newVersion, currentPluginVersion));
                        } else {
                            getLogger().info("No new version available.");
                        }
                    }
                }
            });
        }

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

            if (command.getName().startsWith(Commands.RSSNEWS)) {
                if (args.length == 0) {
                    return false;
                }
                String subCommand = args[0];
                String[] poppedArgs = Arrays.copyOfRange(args, 1, args.length);
                if (Commands.RSSNEWS_CONVERT.equals(subCommand)) {
                    convert(player, poppedArgs);
                } else if (Commands.RSSNEWS_UPDATE.equals(subCommand)) {
                    update(player, poppedArgs);
                } else if (Commands.RSSNEWS_LIST.equals(subCommand)) {
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
                } else if (Commands.RSSNEWS_ADD.equals(subCommand)) {
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

        String newsId = args[0];

        // Check name existing.
        final List<YamlConfigNews> newsList = YamlConfigNews.fromMapList(ymlNews.getConfig().getMapList("news"));
        YamlConfigNews existNews = null;
        for (YamlConfigNews news : newsList) {
            if (newsId.equals(news.getNewsId())) {
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
        Api.getInstance().reqReadRss(this, news.getNewsId(), fetchUrl.toString(), new Api.ApiCallback<Feed>() {
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
        if (news == null) {
            final RssMeta rssMeta;
            try {
                rssMeta = cbpMgr.getRssMeta();
            } catch (ParseException e) {
                // Not found meta. (maybe not RSSNewsBook yet) Fetch new RSS data.
                player.sendMessage(ChatColor.RED + String.format(Locale.US, "This is not a URL. Please check your argument. (%1$s)", e.getMessage()));
                return true;
            }

            final List<YamlConfigNews> newsList = YamlConfigNews.fromMapList(ymlNews.getConfig().getMapList("news"));

            // Migration for (0.1.0-0.1.1) -> (0.2.0)
            try {
                new URL(rssMeta.newsId);
                String url = rssMeta.newsId;
                // Old format. migrate to newsId
                // TODO: convert url to newsId
                for (YamlConfigNews news_ : newsList) {
                    if (news_.getUrl().equals(url)) {
                        news = news_;
                        break;
                    }
                }
            } catch (MalformedURLException e) {
                // Maybe correctly!

                // newsId to URL.
                for (YamlConfigNews news_ : newsList) {
                    if (news_.getNewsId().equals(rssMeta.newsId)) {
                        news = news_;
                        break;
                    }
                }
            }

            if (news == null) {
                player.sendMessage(String.format(Locale.US, "Unknown newsId (%1$s). Try to convert again.", rssMeta.newsId));
                return true;
            }
        }

        // キャッシュを確認して、
        Feed feed = null;
        long expireTime = news.getLastUpdate().getTime() + (getConfig().getInt(ConfigKeys.NEWS_EXPIRE) * 1000L);
        long currentTime = System.currentTimeMillis();
        if (expireTime >= currentTime) {
            // Not expired. Use XML Cache.
            // use saved xml cache.
            File newsCacheFile;
            try {
                newsCacheFile = MyFileUtils.getNewsCacheFile(this, news.getNewsId());
            } catch (IOException e) {
                player.sendMessage(ChatColor.RED + "Cannot run your command. Please ask to Server Administrator.");
                getLogger().severe(e.getMessage());
                return true;
            }
            if (newsCacheFile.exists()) {
                RSSFeedParser parser = new RSSFeedParser();
                try {
                    feed = parser.readFeed(newsCacheFile);
                } catch (IOException e) {
                    // ローカルなので、読み込み失敗 == キャッシュが無いとみなす。
                    getLogger().info(String.format(Locale.US, "Illegal RSS Format (%1$s). Trying fetch from Web...", e.getMessage()));
                    e.printStackTrace();
                }
            }
        } else {
            // Expired.
//            SimpleDateFormat fmt = new SimpleDateFormat(MyDate.FMT_ISO8601);
//            player.sendMessage(ChatColor.AQUA + String.format(Locale.US,
//                    "This book already newest. Next update: %1$s", fmt.format(new Date(expireTime))));
//            return true;
        }

        final YamlConfigNews fixedNews = news;
        if (feed != null) {
            writeToBookMeta(cbpMgr, writtenBookMeta, feed, fixedNews);

            //update the ItemStack with this new meta
            writtenBookInMainHand.setItemMeta(writtenBookMeta);
        } else {
            // Request to Web
            player.sendMessage("Fetching...");
            Api.getInstance().reqReadRss(this, fixedNews.getNewsId(), fixedNews.getUrl(), new Api.ApiCallback<Feed>() {
                @Override
                public void onFinish(Feed data, Throwable tr) {
                    if (tr != null) {
                        player.sendMessage(ChatColor.RED + String.format(Locale.US, "Cannot got news. (%1$s)", tr.getMessage()));
                        return;
                    }

                    writeToBookMeta(cbpMgr, writtenBookMeta, data, fixedNews);

                    //update the ItemStack with this new meta
                    writtenBookInMainHand.setItemMeta(writtenBookMeta);
                    fixedNews.getLastUpdate().setTime(System.currentTimeMillis());

                    // save news.yml
                    final List<YamlConfigNews> newsList = YamlConfigNews.fromMapList(ymlNews.getConfig().getMapList("news"));
                    for (YamlConfigNews news : newsList) {
                        if (fixedNews.getNewsId().equals(news.getNewsId())) {
                            news.getLastUpdate().setTime(System.currentTimeMillis());
                            break;
                        }
                    }
                    ymlNews.getConfig().set("news", YamlConfigNews.toMapList(newsList));
                    try {
                        ymlNews.save();
                    } catch (IOException e) {
                        player.sendMessage(ChatColor.RED + String.format(Locale.US, "Cannot save news. (%1$s)", e.getMessage()));
                    }
                }
            });
        }

        return true;
    }

    private void writeToBookMeta(ICraftBookPageManager cbpMgr, BookMeta writtenBookMeta, Feed data, YamlConfigNews fixedNews) {
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
        rssMeta.newsId = fixedNews.getNewsId();
        rssMeta.lastUpdate = new Date(System.currentTimeMillis());
        cbpMgr.add(rssMeta.toComponent());

        //set the title and author of this book
        if (data.getTitle() != null) {
            writtenBookMeta.setTitle(data.getTitle());
        } else {
            writtenBookMeta.setTitle("[No Title]");
        }
        writtenBookMeta.setAuthor("" + data.getCopyright());
    }
}
