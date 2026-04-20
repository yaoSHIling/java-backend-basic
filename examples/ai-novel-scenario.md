# AI 写小说 → 自动发布到番茄小说 · 完整场景案例

> 本文件展示一个完整的业务场景实现：从需求分析 → 数据库设计 → 核心代码 → 定时任务。
> 代码基于本脚手架，可直接复制使用。

---

## 📋 需求描述

**目标：** 后端定时调用 Coze AI 工作流生成小说章节，存入草稿箱后推送到番茄小说平台。

**核心流程：**
```
定时触发（每天 09:30）
  → 调用 Coze AI 工作流生成章节内容
  → 内容存入本地草稿箱
  → 异步推送到番茄小说平台
  → 发送钉钉/Server酱通知
```

---

## 🗄️ 数据库设计

```sql
-- 小说信息表
CREATE TABLE novel_info (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    title           VARCHAR(200) NOT NULL COMMENT '书名',
    author          VARCHAR(100) NOT NULL COMMENT '作者',
    genre           VARCHAR(50)  COMMENT '类型：都市/玄幻/科幻等',
    fanqie_book_id VARCHAR(64)  COMMENT '番茄小说 book_id',
    fanqie_token    VARCHAR(500) COMMENT '番茄登录 Token',
    last_chapter_no INT          DEFAULT 0 COMMENT '最后更新章节号',
    status          TINYINT      DEFAULT 1 COMMENT '状态：1=连载中 2=已完结',
    created_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='小说信息';

-- 章节草稿表
CREATE TABLE novel_chapter (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    novel_id        BIGINT       NOT NULL COMMENT '所属小说ID',
    chapter_no      INT          NOT NULL COMMENT '章节号',
    title           VARCHAR(200) NOT NULL COMMENT '章节标题',
    content         LONGTEXT     NOT NULL COMMENT '章节正文',
    word_count      INT          DEFAULT 0 COMMENT '正文字数',
    status          TINYINT      NOT NULL DEFAULT 0 COMMENT '状态：0=草稿 1=已发布 2=失败',
    published_at    DATETIME     COMMENT '发布时间',
    coze_run_id     VARCHAR(64)  COMMENT 'Coze 工作流执行ID',
    error_msg       VARCHAR(500) COMMENT '失败原因',
    created_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_novel_chapter (novel_id, chapter_no),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='章节草稿';
```

---

## 💻 核心代码

### 1. NovelChapter 实体

```java
package com.example.basic.modules.novel.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.example.basic.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("novel_chapter")
public class NovelChapter extends BaseEntity {

    /** 所属小说ID */
    private Long novelId;

    /** 章节号 */
    private Integer chapterNo;

    /** 章节标题 */
    private String title;

    /** 章节正文 */
    private String content;

    /** 正文字数 */
    private Integer wordCount;

    /** 状态：0=草稿 1=已发布 2=失败 */
    private Integer status;

    /** 发布时间 */
    private Date publishedAt;

    /** Coze 工作流执行ID */
    private String cozeRunId;

    /** 失败原因 */
    private String errorMsg;

    // ===== 枚举值 =====
    public static final int STATUS_DRAFT = 0;
    public static final int STATUS_PUBLISHED = 1;
    public static final int STATUS_FAILED = 2;
}
```

### 2. NovelChapterDao

```java
package com.example.basic.modules.novel.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.basic.modules.novel.entity.NovelChapter;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NovelChapterDao extends BaseMapper<NovelChapter> {
}
```

### 3. NovelChapterService（接口）

```java
package com.example.basic.modules.novel.service;

import com.example.basic.modules.novel.entity.NovelChapter;
import java.util.List;

/**
 * 小说章节服务。
 */
public interface NovelChapterService {

    /**
     * AI 生成章节草稿。
     *
     * <p>调用 Coze 工作流生成小说章节内容，存入本地草稿箱。
     *
     * @param novelId   小说ID
     * @param chapterNo 章节号
     * @return 章节ID
     */
    Long generateAndSaveDraft(Long novelId, Integer chapterNo);

    /**
     * 发布章节到番茄小说。
     *
     * @param chapterId 章节ID
     */
    void publishToFanqie(Long chapterId);

    /**
     * 批量发布待审章节（后台异步）。
     *
     * <p>每次最多处理 50 章，避免频率限制。
     */
    void batchPublishPending();

    /**
     * 章节分页查询。
     */
    com.baomidou.mybatisplus.core.metadata.IPage<NovelChapter> page(
            com.example.basic.model.query.PageParams query, Long novelId);

    NovelChapter getById(Long id);
}
```

### 4. NovelChapterServiceImpl（核心实现）

```java
package com.example.basic.modules.novel.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.basic.common.exception.GlobalExceptionHandler.BizException;
import com.example.basic.common.result.ResultCode;
import com.example.basic.modules.coze.entity.CozeRequest;
import com.example.basic.modules.coze.entity.CozeWorkflowResponse;
import com.example.basic.modules.coze.service.CozeService;
import com.example.basic.modules.notification.service.NotificationService;
import com.example.basic.modules.novel.dao.NovelChapterDao;
import com.example.basic.modules.novel.dao.NovelInfoDao;
import com.example.basic.modules.novel.entity.NovelChapter;
import com.example.basic.modules.novel.entity.NovelInfo;
import com.example.basic.modules.novel.service.NovelChapterService;
import com.example.basic.util.RetryingUtil;
import com.example.basic.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class NovelChapterServiceImpl implements NovelChapterService {

    private final NovelChapterDao chapterDao;
    private final NovelInfoDao novelDao;
    private final CozeService cozeService;
    private final NotificationService notificationService;

    /** 每章目标字数 */
    private static final int CHAPTER_WORD_TARGET = 3000;

    // ==================== AI 生成章节 ====================

    @Override
    public Long generateAndSaveDraft(Long novelId, Integer chapterNo) {
        // 1. 查询小说信息
        NovelInfo novel = novelDao.selectById(novelId);
        if (novel == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "小说不存在");
        }

        // 2. 构造 Coze 工作流请求
        CozeRequest request = CozeRequest.builder()
                .workflowId("your-novel-workflow-id")   // Coze 工作流 ID
                .variables(Map.of(
                        "novel_title", novel.getTitle(),
                        "chapter_no", chapterNo,
                        "author", novel.getAuthor(),
                        "word_target", CHAPTER_WORD_TARGET,
                        "genre", novel.getGenre() != null ? novel.getGenre() : "都市"
                ))
                .build();

        // 3. 调用 Coze AI 工作流（指数退避重试，网络不稳定也能成功）
        CozeWorkflowResponse response;
        try {
            response = RetryingUtil.withExponentialBackoff(
                    () -> cozeService.triggerWorkflow(request),
                    3,       // 最多重试 3 次
                    2000     // 基础间隔 2 秒，自动翻倍
            );
        } catch (Exception e) {
            log.error("AI 生成章节失败 | novelId={} | chapterNo={}", novelId, chapterNo, e);
            notificationService.sendAlert("AI 写小说失败",
                    StrUtil.format("小说《{}》第{}章生成失败\n原因：{}",
                            novel.getTitle(), chapterNo, e.getMessage()));
            throw new BizException(ResultCode.SYSTEM_ERROR,
                    "AI 生成失败，请稍后重试: " + e.getMessage());
        }

        // 4. 检查工作流执行结果
        if (!response.isCompleted()) {
            throw new BizException(ResultCode.SYSTEM_ERROR,
                    "AI 工作流未完成，状态：" + response.getStatus());
        }

        String rawContent = response.getOutputText();
        if (StrUtil.isBlank(rawContent)) {
            throw new BizException(ResultCode.SYSTEM_ERROR, "AI 返回内容为空");
        }

        // 5. 解析工作流输出（格式：标题|正文）
        String title;
        String content;
        if (rawContent.contains("|")) {
            String[] parts = rawContent.split("\|", 2);
            title = parts[0].trim();
            content = parts[1].trim();
        } else {
            title = StrUtil.format("第{}章", chapterNo);
            content = rawContent;
        }

        // 6. 保存草稿
        NovelChapter chapter = new NovelChapter();
        chapter.setNovelId(novelId);
        chapter.setChapterNo(chapterNo);
        chapter.setTitle(title);
        chapter.setContent(content);
        chapter.setWordCount(StrUtil.trimAll(content).length());
        chapter.setStatus(NovelChapter.STATUS_DRAFT);
        chapter.setCozeRunId(response.getWorkflowRunId());
        chapterDao.insert(chapter);

        // 7. 更新小说最后章节号
        novel.setLastChapterNo(chapterNo);
        novelDao.updateById(novel);

        log.info("AI 生成章节草稿成功 | chapterId={} | chapterNo={} | 字数={}",
                chapter.getId(), chapterNo, chapter.getWordCount());

        // 8. 发送通知
        notificationService.sendDefault("📝 章节草稿已生成",
                StrUtil.format("《{}》第{}章\n标题：{}\n字数：{} 字",
                        novel.getTitle(), chapterNo, title, chapter.getWordCount()));

        return chapter.getId();
    }

    // ==================== 发布到番茄 ====================

    @Override
    public void publishToFanqie(Long chapterId) {
        NovelChapter chapter = chapterDao.selectById(chapterId);
        if (chapter == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "章节不存在");
        }
        if (chapter.getStatus() == NovelChapter.STATUS_PUBLISHED) {
            throw new BizException(ResultCode.BAD_REQUEST, "章节已发布，请勿重复操作");
        }

        NovelInfo novel = novelDao.selectById(chapter.getNovelId());
        if (novel == null || StrUtil.isBlank(novel.getFanqieBookId())) {
            throw new BizException(ResultCode.BAD_REQUEST, "小说未配置番茄信息");
        }

        try {
            // ========== TODO: 调用番茄小说 API ==========
            // 此处为模拟实现，实际需要通过浏览器自动化或 API 调用
            // 参考：https://github.com/hermes-agent/skills/fanqie-novel-auto-publish
            //
            // FanqiePublishResult result = fanqieApi.publishChapter(
            //         novel.getFanqieBookId(),
            //         novel.getFanqieToken(),
            //         chapter.getTitle(),
            //         chapter.getContent()
            // );

            // 模拟：随机成功率 90%
            if (ThreadLocalRandom.current().nextInt(100) < 90) {
                // 模拟发布成功
                chapter.setStatus(NovelChapter.STATUS_PUBLISHED);
                chapter.setPublishedAt(new Date());
            } else {
                throw new RuntimeException("番茄平台返回：设备指纹验证失败");
            }

            chapterDao.updateById(chapter);
            log.info("章节发布成功 | chapterId={} | novel={}", chapterId, novel.getTitle());

        } catch (Exception e) {
            // 发布失败
            chapter.setStatus(NovelChapter.STATUS_FAILED);
            chapter.setErrorMsg(e.getMessage());
            chapterDao.updateById(chapter);

            notificationService.sendAlert("⚠️ 章节发布失败",
                    StrUtil.format("《{}》第{}章发布失败\n原因：{}",
                            novel.getTitle(), chapter.getChapterNo(), e.getMessage()));

            throw new BizException(ResultCode.SYSTEM_ERROR,
                    "发布失败: " + e.getMessage());
        }
    }

    // ==================== 批量发布 ====================

    @Override
    @Async    // 异步执行，不阻塞调用方
    public void batchPublishPending() {
        // 1. 查询待发布的草稿（每次最多 50 章）
        List<NovelChapter> pending = chapterDao.selectList(
                new LambdaQueryWrapper<NovelChapter>()
                        .eq(NovelChapter::getStatus, NovelChapter.STATUS_DRAFT)
                        .orderByAsc(NovelChapter::getNovelId, NovelChapter::getChapterNo)
                        .last("LIMIT 50")
        );

        if (pending.isEmpty()) {
            log.info("没有待发布的章节草稿");
            return;
        }

        log.info("开始批量发布 | 数量={}", pending.size());

        int success = 0, failed = 0;
        for (NovelChapter chapter : pending) {
            try {
                publishToFanqie(chapter.getId());
                success++;

                // 番茄有频率限制，间隔 1 秒
                Thread.sleep(1000);

            } catch (BizException e) {
                // 业务异常（预期内的失败，如已发布）
                failed++;
                log.warn("章节发布业务异常 | chapterId={} | msg={}",
                        chapter.getId(), e.getMessage());

            } catch (Exception e) {
                // 系统异常
                failed++;
                log.error("章节发布系统异常 | chapterId={}", chapter.getId(), e);
            }
        }

        // 发送汇总通知
        String summary = StrUtil.format("批量发布完成 ✅\n成功：{} 章\n失败：{} 章",
                success, failed);
        notificationService.sendDefault("📚 小说批量发布汇总", summary);

        log.info("批量发布完成 | 成功={} | 失败={}", success, failed);
    }

    // ==================== 查询 ====================

    @Override
    public IPage<NovelChapter> page(com.example.basic.model.query.PageParams queryParams, Long novelId) {
        LambdaQueryWrapper<NovelChapter> wrapper = new LambdaQueryWrapper<>();
        if (novelId != null) {
            wrapper.eq(NovelChapter::getNovelId, novelId);
        }
        wrapper.orderByDesc(NovelChapter::getChapterNo);
        return chapterDao.selectPage(queryParams.toPage(), wrapper);
    }

    @Override
    public NovelChapter getById(Long id) {
        return chapterDao.selectById(id);
    }
}
```

### 5. NovelChapterController

```java
package com.example.basic.modules.novel.controller;

import com.example.basic.annotation.Login;
import com.example.basic.annotation.LogOperation;
import com.example.basic.common.result.Result;
import com.example.basic.model.query.PageParams;
import com.example.basic.modules.novel.entity.NovelChapter;
import com.example.basic.modules.novel.service.NovelChapterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 小说章节管理接口。
 *
 * <p>流程：生成草稿 → 发布到番茄 → 汇总通知
 */
@Tag(name = "09. 小说管理", description = "AI 写作 + 番茄发布")
@RestController
@RequestMapping("/novel/chapter")
@RequiredArgsConstructor
public class NovelChapterController {

    private final NovelChapterService chapterService;

    @Operation(summary = "AI 生成章节草稿",
            description = "调用 Coze AI 工作流生成小说章节内容，存入草稿箱后返回")
    @PostMapping("/generate")
    @Login
    @LogOperation("AI 生成章节草稿")
    public Result<Long> generate(
            @Parameter(description = "小说ID") @RequestParam Long novelId,
            @Parameter(description = "章节号") @RequestParam Integer chapterNo) {
        Long chapterId = chapterService.generateAndSaveDraft(novelId, chapterNo);
        NovelChapter chapter = chapterService.getById(chapterId);
        return Result.success(chapterId, "草稿生成成功，" + chapter.getWordCount() + " 字");
    }

    @Operation(summary = "发布章节到番茄",
            description = "将草稿箱中的章节推送到番茄小说平台（同步）")
    @PostMapping("/publish/{chapterId}")
    @Login
    @LogOperation("发布章节到番茄")
    public Result<Void> publish(@PathVariable Long chapterId) {
        chapterService.publishToFanqie(chapterId);
        return Result.success("发布成功");
    }

    @Operation(summary = "批量发布待审章节",
            description = "后台异步发布所有待发布的草稿章节，完成后发送通知")
    @PostMapping("/publish/batch")
    @Login
    @LogOperation("批量发布章节")
    public Result<Void> batchPublish() {
        chapterService.batchPublishPending();
        return Result.success("批量发布任务已启动，完成后将在通知中查看结果");
    }

    @Operation(summary = "章节分页查询")
    @GetMapping("/page")
    @Login
    public Result<com.example.basic.common.result.PageResult<NovelChapter>> page(
            NovelChapterPageQuery query) {
        return Result.success(
                com.example.basic.common.result.PageResult.of(
                        chapterService.page(query, query.getNovelId())));
    }

    @Operation(summary = "查看章节详情")
    @GetMapping("/{id}")
    @Login
    public Result<NovelChapter> getById(@PathVariable Long id) {
        NovelChapter chapter = chapterService.getById(id);
        return Result.success(chapter);
    }

    @Operation(summary = "删除草稿章节")
    @DeleteMapping("/{id}")
    @Login
    @LogOperation("删除草稿章节")
    public Result<Void> delete(@PathVariable Long id) {
        chapterService.deleteById(id);
        return Result.success();
    }

    // ===== 查询参数 =====
    @Data
    public static class NovelChapterPageQuery extends PageParams {
        private Long novelId;
        private Integer status;  // 0=草稿 1=已发布 2=失败
    }
}
```

### 6. 定时任务（每天自动执行）

```java
package com.example.basic.modules.novel;

import com.example.basic.modules.notification.service.NotificationService;
import com.example.basic.modules.novel.entity.NovelInfo;
import com.example.basic.modules.novel.impl.NovelChapterServiceImpl;
import com.example.basic.modules.novel.dao.NovelInfoDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 小说自动写作定时任务。
 *
 * <p>触发时间：每天 09:30（工作时间开始前）
 * <p>流程：生成新章节草稿 → 自动发布到番茄 → 通知作者
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NovelAutoWriteJob {

    private final NovelChapterServiceImpl chapterService;
    private final NovelInfoDao novelDao;
    private final NotificationService notificationService;

    /** 默认小说ID（实际项目中可改为查询所有连载中的小说） */
    private static final Long DEFAULT_NOVEL_ID = 1L;

    /**
     * 每天 09:30 自动生成并发布新章节。
     *
     * <p>Cron 表达式说明：秒 分 时 日 月 周
     * <pre>
     * 0 30 9 * * ?  → 每天 9:30:00 执行
     * 0 0/30 9 * * ? → 每天 9:00 和 9:30 各执行一次
     * </pre>
     */
    @Scheduled(cron = "0 30 9 * * ?")
    @Async
    public void autoGenerateAndPublish() {
        log.info("🕘 定时任务开始：AI 写小说");

        try {
            // 1. 查询小说信息
            NovelInfo novel = novelDao.selectById(DEFAULT_NOVEL_ID);
            if (novel == null) {
                log.warn("小说不存在，ID={}", DEFAULT_NOVEL_ID);
                return;
            }

            if (novel.getStatus() != 1) {
                log.info("小说已完结，跳过 | title={}", novel.getTitle());
                return;
            }

            // 2. 计算下一章编号
            int nextChapter = novel.getLastChapterNo() + 1;
            log.info("开始生成 | novel={} | chapterNo={}", novel.getTitle(), nextChapter);

            // 3. AI 生成章节草稿
            Long chapterId = chapterService.generateAndSaveDraft(novel.getId(), nextChapter);

            // 4. 自动发布到番茄（失败不影响草稿）
            try {
                chapterService.publishToFanqie(chapterId);

                notificationService.sendDefault("✅ AI 写小说任务完成",
                        StrUtil.format(
                                "《{}》第{}章已自动发布\n📖 标题：{}\n📝 字数：约 3000 字\n⏰ 执行时间：{}",
                                novel.getTitle(),
                                nextChapter,
                                "（见草稿）",
                                DateUtil.format(new Date())
                        ));

            } catch (Exception e) {
                // 发布失败，草稿保留，发送告警
                log.error("自动发布失败，草稿已保留 | chapterId={}", chapterId, e);
                notificationService.sendAlert("⚠️ AI 章节已生成但发布失败",
                        StrUtil.format(
                                "《{}》第{}章已生成草稿，但发布到番茄失败\n原因：{}\n请手动检查后重试发布",
                                novel.getTitle(), nextChapter, e.getMessage()));
            }

        } catch (Exception e) {
            log.error("AI 写小说定时任务异常", e);
            notificationService.sendAlert("❌ AI 写小说任务异常",
                    "定时任务执行失败，请检查系统日志\n原因：" + e.getMessage());
        }
    }

    /**
     * 每周日凌晨 02:00 批量处理发布失败的章节重试。
     */
    @Scheduled(cron = "0 0 2 ? * SUN")
    @Async
    public void retryFailedChapters() {
        log.info("🕑 定时任务开始：重试发布失败的章节");
        try {
            chapterService.batchPublishPending();
        } catch (Exception e) {
            log.error("重试失败章节任务异常", e);
        }
    }
}
```

---

## 🔧 application.yml 配置

```yaml
# Coze AI 工作流配置
coze:
  enabled: true
  base-url: https://api.coze.cn
  api-token: ${COZE_API_TOKEN:}
  workflow-id: your-novel-writing-workflow-id
  poll-interval: 2
  poll-timeout: 120    # AI 生成可能较慢，设长一点

# 通知渠道配置
notification:
  dingtalk:
    enabled: true
    webhook-url: ${DINGTALK_WEBHOOK_URL:}
  serverchan:
    enabled: true
    sendkey: ${SERVERCHAN_SENDKEY:}

# 异步任务线程池（已内置）
# TaskConfig 中的 threadPoolTaskScheduler 用于 @Async 和 @Scheduled
```

---

## 📊 调用时序图

```
┌──────────────┐      ┌──────────────────────────────────────────┐
│  定时任务    │      │           NovelChapterService             │
│ NovelCronJob │      │                                          │
└──────┬───────┘      │  generateAndSaveDraft()                 │
       │              │       │                                   │
       │              │       ▼                                   │
       │              │  CozeService.triggerWorkflow()           │
       │              │       │                                   │
       │              │       ▼  (指数退避重试)                   │
       │              │  Coze AI 工作流执行                       │
       │              │       │                                   │
       │              │       ▼                                   │
       │              │  解析输出（标题|正文）                    │
       │              │       │                                   │
       │              │       ▼                                   │
       │              │  NovelChapterDao.insert()  [草稿]         │
       │              │       │                                   │
       │              │       ▼                                   │
       │              │  NotificationService.sendDefault()        │
       │              │       │                                   │
       │              │       ▼                                   │
       │              │  返回 chapterId                          │
       │              └──────────────────────────────────────────┘
       │
       │ (异步)
       ▼
publishToFanqie()
       │
       ├── NovelChapterDao.update(status=1)
       │
       └── NotificationService.sendAlert()
```

---

## ⚠️ 番茄小说发布注意事项

> 番茄小说平台使用 ByteDance 安全 SDK，自动化发布存在设备指纹检测。
> 当前已知方案：

| 方案 | 原理 | 难度 |
|------|------|------|
| Browserbase 云浏览器 | 住宅代理 + 高级隐身模式 | 中等 |
| Playwright + 住宅代理 | 本地浏览器 + 代理 | 高 |
| 官方 API（需申请）| 申请作家 API 白名单 | 高 |
| 手动复制发布 | 最稳定 | — |

本脚手架已将 **Coze AI 生成** 环节完全打通，**发布到番茄** 环节需根据实际情况选择方案。
