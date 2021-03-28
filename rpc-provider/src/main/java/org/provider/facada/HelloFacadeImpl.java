package org.provider.facada;

import org.facade.rpcfacade.HelloInterface;
import org.provider.annotation.Service;


@Service
public class HelloFacadeImpl implements HelloInterface {
    @Override
    public String sayHello() {
        return "yes hello , you are right";
    }
}
