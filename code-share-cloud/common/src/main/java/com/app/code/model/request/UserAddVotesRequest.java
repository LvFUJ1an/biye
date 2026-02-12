package com.app.code.model.request;

import lombok.Data;

import java.util.List;

/**
 * @author lfj
 * @createDate 2025/5/2
 */
@Data
public class UserAddVotesRequest {
    private Integer voteId;
    private List<Integer> optionIdList;
}
