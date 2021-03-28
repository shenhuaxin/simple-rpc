package org.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class MiniRpcRequestHolder {



    public final static AtomicLong REQUEST_ID_GEN = new AtomicLong(0);

    public static Long getRequestId() {
        return REQUEST_ID_GEN.incrementAndGet();
    }

    public static final Map<Long, RpcFuture<MiniRpcResponse>> REQUEST_MAP = new ConcurrentHashMap<>();



}
