package com.example.shop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.shop.common.BusinessException;
import com.example.shop.common.ErrorCode;
import com.example.shop.common.PageQuery;
import com.example.shop.common.PageResult;
import com.example.shop.entity.Notice;
import com.example.shop.mapper.NoticeMapper;
import com.example.shop.security.UserContext;
import com.example.shop.service.NotificationService;
import com.example.shop.vo.NotificationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 站内通知服务实现
 * U-022/U-023/U-025；数据范围：仅接收者本人（T4 数据权限清单）
 */
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NoticeMapper noticeMapper;

    @Override
    public PageResult<NotificationVO> list(PageQuery pageQuery, Integer readStatus) {
        Long userId = UserContext.requireUserId();
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<Notice>()
                .eq(Notice::getReceiverId, userId)
                .orderByDesc(Notice::getCreatedAt);
        if (readStatus != null) {
            if (readStatus != 0 && readStatus != 1) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "readStatus 只能为 0 或 1");
            }
            wrapper.eq(Notice::getReadStatus, readStatus);
        }
        Page<Notice> page = noticeMapper.selectPage(
                new Page<>(pageQuery.getPage(), pageQuery.getPageSize()), wrapper);
        List<NotificationVO> list = page.getRecords().stream().map(n -> new NotificationVO(
                n.getId(), n.getTitle(), n.getContent(), n.getReadStatus(), n.getCreatedAt())).toList();
        return PageResult.of(list, page.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markRead(Long id) {
        Notice notice = requireOwnNotice(id);
        if (notice.getReadStatus() != null && notice.getReadStatus() == 1) {
            return;
        }
        notice.setReadStatus(1);
        noticeMapper.updateById(notice);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllRead() {
        Long userId = UserContext.requireUserId();
        noticeMapper.update(null, new LambdaUpdateWrapper<Notice>()
                .eq(Notice::getReceiverId, userId)
                .eq(Notice::getReadStatus, 0)
                .set(Notice::getReadStatus, 1));
    }

    /** 校验通知归属本人（T4 数据权限；1004 资源不存在） */
    private Notice requireOwnNotice(Long id) {
        Notice notice = noticeMapper.selectById(id);
        Long userId = UserContext.requireUserId();
        if (notice == null || !notice.getReceiverId().equals(userId)) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        return notice;
    }
}
