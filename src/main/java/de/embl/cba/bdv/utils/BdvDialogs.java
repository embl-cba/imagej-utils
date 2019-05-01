package de.embl.cba.bdv.utils;

import bdv.tools.brightness.ConverterSetup;
import bdv.tools.brightness.SliderPanelDouble;
import bdv.util.Bdv;
import bdv.util.BdvStackSource;
import bdv.util.BoundedValueDouble;
import bdv.viewer.VisibilityAndGrouping;
import bdv.viewer.state.SourceState;
import de.embl.cba.bdv.utils.converters.LinearARGBConverter;
import ij.gui.GenericDialog;
import net.imglib2.type.numeric.ARGBType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class BdvDialogs
{


	public static void addSourceDisplaySettingsUI( JPanel panel,
												   String name,
												   Bdv bdv,
												   Integer sourceIndex,
												   Color color )
	{

		final ArrayList< Integer > indices = new ArrayList<>();
		indices.add( sourceIndex );

		addSourcesDisplaySettingsUI(  panel,
				 name,
				 bdv,
				 indices,
				 color, 0.0, 65535.0 );

	}

	public static JPanel getSourceDisplaySettingsPanel(
			Bdv bdv,
			String name,
			Integer sourceIndex,
			Color color,
			double rangeMin,
			double rangeMax )
	{

		final ArrayList< Integer > indices = new ArrayList<>();
		indices.add( sourceIndex );

		final JPanel panel = getSourcesDisplaySettingsPanel(
				bdv,
				name,
				indices,
				color,
				rangeMin,
				rangeMax );

		return panel;
	}

	public static JPanel getSourceDisplaySettingsPanel(
			Bdv bdv,
			String name,
			Integer sourceIndex,
			ARGBType color,
			double rangeMin,
			double rangeMax )
	{

		final ArrayList< Integer > indices = new ArrayList<>();
		indices.add( sourceIndex );

		final JPanel panel = getSourcesDisplaySettingsPanel(
				bdv,
				name,
				indices,
				new Color( color.get() ),
				rangeMin,
				rangeMax );

		return panel;
	}

	public static JPanel getSourceDisplaySettingsPanel(
			Bdv bdv,
			Integer sourceIndex,
			double rangeMin,
			double rangeMax )
	{

		final JPanel panel = getSourceDisplaySettingsPanel(
				bdv,
				BdvUtils.getSourceName( bdv, sourceIndex ),
				sourceIndex,
				BdvUtils.getSourceColor( bdv, sourceIndex ),
				rangeMin,
				rangeMax );

		return panel;
	}

	public static JPanel addSourcesDisplaySettingsUI( JPanel parentPanel,
													  String name,
													  Bdv bdv,
													  ArrayList< Integer > sourceIndices,
													  Color color,
													  double rangeMin,
													  double rangeMax )
	{

		JPanel panel =
				getSourcesDisplaySettingsPanel(
						bdv,
						name,
						sourceIndices,
						color,
						rangeMin,
						rangeMax );

		parentPanel.add( panel );

		return panel;

	}

	public static JPanel getSourcesDisplaySettingsPanel(
			Bdv bdv,
			String name,
			ArrayList< Integer > sourceIndices,
			Color color,
			double rangeMin,
			double rangeMax )
	{
		JPanel panel = new JPanel();

		panel.setLayout( new BoxLayout( panel, BoxLayout.LINE_AXIS ) );
		panel.setBorder( BorderFactory.createEmptyBorder(
				0,10,0,10) );
		panel.add( Box.createHorizontalGlue() );
		panel.setOpaque( true );
		panel.setBackground( color );

		JLabel jLabel = new JLabel( name );
		jLabel.setHorizontalAlignment( SwingConstants.CENTER );

		panel.add( jLabel );

		int[] buttonDimensions = new int[]{ 50, 30 };

		panel.add( createColorButton( panel, buttonDimensions,
				bdv, sourceIndices ) );

		panel.add( createBrightnessButton( buttonDimensions, name,
				bdv, sourceIndices, rangeMin, rangeMax ) );

		panel.add( createVisibilityCheckbox( buttonDimensions, bdv,
				sourceIndices ) );
		return panel;
	}

	public static JButton createColorButton( JPanel panel,
											 int[] buttonDimensions,
											 Bdv bdv,
											 ArrayList< Integer > sourceIndices )
	{
		JButton colorButton = new JButton( "C" );
		colorButton.setPreferredSize( new Dimension(
				buttonDimensions[ 0 ], buttonDimensions[ 1 ] ) );

		colorButton.addActionListener( e -> {

			Color color = JColorChooser.showDialog(
					null, "", null );
			if ( color == null ) return;

			for ( int i : sourceIndices )
			{
				bdv.getBdvHandle().getSetupAssignments()
						.getConverterSetups().get( i )
						.setColor( BdvUtils.asArgbType( color ) );
			}

			panel.setBackground( color );
		} );


		return colorButton;
	}

	public static JButton createColorButton( JPanel panel,
											 int[] buttonDimensions,
											 BdvStackSource bdvStackSource )
	{

		JButton colorButton;
		colorButton = new JButton( "C" );

		colorButton.setPreferredSize(
				new Dimension( buttonDimensions[ 0 ], buttonDimensions[ 1 ] ) );

		colorButton.addActionListener( e -> {
			Color color = JColorChooser.showDialog( null, "", null );
			if ( color == null ) return;
			bdvStackSource.setColor( BdvUtils.asArgbType( color ) );
			panel.setBackground( color );
		} );

		return colorButton;
	}

	public static JButton createBrightnessButton( int[] buttonDimensions,
												  String name, Bdv bdv,
												  Integer sourceIndex,
												  double rangeMin,
												  double rangeMax )
	{
		final ArrayList< Integer > indices = new ArrayList<>();
		indices.add( sourceIndex );
		return createBrightnessButton( buttonDimensions, name, bdv,
				indices, rangeMin, rangeMax );
	}



	public static JButton createBrightnessButton( int[] buttonDimensions,
												  String name, Bdv bdv,
												  ArrayList< Integer > sourceIndices,
												  double rangeMin,
												  double rangeMax )
	{
		JButton button = new JButton( "B" );
		button.setPreferredSize(
				new Dimension(
						buttonDimensions[ 0 ],
						buttonDimensions[ 1 ] ) );

		button.addActionListener( e -> {
			final ArrayList< ConverterSetup > converterSetups = new ArrayList<>();

			for ( int i : sourceIndices )
			{
				converterSetups.add( bdv.getBdvHandle()
						.getSetupAssignments()
						.getConverterSetups().get( i ) );
			}

			showBrightnessDialog( name,
					converterSetups,
					rangeMin,
					rangeMax );
		} );

		return button;
	}


	public static JButton createBrightnessButton( int[] buttonDimensions,
												  String name,
												  BdvStackSource bdvStackSource,
												  final double rangeMin,
												  final double rangeMax )
	{
		JButton button = new JButton( "B" );
		button.setPreferredSize( new Dimension(
				buttonDimensions[ 0 ],
				buttonDimensions[ 1 ] ) );

		button.addActionListener( e ->
		{
			final ArrayList< ConverterSetup > converterSetups
					= getConverterSetups( bdvStackSource );

			showBrightnessDialog(
					name,
					converterSetups,
					rangeMin,
					rangeMax );
		} );

		return button;
	}


	private static ArrayList< ConverterSetup > getConverterSetups(
			BdvStackSource bdvStackSource )
	{
		bdvStackSource.setCurrent();
		final int sourceIndex = bdvStackSource.getBdvHandle()
				.getViewerPanel().getVisibilityAndGrouping().getCurrentSource();
		final ArrayList< ConverterSetup > converterSetups = new ArrayList<>();
		converterSetups.add( bdvStackSource.getBdvHandle()
				.getSetupAssignments().getConverterSetups().get( sourceIndex ) );
		return converterSetups;
	}


//	public static void showBrightnessDialog( String name,
//											 ConverterSetup converterSetup )
//	{
//		final ArrayList< ConverterSetup > converterSetups = new ArrayList<>();
//		converterSetups.add( converterSetup );
//
//		showBrightnessDialog(
//				name,
//				converterSetups,
//				0,
//				3 * currentRangeMax );
//	}

	public static void showBrightnessDialog(
			String name,
			ArrayList< ConverterSetup > converterSetups,
			double rangeMin,
			double rangeMax )
	{
		JFrame frame = new JFrame( name );
		frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

		final double currentRangeMin = converterSetups.get( 0 ).getDisplayRangeMin();
		final double currentRangeMax = converterSetups.get( 0 ).getDisplayRangeMax();

		final BoundedValueDouble min =
				new BoundedValueDouble(
						rangeMin,
						rangeMax,
						currentRangeMin );

		final BoundedValueDouble max =
				new BoundedValueDouble(
						rangeMin,
						rangeMax,
						currentRangeMax );

		JPanel panel = new JPanel();
		panel.setLayout( new BoxLayout( panel, BoxLayout.PAGE_AXIS ) );
		final SliderPanelDouble minSlider =
				new SliderPanelDouble( "Min", min, 1 );
		final SliderPanelDouble maxSlider =
				new SliderPanelDouble( "Max", max, 1 );

		final BrightnessUpdateListener brightnessUpdateListener =
				new BrightnessUpdateListener(
						min, max, minSlider, maxSlider, converterSetups );

		min.setUpdateListener( brightnessUpdateListener );
		max.setUpdateListener( brightnessUpdateListener );

		panel.add( minSlider );
		panel.add( maxSlider );

		frame.setContentPane( panel );

		//Display the window.
		frame.setBounds( MouseInfo.getPointerInfo().getLocation().x,
				MouseInfo.getPointerInfo().getLocation().y,
				120, 10);
		frame.setResizable( false );
		frame.pack();
		frame.setVisible( true );

	}

	public static void showBrightnessDialog(
			Bdv bdv,
			String name,
			LinearARGBConverter linearARGBConverter )
	{
		JFrame frame = new JFrame( name );

		frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

		final BoundedValueDouble min = new BoundedValueDouble(
				linearARGBConverter.getMin(),
				linearARGBConverter.getMax(),
				linearARGBConverter.getMin() );

		final BoundedValueDouble max = new BoundedValueDouble(
				linearARGBConverter.getMin(),
				linearARGBConverter.getMax(),
				linearARGBConverter.getMax() );

		JPanel panel = new JPanel();
		panel.setLayout( new BoxLayout( panel, BoxLayout.PAGE_AXIS ) );
		final SliderPanelDouble minSlider = new SliderPanelDouble( "Min", min, 1 );
		final SliderPanelDouble maxSlider = new SliderPanelDouble( "Max", max, 1 );

		class UpdateListener implements BoundedValueDouble.UpdateListener
		{
			@Override
			public void update()
			{
				linearARGBConverter.setMin( min.getCurrentValue() );
				linearARGBConverter.setMax( max.getCurrentValue() );
				BdvUtils.repaint( bdv );
			}
		}

		final UpdateListener updateListener = new UpdateListener();

		min.setUpdateListener( updateListener );
		max.setUpdateListener( updateListener );

		panel.add( minSlider );
		panel.add( maxSlider );

		frame.setContentPane( panel );

		//Display the window.
		frame.setBounds( MouseInfo.getPointerInfo().getLocation().x,
				MouseInfo.getPointerInfo().getLocation().y,
				120, 10);
		frame.setResizable( false );
		frame.pack();
		frame.setVisible( true );

	}

	public static void showGenericBrightnessDialog( ConverterSetup converterSetup )
	{
		GenericDialog gd = new GenericDialog( "LUT max value" );
		gd.addNumericField( "LUT max value: ", converterSetup.getDisplayRangeMax(), 0 );
		gd.showDialog();
		converterSetup.setDisplayRange( converterSetup.getDisplayRangeMin(), ( int ) gd.getNextNumber() );
	}

	public static JCheckBox createVisibilityCheckbox( int[] buttonDimensions,
													  Bdv bdv,
													  ArrayList< Integer > sourceIndices )
	{
		JCheckBox checkBox = new JCheckBox( "" );
		checkBox.setSelected( true );
		checkBox.setPreferredSize( new Dimension( buttonDimensions[ 0 ], buttonDimensions[ 1 ] ) );

		checkBox.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				final VisibilityAndGrouping visibilityAndGrouping = bdv.getBdvHandle().getViewerPanel().getVisibilityAndGrouping();
				for ( int sourceIndex : sourceIndices )
				{
					visibilityAndGrouping.setSourceActive( sourceIndex, checkBox.isSelected() );
				}
			}
		} );

		return checkBox;
	}


	public static JCheckBox createVisibilityCheckbox(
			int[] dims,
			BdvStackSource bdvStackSource,
			boolean isVisible )
	{
		JCheckBox checkBox = new JCheckBox( "" );
		checkBox.setSelected( isVisible );
		checkBox.setPreferredSize( new Dimension( dims[ 0 ], dims[ 1 ] ) );

		checkBox.addActionListener( e ->
				bdvStackSource.setActive( checkBox.isSelected() ) );

		return checkBox;
	}

	public static void addDisplaySettingsUI( Bdv bdv, JPanel panel )
	{
		final java.util.List< ConverterSetup > converterSetups =
				bdv.getBdvHandle().getSetupAssignments().getConverterSetups();
		final java.util.List< SourceState< ? > > sources =
				bdv.getBdvHandle().getViewerPanel().getState().getSources();
		final List< Integer > nonOverlaySourceIndices =
				BdvUtils.getNonOverlaySourceIndices( bdv, sources );
		ArrayList< Color > defaultColors = BdvUtils.getColors( nonOverlaySourceIndices );

		final HashMap< String, JPanel > nameToDisplayPanel = new HashMap<>();

		int iColor = 0;
		for ( int sourceIndex : nonOverlaySourceIndices )
		{
			final Color color = defaultColors.get( iColor++ );

			converterSetups.get( sourceIndex ).setColor( BdvUtils.asArgbType( color ) );

			final ArrayList< Integer > indices = new ArrayList<>( );
			indices.add( sourceIndex );

			String name = BdvUtils.getSourceName( bdv, sourceIndex );

			addSourcesDisplaySettingsUI( panel, name, bdv, indices, color, 0.0, 65535.0 );
		}

	}
}
