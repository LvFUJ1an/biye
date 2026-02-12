package com.code.blog.service;

import com.app.code.model.po.Sections;
import com.app.code.model.dto.SectionsDTO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
* @author lfj
* @description 针对表【sections(技术交流板块表)】的数据库操作Service
* @createDate 2025-04-26 14:47:41
*/
public interface SectionsService extends IService<Sections> {
    /**
     * 多条件分页查询板块
     * @param sectionsDTO 查询条件
     * @return 分页结果
     */
    Page<Sections> pageQuery(SectionsDTO sectionsDTO);
}
