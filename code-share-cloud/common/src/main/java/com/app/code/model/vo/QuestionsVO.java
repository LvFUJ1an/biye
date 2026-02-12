package com.app.code.model.vo;

import com.app.code.model.po.Answers;
import com.app.code.model.po.Questions;
import com.app.code.model.po.User;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author lfj
 * @createDate 2025/5/1
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class QuestionsVO extends Questions {

    public QuestionsVO() {
    }

    public QuestionsVO(Questions questions) {
        this.setId(questions.getId());
        this.setTitle(questions.getTitle());
        this.setContent(questions.getContent());
        this.setUserId(questions.getUserId());
        this.setIsResolved(questions.getIsResolved());
        this.setCreateTime(questions.getCreateTime());
        this.setUpdateTime(questions.getUpdateTime());
    }

    private List<Answers> answers;

    private User user;
}
