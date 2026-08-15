package com.example.shop.common;

import lombok.Data;

/**
 * 分页查询参数基类（T5 通用约定：?page=1&pageSize=10，pageSize max 100）
 * Controller 用 @ModelAttribute 或直接在方法参数接收；Service 统一转 MyBatis-Plus Page
 */
@Data
public class PageQuery {

    /** 页码（从 1 开始） */
    private long page = 1;

    /** 每页条数（默认 10，上限 100） */
    private long pageSize = 10;

    public long getPage() {
        return page < 1 ? 1 : page;
    }

    public long getPageSize() {
        if (pageSize < 1) {
            return 10;
        }
        return Math.min(pageSize, 100);
    }

    /** 计算 OFFSET（供 Mapper XML 手写 SQL 用） */
    public long offset() {
        return (getPage() - 1) * getPageSize();
    }
}
