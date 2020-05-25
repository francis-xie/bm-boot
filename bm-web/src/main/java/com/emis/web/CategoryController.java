package com.emis.web;

import java.util.List;

import com.emis.mapper.CategoryMapper;
import com.emis.pojo.Category;
import com.emis.service.CategoryService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 分类管理Controller
 */
//@Api：用于修饰Controller类，生成Controller相关文档信息
@Api(tags = "CategoryController", description = "商品分类管理")
@Controller
public class CategoryController {
    //categoryMapper方式获取转为categoryService方式获取以便使用Redis缓存
    //@Autowired CategoryMapper categoryMapper;
    //自动装配对象
    @Autowired
    private CategoryService categoryService;

    /**
     * 1. 接受listCategory映射
     * 2. 然后获取所有的分类数据
     * 3. 接着放入Model中
     * 4. 跳转到listCategory.jsp中
     *
     * @param m
     * @return
     * @throws Exception
     */
    @ApiOperation("获取所有分类列表")
    @RequestMapping(value = "/listCategory", method = RequestMethod.GET)
    public String listCategory(Model m) throws Exception {
        List<Category> cs = categoryService.findAll();
        m.addAttribute("cs", cs);

        return "listCategory";
    }

    /**
     * 1. 在参数里接受当前是第几页 start ，以及每页显示多少条数据 size。 默认值分别是0和5。
     *
     * @param m
     * @param start
     * @param size
     * @return
     * @throws Exception
     */
    //@ApiOperation：用于修饰Controller类中的方法，生成接口方法相关文档信息
    //@ApiParam：用于修饰接口中的参数，生成接口参数相关文档信息
    @ApiOperation("分页查询分类列表")
    @RequestMapping(value = "/listCategoryPage", method = RequestMethod.GET)
    public String listCategoryPage(Model m, @RequestParam(value = "start", defaultValue = "0") @ApiParam("页码") int start, @RequestParam
            (value = "size", defaultValue = "2") @ApiParam("每页数量") int size) throws Exception {
        //2. 根据start,size进行分页，并且设置id 倒排序
        //PageHelper.startPage(start, size, "id desc");
        //3. 因为PageHelper的作用，这里就会返回当前分页的集合了
        //List<Category> cs = categoryMapper.findAll();
        //4. 根据返回的集合，创建PageInfo对象
        //PageInfo<Category> page = new PageInfo<>(cs);
        PageInfo<Category> page = categoryService.findAll(start, size);
        //5. 把PageInfo对象扔进model，以供后续显示
        m.addAttribute("page", page);
        //6. 跳转到listCategory.jsp
        return "listCategoryPage";
    }

    @ApiOperation("添加分类")
    @RequestMapping(value = "/addCategory", method = RequestMethod.POST)
    public String addCategory(Category c) throws Exception {
        categoryService.save(c);
        return "redirect:listCategoryPage";
    }

    @ApiOperation("删除指定id的分类")
    @RequestMapping(value = "/deleteCategory", method = RequestMethod.GET)
    public String deleteCategory(Category c) throws Exception {
        categoryService.delete(c.getId());
        return "redirect:listCategoryPage";
    }

    @ApiOperation("更新指定id分类信息")
    @RequestMapping(value = "/updateCategory", method = RequestMethod.POST)
    public String updateCategory(Category c) throws Exception {
        categoryService.update(c);
        return "redirect:listCategoryPage";
    }

    @ApiOperation("获取指定id的分类详情")
    @RequestMapping(value = "/editCategory", method = RequestMethod.GET)
    public String editCategory(int id, Model m) throws Exception {
        Category c = categoryService.get(id);
        m.addAttribute("c", c);
        return "editCategory";
    }
}
