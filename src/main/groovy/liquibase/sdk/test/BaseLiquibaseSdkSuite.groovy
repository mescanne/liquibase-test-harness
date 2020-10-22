package liquibase.sdk.test

import liquibase.sdk.test.change.ChangeObjectTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses([ChangeObjectTests.class])
public abstract class BaseLiquibaseSdkSuite {
}
