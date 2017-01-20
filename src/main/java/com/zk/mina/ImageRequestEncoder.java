package com.zk.mina;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 客户端加密发送的请求
 * Created by Ken on 2016/11/4.
 */
public class ImageRequestEncoder implements ProtocolEncoder {

    @Override
    public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
        if(message instanceof ImageRequest){
            ImageRequest request = (ImageRequest) message;
            int capacity = 4 + request.getImageByte().length + 8;
            IoBuffer buffer = IoBuffer.allocate(capacity, false);
            buffer.setAutoExpand(true);//设置自动扩充
            buffer.putInt(request.getImageByte().length);
            buffer.put(request.getImageByte());
            buffer.flip();
            out.write(buffer);
        }
    }

    @Override
    public void dispose(IoSession session) throws Exception {
        // nothing to dispose
    }
}
