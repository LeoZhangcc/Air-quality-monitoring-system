package com.monash.student.entity;

import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class HealthAdvisorService {

    // 模拟获取 Shah Alam 的实时 AQI 数据（后续可替换为真实的 WAQI API 调用）
    public int getCurrentAqi() {
        // 为了方便你测试 5 秒报警功能，你可以手动修改这个数字
        // 设为 105 测试报警，设为 55 测试正常状态
        return 105;
    }

    // Epic 1.2 & 2.1 & 2.2: 根据 AQI 生成交通灯颜色、口罩建议和行动指南
    public Map<String, Object> getHealthAdvice(int aqi) {
        Map<String, Object> advice = new HashMap<>();
        advice.put("aqi", aqi);

        if (aqi <= 50) {
            advice.put("color", "bg-green-500"); // 绿灯
            advice.put("status", "Good");
            advice.put("mask", "Surgical Mask (Optional)");
            advice.put("actions", Arrays.asList("Enjoy your outdoor activities safely."));
            advice.put("isAlert", false);
        } else if (aqi <= 100) {
            advice.put("color", "bg-yellow-400"); // 黄灯
            advice.put("status", "Moderate");
            advice.put("mask", "Surgical Mask");
            advice.put("actions", Arrays.asList("Safe for short walks.", "Monitor your breathing."));
            advice.put("isAlert", false);
        } else {
            advice.put("color", "bg-red-600"); // 红灯
            advice.put("status", "Unhealthy");
            advice.put("mask", "N95 Mask Required"); // US 2.1: Mask Expert
            // US 2.2: 5 Quick Actions for respiratory care
            advice.put("actions", Arrays.asList(
                    "1. Stay indoors immediately.",
                    "2. Keep all doors and windows closed.",
                    "3. Avoid any heavy exertion.",
                    "4. Hydrate frequently with warm water.",
                    "5. Turn on air purifier if available."
            ));
            advice.put("isAlert", true); // 触发 5 秒警报的核心标志
        }
        return advice;
    }

    // Epic 3.1: 5 种抗炎润肺食谱
    public List<Map<String, String>> getRecipes() {
        return Arrays.asList(
                ImmutableMap.of("name", "Snow Fungus & Pear Soup", "benefit", "Moistens lungs and soothes throat micro-inflammation."),
                ImmutableMap.of("name", "Barley & Winter Melon Water", "benefit", "Flushes toxins and balances body hydration."),
                ImmutableMap.of("name", "Chrysanthemum & Goji Tea", "benefit", "Fights oxidative stress caused by PM2.5."),
                ImmutableMap.of("name", "Turmeric & Ginger Warm Drink", "benefit", "Strong natural anti-inflammatory for respiratory tract."),
                ImmutableMap.of("name", "Warm Honey Lemon Water", "benefit", "Natural antibacterial, boosts immunity quickly.")
        );
    }

    // Epic 3.2: 3 种室内低冲击运动
    public List<Map<String, String>> getExercises() {
        return Arrays.asList(
                ImmutableMap.of("name", "Seated Tai Chi (Cloud Hands)", "guide", "Sit upright. Move hands slowly in circles coordinating with deep breaths. Expands lung capacity safely."),
                ImmutableMap.of("name", "Wall Chest Stretch", "guide", "Stand sideways to a wall, place forearm on wall, turn body gently. Opens up a compressed chest."),
                ImmutableMap.of("name", "4-7-8 Diaphragmatic Breathing", "guide", "Inhale 4s, hold 7s, exhale slowly 8s. Strengthens diaphragm and reduces indoor anxiety.")
        );
    }
}
