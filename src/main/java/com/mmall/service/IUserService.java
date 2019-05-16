package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;

/**
 * @author caoduanxi
 * @2019/4/28 21:52
 */
public interface IUserService {
    //提供一个实现登录的接口方法即可。
    ServerResponse<User> login(String username, String password);
    //实现注册功能
    ServerResponse<String> register(User user);
    //校验用户名和邮箱
    ServerResponse<String> checkValid(String str, String type);

    ServerResponse<String> selectQuestion(String username);

    ServerResponse<String> checkAnswer(String username, String question, String answer);

    ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken);

    ServerResponse<String> resetPassword(String passwordOld, String passwordNew,User user);

    ServerResponse<User> updateInformation(User user);

    ServerResponse<User> getInformation(int userId);

    ServerResponse<String> checkAdminRole(User user);
}
