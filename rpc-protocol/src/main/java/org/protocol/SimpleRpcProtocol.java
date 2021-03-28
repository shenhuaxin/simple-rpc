package org.protocol;


import lombok.Data;

import java.io.Serializable;

@Data
public class SimpleRpcProtocol<T> implements Serializable{

        private MsgHeader header;
        private T body;
}
