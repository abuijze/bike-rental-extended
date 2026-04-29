package io.axoniq.demo.bikerental.payment;

import io.axoniq.demo.bikerental.coreapi.payment.PaymentStatus;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentStatusRepository extends CrudRepository<PaymentStatus, String> {

    List<PaymentStatus> findAllByStatus(PaymentStatus.Status status);

    Optional<PaymentStatus> findByReferenceAndStatus(String reference, PaymentStatus.Status status);

    @Modifying
    @Transactional(propagation = Propagation.MANDATORY)
    @Query("UPDATE PaymentStatus p SET p.status = :status WHERE p.id = :id")
    void updateStatus(String id, PaymentStatus.Status status);
}
