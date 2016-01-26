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
package au.com.cybersearch2.classyfy.helper;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.Intent;

/**
 * TicketManager
 * @author Andrew Bowley
 * 8 Jul 2015
 */
public class TicketManager
{
    public static int VOID_TICKET = -1;
    
    //protected ConcurrentHashMap<Integer, Intent> intentionMap;
    protected HashMap<Integer, Intent> intentionMap;
    protected AtomicInteger currentTicket;
    
    public TicketManager()
    {
        currentTicket = new AtomicInteger();
        //intentionMap = new ConcurrentHashMap<Integer, Intent>();
        intentionMap = new HashMap<Integer, Intent>();
    }
    
    public int addIntent(Intent intent)
    {
        int ticket = currentTicket.getAndIncrement();
        intentionMap.put(Integer.valueOf(ticket), intent);
        return ticket;
    }
    
    public Intent removeIntent(int ticket)
    {
        if (ticket == VOID_TICKET)
            return null;
        Integer key = Integer.valueOf(ticket);
        Intent intent = intentionMap.get(key);
        if (intent != null)
        {
            intentionMap.remove(key);
            synchronized(intent)
            {
                intent.notifyAll();
            }
        }
        return intent;
    }
    
    public int voidTicket()
    {
        return VOID_TICKET;
    }
}
