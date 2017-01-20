package com.zk.mina;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * 客户端解码返回
 * Created by Ken on 2016/11/4.
 */
public class ImageResponseDecoder extends CumulativeProtocolDecoder {
    private static final String DECODER_STATE_KEY = ImageResponseDecoder.class.getName() + ".STATE";//存储decoding的进度

    private static class DecoderState {
        int length;
        byte[] image1;
    }

    protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {

        DecoderState decoderState = (DecoderState) session.getAttribute(DECODER_STATE_KEY);
        if (decoderState == null) {
            decoderState = new DecoderState();
            session.setAttribute(DECODER_STATE_KEY, decoderState);
        }

        if (decoderState.length == 0) {
            decoderState.length = in.getInt();
        }if (in.remaining() >= decoderState.length) {//这个方法对于有长度前缀的message解析很好用
            decoderState.image1 = new byte[decoderState.length];
            in.get(decoderState.image1);
            ImageRequest imageRequest = new ImageRequest(decoderState.length, decoderState.image1);
            out.write(imageRequest);
            decoderState.image1 = null;
            return true;
        } else {
            return false;
        }
    }
}
