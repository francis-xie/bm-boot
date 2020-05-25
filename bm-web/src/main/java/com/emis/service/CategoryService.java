package com.emis.service;

import com.emis.mapper.CategoryMapper;
import com.emis.pojo.Category;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
//Redis的缓存，通常都会在 Service 这一层上面做
//给分类加上如下注解，就表示分类在缓存里的keys，都是归 "categories" 这个管理的。
//通过工具Redis 图形界面客户端 可以看到有一个 categories~keys，用于维护分类信息在 redis里都有哪些 key
@CacheConfig(cacheNames = "categories")
public class CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;

    @Cacheable(key = "'categories-all'")
    public List<Category> findAll() {
        return categoryMapper.findAll();
    }

    //删除 categories~keys 里的所有的keys，即，一旦增加了某个分类数据，那么就把缓存里所有分类相关的数据，都清除掉。
    //下一次再访问的时候，一看，缓存里没数据，那么就会从数据库中读出来，读出来之后，再放在缓存里。
    @CacheEvict(allEntries = true)
    //@CachePut(key="'category-one-'+ #p0")
    public void save(Category bean) {
        categoryMapper.save(bean);
    }

    @CacheEvict(allEntries = true)
    //@CacheEvict(key="'category-one-'+ #p0")
    public void delete(int id) {
        categoryMapper.delete(id);
    }

    //获取一个缓存，第一次访问的时候， redis 是不会有数据的，所以就会通过 mybatis 到数据库里去取出来，
    //一旦取出来之后，就会放在 redis里。 key 呢就是 categories-one-? 这个 key。
    //第二次访问的时候，redis 就有数据了，就不会从数据库里获取了。
    @Cacheable(key = "'categories-one-'+ #p0")
    public Category get(int id) {
        return categoryMapper.get(id);
    }

    @CacheEvict(allEntries = true)
    //@CachePut(key="'category-one-'+ #p0")
    public void update(Category bean) {
        categoryMapper.update(bean);
    }

    //分页查询缓存，数据不再是一个对象，而是一个集合。 （保存在 redis 里是一个 json 数组）
    //如categories-page-0-5 就是第一页数据
    @Cacheable(key = "'categories-page-'+#p0+ '-' + #p1")
    public PageInfo<Category> findAll(int start, int size) {
        start = start < 0 ? 0 : start;
        //根据start,size进行分页，并且设置id 倒排序
        PageHelper.startPage(start, size, "id desc");
        //因为PageHelper的作用，这里就会返回当前分页的集合了
        List<Category> cs = findAll();
        //根据返回的集合，创建PageInfo对象
        PageInfo<Category> pageInfo = new PageInfo<>(cs);
        return pageInfo;
    }
}
