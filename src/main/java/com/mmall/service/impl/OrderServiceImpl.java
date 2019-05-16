package com.mmall.service.impl;

import com.alipay.api.AlipayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.utils.ZxingUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.dao.*;
import com.mmall.pojo.*;
import com.mmall.service.IOrderService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.FTPUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.OrderItemVo;
import com.mmall.vo.OrderProductVo;
import com.mmall.vo.OrderVo;
import com.mmall.vo.ShippingVo;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author caoduanxi
 * @2019/5/12 15:04
 */
@Service("iOrderService")
public class OrderServiceImpl implements IOrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private PayInfoMapper payInfoMapper;
    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private ShippingMapper shippingMapper;





    public ServerResponse pay(Long orderNo, Integer userId, String path){
        Map<String, String> resultMap = Maps.newHashMap();
        //通过orderNo和userId来查询订单中是否有这个订单
        Order order = orderMapper.selectOrderByOrderNoUserId(orderNo, userId);
        if(order == null){
            return ServerResponse.createByErrorMessage("用户没有此订单");
        }
        // 订单存在，放入订单号
        resultMap.put("orderNo",String.valueOf(order.getOrderNo()));
        // 走支付逻辑


        // (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
        // 需保证商户系统端不能重复，建议通过数据库sequence生成，
        String outTradeNo = order.getOrderNo().toString();

        // (必填) 订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店消费”
        String subject = new StringBuilder().append("happymmall扫码支付，订单编号为：").append(order.getOrderNo().toString()).toString();

        // (必填) 订单总金额，单位为元，不能超过1亿元
        // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
        String totalAmount = order.getPayment().toString();

        // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
        // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
        String undiscountableAmount = "0.0";

        // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
        // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
        String sellerId = "";

        // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品3件共20.00元"
        String body = new StringBuilder().append("订单").append(outTradeNo).append("购买商品共").append(totalAmount).toString();

        // 商户操作员编号，添加此参数可以为商户操作员做销售统计
        String operatorId = "test_operator_id";

        // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
        String storeId = "test_store_id";

        // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId("2088100200300400500");

        // 支付超时，定义为120分钟
        String timeoutExpress = "120m";

        // 商品明细列表，需填写购买商品详细信息，
        List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();

        //获取订单中的所有的订单细节
        List<OrderItem> orderItemList = orderItemMapper.selectOrderItemByOrderNoUserId(order.getOrderNo(),userId);
        for(OrderItem orderItem : orderItemList){
            // 创建一个商品信息，参数含义分别为商品id（使用国标）、名称、单价（单位为分）、数量，如果需要添加商品类别，详见GoodsDetail
            GoodsDetail goods = GoodsDetail.newInstance(orderItem.getProductId().toString(),orderItem.getProductName(),
                    BigDecimalUtil.mul(orderItem.getCurrentUnitPrice().doubleValue(),100).longValue(),orderItem.getQuantity());
            // 创建好一个商品后添加至商品明细列表
            goodsDetailList.add(goods);
        }

        // 创建扫码支付请求builder，设置请求参数
        AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
                .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
                .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
                .setTimeoutExpress(timeoutExpress)
                                .setNotifyUrl(PropertiesUtil.getProperty("alipay.callback.url"))//支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
                .setGoodsDetailList(goodsDetailList);

        /** 一定要在创建AlipayTradeService之前调用Configs.init()设置默认参数
         *  Configs会读取classpath下的zfbinfo.properties文件配置信息，如果找不到该文件则确认该文件是否在classpath目录
         */
        Configs.init("zfbinfo.properties");

        /** 使用Configs提供的默认参数
         *  AlipayTradeService可以使用单例或者为静态成员对象，不需要反复new
         */
        AlipayTradeService tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();

        AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);
        switch (result.getTradeStatus()) {
            case SUCCESS:
                logger.info("支付宝预下单成功: )");

                AlipayTradePrecreateResponse response = result.getResponse();
                dumpResponse(response);
                //创建此路径的文件夹
                File folder = new File(path);
                //不存在就创建
                if(!folder.exists()){
                    folder.setWritable(true);
                    folder.mkdirs();
                }
                //存在的话，传入
                //细节 注意是path最后是没有/的，所以需要人为的加上
                String qrPath = String.format(path+"/qr-%s.png",response.getOutTradeNo());
                //后面的交易单号会替换%s
                String qrFileName = String.format("qr-%s.png",response.getOutTradeNo());
                //生成的二维码图片 -> 图片的路径为qrPath
                ZxingUtils.getQRCodeImge(response.getQrCode(), 256, qrPath);
                //第一个是parent，第二个是child
                File targetFile = new File(path,qrFileName);

                try {
                    //上传到FTP服务器
                    FTPUtil.uploadFile(Lists.newArrayList(targetFile));
                } catch (IOException e) {
                    logger.error("上传二维码图片异常",e);
                }
                logger.info("qrPath",qrPath);

                String qrUrl = PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFile.getName();
                //二维码的qrUrl路径
                resultMap.put("qrUrl",qrUrl);
                return ServerResponse.createBySuccess(resultMap);//最后前端获取到的就是订单编号和二维码图片

            case FAILED:
                logger.error("支付宝预下单失败!!!");
                return ServerResponse.createByErrorMessage("支付宝预下单失败!!!");

            case UNKNOWN:
                logger.error("系统异常，预下单状态未知!!!");
                return ServerResponse.createByErrorMessage("系统异常，预下单状态未知!!!");

            default:
                logger.error("不支持的交易状态，交易返回异常!!!");
                return ServerResponse.createByErrorMessage("不支持的交易状态，交易返回异常!!!");
        }

    }
    // 简单打印应答
    private void dumpResponse(AlipayResponse response) {
        if (response != null) {
            logger.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
            if (StringUtils.isNotEmpty(response.getSubCode())) {
                logger.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(),
                        response.getSubMsg()));
            }
            logger.info("body:" + response.getBody());
        }
    }

    //alipayCallback回调业务
    public ServerResponse alipayCallback(Map<String, String> params){
        //从参数中获取订单编号
        Long orderNo = Long.valueOf(params.get("out_trade_no"));
        String tradeNo = params.get("trade_no");
        String tradeStatus = params.get("trade_status");
        //
        System.out.println("----------------------");
        System.out.println(tradeStatus);
        System.out.println("----------------------");
        //在order中查询此订单号是否有订单
        Order order = orderMapper.selectOrderByOrderNo(orderNo);
        if(order == null){
            return ServerResponse.createByErrorMessage("此订单不是快乐慕商城的订单，回调忽略");
        }
        //订单存在的话，需要看其支付状态
        if(order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()){
            //因为返回过去的值需要判断是否success
            return ServerResponse.createBySuccess("支付宝重复调用");
        }
        //判断订单的状态
        if(Const.AlipayCallback.TRADE_STATUS_TRADE_SUCCESS.equals(tradeStatus)){
            /** 这里订单的时间需要设置一下
             */
            order.setPaymentTime(DateTimeUtil.strToDate(params.get("gmt_payment")));
            //订单交易支付成功，改变订单状态
            order.setStatus(Const.OrderStatusEnum.PAID.getCode());
            //更新订单表
            orderMapper.updateByPrimaryKeySelective(order);
        }
        //付款明细
        PayInfo payInfo = new PayInfo();
        payInfo.setOrderNo(order.getOrderNo());
        payInfo.setUserId(order.getUserId());
        payInfo.setPayPlatform(Const.PayPlatFormEnum.ALIPAY.getCode());
        payInfo.setPlatformNumber(tradeNo);
        payInfo.setPlatformStatus(tradeStatus);

        payInfoMapper.insert(payInfo);

        return ServerResponse.createBySuccess();
    }

    public ServerResponse queryOrderPayStatus(Integer userId, Long orderNo){
        Order order = orderMapper.selectOrderByOrderNoUserId(orderNo, userId);
        if(order == null){
            return ServerResponse.createByErrorMessage("用户没有此订单");
        }
        //订单存在的话，需要看其支付状态
        if(order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()){
            //因为返回过去的值需要判断是否success,只需要状态成功即可
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }








    //创建订单
    public ServerResponse createOrder(Integer userId, Integer shippingId){
        //通过userId查询购物车中勾选的商品
        List<Cart> cartList = cartMapper.selectCheckedByUserId(userId);

        //计算这个订单的总价
        //获取购物车中所有的订单item
        ServerResponse<List<OrderItem>> serverResponse = getCartOrderItem(userId, cartList);
        if(!serverResponse.isSuccess()){
            //如果不是成功的，直接返回错误消息
            return serverResponse;
        }
        //计算总的价格
        List<OrderItem> orderItemList = serverResponse.getData();
        BigDecimal payment = getOrderTotalPrice(orderItemList);
        //生成订单
        Order order = this.assembleOrder(userId, shippingId, payment);
        if(order == null){
            return ServerResponse.createByErrorMessage("订单生成失败");
        }
        //判断订单明细是否为空
        if(CollectionUtils.isEmpty(orderItemList)){
            return ServerResponse.createByErrorMessage("购物车为空");
        }
        //遍历orderItemList，将所有的购物车中商品的订单号都置成当前的订单号
        for (OrderItem orderItem : orderItemList) {
            orderItem.setOrderNo(order.getOrderNo());
        }
        //mybatis 批量插入
        orderItemMapper.batchInsert(orderItemList);

        //生成成功，减少库存
        this.reduceProductStock(orderItemList);

        //将购物车中已经生成的订单中的商品清空
        this.cleanCart(cartList);

        //返回数据给前端
        OrderVo orderVo = assembleOrderVo(order,orderItemList);
        return ServerResponse.createBySuccess(orderVo);
    }

    //取消订单
    public ServerResponse<String> cancelOrder(Integer userId, Long orderNo){
        //首先通过Id查询订单是否存在
        Order order = orderMapper.selectOrderByOrderNoUserId(orderNo, userId);
        if(order == null){
            return ServerResponse.createByErrorMessage("此订单不存在");
        }
        //判断付款状态，如果已经付款，没有办法取消
        if(order.getStatus() == Const.OrderStatusEnum.PAID.getCode()){
            return ServerResponse.createByErrorMessage("当前订单已付款，无法取消");
        }
        //未付款且订单存在的话，可以更新状态为取消
        Order updateOrder = new Order();
        updateOrder.setUserId(userId);
        updateOrder.setOrderNo(orderNo);
        updateOrder.setId(order.getId());
        updateOrder.setStatus(Const.OrderStatusEnum.CANCELED.getCode());
        int rowCount = orderMapper.updateByPrimaryKeySelective(updateOrder);
        if(rowCount > 0){
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }

    public ServerResponse getOrderCartProduct(Integer userId){
        //首先需要对产品做一个封装
        OrderProductVo orderProductVo = new OrderProductVo();
        //需要通过userId查询到购物车列表
        List<Cart> cartList = cartMapper.selectCartByUserId(userId);
//        if(CollectionUtils.isEmpty(cartList)){
//            return null;
//        }
        /** 空的判断在getCartOrderItem中判断了，如果为空，输出一定不是success
         */
        //通过userId和cartList获取到具体的订单明细
        ServerResponse serverResponse = this.getCartOrderItem(userId, cartList);
        //不成功，直接返回错误消息
        if(!serverResponse.isSuccess()){
            return serverResponse;
        }
        //成功的话，取出其中的数据
        List<OrderItem> orderItemList = (List<OrderItem>)serverResponse.getData();
        List<OrderItemVo> orderItemVoList = Lists.newArrayList();

        //计算product的总价格
        BigDecimal payment = new BigDecimal("0");
        for (OrderItem orderItem : orderItemList) {
            payment = BigDecimalUtil.add(payment.doubleValue(), orderItem.getTotalPrice().doubleValue());
            orderItemVoList.add(assembleOrderItemVo(orderItem));
        }
        orderProductVo.setOrderItemVoList(orderItemVoList);
        orderProductVo.setProductTotalPrice(payment);
        orderProductVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        return ServerResponse.createBySuccess(orderProductVo);
    }

    public ServerResponse<OrderVo> getOrderDetail(Integer userId, Long orderNo){
        //获取订单详情，也就是获取orderVo
        //通过订单编号和用户名获取订单
        Order order = orderMapper.selectOrderByOrderNoUserId(orderNo, userId);
        //若订单不为空
        if(order != null){
            //需要获取到的是orderVo
            List<OrderItem> orderItemList = orderItemMapper.selectOrderItemByOrderNoUserId(orderNo,userId);
            OrderVo orderVo = assembleOrderVo(order,orderItemList);
            return ServerResponse.createBySuccess(orderVo);
        }
        return ServerResponse.createByErrorMessage("当前订单不存在");
    }

    public ServerResponse<PageInfo> getOrderList(Integer userId, int pageNum, int pageSize){
        PageHelper.startPage(pageNum,pageSize);
        //通过userId查询到订单集合
        List<Order> orderList = orderMapper.selectOrderByUserId(userId);
        List<OrderVo> orderVoList = assembleOrderVoList(orderList, userId);
        PageInfo pageResult = new PageInfo(orderList);
        pageResult.setList(orderVoList);
        return ServerResponse.createBySuccess(pageResult);
    }

    //组装orderVoList
    private List<OrderVo> assembleOrderVoList(List<Order> orderList, Integer userId){
        List<OrderVo> orderVoList = Lists.newArrayList();
        for (Order order : orderList) {
            List<OrderItem> orderItemList = Lists.newArrayList();
            //每一个order被封装成一个orderVo
            if(userId == null){
                //管理员登录
                orderItemMapper.selectOrderItemByOrderNo(order.getOrderNo());
            }else{
                orderItemList = orderItemMapper.selectOrderItemByOrderNoUserId(order.getOrderNo(),userId);
            }
            OrderVo orderVo = assembleOrderVo(order, orderItemList);
            orderVoList.add(orderVo);
        }
        return orderVoList;
    }

    //封装订单
    private OrderVo assembleOrderVo(Order order,List<OrderItem> orderItemList){
        OrderVo orderVo = new OrderVo();
        orderVo.setOrderNo(order.getOrderNo());
        orderVo.setPayment(order.getPayment());
        orderVo.setPaymentType(order.getPaymentType());
        orderVo.setPaymentTypeDesc(Const.PaymentTypeEnum.codeOf(order.getPaymentType()).getValue());
        orderVo.setPostage(order.getPostage());
        orderVo.setStatus(order.getStatus());
        orderVo.setStatusDesc(Const.OrderStatusEnum.codeOf(order.getStatus()).getValue());

        orderVo.setShippingId(order.getShippingId());
        Shipping shipping = shippingMapper.selectByPrimaryKey(order.getShippingId());
        //需要对shipping进行一个空判断
        if(shipping != null){
            orderVo.setReceiversName(shipping.getReceiverName());
            //需要获取组装的地址详细信息
            orderVo.setShippingVo(assembleShppingVo(shipping));
        }
        orderVo.setPaymentTime(DateTimeUtil.dateToStr(order.getPaymentTime()));
        orderVo.setSendTime(DateTimeUtil.dateToStr(order.getSendTime()));
        orderVo.setCreateTime(DateTimeUtil.dateToStr(order.getCreateTime()));
        orderVo.setCloseTime(DateTimeUtil.dateToStr(order.getCloseTime()));
        orderVo.setEndTime(DateTimeUtil.dateToStr(order.getEndTime()));

        orderVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        //需要获取组装的orderItemList

        List<OrderItemVo> orderItemVoList = Lists.newArrayList();
        for(OrderItem orderItem : orderItemList){
            orderItemVoList.add(assembleOrderItemVo(orderItem));
        }
        orderVo.setOrderItemVoList(orderItemVoList);
        return orderVo;
    }
    //封装地址详细信息
    private ShippingVo assembleShppingVo(Shipping shipping){
        ShippingVo shippingVo = new ShippingVo();
        shippingVo.setReceiverName(shipping.getReceiverName());
        shippingVo.setReceiverAddress(shipping.getReceiverAddress());
        shippingVo.setReceiverCity(shipping.getReceiverCity());
        shippingVo.setReceiverDistrict(shipping.getReceiverDistrict());
        shippingVo.setReceiverMobile(shipping.getReceiverMobile());
        shippingVo.setReceiverProvince(shipping.getReceiverProvince());
        shippingVo.setReceiverZip(shipping.getReceiverZip());
        shippingVo.setReceiverPhone(shipping.getReceiverPhone());

        return shippingVo;

    }


    //封装订单商品明细
    private OrderItemVo assembleOrderItemVo(OrderItem orderItem){

        OrderItemVo orderItemVo = new OrderItemVo();

        orderItemVo.setOrderNo(orderItem.getOrderNo());
        orderItemVo.setProductId(orderItem.getProductId());
        orderItemVo.setProductName(orderItem.getProductName());
        orderItemVo.setCurrentUnitPrice(orderItem.getCurrentUnitPrice());
        orderItemVo.setQuantity(orderItem.getQuantity());
        orderItemVo.setTotalPrice(orderItem.getTotalPrice());
        orderItemVo.setCreateTime(DateTimeUtil.dateToStr(orderItem.getCreateTime()));

        return orderItemVo;
    }

    private void cleanCart(List<Cart> cartList){
        for (Cart cartIem : cartList) {
            cartMapper.deleteByPrimaryKey(cartIem.getId());
        }
    }

    private void reduceProductStock( List<OrderItem> orderItemList){
        for (OrderItem orderItem : orderItemList) {
            Product product = productMapper.selectByPrimaryKey(orderItem.getProductId());
            product.setStock(product.getStock() - orderItem.getQuantity());
            //更新product表
            productMapper.updateByPrimaryKeySelective(product);
        }

    }

    private Order assembleOrder(Integer userId, Integer shippingId,BigDecimal payment){
        Order order = new Order();
        //获取订单编号
        Long orderNo = this.generateOrderNo();
        //设置订单
        order.setOrderNo(orderNo);
        order.setStatus(Const.OrderStatusEnum.NO_PAY.getCode());
        order.setPaymentType(Const.PaymentTypeEnum.ONOLINE_PAY.getCode());
        order.setPostage(0);
        order.setPayment(payment);

        order.setUserId(userId);
        order.setShippingId(shippingId);
        //发货时间
        //订单支付时间,后续添上
        //插入order
        int rowCount = orderMapper.insert(order);
        if(rowCount > 0){
            return order;
        }
        return null;
    }

    private Long generateOrderNo(){
        Long currentTime = System.currentTimeMillis();
        return currentTime+currentTime%10;
    }

    private BigDecimal getOrderTotalPrice(List<OrderItem> orderItemList){
        //初始化
        BigDecimal payment = new BigDecimal("0");
        //因为orderItem包含许多的product所以使用TotalPrice
        for (OrderItem orderItem : orderItemList) {
            payment = BigDecimalUtil.add(payment.doubleValue(),orderItem.getTotalPrice().doubleValue());
        }
        return payment;
    }

    private ServerResponse<List<OrderItem>> getCartOrderItem(Integer userId,List<Cart> cartList){
        List<OrderItem> orderItemList = Lists.newArrayList();
        //判断购物车列表是否为空
        if(CollectionUtils.isEmpty(cartList)){
            return ServerResponse.createByErrorMessage("当前购物车为空");
        }
        //校验购物车中产品的状态和数量
        for (Cart cartItem : cartList) {
            OrderItem orderItem = new OrderItem();
            Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());
            if(product.getStatus() != Const.ProductStatusEnum.ON_SALE.getCode()){
                return ServerResponse.createByErrorMessage("产品"+product.getName()+"不在售卖状态");
            }
            //在售卖状态的话 先校验库存
            if(cartItem.getQuantity() > product.getStock()){
                return ServerResponse.createByErrorMessage("产品"+product.getName()+"库存不足");
            }
            //库存充足的话
            orderItem.setUserId(userId);
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setCurrentUnitPrice(product.getPrice());
            //这个是购物车中的产品数量
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(),cartItem.getQuantity()));

            orderItemList.add(orderItem);
        }
        return ServerResponse.createBySuccess(orderItemList);
    }



    //backend
    public ServerResponse<PageInfo> getManageList(Long orderNo, int pageNum, int pageSize){
        //分页
        PageHelper.startPage(pageNum, pageSize);
        //查询所有的订单
        List<Order> orderList = orderMapper.selectAll();
        List<OrderVo> orderVoList = assembleOrderVoList(orderList, null);
        PageInfo pageResult = new PageInfo(orderList);
        pageResult.setList(orderVoList);
        return ServerResponse.createBySuccess(pageResult);
    }

    public ServerResponse<OrderVo> getManageDetail(Long orderNo){
        //管理权限判定之后，可以直接使用orderNo查询某一个订单
        Order order = orderMapper.selectOrderByOrderNo(orderNo);
        if(order != null){
            List<OrderItem> orderItemList = orderItemMapper.selectOrderItemByOrderNo(orderNo);
            //直接组装出一个orderList
            OrderVo orderVo = assembleOrderVo(order, orderItemList);
            return ServerResponse.createBySuccess(orderVo);
        }
        return ServerResponse.createByErrorMessage("此订单不存在");
    }

    public ServerResponse<PageInfo> getManageSearch(Long orderNo,int pageNum, int pageSize){
        PageHelper.startPage(pageNum,pageSize);
        Order order = orderMapper.selectOrderByOrderNo(orderNo);
        if(order != null){
            List<OrderItem> orderItemList = orderItemMapper.selectOrderItemByOrderNo(orderNo);
            //直接组装出一个orderList
            OrderVo orderVo = assembleOrderVo(order, orderItemList);
            PageInfo pageResult = new PageInfo(Lists.newArrayList(orderItemList));
            pageResult.setList(Lists.newArrayList(orderVo));
            return ServerResponse.createBySuccess(pageResult);
        }
        return ServerResponse.createByErrorMessage("订单不存在");
    }

    public ServerResponse manageSendGoods(Long orderNo){
        Order order = orderMapper.selectOrderByOrderNo(orderNo);
        if(order != null){
            if(order.getStatus() == Const.OrderStatusEnum.PAID.getCode()){
                order.setStatus(Const.OrderStatusEnum.SHIPPED.getCode());
                order.setSendTime(new Date());
                orderMapper.updateByPrimaryKeySelective(order);
                return ServerResponse.createBySuccessMessage("发货成功");
            }
        }
        return ServerResponse.createByErrorMessage("订单不存在");
    }







}
