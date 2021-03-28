package org.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.core.MiniRpcRequest;
import org.core.MiniRpcResponse;
import org.protocol.MsgHeader;
import org.protocol.MsgType;
import org.protocol.ProtocolConstants;
import org.protocol.SimpleRpcProtocol;
import org.serialization.RpcSerialization;
import org.serialization.SerializationFactory;

import java.util.List;

public class SimpleDecoder extends ByteToMessageDecoder {
    /*
    +---------------------------------------------------------------+
    | 魔数 2byte | 协议版本号 1byte | 序列化算法 1byte | 报文类型 1byte  |
    +---------------------------------------------------------------+
    | 状态 1byte |        消息 ID 8byte     |      数据长度 4byte     |
    +---------------------------------------------------------------+
    */
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) throws Exception {
        System.out.println("接收到数据， 进行解码");
        if (in.readableBytes() < ProtocolConstants.HEADER_TOTAL_LEN) {
            return;
        }
        in.markReaderIndex();
        short magic = in.readShort();
        if (magic != ProtocolConstants.MAGIC) {
            throw new IllegalArgumentException("magic num is illegal" + magic);
        }

        byte version = in.readByte();
        byte serialization = in.readByte();
        byte messageType = in.readByte();
        byte status = in.readByte();
        long requestId = in.readLong();
        int bodyLen = in.readInt();
        if (in.readableBytes() < bodyLen) {
            in.resetReaderIndex();
            return;
        }
        byte[] data = new byte[bodyLen];
        in.readBytes(data);

        MsgType msgType = MsgType.findByType(messageType);
        if (msgType == null) {
            return;
        }

        MsgHeader header = new MsgHeader();
        header.setMagic(magic);
        header.setVersion(version);
        header.setSerialization(serialization);
        header.setStatus(status);
        header.setRequestId(requestId);
        header.setMsgType(messageType);
        header.setMsgLen(bodyLen);

        RpcSerialization rpcSerialization = SerializationFactory.getRpcSerialization(serialization);

        switch (msgType) {
            case REQUEST:
                System.out.println("解码请求");
                MiniRpcRequest request = rpcSerialization.deserialize(data, MiniRpcRequest.class);
                if (request != null) {
                    SimpleRpcProtocol<MiniRpcRequest> protocol = new SimpleRpcProtocol<>();
                    protocol.setHeader(header);
                    protocol.setBody(request);
                    out.add(protocol);
                }
                break;
            case RESPONSE:
                System.out.println("解码响应");
                MiniRpcResponse response = rpcSerialization.deserialize(data, MiniRpcResponse.class);
                if (response != null) {
                    SimpleRpcProtocol<MiniRpcResponse> protocol = new SimpleRpcProtocol<>();
                    protocol.setHeader(header);
                    protocol.setBody(response);
                    out.add(protocol);
                }
                break;
            case HEARTBEAT:
                // TODO
                break;
        }
    }


    
}
