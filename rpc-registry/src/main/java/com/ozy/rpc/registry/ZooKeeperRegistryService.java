package com.ozy.rpc.registry;

import org.I0Itec.zkclient.ZkClient;

public class ZooKeeperRegistryService implements RegistryService {

    private final ZkClient zkClient;

    private String ZK_REGISTRY_PATH = "/registry";

    public ZooKeeperRegistryService(String zkAddress) {
        // 创建 ZooKeeper 客户端
        zkClient = new ZkClient(zkAddress, 5000, 5000);
    }

    @Override
    public void register(String name, String address) {
        // 创建 registry 节点（持久）
        String registryPath = ZK_REGISTRY_PATH;
        if (!zkClient.exists(registryPath)) {
            zkClient.createPersistent(registryPath);
        }
        // 创建 service 节点（持久）
        String servicePath = registryPath + "/" + name;
        if (!zkClient.exists(servicePath)) {
            zkClient.createPersistent(servicePath);
        }
        // 创建 address 节点（临时）
        String addressPath = servicePath + "/address-";
        String addressNode = zkClient.createEphemeralSequential(addressPath, address);
    }
}
