package org.tibo.warsha.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tibo.warsha.model.Appointment;
import org.tibo.warsha.model.Payment;
import org.tibo.warsha.model.User;
import org.tibo.warsha.repository.PaymentRepository;
import org.tibo.warsha.repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;

// Creates payment records on appointment completion and provides payment history queries
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    public PaymentService(PaymentRepository paymentRepository,
                          UserRepository userRepository) {
        this.paymentRepository = paymentRepository;
        this.userRepository    = userRepository;
    }

    // Called by AppointmentService when a job is marked COMPLETED — creates the payment record
    @Transactional
    public void processAutomaticPayment(Appointment appointment) {

        // Skip if a payment already exists for this appointment
        if (paymentRepository.existsByAppointment(appointment)) {
            return;
        }

        User worker = appointment.getWorker();

        // Use the worker's set price, or fall back to zero if not configured
        BigDecimal amount = (worker.getBasePrice() != null)
                ? worker.getBasePrice()
                : BigDecimal.ZERO;

        Payment payment = new Payment();
        payment.setAppointment(appointment);
        payment.setAmount(amount);
        payment.setMethod(Payment.PaymentMethod.CASH);
        payment.setStatus(Payment.PaymentStatus.COMPLETED);

        paymentRepository.save(payment);

        // Add this payment to the worker's running total earnings
        BigDecimal current = (worker.getTotalEarnings() != null)
                ? worker.getTotalEarnings()
                : BigDecimal.ZERO;
        worker.setTotalEarnings(current.add(amount));
        userRepository.save(worker);
    }

    // Returns all payments for the customer's completed appointments, newest first
    @Transactional(readOnly = true)
    public List<Payment> getPaymentHistoryForCustomer(User customer) {
        return paymentRepository.findByAppointmentCustomerOrderByCreatedAtDesc(customer);
    }
}
