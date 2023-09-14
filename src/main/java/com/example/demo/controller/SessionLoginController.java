package com.example.demo.controller;

import com.example.demo.domain.User;
import com.example.demo.domain.UserRole;
import com.example.demo.dto.JoinRequestDto;
import com.example.demo.dto.LoginRequestDto;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/session-login")
public class SessionLoginController {

    private final UserService userService;
    public static Hashtable sessionList = new Hashtable();

    @GetMapping(value = {"", "/"})
    public String home(Model model, @SessionAttribute(name = "userId", required = false) Long userId) {
        model.addAttribute("loginType", "session-login");
        model.addAttribute("pageName", "세션 로그인");

        User loginUser = userService.getLoginUserById(userId);

        if(loginUser != null) {
            model.addAttribute("nickname", loginUser.getNickname());
        }

        return "home";
    }


    @GetMapping("/join")
    public String joinPage(Model model){
        model.addAttribute("loginType" , "session-login");
        model.addAttribute("pageName", "세션 로그인");

        model.addAttribute("joinRequest" , new JoinRequestDto());
        return "join";
    }

    @PostMapping("/join")
    public String join(@Valid @ModelAttribute JoinRequestDto joinRequestDto, BindingResult bindingResult, Model model){
        model.addAttribute("loginType", "session-login");
        model.addAttribute("pageName" , "세션 로그인");

        if(userService.checkLoginDuplicate(joinRequestDto.getLoginId())){
            bindingResult.addError(new FieldError("joinRequestDto" , "loginId" , "로그인 아이디가 중복됩니다."));
        }

        if(userService.checkNicknameDuplicate(joinRequestDto.getNickname())){
            bindingResult.addError(new FieldError("joinRequestDto" , "nickname" , "닉네임이 중복됩니다."));
        }

        if(!joinRequestDto.getPassword().equals(joinRequestDto.getPasswordCheck())) {
            bindingResult.addError(new FieldError("joinRequestDto", "passwordCheck", "비밀번호가 일치하지 않습니다."));
        }

        if(bindingResult.hasErrors()){
            return "join";
        }

        userService.join(joinRequestDto);
        return "redirect:/session-login";
    }

    @GetMapping("/login")
        public String loginPage(Model model){
        model.addAttribute("loginType" , "session-login");
        model.addAttribute("pageName", "세션 로그인");

        model.addAttribute("loginRequest", new LoginRequestDto());
        return "login";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute("loginRequest")@Valid LoginRequestDto loginRequestDto, BindingResult bindingResult,
                        HttpServletRequest httpServletRequest, Model model){
        model.addAttribute("loginType", "session-login");
        model.addAttribute("pageName", "세션 로그인");

        User user = userService.login(loginRequestDto);

        if(user == null){
            bindingResult.reject("loginFail" , "로그인 아이디 또는 비밀번호가 틀렸습니다.");
        }

        if(bindingResult.hasErrors()){
            return "login";
        }

        httpServletRequest.getSession().invalidate();
        HttpSession session = httpServletRequest.getSession(true);

        session.setAttribute("userId", user.getId());
        session.setMaxInactiveInterval(1800);

        return "redirect:/session-login";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest httpServletRequest, Model model){
        model.addAttribute("loginType", "session-login");
        model.addAttribute("pageName" , "세션 로그인");

        HttpSession session = httpServletRequest.getSession(false);

        if(session != null){
            session.invalidate();
            sessionList.remove(session.getId());
        }
        return "redirect:/session-login";
    }

    @GetMapping("/info")
    public String userInfo(@SessionAttribute(name = "userId" , required = false)Long userId, Model model){
        model.addAttribute("loginType", "session-login");
        model.addAttribute("pageName" , "세션 로그인");

        User loginUser = userService.getLoginUserById(userId);

        if(loginUser == null){
            return "redirect:/session-login/login";
        }

        model.addAttribute("user", "loginUser");
        return "info";
    }

    @GetMapping("/admin")
    public String adminPage(@SessionAttribute(name = "userId" , required = false)Long userId, Model model){
        model.addAttribute("loginType", "session-login");
        model.addAttribute("pageName" , "세션 로그인");

        User loginUser = userService.getLoginUserById(userId);

        if(loginUser == null){
            return "redirect:/session-login/login";
        }

        if(!loginUser.getRole().equals(UserRole.ADMIN)){
            return "redirect:/session-login";
        }
        return "admin";
    }

    @GetMapping("/session-list")
    @ResponseBody

    public Map<String, String> sessionList() {
        Enumeration elements = sessionList.elements();
        Map<String, String> lists = new HashMap<>();
        while(elements.hasMoreElements()) {
            HttpSession session = (HttpSession)elements.nextElement();
            lists.put(session.getId(), String.valueOf(session.getAttribute("userId")));
        }
        return lists;
    }
}
