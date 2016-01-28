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
package au.com.cybersearch2.classyutil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Transcript - Partial replacement for Robolectric Transcript
 * @author Andrew Bowley
 * 13/06/2014
 */
public class Transcript
{
    protected List<String> textList;
    CountDownLatch signal = new CountDownLatch(1);
    public Transcript()
    {
        textList = new ArrayList<String>();
    }

    public void add(String text) 
    {
        textList.add(text);
    }

    public void assertEventsSoFar(String... textSequence) 
    {
        int index = 0;
        for (String text: textSequence)
        {
            if (index == textList.size())
                break;
            if (!textList.get(index).equals(text))
                break;
            ++index;
        }
        if (index != textList.size())
        {
            List<String> expectedList = new ArrayList<String>();
            for (String expected: textSequence)
            {
                expectedList.add(expected);
            }
            assertThat(textList.toString()).isEqualTo(expectedList.toString());
        }
    }

    public void assertEventsInclude(String text) 
    {
        assertThat(textList.size()).isGreaterThan(0);
        int index = 0;
        Iterator<String> iterator = textList.iterator();
        while (iterator.hasNext())
        {
            if (iterator.next().equals(text))
                break;
            ++index;
        }
        assertThat(index).isLessThan(textList.size());
    }

    public void signal()
    {
        signal.countDown();
    }

    public void await() throws InterruptedException
    {
        signal.await();
    }



}
