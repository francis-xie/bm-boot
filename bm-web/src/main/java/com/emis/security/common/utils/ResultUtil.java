package com.emis.security.common.utils;

import com.alibaba.fastjson.JSON;
import com.emis.security.common.response.ServerResponse;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletResponse;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 返回封装
 * @Author: LiuRunYong
 * @Date: 2020/4/28
 **/
@Slf4j
public class ResultUtil {
    /**
     * 私有化构造器
     */
    private ResultUtil() {
    }


    /**
     * 返回成功示例
     */
    public static ServerResponse resultSuccess() {
        return ServerResponse.createBySuccessMessage("success");
    }

    /**
     * 返回失败示例
     */
    public static ServerResponse resultError(Map<String, Object> resultMap) {
        return ServerResponse.createByErrorMessage("操作失败");
    }

    /**
     * 通用示例
     */
    public static Map<String, Object> resultCode(Integer code, String msg) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("message", msg);
        resultMap.put("code", code);
        return resultMap;
    }

}

