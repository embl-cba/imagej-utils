package de.embl.cba.bdv.utils.command;

import bdv.export.ExportMipmapInfo;
import bdv.ij.export.imgloader.ImagePlusImgLoader.MinMaxOption;
import de.embl.cba.bdv.utils.io.BdvImagePlusExport;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.FolderOpener;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.io.File;


@Plugin(type = Command.class,
		menuPath = "Plugins>BigDataTools>Export>Export Tiff Slices as XML/HDF5")
public class BdvTiffPlanesWriterCommand implements Command
{
	@Parameter( label = "Input folder", style = "directory, open" )
	File inputFolder;

	@Parameter( label = "Output file path (.xml)", style = "save" )
	File xmlOutputPath;

	@Parameter( label = "Voxel unit" )
	String voxelUnit = "micrometer";

	@Parameter( label = "Voxel size X" )
	Double voxelSizeX;

	@Parameter( label = "Voxel size Y" )
	Double voxelSizeY;

	@Parameter( label = "Voxel size Z" )
	Double voxelSizeZ;

	@Override
	public void run()
	{
		if ( ij.Prefs.setIJMenuBar )
			System.setProperty( "apple.laf.useScreenMenuBar", "true" );

		final ImagePlus imp = getImagePlus();
		BdvImagePlusExport.saveAsBdv( imp, xmlOutputPath );
	}

	public ImagePlus getImagePlus()
	{
		final FolderOpener folderOpener = new FolderOpener();
		folderOpener.openAsVirtualStack( true );
		ImagePlus imp = folderOpener.openFolder( inputFolder.getParent() );

		imp.getCalibration().pixelWidth = voxelSizeX;
		imp.getCalibration().pixelHeight = voxelSizeY;
		imp.getCalibration().pixelDepth = voxelSizeZ;
		imp.getCalibration().setUnit( voxelUnit );

		return imp;
	}

	protected static class Parameters
	{
		final boolean setMipmapManual;

		final int[][] resolutions;

		final int[][] subdivisions;

		final File seqFile;

		final File hdf5File;

		final MinMaxOption minMaxOption;

		final double rangeMin;

		final double rangeMax;

		final boolean deflate;

		final boolean split;

		final int timepointsPerPartition;

		final int setupsPerPartition;

		public Parameters(
				final boolean setMipmapManual, final int[][] resolutions, final int[][] subdivisions,
				final File seqFile, final File hdf5File,
				final MinMaxOption minMaxOption, final double rangeMin, final double rangeMax, final boolean deflate,
				final boolean split, final int timepointsPerPartition, final int setupsPerPartition )
		{
			this.setMipmapManual = setMipmapManual;
			this.resolutions = resolutions;
			this.subdivisions = subdivisions;
			this.seqFile = seqFile;
			this.hdf5File = hdf5File;
			this.minMaxOption = minMaxOption;
			this.rangeMin = rangeMin;
			this.rangeMax = rangeMax;
			this.deflate = deflate;
			this.split = split;
			this.timepointsPerPartition = timepointsPerPartition;
			this.setupsPerPartition = setupsPerPartition;
		}
	}


	protected Parameters getParametersAutomated( final int bitDepth,
												 final ExportMipmapInfo autoMipmapSettings,
												 String xmlExportPath )
	{

		String seqFilename = xmlExportPath;

		final String hdf5Filename = xmlExportPath.substring( 0, seqFilename.length() - 4 ) + ".h5";
		final File hdf5File = new File( hdf5Filename );

		if ( !seqFilename.endsWith( ".xml" ) ) seqFilename += ".xml";
		final File seqFile = new File( seqFilename );
		final File parent = seqFile.getParentFile();
		if ( parent == null || ! parent.exists() || ! parent.isDirectory() )
		{
			IJ.showMessage( "Invalid export filename " + seqFilename );
			return null;
		}

		final Parameters parameters = new Parameters( false,
				autoMipmapSettings.getExportResolutions(),
				autoMipmapSettings.getSubdivisions(),
				seqFile,
				hdf5File,
				MinMaxOption.SET,
				0,
				Math.pow( 2, bitDepth ) - 1.0 ,
				true,
				false,
				0,
				0 );

		return parameters;
	}

}
