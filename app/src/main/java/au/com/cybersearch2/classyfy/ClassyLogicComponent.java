/**
    Copyright (C) 2015  www.cybersearch2.com.au

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

import javax.inject.Singleton;

import au.com.cybersearch2.classyfy.data.Node;
import au.com.cybersearch2.classyfy.module.ClassyLogicModule;
import dagger.Subcomponent;

/**
 * ClassyLogicComponent
 * @author Andrew Bowley
 * 13 Jan 2016
 */
@Singleton
@Subcomponent(modules = ClassyLogicModule.class)
public interface ClassyLogicComponent
{
    Node node();
}
