package cn.x5456.session.simple.controller;

import org.springframework.beans.factory.annotation.Autowired;
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
     * Set-Cookie: JSESSIONID=1F2DB78ACBF665290ABCA9788C92BDF7; Path=/simple-session; HttpOnly
     *
     * 重复访问这个接口是不会重复 setCookie 的，毕竟改变的是 session，cookie 只是一个标识
     */
    @GetMapping("/login")
    public String login(HttpSession session, HttpServletRequest request) {
        /*
        底层是一个 hashmap，key 是 cookie 中的 JSESSIONID 的值，value 是 Session 对象
         */
        HttpSession session1 = request.getSession();
        session1.setAttribute("user", "测试用户");
        return "login";
    }

}
