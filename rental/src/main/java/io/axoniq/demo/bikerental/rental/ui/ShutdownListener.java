package io.axoniq.demo.bikerental.rental.ui;

import org.axonframework.common.lifecycle.ShutdownInProgressException;
import org.reactivestreams.Publisher;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Listener that allows wrapping of a {@link Publisher} to have it emit an error when the application
 * context shuts down.
 * <p>
 * This helps prevent hanging Server Sent Events requests when the application is performing a graceful shutdown.
 * <p>
 * Native support for automatic closing is planned for Axon Framework 5.2.0.
 */
@Component
public class ShutdownListener implements SmartLifecycle {

    private final Flux<?> shutdownFlux;
    private final AtomicReference<FluxSink<?>> sink = new AtomicReference<>();
    private volatile boolean running = false;

    public ShutdownListener() {
        this.shutdownFlux = Flux.create(sink::set);
    }

    @Override
    public void start() {
        this.running = true;
    }

    @Override
    public void stop() {
        sink.get().error(new ShutdownInProgressException());
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @SuppressWarnings("unchecked")
    public <T> Flux<T> closedOnShutdown(Publisher<T> original) {
        return this.shutdownFlux.map(e -> (T) e).mergeWith(original);
    }

    @Override
    public int getPhase() {
        return SmartLifecycle.DEFAULT_PHASE - 1024;
    }
}
