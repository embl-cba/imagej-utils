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

import bdv.util.BdvHandle;
import bdv.util.BdvOverlay;
import de.embl.cba.bdv.utils.BdvUtils;
import de.embl.cba.bdv.utils.animate.RelativeTranslationAnimator;
import de.embl.cba.tables.Outlier;
import de.embl.cba.tables.select.SelectionListener;
import de.embl.cba.tables.select.SelectionModel;
import de.embl.cba.tables.tablerow.TableRow;
import net.imglib2.RealPoint;
import net.imglib2.realtransform.AffineTransform2D;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.LinAlgHelpers;

import java.awt.*;
import java.awt.geom.Ellipse2D;


public class SelectedPointOverlay < T extends TableRow > extends BdvOverlay implements SelectionListener< T >
{
	private final BdvHandle bdvHandle;
	private final SelectionModel< T > selectionModel;
	private final String[] columnNames;
	private final TableRowsScatterPlot< T > plotView;
	private RealPoint selectedPoint;
	private int selectionCircleWidth;

	public SelectedPointOverlay( TableRowsScatterPlot< T > plotView )
	{
		super();
		this.bdvHandle = plotView.getBdvHandle();
		this.selectionModel = plotView.getSelectionModel();
		this.columnNames = plotView.getSelectedColumns();
		this.plotView = plotView;

		selectionCircleWidth = 20;
		selectionModel.listeners().add( this );
	}

	public void close()
	{
		selectionModel.listeners().remove( this );
	}

	private void centerViewer( RealPoint selectedPoint, long durationMillis )
	{
		final AffineTransform3D currentViewerTransform = new AffineTransform3D();
		getCurrentTransform3D( currentViewerTransform );

		final double[] globalLocation = { selectedPoint.getDoublePosition( 0 ), selectedPoint.getDoublePosition( 1 ), 0 };
		final double[] currentViewerLocation = new double[ 3 ];
		currentViewerTransform.apply( globalLocation, currentViewerLocation );

		final double[] bdvWindowCenter = BdvUtils.getBdvWindowCenter( this.bdvHandle );

		final double[] translation = new double[ 3 ];
		LinAlgHelpers.subtract( bdvWindowCenter, currentViewerLocation, translation );

		final RelativeTranslationAnimator animator = new RelativeTranslationAnimator(
				currentViewerTransform.copy(),
				translation,
				durationMillis );

		this.bdvHandle.getViewerPanel().setTransformAnimator( animator );
	}

	@Override
	protected void draw( final Graphics2D g )
	{
		if ( selectedPoint == null ) return;

		g.setColor( Color.WHITE );

		final RealPoint viewerPoint = getViewerPoint( selectedPoint );

		final Ellipse2D.Double circle = new Ellipse2D.Double(
				viewerPoint.getDoublePosition( 0 ) - selectionCircleWidth / 2,
				viewerPoint.getDoublePosition( 1 ) - selectionCircleWidth / 2,
				selectionCircleWidth,
				selectionCircleWidth );

		g.draw( circle );
	}

	public RealPoint getViewerPoint( RealPoint globalPoint2D )
	{
		final AffineTransform2D globalToViewerTransform = new AffineTransform2D();
		getCurrentTransform2D( globalToViewerTransform );

		final RealPoint viewerPoint2D = new RealPoint( 0, 0 );
		globalToViewerTransform.apply( globalPoint2D, viewerPoint2D );
		return viewerPoint2D;
	}

	@Override
	public void selectionChanged()
	{

	}

	@Override
	public void focusEvent( T selection )
	{
		if ( bdvHandle == null ) return;

		if ( selection instanceof Outlier )
		{
			if ( ( ( Outlier ) selection ).isOutlier() )
			{
				return;
			}
		}

//		final double x = plotView.getLocation( selection.getCell( columnNames[ 0 ] ), 0 );
//		final double y = plotView.getLocation( selection.getCell( columnNames[ 1 ] ), 1 );
//		selectedPoint = new RealPoint( x, y );
//		centerViewer( selectedPoint, 2000 );
	}
}

