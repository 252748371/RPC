package com.ozy.rpc.registry;

import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ZooKeeperDiscoveryService implements DiscoveryService {

    private final static Logger log = LoggerFactory.getLogger(ZooKeeperDiscoveryService.class);
    private String zkAddress;

    public ZooKeeperDiscoveryService(String zkAddress) {
        this.zkAddress = zkAddress;
    }

    private String ZK_REGISTRY_PATH = "/registry";

    @Override
    public String discover(String name) {
        // 创建 ZooKeeper 客户端
        ZkClient zkClient = new ZkClient(zkAddress, 5000, 5000);
        log.info("zkAddress is " + zkAddress);
        try {
            // 获取 service 节点
            String servicePath = ZK_REGISTRY_PATH + "/" + name;
            if (!zkClient.exists(servicePath)) {
                throw new RuntimeException(String.format("can not find any service node on path: %s", servicePath));
            }
            List<String> addressList = zkClient.getChildren(servicePath);
            if (addressList == null || addressList.size() == 0) {
                throw new RuntimeException(String.format("can not find any address node on path: %s", servicePath));
            }
            // 获取 address 节点
            String address;
            int size = addressList.size();
            if (size == 1) {
                address = addressList.get(0);
            } else {
                address = addressList.get(ThreadLocalRandom.current().nextInt(size));
            }
            // 获取 address 节点的值
            String addressPath = servicePath + "/" + address;
            log.info("addressPath is "+addressPath);
            return zkClient.readData(addressPath);
        } finally {
            zkClient.close();
        }
    }
}
