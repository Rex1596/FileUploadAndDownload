package com.rex.api.ctrl;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Author lzw
 * Create 2021/8/23
 * Description
 */
@Controller
public class indexCtrl {

    @GetMapping("/")
    public String index() {
        return "index";
    }
}
