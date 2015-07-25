package au.com.cybersearch2.classyfy.test;

import java.io.FileInputStream;
import java.util.List;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class SqlParserTest
{

    @Test
    public void testSqlParser() throws Exception
    {
        SqlParser sqlParser = new SqlParser();
        FileInputStream fis = new FileInputStream("src/test/java/schema.sql");
        List<String> sqlList = sqlParser.parseStream(fis);
        fis.close();
        assertThat(sqlList).isNotNull();
        assertThat(sqlList).isNotEmpty();
        assertThat(sqlList).hasSize(17);
        for (String statement: sqlList)
        {
            assertThat(statement).endsWith(";");
        }
        assertThat(sqlList.get(11)).isEqualTo("create table T_REWARD (ID integer identity primary key, CONFIRMATION_NUMBER varchar(25) not null, " +
                                              "REWARD_AMOUNT double not null, REWARD_DATE date not null, ACCOUNT_NUMBER varchar(9) not null, DINING_AMOUNT " +
                                              "double not null, DINING_MERCHANT_NUMBER varchar(10) not null, DINING_DATE date not null, unique(CONFIRMATION_NUMBER));");
        fis = new FileInputStream("src/test/java/test-data.sql");
        sqlList = sqlParser.parseStream(fis);
        fis.close();
        assertThat(sqlList).isNotNull();
        assertThat(sqlList).isNotEmpty();
        assertThat(sqlList).hasSize(58);
        for (String statement: sqlList)
        {
            assertThat(statement).startsWith("insert into");
            assertThat(statement).endsWith(";");
        }
        assertThat(sqlList.get(0)).isEqualTo("insert into T_ACCOUNT (NUMBER, NAME) values ('123456789', 'Keith and Keri O''Neil');");
    }
/*    
    @Test
    public void test2() throws Exception
    {
        SqlParser sqlParser = new SqlParser();
        FileInputStream fis = new FileInputStream("res/raw/classyfy_schema.sql");
        List<String> sqlList = sqlParser.parseStream(fis);
        fis.close();
        for (String statement: sqlList)
        {
            System.out.println(statement);
        }
   }
*/   

}
