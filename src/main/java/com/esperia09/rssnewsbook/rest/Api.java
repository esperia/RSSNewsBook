package com.esperia09.rssnewsbook.rest;

import com.esperia09.rssnewsbook.data.storage.MyFileUtils;
import com.esperia09.rssnewsbook.rss.Feed;
import com.esperia09.rssnewsbook.rss.RSSFeedParser;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

/**
 * Created by esperia on 2016/06/05.
 */
public class Api {
    private static Api sInstance = null;

    public static final Api getInstance() {
        if (sInstance == null) {
            sInstance = new Api();
        }
        return sInstance;
    }

    private Api() {
    }

    // ------------------------------------------------------------------------

    public void reqReadRss(final Plugin plugin, final String newsId, final String url, final ApiCallback<Feed> callback) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                Exception tmpEx = null;
                Feed tmpResult = null;
                try {
                    RSSFeedParser parser = new RSSFeedParser();
                    File newsCacheFile;
                    synchronized (MyFileUtils.class) {
                        newsCacheFile = MyFileUtils.getNewsCacheFile(plugin, newsId);
                        if (newsCacheFile.exists()) {
                            if (!newsCacheFile.delete()) {
                                throw new IOException(String.format(Locale.US, "Cannot delete cache. %1$s", newsCacheFile.getAbsolutePath()));
                            }
                        }
                        parser.download(url, newsCacheFile);
                    }
                    tmpResult = parser.readFeed(newsCacheFile);
                } catch (IOException e) {
                    tmpEx = e;
                }
                final Exception tr = tmpEx;
                final Feed result = tmpResult;

                plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        callback.onFinish(result, tr);
                    }
                });
            }
        });
    }

    public void reqCanUpdateVersion(final Plugin plugin, final String currentVersion, final ApiCallback<String> callback) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                Exception tmpEx = null;
                String tmpResult = null;
                try {
                    tmpResult = Updater.getNewestVersionIfReleased(currentVersion);
                } catch (Exception e) {
                    tmpEx = e;
                }
                final Exception tr = tmpEx;
                final String result = tmpResult;

                plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        callback.onFinish(result, tr);
                    }
                });
            }
        });
    }

//    public void reqNtp(final Plugin plugin, final ApiCallback<String> callback) {
//        final String url = "https://ntp-a1.nict.go.jp/cgi-bin/json";
//
//        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
//            @Override
//            public void run() {
//                Exception tmpEx = null;
//                String tmpResult = null;
//                try {
//                    tmpResult = doPostSync(url);
//                } catch (IOException e) {
//                    tmpEx = e;
//                }
//                final Exception tr = tmpEx;
//                final String result = tmpResult;
//
//                plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
//                    @Override
//                    public void run() {
//                        callback.onFinish(result, tr);
//                    }
//                });
//            }
//        });
//    }

    // ------------------------------------------------------------------------

//    private static String doPostSync(String url) throws IOException {
//        String requestJSON = "JSON文字列";
//
//        HttpURLConnection conn = null;
//        try {
//            conn = (HttpURLConnection) new URL(url).openConnection();
//            conn.setRequestMethod("POST");
//            conn.setDoInput(true);
//            conn.setDoOutput(true);
//            conn.setFixedLengthStreamingMode(requestJSON.getBytes().length);
//            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
//
//            conn.connect();
//
//            DataOutputStream os = new DataOutputStream(conn.getOutputStream());
//            os.write(requestJSON.getBytes("UTF-8"));
//            os.flush();
//            os.close();
//
//            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
//                StringBuffer responseJSON = new StringBuffer();
//                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//                String inputLine;
//                while ((inputLine = reader.readLine()) != null) {
//                    responseJSON.append(inputLine);
//                }
//                return responseJSON.toString();
//            }
//        } finally {
//            if (conn != null) {
//                conn.disconnect();
//            }
//        }
//        return null;
//    }

    // ------------------------------------------------------------------------

    public interface ApiCallback<T> {
        void onFinish(T result, Throwable tr);
    }

}
