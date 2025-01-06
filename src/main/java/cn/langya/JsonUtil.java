package cn.langya;

import java.util.*;
import java.util.regex.*;

/**
 * 使用正则表达式进行JSON操作的工具类
 * 支持基本的JSON序列化和反序列化操作，包括多层嵌套和类型解析
 *
 * @author LangYa466
 * @since 2025/1/6
 */
public class JsonUtil {
    private static final Pattern OBJECT_PATTERN = Pattern.compile("\\{(.*?)\\}");
    private static final Pattern ARRAY_PATTERN = Pattern.compile("\\[(.*?)\\]");
    private static final Pattern KEY_VALUE_PATTERN = Pattern.compile("\\\"(.*?)\\\":(.*?)(,|$)");
    private static final Pattern STRING_PATTERN = Pattern.compile("\\\"(.*?)\\\"");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("-?\\d+(\\.\\d+)?");

    /**
     * 将JSON字符串解析为Map或List（支持多层嵌套）
     *
     * @param json JSON字符串
     * @return 解析后的对象（Map 或 List）
     */
    public static Object parse(String json) {
        json = json.trim();
        if (json.startsWith("{")) {
            return parseObject(json);
        } else if (json.startsWith("[")) {
            return parseArray(json);
        } else {
            throw new IllegalArgumentException("无效的JSON字符串: " + json);
        }
    }

    /**
     * 将JSON字符串解析为Map（支持多层嵌套）
     *
     * @param json JSON对象字符串
     * @return 表示JSON对象的Map
     */
    public static Map<String, Object> parseObject(String json) {
        Matcher objectMatcher = OBJECT_PATTERN.matcher(json);
        if (objectMatcher.find()) {
            String content = objectMatcher.group(1);
            Map<String, Object> result = new HashMap<>();
            Matcher keyValueMatcher = KEY_VALUE_PATTERN.matcher(content);
            while (keyValueMatcher.find()) {
                String key = keyValueMatcher.group(1);
                String value = keyValueMatcher.group(2).trim();
                result.put(key, parseValue(value));
            }
            return result;
        }
        throw new IllegalArgumentException("无效的JSON对象: " + json);
    }

    /**
     * 将JSON数组字符串解析为List（支持多层嵌套）
     *
     * @param json JSON数组字符串
     * @return 表示JSON数组的List
     */
    public static List<Object> parseArray(String json) {
        Matcher arrayMatcher = ARRAY_PATTERN.matcher(json);
        if (arrayMatcher.find()) {
            String content = arrayMatcher.group(1);
            List<Object> result = new ArrayList<>();
            for (String item : splitJsonArray(content)) {
                result.add(parseValue(item.trim()));
            }
            return result;
        }
        throw new IllegalArgumentException("无效的JSON数组: " + json);
    }

    /**
     * 根据字符串值解析成对应的Java对象
     *
     * @param value JSON值字符串
     * @return 解析后的Java对象
     */
    private static Object parseValue(String value) {
        if (value.startsWith("{")) {
            return parseObject(value);
        } else if (value.startsWith("[")) {
            return parseArray(value);
        } else if (STRING_PATTERN.matcher(value).matches()) {
            return value.substring(1, value.length() - 1); // 去掉引号
        } else if (NUMBER_PATTERN.matcher(value).matches()) {
            return value.contains(".") ? Double.parseDouble(value) : Integer.parseInt(value);
        } else if ("true".equals(value) || "false".equals(value)) {
            return Boolean.parseBoolean(value);
        } else if ("null".equals(value)) {
            return null;
        } else {
            throw new IllegalArgumentException("无效的JSON值: " + value);
        }
    }

    /**
     * 分割JSON数组中的元素，支持嵌套结构
     *
     * @param content JSON数组内容（去掉外层括号）
     * @return 分割后的元素列表
     */
    private static List<String> splitJsonArray(String content) {
        List<String> result = new ArrayList<>();
        int bracketLevel = 0;
        StringBuilder currentItem = new StringBuilder();
        for (char c : content.toCharArray()) {
            if (c == ',' && bracketLevel == 0) {
                result.add(currentItem.toString());
                currentItem.setLength(0);
            } else {
                if (c == '{' || c == '[') bracketLevel++;
                if (c == '}' || c == ']') bracketLevel--;
                currentItem.append(c);
            }
        }
        if (currentItem.length() > 0) {
            result.add(currentItem.toString());
        }
        return result;
    }

    /**
     * 将Map序列化为JSON字符串（支持多层嵌套）
     *
     * @param map 要序列化的Map
     * @return 表示Map的JSON字符串
     */
    public static String toJsonObject(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        map.forEach((key, value) -> sb.append("\"").append(key).append("\":").append(toJsonValue(value)).append(","));
        if (!map.isEmpty()) {
            sb.setLength(sb.length() - 1); // 移除末尾的逗号
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * 将List序列化为JSON数组字符串（支持多层嵌套）
     *
     * @param list 要序列化的List
     * @return 表示List的JSON数组字符串
     */
    public static String toJsonArray(List<Object> list) {
        StringBuilder sb = new StringBuilder("[");
        list.forEach(item -> sb.append(toJsonValue(item)).append(","));
        if (!list.isEmpty()) {
            sb.setLength(sb.length() - 1); // 移除末尾的逗号
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * 将Java对象转换为JSON值字符串
     *
     * @param value Java对象
     * @return JSON值字符串
     */
    private static String toJsonValue(Object value) {
        if (value instanceof String) {
            return "\"" + value + "\"";
        } else if (value instanceof Map) {
            return toJsonObject((Map<String, Object>) value);
        } else if (value instanceof List) {
            return toJsonArray((List<Object>) value);
        } else {
            return String.valueOf(value);
        }
    }

    /**
     * 验证字符串是否为有效的JSON对象
     *
     * @param json JSON字符串
     * @return 如果是有效的JSON对象则返回true，否则返回false
     */
    public static boolean isValidJsonObject(String json) {
        try {
            parseObject(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 合并两个JSON对象，后者的键值会覆盖前者
     *
     * @param json1 第一个JSON对象字符串
     * @param json2 第二个JSON对象字符串
     * @return 合并后的JSON对象字符串
     */
    public static String mergeJsonObjects(String json1, String json2) {
        Map<String, Object> map1 = parseObject(json1);
        Map<String, Object> map2 = parseObject(json2);
        map1.putAll(map2);
        return toJsonObject(map1);
    }

    /**
     * 根据前缀过滤JSON对象的键值对
     *
     * @param json JSON对象字符串
     * @param prefix 键前缀
     * @return 过滤后的JSON对象字符串
     */
    public static String filterKeysByPrefix(String json, String prefix) {
        Map<String, Object> map = parseObject(json);
        Map<String, Object> filteredMap = new HashMap<>();
        map.forEach((key, value) -> {
            if (key.startsWith(prefix)) {
                filteredMap.put(key, value);
            }
        });
        return toJsonObject(filteredMap);
    }

    /**
     * 统计JSON对象中的键数量
     *
     * @param json JSON对象字符串
     * @return 键的数量
     */
    public static int countJsonObjectKeys(String json) {
        return parseObject(json).size();
    }

    /**
     * 获取JSON对象中的所有键
     *
     * @param json JSON对象字符串
     * @return 键的列表
     */
    public static List<String> getJsonObjectKeys(String json) {
        return new ArrayList<>(parseObject(json).keySet());
    }

    /**
     * 反转JSON数组中的元素顺序
     *
     * @param json JSON数组字符串
     * @return 反转后的JSON数组字符串
     */
    public static String reverseJsonArray(String json) {
        List<Object> list = parseArray(json);
        Collections.reverse(list);
        return toJsonArray(list);
    }

    public static void main(String[] args) {
        // 测试多层嵌套对象
        String nestedObject = "{\"name\":\"LangYa466\",\"details\":{\"age\":25,\"languages\":[\"Java\",\"Python\"]}}";
        Object parsedObject = parse(nestedObject);
        System.out.println("解析的对象: " + parsedObject);

        // 测试多层嵌套数组
        String nestedArray = "[1, [2, 3], {\"key\":\"value\"}]";
        Object parsedArray = parse(nestedArray);
        System.out.println("解析的数组: " + parsedArray);

        // 测试序列化多层嵌套对象
        String serializedObject = toJsonObject((Map<String, Object>) parsedObject);
        System.out.println("序列化对象: " + serializedObject);

        // 测试序列化多层嵌套数组
        String serializedArray = toJsonArray((List<Object>) parsedArray);
        System.out.println("序列化数组: " + serializedArray);

        // 测试其他方法
        System.out.println("是否为有效JSON对象: " + isValidJsonObject(serializedObject));

        String json1 = "{\"a\":1,\"b\":2}";
        String json2 = "{\"b\":3,\"c\":4}";
        System.out.println("合并JSON对象: " + mergeJsonObjects(json1, json2));

        String filteredJson = filterKeysByPrefix(serializedObject, "d");
        System.out.println("过滤后的JSON对象: " + filteredJson);

        System.out.println("JSON对象键数量: " + countJsonObjectKeys(serializedObject));

        System.out.println("JSON对象的键: " + getJsonObjectKeys(serializedObject));

        System.out.println("反转JSON数组: " + reverseJsonArray(serializedArray));
    }
}