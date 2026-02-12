package com.app.code.model.vo;

import com.app.code.model.po.Sections;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author lfj
 * @createDate 2025/4/27
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SectionsVO extends Sections {
    private String count;
}
