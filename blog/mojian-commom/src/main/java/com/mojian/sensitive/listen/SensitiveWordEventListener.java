package com.mojian.sensitive.listen;

import com.mojian.sensitive.service.SensitiveWordBsHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 敏感词变更事件监听器
 * <p>
 * 用于接收敏感词变更事件，触发词库重新加载.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SensitiveWordEventListener {

    private final SensitiveWordBsHolder sensitiveWordBsHolder;

    /**
     * 监听敏感词变更事件
     *
     * @param event 变更事件
     */
    @EventListener
    public void handleSensitiveWordChange(SensitiveWordChangeEvent event) {
        log.info("收到敏感词变更事件: action={}", event.getAction());
        sensitiveWordBsHolder.reload();
    }
}
