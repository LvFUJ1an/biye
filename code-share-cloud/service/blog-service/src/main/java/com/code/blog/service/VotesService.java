package com.code.blog.service;

import com.app.code.model.po.User;
import com.app.code.model.po.Votes;
import com.app.code.model.request.VatesQueryRequest;
import com.app.code.model.vo.VotesVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author lfj
* @description 针对表【votes(投票信息表)】的数据库操作Service
* @createDate 2025-05-02 17:22:20
*/
public interface VotesService extends IService<Votes> {

    Page<Votes> pageQuery(VatesQueryRequest vatesQueryRequest, User user);
}
