package com.app.code.model.dto;

import lombok.Data;
import java.util.Date;

/**
 * 板块查询条件DTO
 * @author lfj
 * @createDate 2025/4/26
 */
@Data
public class SectionsDTO {
    
    /**
     * 板块ID
     */
    private Integer id;
    
    /**
     * 板块名称
     */
    private String name;
    
    /**
     * 板块描述
     */
    private String description;

    
    /**
     * 创建时间开始
     */
    private Date createTimeStart;
    
    /**
     * 创建时间结束
     */
    private Date createTimeEnd;
    
    /**
     * 当前页
     */
    private Integer current = 1;
    
    /**
     * 每页大小
     */
    private Integer size = 10;
} 