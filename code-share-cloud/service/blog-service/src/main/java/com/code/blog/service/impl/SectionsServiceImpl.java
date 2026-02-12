package com.code.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.app.code.model.po.Sections;
import com.app.code.model.dto.SectionsDTO;
import com.code.blog.service.SectionsService;
import com.code.blog.mapper.SectionsMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
* @author lfj
* @description 针对表【sections(技术交流板块表)】的数据库操作Service实现
* @createDate 2025-04-26 14:47:41
*/
@Service
public class SectionsServiceImpl extends ServiceImpl<SectionsMapper, Sections>
    implements SectionsService{

    @Override
    public Page<Sections> pageQuery(SectionsDTO sectionsDTO) {
        // 创建查询条件
        LambdaQueryWrapper<Sections> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
//        if (sectionsDTO.getId() != null) {
//            queryWrapper.eq(Sections::getId, sectionsDTO.getId());
//        }
        
        if (StringUtils.hasText(sectionsDTO.getName())) {
            queryWrapper.like(Sections::getName, sectionsDTO.getName());
        }
        
        if (StringUtils.hasText(sectionsDTO.getDescription())) {
            queryWrapper.like(Sections::getDescription, sectionsDTO.getDescription());
        }
        
//        if (sectionsDTO.getAdminId() != null) {
//            queryWrapper.eq(Sections::getAdminId, sectionsDTO.getAdminId());
//        }
        
        if (sectionsDTO.getCreateTimeStart() != null) {
            queryWrapper.ge(Sections::getCreateTime, sectionsDTO.getCreateTimeStart());
        }
        
        if (sectionsDTO.getCreateTimeEnd() != null) {
            queryWrapper.le(Sections::getCreateTime, sectionsDTO.getCreateTimeEnd());
        }
        
        // 创建分页对象
        Page<Sections> page = new Page<>(sectionsDTO.getCurrent(), sectionsDTO.getSize());
        
        // 执行分页查询
        return page(page, queryWrapper);
    }
}




