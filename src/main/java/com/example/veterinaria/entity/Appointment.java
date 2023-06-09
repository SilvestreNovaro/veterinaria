package com.example.veterinaria.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Appointment")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime appointmentDateTime;

    private String appointmentReason;

    private String appointmentNotes;

    @ManyToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "customer_id")
    @JsonIgnoreProperties({"address", "email", "contactNumber"})
    private Customer customer;


    @ManyToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "vet_id")
    private Vet vet;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "appointment_id")
    private List<Pet> pets;
}

