package com.esperia09.rssnewsbook;

/**
 * Created by esperia on 2016/06/05.
 */
public interface Consts {
    String PLUGIN_NAME = "RSSNewsBook";
    String FMT_ISO8601 = "yyyyMMdd'T'HHmmss.SSSZ";
    String URL_UPDATE_CHECK = "http://esperia.herokuapp.com/rssnewsbook.xml";

    enum Commands {
        rssnews
    }
}
