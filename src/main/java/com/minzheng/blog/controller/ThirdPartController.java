package com.minzheng.blog.controller;

import com.minzheng.blog.cache.RedisCache;
import com.minzheng.blog.constant.RedisPrefixConst;
import com.minzheng.blog.vo.News;
import com.minzheng.blog.vo.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author xiaoli
 * @date 2024-07-09 11:40
 * @slogan: 天下风云出我辈，一入代码岁月催
 */
@Api(tags = "第三方模块")
@RestController
public class ThirdPartController {

    @Autowired
    private RedisCache redisCache;

    /**
     * 新浪微博热搜
     *
     */
    @ApiOperation(value = "新浪微博热搜")
    @GetMapping("/thirdPart/weiboHotSearch")
    public Result<List<News>> weiboHotSearch() {
        List<News> cacheList = redisCache.getCacheList(RedisPrefixConst.HOT_SEARCH + "weibo");
        return Result.ok(cacheList);
    }
}
