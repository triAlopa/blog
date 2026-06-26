package com.mojian.controller.bangumi;

import com.mojian.common.Result;
import com.mojian.service.BangumiService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Bangumi 追番控制器
 */
@RestController
@RequestMapping("/bangumi")
@RequiredArgsConstructor
@Api(tags = "Bangumi追番管理")
public class BangumiController {

    private final BangumiService bangumiService;

    /**
     * 获取用户追番列表
     * @param type 收藏类型：1=想看, 2=看过, 3=在看, 4=搁置, 5=抛弃
     */
    @GetMapping("/collections")
    @ApiOperation(value = "获取追番列表")
    public Result<Object> getCollections(
            @RequestParam(defaultValue = "3") int type,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "50") int limit) {
        return Result.success(bangumiService.getCollections(type, offset, limit));
    }

    /**
     * 获取所有追番数据（在看+想看+看过+搁置+抛弃）
     */
    @GetMapping("/all")
    @ApiOperation(value = "获取所有追番数据")
    public Result<Map<String, Object>> getAllCollections() {
        return Result.success(bangumiService.getAllCollections());
    }

    /**
     * 获取条目详情
     */
    @GetMapping("/subject/{subjectId}")
    @ApiOperation(value = "获取条目详情")
    public Result<Object> getSubject(@PathVariable int subjectId) {
        return Result.success(bangumiService.getSubject(subjectId));
    }

    /**
     * 获取条目相关人员信息
     */
    @GetMapping("/subject/{subjectId}/persons")
    @ApiOperation(value = "获取条目相关人员")
    public Result<Object> getSubjectPersons(@PathVariable int subjectId) {
        return Result.success(bangumiService.getSubjectPersons(subjectId));
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/user")
    @ApiOperation(value = "获取Bangumi用户信息")
    public Result<Object> getUserInfo() {
        return Result.success(bangumiService.getUserInfo());
    }

    /**
     * 测试 API 连接
     */
    @GetMapping("/test")
    @ApiOperation(value = "测试API连接")
    public Result<String> testConnection() {
        return Result.success(bangumiService.testConnection());
    }
}
