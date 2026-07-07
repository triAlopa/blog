package com.mojian.sensitive.listen;

import org.springframework.context.ApplicationEvent;

/**
 * 敏感词变更事件
 * <p>
 * 用于通知词库持有者重新加载词库.
 */
public class SensitiveWordChangeEvent extends ApplicationEvent {

    private final String action;

    /**
     * 构造函数
     *
     * @param source 事件源
     * @param action 操作类型: ADD/DELETE/BATCH_ADD/BATCH_DELETE/WHITELIST_ADD/WHITELIST_DELETE
     */
    public SensitiveWordChangeEvent(Object source, String action) {
        super(source);
        this.action = action;
    }

    /**
     * 获取操作类型
     *
     * @return 操作类型
     */
    public String getAction() {
        return action;
    }
}
