package com.minzheng.blog.task;

import cn.hutool.core.thread.NamedThreadFactory;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.minzheng.blog.cache.RedisCache;
import com.minzheng.blog.constant.RedisPrefixConst;
import com.minzheng.blog.vo.News;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author xiaoli
 * @date 2024-07-09 8:51
 * @slogan: 天下风云出我辈，一入代码岁月催
 */
@Component
@Slf4j
public class NewsTask implements CommandLineRunner {

    @Autowired
    private RedisCache redisCache;

    /**
     * 条数限制
     */
    private final static int maxLimitCount = 10;

    /**
     * 微博热搜
     */
    public void weibo() {
        String weiboUrl = "https://weibo.com/ajax/side/hotSearch";
        String weiboJson = HttpUtil.get(weiboUrl);
        List<Map<String, Object>> realtimeList = (List<Map<String, Object>>) JSONUtil.getByPath(JSONUtil.parse(weiboJson), "data.realtime");
        List<News> list = new ArrayList<>();
        realtimeList.forEach(item -> {
            String title = item.get("word") == null ?"":String.valueOf(item.get("word"));
            String url = "https://s.weibo.com/weibo?q=%23" + title + "%23";
            String iconDesc = item.get("icon_desc") == null ? "":String.valueOf(item.get("icon_desc"));
            iconDesc = this.checkIconDesc(iconDesc);
            Integer num = (Integer) item.get("num");
            News news = new News();
            news.setTitle(title);
            news.setUrl(url);
            news.setIconDesc(iconDesc);
            news.setNum(num);
            list.add(news);
        });
        List<News> collect = list.stream().limit(maxLimitCount).collect(Collectors.toList());
        redisCache.deleteObject(RedisPrefixConst.HOT_SEARCH + "weibo");
        redisCache.setCacheList(RedisPrefixConst.HOT_SEARCH + "weibo", collect);
    }

    private String checkIconDesc(String iconDesc) {

        List<String> iconList = new ArrayList<>();
        iconList.add("爆");
        iconList.add("热");
        iconList.add("沸");
        iconList.add("新");
        iconList.add("荐");
        iconList.add("音");
        iconList.add("影");
        iconList.add("剧");
        iconList.add("综");
        if (!iconList.contains(iconDesc)){
            return "荐";
        }
        return iconDesc;
    }


    @Override
    public void run(String... args) {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(() -> {
            try {
                log.info("schedule weibo start");
                weibo();
                log.info("schedule weibo success");
            } catch (Throwable e) {
                log.info("failed to fetch weiboApi {}", e.getMessage());
            }

        }, 0, 600, TimeUnit.SECONDS);


    }

    /**
     * 百度热搜
     */
    public void baidu() {
        String baiduUrl = "https://top.baidu.com/board?tab=realtime";
        String baiduHtml = HttpUtil.get(baiduUrl);
        Document doc = Jsoup.parse(baiduHtml);
        Elements ul = doc.select(".content_1YWBm");
        List<News> list = new ArrayList<>();
        for (Element li : ul) {
            String url = li.select("a").attr("href");
            String title = li.select(".c-single-text-ellipsis").text();
            News news = new News();
            news.setTitle(title);
            news.setUrl(url);
            list.add(news);
        }
        List<News> collect = list.stream().limit(maxLimitCount).collect(Collectors.toList());
        redisCache.deleteObject(RedisPrefixConst.HOT_SEARCH + "baidu");
        redisCache.setCacheList(RedisPrefixConst.HOT_SEARCH + "baidu", collect);
    }


    /**
     * 头条热搜
     */
    public void toutiao() {
        String toutiaoUrl = "https://www.toutiao.com/hot-event/hot-board/?origin=toutiao_pc";
        String toutiaoJson = HttpUtil.get(toutiaoUrl);
        List<Map<String, String>> data = (List<Map<String, String>>) JSONUtil.getByPath(JSONUtil.parse(toutiaoJson), "data");
        List<Map<String, String>> fixed_top_data = (List<Map<String, String>>) JSONUtil.getByPath(JSONUtil.parse(toutiaoJson), "fixed_top_data");
        data.add(0, fixed_top_data.get(0));
        List<News> list = new ArrayList<>();
        data.forEach(item -> {
            String title = item.get("Title");
            String url = item.get("Url");
            News news = new News();
            news.setTitle(title);
            news.setUrl(url);
            list.add(news);
        });
        List<News> collect = list.stream().limit(maxLimitCount).collect(Collectors.toList());
        redisCache.deleteObject(RedisPrefixConst.HOT_SEARCH + "toutiao");
        redisCache.setCacheList(RedisPrefixConst.HOT_SEARCH + "toutiao", collect);
    }

    /**
     * 知乎热搜
     */
    public void zhihu() {
        String zhihuUrl = "https://www.zhihu.com/api/v3/feed/topstory/hot-lists/total?limit=50&desktop=true";
        String zhihuJson = HttpUtil.get(zhihuUrl);
        List<Map<String, JSONObject>> data = (List<Map<String, JSONObject>>) JSONUtil.getByPath(JSONUtil.parse(zhihuJson), "data");
        List<News> list = new ArrayList<>();
        data.forEach(item -> {
            String title = (String) item.get("target").get("title");
            String cardId = String.valueOf(item.get("card_id")).split("_")[1];
            String url = "https://www.zhihu.com/question/" + cardId;
            News news = new News();
            news.setTitle(title);
            news.setUrl(url);
            list.add(news);
        });
        List<News> collect = list.stream().limit(maxLimitCount).collect(Collectors.toList());
        redisCache.deleteObject(RedisPrefixConst.HOT_SEARCH + "zhihu");
        redisCache.setCacheList(RedisPrefixConst.HOT_SEARCH + "zhihu", collect);
    }


}
