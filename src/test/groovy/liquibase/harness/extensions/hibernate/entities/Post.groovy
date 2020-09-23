package liquibase.harness.extensions.hibernate.entities

import javax.persistence.*
import java.time.ZonedDateTime

@Entity
@Table(name = "posts")
class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    int id

    String authorId

    @Column(nullable = false)
    String title

    @Column(nullable = false)
    String description

    @Column(nullable = false)
    String content

    @Column (name = "inserted_date", nullable = false)
    ZonedDateTime insertedDate
}
