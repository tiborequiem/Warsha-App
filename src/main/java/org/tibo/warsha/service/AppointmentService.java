package org.tibo.warsha.service;

import org.tibo.warsha.model.Appointment;
import org.tibo.warsha.model.User;
import org.tibo.warsha.repository.AppointmentRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// Manages the full lifecycle of appointments: booking, confirming, completing, and cancelling
@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserService userService;
    private final PaymentService paymentService;

    // @Lazy on PaymentService breaks the circular dependency between these two services
    public AppointmentService(AppointmentRepository appointmentRepository,
                              UserService userService,
                              @Lazy PaymentService paymentService) {
        this.appointmentRepository = appointmentRepository;
        this.userService           = userService;
        this.paymentService        = paymentService;
    }

    // Creates a new appointment after validating the time, users, and roles
    @Transactional
    public Appointment bookAppointment(Long customerId, Long workerId, String serviceType,
                                       LocalDateTime appointmentDate, String notes) {
        if (appointmentDate.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new IllegalArgumentException("Appointment must be at least 2 hours in the future.");
        }

        User customer = userService.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found."));
        User worker = userService.findById(workerId)
                .orElseThrow(() -> new IllegalArgumentException("Worker not found."));

        if (!worker.getRole().equals(User.Role.WORKER)) {
            throw new IllegalArgumentException("Selected user is not a worker.");
        }

        if (customerId.equals(workerId)) {
            throw new IllegalArgumentException("You cannot book an appointment with yourself.");
        }

        Appointment appointment = new Appointment();
        appointment.setCustomer(customer);
        appointment.setWorker(worker);
        appointment.setServiceType(serviceType);
        appointment.setAppointmentDate(appointmentDate);
        appointment.setNotes(notes);
        appointment.setStatus(Appointment.Status.PENDING);

        return appointmentRepository.save(appointment);
    }

    // Either participant (customer or worker) can cancel, but not after completion
    @Transactional
    public void cancelAppointment(Long appointmentId, Long userId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found."));

        if (!appointment.getCustomer().getId().equals(userId) &&
            !appointment.getWorker().getId().equals(userId)) {
            throw new IllegalArgumentException("You are not authorized to cancel this appointment.");
        }

        if (appointment.getStatus() == Appointment.Status.COMPLETED) {
            throw new IllegalArgumentException("Cannot cancel a completed appointment.");
        }

        appointment.setStatus(Appointment.Status.CANCELLED);
        appointmentRepository.save(appointment);
    }

    // Only the assigned worker can move a PENDING appointment to CONFIRMED
    @Transactional
    public void confirmAppointment(Long appointmentId, Long workerId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found."));

        if (!appointment.getWorker().getId().equals(workerId)) {
            throw new IllegalArgumentException("Only the assigned worker can confirm this appointment.");
        }

        if (appointment.getStatus() != Appointment.Status.PENDING) {
            throw new IllegalArgumentException("Only pending appointments can be confirmed.");
        }

        appointment.setStatus(Appointment.Status.CONFIRMED);
        appointmentRepository.save(appointment);
    }

    // Marks a CONFIRMED appointment as COMPLETED and triggers automatic payment creation
    @Transactional
    public void completeAppointment(Long appointmentId, Long workerId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found."));

        if (!appointment.getWorker().getId().equals(workerId)) {
            throw new IllegalArgumentException("Only the assigned worker can complete this appointment.");
        }

        if (appointment.getStatus() != Appointment.Status.CONFIRMED) {
            throw new IllegalArgumentException("Only confirmed appointments can be marked as completed.");
        }

        appointment.setStatus(Appointment.Status.COMPLETED);
        Appointment saved = appointmentRepository.save(appointment);

        // Create the payment record now that the appointment is complete
        paymentService.processAutomaticPayment(saved);
    }

    // Worker rejection re-uses the cancel logic
    @Transactional
    public void rejectAppointment(Long appointmentId, Long workerId) {
        cancelAppointment(appointmentId, workerId);
    }

    // Returns all appointments for a customer, sorted newest first
    @Transactional(readOnly = true)
    public List<Appointment> findByCustomer(User customer) {
        return appointmentRepository.findByCustomerOrderByAppointmentDateDesc(customer);
    }

    // Returns all appointments assigned to a worker, sorted newest first
    @Transactional(readOnly = true)
    public List<Appointment> findByWorker(User worker) {
        return appointmentRepository.findByWorkerOrderByAppointmentDateDesc(worker);
    }

    // Looks up a single appointment by ID
    @Transactional(readOnly = true)
    public Optional<Appointment> findById(Long id) {
        return appointmentRepository.findById(id);
    }

    // Returns only COMPLETED appointments for a worker — used on the earnings page
    @Transactional(readOnly = true)
    public List<Appointment> getCompletedAppointments(User worker) {
        return appointmentRepository.findByWorkerAndStatus(worker, Appointment.Status.COMPLETED);
    }

    // Returns the count of PENDING requests — used for the dashboard notification badge
    @Transactional(readOnly = true)
    public long getPendingRequestsCount(User worker) {
        return appointmentRepository.countByWorkerAndStatus(worker, Appointment.Status.PENDING);
    }

    // Filters all worker appointments down to those scheduled for today
    @Transactional(readOnly = true)
    public List<Appointment> getTodaysAppointments(User worker) {
        LocalDate today = LocalDate.now();
        return findByWorker(worker).stream()
                .filter(a -> a.getAppointmentDate().toLocalDate().equals(today))
                .toList();
    }
}
