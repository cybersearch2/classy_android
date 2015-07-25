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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import au.com.cybersearch2.classytask.BackgroundTask;

/**
 * ProgressFragment
 * @author Andrew Bowley
 * 29/05/2014
 */
public class ProgressFragment extends Fragment
{
    ProgressBar spinner;
    BackgroundTask hideTask;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.progress, container,
                false);
        spinner = (ProgressBar)rootView.findViewById(R.id.mainProgressBar);
        return rootView;
    }
    
    public ProgressBar getSpinner()
    {
        return spinner;
    }
    
    public void showSpinner()
    {
        if (spinner != null)
            spinner.setVisibility(View.VISIBLE);
    }
    
    public void hideSpinner()
    {
        if (spinner != null)
            spinner.setVisibility(View.GONE);
    }
}
