package com.app.code.common;

import lombok.Data;

/**
 * @author lfj
 * @createDate 2025/5/2
 */
@Data
public class ResultMessage {
    private Boolean flag;
    private String fromName;
    private Object message;
}
