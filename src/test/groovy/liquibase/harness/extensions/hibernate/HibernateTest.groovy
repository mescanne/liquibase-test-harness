package liquibase.harness.extensions.hibernate

import liquibase.Liquibase
import liquibase.Scope
import liquibase.database.Database
import liquibase.database.core.MySQLDatabase
import liquibase.database.jvm.JdbcConnection
import liquibase.diff.DiffResult
import liquibase.diff.compare.CompareControl
import liquibase.diff.output.DiffOutputControl
import liquibase.diff.output.changelog.DiffToChangeLog
import liquibase.diff.output.report.DiffToReport
import liquibase.ext.hibernate.database.HibernateClassicDatabase
import liquibase.ext.hibernate.database.connection.HibernateConnection
import liquibase.harness.extensions.hibernate.util.HibernateUtil
import liquibase.harness.util.HarnessResourceAccessor
import liquibase.resource.ClassLoaderResourceAccessor
import liquibase.resource.FileSystemResourceAccessor
import liquibase.structure.DatabaseObject
import liquibase.structure.core.Column
import liquibase.structure.core.ForeignKey
import liquibase.structure.core.Index
import liquibase.structure.core.PrimaryKey
import liquibase.structure.core.Sequence
import liquibase.structure.core.Table
import liquibase.structure.core.UniqueConstraint
import org.hibernate.SessionFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Shared
import spock.lang.Specification

import java.sql.Connection
import java.sql.DriverManager

class HibernateTest extends Specification {
    @Shared Logger logger = LoggerFactory.getLogger(getClass())
    @Shared private Database database;
    @Shared private Connection connection;
    @Shared private CompareControl compareControl;
    @Shared private static final String HIBERNATE_CONFIG_FILE = "hibernate/hibernate.cfg.xml";

    def setupSpec() {

        //Class.forName("com.mysql.jdbc.Driver");
        connection = DriverManager.getConnection("jdbc:mysql://localhost:33062/lbcat", "lbuser", "LiquibasePass1");
        database = new MySQLDatabase()
        database.setConnection(new JdbcConnection(connection))
        configureCompareControl()
    }

    def "apply #testInput.changeObject for #testInput.databaseName #testInput.version; verify generated SQL and DB snapshot"() {
        given:
        Liquibase liquibase = new Liquibase((String) null, new HarnessResourceAccessor(), database)
        Database hibernateDatabase = new HibernateClassicDatabase()
        hibernateDatabase.setConnection(new JdbcConnection(new HibernateConnection("hibernate:classic:" + HIBERNATE_CONFIG_FILE, new HarnessResourceAccessor())));

        when:
        DiffResult diffResult = liquibase.diff(hibernateDatabase, database, compareControl)

        then:
        diffResult.getMissingObjects().size() > 0

        when:
        File outFile = File.createTempFile("lb-test", ".xml")
        OutputStream outChangeLog = new FileOutputStream(outFile)
        String changeLogString = toChangeLog(diffResult)
        outChangeLog.write(changeLogString.getBytes("UTF-8"))
        outChangeLog.close()

        Scope.getCurrentScope().getLog(getClass()).info("Changelog:\n" + changeLogString)

        liquibase = new Liquibase(outFile.toString(), new FileSystemResourceAccessor(File.listRoots()), database)
        StringWriter stringWriter = new StringWriter()
        liquibase.update((String) null, stringWriter)
        Scope.getCurrentScope().getLog(getClass()).info(stringWriter.toString())
        liquibase.update((String) null)

        diffResult = liquibase.diff(hibernateDatabase, database, compareControl)
        ignoreDatabaseChangeLogTable(diffResult)

        String differences = toString(diffResult)
        logger.info(differences)

        then:
        diffResult.getMissingObjects().size() == 0
        diffResult.getUnexpectedObjects().size() == 0

    }

    private static String toString(DiffResult diffResult) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream()
        PrintStream printStream = new PrintStream(out, true, "UTF-8")
        DiffToReport diffToReport = new DiffToReport(diffResult, printStream)
        diffToReport.print()
        printStream.close()
        return out.toString("UTF-8")
    }

    private static String toChangeLog(DiffResult diffResult) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream()
        PrintStream printStream = new PrintStream(out, true, "UTF-8")
        DiffOutputControl diffOutputControl = new DiffOutputControl()
        diffOutputControl.setIncludeCatalog(false)
        diffOutputControl.setIncludeSchema(false)
        DiffToChangeLog diffToChangeLog = new DiffToChangeLog(diffResult,
                diffOutputControl)
        diffToChangeLog.print(printStream)
        printStream.close()
        return out.toString("UTF-8")
    }

    private static void ignoreDatabaseChangeLogTable(DiffResult diffResult) throws Exception {
        Set<Table> unexpectedTables = diffResult.getUnexpectedObjects(Table.class)
        for (Iterator<Table> iterator = unexpectedTables.iterator(); iterator.hasNext(); ) {
            Table table = iterator.next()
            if ("DATABASECHANGELOGLOCK".equalsIgnoreCase(table.getName()) || "DATABASECHANGELOG".equalsIgnoreCase(table.getName()))
                diffResult.getUnexpectedObjects().remove(table)
        }
        Set<Table> missingTables = diffResult.getMissingObjects(Table.class)
        for (Iterator<Table> iterator = missingTables.iterator(); iterator.hasNext(); ) {
            Table table = iterator.next()
            if ("DATABASECHANGELOGLOCK".equalsIgnoreCase(table.getName()) || "DATABASECHANGELOG".equalsIgnoreCase(table.getName()))
                diffResult.getMissingObjects().remove(table)
        }
        Set<Column> unexpectedColumns = diffResult.getUnexpectedObjects(Column.class)
        for (Iterator<Column> iterator = unexpectedColumns.iterator(); iterator.hasNext(); ) {
            Column column = iterator.next()
            if ("DATABASECHANGELOGLOCK".equalsIgnoreCase(column.getRelation().getName()) || "DATABASECHANGELOG".equalsIgnoreCase(column.getRelation().getName()))
                diffResult.getUnexpectedObjects().remove(column)
        }
        Set<Column> missingColumns = diffResult.getMissingObjects(Column.class)
        for (Iterator<Column> iterator = missingColumns.iterator(); iterator.hasNext(); ) {
            Column column = iterator.next()
            if ("DATABASECHANGELOGLOCK".equalsIgnoreCase(column.getRelation().getName()) || "DATABASECHANGELOG".equalsIgnoreCase(column.getRelation().getName()))
                diffResult.getMissingObjects().remove(column)
        }
        Set<Index> unexpectedIndexes = diffResult.getUnexpectedObjects(Index.class)
        for (Iterator<Index> iterator = unexpectedIndexes.iterator(); iterator.hasNext(); ) {
            Index index = iterator.next()
            if ("DATABASECHANGELOGLOCK".equalsIgnoreCase(index.getRelation().getName()) || "DATABASECHANGELOG".equalsIgnoreCase(index.getTable().getName()))
                diffResult.getUnexpectedObjects().remove(index)
        }
        Set<Index> missingIndexes = diffResult.getMissingObjects(Index.class)
        for (Iterator<Index> iterator = missingIndexes.iterator(); iterator.hasNext(); ) {
            Index index = iterator.next()
            if ("DATABASECHANGELOGLOCK".equalsIgnoreCase(index.getRelation().getName()) || "DATABASECHANGELOG".equalsIgnoreCase(index.getTable().getName()))
                diffResult.getMissingObjects().remove(index)
        }
        Set<PrimaryKey> unexpectedPrimaryKeys = diffResult.getUnexpectedObjects(PrimaryKey.class)
        for (Iterator<PrimaryKey> iterator = unexpectedPrimaryKeys.iterator(); iterator.hasNext(); ) {
            PrimaryKey primaryKey = iterator.next()
            if ("DATABASECHANGELOGLOCK".equalsIgnoreCase(primaryKey.getTable().getName()) || "DATABASECHANGELOG".equalsIgnoreCase(primaryKey.getTable().getName()))
                diffResult.getUnexpectedObjects().remove(primaryKey)
        }
        Set<PrimaryKey> missingPrimaryKeys = diffResult.getMissingObjects(PrimaryKey.class)
        for (Iterator<PrimaryKey> iterator = missingPrimaryKeys.iterator(); iterator.hasNext(); ) {
            PrimaryKey primaryKey = iterator.next()
            if ("DATABASECHANGELOGLOCK".equalsIgnoreCase(primaryKey.getTable().getName()) || "DATABASECHANGELOG".equalsIgnoreCase(primaryKey.getTable().getName()))
                diffResult.getMissingObjects().remove(primaryKey)
        }
    }

    void configureCompareControl() {
        Set<Class<? extends DatabaseObject>> typesToInclude = new HashSet<Class<? extends DatabaseObject>>()
        typesToInclude.add(Table.class)
        typesToInclude.add(Column.class)
        typesToInclude.add(PrimaryKey.class)
        typesToInclude.add(ForeignKey.class)
        typesToInclude.add(Index.class)
        typesToInclude.add(UniqueConstraint.class)
        typesToInclude.add(Sequence.class)
        compareControl = new CompareControl(typesToInclude)
        compareControl.addSuppressedField(Table.class, "remarks")
        compareControl.addSuppressedField(Column.class, "remarks")
        compareControl.addSuppressedField(Column.class, "certainDataType")
        compareControl.addSuppressedField(Column.class, "autoIncrementInformation")
        compareControl.addSuppressedField(ForeignKey.class, "deleteRule")
        compareControl.addSuppressedField(ForeignKey.class, "updateRule")
        compareControl.addSuppressedField(Index.class, "unique")
    }
}
