package io.axoniq.demo.bikerental.rental.paymentsaga;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentStateRepository extends JpaRepository<PaymentState, String> {

    List<PaymentState> findAllByTimestampLessThanAndStatusIn(long timestamp, PaymentState.Status... status);

}
