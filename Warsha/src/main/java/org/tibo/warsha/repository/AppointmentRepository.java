package org.tibo.warsha.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tibo.warsha.model.Appointment;
import org.tibo.warsha.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByCustomerOrderByAppointmentDateDesc(User customer);

    List<Appointment> findByWorkerOrderByAppointmentDateDesc(User worker);

    List<Appointment> findByCustomerAndStatus(User customer, Appointment.Status status);

    List<Appointment> findByWorkerAndStatus(User worker, Appointment.Status status);

    long countByWorkerAndStatus(User worker, Appointment.Status status);
    // Dashboard: Today's appointments for worker
    @Query
            ("SELECT a FROM Appointment a WHERE a.worker = :worker " +
            "AND a.status = :status " +
            "AND DATE(a.appointmentDate) = CURRENT_DATE " +
            "ORDER BY a.appointmentDate ASC")
    List<Appointment> findTodayAppointmentsByWorkerAndStatus(
            @Param("worker") User worker,
            @Param("status") Appointment.Status status);
    // Earnings: All completed appointments for worker
    List<Appointment> findByWorkerAndStatusOrderByAppointmentDateDesc(
            User worker, Appointment.Status status);
    // Availability Check: Check if worker has ACCEPTED appointment at same date/time
    @Query
            ("SELECT COUNT(a) > 0 FROM Appointment a " +
            "WHERE a.worker.id = :workerId " +
            "AND a.status = 'ACCEPTED' " +
            "AND a.appointmentDate = :dateTime")
    boolean existsAcceptedAppointmentAtDateTime(
            @Param("workerId") Long workerId,
            @Param("dateTime") LocalDateTime dateTime);
}

