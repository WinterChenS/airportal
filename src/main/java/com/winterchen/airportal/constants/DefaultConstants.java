package com.winterchen.airportal.constants;

/**
 * @author winterchen
 * @version 1.0
 * @date 2021/12/28 16:58
 * @description 常量
 **/
public class DefaultConstants {


    public static class Common {

        public static final Integer TRUE = 1;

        public static final Integer FALSE = 0;

    }

    public static class EHCache {

        /**
         * 元素最大数量
         */

        public static int MAXELEMENTSINMEMORY = 50000;

        /**
         *
         * 是否把溢出数据持久化到硬盘
         */

        public static boolean OVERFLOWTODISK = true;

        /**
         *
         * 是否会死亡
         */

        public static boolean ETERNAL = false;

        /**
         *
         * 缓存的间歇时间
         */

        public static int TIMETOIDLESECONDS = 600;

        /**
         *
         * 存活时间(默认2小时)
         */

        public static int TIMETOlIVESECONDS = 7200;

        /**
         *
         * 需要持久化到硬盘否
         */

        public static boolean DISKPERSISTENT = false;

        /**
         *
         * 内存存取策略
         */

        public static String MEMORYSTOREEVICTIONPOLICY = "LFU";
    }

    public static class Message {
        /**
         * 消息分片大小
         */
        public static Integer BLOCK_MAX_NUM = 20;


    }

    public static class User {
        public static final String USER_ID = "userId";
        public static final String TOKEN_KEY = "Authorization";
        public static final String USER = "airportal_user";
        /**
         * 登陆密码加密秘钥
         */
        public static final String SECRET_KEY = "airportal_user";
    }

}