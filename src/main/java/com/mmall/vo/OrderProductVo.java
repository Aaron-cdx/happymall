package com.mmall.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author caoduanxi
 * @2019/5/15 13:49
 */
public class OrderProductVo {
    //商品细节的列表
    private List<OrderItemVo> orderItemVoList;
    //产品总的价格
    private BigDecimal productTotalPrice;
    //产品的头图
    private String imageHost;

    public List<OrderItemVo> getOrderItemVoList() {
        return orderItemVoList;
    }

    public void setOrderItemVoList(List<OrderItemVo> orderItemVoList) {
        this.orderItemVoList = orderItemVoList;
    }

    public BigDecimal getProductTotalPrice() {
        return productTotalPrice;
    }

    public void setProductTotalPrice(BigDecimal productTotalPrice) {
        this.productTotalPrice = productTotalPrice;
    }

    public String getImageHost() {
        return imageHost;
    }

    public void setImageHost(String imageHost) {
        this.imageHost = imageHost;
    }
}
