package liquibase.harness.extensions.hibernate.entities

import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.OneToMany
import javax.persistence.Table
import java.time.LocalDate
import java.time.ZonedDateTime

@Entity
@Table(name = "authors")
class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    int id

    @Column(name = "first_name", nullable = false)
    String firstName

    @Column(name = "last_name", nullable = false)
    String lastName

    @Column(nullable = false)
    String email

    @Column(name = "birthdate", nullable = false)
    LocalDate birthdate

    @Column (name = "added", nullable = false)
    ZonedDateTime added

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    List<Post> posts
}
