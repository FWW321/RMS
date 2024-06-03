package com.fww.interceptor;

import com.fww.utils.JwtFun;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)  {
        String token = request.getHeader("token");

        System.out.println(token);
        try{
            Map<String, Object> result = JwtFun.verifyToken(token);
            request.setAttribute("mPhone", result.get("user"));
            System.out.println("result");
            System.out.println(result);
            return true;
        }catch (Exception e){
            System.out.println("令牌验证失败");
            response.setStatus(401);
            return false;
        }
    }
}
