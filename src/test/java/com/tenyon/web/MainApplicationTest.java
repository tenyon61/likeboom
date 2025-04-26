package com.tenyon.web;

import cn.hutool.core.date.DateUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

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