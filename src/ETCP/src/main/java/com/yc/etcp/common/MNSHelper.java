package com.yc.etcp.common;

import com.aliyun.mns.client.CloudAccount;
import com.aliyun.mns.client.CloudQueue;
import com.aliyun.mns.client.MNSClient;
import com.aliyun.mns.common.utils.ServiceSettings;

public class MNSHelper {
    public static MNSClient client = null;
    public static final String QUEUE_NAME = "yishequ";

    static {
        CloudAccount account = new CloudAccount(
                ServiceSettings.getMNSAccessKeyId(),
                ServiceSettings.getMNSAccessKeySecret(),
                ServiceSettings.getMNSAccountEndpoint());
        client = account.getMNSClient();
    }

    public static CloudQueue getCloudQueue(String queueName){
        return client.getQueueRef(queueName);
    }
}
