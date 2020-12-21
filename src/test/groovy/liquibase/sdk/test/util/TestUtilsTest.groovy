package liquibase.sdk.test.util

import liquibase.database.OfflineConnection
import liquibase.database.core.MySQLDatabase
import liquibase.database.core.PostgresDatabase
import liquibase.resource.ClassLoaderResourceAccessor
import liquibase.sdk.test.config.DatabaseUnderTest
import spock.lang.Specification

class TestUtilsTest extends Specification {

    def "getChangeLogPaths"() {
        when:
        def database = new MySQLDatabase()
        database.setConnection(new OfflineConnection("offline:mysql?version=8.1", new ClassLoaderResourceAccessor()))

        DatabaseUnderTest databaseUnderTest = new DatabaseUnderTest()
        databaseUnderTest.database = database
        databaseUnderTest.name = "mysql"
        databaseUnderTest.version = "8"

        def paths = TestUtils.getChangeLogPaths(databaseUnderTest, "xml")

        then:
        paths["addColumn"] == "liquibase/sdk/test/change/changelogs/addColumn.xml"
        paths["addPrimaryKey"] == "liquibase/sdk/test/change/changelogs/addPrimaryKey.xml"
        paths["renameColumn"] == "liquibase/sdk/test/change/changelogs/renameColumn.xml"

        when:
        paths = TestUtils.getChangeLogPaths(databaseUnderTest, "sql")

        then:
        paths["renameColumn"] == "liquibase/sdk/test/change/changelogs/mysql/8/renameColumn.sql"
        paths["renameTable"] == "liquibase/sdk/test/change/changelogs/mysql/renameTable.sql"

        when:
        database = new PostgresDatabase()
        database.setConnection(new OfflineConnection("offline:postgresql?version=12.1", new ClassLoaderResourceAccessor()))

        DatabaseUnderTest databaseUnderTestPostgre = new DatabaseUnderTest()
        databaseUnderTestPostgre.database = database
        databaseUnderTestPostgre.name = "postgresql"
        databaseUnderTestPostgre.version = "12"
        paths = TestUtils.getChangeLogPaths(databaseUnderTestPostgre, "xml")

        then:
        paths["addColumn"] == "liquibase/sdk/test/change/changelogs/addColumn.xml"
        paths["addPrimaryKey"] == "liquibase/sdk/test/change/changelogs/addPrimaryKey.xml"
        paths["datatypes.binary"] == "liquibase/sdk/test/change/changelogs/postgresql/datatypes.binary.xml"
    }
}
