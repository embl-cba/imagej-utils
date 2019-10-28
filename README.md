# bdv-utils


## Commands

## Export Tiff slices as XML/HDF5

Loads Tiff slices from the specified input folder and saves them in BigDataViewer format.

### ImageJ user interface

<img width="587" alt="image" src="https://user-images.githubusercontent.com/2157566/67704541-970b2e80-f9b5-11e9-8e81-aa41b8d19c17.png">

### ImageJ Macro

```
run("Export Tiff Slices as XML/HDF5", "inputfolder=/Users/tischer/Desktop/Tiff-slices xmloutputpath=/Users/tischer/Desktop/bdv.xml voxelunit=micrometer voxelsizex=0.7 voxelsizey=0.7 voxelsizez=10.0");
```

### Java

```
final BdvTiffPlanesWriterCommand command = new BdvTiffPlanesWriterCommand();

command.inputFolder = new File( "/Users/tischer/Desktop/Tiff-slices" );
command.voxelSizeX = 1.0;
command.voxelSizeY = 1.0;
command.voxelSizeZ = 20.0;
command.xmlOutputPath = new File( "/Users/tischer/Desktop/bdv.xml" );

command.run();
```

### Command line

```
/Applications/Fiji.app/Contents/MacOS/ImageJ-macosx --ij2 --headless --run "Export Tiff Slices as XML/HDF5" "inputFolder='/Users/tischer/Desktop/Tiff-slices',xmlOutputPath='/Users/tischer/Desktop/bdv.xml',voxelUnit='micrometer',voxelSizeX='0.7',voxelSizeY='0.7',voxelSizeZ='10.0'")
```
