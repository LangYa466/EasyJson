package cn.langya;

import java.util.*;
import java.util.regex.*;

/**
 * 使用正则表达式进行JSON操作的工具类
 * 支持基本的JSON序列化和反序列化操作
 *
 * @author LangYa466
 * @since 2025/1/6
 */
public class JsonUtil {
    private static final Pattern OBJECT_PATTERN = Pattern.compile("\\{(.*?)}");
    private static final Pattern ARRAY_PATTERN = Pattern.compile("\\[(.*?)]");
    private static final Pattern KEY_VALUE_PATTERN = Pattern.compile("\"(.*?)\":\"(.*?)\"");

    /**
     * 将JSON字符串解析为Map
     *
     * @param json JSON字符串
     * @return 表示JSON对象的Map
     */
    public static Map<String, String> parseObject(String json) {
        Matcher objectMatcher = OBJECT_PATTERN.matcher(json);
        if (objectMatcher.find()) {
            String content = objectMatcher.group(1);
            Map<String, String> result = new HashMap<>();
            Matcher keyValueMatcher = KEY_VALUE_PATTERN.matcher(content);
            while (keyValueMatcher.find()) {
                result.put(keyValueMatcher.group(1), keyValueMatcher.group(2));
            }
            return result;
        }
        throw new IllegalArgumentException("无效的JSON对象: " + json);
    }

    /**
     * 将JSON数组字符串解析为List
     *
     * @param json JSON数组字符串
     * @return 表示JSON数组的List
     */
    public static List<String> parseArray(String json) {
        Matcher arrayMatcher = ARRAY_PATTERN.matcher(json);
        if (arrayMatcher.find()) {
            String content = arrayMatcher.group(1);
            return Arrays.asList(content.split(",\\s*"));
        }
        throw new IllegalArgumentException("无效的JSON数组: " + json);
    }

    /**
     * 将Map序列化为JSON字符串
     *
     * @param map 要序列化的Map
     * @return 表示Map的JSON字符串
     */
    public static String toJsonObject(Map<String, String> map) {
        StringBuilder sb = new StringBuilder("{");
        map.forEach((key, value) -> sb.append("\"").append(key).append("\":\"").append(value).append("\","));
        if (!map.isEmpty()) {
            sb.setLength(sb.length() - 1); // 移除末尾的逗号
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * 将List序列化为JSON数组字符串
     *
     * @param list 要序列化的List
     * @return 表示List的JSON数组字符串
     */
    public static String toJsonArray(List<String> list) {
        StringBuilder sb = new StringBuilder("[");
        list.forEach(item -> sb.append("\"").append(item).append("\","));
        if (!list.isEmpty()) {
            sb.setLength(sb.length() - 1); // 移除末尾的逗号
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * 检查字符串是否是有效的JSON对象
     *
     * @param json JSON字符串
     * @return 如果是有效的JSON对象，返回true；否则返回false
     */
    public static boolean isValidJsonObject(String json) {
        return OBJECT_PATTERN.matcher(json).matches();
    }

    /**
     * 检查字符串是否是有效的JSON数组
     *
     * @param json JSON字符串
     * @return 如果是有效的JSON数组，返回true；否则返回false
     */
    public static boolean isValidJsonArray(String json) {
        return ARRAY_PATTERN.matcher(json).matches();
    }

    /**
     * 合并两个JSON对象
     *
     * @param json1 第一个JSON对象字符串
     * @param json2 第二个JSON对象字符串
     * @return 合并后的JSON对象字符串
     */
    public static String mergeJsonObjects(String json1, String json2) {
        Map<String, String> map1 = parseObject(json1);
        Map<String, String> map2 = parseObject(json2);
        map1.putAll(map2);
        return toJsonObject(map1);
    }

    /**
     * 根据前缀过滤JSON对象中的键
     *
     * @param json JSON对象字符串
     * @param prefix 要过滤的键前缀
     * @return 过滤后的JSON对象字符串
     */
    public static String filterKeysByPrefix(String json, String prefix) {
        Map<String, String> map = parseObject(json);
        Map<String, String> filteredMap = new HashMap<>();
        map.forEach((key, value) -> {
            if (key.startsWith(prefix)) {
                filteredMap.put(key, value);
            }
        });
        return toJsonObject(filteredMap);
    }

    /**
     * 统计JSON对象中键值对的数量
     *
     * @param json JSON对象字符串
     * @return 键值对的数量
     */
    public static int countJsonObjectKeys(String json) {
        return parseObject(json).size();
    }

    /**
     * 获取JSON对象中的所有键
     *
     * @param json JSON对象字符串
     * @return 键的集合
     */
    public static Set<String> getJsonObjectKeys(String json) {
        return parseObject(json).keySet();
    }

    /**
     * 将JSON数组反转
     *
     * @param json JSON数组字符串
     * @return 反转后的JSON数组字符串
     */
    public static String reverseJsonArray(String json) {
        List<String> list = parseArray(json);
        Collections.reverse(list);
        return toJsonArray(list);
    }

    public static void main(String[] args) {
        // 测试解析对象
        String jsonObject = "{\"name\":\"LangYa466\",\"project\":\"JsonUtil\"}";
        Map<String, String> map = parseObject(jsonObject);
        System.out.println("解析的Map: " + map);

        // 测试解析数组
        String jsonArray = "[\"item1\", \"item2\", \"item3\"]";
        List<String> list = parseArray(jsonArray);
        System.out.println("解析的List: " + list);

        // 测试序列化对象
        String serializedObject = toJsonObject(map);
        System.out.println("序列化对象: " + serializedObject);

        // 测试序列化数组
        String serializedArray = toJsonArray(list);
        System.out.println("序列化数组: " + serializedArray);

        // 测试有效JSON检查
        System.out.println("是否为有效JSON对象: " + isValidJsonObject(jsonObject));
        System.out.println("是否为有效JSON数组: " + isValidJsonArray(jsonArray));

        // 测试合并JSON对象
        String jsonObject2 = "{\"version\":\"1.0\",\"author\":\"LangYa466\"}";
        String mergedObject = mergeJsonObjects(jsonObject, jsonObject2);
        System.out.println("合并对象: " + mergedObject);

        // 测试按前缀过滤键
        String filteredObject = filterKeysByPrefix(jsonObject, "pro");
        System.out.println("过滤后的对象: " + filteredObject);

        // 测试统计键数量
        System.out.println("键数量: " + countJsonObjectKeys(jsonObject));

        // 测试获取所有键
        System.out.println("对象的键: " + getJsonObjectKeys(jsonObject));

        // 测试反转JSON数组
        String reversedArray = reverseJsonArray(jsonArray);
        System.out.println("反转后的数组: " + reversedArray);
    }
}
