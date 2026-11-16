package com.hotelio.booking.config;

import com.hotelio.booking.controller.BookingGrpcController;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/** Поднимает gRPC-сервер вместе с приложением. Reflection включён, чтобы можно было звать сервис через grpcurl. */
@Component
public class GrpcServerConfig implements SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(GrpcServerConfig.class);

    private final Server server;
    private volatile boolean running;

    public GrpcServerConfig(BookingGrpcController bookingGrpcController, @Value("${grpc.port}") int port) {
        this.server = ServerBuilder.forPort(port)
                .addService(bookingGrpcController)
                .addService(ProtoReflectionService.newInstance())
                .build();
    }

    @Override
    public void start() {
        try {
            server.start();
            running = true;
            log.info("gRPC server started on port {}", server.getPort());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot start gRPC server", e);
        }
    }

    @Override
    public void stop() {
        try {
            server.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}
