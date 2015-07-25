package au.com.cybersearch2.classyfy.data.alfresco;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.InputStream;
import java.io.Writer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import android.net.Uri;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import au.com.cybersearch2.classyfy.BuildConfig;
import au.com.cybersearch2.classyfy.TestClassyFyApplication;
import au.com.cybersearch2.classynode.Node;
import au.com.cybersearch2.classyfy.data.SqlFromNodeGenerator;
import au.com.cybersearch2.classyfy.data.TestDataStreamParser;
import au.com.cybersearch2.classyfy.data.alfresco.AlfrescoFilePlanLoader;
import au.com.cybersearch2.classyfy.data.alfresco.AlfrescoFilePlanXmlParser;
import au.com.cybersearch2.classyfy.test.AndroidEnvironmentFile;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, emulateSdk = 21)
public class AlfrescoFilePlanLoaderTest
{
    private static String PUBLIC_DOWNLOADS_PATH = "src/test/java/External/Download";
    private static String DATA_FILENAME = "cybersearch2-fileplan.xml";
    private static AndroidEnvironmentFile testFile;
    private static TestAlfrescoFilePlanLoaderModule testAlfrescoFilePlanLoaderModule;

    @Before
    public void setup() throws Exception 
    {
    	File tempfile = new File(".");
    	System.out.println("CWD = " + tempfile.getAbsolutePath());
        if (testFile == null)
        {
            // Add test module as extra module to override normal module
            testAlfrescoFilePlanLoaderModule = new TestAlfrescoFilePlanLoaderModule();
             // Set up dependency injection
            TestClassyFyApplication.getTestInstance().init(testAlfrescoFilePlanLoaderModule);
            testFile = new AndroidEnvironmentFile(PUBLIC_DOWNLOADS_PATH,DATA_FILENAME);
        }
    }

    @Test
    public void testAlfrescoFilePlanLoader() throws Exception
    {
        AlfrescoFilePlanLoader alfrescoFilePlanLoader = new TestAlfrescoFilePlanLoader();
        TestDataStreamParser dataStreamLoader = testAlfrescoFilePlanLoaderModule.getTestDataStreamLoader();
        assertThat(dataStreamLoader).isNotNull();
        AlfrescoFilePlanXmlParser alfrescoFilePlanXmlParser = mock(AlfrescoFilePlanXmlParser.class);
        Node rootNode = Node.rootNodeNewInstance();
        when(alfrescoFilePlanXmlParser.parseDataStream(isA(InputStream.class))).thenReturn(rootNode);
        dataStreamLoader.setDataStreamParser(alfrescoFilePlanXmlParser);
        Uri dataUri = Uri.fromFile(testFile.getTestFile());

        alfrescoFilePlanLoader.loadData(dataUri);
        SqlFromNodeGenerator sqlFromNodeGenerator = testAlfrescoFilePlanLoaderModule.getSqlFromNodeGenerator();
        verify(alfrescoFilePlanXmlParser).parseDataStream(isA(InputStream.class));
        verify(sqlFromNodeGenerator).generateSql(eq(rootNode), isA(Writer.class));
        assertThat(((TestAlfrescoFilePlanLoader)alfrescoFilePlanLoader).processFilesCallable).isNotNull();
    }
  
}
