package com.winterchen.airportal.utils;

import com.winterchen.airportal.entity.User;
import lombok.Data;

/**
 * @author winterchen
 * @version 1.0
 * @date 2022/2/24 9:53
 * @description 用户信息
 **/
public final class UserThreadLocal {

    private static final ThreadLocal<ThreadContext> LOCAL = new ThreadLocal<ThreadContext>();


    public static  void initThreadLocal() {
        if(LOCAL.get() == null)
            LOCAL.set(new ThreadContext());
    }

    public static void setUser(User user){
        //必须先调用该方法才能get set
        initThreadLocal();
        LOCAL.get().setUser(user);
    }

    public static User getUser(){
        //必须先调用该方法才能get set
        initThreadLocal();
        return LOCAL.get().getUser();
    }

    public static void remove(){
        LOCAL.remove();
    }

    @Data
    static  class ThreadContext{

        private User user;

    }

}