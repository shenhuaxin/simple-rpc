package org.registry;

public class RegistryFactory {

    private static volatile RegistryService registryService;


    public static RegistryService getInstance(String registryAddr, RegistryType type) throws Exception {
        if (null == registryService) {
            synchronized (RegistryFactory.class) {
                if (null == registryService) {
                    switch (type) {
                        case EUREKA:
                            registryService = new EurekaRegistryService(registryAddr);
                            break;
                        case ZOOKEEPER:
                            registryService = new ZookeeperRegistryService(registryAddr);
                            break;
                    }
                }
            }
        }
        return registryService;
    }

}
