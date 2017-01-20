package com.zk.mina;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Service("serverHandler")
public class ServerHandler extends IoHandlerAdapter {

    private final int IDLE = 300;

    private static Logger logger = LoggerFactory.getLogger(ServerHandler.class);

    public static Set<IoSession> sessions = Collections.synchronizedSet(new HashSet<IoSession>());

    /**
     *
     */
    public ServerHandler() {

    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause)
            throws Exception {
        session.close(false);
    }

    @Override
    public void sessionCreated(IoSession session) {
        sessions.add(session);
    }

    @Override
    public void messageReceived(IoSession session, Object message)
            throws Exception {
        ImageRequest request = (ImageRequest) message;
        ServerHandler.byte2File(request.getImageByte(), "F://client//", "1.jpg");
        session.setAttribute("type", message);
        String remoteAddress = ((InetSocketAddress) session.getRemoteAddress()).getAddress().getHostAddress();
        session.setAttribute("ip", remoteAddress);
        System.out.println(request.getLength());
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        sessions.remove(session);
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status)
            throws Exception {
        session.close(false);
    }

    //
    @Override
    public void messageSent(IoSession session, Object message) throws Exception {
        logger.debug("messageSent.");
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        session.getConfig().setIdleTime(IdleStatus.BOTH_IDLE, IDLE);
    }

    public static void byte2File(byte[] buf, String filePath, String fileName)
    {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        try
        {
            File dir = new File(filePath);
            if (!dir.exists())
            {
                dir.mkdirs();
            }
            file = new File(filePath + File.separator + fileName);
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(buf);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (bos != null)
            {
                try
                {
                    bos.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            if (fos != null)
            {
                try
                {
                    fos.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
}
