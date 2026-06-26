package com.mojian.service.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mojian.entity.SysConfig;
import com.mojian.exception.ServiceException;
import com.mojian.mapper.SysConfigMapper;
import com.mojian.service.BangumiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Bangumi 追番服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BangumiServiceImpl implements BangumiService {

    private final SysConfigMapper sysConfigMapper;

    /**
     * 获取 Bangumi API 基础地址
     */
    private String getApiBaseUrl() {
        SysConfig config = sysConfigMapper.selectOne(
                new LambdaQueryWrapper<SysConfig>()
                        .eq(SysConfig::getConfigKey, "bangumi_api_url")
        );
        return config != null && config.getConfigValue() != null
                ? config.getConfigValue() : "https://api.bgm.tv";
    }

    /**
     * 获取 Bangumi 图片 CDN 地址
     */
    private String getImgBaseUrl() {
        SysConfig config = sysConfigMapper.selectOne(
                new LambdaQueryWrapper<SysConfig>()
                        .eq(SysConfig::getConfigKey, "bangumi_img_url")
        );
        return config != null && config.getConfigValue() != null
                ? config.getConfigValue() : "https://lain.bgm.tv";
    }

    /**
     * 获取 Bangumi 用户ID
     */
    private String getUserId() {
        SysConfig config = sysConfigMapper.selectOne(
                new LambdaQueryWrapper<SysConfig>()
                        .eq(SysConfig::getConfigKey, "bangumi_user_id")
        );
        if (config == null || config.getConfigValue() == null || config.getConfigValue().isEmpty()) {
            throw new ServiceException("未配置 Bangumi 用户ID");
        }
        return config.getConfigValue();
    }

    /**
     * 替换图片 URL 中的域名
     * 将原始 CDN 地址替换为配置的镜像站地址
     */
    private String replaceImgUrl(String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }
        String imgBaseUrl = getImgBaseUrl();
        // 替换常见的 Bangumi 图片 CDN 地址
        url = url.replace("https://lain.bgm.tv", imgBaseUrl);
        url = url.replace("https://bgm.tv", imgBaseUrl);
        url = url.replace("https://api.bgm.tv", imgBaseUrl);
        return url;
    }

    /**
     * 递归替换 JSON 对象中的图片 URL
     */
    private Object replaceImgUrlsInJson(Object json) {
        if (json instanceof JSONObject) {
            JSONObject obj = (JSONObject) json;
            JSONObject newObj = new JSONObject();
            for (Map.Entry<String, Object> entry : obj.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                // 图片相关字段进行替换
                if (key.equals("medium") || key.equals("large") || key.equals("common") ||
                    key.equals("small") || key.equals("grid") || key.equals("icon") ||
                    key.equals("cover") || key.equals("image") || key.equals("avatar") ||
                    key.equals("url") && value instanceof String && ((String) value).contains("bgm.tv")) {
                    newObj.set(key, replaceImgUrl(String.valueOf(value)));
                } else {
                    newObj.set(key, replaceImgUrlsInJson(value));
                }
            }
            return newObj;
        } else if (json instanceof JSONArray) {
            JSONArray arr = (JSONArray) json;
            JSONArray newArr = new JSONArray();
            for (Object item : arr) {
                newArr.add(replaceImgUrlsInJson(item));
            }
            return newArr;
        }
        return json;
    }

    /**
     * 发送 GET 请求到 Bangumi API
     */
    private Object sendGetRequest(String path) {
        String baseUrl = getApiBaseUrl();
        String url = baseUrl + path;

        try {
            log.info("请求 Bangumi API: {}", url);

            HttpResponse response = HttpRequest.get(url)
                    .header("User-Agent", "ShiyiBlog/1.0")
                    .header("Accept", "application/json")
                    .timeout(10000)
                    .execute();

            if (response.getStatus() == 200) {
                String body = response.body();
                Object json = JSONUtil.parse(body);
                // 替换图片 URL
                return replaceImgUrlsInJson(json);
            } else {
                log.error("Bangumi API 请求失败: HTTP {}", response.getStatus());
                throw new ServiceException("Bangumi API 请求失败: HTTP " + response.getStatus());
            }
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Bangumi API 请求异常: {}", e.getMessage());
            throw new ServiceException("Bangumi API 请求失败: " + e.getMessage());
        }
    }

    @Override
    public Object getCollections(int type, int offset, int limit) {
        String userId = getUserId();
        String path = String.format("/v0/users/%s/collections?subject_type=2&type=%d&limit=%d&offset=%d",
                userId, type, limit, offset);
        return sendGetRequest(path);
    }

    @Override
    @Cacheable(value = "bangumi_all_collections", key = "#root.methodName", unless = "#result == null")
    public Map<String, Object> getAllCollections() {
        String userId = getUserId();
        Map<String, Object> result = new HashMap<>();

        // 获取在看列表 (type=3)
        result.put("watching", getCollectionsByType(userId, 3));
        // 获取想看列表 (type=1)
        result.put("planned", getCollectionsByType(userId, 1));
        // 获取看过列表 (type=2)
        result.put("completed", getCollectionsByType(userId, 2));
        // 获取搁置列表 (type=4)
        result.put("onhold", getCollectionsByType(userId, 4));
        // 获取抛弃列表 (type=5)
        result.put("dropped", getCollectionsByType(userId, 5));

        return result;
    }

    /**
     * 根据类型获取收藏列表
     */
    private Object getCollectionsByType(String userId, int type) {
        String path = String.format("/v0/users/%s/collections?subject_type=2&type=%d&limit=50&offset=0",
                userId, type);
        return sendGetRequest(path);
    }

    @Override
    public Object getSubject(int subjectId) {
        String path = String.format("/v0/subjects/%d", subjectId);
        return sendGetRequest(path);
    }

    @Override
    public Object getSubjectPersons(int subjectId) {
        String path = String.format("/v0/subjects/%d/persons", subjectId);
        return sendGetRequest(path);
    }

    @Override
    public Object getUserInfo() {
        String userId = getUserId();
        String path = String.format("/v0/users/%s", userId);
        return sendGetRequest(path);
    }

    @Override
    public String testConnection() {
        try {
            String apiBaseUrl = getApiBaseUrl();
            String imgBaseUrl = getImgBaseUrl();
            String userId = getUserId();

            // 测试获取用户信息
            String path = String.format("/v0/users/%s", userId);
            Object result = sendGetRequest(path);

            return String.format("连接成功！\nAPI地址: %s\n图片CDN: %s\n用户ID: %s",
                    apiBaseUrl, imgBaseUrl, userId);
        } catch (Exception e) {
            return "连接失败: " + e.getMessage();
        }
    }
}
