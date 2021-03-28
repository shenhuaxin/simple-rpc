package org.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;
import org.core.MiniRpcRequest;
import org.protocol.MsgHeader;
import org.protocol.SimpleRpcProtocol;
import org.serialization.RpcSerialization;
import org.serialization.SerializationFactory;

@Slf4j
public class SimpleEncoder extends MessageToByteEncoder<SimpleRpcProtocol<Object>> {

    @Override
    protected void encode(ChannelHandlerContext ctx, SimpleRpcProtocol<Object> msg, ByteBuf byteBuf) throws Exception {
        MsgHeader header = msg.getHeader();
        byteBuf.writeShort(header.getMagic());
        byteBuf.writeByte(header.getVersion());
        byteBuf.writeByte(header.getSerialization());
        byteBuf.writeByte(header.getMsgType());
        byteBuf.writeByte(header.getStatus());
        byteBuf.writeLong(header.getRequestId());
        RpcSerialization rpcSerialization = SerializationFactory.getRpcSerialization(header.getSerialization());
        byte[] body = rpcSerialization.serialize(msg.getBody());

        byteBuf.writeInt(body.length);
        byteBuf.writeBytes(body);
        System.out.println("数据编码");
    }
}
