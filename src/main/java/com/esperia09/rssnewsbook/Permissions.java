package com.esperia09.rssnewsbook;

/**
 * Created by esperia on 2016/06/20.
 */
public interface Permissions {
    String PREFIX = "rssnewsbook";

    String LIST = PREFIX + ".command.list";
    String CONVERT = PREFIX + ".command.convert";
    String UPDATE = PREFIX + ".command.update";

    String ADMIN_NEWS_ADD = PREFIX + ".admin.command.add";
}
