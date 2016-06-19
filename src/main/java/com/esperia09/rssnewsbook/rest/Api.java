package com.esperia09.rssnewsbook.rest;

import com.esperia09.rssnewsbook.rss.Feed;
import com.esperia09.rssnewsbook.rss.RSSFeedParser;
import org.bukkit.plugin.Plugin;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

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

    public void reqReadRss(final Plugin plugin, final ApiCallback<Feed> callback) {
        final String url = "http://rss.rssad.jp/rss/itm/2.0/netlab.xml";

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                Exception tmpEx = null;
                Feed tmpResult = null;
                try {
                    tmpResult = reqReadRssSync(url);
                } catch (IOException e) {
                    tmpEx = e;
                }
                final Exception tr = tmpEx;
                final Feed result = tmpResult;

                plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        if (result == null) {
                            if (tr == null) {
                                throw new IllegalStateException("This is Plugin's bug!!");
                            }
                            return;
                        }
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

    private Feed reqReadRssSync(String url) throws IOException {
        RSSFeedParser parser = new RSSFeedParser(url);
        Feed feed = parser.readFeed();
        return feed;
    }

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
