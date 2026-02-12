package com.code.blog.controller;

import com.app.code.common.BaseResponse;
import com.app.code.model.po.Blogs;
import com.app.code.model.vo.DashboardVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.code.blog.client.UserFeignClient;
import com.code.blog.service.BlogsService;
import com.code.blog.service.EbookService;
import com.code.blog.service.VideosService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 仪表盘数据接口
 * @author lfj
 * @createDate 2025/5/4
 */
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Resource
    private BlogsService blogsService;
    
    @Resource
    private VideosService videosService;
    
    @Resource
    private EbookService ebookService;
    
    @Resource
    private UserFeignClient userFeignClient;

    /**
     * 获取仪表盘数据，包括：
     * 1. 博客总数
     * 2. 用户总数
     * 3. 视频总数
     * 4. 电子书总数
     * 5. 用户本月增长曲线
     * 6. 本月每天的博客数量
     * @return 仪表盘数据
     */
    @GetMapping("/getDashboard")
    public BaseResponse<DashboardVO> getDashboard() {
        DashboardVO dashboardVO = new DashboardVO();
        
        // 1. 获取博客总数
        long blogCount = blogsService.count();
        dashboardVO.setBlogCount((int) blogCount);
        
        // 2. 获取用户总数
        Integer userCount = userFeignClient.getUserCount().getData();
        dashboardVO.setUserCount(userCount);
        
        // 3. 获取视频总数
        long videoCount = videosService.count();
        dashboardVO.setVideoCount((int) videoCount);
        
        // 4. 获取电子书总数
        long ebookCount = ebookService.count();
        dashboardVO.setEbookCount((int) ebookCount);
        
        // 5. 获取本月每天的用户注册数量
        Map<String, Integer> userGrowthMap = userFeignClient.getUserCountByDayInCurrentMonth().getData();
        List<String> userGrowthLabels = new ArrayList<>();
        List<Integer> userGrowthData = new ArrayList<>();
        processDailyData(userGrowthMap, userGrowthData, userGrowthLabels);
        dashboardVO.setUserGrowthLabels(userGrowthLabels);
        dashboardVO.setUserGrowthData(userGrowthData);
        
        // 6. 获取本月每天的博客发布数量
        Map<String, Integer> blogDailyMap = getBlogCountByDayInCurrentMonth();
        List<String> blogDailyLabels = new ArrayList<>();
        List<Integer> blogDailyData = new ArrayList<>();
        processDailyData(blogDailyMap, blogDailyData, blogDailyLabels);
        dashboardVO.setBlogDailyLabels(blogDailyLabels);
        dashboardVO.setBlogDailyData(blogDailyData);
        
        return BaseResponse.success(dashboardVO);
    }
    
    /**
     * 获取本月每天的博客发布数量
     * @return 本月每天的博客发布数量，格式为：{"日期1": 数量1, "日期2": 数量2, ...}
     */
    private Map<String, Integer> getBlogCountByDayInCurrentMonth() {
        // 获取当前月的第一天和最后一天
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date startDate = calendar.getTime();
        
        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.SECOND, -1);
        Date endDate = calendar.getTime();
        
        // 查询本月的所有博客
        LambdaQueryWrapper<Blogs> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.between(Blogs::getCreateTime, startDate, endDate);
        List<Blogs> blogs = blogsService.list(queryWrapper);
        
        // 按天统计博客数量
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Map<String, Integer> result = new HashMap<>();
        
        // 初始化本月每一天的数据
        calendar.setTime(startDate);
        while (calendar.getTime().before(endDate) || calendar.getTime().equals(endDate)) {
            result.put(dateFormat.format(calendar.getTime()), 0);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        // 统计博客数量
        for (Blogs blog : blogs) {
            String dateStr = dateFormat.format(blog.getCreateTime());
            result.put(dateStr, result.getOrDefault(dateStr, 0) + 1);
        }
        
        return result;
    }
    
    /**
     * 处理每日数据，转换为前端需要的格式
     * @param dataMap 原始数据Map
     * @param valuesList 值列表
     * @param labelsList 标签列表
     */
    private void processDailyData(Map<String, Integer> dataMap, List<Integer> valuesList, List<String> labelsList) {
        // 按日期排序
        List<Map.Entry<String, Integer>> sortedEntries = dataMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toList());
        
        // 分离标签和数据
        for (Map.Entry<String, Integer> entry : sortedEntries) {
            labelsList.add(entry.getKey());
            valuesList.add(entry.getValue());
        }
    }
}
