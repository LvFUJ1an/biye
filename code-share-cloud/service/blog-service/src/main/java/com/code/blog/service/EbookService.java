package com.code.blog.service;

import com.app.code.model.po.Ebook;
import com.app.code.model.po.User;
import com.app.code.model.request.EBookQueryRequest;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author lfj
* @description 针对表【ebook】的数据库操作Service
* @createDate 2025-04-29 20:33:53
*/
public interface EbookService extends IService<Ebook> {

    Page<Ebook> pageQuery(EBookQueryRequest eBookQueryRequest, User user);
}
