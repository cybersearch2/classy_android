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
package au.com.cybersearch2.example.v2;

import javax.inject.Singleton;

import au.com.cybersearch2.classyinject.ApplicationModule;

import com.example.hellotwodbs.HelloTwoDbs;
import com.example.hellotwodbs.HelloTwoDbsApplication;

import dagger.Module;
import dagger.Provides;

/**
 * AndroidHelloTwoDbsModule
 * Dependency injection data object. @see AndroidHelloTwoDbs.
 * @author Andrew Bowley
 * 23 Sep 2014
 */
@Module(injects = { HelloTwoDbs.class, AndroidHelloTwoDbs.class, SimpleOpenHelperCallbacks.class, ComplexOpenHelperCallbacks.class },
        includes = HelloTwoDbsEnvironmentModule.class) 
public class AndroidHelloTwoDbsModule implements ApplicationModule
{
	@Provides @Singleton AndroidHelloTwoDbs provideAndroidHelloTwoDbs()
	{
		return HelloTwoDbsApplication.getAndroidHelloTwoDbsSingleton();
	}
	
}


