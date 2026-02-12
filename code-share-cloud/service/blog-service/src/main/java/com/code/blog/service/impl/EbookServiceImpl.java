package com.code.blog.service.impl;

import com.app.code.model.po.User;
import com.app.code.model.po.Videos;
import com.app.code.model.request.EBookQueryRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.app.code.model.po.Ebook;
import com.code.blog.service.EbookService;
import com.code.blog.mapper.EbookMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
* @author lfj
* @description 针对表【ebook】的数据库操作Service实现
* @createDate 2025-04-29 20:33:53
*/
@Service
public class EbookServiceImpl extends ServiceImpl<EbookMapper, Ebook>
    implements EbookService{

    @Override
    public Page<Ebook> pageQuery(EBookQueryRequest eBookQueryRequest, User user) {
        LambdaQueryWrapper<Ebook> wrapper = new LambdaQueryWrapper<>();
        if (user == null){
            wrapper.eq(Ebook::getIsPublished, 0);
        }
        wrapper.like(StringUtils.hasText(eBookQueryRequest.getKeyword()), Ebook::getTitle, eBookQueryRequest.getKeyword());
        wrapper.orderByDesc(Ebook::getCreateTime);
        // 创建分页对象
        Page<Ebook> page = new Page<>(eBookQueryRequest.getCurrent(), eBookQueryRequest.getSize());
        return page(page, wrapper);
    }
}




