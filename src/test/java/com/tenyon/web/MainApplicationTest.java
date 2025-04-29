package com.tenyon.web;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.tenyon.web.common.constant.BmsConstant;
import com.tenyon.web.domain.dto.user.UserLoginDTO;
import com.tenyon.web.domain.entity.User;
import com.tenyon.web.service.UserService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.DigestUtils;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;


@SpringBootTest
@AutoConfigureMockMvc
class MainApplicationTest {

    @Resource
    private UserService userService;

    @Resource
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
        Date nowDate = new Date();
        // 格式化日期和时间
        System.out.println(DateUtil.second(nowDate));
        String date = DateUtil.format(nowDate, "HH:mm:") + ((DateUtil.second(nowDate) / 10) * 10);
        System.out.println(date);
    }

    @Test
    void addUser() {
        String encryptPassword = DigestUtils.md5DigestAsHex((BmsConstant.ENCRYPT_SALT + "11111").getBytes());
        for (int i = 0; i < 11; i++) {
            List<User> list = new ArrayList<>();
            for (int j = 0; j < 2000; j++) {
                User user = new User();
                user.setAccount(RandomUtil.randomString(12));
                user.setPassword(encryptPassword);
                list.add(user);
            }
            userService.saveBatch(list);
        }
    }


    @Test
    void testLoginAndExportSessionToCsv() throws Exception {
        List<User> list = userService.list();

        try (PrintWriter writer = new PrintWriter(new FileWriter("session_output.csv", true))) {
            // 如果文件是第一次写入，可以加一个逻辑写表头
            writer.println("userId,sessionId,timestamp");

            for (User user : list) {
                long testUserId = user.getId();

                UserLoginDTO loginDTO = new UserLoginDTO();
                loginDTO.setAccount(user.getAccount());
                loginDTO.setPassword("11111");

                MvcResult result = mockMvc.perform(post("/api/auth/login")
                                .content(JSONUtil.toJsonStr(loginDTO))
                                .contentType(MediaType.APPLICATION_JSON))
                        .andReturn();

                List<String> setCookieHeaders = result.getResponse().getHeaders("Set-Cookie");
                assertThat(setCookieHeaders).isNotEmpty();

                String sessionId = setCookieHeaders.stream()
                        .filter(cookie -> cookie.startsWith("satoken"))
                        .map(cookie -> cookie.split(";")[0]) // satoken=xxx
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("No SaToken found in response"));

                String sessionValue = sessionId.split("=")[1];

                writer.printf("%d,%s,%s%n", testUserId, sessionValue, LocalDateTime.now());

                System.out.println("✅ 写入 CSV：" + testUserId + " -> " + sessionValue);
            }
        }
    }
}