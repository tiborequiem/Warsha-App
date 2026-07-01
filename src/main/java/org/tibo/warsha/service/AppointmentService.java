package org.tibo.warsha.service;

import org.tibo.warsha.model.Appointment;
import org.tibo.warsha.model.User;
import org.tibo.warsha.repository.AppointmentRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
<<<<<<<< HEAD:src/main/java/org/tibo/warsha/service/AppointmentService.java
import java.time.LocalDate;
========

import java.math.BigDecimal;
>>>>>>>> f36b9b0d0ac63db333d4f5e45471654a5b7ed634:Warsha/src/main/java/org/tibo/warsha/service/AppointmentService.java
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

<<<<<<<< HEAD:src/main/java/org/tibo/warsha/service/AppointmentService.java
    // Creates a new appointment after validating the time, users, and roles
========
    // ── existing booking (unchanged) ──

>>>>>>>> f36b9b0d0ac63db333d4f5e45471654a5b7ed634:Warsha/src/main/java/org/tibo/warsha/service/AppointmentService.java
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

<<<<<<<< HEAD:src/main/java/org/tibo/warsha/service/AppointmentService.java
    // Either participant (customer or worker) can cancel, but not after completion
========
    // ── existing queries (unchanged) ──

    @Transactional(readOnly = true)
    public List<Appointment> findByCustomer(User customer) {
        return appointmentRepository.findByCustomerOrderByAppointmentDateDesc(customer);
    }

    @Transactional(readOnly = true)
    public List<Appointment> findByWorker(User worker) {
        return appointmentRepository.findByWorkerOrderByAppointmentDateDesc(worker);
    }

    @Transactional(readOnly = true)
    public Optional<Appointment> findById(Long id) {
        return appointmentRepository.findById(id);
    }

    // ── existing cancel (unchanged) ──

>>>>>>>> f36b9b0d0ac63db333d4f5e45471654a5b7ed634:Warsha/src/main/java/org/tibo/warsha/service/AppointmentService.java
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

<<<<<<<< HEAD:src/main/java/org/tibo/warsha/service/AppointmentService.java
    // Only the assigned worker can move a PENDING appointment to CONFIRMED
========
    // ← REPLACED: confirmAppointment → acceptAppointment (status ACCEPTED not CONFIRMED)

>>>>>>>> f36b9b0d0ac63db333d4f5e45471654a5b7ed634:Warsha/src/main/java/org/tibo/warsha/service/AppointmentService.java
    @Transactional
    public void acceptAppointment(Long appointmentId, Long workerId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found."));

        if (!appointment.getWorker().getId().equals(workerId)) {
            throw new IllegalArgumentException("Only the assigned worker can accept this appointment.");
        }

        if (appointment.getStatus() != Appointment.Status.PENDING) {
            throw new IllegalArgumentException("Only pending appointments can be accepted.");
        }

        appointment.setStatus(Appointment.Status.ACCEPTED);  // ← was CONFIRMED
        appointmentRepository.save(appointment);
    }

<<<<<<<< HEAD:src/main/java/org/tibo/warsha/service/AppointmentService.java
    // Marks a CONFIRMED appointment as COMPLETED and triggers automatic payment creation
========
    // ← ADDED: rejectAppointment — new terminal state for declined requests

    @Transactional
    public void rejectAppointment(Long appointmentId, Long workerId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found."));

        if (!appointment.getWorker().getId().equals(workerId)) {
            throw new IllegalArgumentException("Only the assigned worker can reject this appointment.");
        }

        if (appointment.getStatus() != Appointment.Status.PENDING) {
            throw new IllegalArgumentException("Only pending appointments can be rejected.");
        }

        appointment.setStatus(Appointment.Status.REJECTED);
        appointmentRepository.save(appointment);
    }

    // ← UPDATED: completeAppointment — checks ACCEPTED, triggers earnings

>>>>>>>> f36b9b0d0ac63db333d4f5e45471654a5b7ed634:Warsha/src/main/java/org/tibo/warsha/service/AppointmentService.java
    @Transactional
    public void completeAppointment(Long appointmentId, Long workerId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found."));

        if (!appointment.getWorker().getId().equals(workerId)) {
            throw new IllegalArgumentException("Only the assigned worker can complete this appointment.");
        }

        // ← UPDATED: check ACCEPTED (was CONFIRMED)
        if (appointment.getStatus() != Appointment.Status.ACCEPTED) {
            throw new IllegalArgumentException("Only accepted appointments can be marked as completed.");
        }

        appointment.setStatus(Appointment.Status.COMPLETED);
<<<<<<<< HEAD:src/main/java/org/tibo/warsha/service/AppointmentService.java
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
========
        appointmentRepository.save(appointment);

        // ← ADDED: earnings calculation — atomic within same transaction
        if (appointment.getPrice() != null) {
            User worker = appointment.getWorker();

            BigDecimal currentEarnings = worker.getTotalEarnings() != null
                    ? worker.getTotalEarnings()
                    : BigDecimal.ZERO;

            worker.setTotalEarnings(currentEarnings.add(appointment.getPrice()));
            userService.save(worker);  // persist within same transaction
        }
>>>>>>>> f36b9b0d0ac63db333d4f5e45471654a5b7ed634:Warsha/src/main/java/org/tibo/warsha/service/AppointmentService.java
    }

    // ← ADDED: availability check — prevents double-booking

    @Transactional(readOnly = true)
    public boolean isWorkerAvailable(Long workerId, LocalDateTime dateTime) {
        boolean hasConflict = appointmentRepository.existsAcceptedAppointmentAtDateTime(workerId, dateTime);
        return !hasConflict;  // true = available, false = busy
    }

    // ← ADDED: dashboard query methods (all read-only)

    @Transactional(readOnly = true)
    public long getPendingRequestsCount(User worker) {
        return appointmentRepository.countByWorkerAndStatus(worker, Appointment.Status.PENDING);
    }

    @Transactional(readOnly = true)
    public List<Appointment> getTodaysAppointments(User worker) {
        // uses database-level date filtering — efficient
        return appointmentRepository.findTodayAppointmentsByWorkerAndStatus(
                worker, Appointment.Status.ACCEPTED);
    }

    @Transactional(readOnly = true)
    public List<Appointment> getCompletedAppointments(User worker) {
        return appointmentRepository.findByWorkerAndStatusOrderByAppointmentDateDesc(
                worker, Appointment.Status.COMPLETED);
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateTotalEarnings(User worker) {
        List<Appointment> completed = getCompletedAppointments(worker);
        return completed.stream()
                .map(a -> a.getPrice() != null ? a.getPrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}