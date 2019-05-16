package com.mmall.service.impl;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CartMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Cart;
import com.mmall.pojo.Product;
import com.mmall.service.ICartService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.CartProductVo;
import com.mmall.vo.CartVo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author caoduanxi
 * @2019/5/10 11:12
 */
@Service("iCartService")
public class CartServiceImpl implements ICartService {

    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ProductMapper productMapper;


    /*
    购物车添加商品，计算商品的价格以及商品购买的数量与库存之间的交互
     */
    public ServerResponse<CartVo> add(Integer userId, Integer count, Integer productId){
        //判断一个参数的逻辑
        if(productId == null || count == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        //查询当前商品是否在购物车中
        Cart cart = cartMapper.selectCartByUserIdProductId(userId, productId);
        if(cart == null){
            //则当前产品不在购物车中，需要创建此商品，然后加入
            Cart cartItem = new Cart();
            cartItem.setQuantity(count);
            cartItem.setChecked(Const.Cart.CHECKED);
            cartItem.setUserId(userId);
            cartItem.setProductId(productId);
            //插入此商品
            cartMapper.insert(cartItem);
        }else{
            //商品存在购物车，更新购物车的状态
            count = cart.getQuantity() + count;
            cart.setQuantity(count);
            cartMapper.updateByPrimaryKeySelective(cart);
        }
        return this.list(userId);
    }

    //更新商品
    public ServerResponse<CartVo> update(Integer userId, Integer count, Integer productId){
        //判断一个参数的逻辑
        if(productId == null || count == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        //更新商品，商品肯定是在这个购物车中的，只是对数量进行一个更新
        //在数据库中查询当前商品
        Cart cart = cartMapper.selectCartByUserIdProductId(userId,productId);
        cart.setQuantity(count);
        cartMapper.updateByPrimaryKeySelective(cart);
        //更新购物车
        return this.list(userId);
    }

    //删除商品
    public ServerResponse<CartVo> delete(Integer userId, String productIds){
        //将Id转换成List集合形式
        List<String> productIdList = Splitter.on(",").splitToList(productIds);
        cartMapper.deleteProductByProductIds(userId,productIdList);
        return this.list(userId);
    }
    //商品列表
    public ServerResponse<CartVo> list(Integer userId){
        CartVo cartVoLimit = this.getCartVoLimit(userId);
        return ServerResponse.createBySuccess(cartVoLimit);
    }


    //单选或者单反选
    public ServerResponse<CartVo> selectOneOrUnSelectOne(Integer userId,Integer checked, Integer productId){
        //这是将所有的cart中的商品check置成1
        int rowCount = cartMapper.updateCartProductSelectChecked(userId, checked, productId);
        if(rowCount == 0){
            System.out.println("出问题了");
        }
        return this.list(userId);
    }

    //全选或者全反选
    public ServerResponse<CartVo> selectAllOrUnSelect(Integer userId,Integer checked){
        //这是将所有的cart中的商品check置成1
        cartMapper.updateCartProductSelectAll(userId, checked);

        return this.list(userId);
    }


    //获取购物车中的商品的数量
    public ServerResponse<Integer> getCartProductCount(Integer userId){
        if(userId == null){
            return ServerResponse.createBySuccess(0);
        }
        int resultCount = cartMapper.selectCartProductCountByUserId(userId);
        return ServerResponse.createBySuccess(resultCount);
    }


    //主要是针对于商品的数量和库存对商品做一个限制
    private CartVo getCartVoLimit(Integer userId){
        CartVo cartVo = new CartVo();
        //获取购物车的列表，购物车中包含产品，所以需要对产品进行一个封装
        List<Cart> cartList = cartMapper.selectCartByUserId(userId);
        //对购物车产品的封装
        List<CartProductVo> cartProductVoList = Lists.newArrayList();
        //购物车总的价格，需要满足精度，所以使用String形式的数来计算
        BigDecimal cartTotalPrice = new BigDecimal("0");
        //对所有的cartList进行遍历
        if(CollectionUtils.isNotEmpty(cartList)){
            for(Cart cartItem : cartList){
                CartProductVo cartProductVo = new CartProductVo();
                cartProductVo.setId(cartItem.getId());
                //当前购物车的用户名!
                cartProductVo.setUserId(userId);
                cartProductVo.setProductId(cartItem.getProductId());
                //quantity在后面设置，因为涉及到库存的问题

                Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());
                if(product != null){
                    //继续对产品进行一个封装
                    cartProductVo.setProductMainImage(product.getMainImage());
                    cartProductVo.setProductName(product.getName());
                    cartProductVo.setProductPrice(product.getPrice());
                    cartProductVo.setProductStatus(product.getStatus());
                    cartProductVo.setProductSubtitle(product.getSubtitle());
                    cartProductVo.setProductStock(product.getStock());

                    //对产品库存进行一个判定
                    int buyLimitCount = 0;
                    if(product.getStock() >= cartItem.getQuantity()){
                        //库存充足
                        buyLimitCount = cartItem.getQuantity();
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_SUCCESS);
                    }else{
                        buyLimitCount = product.getStock();
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_FAIL);
                        //更新购物车中的产品的有效库存
                        Cart cartForQuantity = new Cart();
                        //需要传入主键id
                        cartForQuantity.setId(cartItem.getId());
                        cartForQuantity.setQuantity(buyLimitCount);
                        //更新当前产品的库存
                        cartMapper.updateByPrimaryKeySelective(cartForQuantity);
                    }
                    cartProductVo.setQuantity(buyLimitCount);
                    //计算总的价格
                    cartProductVo.setProductTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(),cartProductVo.getQuantity()));
                    //是否被选中
                    cartProductVo.setProductChecked(cartItem.getChecked());
                }
                //计算总的价格
                if(cartItem.getChecked() == Const.Cart.CHECKED){
                    //当前价格与获取的当前产品总的价格相加
                    cartTotalPrice = BigDecimalUtil.add(cartTotalPrice.doubleValue(),cartProductVo.getProductTotalPrice().doubleValue());
                }
                cartProductVoList.add(cartProductVo);
            }
        }
        cartVo.setCartProductVoList(cartProductVoList);
        cartVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        //购物车总的价格
        cartVo.setCartTotalPrice(cartTotalPrice);
        cartVo.setAllChecked(this.getAllChecked(userId));
        return cartVo;
    }

    private boolean getAllChecked(Integer userId){
        if(userId == null){
            return false;
        }
        //等于0为全选，否则没有全选
        return cartMapper.selectCartProductStatusCheckedByUserId(userId) == 0;
    }
}
