package org.tibo.warsha.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.tibo.warsha.model.Appointment;
import org.tibo.warsha.model.Payment;
import org.tibo.warsha.model.User;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Fetch the payment linked to a specific appointment
    Optional<Payment> findByAppointment(Appointment appointment);

    // Returns all payments for a customer, newest first — used for payment history page
    List<Payment> findByAppointmentCustomerOrderByCreatedAtDesc(User customer);

    // Idempotency check — prevents creating a duplicate payment for the same appointment
    boolean existsByAppointment(Appointment appointment);
}
