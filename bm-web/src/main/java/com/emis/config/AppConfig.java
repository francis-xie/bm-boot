package com.emis.config;

import com.emis.webservices.service.bm.syndata.emisBMSynDataImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean(name = "syndata_checkLogin")
    public emisBMSynDataImpl helloWorld() {
        return new emisBMSynDataImpl();
    }

}

