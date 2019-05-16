package com.mmall.controller.portal.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * @author caoduanxi
 * @2019/5/6 20:53
 */
@Controller
@RequestMapping("/manager/")
public class UserManageController {

    @Autowired
    private IUserService iUserService;

    /*
    需要注意，一般的登录，登录之后检验身份，身份正确的话存入session中，等待后续操作
     */
    @RequestMapping(value = "login.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> login(String username, String password, HttpSession session){
        //管理员登录
        ServerResponse<User> response = iUserService.login(username, password);
        //判断用户的级别
        if(response.isSuccess()){
            User user = response.getData();
            if(user.getRole() == Const.Role.ROLE_ADMIN){
                //存入session中
                session.setAttribute(Const.CURRENT_USER, user);
            }else{
                return ServerResponse.createByErrorMessage("不是管理员，无法登录");
            }
        }
        return response;
    }
}
