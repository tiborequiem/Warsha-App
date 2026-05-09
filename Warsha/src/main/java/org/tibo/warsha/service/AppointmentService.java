package org.tibo.warsha.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.tibo.warsha.model.Appointment;
import org.tibo.warsha.model.User;
import org.tibo.warsha.repository.AppointmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserService userService;

    public AppointmentService(AppointmentRepository appointmentRepository, UserService userService) {
        this.appointmentRepository = appointmentRepository;
        this.userService = userService;
    }

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
    @Transactional
    public void rejectAppointment(Long appointmentId, Long workerId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found."));

        // Authorization: Only assigned worker can reject
        if (!appointment.getWorker().getId().equals(workerId)) {
            throw new IllegalArgumentException("Only the assigned worker can reject this appointment.");
        }

        // Business rule: Only PENDING appointments can be rejected
        if (appointment.getStatus() != Appointment.Status.PENDING) {
            throw new IllegalArgumentException("Only pending appointments can be rejected.");
        }

        appointment.setStatus(Appointment.Status.REJECTED);
        appointmentRepository.save(appointment);
    }


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
        appointmentRepository.save(appointment);

    }
    if (appointment.getPrice() != null) {
        User worker = appointment.getWorker();
        BigDecimal currentEarnings = worker.getTotalEarnings() != null
                ? worker.getTotalEarnings()
                : BigDecimal.ZERO;
        worker.setTotalEarnings(currentEarnings.add(appointment.getPrice()));
        userService.updateWorkerEarnings(worker);
    }
    @Transactional(readOnly = true)//count of pending appointment
    public long getPendingRequestsCount(User worker) {
        return appointmentRepository.countByWorkerAndStatus(worker, Appointment.Status.PENDING);
    }
    @Transactional(readOnly = true)//today accepted appointments
    public List<Appointment> getTodaysAppointments(User worker) {
        return appointmentRepository.findTodayAppointmentsByWorkerAndStatus(
                worker, Appointment.Status.ACCEPTED);
    }
    @Transactional(readOnly = true)//completed appointments
    public List<Appointment> getCompletedAppointments(User worker) {
        return appointmentRepository.findByWorkerAndStatusOrderByAppointmentDateDesc(
                worker, Appointment.Status.COMPLETED);
    }
    @Transactional(readOnly = true)//total earning
    public BigDecimal calculateTotalEarnings(User worker) {
        List<Appointment> completed = getCompletedAppointments(worker);
        return completed.stream()
                .map(a -> a.getPrice() != null ? a.getPrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    private User getAuthenticatedWorker(UserDetails principal) {
        return userService.findByUsername(principal.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated worker not found"));
    }
}
