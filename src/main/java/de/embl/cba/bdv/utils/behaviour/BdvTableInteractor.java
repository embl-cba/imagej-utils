package de.embl.cba.bdv.utils.behaviour;

import bdv.util.Bdv;
import de.embl.cba.bdv.utils.BdvUtils;
import de.embl.cba.bdv.utils.argbconversion.SelectableRealVolatileARGBConverter;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.util.HashSet;
import java.util.TreeMap;

public class BdvTableInteractor
{
	final private JTable table;
	final private String objectLabelColumn;
	final private Bdv bdv;
	final private SelectableRealVolatileARGBConverter converter;

	private TreeMap< Double, Integer > labelRowMap;
	private Integer mostRecentBdvTriggeredHighlightedRow;

	public BdvTableInteractor( Bdv bdv,
							   SelectableRealVolatileARGBConverter converter,
							   JTable table,
							   String objectLabelColumn )
	{
		this.bdv = bdv;
		this.converter = converter;
		this.table = table;
		this.objectLabelColumn = objectLabelColumn;
		this.mostRecentBdvTriggeredHighlightedRow = -1;

		initBdvTableListening();
	}

	private void initBdvTableListening( )
	{
		table.getSelectionModel().addListSelectionListener( new ListSelectionListener()
		{
			@Override
			public void valueChanged( ListSelectionEvent e )
			{
				if ( e.getValueIsAdjusting() ) return;

				final int selectedRow = table.convertRowIndexToModel( table.getSelectedRow() );

				if ( selectedRow == mostRecentBdvTriggeredHighlightedRow ) return;

				mostRecentBdvTriggeredHighlightedRow = -1;

				exclusivelyHighlightObjectInSelectedRowInBdv( selectedRow );
			}
		} );
	}

	public void exclusivelyHighlightObjectInSelectedRowInBdv( int selectedRow )
	{

		if ( table.getModel() instanceof DefaultTableModel )
		{
			final Number objectLabel = (Number) table.getModel().getValueAt(
					selectedRow,
					table.getColumnModel().getColumnIndex( objectLabelColumn ) );

			highlightLabelInConverter( objectLabel );

			BdvUtils.repaint( bdv );
		}
	}

	public void highlightLabelInConverter( Number objectLabel )
	{
		final HashSet< Double > selectedLabel = new HashSet<>();
		selectedLabel.add( objectLabel.doubleValue() );
		converter.highlightSelectedValues( selectedLabel );
	}

	public void highlightRowInTable( double selectedLabel )
	{
		new Thread( new Runnable()
		{
			@Override
			public void run()
			{
				if ( table != null )
				{
					mostRecentBdvTriggeredHighlightedRow = getRowInTable( selectedLabel );
					table.setRowSelectionInterval( mostRecentBdvTriggeredHighlightedRow, mostRecentBdvTriggeredHighlightedRow );
				}
			}
		} ).start();
	}


	private void createLabelRowMap()
	{
		labelRowMap = new TreeMap();

		final int labelColumnIndex = table.getColumnModel().getColumnIndex( objectLabelColumn );

		final int rowCount = table.getRowCount();
		for ( int row = 0; row < rowCount; row++ )
		{
			labelRowMap.put(
					( Double ) table.getValueAt( row, labelColumnIndex ),
					( Integer ) row );
		}
	}

	private int getRowInTable( double objectLabel )
	{
		if ( labelRowMap == null )
		{
			createLabelRowMap();
		}

		return labelRowMap.get( objectLabel );
	}

}
