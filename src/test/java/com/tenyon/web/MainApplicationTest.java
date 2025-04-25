package com.tenyon.web;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import com.tenyon.web.common.utils.RedisUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.ZoneOffset;
import java.util.Date;

@SpringBootTest
class MainApplicationTest {

    @Test
    void contextLoads() {
        Date nowDate = new Date();
        // 格式化日期和时间
        System.out.println(DateUtil.second(nowDate));
        String date = DateUtil.format(nowDate, "HH:mm:") + ((DateUtil.second(nowDate) / 10) * 10);
        System.out.println(date);
    }
}