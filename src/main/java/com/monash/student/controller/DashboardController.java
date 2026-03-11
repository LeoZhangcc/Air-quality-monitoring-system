package com.monash.student.controller;

import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;
import java.util.Arrays;

@Controller
public class DashboardController {

    @GetMapping("/")
    public String index(Model model) {
        // 构造食谱数据 (复刻 Diet.tsx 中的数据)[cite: 2]
        List<Map<String, Object>> recipes = Arrays.asList(
                ImmutableMap.of(
                        "title", "Ginger Honey Lemon Tea",
                        "image", "https://images.unsplash.com/photo-1650298281766-fe31a198bb64?w=1080&q=80",
                        "tags", Arrays.asList("Hydration", "Immunity"),
                        "benefits", "Ginger reduces inflammation in the respiratory tract, while honey soothes the throat. Lemon provides Vitamin C to boost your immune system against pollutants.",
                        "ingredients", Arrays.asList("1 inch fresh ginger, sliced", "1 tbsp raw honey", "Half a lemon, juiced", "2 cups warm water")
                ),
                ImmutableMap.of(
                        "title", "Clear Chicken Soup",
                        "image", "https://images.unsplash.com/photo-1708782344071-35ed44b849a9?w=1080&q=80",
                        "tags", Arrays.asList("Comfort", "Recovery"),
                        "benefits", "Warm liquids help clear mucus from the airways. Chicken soup provides essential amino acids and minerals to help your body repair cellular damage from smog.",
                        "ingredients", Arrays.asList("200g chicken breast", "1 carrot, chopped", "1 celery stalk", "Garlic & onions", "Salt & pepper")
                )
                // ... 根据需要添加其他 3 个食谱[cite: 2]
        );

        // 构造运动数据 (复刻 Activities.tsx 中的数据)[cite: 4]
        List<Map<String, Object>> activities = Arrays.asList(
                ImmutableMap.of(
                        "title", "Indoor Yoga & Deep Breathing",
                        "image", "https://images.unsplash.com/photo-1758599879514-37ce0f87e6f2?w=1080&q=80",
                        "description", "Gentle yoga improves circulation and lung capacity without causing heavy breathing that would draw in more polluted air from outside.",
                        "steps", Arrays.asList(
                                "Find a quiet, clear space on your floor and lay down a mat.",
                                "Begin with 5 minutes of alternate nostril breathing to clear airways.",
                                "Move into Cat-Cow pose for 10 repetitions to open the chest.",
                                "Transition into Downward Dog, holding for 5 deep breaths.",
                                "End with Corpse pose (Savasana) for relaxation."
                        )
                )
                // ... 根据需要添加其他 2 个运动[cite: 4]
        );

        model.addAttribute("recipes", recipes);
        model.addAttribute("activities", activities);

        return "dashboard"; // 返回模板名称
    }



}