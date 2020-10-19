package liquibase.sdk.test.util

import liquibase.CatalogAndSchema
import liquibase.Liquibase
import liquibase.change.Change
import liquibase.changelog.ChangeSet
import liquibase.database.Database
import liquibase.sdk.test.config.DatabaseUnderTest
import liquibase.sdk.test.config.TestConfig
import liquibase.sql.Sql
import liquibase.sqlgenerator.SqlGeneratorFactory

import java.util.logging.Logger

class TestUtils {

    static Liquibase createLiquibase(String pathToFile, Database database) {
        database.resetInternalState()
        return new Liquibase(pathToFile, TestConfig.instance.resourceAccessor, database)
    }

    static String toSqlFromLiquibaseChangeSets(Liquibase liquibase) {
        Database db = liquibase.database
        List<ChangeSet> changeSets = liquibase.databaseChangeLog.changeSets
        List<String> stringList = new ArrayList<>()
        changeSets.each { stringList.addAll(toSql(it, db)) }

        return stringList.join(System.lineSeparator())
    }

    private static List<String> toSql(ChangeSet changeSet, Database db) {
        return toSql(changeSet.changes, db)
    }

    private static List<String> toSql(List<? extends Change> changes, Database db) {
        List<String> stringList = new ArrayList<>()
        changes.each { stringList.addAll(toSql(it, db)) }
        return stringList
    }

    private static List<String> toSql(Change change, Database db) {
        Sql[] sqls = SqlGeneratorFactory.newInstance().generateSql(change, db)
        return sqls*.toSql()
    }

    static ArrayList<CatalogAndSchema> getCatalogAndSchema(Database database, String dbSchema) {
        List<String> schemaList = parseValuesToList(dbSchema, ",")

        List<CatalogAndSchema> finalList = new ArrayList<>()
        schemaList?.each { sch ->
            String[] catSchema = sch.split("\\.")
            String catalog, schema
            if (catSchema.length == 2) {
                catalog = catSchema[0]?.trim()
                schema = catSchema[1]?.trim()
            } else if (catSchema.length == 1) {
                catalog = null
                schema = catSchema[0]?.trim()
            } else {
                return finalList
            }
            finalList.add(new CatalogAndSchema(catalog, schema).customize(database))
        }

        return finalList
    }

    static List<String> parseValuesToList(String str, String regex = null) {
        List<String> returnList = new ArrayList<>()
        if (str) {
            if (regex == null) {
                returnList.add(str)
                return returnList
            }
            return str?.split(regex)*.trim()
        }
        return returnList
    }

    static SortedMap<String, String> getChangeLogPaths(DatabaseUnderTest database, String inputFormat) {
        inputFormat = inputFormat ?: ""
        def returnPaths = new TreeMap<String, String>()
        for (String changeLogPath : TestConfig.instance.resourceAccessor.list(null, "liquibase/sdk/test/changelogs", true, true, false)) {
            def validChangeLog = false

            //is it a common changelog?
            if (changeLogPath =~ "liquibase/sdk/test/changelogs/[\\w.]+${inputFormat}+\$") {
                validChangeLog = true
            } else if (changeLogPath =~ "liquibase/sdk/test/changelogs/${database.name}/[\\w.]+${inputFormat}+\$") {
                //is it a database-specific changelog?
                validChangeLog = true
            } else if (changeLogPath =~ "liquibase/sdk/test/changelogs/${database.name}/${database.version}/[\\w" +
                    ".]+${inputFormat}+\$") {
                //is it a database-major-version specific changelog?
                validChangeLog = true
            }

            if (validChangeLog) {
                def fileName = changeLogPath.replaceFirst(".*/", "").replaceFirst("\\.[^.]+\$", "")
                if (!returnPaths.containsKey(fileName) || returnPaths.get(fileName).length() < changeLogPath.length()) {
                    returnPaths.put(fileName, changeLogPath)
                }
            }
        }

        Logger.getLogger(this.class.name).info("Found " + returnPaths.size() + " changeLogs for " + database.name +
                "/" + database.version + " in liquibase/sdk/test/changelogs")


        return returnPaths
    }

    static void validateAndSetPropertiesFromCommandLine(TestConfig testConfig) {
        def log = Logger.getLogger(this.class.name)

        if (System.getProperty("revalidateSql") == null) {
            revalidateSql = true
        } else {
            revalidateSql = Boolean.valueOf(System.getProperty("revalidateSql"))
        }
        log.info("Revalidate SQL: ${revalidateSql}")

        String inputFormat = System.getProperty("inputFormat")
        String changeObjects = System.getProperty("changeObjects")
        String dbName = System.getProperty("dbName")
        String dbVersion = System.getProperty("dbVersion")
        if (inputFormat && (!supportedChangeLogFormats.contains(inputFormat))) {
            throw new IllegalArgumentException(inputFormat + " inputFormat is not supported")
        }
        testConfig.inputFormat = inputFormat ?: testConfig.inputFormat
        log.warning("Only " + testConfig.inputFormat + " input files are taken into account for this test run")

//        if (changeObjects) {
//            testConfig.defaultChangeObjects = Arrays.asList(changeObjects.split(","))
//            //in case user provided changeObjects in cmd run only them regardless of config file
//            for (def db : testConfig.databasesUnderTest) {
//                db.databaseSpecificChangeObjects = null
//            }
//            log.info("running for next changeObjects : " + testConfig.defaultChangeObjects)
//        }
        if (dbName) {
            //TODO try improve this, add logging
            testConfig.databasesUnderTest = testConfig.databasesUnderTest.stream()
                    .filter({ it.name.equalsIgnoreCase(dbName) })
                    .findAny()
                    .map({ Collections.singletonList(it) })
                    .orElse(testConfig.databasesUnderTest)

            if (dbVersion)
                for (DatabaseUnderTest databaseUnderTest : testConfig.databasesUnderTest) {
                    databaseUnderTest.versions = databaseUnderTest.versions.stream()
                            .filter({ it.version.equalsIgnoreCase(dbVersion) })
                            .findAny()
                            .map({ Collections.singletonList(it) })
                            .orElse(databaseUnderTest.versions)
                }
        }
        log.info(testConfig.toString())
    }
}
