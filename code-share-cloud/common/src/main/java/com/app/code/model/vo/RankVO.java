package com.app.code.model.vo;

import com.app.code.model.po.Blogs;
import com.app.code.model.po.User;
import lombok.Data;

import java.util.List;

/**
 * @author lfj
 * @createDate 2025/5/3
 */
@Data
public class RankVO {
    private List<Blogs> blogsRanks;
    private List<User> usersRanks;
}
