package com.mmall.controller.portal.controller.backend;

import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.pojo.User;
import com.mmall.service.IFileService;
import com.mmall.service.IProductService;
import com.mmall.service.IUserService;
import com.mmall.util.PropertiesUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * @author caoduanxi
 * @2019/5/7 13:49
 */
@Controller
@RequestMapping("/manage/product/")
public class ProductManageController {
    @Autowired
    private IUserService iUserService;
    @Autowired
    private IProductService iProductService;
    @Autowired
    private IFileService iFileService;

    @RequestMapping(value = "save.do")
    @ResponseBody
    public ServerResponse saveOrUpdateProduct(HttpSession session, Product product){
        //获取当前对象
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录，需要登录");
        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            //执行更新或者保存步骤
            return iProductService.saveOrUpdateProduct(product);
        }else{
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }

    //产品上下架
    @RequestMapping(value = "set_sale_status.do")
    @ResponseBody
    public ServerResponse setSaleStatus(HttpSession session, Integer productId, Integer status){
        //获取当前对象
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录，需要登录");
        }
        if(iUserService.checkAdminRole(user).isSuccess()){
           return iProductService.updateSaleStatus(productId,status);
        }else{
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }

    @RequestMapping(value = "detail.do")
    @ResponseBody
    public ServerResponse getDetail(HttpSession session, Integer productId){
        //获取当前对象
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录，需要登录");
        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            return iProductService.manageProductDetail(productId);
        }else{
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }

    //主要是获取分页
    @RequestMapping(value = "list.do")
    @ResponseBody
    public ServerResponse getList(HttpSession session, @RequestParam(value = "pageNum", defaultValue = "10") int pageNum,@RequestParam(value = "pageSize", defaultValue = "1") int pageSize){
        //获取当前对象
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录，需要登录");
        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            //填充业务代码
            return iProductService.getList(pageNum,pageSize);
        }else{
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }
    @RequestMapping(value = "search.do")
    @ResponseBody
    public ServerResponse productSearch(HttpSession session,String productName, Integer productId, @RequestParam(value = "pageNum", defaultValue = "10") int pageNum,@RequestParam(value = "pageSize", defaultValue = "1") int pageSize){
        //获取当前对象
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录，需要登录");
        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            //填充业务代码
            return iProductService.searchProduct(productName,productId,pageNum,pageSize);
        }else{
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }
    @RequestMapping(value = "upload.do")
    @ResponseBody
    public ServerResponse upload(HttpSession session, @RequestParam(value = "upload_file",required = false)MultipartFile file, HttpServletRequest request){
        //获取当前对象
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录，需要登录");
        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            //获取文件的路径 相当于在webapp下建立一个upload的文件夹
            String path = request.getSession().getServletContext().getRealPath("upload");
            //上传文件 封装一个工具类
            String targetFileName = iFileService.upload(file, path);
            //拼接一个url
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFileName;
            //将文件存入Map集合中
            Map<String, String> fileMap = Maps.newHashMap();
            fileMap.put("uri",targetFileName);
            fileMap.put("url",url);
            return ServerResponse.createBySuccess(fileMap);
        }else{
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }

    @RequestMapping(value = "richtext_img_upload.do")
    @ResponseBody
    public Map richTextImgUpload(HttpSession session, @RequestParam(value = "upload_file",required = false)MultipartFile file, HttpServletRequest request, HttpServletResponse response){
        //创建一个map来存储富文本所要返回的信息
        Map<String, String> resultMap = Maps.newHashMap();
        //获取当前对象
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            resultMap.put("success","false");
            resultMap.put("msg","需要登录管理员权限");
            return resultMap;
        }
        //富文本对于返回值有自己的格式要求。
//        返回的信息格式
//        {
//          "success": true/false,
//          "msg": "error message", # optional
//          "file_path": "[real file path]"
//        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            //获取文件的路径 相当于在webapp下建立一个upload的文件夹
            String path = request.getSession().getServletContext().getRealPath("upload");
            //上传文件 封装一个工具类
            String targetFileName = iFileService.upload(file, path);
            if(StringUtils.isBlank(targetFileName)){
                resultMap.put("success","false");
                resultMap.put("msg","上传失败");
                return resultMap;
            }
            //拼接一个url
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFileName;
            resultMap.put("success","false");
            resultMap.put("msg","上传文件成功");
            resultMap.put("file_path",url);
            response.setHeader("Access-Control-Allow-Headers","X-File-Name");
            return resultMap;
        }else{
            resultMap.put("success","false");
            resultMap.put("msg","无权限操作");
            return resultMap;
        }
    }
}
