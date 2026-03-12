package com.monash.student.controller;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController // 注意这里必须是 @RestController，因为它要返回 JSON 数据给前端，而不是返回网页
@Slf4j
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
            log.info("正在请求 WAQI API: " + apiUrl);

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
            log.info("服务器内部/网络异常: " + e.getMessage());

            response.put("error", "Internal Server Error");
            response.put("aqi", 50); // 兜底
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    public static class Diet {
        public String title;
        public String description;
        public String imageUrl;
        public List<String> tags;
        public List<String> ingredients;

        public Diet(String title, String description, String imageUrl, List<String> tags, List<String> ingredients) {
            this.title = title;
            this.description = description;
            this.imageUrl = imageUrl;
            this.tags = tags;
            this.ingredients = ingredients;
        }
    }

    public static class Activity {
        public String title;
        public String description;
        public String imageUrl;
        public List<String> steps;

        public Activity(String title, String description, String imageUrl, List<String> steps) {
            this.title = title;
            this.description = description;
            this.imageUrl = imageUrl;
            this.steps = steps;
        }
    }

    @GetMapping("/getRecommendations")
    public Map<String, Object> getRandomRecommendations() {
        log.info("======================getRandomRecommendations============================");

        // ==========================================
// 1. 准备 20 种护肺/抗炎饮食数据池 (图片已全部更新为独立有效链接)
// ==========================================
        List<Diet> allDiets = new ArrayList<>(Arrays.asList(
                new Diet("Ginger Honey Lemon Tea", "Reduces inflammation in the throat and boosts immune system.", "https://images.unsplash.com/photo-1576092768241-dec231879fc3?w=500&auto=format&fit=crop", Arrays.asList("Hydration", "Immunity"), Arrays.asList("1 inch fresh ginger, sliced", "1 tbsp raw honey", "Half a lemon, juiced")),
                new Diet("Clear Chicken Soup", "Warm liquids help clear mucus. Easy to digest and repairing.", "https://images.unsplash.com/photo-1547592180-85f173990554?w=500&auto=format&fit=crop", Arrays.asList("Comfort", "Recovery"), Arrays.asList("200g chicken breast", "1 carrot, chopped", "1 celery stalk")),
                new Diet("Steamed Broccoli", "Softened broccoli helps excrete airborne pollutants safely.", "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=500&auto=format&fit=crop", Arrays.asList("Detox", "Vitamins"), Arrays.asList("1 cup fresh broccoli", "Olive oil dressing", "Pinch of black pepper")),
                new Diet("Warm Pear & Rock Sugar Soup", "A traditional Asian remedy to moisten lungs and relieve dry coughs.", "https://images.unsplash.com/photo-1565557623262-b51c2513a641?w=500&auto=format&fit=crop", Arrays.asList("Traditional", "Soothing"), Arrays.asList("2 Asian pears", "20g rock sugar", "10g snow fungus")),
                new Diet("Green Tea with Mint", "Rich in antioxidants that reduce respiratory inflammation.", "https://images.unsplash.com/photo-1627435601361-ec25f5b1d0e5?w=500&auto=format&fit=crop", Arrays.asList("Antioxidant", "Relaxing"), Arrays.asList("1 green tea bag", "Handful of fresh mint", "Hot water")),
                new Diet("Chrysanthemum Tea", "Cooling herbal tea that gently soothes a scratchy throat.", "https://images.unsplash.com/photo-1558160074-4d7d8bdf4256?w=500&auto=format&fit=crop", Arrays.asList("Herbal", "Cooling"), Arrays.asList("Dried chrysanthemum flowers", "Goji berries", "Warm water")),
                new Diet("Warm Oatmeal with Berries", "Soft, easy to swallow, and packed with lung-protecting antioxidants.", "https://images.unsplash.com/photo-1517673132405-a56a62b18caf?w=500&auto=format&fit=crop", Arrays.asList("Fiber", "Antioxidant"), Arrays.asList("1/2 cup rolled oats", "Handful of blueberries", "Warm milk")),
                new Diet("Steamed Salmon", "Omega-3 fatty acids combat the inflammatory effects of smog.", "https://images.unsplash.com/photo-1467003909585-2f8a72700288?w=500&auto=format&fit=crop", Arrays.asList("Omega-3", "Protein"), Arrays.asList("1 salmon fillet", "Ginger slices", "Light soy sauce")),
                new Diet("Turmeric Golden Milk", "Curcumin provides strong anti-inflammatory properties for lung health.", "https://images.unsplash.com/photo-1626844131082-256783844137?w=500&auto=format&fit=crop", Arrays.asList("Anti-inflammatory", "Warm"), Arrays.asList("1 cup warm milk", "1/2 tsp turmeric powder", "Pinch of black pepper")),
                new Diet("Spinach and Tofu Soup", "Soft texture with magnesium to help relax the muscles in your lungs.", "https://images.unsplash.com/photo-1576045057995-568f588f82fb?w=500&auto=format&fit=crop", Arrays.asList("Vitamins", "Soft Diet"), Arrays.asList("Handful of spinach", "Soft silken tofu", "Clear vegetable broth")),
                new Diet("Papaya Milk", "Highly soothing for the throat and very easy on the digestive system.", "https://images.unsplash.com/photo-1517282009859-f000ec3b26fe?w=500&auto=format&fit=crop", Arrays.asList("Vitamins", "Digestion"), Arrays.asList("1/2 ripe papaya", "1 cup warm milk", "Blended smooth")),
                new Diet("Steamed Egg Custard", "Protein-rich, zero-chewing required, perfect for sensitive throats.", "https://images.unsplash.com/photo-1496116218417-1a781b1c416c?w=500&auto=format&fit=crop", Arrays.asList("Protein", "Soft Diet"), Arrays.asList("2 eggs", "Warm water", "Drop of sesame oil")),
                new Diet("Apple & Carrot Juice", "Flavonoids in apples are linked to better lung function.", "https://images.unsplash.com/photo-1600271886742-f049cd451bba?w=500&auto=format&fit=crop", Arrays.asList("Vitamins", "Hydration"), Arrays.asList("1 fresh apple", "1 medium carrot", "Filtered water")),
                new Diet("Mushroom & Red Date Soup", "Boosts overall immunity and stamina without heavy oils.", "https://images.unsplash.com/photo-1548943487-a2e4e43b485d?w=500&auto=format&fit=crop", Arrays.asList("Immunity", "Traditional"), Arrays.asList("Dried shiitake mushrooms", "3-5 red dates", "Slow-cooked broth")),
                new Diet("Warm Lemon Water & Chia", "Highly hydrating. Chia seeds provide Omega-3 for reducing inflammation.", "https://images.unsplash.com/photo-1513364776144-60967b0f800f?w=500&auto=format&fit=crop", Arrays.asList("Hydration", "Detox"), Arrays.asList("1 tbsp chia seeds", "Warm water", "Slice of lemon")),
                new Diet("Almond Milk", "High in Vitamin E, protecting lung cells from oxidative stress.", "https://images.unsplash.com/photo-1563636619-e9143da7973b?w=500&auto=format&fit=crop", Arrays.asList("Vitamin E", "Dairy-free"), Arrays.asList("1 cup unsweetened almond milk", "Warm gently before drinking")),
                new Diet("Boiled Mung Bean Soup", "A classic cooling soup to clear internal heat and toxins.", "https://images.unsplash.com/photo-1543352634-99a5d50ae78e?w=500&auto=format&fit=crop", Arrays.asList("Detox", "Cooling"), Arrays.asList("1/2 cup green mung beans", "Water", "A little rock sugar")),
                new Diet("White Radish (Daikon) Soup", "Known in traditional diets to help clear mucus and ease breathing.", "https://images.unsplash.com/photo-1555072956-7758afb20e8f?w=500&auto=format&fit=crop", Arrays.asList("Clear Mucus", "Comfort"), Arrays.asList("1/2 white radish, cubed", "Pork ribs or chicken broth")),
                new Diet("Mashed Sweet Potato", "Rich in beta-carotene to maintain the health of the respiratory lining.", "https://images.unsplash.com/photo-1605050604810-09a8ccf8cc0e?w=500&auto=format&fit=crop", Arrays.asList("Vitamins", "Soft Diet"), Arrays.asList("1 large sweet potato, steamed", "Dash of milk to mash")),
                new Diet("Garlic and Onion Broth", "Natural antibacterial properties that boost the immune system's defense.", "https://images.unsplash.com/photo-1512058564366-18510be2db19?w=500&auto=format&fit=crop", Arrays.asList("Immunity", "Warm"), Arrays.asList("2 cloves garlic, crushed", "1/2 onion, chopped", "Vegetable stock"))
        ));

// ==========================================
// 2. 准备 20 种室内低强度运动数据池 (图片已全部更新为独立有效链接)
// ==========================================
        List<Activity> allActivities = new ArrayList<>(Arrays.asList(
                new Activity("Indoor Yoga & Deep Breathing", "Improves circulation and lung capacity without heavy breathing.", "https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=500&auto=format&fit=crop", Arrays.asList("Find a quiet space and sit comfortably.", "5 minutes of alternate nostril breathing.", "Perform gentle seated stretches.", "End with relaxation.")),
                new Activity("Chair Tai Chi", "Slow, deliberate movements that improve balance indoors safely.", "https://images.unsplash.com/photo-1516636176313-0857dc4622b1?w=500&auto=format&fit=crop", Arrays.asList("Sit upright in a sturdy chair.", "Slowly raise arms to shoulder height, inhaling.", "Gently push hands forward while exhaling.", "Repeat for 10 minutes.")),
                new Activity("Living Room Walking", "Keeps joints flexible and heart rate slightly elevated.", "https://images.unsplash.com/photo-1538805060514-97d9cc17730c?w=500&auto=format&fit=crop", Arrays.asList("Clear a safe path in your living room.", "Walk at a moderate, steady pace for 5-10 mins.", "Swing your arms gently.", "Stop if you feel any shortness of breath.")),
                new Activity("Seated Leg Lifts", "Strengthens the lower body while comfortably sitting on a sofa.", "https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?w=500&auto=format&fit=crop", Arrays.asList("Sit fully back in a sturdy chair.", "Slowly straighten one leg out in front of you.", "Hold for 3 seconds, then lower it.", "Repeat 10 times per leg.")),
                new Activity("Wall Push-ups", "A safe way to build upper body strength without straining joints.", "https://images.unsplash.com/photo-1599058917212-d750089bc07e?w=500&auto=format&fit=crop", Arrays.asList("Stand an arm's length facing a sturdy wall.", "Place hands flat on the wall at shoulder height.", "Slowly bend elbows to bring your chest closer.", "Push back to the starting position. Repeat 10 times.")),
                new Activity("Seated Neck Rolls", "Relieves tension from staying indoors with gentle stretches.", "https://images.unsplash.com/photo-1518611012118-696072aa579a?w=500&auto=format&fit=crop", Arrays.asList("Sit up straight and relax your shoulders.", "Gently drop your chin to your chest.", "Slowly roll your right ear to your right shoulder.", "Roll back down and over to the left.")),
                new Activity("Ankle Rotations", "Keeps ankle joints flexible and prevents stiffness while watching TV.", "https://images.unsplash.com/photo-1552674605-db6ffd4facb5?w=500&auto=format&fit=crop", Arrays.asList("Sit securely in a chair.", "Lift your right foot slightly off the floor.", "Rotate the ankle slowly 5 times clockwise.", "Repeat counter-clockwise, then switch feet.")),
                new Activity("Gentle Arm Circles", "Enhances shoulder mobility and opens up the chest for better breathing.", "https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?w=500&auto=format&fit=crop", Arrays.asList("Sit or stand tall.", "Extend your arms straight out to the sides.", "Draw small circles in the air with your hands.", "Do 10 forward circles, then 10 backward.")),
                new Activity("Heel-to-Toe Standing", "Excellent for improving balance and preventing falls at home.", "https://images.unsplash.com/photo-1566241440091-ec10de8db2e1?w=500&auto=format&fit=crop", Arrays.asList("Stand next to a wall or counter for support.", "Place the heel of one foot directly in front of the toes of the other.", "Hold this balanced position for 10 seconds.", "Switch feet and repeat.")),
                new Activity("Pursed-Lip Breathing", "A clinical exercise to strengthen lung capacity during hazy days.", "https://images.unsplash.com/photo-1499209974431-9dddcece7f88?w=500&auto=format&fit=crop", Arrays.asList("Relax your neck and shoulder muscles.", "Breathe in slowly through your nose for 2 counts.", "Pucker your lips as if you're going to whistle.", "Breathe out slowly and gently through your lips for 4 counts.")),
                new Activity("Seated Torso Twists", "Maintains spinal flexibility and aids digestion.", "https://images.unsplash.com/photo-1554284126-aa88f22d8b74?w=500&auto=format&fit=crop", Arrays.asList("Sit tall in a chair, feet flat on the floor.", "Cross your arms over your chest.", "Slowly twist your upper body to the right.", "Hold for 3 seconds, return to center, twist left.")),
                new Activity("Calf Raises", "Strengthens calves to support steady walking.", "https://images.unsplash.com/photo-1584735935682-2f2b69dff9d2?w=500&auto=format&fit=crop", Arrays.asList("Stand behind a sturdy chair, holding the back for balance.", "Slowly rise up onto your tiptoes.", "Hold for 1 second at the top.", "Slowly lower your heels back to the floor. Repeat 10 times.")),
                new Activity("Gentle Knee Extensions", "Lubricates the knee joints safely while seated.", "https://images.unsplash.com/photo-1571019613576-2b22c76fd955?w=500&auto=format&fit=crop", Arrays.asList("Sit comfortably with your back supported.", "Extend your right leg straight out, flexing the foot.", "Hold for 2 seconds, then slowly lower.", "Repeat 5-8 times per leg.")),
                new Activity("Sit-to-Stand Exercise", "A crucial exercise to maintain independent mobility and leg strength.", "https://images.unsplash.com/photo-1567598508481-65985588ce2a?w=500&auto=format&fit=crop", Arrays.asList("Sit on the edge of a sturdy, armless chair.", "Lean forward slightly from the hips.", "Push through your heels to stand up fully.", "Slowly sit back down with control. Repeat 5 times.")),
                new Activity("Finger Stretching", "Keeps hands nimble and relieves stiffness.", "https://images.unsplash.com/photo-1513258496099-4816c023d6fa?w=500&auto=format&fit=crop", Arrays.asList("Hold your hands out in front of you.", "Spread your fingers as far apart as possible.", "Hold for 3 seconds.", "Squeeze your fingers into a tight fist. Repeat 10 times.")),
                new Activity("Light Resistance Band Pulls", "Maintains upper back strength to support good breathing posture.", "https://images.unsplash.com/photo-1598266663439-2056e6900339?w=500&auto=format&fit=crop", Arrays.asList("Sit up straight and hold a light resistance band (or a small towel) in front of you.", "Keep your arms straight at chest height.", "Gently pull your hands apart, squeezing shoulder blades.", "Slowly return to start.")),
                new Activity("Water Bottle Bicep Curls", "Simple strength training using household items.", "https://images.unsplash.com/photo-1581009146145-b5ef050c2e1e?w=500&auto=format&fit=crop", Arrays.asList("Hold a small water bottle (500ml) in each hand.", "Sit tall with arms by your sides.", "Slowly bend your elbows to curl the bottles toward your shoulders.", "Lower them back down slowly. Repeat 10 times.")),
                new Activity("Side Leg Raises", "Strengthens hip abductors for better walking stability.", "https://images.unsplash.com/photo-1574680093668-39f156d9be7a?w=500&auto=format&fit=crop", Arrays.asList("Stand behind a chair, holding on for balance.", "Slowly lift your right leg out to the side.", "Keep your back straight and toes pointing forward.", "Lower slowly. Do 5-8 reps per side.")),
                new Activity("Seated Marching", "A great cardiovascular workout that doesn't risk falls.", "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=500&auto=format&fit=crop", Arrays.asList("Sit towards the front of your chair.", "Alternate lifting your knees up toward the ceiling, like marching.", "Pump your arms gently as you march.", "Continue for 2-3 minutes.")),
                new Activity("Guided Meditation", "Reduces anxiety and stress caused by being confined indoors.", "https://images.unsplash.com/photo-1506126613408-eca07ce68773?w=500&auto=format&fit=crop", Arrays.asList("Sit comfortably and close your eyes.", "Focus entirely on the sensation of air entering your nose.", "Notice your chest rising and falling.", "When your mind wanders, gently bring focus back to your breath."))
        ));
        // ==========================================
        // 3. 随机打乱列表，并截取所需数量
        // ==========================================
        Collections.shuffle(allDiets);
        Collections.shuffle(allActivities);

        Map<String, Object> response = new HashMap<>();

        // 要求：随机返回 5 个食谱 和 3 个运动
        response.put("diets", allDiets.subList(0, 5));
        response.put("activities", allActivities.subList(0, 3));

        return response;
    }

}

