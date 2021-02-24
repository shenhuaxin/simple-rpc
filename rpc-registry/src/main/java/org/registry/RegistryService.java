package org.registry;

import org.core.ServiceMeta;

public interface RegistryService {

    void register(ServiceMeta serviceMeta);


    void unRegister(ServiceMeta serviceMeta);


    ServiceMeta discovery(String serviceName, int invokerHashCode);


    void destroy();
}
