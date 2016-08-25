package au.com.cybersearch2.classyfy;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import au.com.cybersearch2.classyfy.provider.ClassyFyProviderTest;
import au.com.cybersearch2.classyjpa.entity.PersistenceLoaderTest;

/**
 * Created by andrew on 21/07/2015.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        MainActivityAndroidTest.class,
        TitleSearchResultsActivityTest.class,
        ClassyFyProviderTest.class,
        PersistenceLoaderTest.class
        })
public class ClassyfyTestSuite
{
}
