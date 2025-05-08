package com.tenyon.web.controller;

import com.tenyon.web.common.domain.vo.resp.RtnData;
import com.tenyon.web.common.exception.ErrorCode;
import com.tenyon.web.domain.dto.thumb.DoThumbDTO;
import com.tenyon.web.service.ThumbService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 描述
 *
 * @author tenyon
 * @date 2025/4/18
 */
@Tag(name = "点赞接口", description = "点赞接口")
@RestController
@RequestMapping("/api/thumb")
public class ThumbController {

    private final Counter successCounter;
    private final Counter failureCounter;

    public ThumbController(MeterRegistry registry) {
        this.successCounter = Counter.builder("thumb.success.count")
                .description("Total sucessful thumb")
                .register(registry);

        this.failureCounter = Counter.builder("thumb.failure.count")
                .description("Total failed thumb")
                .register(registry);
    }

    @Resource
    private ThumbService thumbService;

    @Operation(summary = "点赞")
    @PostMapping("/do")
    public RtnData<Boolean> doThumb(@RequestBody DoThumbDTO doThumbDTO) {
        try {
            Boolean success = thumbService.doThumb(doThumbDTO);
            if (success) {
                successCounter.increment();
                return RtnData.success(true);
            } else {
                failureCounter.increment();
                return RtnData.fail(ErrorCode.SYSTEM_ERROR);
            }
        } catch (Exception e) {
            failureCounter.increment();
            return RtnData.fail(ErrorCode.SYSTEM_ERROR);
        }
    }

    @Operation(summary = "取消点赞")
    @PostMapping("/undo")
    public RtnData<Boolean> undoThumb(@RequestBody DoThumbDTO doThumbDTO) {
        Boolean success = thumbService.undoThumb(doThumbDTO);
        return RtnData.success(success);
    }
}
