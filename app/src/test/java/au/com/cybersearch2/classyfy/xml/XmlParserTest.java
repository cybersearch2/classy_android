package au.com.cybersearch2.classyfy.xml;

import java.io.FileInputStream;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import au.com.cybersearch2.classyfy.BuildConfig;
import au.com.cybersearch2.classynode.Node;
import au.com.cybersearch2.classyfy.data.alfresco.AlfrescoFilePlanXmlParser;
import static org.fest.assertions.api.Assertions.assertThat;

// Run as API 19 as workaround to Robolectric not instrumenting classes in org.kxml3 -
// https://github.com/robolectric/robolectric/issues/1876
// Requires 40634K  android-all-4.4_r1-robolectric-1.jar
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, emulateSdk = 19)
public class XmlParserTest
{
    interface NodeFunction
    {
        boolean onNode(Node node);
    }
    
    private boolean breadthFirst(Node node, NodeFunction nodeFunction)
    {
        //if (node.getModel() != Model.root)
        for (Node child: node.getChildren())
        {
            if (!nodeFunction.onNode(child))
                return false;
        }
        for (Node child: node.getChildren())
        {
            if (!breadthFirst(child, nodeFunction))
                return false;
        }
        return true;
    }
    
    String LEVEL_2_NAMES[] =
    {
            "Administration",
            "Communications",
            "Corporate Management",
            "Customer Relations",
            "Financial Management",
            "Information & Communications Technology",
            "Workforce Management"        
    };

    @Test
    public void doXmlParserTest() throws Exception
    {
        FileInputStream fis = new FileInputStream("src/test/java/cybersearch2-fileplan.xml");
        AlfrescoFilePlanXmlParser xmlParser = new AlfrescoFilePlanXmlParser();
        Node rootNode = xmlParser.parseDataStream(fis);
        fis.close();
        assertThat(rootNode).isNotNull();
        final int[] countHolder = new int[] { 0 };
        breadthFirst(rootNode, new NodeFunction(){

            @Override
            public boolean onNode(Node node) {
                ++countHolder[0];
                assertThat(node.getProperties().size() > 0);
                //System.out.println("--- " + node.getTitle() + " ---");
                //System.out.println(node.getProperties().toString());
                return true;
            }});
        //System.out.println("Count = " + countHolder[0]);
        List<Node> children = rootNode.getChildren();
        assertThat(children).hasSize(1);
        Node level1Node = children.get(0);
        assertThat(level1Node.getName()).isEqualTo("Cybersearch2-Records");
        assertThat(level1Node.getTitle()).isEqualTo("Cybersearch2 Records");
        assertThat(level1Node.getLevel()).isEqualTo(1);
        assertThat(level1Node.getParent()).isEqualTo(rootNode);
        int index = 0;
        for (Node node: level1Node.getChildren())
        {
            assertThat(node.getName()).isEqualTo(LEVEL_2_NAMES[index++]);
            assertThat(node.getChildren()).isNotEmpty();
        }
    }
/*
    @Test
    public void generateSqlTest() throws Exception
    {
        FileInputStream fis = new FileInputStream("java/cybersearch2-fileplan.xml");
        AlfrescoFilePlanXmlParser xmlParser = new AlfrescoFilePlanXmlParser();
        Node rootNode = xmlParser.parseDataStream(fis);
        fis.close();
        SqlFromNodeGenerator generateSql = new SqlFromNodeGenerator();
        File createSqlFile = new File("java", "sample_alfresco_fileplan2.sql");
        Writer writer = new BufferedWriter(new FileWriter(createSqlFile));
        generateSql.generateSql(rootNode, writer);
        writer.flush();
        writer.close();
    }
*/    
}
