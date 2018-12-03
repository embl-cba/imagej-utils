package de.embl.cba.bdv.utils;

import bdv.tools.brightness.ConverterSetup;
import bdv.tools.brightness.SliderPanelDouble;
import bdv.util.Bdv;
import bdv.util.BdvStackSource;
import bdv.util.BoundedValueDouble;
import bdv.viewer.VisibilityAndGrouping;
import bdv.viewer.state.SourceState;
import ij.gui.GenericDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public abstract class BdvUserInterfaceUtils
{


	public static void addSourcesDisplaySettingsUI( JPanel panel,
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
				 color );

	}

	public static void addSourcesDisplaySettingsUI( JPanel panel,
													String name,
													Bdv bdv,
													ArrayList< Integer > sourceIndices,
													Color color )
	{
		int[] buttonDimensions = new int[]{ 50, 30 };

		JPanel channelPanel = new JPanel();
		channelPanel.setLayout( new BoxLayout( channelPanel, BoxLayout.LINE_AXIS ) );
		channelPanel.setBorder( BorderFactory.createEmptyBorder(0,10,0,10) );
		channelPanel.add( Box.createHorizontalGlue() );
		channelPanel.setOpaque( true );
		channelPanel.setBackground( color );

		JLabel jLabel = new JLabel( name );
		jLabel.setHorizontalAlignment( SwingConstants.CENTER );

		channelPanel.add( jLabel );
		channelPanel.add( createColorButton( channelPanel, buttonDimensions, bdv, sourceIndices ) );
		channelPanel.add( createBrightnessButton( buttonDimensions, name, bdv, sourceIndices ) );
		channelPanel.add( createVisibilityCheckbox( buttonDimensions, bdv, sourceIndices ) );

		panel.add( channelPanel );

	}

	public static JButton createColorButton( JPanel panel,
											 int[] buttonDimensions,
											 Bdv bdv,
											 ArrayList< Integer > sourceIndices )
	{
		JButton colorButton = new JButton( "C" );
		colorButton.setPreferredSize( new Dimension( buttonDimensions[ 0 ], buttonDimensions[ 1 ] ) );

		colorButton.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				Color color = JColorChooser.showDialog( null, "", null );

				for ( int i : sourceIndices )
				{
					bdv.getBdvHandle().getSetupAssignments().getConverterSetups().get( i ).setColor( BdvUtils.asArgbType( color ) );
				}

				panel.setBackground( color );
			}
		} );


		return colorButton;
	}

	public static JButton createColorButton( JPanel panel,
											 int[] buttonDimensions,
											 BdvStackSource bdvStackSource )
	{

		JButton colorButton;

		if ( BdvUtils.isLabelsSource( bdvStackSource ) )
		{
			colorButton = new JButton( "S" );
		}
		else
		{
			colorButton = new JButton( "C" );
		}

		colorButton.setPreferredSize( new Dimension( buttonDimensions[ 0 ], buttonDimensions[ 1 ] ) );

		colorButton.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				if ( BdvUtils.isLabelsSource( bdvStackSource ) )
				{
					BdvUtils.getLabelsSource( bdvStackSource ).incrementSeed();
					bdvStackSource.getBdvHandle().getViewerPanel().requestRepaint();
				}
				else
				{
					Color color = JColorChooser.showDialog( null, "", null );
					bdvStackSource.setColor( BdvUtils.asArgbType( color ) );
					panel.setBackground( color );
				}

			}
		} );


		return colorButton;
	}

	public static JButton createBrightnessButton( int[] buttonDimensions,
												  String name, Bdv bdv,
												  Integer sourceIndex )
	{
		final ArrayList< Integer > indices = new ArrayList<>();
		indices.add( sourceIndex );
		return createBrightnessButton( buttonDimensions, name, bdv, indices );
	}



	public static JButton createBrightnessButton( int[] buttonDimensions,
												  String name, Bdv bdv,
												  ArrayList< Integer > sourceIndices )
	{
		JButton button = new JButton( "B" );
		button.setPreferredSize( new Dimension( buttonDimensions[ 0 ], buttonDimensions[ 1 ] ) );

		button.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				final ArrayList< ConverterSetup > converterSetups = new ArrayList<>();

				for ( int i : sourceIndices )
				{
					converterSetups.add( bdv.getBdvHandle().getSetupAssignments().getConverterSetups().get( i ) );
				}

				showBrightnessDialog( name, converterSetups );
			}
		} );

		return button;
	}


	public static JButton createBrightnessButton( int[] buttonDimensions,
												  String name,
												  BdvStackSource bdvStackSource )
	{
		JButton button = new JButton( "B" );
		button.setPreferredSize( new Dimension( buttonDimensions[ 0 ], buttonDimensions[ 1 ] ) );


		button.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				final ArrayList< ConverterSetup > converterSetups = getConverterSetups( bdvStackSource );

				showBrightnessDialog( name, converterSetups );
			}
		} );

		return button;
	}


	private static ArrayList< ConverterSetup > getConverterSetups( BdvStackSource bdvStackSource )
	{
		bdvStackSource.setCurrent();
		final int sourceIndex = bdvStackSource.getBdvHandle().getViewerPanel().getVisibilityAndGrouping().getCurrentSource();
		final ArrayList< ConverterSetup > converterSetups = new ArrayList<>();
		converterSetups.add( bdvStackSource.getBdvHandle().getSetupAssignments().getConverterSetups().get( sourceIndex ) );
		return converterSetups;
	}


	public static void showBrightnessDialog( String name, ConverterSetup converterSetup )
	{
		final ArrayList< ConverterSetup > converterSetups = new ArrayList<>();
		converterSetups.add( converterSetup );

		showBrightnessDialog( name, converterSetups );
	}

	public static void showBrightnessDialog( String name, ArrayList< ConverterSetup > converterSetups )
	{

		JFrame frame = new JFrame( name );

		frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

		final BoundedValueDouble min = new BoundedValueDouble( 0, 65535, ( int ) converterSetups.get(0).getDisplayRangeMin() );
		final BoundedValueDouble max = new BoundedValueDouble( 0, 65535, ( int ) converterSetups.get(0).getDisplayRangeMax() );

		JPanel panel = new JPanel();
		panel.setLayout( new BoxLayout( panel, BoxLayout.PAGE_AXIS ) );
		final SliderPanelDouble minSlider = new SliderPanelDouble( "Min", min, 1 );
		final SliderPanelDouble maxSlider = new SliderPanelDouble( "Max", max, 1 );

		final BrightnessUpdateListener brightnessUpdateListener =
				new BrightnessUpdateListener( min, max, converterSetups );

		min.setUpdateListener( brightnessUpdateListener );
		max.setUpdateListener( brightnessUpdateListener );

		panel.add( minSlider );
		panel.add( maxSlider );

		frame.setContentPane( panel );

		//Display the window.
		frame.setBounds( MouseInfo.getPointerInfo().getLocation().x,
				MouseInfo.getPointerInfo().getLocation().y,
				120, 10);
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


	public static JCheckBox createVisibilityCheckbox( int[] buttonDimensions, BdvStackSource bdvStackSource, boolean isVisible )
	{
		JCheckBox checkBox = new JCheckBox( "" );
		checkBox.setSelected( isVisible );
		checkBox.setPreferredSize( new Dimension( buttonDimensions[ 0 ], buttonDimensions[ 1 ] ) );

//		JButton button = new JButton( "T" );
//		button.setPreferredSize( new Dimension( buttonDimensions[ 0 ], buttonDimensions[ 1 ] ) );

		checkBox.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				bdvStackSource.setActive( checkBox.isSelected() );
			}
		} );

		return checkBox;
	}

	public static void addDisplaySettingsUI( Bdv bdv, JPanel panel )
	{
		final java.util.List< ConverterSetup > converterSetups = bdv.getBdvHandle().getSetupAssignments().getConverterSetups();
		final java.util.List< SourceState< ? > > sources = bdv.getBdvHandle().getViewerPanel().getState().getSources();
		final List< Integer > nonOverlaySourceIndices = BdvUtils.getNonOverlaySourceIndices( bdv, sources );
		ArrayList< Color > defaultColors = BdvUtils.getColors( nonOverlaySourceIndices );

		int iColor = 0;
		for ( int sourceIndex : nonOverlaySourceIndices )
		{
			final Color color = defaultColors.get( iColor++ );

			converterSetups.get( sourceIndex ).setColor( BdvUtils.asArgbType( color ) );

			final ArrayList< Integer > indices = new ArrayList<>( );
			indices.add( sourceIndex );

			String name = BdvUtils.getName( bdv, sourceIndex );

			addSourcesDisplaySettingsUI( panel, name, bdv, indices, color );
		}

	}
}
