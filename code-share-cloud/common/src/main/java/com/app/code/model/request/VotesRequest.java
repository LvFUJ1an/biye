package com.app.code.model.request;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author lfj
 * @createDate 2025/5/2
 */
@Data
public class VotesRequest {
    /**
     * 投票标题
     */
    private String title;


    /**
     * 投票类型：0-单选，1-多选
     */
    private Integer voteType;

    private Date endTime;

    private List<String> optionTextList;
}
