package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.pojo.Category;
import com.mmall.service.ICategoryService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author caoduanxi
 * @2019/5/7 10:03
 */
@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService {
    @Autowired
    private CategoryMapper categoryMapper;

    //增加品类操作 parentId和品类名称
    public ServerResponse addCategory(Integer parentId, String categoryName){
        //首先查询parentId是否存在
        if(parentId == null || StringUtils.isBlank(categoryName)){
            return ServerResponse.createByErrorMessage("添加品类参数错误");
        }
        //创建一个新的品类
        Category category = new Category();
        category.setName(categoryName);
        category.setParentId(parentId);//父类的id
        category.setStatus(true);//这个品类是可用的
        int rowCount = categoryMapper.insert(category);
        if(rowCount > 0){
            return ServerResponse.createBySuccessMessage("添加品类成功");
        }
        return ServerResponse.createByErrorMessage("添加品类失败");
    }

    //修改品类名称
    public ServerResponse<String> updateCategoryName(Integer categoryId, String categoryName){
        if(categoryId == null || StringUtils.isBlank(categoryName)){
            return ServerResponse.createByErrorMessage("添加品类参数错误");
        }
        //通过id查询到当前对象
//        Category category = categoryMapper.selectByPrimaryKey(categoryId);
//        category.setName(categoryName);
        Category category = new Category();
        category.setId(categoryId);
        category.setName(categoryName);
        int rowCount = categoryMapper.updateByPrimaryKeySelective(category);
        if(rowCount > 0){
            return ServerResponse.createBySuccessMessage("更新品类名称成功");
        }else{
            return ServerResponse.createByErrorMessage("更新品类名称失败");
        }
    }

    public ServerResponse<List<Category>> selectChildrenParallelCategory(Integer parentId){
        //否则查询当前
        List<Category> categoryList = categoryMapper.selectChildrenParallelCategory(parentId);
        if(CollectionUtils.isEmpty(categoryList)){
            return ServerResponse.createByErrorMessage("未找到当前分类的子分类");
        }
        return ServerResponse.createBySuccess(categoryList);
    }

    public ServerResponse<List<Integer>> selectCategoryAndChildrenCategoryById(Integer categoryId){
        Set<Category> categorySet = Sets.newHashSet();
        findChildrenCategory(categorySet, categoryId);
        //两个都是Guaua的数据结构
        List<Integer> categoryList = Lists.newArrayList();
        if(categoryId != null){
            for(Category categoryItem : categorySet){
                categoryList.add(categoryItem.getId());
            }
        }
        return ServerResponse.createBySuccess(categoryList);
    }

    private Set<Category> findChildrenCategory(Set<Category> categorySet,int categoryId){
        //通过categoryId查找所有的对象，然后通过递归categoryId获取到所有的子对象
        Category category = categoryMapper.selectByPrimaryKey(categoryId);
        if (category != null) {
            categorySet.add(category);
        }
        //遍历获得id 这里就是终止条件
        List<Category> categoryList = categoryMapper.selectChildrenParallelCategory(categoryId);
        //因为如果这个为空也不会报错，所以使用遍历的方法，一旦为空就退出循环
        for(Category categoryItem : categoryList){
            findChildrenCategory(categorySet, categoryItem.getId());
        }
        return categorySet;
    }
}
