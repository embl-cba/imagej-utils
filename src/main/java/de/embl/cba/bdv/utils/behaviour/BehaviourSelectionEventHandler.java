package de.embl.cba.bdv.utils.behaviour;

import bdv.util.Bdv;
import bdv.viewer.Source;
import de.embl.cba.bdv.utils.*;
import de.embl.cba.bdv.utils.argbconversion.SelectableRealVolatileARGBConverter;
import net.imglib2.RealPoint;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

public class BehaviourSelectionEventHandler
{
	final Bdv bdv;
	final SelectableRealVolatileARGBConverter converter;
	final Source source;
	final Set< Double > selectedValues;
	final String sourceName;

	Behaviours bdvBehaviours;

	private String toggleSelectionTrigger = "ctrl button1";
	private String selectNoneTrigger = "ctrl Q";
	private BdvTableInteractor bdvTableInteractor;


	public BehaviourSelectionEventHandler( Bdv bdv,
										   Source source,
										   SelectableRealVolatileARGBConverter converter,
										   JTable table,
										   String objectLabelColumn )
	{
		this( bdv, source, converter );

		bdvTableInteractor = new BdvTableInteractor( bdv, converter, table, objectLabelColumn );
	}


	/**
	 * Selection of argbconversion (objects) in a label source.
	 * @param bdv Bdv window in which the source is shown.
	 * @param source Source containing numeric values.
	 * @param converter Configurable converter, converting numeric values to colors for display.
	 */
	public BehaviourSelectionEventHandler( Bdv bdv,
										   Source source,
										   SelectableRealVolatileARGBConverter converter )
	{
		this.bdv = bdv;
		this.converter = converter;
		this.source = source;
		this.sourceName = source.getName();
		this.selectedValues = new HashSet<>( );

		installBdvBehaviours();
	}

	public Set< Double > getSelectedValues()
	{
		return selectedValues;
	}

	public void selectNone()
	{
		converter.highlightSelectedValues( null );
		selectedValues.clear();
		BdvUtils.repaint( bdv );
	}

	private void installBdvBehaviours()
	{
		bdvBehaviours = new Behaviours( new InputTriggerConfig() );
		bdvBehaviours.install( bdv.getBdvHandle().getTriggerbindings(),  sourceName + "-bdv-selection-handler" );

		installSelectionBehaviour( toggleSelectionTrigger );

		installSelectNoneBehaviour( selectNoneTrigger );
	}

	private void installSelectNoneBehaviour( String selectNoneTrigger )
	{
		bdvBehaviours.behaviour( ( ClickBehaviour ) ( x, y ) -> {
			selectNone();
		}, sourceName + "-select-none", selectNoneTrigger );
	}


	private void installSelectionBehaviour( String objectSelectionTrigger )
	{
		bdvBehaviours.behaviour( ( ClickBehaviour ) ( x, y ) -> {
			toggleSelectionAtMousePosition();
		}, sourceName+"-toggle-selection", objectSelectionTrigger ) ;
	}

	private void toggleSelectionAtMousePosition()
	{
		final RealPoint globalMouseCoordinates = BdvUtils.getGlobalMouseCoordinates( bdv );

		final double selectedLabel = BdvUtils.getValueAtGlobalCoordinates( source, globalMouseCoordinates, 0 );

		if ( selectedValues.contains( selectedLabel ) )
		{
			selectedValues.remove( selectedLabel );
		}
		else
		{
			selectedValues.add( selectedLabel );
			if ( bdvTableInteractor != null ) bdvTableInteractor.highlightRowInTable( selectedLabel );
		}

		converter.highlightSelectedValues( selectedValues );

		BdvUtils.repaint( bdv );
	}



	public static class BdvTableInteractor
	{
		final private JTable table;
		final private String objectLabelColumn;
		private final Bdv bdv;
		private final SelectableRealVolatileARGBConverter converter;
		private TreeMap< Double, Integer > labelRowMap;

		public BdvTableInteractor( Bdv bdv,
								   SelectableRealVolatileARGBConverter converter,
								   JTable table,
								   String objectLabelColumn )
		{
			this.bdv = bdv;
			this.converter = converter;
			this.table = table;
			this.objectLabelColumn = objectLabelColumn;

			initTableBdvInteraction();
		}

		private void initTableBdvInteraction( )
		{
			table.getSelectionModel().addListSelectionListener( new ListSelectionListener()
			{
				@Override
				public void valueChanged( ListSelectionEvent e )
				{
					if ( e.getValueIsAdjusting() ) return;
					highlightObjectInBdv();
				}
			} );
		}

		public void highlightObjectInBdv()
		{
			final int row = table.convertRowIndexToModel( table.getSelectedRow() );

			if ( table.getModel() instanceof DefaultTableModel )
			{
				final Number objectLabel = (Number) table.getModel().getValueAt(
						row,
						table.getColumnModel().getColumnIndex( objectLabelColumn ) );

				final HashSet< Double > selectedLabel = new HashSet<>();
				selectedLabel.add( objectLabel.doubleValue() );

				converter.highlightSelectedValues( selectedLabel );

				BdvUtils.repaint( bdv );
			}
		}

		private void highlightRowInTable( double selectedLabel )
		{
			new Thread( new Runnable()
			{
				@Override
				public void run()
				{
					if ( table != null )
					{
						final Integer row = getRowInTable( selectedLabel );
						table.setRowSelectionInterval( row, row );
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

		private int getRowInTable( Double objectLabel )
		{
			if ( labelRowMap == null )
			{
				createLabelRowMap();
			}

			return labelRowMap.get( objectLabel );
		}

	}
}

