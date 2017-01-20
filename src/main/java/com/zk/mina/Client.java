package com.zk.mina;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.ReadFuture;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.InetSocketAddress;

/**
 * Created by Ken on 2016/11/3.
 */
public class Client {
    public static void main(String args[]) throws IOException {
        NioSocketConnector connector = new NioSocketConnector();
        connector.getFilterChain().addLast("logger", new LoggingFilter());
        connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new ImageCodecFactory()));
        connector.setConnectTimeout(1);
        connector.setHandler(new ServerHandler());//
        ConnectFuture cf = connector.connect(
                new InetSocketAddress("127.0.0.1", 10086));//
        cf.awaitUninterruptibly();//
        byte[] bb = File2byte("E://sy3f.jpg");
        ImageRequest request = new ImageRequest(bb.length,bb);
        System.out.println(bb.length);
        cf.getSession().write(request);//

        //cf.getSession().write("quit");//
//        cf.getSession().close(true);
//        cf.getSession().getCloseFuture().awaitUninterruptibly();//
//        connector.dispose();
    }

    public static byte[] File2byte(String filePath)
    {
        byte[] buffer = null;
        try
        {
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int n;
            while ((n = fis.read(b)) != -1)
            {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return buffer;
    }
}
