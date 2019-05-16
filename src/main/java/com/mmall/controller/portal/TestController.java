package com.mmall.controller.portal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author caoduanxi
 * @2019/5/12 14:32
 */
@Controller
@RequestMapping("/test/")
public class TestController {
    private static final Logger logger = LoggerFactory.getLogger(TestController.class);


    @RequestMapping("test.do")
    @ResponseBody
    public String test(String string){
        logger.info("testInfo");
        logger.warn("testWarn");
        logger.error("testError");
        return "testStr:"+string;
    }
}
