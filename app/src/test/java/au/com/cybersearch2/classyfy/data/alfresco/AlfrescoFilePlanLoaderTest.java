package au.com.cybersearch2.classyfy.data.alfresco;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.InputStream;
import java.io.Writer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import com.j256.ormlite.support.DatabaseConnection;

import android.net.Uri;

import au.com.cybersearch2.classyfy.BuildConfig;
import au.com.cybersearch2.classyfy.ClassyFyApplication;
import au.com.cybersearch2.classyfy.ClassyFyResourceEnvironment;
import au.com.cybersearch2.classyfy.data.SqlFromNodeGenerator;
import au.com.cybersearch2.classyfy.data.TestDataStreamParser;
import au.com.cybersearch2.classyfy.provider.ClassyFyProvider;
import au.com.cybersearch2.classyfy.test.AndroidEnvironmentFile;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classyjpa.transaction.TransactionCallable;
import au.com.cybersearch2.classynode.Node;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23)
public class AlfrescoFilePlanLoaderTest
{
    private static String PUBLIC_DOWNLOADS_PATH = "src/test/java/External/Download";
    private static String DATA_FILENAME = "cybersearch2-fileplan.xml";
    private AndroidEnvironmentFile testFile;

    @Before
    public void setup() throws Exception 
    {
    	File tempfile = new File(".");
    	System.out.println("CWD = " + tempfile.getAbsolutePath());
        testFile = new AndroidEnvironmentFile(PUBLIC_DOWNLOADS_PATH,DATA_FILENAME);
    }

    @After
    public void tearDown()
    {
    }
    
    @Test
    public void testAlfrescoFilePlanLoader() throws Exception
    {
        PersistenceContext persistenceContext = mock(PersistenceContext.class);
        PersistenceAdmin persistenceAdmin = mock(PersistenceAdmin.class);
        when(persistenceContext.getPersistenceAdmin(ClassyFyProvider.PU_NAME)).thenReturn(persistenceAdmin);
        TestDataStreamParser dataStreamLoader = new TestDataStreamParser();
        SqlFromNodeGenerator sqlFromNodeGenerator = mock(SqlFromNodeGenerator.class);
        Node rootNode = Node.rootNodeNewInstance();
        AlfrescoFilePlanLoader alfrescoFilePlanLoader =
            new TestAlfrescoFilePlanLoader(persistenceContext, dataStreamLoader, sqlFromNodeGenerator);
        AlfrescoFilePlanXmlParser alfrescoFilePlanXmlParser = mock(AlfrescoFilePlanXmlParser.class);
        when(alfrescoFilePlanXmlParser.parseDataStream(isA(InputStream.class))).thenReturn(rootNode);
        dataStreamLoader.setDataStreamParser(alfrescoFilePlanXmlParser);
        Uri dataUri = Uri.fromFile(testFile.getTestFile());
        TransactionCallable dataLoadTask = alfrescoFilePlanLoader.createDataLoadTask(dataUri);
        assertThat(dataLoadTask).isNotNull();
        verify(alfrescoFilePlanXmlParser).parseDataStream(isA(InputStream.class));
        verify(sqlFromNodeGenerator).generateSql(eq(rootNode), isA(Writer.class));
        DatabaseConnection databaseConnection = mock(DatabaseConnection.class);
        // Insert 8-line SQL file stream in object under test to exercise dataLoadTask
        InputStream filestream = new ClassyFyResourceEnvironment().openResource("classyfy_schema.sql");
        alfrescoFilePlanLoader.instream = filestream;
        dataLoadTask.call(databaseConnection);
        filestream.close();
        verify(databaseConnection, times(8)).executeStatement(isA(String.class), eq(DatabaseConnection.DEFAULT_RESULT_FLAGS));
    }
  
}
