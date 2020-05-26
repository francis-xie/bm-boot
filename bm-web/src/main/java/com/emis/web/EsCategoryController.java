package com.emis.web;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

import com.emis.dao.CategoryDAO;
import com.emis.pojo.Category;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class EsCategoryController {
    @Autowired
    CategoryDAO categoryDAO;

    //每页数量
    @GetMapping("/esListCategory")
    public String listCategory(Model m, @RequestParam(value = "start", defaultValue = "0") int start, @RequestParam(value = "size", defaultValue = "5") int size) {
        String query = "商品"; //查询条件，但是并未使用，放在这里，为的是将来使用，方便参考，知道如何用
        //SearchQuery searchQuery = getEntitySearchQuery(start, size, query);
        //Page<Category> page = categoryDAO.search(searchQuery);
        // 设置分页
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        //Pageable pageable = new PageRequest(start, size, sort);
        Pageable pageable = PageRequest.of(start, size);
        Page<Category> page = categoryDAO.search(QueryBuilders.matchAllQuery(), pageable);
        m.addAttribute("page", page);
        return "esListCategory";
    }

    /*private SearchQuery getEntitySearchQuery(int start, int size, String searchContent) {
        FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders.functionScoreQuery()
                .add(QueryBuilders.matchAllQuery(), //查询所有
                        ScoreFunctionBuilders.weightFactorFunction(100))

//                查询条件，但是并未使用，放在这里，为的是将来使用，方便参考，知道如何用
//                .add(QueryBuilders.matchPhraseQuery("name", searchContent),
//                		ScoreFunctionBuilders.weightFactorFunction(100))
                //设置权重分 求和模式
                .scoreMode("sum")
                //设置权重分最低分
                .setMinScore(10);

        // 设置分页
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable = new PageRequest(start, size, sort);
        return new NativeSearchQueryBuilder()
                .withPageable(pageable)
                .withQuery(functionScoreQueryBuilder).build();
    }*/

    @RequestMapping("/esAddCategory")
    public String addCategory(Category c) throws Exception {
        int id = currentTime();
        c.setId(id);
        categoryDAO.save(c);
        return "redirect:esListCategory";
    }

    private int currentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMddHHmmss");
        String time = sdf.format(new Date());
        return Integer.parseInt(time);
    }

    @RequestMapping("/esDeleteCategory")
    public String deleteCategory(Category c) throws Exception {
        categoryDAO.delete(c);
        return "redirect:esListCategory";
    }

    @RequestMapping("/esUpdateCategory")
    public String updateCategory(Category c) throws Exception {
        categoryDAO.save(c);
        return "redirect:esListCategory";
    }

    @RequestMapping("/esEditCategory")
    public String ediitCategory(int id, Model m) throws Exception {
        //Category c = categoryDAO.findOne(id);
        Optional<Category> c = categoryDAO.findById(id);
        m.addAttribute("c", c.get());
        return "esEditCategory";
    }
}
