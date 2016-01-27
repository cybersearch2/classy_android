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
package au.com.cybersearch2.classyfts;

/**
 * WordFilter
 * @author Andrew Bowley
 * 28/04/2014
 */
public interface WordFilter
{
    /**
     * Search result word filter 
     * @param key Database column name 
     * @param word Word from column identified by the key
     * @return Same value as "word" parameter or a replacement value
     */
    String filter(String key, String word);
}
