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
    private static final Pattern ARRAY_PATTERN = Pattern.compile("\\[(.*?)]", Pattern.DOTALL);
    private static final Pattern STRING_PATTERN = Pattern.compile("\"(.*?)\"");
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
        json = json.trim();
        if (!json.startsWith("{") || !json.endsWith("}")) {
            throw new IllegalArgumentException("无效的JSON对象: " + json);
        }

        Map<String, Object> result = new HashMap<>();
        String content = json.substring(1, json.length() - 1).trim();
        int bracketLevel = 0;
        StringBuilder currentKey = new StringBuilder();
        StringBuilder currentValue = new StringBuilder();
        boolean inQuotes = false;
        boolean parsingKey = true;

        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);

            if (c == '\"' && (i == 0 || content.charAt(i - 1) != '\\')) {
                inQuotes = !inQuotes;
            }

            if (!inQuotes) {
                if (c == '{' || c == '[') {
                    bracketLevel++;
                } else if (c == '}' || c == ']') {
                    bracketLevel--;
                } else if (c == ':' && parsingKey && bracketLevel == 0) {
                    parsingKey = false;
                    continue;
                } else if (c == ',' && bracketLevel == 0) {
                    result.put(currentKey.toString().trim(), parseValue(currentValue.toString().trim()));
                    currentKey.setLength(0);
                    currentValue.setLength(0);
                    parsingKey = true;
                    continue;
                }
            }

            if (parsingKey) {
                currentKey.append(c);
            } else {
                currentValue.append(c);
            }
        }

        if (currentKey.length() > 0 && currentValue.length() > 0) {
            result.put(currentKey.toString().trim(), parseValue(currentValue.toString().trim()));
        }

        return result;
    }

    /**
     * 将JSON数组字符串解析为List（支持多层嵌套）
     *
     * @param json JSON数组字符串
     * @return 表示JSON数组的List
     */
    public static List<Object> parseArray(String json) {
        // 检查数组是否完整闭合
        if (!json.trim().endsWith("]")) {
            throw new IllegalArgumentException("无效的JSON数组: " + json + "，缺少右方括号");
        }

        Matcher arrayMatcher = ARRAY_PATTERN.matcher(json);
        if (arrayMatcher.find()) {
            String content = arrayMatcher.group(1).trim();
            List<Object> result = new ArrayList<>();
            for (String item : splitJsonArray(content)) {
                try {
                    result.add(parseValue(item.trim())); // 递归解析数组中的每个元素
                } catch (Exception e) {
                    throw new IllegalArgumentException("无法解析JSON数组中的元素: " + item, e);
                }
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
        boolean inQuotes = false;

        for (char c : content.toCharArray()) {
            // 检查是否是引号，注意转义字符
            if (c == '\"' && (currentItem.length() == 0 || currentItem.charAt(currentItem.length() - 1) != '\\')) {
                inQuotes = !inQuotes;
            }

            if (!inQuotes) {
                // 处理括号层级
                if (c == ',' && bracketLevel == 0) {
                    result.add(currentItem.toString().trim());
                    currentItem.setLength(0);
                    continue;
                } else if (c == '{' || c == '[') {
                    bracketLevel++; // 进入新的对象或数组
                } else if (c == '}' || c == ']') {
                    bracketLevel--; // 退出对象或数组
                }
            }

            currentItem.append(c);
        }

        if (currentItem.length() > 0) {
            result.add(currentItem.toString().trim());
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

    public static void main(String[] args) {
        // 测试多层嵌套对象
        String nestedObject = "{\"name\":\"LangYa466\",\"details\":{\"age\":25,\"languages\":[\"Java\",\"Python\"]}}";
        Object parsedObject = parse(nestedObject);
        System.out.println("解析的对象: " + parsedObject);

        // 测试序列化多层嵌套对象
        String serializedObject = toJsonObject((Map<String, Object>) parsedObject);
        System.out.println("序列化对象: " + serializedObject);
    }
}
