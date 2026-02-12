package com.app.code.model.vo;

import com.app.code.model.po.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author lfj
 * @createDate 2025/5/2
 */
@Data
public class UserDetailVO{
    //用户信息
    private User user;
    //发布的博客列表
    private List<Blogs> blogsList;
    //发布的视频列表
    private List<Videos> videosList;
    //发布的问答列表
    private List<Questions> questionsList;
    //发布的投票列表
    private List<Votes> votesList;
    //关注列表
    private List<User> followList;
    //粉丝列表
    private List<User> fansList;
    //关注数量
    private Integer followCount;
    //粉丝数量
    private Integer fansCount;
}
