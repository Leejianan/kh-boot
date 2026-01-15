package com.kh.boot.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Hidden
@ConditionalOnProperty(name = "kh.boot.index.enabled", havingValue = "true", matchIfMissing = true)
public class IndexController {

    @GetMapping("/")
    public String index() {
        return "欢迎使用Kh-boot,请通过API接口地址进行访问";
    }
}
