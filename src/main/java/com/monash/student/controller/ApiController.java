package com.monash.student.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController // 注意这里必须是 @RestController，因为它要返回 JSON 数据给前端，而不是返回网页
public class ApiController {
    private static final String WAQI_TOKEN = "e9fba4246eaf38b72b8036cbf752ab38b076b46d";

    // 这个路径必须和前端 fetch 请求的路径完全一致
    @GetMapping("/getApi")
    public ResponseEntity<Map<String, Object>> getAqiByLocation(@RequestParam("location") String location) {


        Map<String, Object> response = new HashMap<>();

        if (location.contains("bad")){
            response.put("aqi", "120");
            response.put("stationName", "");

            return ResponseEntity.ok(response);
        }
        try {
            // 1. 动态构建 WAQI 请求 URL
            // 注意：Spring Boot 的 RestTemplate 会自动处理 URL 里的特殊字符编码
            String apiUrl = "https://api.waqi.info/feed/" + location + "/?token=" + WAQI_TOKEN;
            System.out.println("正在请求 WAQI API: " + apiUrl);

            // 2. 发起 HTTP GET 请求
            RestTemplate restTemplate = new RestTemplate();
            // JsonNode 是 Jackson 库非常强大的工具，可以直接把嵌套的 JSON 像树一样解析
            JsonNode rootNode = restTemplate.getForObject(apiUrl, JsonNode.class);

            // 3. 校验官方返回的数据状态 (WAQI 成功时 status 会返回 "ok")
            if (rootNode != null && "ok".equals(rootNode.path("status").asText())) {

                JsonNode dataNode = rootNode.path("data");

                // ⚠️ 核心容错处理：当某个监测站临时停机检修时，WAQI 返回的 aqi 会是字符串 "-"
                // 我们必须判断它是不是一个真实的数字，否则 Java 直接转换 int 会报错崩溃
                int aqiValue;
                JsonNode aqiNode = dataNode.path("aqi");
                if (aqiNode.isInt()) {
                    aqiValue = aqiNode.asInt();
                } else {
                    System.out.println("警告：该地区监测站数据暂时不可用，默认返回 50");
                    aqiValue = 50; // 兜底安全值
                }

                // (可选进阶功能) 提取真实的监测站名字
                // 如果你用 GPS 定位，它会返回距离你最近的那个气象站的具体名字 (比如 "Shah Alam, Selangor")
                String stationName = dataNode.path("city").path("name").asText();

                System.out.println("获取成功！" + stationName + " 的实时 API 为: " + aqiValue);

                // 4. 将提取出的干净数据返回给前端的 dashboard.html
                response.put("aqi", aqiValue);
                response.put("stationName", stationName);

                return ResponseEntity.ok(response);

            } else {
                // 处理找不到城市的情况（比如用户瞎敲了一串乱码）
                String errorMsg = rootNode != null ? rootNode.path("data").asText() : "Unknown API Error";
                System.err.println("WAQI 查询失败，原因: " + errorMsg);

                response.put("error", "Location not found");
                response.put("aqi", 50); // 兜底：防止前端红屏
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

        } catch (Exception e) {
            // 处理网络断开、WAQI 服务器宕机等严重异常
            System.err.println("服务器内部/网络异常: " + e.getMessage());

            response.put("error", "Internal Server Error");
            response.put("aqi", 50); // 兜底
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}

