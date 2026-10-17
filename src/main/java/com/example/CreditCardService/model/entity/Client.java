package com.example.CreditCardService.model.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Entity
@Table(name = "clients",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"series", "passportNumber"})
        })
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;
    private String surname;
    private String middleName;

    private LocalDate birthdate;

    private String phoneNumber;

    private String email;

    @Column(length = 4, nullable = false)
    private String series;

    @Column(length = 6, nullable = false)
    private String passportNumber;

    @CreationTimestamp
    private LocalDate createDate;

    @OneToMany(mappedBy = "client")
    private List<CreditApplication> applications = new ArrayList<>();

    public Integer getAge() {
        if (birthdate == null) {
            return null;
        }

        return Period.between(
                birthdate,
                LocalDate.now()
        ).getYears();
    }

}
