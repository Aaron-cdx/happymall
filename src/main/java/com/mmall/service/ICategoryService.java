package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Category;

import java.util.List;

/**
 * @author caoduanxi
 * @2019/5/7 10:03
 */
public interface ICategoryService {
    ServerResponse addCategory(Integer parentId, String categoryName);

    ServerResponse<String> updateCategoryName(Integer categoryId, String categoryName);

    ServerResponse<List<Category>> selectChildrenParallelCategory(Integer parentId);

    ServerResponse<List<Integer>> selectCategoryAndChildrenCategoryById(Integer categoryId);

}
