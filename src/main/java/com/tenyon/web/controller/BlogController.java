package com.tenyon.web.controller;

import com.tenyon.web.common.domain.vo.resp.RtnData;
import com.tenyon.web.domain.entity.Blog;
import com.tenyon.web.domain.vo.blog.BlogVO;
import com.tenyon.web.service.BlogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "博客接口", description = "通用授权接口")
@RestController
@RequestMapping("api/blog")
public class BlogController {
    @Resource
    private BlogService blogService;

    @Operation(summary = "获取博客视图")
    @GetMapping("/getBlogVO")
    public RtnData<BlogVO> getBlogVOById(long blogId) {
        BlogVO blogVO = blogService.getBlogVOById(blogId);
        return RtnData.success(blogVO);
    }

    @Operation(summary = "获取所有博客视图列表")
    @GetMapping("/getBlogVOList")
    public RtnData<List<BlogVO>> getBlogVOList() {
        List<Blog> blogList = blogService.list();
        List<BlogVO> blogVOList = blogService.getBlogVOList(blogList);
        return RtnData.success(blogVOList);
    }

}
