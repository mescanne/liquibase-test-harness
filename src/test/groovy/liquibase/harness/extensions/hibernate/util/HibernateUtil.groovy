package liquibase.harness.extensions.hibernate.util

import liquibase.diff.DiffResult
import liquibase.structure.core.Column
import liquibase.structure.core.Index
import liquibase.structure.core.PrimaryKey
import liquibase.structure.core.Table
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

class HibernateUtil {
    private static StandardServiceRegistry registry;
    private static SessionFactory sessionFactory;

    static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            try {
                // Create registry
                registry = new StandardServiceRegistryBuilder()
                        .configure("hibernate/hibernate.cfg.xml").build()

                // Create MetadataSources
                MetadataSources sources = new MetadataSources(registry)

                // Create Metadata
                Metadata metadata = sources.getMetadataBuilder().build()

                // Create SessionFactory
                sessionFactory = metadata.getSessionFactoryBuilder().build()

            } catch (Exception e) {
                e.printStackTrace()
                if (registry != null) {
                    StandardServiceRegistryBuilder.destroy(registry)
                }
            }
        }
        return sessionFactory
    }

    static void shutdown() {
        if (registry != null) {
            StandardServiceRegistryBuilder.destroy(registry)
        }
    }

    static void removeDatabaseChangeLogTableFromResults(DiffResult diffResult) throws Exception {
        Set<Table> unexpectedTables = diffResult.getUnexpectedObjects(Table.class)
        for (Iterator<Table> iterator = unexpectedTables.iterator(); iterator.hasNext();) {
            Table table = iterator.next()
            if ("DATABASECHANGELOGLOCK".equalsIgnoreCase(table.getName()) || "DATABASECHANGELOG".equalsIgnoreCase(table.getName()))
                diffResult.getUnexpectedObjects().remove(table)
        }
        Set<Table> missingTables = diffResult.getMissingObjects(Table.class)
        for (Iterator<Table> iterator = missingTables.iterator(); iterator.hasNext();) {
            Table table = iterator.next()
            if ("DATABASECHANGELOGLOCK".equalsIgnoreCase(table.getName()) || "DATABASECHANGELOG".equalsIgnoreCase(table.getName()))
                diffResult.getMissingObjects().remove(table)
        }
        Set<Column> unexpectedColumns = diffResult.getUnexpectedObjects(Column.class)
        for (Iterator<Column> iterator = unexpectedColumns.iterator(); iterator.hasNext();) {
            Column column = iterator.next()
            if ("DATABASECHANGELOGLOCK".equalsIgnoreCase(column.getRelation().getName()) || "DATABASECHANGELOG".equalsIgnoreCase(column.getRelation().getName()))
                diffResult.getUnexpectedObjects().remove(column)
        }
        Set<Column> missingColumns = diffResult.getMissingObjects(Column.class)
        for (Iterator<Column> iterator = missingColumns.iterator(); iterator.hasNext();) {
            Column column = iterator.next()
            if ("DATABASECHANGELOGLOCK".equalsIgnoreCase(column.getRelation().getName()) || "DATABASECHANGELOG".equalsIgnoreCase(column.getRelation().getName()))
                diffResult.getMissingObjects().remove(column)
        }
        Set<Index> unexpectedIndexes = diffResult.getUnexpectedObjects(Index.class)
        for (Iterator<Index> iterator = unexpectedIndexes.iterator(); iterator.hasNext();) {
            Index index = iterator.next()
            if ("DATABASECHANGELOGLOCK".equalsIgnoreCase(index.getRelation().getName()) || "DATABASECHANGELOG".equalsIgnoreCase(index.getTable().getName()))
                diffResult.getUnexpectedObjects().remove(index)
        }
        Set<Index> missingIndexes = diffResult.getMissingObjects(Index.class)
        for (Iterator<Index> iterator = missingIndexes.iterator(); iterator.hasNext();) {
            Index index = iterator.next()
            if ("DATABASECHANGELOGLOCK".equalsIgnoreCase(index.getRelation().getName()) || "DATABASECHANGELOG".equalsIgnoreCase(index.getTable().getName()))
                diffResult.getMissingObjects().remove(index)
        }
        Set<PrimaryKey> unexpectedPrimaryKeys = diffResult.getUnexpectedObjects(PrimaryKey.class)
        for (Iterator<PrimaryKey> iterator = unexpectedPrimaryKeys.iterator(); iterator.hasNext();) {
            PrimaryKey primaryKey = iterator.next()
            if ("DATABASECHANGELOGLOCK".equalsIgnoreCase(primaryKey.getTable().getName()) || "DATABASECHANGELOG".equalsIgnoreCase(primaryKey.getTable().getName()))
                diffResult.getUnexpectedObjects().remove(primaryKey)
        }
        Set<PrimaryKey> missingPrimaryKeys = diffResult.getMissingObjects(PrimaryKey.class)
        for (Iterator<PrimaryKey> iterator = missingPrimaryKeys.iterator(); iterator.hasNext();) {
            PrimaryKey primaryKey = iterator.next()
            if ("DATABASECHANGELOGLOCK".equalsIgnoreCase(primaryKey.getTable().getName()) || "DATABASECHANGELOG".equalsIgnoreCase(primaryKey.getTable().getName()))
                diffResult.getMissingObjects().remove(primaryKey)
        }
    }
}