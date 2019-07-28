package net.easydebug.osexplorer.util;

import com.amazonaws.services.s3.AmazonS3;

import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

public class AppConstants {

    /**
     * 软件名称
     */
    public static final String APP_NAME = "OSExplorer";

    /**
     * 配置文件
     */
    public static final String USER_HOME_PATH = System.getProperty("user.home") + "/";
    public static final String CONFIG_DIR_PATH = USER_HOME_PATH + ".os_explorer/";
    public static final String CONFIG_FILE_PATH = "sources.settings";
    public static final String CONFIG_PATH = CONFIG_DIR_PATH + CONFIG_FILE_PATH;

    /**
     * 全局变量
     */
    public static String serviceType;
    public static AmazonS3 s3;
    public static String bucketName;

}
