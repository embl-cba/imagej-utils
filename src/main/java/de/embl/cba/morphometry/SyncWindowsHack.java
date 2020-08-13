package de.embl.cba.morphometry;

import ij.plugin.frame.SyncWindows;

import java.lang.reflect.Method;
import java.util.Vector;

public class SyncWindowsHack extends SyncWindows
{

	public SyncWindowsHack()
	{
		super();
	}

	public void syncAll()
	{

		if (wList == null) return;
		// Select all items on list.
		Vector v = new Vector();
		Integer I;
		for(int i=0; i<wList.getItemCount();++i) {
			wList.select(i);
			I = (Integer)vListMap.elementAt(i);
			v.addElement(I);
		}

		Method m = null;
		try
		{
			m = SyncWindows.class.getDeclaredMethod("addWindows", Vector.class);
			m.setAccessible( true ); //bypasses the private modifier
			m.invoke( this, v );
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}


	}
}
