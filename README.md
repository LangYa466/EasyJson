# JsonUtil

`JsonUtil` 是一个简单的 JSON 工具类，方便你处理 JSON 数据，比如解析、生成、合并等。

## 功能
- **解析 JSON 对象**：把 JSON 对象字符串变成 `Map`。
- **解析 JSON 数组**：把 JSON 数组字符串变成 `List`。
- **生成 JSON 对象**：把 `Map` 变成 JSON 对象字符串。
- **生成 JSON 数组**：把 `List` 变成 JSON 数组字符串。
- **校验 JSON**：检查字符串是不是有效的 JSON。
- **合并 JSON 对象**：合并两个 JSON 对象。
- **过滤键**：根据前缀过滤 JSON 对象里的键。
- **统计键数量**：计算 JSON 对象里有多少个键。
- **获取所有键**：获取 JSON 对象里的所有键。
- **反转数组**：把 JSON 数组里的元素顺序反过来。

## 示例代码

```java
String jsonObject = "{\"name\":\"LangYa466\",\"project\":\"JsonUtil\"}";
Map<String, String> map = JsonUtil.parseObject(jsonObject);
System.out.println(map);

String jsonArray = "[\"item1\", \"item2\", \"item3\"]";
List<String> list = JsonUtil.parseArray(jsonArray);
System.out.println(list);

String serializedObject = JsonUtil.toJsonObject(map);
System.out.println(serializedObject);

String serializedArray = JsonUtil.toJsonArray(list);
System.out.println(serializedArray);

String jsonObject2 = "{\"version\":\"1.0\"}";
String mergedObject = JsonUtil.mergeJsonObjects(jsonObject, jsonObject2);
System.out.println(mergedObject);

String filteredObject = JsonUtil.filterKeysByPrefix(jsonObject, "pro");
System.out.println(filteredObject);
```

## 环境要求
- **Java 版本**：JDK 8 或更高

## 作者
- **作者**: LangYa466
- **邮箱**: [langya466@gmail.com](mailto:langya466@gmail.com)

## 许可证
MIT 许可证
