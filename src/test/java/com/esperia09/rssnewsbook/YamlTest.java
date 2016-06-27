package com.esperia09.rssnewsbook;

import static org.junit.Assert.*;
import com.esperia09.rssnewsbook.data.config.YamlConfig;
import com.esperia09.rssnewsbook.data.config.YamlConfigNews;
import com.esperia09.rssnewsbook.utils.YamlUtilsForTest;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by esperia on 2016/06/24.
 */
@RunWith(JUnit4.class)
public class YamlTest {

    private YamlConfig yamlConfig;

    @Before
    public void setup() throws IOException {
        String path = System.getProperty("user.dir");
        yamlConfig = new YamlConfig(YamlUtilsForTest.getFile("plugin.yml"));
        System.out.println("testFile=" + yamlConfig.getFile());

        yamlConfig.save();
    }

    @After
    public void teardown() {
        yamlConfig.getFile().delete();
    }

    @Test
    public void setAnyParams() throws IOException {
        YamlConfiguration testConfig = yamlConfig.getConfig();

        testConfig.set("stringParam", "mystring");
        testConfig.set("integerParam", Integer.MAX_VALUE);
        testConfig.set("integerMinParam", Integer.MIN_VALUE);
        testConfig.set("longParam", Long.MAX_VALUE);
        testConfig.set("longMinParam", Long.MIN_VALUE);
        testConfig.set("stringParam", "mystring");

        String[] strings = {
                "a", "b"
        };
        testConfig.set("listParam", strings);


        Map<String, String> map1 = new HashMap<>();
        map1.put("map1A", "value1A");
        map1.put("map1B", "value1B");

        Map<String, String> map2 = new HashMap<>();
        map2.put("map2A", "value2A");
        map2.put("map2B", "value2B");

        List<Map<String, String>> mapList = new ArrayList<>();
        mapList.add(map1);
        mapList.add(map2);
        testConfig.set("mapList", mapList);

        yamlConfig.save();

        System.out.println("Check your yml");
    }

    @Test
    public void getList() throws IOException {
        YamlConfiguration testConfig = yamlConfig.getConfig();

        Map<String, String> netlabMap = new HashMap<>();
        netlabMap.put("url", "http://www.google.co.jp");

        Map<String, String> mynewsMap = new HashMap<>();
        mynewsMap.put("url", "http://www.yahoo.co.jp");

        Map<String, Map<String, String>> map1 = new HashMap<>();
        map1.put("netlab", netlabMap);
        map1.put("mynews", mynewsMap);

        List<Map<String, Map<String, String>>> mapList = new ArrayList<>();
        mapList.add(map1);
        testConfig.set("news", mapList);

        yamlConfig.save();

        List<Map<?, ?>> loadMapList = testConfig.getMapList("news");

        System.out.println("Check your yml");
    }

    @Test
    public void saveNews() throws IOException {
        YamlConfiguration testConfig = yamlConfig.getConfig();

        ArrayList<YamlConfigNews> list = new ArrayList<>();
        list.add(new YamlConfigNews("hoge", "http://www.google.co.jp/"));
        list.add(new YamlConfigNews("fuga", "http://www.yahoo.co.jp/"));

        List<Map<String, Map<String, String>>> newsMaps = YamlConfigNews.toMapList(list);
        testConfig.set("news", newsMaps);

        yamlConfig.save();

        List<Map<?, ?>> loadMapList = testConfig.getMapList("news");
        List<YamlConfigNews> loadedNewsMaps = YamlConfigNews.fromMapList(loadMapList);
        assertEquals(2, loadedNewsMaps.size());

        YamlConfigNews yamlConfigNewsHoge = loadedNewsMaps.get(0);
        assertEquals("hoge", yamlConfigNewsHoge.getNewsId());
        assertEquals("http://www.google.co.jp/", yamlConfigNewsHoge.getUrl());

        YamlConfigNews yamlConfigNewsFuga = loadedNewsMaps.get(1);
        assertEquals("fuga", yamlConfigNewsFuga.getNewsId());
        assertEquals("http://www.yahoo.co.jp/", yamlConfigNewsFuga.getUrl());
    }

}
