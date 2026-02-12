package com.code.blog.manager;

import com.zhipu.oapi.ClientV4;
import com.zhipu.oapi.Constants;
import com.zhipu.oapi.service.v4.model.*;
import io.reactivex.Flowable;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lfj
 * @createDate 2025/4/20
 */

@Component
public class AiManager {



    // 较稳定的随机数
    private static final float STABLE_TEMPERATURE = 0.05f;

    // 不稳定的随机数
    private static final float UNSTABLE_TEMPERATURE = 0.99f;


    @Bean
    private WebClient init() {
        return WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "text/event-stream").build();
    }


}
