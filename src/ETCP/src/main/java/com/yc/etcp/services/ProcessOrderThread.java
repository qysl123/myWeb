package com.yc.etcp.services;

import com.yc.edsi.juhe.JuheDataPO;
import com.yc.edsi.order.third.IThirdSellerService;
import com.yc.etcp.request.hongjiushijie.HongjiuOrderProcessHandler;
import com.yc.mq.util.MQUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProcessOrderThread {
    private final static Logger LOGGER = LoggerFactory.getLogger(ProcessOrderThread.class);
    @Value("#{eps['juhe.sleep.interval']}")
    private long SLEEP_TIME;

    @Resource
    private IThirdSellerService thirdSellerService;

    @Resource
    private UploadReqHandler uploadReqHandler;

    @Autowired
    private ApplicationContext appContext;

    private ProcessOrderSysThread processOrderSysThread;

    private HongjiuOrderProcessHandler processHandler;

    @PostConstruct
    private synchronized void init() {
        processHandler = new HongjiuOrderProcessHandler(thirdSellerService, appContext, uploadReqHandler);
        if (processOrderSysThread == null) {
            try {
                processOrderSysThread = new ProcessOrderSysThread();
                processOrderSysThread.start();
            } catch (Exception e) {
            }

        }
    }

    class ProcessOrderSysThread extends Thread {
        private boolean canRun = true;
        private long lastSleepTime = System.currentTimeMillis();

        public void run() {
            while (canRun) {
                long thisSleepTime = SLEEP_TIME - (System.currentTimeMillis() - lastSleepTime);
                if (thisSleepTime > 0) {
                    try {
                        Thread.sleep(thisSleepTime);
                        lastSleepTime = System.currentTimeMillis();
                    } catch (Exception e) {
                        LOGGER.error("线程休眠时出现异常", e);
                    }
                }
                MQUtil.readMsg("HJSJ", processHandler);
            }
        }

        public void setCanRun(boolean canRun) {
            this.canRun = canRun;
        }

    }

    private void chickStatus() {


    }

    @PreDestroy
    private void destroy() {
        if (processOrderSysThread != null) {
            processOrderSysThread.setCanRun(false);
        }
    }
}
