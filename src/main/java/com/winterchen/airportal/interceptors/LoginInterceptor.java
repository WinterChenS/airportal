package com.winterchen.airportal.interceptors;

import com.google.gson.Gson;
import com.winterchen.airportal.annotation.NotLoginAccess;
import com.winterchen.airportal.base.Result;
import com.winterchen.airportal.base.ResultCode;
import com.winterchen.airportal.constants.DefaultConstants;
import com.winterchen.airportal.entity.User;
import com.winterchen.airportal.service.UserService;
import com.winterchen.airportal.utils.EhcacheUtil;
import com.winterchen.airportal.utils.UserThreadLocal;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @author winterchen
 * @version 1.0
 * @date 2022/4/13 14:53
 * @description 权限拦截
 **/
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        try {
            if (handler instanceof ResourceHttpRequestHandler) {
                return true;
            }
            boolean isNotLoginAccess = hasAnnotation(handler, NotLoginAccess.class);
            if (isNotLoginAccess) {
                log.debug("==> 方法标记为无需登录也可访问");
                return true;
            }
            //若是白名单,获取登录信息
            User user = getUserByRequest(request);


            //若未登录
            if(user == null) {
                outErrorMsg(response);
                return false;
            }


            //登录用户存在ThreadLocal中
            UserThreadLocal.setUser(user);


            response.addHeader(DefaultConstants.User.TOKEN_KEY, request.getHeader(DefaultConstants.User.TOKEN_KEY));

            //true : 继续执行后续操作 , false , 不执行后续操作
            return true;

        } catch (Exception e) {
            log.error("拦截器preHandle异常 : ", e);
            outErrorMsg(response, 500 , e.getMessage());
            return false;
        }

    }

    private User getUserByRequest(HttpServletRequest request) {
        final String token = request.getHeader(DefaultConstants.User.TOKEN_KEY);
        if (StringUtils.isEmpty(token)) {
            return null;
        }
        return  (User) EhcacheUtil.get(token);
    }

    /**
     * 请求处理之后进行调用，但是在视图被渲染之前（Controller方法调用之后）
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        UserThreadLocal.remove();
    }

    /**
     * 在整个请求结束之后被调用，也就是在DispatcherServlet 渲染了对应的视图之后执行（主要是用于进行资源清理工作）
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }


    /**
     * @function 输出错误信息
     * @param response
     * @author chendonghua
     * @throws IOException
     * @date 2020年11月3日
     */
    private void outErrorMsg(HttpServletResponse response , int code , String msg) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        PrintWriter out = response.getWriter();

        out.append(new Gson().toJson(Result.error(code, msg)));
        out.flush();
    }

    /**
     * @function 输出错误信息
     * @param response
     * @author 肖荣辉
     * @throws IOException
     * @date 2020年11月3日
     */
    private void outErrorMsg(HttpServletResponse response) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        PrintWriter out = response.getWriter();
        out.append(new Gson().toJson(Result.error(ResultCode.USER_NOT_LOGIN.getCode())));
        out.flush();
    }

    private boolean hasAnnotation(Object handler, Class<? extends Annotation> annotationClass) {
        boolean hasAnnotation = false;
        try {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            hasAnnotation = AnnotationUtils.findAnnotation(method, annotationClass) != null;
        } catch (Throwable throwable) {
            log.error(throwable.getMessage(), throwable);
        }
        return hasAnnotation;
    }

}