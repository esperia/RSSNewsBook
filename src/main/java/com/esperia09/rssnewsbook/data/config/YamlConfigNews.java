package com.esperia09.rssnewsbook.data.config;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Created by esperia on 2016/06/24.
 */
public class YamlConfigNews {
    private static final String KEY_URL = "url";

    /** e.g.) netlab */
    private final String newsId;
    /** e.g.) "http://rss.rssad.jp/rss/itm/2.0/netlab.xml" */
    private String url;

    public YamlConfigNews(String newsId, String url) {
        this.newsId = newsId;
        this.url = url;
    }

    private YamlConfigNews(String newsId, Map newsMap) {
        this.newsId = newsId;

        String url = null;
        for (Object keyObj : newsMap.keySet()) {
            if (!(keyObj instanceof String)) {
                throw new IllegalArgumentException("Not a newsData. Check your config format.");
            }
            String key = (String) keyObj;
            if (KEY_URL.equals(key)) {
                Object valueObj = newsMap.get(key);
                if (!(valueObj instanceof String)) {
                    throw new IllegalArgumentException("Not a URL. Check your config format.");
                }

                final URL urlForParse;
                try {
                    urlForParse = new URL((String) valueObj);
                } catch (MalformedURLException e) {
                    throw new IllegalArgumentException("Not a URL. Check your config format.");
                }
                url = urlForParse.toString();
            } else {
                System.out.println(String.format(Locale.US, "Unknown key (%1$s)", key));
            }
        }
        this.url = url;
    }

    public String getNewsId() {
        return newsId;
    }

    public String getUrl() {
        return url;
    }


    private Map<String, String> toMap() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put(KEY_URL, this.url);
        return map;
    }

    /**
     * Convert to human-readable list.
     *
     * @param list
     * @return
     */
    public static List<Map<String, Map<String, String>>> toMapList(List<YamlConfigNews> list) {
        Map<String, Map<String, String>> newsMaps = new LinkedHashMap<>();

        for (YamlConfigNews news : list) {
            Map<String, String> newsMap = news.toMap();

            newsMaps.put(news.newsId, newsMap);
        }

        List<Map<String, Map<String, String>>> mapList = new ArrayList<>();
        mapList.add(newsMaps);

        return mapList;
    }

    /**
     * Convert to List for saving.
     * @param mapList
     * @return
     */
    public static List<YamlConfigNews> fromMapList(List<Map<?, ?>> mapList) {
        List<YamlConfigNews> list = new ArrayList<>();
        for (Map<?, ?> map : mapList) {
            Set<?> newsIds = map.keySet();
            for (Object newsIdObj : newsIds) {
                if (!(newsIdObj instanceof String)) {
                    throw new IllegalArgumentException("Not a newsId. Check your config format.");
                }
                String newsId = (String) newsIdObj;
                Object newsMapObj = map.get(newsId);
                if (!(newsMapObj instanceof Map)) {
                    throw new IllegalArgumentException("Not a newsMap. Check your config format.");
                }
                Map newsMap = (Map) newsMapObj;

                list.add(new YamlConfigNews(newsId, newsMap));
            }
        }

        return list;
    }

    public void setUrl(String url) {
        if (url == null) {
            throw new NullPointerException("MUST be not null.");
        }
        this.url = url;
    }
}
