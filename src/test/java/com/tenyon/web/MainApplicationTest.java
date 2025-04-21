package com.tenyon.web;

import cn.hutool.core.date.LocalDateTimeUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.ZoneOffset;

@SpringBootTest
class MainApplicationTest {

    @Test
    void contextLoads() {
        long l = LocalDateTimeUtil.now().plusDays(30).toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
        System.out.println(l);
    }
}