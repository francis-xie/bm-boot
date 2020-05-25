package com.emis.dao;

import com.emis.pojo.Category;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface CategoryDAO extends ElasticsearchRepository<Category, Integer> {

}
