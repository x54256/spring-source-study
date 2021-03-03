package cn.x5456.session.redis1.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.session.SessionRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author yujx
 * @date 2021/02/20 09:21
 */
@RestController
public class LoginController {

    @Autowired
    private SessionRepository sessionRepository;

    // 自动注入的 session 如果不主动使用也不会 setCookie
    @Autowired
    private HttpSession session;

    /**
     * 普通接口，不设置 session
     */
    @GetMapping("/func")
    public String func() {
        return "func";
    }

    /**
     * 测试登录接口，设置 session
     *
     * Set-Cookie: SESSION=NzUyNTE5MjgtMDY5Yy00MDcxLTg5NzEtN2JhMzYzZjIxMmYy; Path=/redis-session/; HttpOnly; SameSite=Lax
     *
     * 重复访问这个接口是不会重复 setCookie 的，毕竟改变的是 session，cookie 只是一个标识
     */
    @GetMapping("/login")
    public String login(HttpSession session, HttpServletRequest request) {
        session.setAttribute("user", "测试用户");
        return "login";
    }
}
