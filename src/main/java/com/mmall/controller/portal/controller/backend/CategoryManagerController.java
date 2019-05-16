package com.mmall.controller.portal.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.ICategoryService;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

/**
 * @author caoduanxi
 * @2019/5/7 10:02
 */
@Controller
@RequestMapping("/manage/category/")
public class CategoryManagerController {
    @Autowired
    private IUserService iUserService;

    @Autowired
    private ICategoryService iCategoryService;


    @RequestMapping(value = "add_category.do")
    @ResponseBody
    public ServerResponse addCategory(HttpSession session, @RequestParam(value = "parentId", defaultValue="0") int parentId, String categoryName){
        //从session中获取对象
        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if(currentUser == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登录");
        }
        //判断对象是否是管理员
        if(iUserService.checkAdminRole(currentUser).isSuccess()){
            //是管理员，可以执行增加品类操作
            ServerResponse response = iCategoryService.addCategory(parentId, categoryName);
            if(response.isSuccess()){
                return ServerResponse.createBySuccessMessage("添加品类成功");
            }
        }else{
            return ServerResponse.createByErrorMessage("当前用户无管理员权限");
        }
        return ServerResponse.createByErrorMessage("添加品类失败");
    }

    //修改品类名称
    @RequestMapping(value = "set_category_name.do")
    @ResponseBody
    public ServerResponse<String> setCategoryName(HttpSession session,int categoryId, String categoryName){
        //从session中获取对象
        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if(currentUser == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登录");
        }
        //判断对象是否是管理员
        if(iUserService.checkAdminRole(currentUser).isSuccess()){
            return iCategoryService.updateCategoryName(categoryId,categoryName);
        }else{
            return ServerResponse.createByErrorMessage("当前用户无管理员权限");
        }
    }

    //查询与自己同级别的品类
    @RequestMapping(value = "get_category.do")
    @ResponseBody
    public ServerResponse getChildrenParallelCategory(HttpSession session, @RequestParam(value = "categoryId" ,defaultValue = "0") Integer categoryId){
        //从session中获取对象
        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if(currentUser == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登录");
        }
        //判断对象是否是管理员
        if(iUserService.checkAdminRole(currentUser).isSuccess()){
            //获取同级别品类的id
            return iCategoryService.selectChildrenParallelCategory(categoryId);
        }else{
            return ServerResponse.createByErrorMessage("当前用户无管理员权限");
        }
    }
    @RequestMapping(value = "get_deep_category.do")
    @ResponseBody
    public ServerResponse getCategoryAndChildrenCategory(HttpSession session, @RequestParam(value = "categoryId" ,defaultValue = "0") Integer categoryId){
        //从session中获取对象
        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if(currentUser == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登录");
        }
        //判断对象是否是管理员
        if(iUserService.checkAdminRole(currentUser).isSuccess()){
            //获取递归得到的所有的子节点
            return iCategoryService.selectCategoryAndChildrenCategoryById(categoryId);
        }else{
            return ServerResponse.createByErrorMessage("当前用户无管理员权限");
        }
    }
}
