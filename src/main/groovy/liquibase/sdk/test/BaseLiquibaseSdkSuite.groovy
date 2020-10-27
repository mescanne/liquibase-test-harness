package liquibase.sdk.test

import liquibase.sdk.test.change.ChangeObjectTests
import liquibase.sdk.test.snapshot.SnapshotObjectTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses([ChangeObjectTests, SnapshotObjectTests])
public abstract class BaseLiquibaseSdkSuite {
}
