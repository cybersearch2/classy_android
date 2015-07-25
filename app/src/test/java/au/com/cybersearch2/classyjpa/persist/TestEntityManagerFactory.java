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
package au.com.cybersearch2.classyjpa.persist;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import javax.persistence.EntityTransaction;

import au.com.cybersearch2.classyjpa.EntityManagerLite;
import au.com.cybersearch2.classyjpa.EntityManagerLiteFactory;
import au.com.cybersearch2.classyjpa.entity.EntityManagerImpl;
import au.com.cybersearch2.classyjpa.transaction.EntityTransactionImpl;

/**
 * TestEntityManagerFactory
 * @author Andrew Bowley
 * 14/06/2014
 */
public class TestEntityManagerFactory implements EntityManagerLiteFactory
{
    public static EntityManagerLite entityManager;
    
    @Override
    public void close() {
    }

    @Override
    public EntityManagerLite createEntityManager() {
        return entityManager;
    }

    @Override
    public EntityManagerLite createEntityManager(Map<String, Object>  arg0) {
        return entityManager;
    }

    @Override
    public boolean isOpen() {
        return true;
    }
    
    public static EntityManagerLite getEntityManager()
    {
        return entityManager;
    }
    
    public static EntityTransactionImpl setEntityManagerInstance()
    {
        EntityTransactionImpl transaction = mock(EntityTransactionImpl.class);
        setEntityManagerInstance(transaction);
        return transaction;
    }
    
    public static void setEntityManagerInstance(EntityTransaction transaction)
    {

        entityManager = mock(EntityManagerImpl.class);
        when(entityManager.getTransaction()).thenReturn(transaction);
    }

    @Override
    public Map<String, Object> getProperties() {
        return null;
    }
}
