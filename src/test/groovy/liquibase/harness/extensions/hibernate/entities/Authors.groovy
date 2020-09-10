package liquibase.harness.extensions.hibernate.entities

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table
import java.time.LocalDate
import java.time.ZonedDateTime

@Entity
@Table(name = "authors")
class Authors {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    int id

    @Column(name = "first_name")
    String firstName

    @Column(name = "last_name")
    String lastName

    String email

    @Column(name = "birthdate")
    LocalDate birthdate

    @Column (name = "added")
    ZonedDateTime added
}
