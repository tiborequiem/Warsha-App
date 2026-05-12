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

    // ← ADDED: today's appointments for dashboard (database-level filter)
    @Query("SELECT a FROM Appointment a WHERE a.worker = :worker " +
            "AND a.status = :status " +
            "AND DATE(a.appointmentDate) = CURRENT_DATE " +
            "ORDER BY a.appointmentDate ASC")
    List<Appointment> findTodayAppointmentsByWorkerAndStatus(
            @Param("worker") User worker,
            @Param("status") Appointment.Status status);

    // ← ADDED: completed appointments for earnings history (newest first)
    List<Appointment> findByWorkerAndStatusOrderByAppointmentDateDesc(
            User worker, Appointment.Status status);

    // ← ADDED: core availability check — prevents double-booking
    @Query("SELECT COUNT(a) > 0 FROM Appointment a " +
            "WHERE a.worker.id = :workerId " +
            "AND a.status = 'ACCEPTED' " +
            "AND a.appointmentDate = :dateTime")
    boolean existsAcceptedAppointmentAtDateTime(
            @Param("workerId") Long workerId,
            @Param("dateTime") LocalDateTime dateTime);
}