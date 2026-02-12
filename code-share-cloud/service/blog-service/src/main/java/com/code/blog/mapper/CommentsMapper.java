package com.code.blog.mapper;

import com.app.code.model.po.Comments;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author lfj
* @description 针对表【comments(评论表)】的数据库操作Mapper
* @createDate 2025-04-27 14:30:57
* @Entity com.app.code.model.po.Comments
*/
@Mapper
public interface CommentsMapper extends BaseMapper<Comments> {

}




