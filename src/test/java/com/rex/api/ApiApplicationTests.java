package com.rex.api;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.http.HttpConnection;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.rex.api.util.HttpUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.yaml.snakeyaml.util.UriEncoder;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
class ApiApplicationTests {

    @Test
    void contextLoads() {
        String urlString = "http://localhost:8098/publicFiles/v1/uploadTemplateInputStream";
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("fileName", "北京");
        StringBuilder stringBuffer = new StringBuilder();
        for (int i = 0; i < 100000; i++) {
            stringBuffer.append("淡淡的点点滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴多多多多多多多多多多多多多多多多多多多多多多");
            stringBuffer.append("淡淡的点点滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴多多多多多多多多多多多多多多多多多多多多多多");
            stringBuffer.append("淡淡的点点滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴多多多多多多多多多多多多多多多多多多多多多多");
            stringBuffer.append("淡淡的点点滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴多多多多多多多多多多多多多多多多多多多多多多");
            stringBuffer.append("淡淡的点点滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴多多多多多多多多多多多多多多多多多多多多多多");
            stringBuffer.append("淡淡的点点滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴多多多多多多多多多多多多多多多多多多多多多多");
            stringBuffer.append("淡淡的点点滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴多多多多多多多多多多多多多多多多多多多多多多");
            stringBuffer.append("淡淡的点点滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴多多多多多多多多多多多多多多多多多多多多多多\n");
        }
        InputStream inputStream = IoUtil.toStream(stringBuffer.toString(), StandardCharsets.UTF_8);


        HttpUtil.post(urlString, paramMap);

    }

    @Test
    void DateUtil() {
        System.out.println(DateUtil.format(new Date(), "yyyyMMdd"));
    }

    @Test
    void StringTest() throws IOException {
        String urlString = "http://localhost:8098/publicFiles/v1/uploadTemplateInputStream?fileName=";
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("fileName", "北京");
        StringBuilder stringBuffer = new StringBuilder();
        for (int i = 0; i < 100000; i++) {
            stringBuffer.append("淡淡的点点滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴多多多多多多多多多多多多多多多多多多多多多多");
            stringBuffer.append("淡淡的点点滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴多多多多多多多多多多多多多多多多多多多多多多");
            stringBuffer.append("淡淡的点点滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴多多多多多多多多多多多多多多多多多多多多多多");
            stringBuffer.append("淡淡的点点滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴多多多多多多多多多多多多多多多多多多多多多多");
            stringBuffer.append("淡淡的点点滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴多多多多多多多多多多多多多多多多多多多多多多");
            stringBuffer.append("淡淡的点点滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴多多多多多多多多多多多多多多多多多多多多多多");
            stringBuffer.append("淡淡的点点滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴多多多多多多多多多多多多多多多多多多多多多多");
            stringBuffer.append("淡淡的点点滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴滴多多多多多多多多多多多多多多多多多多多多多多\n");
        }
        String encode = UriEncoder.encode("北京");
        InputStream inputStream = IoUtil.toStream(stringBuffer.toString(), StandardCharsets.UTF_8);
        System.out.println(HttpUtils.doPost(urlString + encode, inputStream));
    }
}
