package com.code.blog.mapper;

import com.app.code.model.po.Blogs;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author lfj
* @description 针对表【blogs(技术博客表)】的数据库操作Mapper
* @createDate 2025-04-27 09:36:34
* @Entity com.app.code.model.po.Blogs
*/
@Mapper
public interface BlogsMapper extends BaseMapper<Blogs> {

}




