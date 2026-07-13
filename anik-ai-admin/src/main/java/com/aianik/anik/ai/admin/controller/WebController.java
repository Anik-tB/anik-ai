package com.aianik.anik.ai.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Default interface
 *
 * @author: openanik
 * @date : 2022-03-28 22:17
 */
@Controller
public class WebController {

    @GetMapping("/root")
    public String forwardIndex() {
        return "index";
    }

}
