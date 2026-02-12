package com.app.code.model.vo;

import lombok.Data;

import java.util.List;

/**
 * 仪表盘数据VO
 * @author lfj
 * @createDate 2025/5/4
 */
@Data
public class DashboardVO {
    /**
     * 博客总数
     */
    private Integer blogCount;
    
    /**
     * 用户总数
     */
    private Integer userCount;
    
    /**
     * 视频总数
     */
    private Integer videoCount;
    
    /**
     * 电子书总数
     */
    private Integer ebookCount;
    
    /**
     * 本月每天的新增用户数（用于折线图）
     * 数据格式：[日期1用户数, 日期2用户数, ...]
     */
    private List<Integer> userGrowthData;
    
    /**
     * 本月每天的新增用户数对应的日期标签（用于折线图）
     * 数据格式：["日期1", "日期2", ...]
     */
    private List<String> userGrowthLabels;
    
    /**
     * 本月每天的博客数量（用于柱状图）
     * 数据格式：[日期1博客数, 日期2博客数, ...]
     */
    private List<Integer> blogDailyData;
    
    /**
     * 本月每天的博客数量对应的日期标签（用于柱状图）
     * 数据格式：["日期1", "日期2", ...]
     */
    private List<String> blogDailyLabels;
}
