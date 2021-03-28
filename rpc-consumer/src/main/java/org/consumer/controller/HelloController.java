package org.consumer.controller;

import org.consumer.annotation.RpcReference;
import org.facade.rpcfacade.HelloInterface;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @RpcReference
    private HelloInterface helloFacade;

    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    public String sayHello() {
        return helloFacade.sayHello();
    }
}

