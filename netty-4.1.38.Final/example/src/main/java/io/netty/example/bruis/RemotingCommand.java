package io.netty.example.bruis;

import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;

/**
 * @author lhy
 * @date 2021/8/19
 */
public class RemotingCommand {
    private Integer code;       // 请求码
    private byte[] body;        // 请求内容

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public static RemotingCommand decode(final ByteBuffer byteBuffer) {
        int limit = byteBuffer.limit();
        byte[] body = new byte[limit];
        byteBuffer.get(body);
        RemotingCommand remotingCommand = new RemotingCommand();
        remotingCommand.setBody(body);
        return remotingCommand;
    }
}
