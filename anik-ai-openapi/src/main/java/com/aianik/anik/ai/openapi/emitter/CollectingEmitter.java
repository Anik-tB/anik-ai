package com.aianik.anik.ai.openapi.emitter;

import com.aianik.anik.ai.common.execption.AnikAiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Collected Emitter, used in synchronous conversation mode
 * <p>
 * Collect all chunks of streaming chat into buffer,
 * Block and wait for completion via CountDownLatch, returning the complete text.
 *
 * @author openanik
 * @date 2026-04-24
 */
@Slf4j
public class CollectingEmitter extends ResponseBodyEmitter {

    private final StringBuilder buffer = new StringBuilder();
    private final CountDownLatch latch = new CountDownLatch(1);
    private volatile Throwable error;

    public CollectingEmitter() {
        super(0L);
    }

    @Override
    public void send(Object data) throws IOException {
        buffer.append(data);
    }

    @Override
    public void send(Object data, MediaType mediaType) throws IOException {
        buffer.append(data);
    }

    @Override
    public void complete() {
        latch.countDown();
    }

    @Override
    public void completeWithError(Throwable ex) {
        this.error = ex;
        latch.countDown();
    }

    public String awaitAndGetFullText(long timeoutMs) throws InterruptedException, TimeoutException {
        if (!latch.await(timeoutMs, TimeUnit.MILLISECONDS)) {
            throw new TimeoutException("Conversation response timeout");
        }
        if (error != null) {
            throw new AnikAiException("Dialogue execution failed: " + error.getMessage(), error);
        }
        return buffer.toString();
    }
}
