package com.mmall.controller.portal.controller.backend;

import com.github.pagehelper.PageInfo;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * @author caoduanxi
 * @2019/5/15 15:23
 */
@Controller
@RequestMapping("/manage/order/")
public class OrderManageController {
    @Autowired
    private IUserService iUserService;
    @Autowired
    private IOrderService iOrderService;

    //后台获取详情页
    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse manageDetail(HttpSession session, Long orderNo){
        //获取当前对象
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录，需要登录");
        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            //填充业务代码
            return iOrderService.getManageDetail(orderNo);
        }else{
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }

    //后台获取列表页
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse<PageInfo> manageList(HttpSession session, Long orderNo,
                                               @RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
                                               @RequestParam(value = "pageSize",defaultValue = "10") int pageSize){
        //获取当前对象
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录，需要登录");
        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            //填充业务代码
            return iOrderService.getManageList(orderNo,pageNum,pageSize);
        }else{
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }

    //后台查询
    @RequestMapping("search.do")
    @ResponseBody
    public ServerResponse<PageInfo> manageSearch(HttpSession session, Long orderNo,
                                                 @RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
                                                 @RequestParam(value = "pageSize",defaultValue = "10") int pageSize){
        //获取当前对象
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录，需要登录");
        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            //填充业务代码
            return iOrderService.getManageSearch(orderNo,pageNum,pageSize);
        }else{
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }

    //后台发货
    @RequestMapping("send_goods.do")
    @ResponseBody
    public ServerResponse<PageInfo> manageSendGoods(HttpSession session, Long orderNo){
        //获取当前对象
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录，需要登录");
        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            //填充业务代码
            return iOrderService.manageSendGoods(orderNo);
        }else{
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }


}
