package com.emis.pojo;

import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.elasticsearch.annotations.Document;

//@Document就表明了要连接到 ElasticSearch 的哪个索引和哪个 type 上
// indexName索引名称 相当于就是数据库 必须为小写 不然会报异常，type 相当于就是表
@Document(indexName = "how2java", type = "category")
public class Category {

    //@ApiModelProperty：用于修饰实体类的属性，当实体类是请求参数或返回结果时，直接生成相关文档信息
    @ApiModelProperty(value = "主键")
    private int id;

    @ApiModelProperty(value = "分类名")
    private String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
