/**
    Copyright (C) 2014  www.cybersearch2.com.au

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/> */
package au.com.cybersearch2.classyfy;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import au.com.cybersearch2.classyfy.data.alfresco.AlfrescoFilePlanLoaderTest;
import au.com.cybersearch2.classyfy.provider.ClassyFyProviderTest;
import au.com.cybersearch2.classyfy.provider.ClassyFySearchEngineTest;
import au.com.cybersearch2.classyfy.test.SqlParserTest;
import au.com.cybersearch2.classyfy.xml.XmlParserTest;

/**
 * ClassyFyTestSuite
 * @author Andrew Bowley
 * 08/07/2014
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    ClassyfyLogicTest.class,
    MainActivityTest.class,
    TitleSearchResultsActivityTest.class,
    ClassyFyProviderTest.class,
    ClassyFySearchEngineTest.class,
    SqlParserTest.class,
    XmlParserTest.class,
    AlfrescoFilePlanLoaderTest.class,
    IntegrateMainActivityTest.class
})
public class ClassyFyTestSuite
{
}
