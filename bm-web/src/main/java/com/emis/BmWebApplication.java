package com.emis;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
//开启redis缓存
@EnableCaching
@MapperScan({"com.emis.security.dao", "com.emis.mapper"})
public class BmWebApplication {

    public static void main(String[] args) {
        System.setProperty("es.set.netty.runtime.available.processors", "false");
        SpringApplication.run(BmWebApplication.class, args);
    }

}
