package com.gearcode.brush.server.controller;

import com.gearcode.brush.server.client.bean.BrushClient;
import com.gearcode.brush.server.boot.NettyBrushServerSpringHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author jason
 */
@RequestMapping("/")
@Controller
public class ClientInfoController {
    private static final Logger logger = LoggerFactory.getLogger(ClientInfoController.class);

    @Autowired
    @Qualifier("brushServerHolder")
    NettyBrushServerSpringHolder brushServer;

    @RequestMapping("/clients")
    @ResponseBody
    public List<BrushClient> clients(HttpServletRequest request, HttpServletResponse response) {
        logger.info("load clients");
        List<BrushClient> clients = brushServer.getServer().clients();
        return clients;
    }

}
