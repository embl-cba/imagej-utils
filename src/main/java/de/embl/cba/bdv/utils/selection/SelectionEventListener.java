package de.embl.cba.bdv.utils.selection;

public interface SelectionEventListener
{
	void valueSelected( double value, int timepoint );
	void valueUnselected( double value, int timepoint );
}
