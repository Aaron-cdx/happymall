package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.vo.CartVo;

/**
 * @author caoduanxi
 * @2019/5/10 11:11
 */
public interface ICartService {
    ServerResponse<CartVo> add(Integer userId, Integer count, Integer productId);

    ServerResponse<CartVo> update(Integer userId, Integer count, Integer productId);

    ServerResponse<CartVo> delete(Integer userId, String productIds);

    ServerResponse<CartVo> list(Integer userId);

    ServerResponse<CartVo> selectOneOrUnSelectOne(Integer userId,Integer checked, Integer productId);

    ServerResponse<CartVo> selectAllOrUnSelect(Integer userId,Integer checked);

    ServerResponse<Integer> getCartProductCount(Integer userId);
}
