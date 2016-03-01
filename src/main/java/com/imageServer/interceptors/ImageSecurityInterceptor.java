package com.imageServer.interceptors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Repository
public class ImageSecurityInterceptor extends HandlerInterceptorAdapter {

    private Logger log = LoggerFactory.getLogger(ImageSecurityInterceptor.class);

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
			Object arg2) throws Exception {
        response.setCharacterEncoding("UTF-8");  
        response.setContentType("text/html;charset=UTF-8"); 
        String url = request.getRequestURI();
        String root = request.getContextPath();
        url = url.replaceAll(root, "");
//        if(url.equals("/") || url.equals("\\")){
//        	return true;
//        }
        log.info("接受请求：" + request.getRemoteAddr() + ":" + request.getRequestURI());
        return true;
	}
}
