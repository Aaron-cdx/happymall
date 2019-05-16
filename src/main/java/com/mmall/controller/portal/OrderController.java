package com.mmall.controller.portal;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import com.mmall.vo.OrderVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Iterator;
import java.util.Map;

/**
 * @author caoduanxi
 * 永远不要畏惧失败，因为只有失败之后才会实质成长！
 * @2019/5/12 15:03
 */
@Controller
@RequestMapping("/order/")
public class OrderController {
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    @Autowired
    private IOrderService iOrderService;




    @RequestMapping("create.do")
    @ResponseBody
    public ServerResponse createOrder(HttpSession session, Integer shippingId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        //逻辑代码的调用
        return iOrderService.createOrder(user.getId(),shippingId);
    }

    //取消订单
    @RequestMapping("cancel.do")
    @ResponseBody
    public ServerResponse cancel(HttpSession session, Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        //逻辑代码的调用
        return iOrderService.cancelOrder(user.getId(),orderNo);
    }

    //用户订单生成之后，要获取其余未勾选商品的信息
    @RequestMapping("get_order_cart_product.do")
    @ResponseBody
    public ServerResponse getOrderCartProduct(HttpSession session){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        //逻辑代码的调用
        return iOrderService.getOrderCartProduct(user.getId());
    }

    //用户订单详情
    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse<OrderVo> orderDetail(HttpSession session, Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        //逻辑代码的调用
        return iOrderService.getOrderDetail(user.getId(),orderNo);
    }

    //用户订单列表
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse<PageInfo> orderList(HttpSession session,
                                              @RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
                                              @RequestParam(value = "pageSize",defaultValue = "10")int pageSize){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        //逻辑代码的调用
        return iOrderService.getOrderList(user.getId(), pageNum, pageSize);
    }


















    //使用支付宝支付账单 注意订单编号是Long类型
    @RequestMapping("pay.do")
    @ResponseBody
    public ServerResponse pay(HttpSession session, Long orderNo, HttpServletRequest request){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        //获取地址,请求的真实地址
        String path = request.getSession().getServletContext().getRealPath("upload");
        //逻辑代码的调用
        return iOrderService.pay(orderNo,user.getId(),path);
    }

    //支付宝的回调函数
    @RequestMapping("alipay_callback.do")
    @ResponseBody
    public Object alipayCallback(HttpServletRequest request){
        Map<String, String> params = Maps.newHashMap();
        //所有的内容都放在请求中
        Map<String, String[]> requestParams = request.getParameterMap();
        //使用迭代器
        for(Iterator iterator = requestParams.keySet().iterator(); iterator.hasNext();){
            String name = (String)iterator.next();
            String[] values = requestParams.get(name);
            //对于值做一个拼接
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                //判断是否是最后一个元素，以判断是否需要使用逗号拼接
                valueStr = (i == values.length - 1) ? valueStr+values[i] : valueStr+values[i]+",";
            }
            //从request中获取到了数据
            params.put(name,valueStr);
        }
        logger.info("支付宝回调,sign:{},trade_status:{},参数:{}",params.get("sign"),params.get("trade_status"),params.toString());
        //验证回调的正确性，验证是否是支付宝发送的，同时要避免重复通知
        //首先根据要求移除sign、sign_type,由于sign支付宝自己移除了
        params.remove("sign_type");
        try {
            boolean alipayRSTCheckedV2 = AlipaySignature.rsaCheckV2(params,Configs.getAlipayPublicKey(),"utf-8",Configs.getSignType());
            if(!alipayRSTCheckedV2){
                return ServerResponse.createByErrorMessage("回调请求异常，错误请求再次发送直接转网警");
            }
        } catch (AlipayApiException e) {
            logger.error("支付宝回调异常");
        }
        /**
         * 商户需要验证该通知数据中的 out_trade_no 是否为商户系统中创建的订单号，
         * 并判断 total_amount 是否确实为该订单的实际金额（即商户订单创建时的金额），
         * 同时需要校验通知中的 seller_id（或者seller_email) 是否为 out_trade_no 这笔单据的对应的操作方
         * （有的时候，一个商户可能有多个 seller_id/seller_email），
         * 上述有任何一个验证不通过，则表明本次通知是异常通知，务必忽略。
         * 在上述验证通过后商户必须根据支付宝不同类型的业务通知，
         * 正确的进行不同的业务处理，并且过滤重复的通知结果数据。
         * 在支付宝的业务通知中，只有交易通知状态为 TRADE_SUCCESS 或 TRADE_FINISHED 时，
         * 支付宝才会认定为买家付款成功。
         */
        //TODO 验证各种参数的正确性 这里没有做验证
        //如果支付宝能够完成回调的逻辑，那么可以表示此订单没有问题，可以执行验证订单是否支付
        //逻辑代码 验证订单是否支付
        ServerResponse serverResponse = iOrderService.alipayCallback(params);
        if(serverResponse.isSuccess()){
            return Const.AlipayCallback.RESPONSE_SUCCESS;
        }
        return Const.AlipayCallback.RESPONSE_FAILED;
    }

    //前端需要知道订单的是否支付成功
    @RequestMapping("query_order_pay_status.do")
    @ResponseBody
    public ServerResponse<Boolean> queryOrderPayStatus(HttpSession session, Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        ServerResponse serverResponse = iOrderService.queryOrderPayStatus(user.getId(), orderNo);
        if(serverResponse.isSuccess()){
            return ServerResponse.createBySuccess(true);
        }
        return serverResponse.createBySuccess(false);
    }

}
