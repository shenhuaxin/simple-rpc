package org.registry;

import org.core.ServiceMeta;

public class EurekaRegistryService implements RegistryService{


    public EurekaRegistryService(String registerAddr) {
    }

    @Override
    public void register(ServiceMeta serviceMeta) {

    }

    @Override
    public void unRegister(ServiceMeta serviceMeta) {

    }

    @Override
    public ServiceMeta discovery(String serviceName, int invokerHashCode) {
        return null;
    }

    @Override
    public void destroy() {

    }
}
