package org.tibo.warsha.repository;

import org.tibo.warsha.model.Appointment;
import org.tibo.warsha.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByCustomerOrderByAppointmentDateDesc(User customer);

    List<Appointment> findByWorkerOrderByAppointmentDateDesc(User worker);

    List<Appointment> findByCustomerAndStatus(User customer, Appointment.Status status);

    List<Appointment> findByWorkerAndStatus(User worker, Appointment.Status status);

    long countByWorkerAndStatus(User worker, Appointment.Status status);
}
