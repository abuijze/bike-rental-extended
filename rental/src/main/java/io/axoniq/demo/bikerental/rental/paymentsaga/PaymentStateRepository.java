package io.axoniq.demo.bikerental.rental.paymentsaga;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentStateRepository extends JpaRepository<PaymentState, String> {

    List<PaymentState> findAllByTimestampLessThanAndStatusIn(long timestamp, PaymentState.Status... status);

    @Modifying
    @Transactional(propagation = Propagation.MANDATORY)
    @Query("UPDATE PaymentState p SET p.status = :status WHERE p.paymentReference = :paymentReference")
    void updateStatus(String paymentReference, PaymentState.Status status);

    @Modifying
    @Transactional(propagation = Propagation.MANDATORY)
    @Query("UPDATE PaymentState p SET p.status = :status, p.paymentId = :paymentId WHERE p.paymentReference = :paymentReference")
    void updateStatusAndPaymentId(String paymentReference, String paymentId, PaymentState.Status status);

    @Query("SELECT MIN(p.timestamp) FROM PaymentState p WHERE p.status IN :statuses")
    Optional<Long> findEarliestActiveTimestamp(@Param("statuses") List<PaymentState.Status> statuses);
}
