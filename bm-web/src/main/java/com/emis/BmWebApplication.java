package com.emis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
//开启redis缓存
@EnableCaching
public class BmWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(BmWebApplication.class, args);
	}

}
