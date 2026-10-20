package org.tibo.warsha.repository;

import org.tibo.warsha.model.Appointment;
import org.tibo.warsha.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // ── existing methods (unchanged) ──

    List<Appointment> findByCustomerOrderByAppointmentDateDesc(User customer);

    List<Appointment> findByWorkerOrderByAppointmentDateDesc(User worker);

    List<Appointment> findByCustomerAndStatus(User customer, Appointment.Status status);

    List<Appointment> findByWorkerAndStatus(User worker, Appointment.Status status);

    long countByWorkerAndStatus(User worker, Appointment.Status status);

    // Today's appointments for dashboard
    List<Appointment> findByWorkerAndStatusAndAppointmentDateBetweenOrderByAppointmentDateAsc(
            User worker, Appointment.Status status, LocalDateTime start, LocalDateTime end);

    default List<Appointment> findTodayAppointmentsByWorkerAndStatus(User worker, Appointment.Status status) {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        return findByWorkerAndStatusAndAppointmentDateBetweenOrderByAppointmentDateAsc(
                worker, status, startOfDay, endOfDay);
    }

    // Completed appointments for earnings history (newest first)
    List<Appointment> findByWorkerAndStatusOrderByAppointmentDateDesc(
            User worker, Appointment.Status status);

    // Core availability check — prevents double-booking
    boolean existsByWorkerIdAndAppointmentDateAndStatusIn(
            Long workerId, LocalDateTime appointmentDate, List<Appointment.Status> statuses);

    default boolean existsAcceptedAppointmentAtDateTime(Long workerId, LocalDateTime dateTime) {
        return existsByWorkerIdAndAppointmentDateAndStatusIn(workerId, dateTime,
                List.of(Appointment.Status.ACCEPTED, Appointment.Status.CONFIRMED));
    }
}