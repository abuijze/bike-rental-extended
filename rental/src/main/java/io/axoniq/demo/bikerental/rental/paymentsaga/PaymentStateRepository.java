package io.axoniq.demo.bikerental.rental.paymentsaga;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentStateRepository extends JpaRepository<PaymentState, String> {

    List<PaymentState> findAllByTimestampLessThanAndStatusIn(long timestamp, PaymentState.Status... status);

    @Modifying
    @Query("UPDATE PaymentState p SET p.status = :status WHERE p.paymentReference = :paymentReference")
    void updateStatus(String paymentReference, PaymentState.Status status);

    @Modifying
    @Query("UPDATE PaymentState p SET p.status = :status, p.paymentId = :paymentId WHERE p.paymentReference = :paymentReference")
    void updateStatusAndPaymentId(String paymentReference, String paymentId, PaymentState.Status status);

}
