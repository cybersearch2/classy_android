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
//// Copyright of origin code which has only been slightly modified:////    
/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package au.com.cybersearch2.classyfts;

import android.app.SearchManager;
import android.database.Cursor;

/**
 * FtsSearch
 * Encapsulates the logic necessary to return specific words from the dictionary, and
 * load the dictionary table when it needs to be created.
 */
public class FtsSearch
{
    /** The fast text search query engine */
    protected FtsQuery ftsQuery;

    /**
     * Construct an FtsSearch object 
     * @param ftsQuery The fast text search query engine
     */
    public FtsSearch(FtsQuery ftsQuery) 
    {
        this.ftsQuery = ftsQuery;
    }

    /**
     * Returns a Cursor positioned at the word specified by rowId
     *
     * @param rowId ID of word to retrieve
     * @param columns The columns to include, if null then all are included
     * @param limit Maximum number of results to return, or zero if no limit 
     * @return Cursor positioned to matching word, or null if not found.
     */
    public Cursor getWord(String rowId, String[] columns, int limit) 
    {
        String selection = "rowid = ?";
        String[] selectionArgs = new String[] {rowId};

        return ftsQuery.query(selection, selectionArgs, columns, limit);

        /* This builds a query that looks like:
         *     SELECT <columns> FROM <table> WHERE rowid = <rowId>
         */
    }

    /**
     * Returns a Cursor over all words that match the given query
     *
     * @param query The string to search for
     * @param columns The columns to include, if null then all are included
     * @param limit Maximum number of results to return, or zero if no limit 
     * @return Cursor over all words that match, or null if none found.
     */
    public Cursor getWordMatches(String query, String[] columns, int limit) 
    {
        String selection = SearchManager.SUGGEST_COLUMN_TEXT_1 + " MATCH ?";
        String[] selectionArgs = new String[] {query+"*"};

        return ftsQuery.query(selection, selectionArgs, columns, limit);

        /* This builds a query that looks like:
         *     SELECT <columns> FROM <table> WHERE <KEY_TITLE> MATCH 'query*'
         * which is an FTS3 search for the query text (plus a wildcard) inside the word column.
         *
         * - "rowid" is the unique id for all rows but we need this value for the "_id" column in
         *    order for the Adapters to work, so the columns need to make "_id" an alias for "rowid"
         * - "rowid" also needs to be used by the SUGGEST_COLUMN_INTENT_DATA alias in order
         *   for suggestions to carry the proper intent data.
         *   These aliases are defined in the DictionaryProvider when queries are made.
         * - This can be revised to also search the definition text with FTS3 by changing
         *   the selection clause to use FTS_VIRTUAL_TABLE instead of KEY_TITLE (to search across
         *   the entire table, but sorting the relevance could be difficult.
         */
    }

    /**
     * Returns the fast text search query engine
     * @return FtsQuery
     */
    public FtsQuery getFtsQuery()
    {
        return ftsQuery;
    }
    
}
