package com.app.code.model.vo;

import com.app.code.model.po.VoteOptions;
import com.app.code.model.po.Votes;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lfj
 * @createDate 2025/5/2
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class VotesVO extends Votes {
    private List<VoteOptions> options;
}
