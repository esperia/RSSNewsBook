package com.esperia09.rssnewsbook.rest;

import com.esperia09.rssnewsbook.Consts;
import com.esperia09.rssnewsbook.rss.Feed;
import com.esperia09.rssnewsbook.rss.FeedMessage;
import com.esperia09.rssnewsbook.rss.RSSFeedParser;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by neske on 2016/06/27.
 */
public class Updater {

    /**
     * Get version if release newest plugin.
     *
     * @param currentVersion
     * @return
     */
    protected static String getNewestVersionIfReleased(String currentVersion) {
        String url = Consts.URL_UPDATE_CHECK;
        RSSFeedParser parser = new RSSFeedParser(url);
        Feed feed = parser.readFeed();

        // Parse version
        List<FeedMessage> messages = feed.getMessages();
        if (messages.size() <= 0) {
            throw new RuntimeException("messages is empty.");
        }
        FeedMessage feedMessage = messages.get(0);
        String title = feedMessage.getTitle();
        String rssVersion = Updater.getVersion(title);
        if (rssVersion == null) {
            throw new RuntimeException("Illegal Format of RSS for Update");
        }

        if (Updater.isNewVersion(currentVersion, rssVersion)) {
            return rssVersion;
        }
        return null;
    }

    /**
     * Get version string in text.
     *
     * @param includingVersionText  "vX.X.X" を含む文字列。Xは数値[0-9]。
     * @return "X.X.X" string. e.g.) 0.1.0
     */
    public static String getVersion(String includingVersionText) {
        Pattern p = Pattern.compile("v(\\d+.\\d+.\\d+)");
        Matcher m = p.matcher(includingVersionText);
        if (m.find()) {
            int i = m.groupCount();
            if (m.groupCount() >= 1) {
                String group = m.group(1);
                return group;
            }
        }
        return null;
    }

    /**
     * Compare two versions.
     *
     * @param currentVersion
     * @param newVersion
     * @return If {@param newVersion} is newest, return true. otherwise false.
     * @throws IllegalArgumentException difference version format.
     */
    public static boolean isNewVersion(String currentVersion, String newVersion) throws IllegalArgumentException {
        String[] currentVersions = currentVersion.split("\\.");
        String[] newVersions = newVersion.split("\\.");
        if (currentVersions.length != newVersions.length) {
            throw new IllegalArgumentException("Different version format.");
        }
        try {
            for (int i=0; i<currentVersions.length; i++) {
                int currentVerInt = Integer.parseInt(currentVersions[i]);
                int newVerInt = Integer.parseInt(newVersions[i]);
                if (newVerInt > currentVerInt) {
                    return true;
                }
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Different version format.", e);
        }

        return false;
    }
}
