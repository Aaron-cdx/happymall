package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.mmall.common.ServerResponse;
import com.mmall.dao.ShippingMapper;
import com.mmall.pojo.Shipping;
import com.mmall.service.IShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author caoduanxi
 * @2019/5/11 14:58
 */
@Service("iShippingService")
public class ShippingServiceImpl implements IShippingService {

    @Autowired
    private ShippingMapper shippingMapper;

    public ServerResponse add(Integer userId, Shipping shipping){
        //新增的地址是没有userId的，插入之前先设置
        shipping.setId(userId);
        //将地址插入
        int rowCount = shippingMapper.insert(shipping);
        //由于是新增地址，需要返回id给前端
        if(rowCount > 0){
            Map result = Maps.newHashMap();
            result.put("shippingId",shipping.getId());
            return ServerResponse.createBySuccess("新增地址成功",result);
        }
        return ServerResponse.createByErrorMessage("新增地址失败");
    }

    public ServerResponse delete(Integer userId, Integer shippingId){
        //直接使用shippingId的话，容易引起横向越权，就是非此用户下的用户会删除此地址
        //所以结合userId来避免横向越权
        int rowCount = shippingMapper.deleteShippingByUserIdShippingId(userId, shippingId);
        if(rowCount > 0){
            return ServerResponse.createBySuccessMessage("删除地址成功");
        }
        return ServerResponse.createByErrorMessage("删除地址失败");
    }

    public ServerResponse update(Integer userId, Shipping shipping){
        //用户id是不能被更新的
        shipping.setId(userId);
        int rowCount = shippingMapper.updateShippingByShipping(shipping);
        if(rowCount > 0){
            return ServerResponse.createBySuccessMessage("更新地址成功");
        }
        return ServerResponse.createByErrorMessage("更新地址失败");
    }

    public ServerResponse<Shipping> select(Integer userId, Integer shippingId){
        //只能查询当前用户下的shippingId
        Shipping shipping = shippingMapper.selectShippingByUserIdShippingId(userId, shippingId);
        if(shipping == null){
            return ServerResponse.createByErrorMessage("无法查询到当前用户地址");
        }
        return ServerResponse.createBySuccess(shipping);
    }

    public ServerResponse<PageInfo> list(Integer userId,int pageNum, int pageSize){
        PageHelper.startPage(pageNum,pageSize);
        //根据userId查询所有的地址
        List<Shipping> shippingList = shippingMapper.selectShippingListByUserId(userId);
        PageInfo pageInfo = new PageInfo(shippingList);
        return ServerResponse.createBySuccess(pageInfo);
    }


}
