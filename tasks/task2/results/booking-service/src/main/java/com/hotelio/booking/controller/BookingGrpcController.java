package com.hotelio.booking.controller;

import com.hotelio.booking.entity.Booking;
import com.hotelio.booking.service.BookingService;
import com.hotelio.proto.booking.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class BookingGrpcController extends BookingServiceGrpc.BookingServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(BookingGrpcController.class);

    private final BookingService bookingService;

    public BookingGrpcController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @Override
    public void createBooking(BookingRequest request, StreamObserver<BookingResponse> response) {
        try {
            Booking booking = bookingService.create(request.getUserId(), request.getHotelId(), request.getPromoCode());
            response.onNext(toProto(booking));
            response.onCompleted();
        } catch (IllegalArgumentException e) {
            log.warn("Booking rejected: {}", e.getMessage());
            response.onError(Status.FAILED_PRECONDITION.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            log.error("Booking failed", e);
            response.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void listBookings(BookingListRequest request, StreamObserver<BookingListResponse> response) {
        try {
            BookingListResponse.Builder list = BookingListResponse.newBuilder();
            bookingService.list(request.getUserId()).forEach(b -> list.addBookings(toProto(b)));
            response.onNext(list.build());
            response.onCompleted();
        } catch (Exception e) {
            log.error("List bookings failed", e);
            response.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    private static BookingResponse toProto(Booking b) {
        return BookingResponse.newBuilder()
                .setId(String.valueOf(b.getId()))
                .setUserId(nvl(b.getUserId()))
                .setHotelId(nvl(b.getHotelId()))
                .setPromoCode(nvl(b.getPromoCode()))
                .setDiscountPercent(b.getDiscountPercent() == null ? 0.0 : b.getDiscountPercent())
                .setPrice(b.getPrice())
                .setCreatedAt(b.getCreatedAt() == null ? "" : b.getCreatedAt().toString())
                .build();
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }
}
