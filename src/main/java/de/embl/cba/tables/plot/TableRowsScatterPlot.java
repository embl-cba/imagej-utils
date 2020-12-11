/*-
 * #%L
 * TODO
 * %%
 * Copyright (C) 2018 - 2020 EMBL
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package de.embl.cba.tables.plot;

import bdv.util.*;
import de.embl.cba.DebugHelper;
import de.embl.cba.bdv.utils.BdvUtils;
import de.embl.cba.bdv.utils.popup.BdvPopupMenus;
import de.embl.cba.tables.color.SelectionColoringModel;
import de.embl.cba.tables.select.SelectionModel;
import de.embl.cba.tables.tablerow.TableRow;
import ij.IJ;
import net.imglib2.*;
import net.imglib2.neighborsearch.NearestNeighborSearchOnKDTree;
import net.imglib2.position.FunctionRealRandomAccessible;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.ARGBType;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;

import javax.swing.*;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class TableRowsScatterPlot< T extends TableRow >
{
	private final List< T > tableRows;
	private final SelectionColoringModel< T > selectionColoringModel;
	private final SelectionModel< T > selectionModel;

	private String[] selectedColumns;
	private SelectedPointOverlay selectedPointOverlay;
	private double[] scaleFactors;
	private double dotSizeScaleFactor;
	private BdvHandle bdvHandle;

	public TableRowsScatterPlot(
			List< T > tableRows,
			SelectionColoringModel< T > selectionColoringModel,
			String[] selectedColumns,
			double[] scaleFactors,
			double dotSizeScaleFactor )
	{
		this.tableRows = tableRows;
		this.selectionColoringModel = selectionColoringModel;
		this.selectionModel = selectionColoringModel.getSelectionModel();
		this.selectedColumns = selectedColumns;
		this.scaleFactors = scaleFactors;
		this.dotSizeScaleFactor = dotSizeScaleFactor;

		//this.axisLabelsFontSize = axisLabelsFontSize;
		//this.lineOverlay = lineOverlay;
	}

	public void show()
	{
		show( null );
	}

	public void show( JComponent parentComponent )
	{
		if ( parentComponent != null )
		{
			JFrame topFrame = ( JFrame ) SwingUtilities.getWindowAncestor( parentComponent );
			final int x = topFrame.getLocationOnScreen().x + parentComponent.getWidth() + 10;
			final int y = topFrame.getLocationOnScreen().y;
			createAndShowScatterPlot( x, y );
		}
		else
		{
			createAndShowScatterPlot( 10, 10 );
		}
	}

	private void createAndShowScatterPlot( int x, int y )
	{
		TableRowKDTreeSupplier< T > kdTreeSupplier = new TableRowKDTreeSupplier<>( tableRows, selectedColumns, scaleFactors );
		double[] min = kdTreeSupplier.getMin();
		double[] max = kdTreeSupplier.getMax();

		double aspectRatio = ( max[ 1 ] - min[ 1 ] ) / ( max[ 0 ] - min[ 0 ] );
		if ( aspectRatio > 10 || aspectRatio < 0.1 )
		{
			IJ.showMessage( "The aspect ratio, (yMax-yMin)/(xMax-xMin), of your data is " + aspectRatio + "." +
					"\nIn order to see anything you may have to scale either the x or y values" +
					"\nsuch that this ratio becomes closer to one." +
					"\nYou can do so by right-clicking into the scatter plot" +
					"\nand selecting \"Reconfigure...\"");
		}

		Supplier< BiConsumer< RealPoint, ARGBType > > biConsumerSupplier = new RealPointARGBTypeBiConsumerSupplier<>( kdTreeSupplier, selectionColoringModel,  dotSizeScaleFactor *( min[ 0 ] - max[ 0 ] ) / 100.0 );

		FunctionRealRandomAccessible< ARGBType > randomAccessible = new FunctionRealRandomAccessible( 2, biConsumerSupplier, ARGBType::new );

		bdvHandle = show( randomAccessible, FinalInterval.createMinMax( (long) min[ 0 ], (long) min[ 1 ], 0, (long) Math.ceil( max[ 0 ] ), (long) Math.ceil( max[ 1 ] ), 0 ), selectedColumns );

		selectionColoringModel.listeners().add( () -> {
			bdvHandle.getViewerPanel().requestRepaint();
		} );

		installBdvBehaviours( new NearestNeighborSearchOnKDTree< T >( kdTreeSupplier.get() ) );

//		viewerTransform = viewerTransform( bdvHandle, dataInterval, viewerAspectRatio );
//
//		registerAsViewerTransformListener();
//
//		bdvHandle.getViewerPanel().setCurrentViewerTransform( viewerTransform );
//

//
//		setWindowPosition( x, y );
//
//		addGridLinesOverlay();

		//addAxisTickLabelsOverlay();

		//addSelectedPointsOverlay();
	}


	private void addSelectedPointsOverlay()
	{
		if ( selectedPointOverlay != null )
			selectedPointOverlay.close();

		selectedPointOverlay = new SelectedPointOverlay( this );

		BdvFunctions.showOverlay( selectedPointOverlay, "selected point overlay", BdvOptions.options().addTo( bdvHandle ).is2D() );
	}

//	private void addGridLinesOverlay()
//	{
//		GridLinesOverlay gridLinesOverlay = new GridLinesOverlay( bdvHandle, columnNames, columnNameY, dataPlotInterval, lineOverlay, axisLabelsFontSize );
//
//		BdvFunctions.showOverlay( gridLinesOverlay, "grid lines overlay", BdvOptions.options().addTo( bdvHandle ).is2D() );
//	}

//	private void addAxisTickLabelsOverlay()
//	{
//		AxisTickLabelsOverlay scatterPlotGridLinesOverlay = new AxisTickLabelsOverlay( xLabelToIndex, yLabelToIndex, dataInterval );
//
//		BdvFunctions.showOverlay( scatterPlotGridLinesOverlay, "axis tick labels overlay", BdvOptions.options().addTo( bdvHandle ).is2D() );
//	}

	private void installBdvBehaviours( NearestNeighborSearchOnKDTree< T > search )
	{
		Behaviours behaviours = new Behaviours( new InputTriggerConfig() );
		behaviours.install( bdvHandle.getTriggerbindings(), "scatterplot" + selectedColumns[ 0 ] + selectedColumns[ 1 ] );

		BdvPopupMenus.addAction( bdvHandle,"Focus closest point [ Ctrl Left-Click ]",
				( x, y ) -> focusClosestPoint( search )
		);

		behaviours.behaviour( ( ClickBehaviour ) ( x, y ) -> focusClosestPoint( search ), "Focus closest point", "ctrl button1" ) ;

		BdvPopupMenus.addAction( bdvHandle,"Reconfigure...",
				( x, y ) -> {

					ScatterPlotDialog dialog = new ScatterPlotDialog( tableRows.get( 0 ).getColumnNames().stream().toArray( String[]::new ), selectedColumns, scaleFactors, dotSizeScaleFactor );
					if ( dialog.show() )
					{
						selectedColumns = dialog.getSelectedColumns();
						scaleFactors = dialog.getScaleFactors();
						dotSizeScaleFactor = dialog.getDotSizeScaleFactor();

						final int xLoc = SwingUtilities.getWindowAncestor( bdvHandle.getViewerPanel() ).getLocationOnScreen().x;
						final int yLoc = SwingUtilities.getWindowAncestor( bdvHandle.getViewerPanel() ).getLocationOnScreen().y;
						bdvHandle.close();
						createAndShowScatterPlot( xLoc, yLoc );
					}
				}
		);
	}

	private synchronized void focusClosestPoint( NearestNeighborSearchOnKDTree< T > search )
	{
		final RealPoint realPoint = new RealPoint( 3 );
		bdvHandle.getViewerPanel().getGlobalMouseCoordinates( realPoint );
		RealPoint realPoint2d = new RealPoint( realPoint.getDoublePosition( 0 ), realPoint.getDoublePosition( 1 ) );
		search.search( realPoint2d );
		final T tableRow = search.getSampler().get();

		if ( tableRow != null )
			selectionModel.focus( search.getSampler().get() );
		else
			throw new RuntimeException( "No closest point found." );
	}

	private RealPoint getViewerMouse3dPosition()
	{
		final RealPoint mouse2d = new RealPoint( 0, 0 );
		bdvHandle.getViewerPanel().getMouseCoordinates( mouse2d );
		final RealPoint mouse3d = new RealPoint( 3 );
		for ( int d = 0; d < 2; d++ )
		{
			mouse3d.setPosition( mouse2d.getDoublePosition( d ), d );
		}
		return mouse3d;
	}

	private static BdvHandle show( FunctionRealRandomAccessible< ARGBType > randomAccessible, FinalInterval interval, String[] selectedColumns )
	{
		Prefs.showMultibox( false );

		return BdvFunctions.show(
				randomAccessible,
				interval,
				getPlotName( selectedColumns ),
				BdvOptions.options().is2D().numRenderingThreads( 1 ).frameTitle( getPlotName( selectedColumns ) ) ).getBdvHandle();
	}

	private static String getPlotName( String[] selectedColumns )
	{
		return "x: " + selectedColumns[ 0 ] + ", y: " + selectedColumns[ 1 ];
	}


	public AffineTransform3D viewerTransform( BdvHandle bdvHandle, FinalRealInterval dataInterval, double viewerAspectRatio )
	{
		AffineTransform3D viewerTransform = new AffineTransform3D();
		// bdvHandle.getViewerPanel().getState().getViewerTransform( viewerTransform );

		AffineTransform3D reflectY = new AffineTransform3D();
		reflectY.set( -1.0, 1, 1 );
		viewerTransform.preConcatenate( reflectY );

		final AffineTransform3D scale = new AffineTransform3D();

		final double scaleX = 1.0 * BdvUtils.getBdvWindowWidth( bdvHandle ) / ( dataInterval.realMax( 0 ) - dataInterval.realMin( 0 ) );

		final double zoom = 1.0;
		scale.scale( zoom * scaleX, zoom * scaleX * viewerAspectRatio, 1.0  );
		viewerTransform.preConcatenate( scale );

		FinalRealInterval scaledBounds = viewerTransform.estimateBounds( dataInterval );

		shiftViewerTransformToDataCenter( viewerTransform, dataInterval, bdvHandle );

		FinalRealInterval finalBounds = viewerTransform.estimateBounds( dataInterval );

		return viewerTransform;
	}

	private static void shiftViewerTransformToDataCenter( AffineTransform3D viewerTransform, FinalRealInterval dataInterval, BdvHandle bdvHandle )
	{
		FinalRealInterval bounds = viewerTransform.estimateBounds( dataInterval );
		final AffineTransform3D translate = new AffineTransform3D();
		translate.translate( - ( bounds.realMin( 0 ) ), - ( bounds.realMin( 1 ) ), 0 );
		viewerTransform.preConcatenate( translate );
		bounds = viewerTransform.estimateBounds( dataInterval );

		final double[] translation = new double[ 2 ];
		translation[ 0 ] = 0.5 * ( BdvUtils.getBdvWindowWidth( bdvHandle ) - bounds.realMax( 0 ) );
		translation[ 1 ] = 0.5 * ( BdvUtils.getBdvWindowHeight( bdvHandle ) - bounds.realMax( 1 ));

		final AffineTransform3D translate2 = new AffineTransform3D();
		translate2.translate( translation[ 0 ], translation[ 1 ], 0 );
		viewerTransform.preConcatenate( translate2 );
	}


	public void setWindowPosition( int x, int y )
	{
		BdvUtils.getViewerFrame( bdvHandle ).setLocation( x, y );
	}

	public List< T > getTableRows()
	{
		return tableRows;
	}

	public BdvHandle getBdvHandle()
	{
		return bdvHandle;
	}

	public SelectionModel< T > getSelectionModel()
	{
		return selectionModel;
	}

	public String[] getSelectedColumns()
	{
		return selectedColumns;
	}
}
