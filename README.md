[![](https://travis-ci.com/tischi/imagej-utils.svg?branch=master)](https://travis-ci.com/tischi/imagej-utils)

# imagej-utils


## Commands

### Convert Tiff Slices to XML/HDF5

Loads Tiff slices from the specified input folder and saves them in BigDataViewer format.

#### ImageJ user interface

<img width="400" alt="image" src="https://user-images.githubusercontent.com/2157566/67705500-67f5bc80-f9b7-11e9-88c6-df99310166b3.png">

#### ImageJ Macro

```
run("Convert Tiff Slices to XML/HDF5", "inputfolder=/Users/tischer/Desktop/Tiff-slices xmloutputpath=/Users/tischer/Desktop/bdv.xml voxelunit=micrometer voxelsizex=0.7 voxelsizey=0.7 voxelsizez=10.0");
```

#### Java

```
final BdvTiffPlanesWriterCommand command = new BdvTiffPlanesWriterCommand();
command.inputFolder = new File( "/Users/tischer/Desktop/Tiff-slices" );
command.voxelSizeX = 1.0;
command.voxelSizeY = 1.0;
command.voxelSizeZ = 10.0;
command.xmlOutputPath = new File( "/Users/tischer/Desktop/bdv.xml" );
command.run();
```

#### Command line

```
/Applications/Fiji.app/Contents/MacOS/ImageJ-macosx --ij2 --headless --run "Convert Tiff Slices to XML/HDF5" "inputFolder='/Users/tischer/Desktop/Tiff-slices',xmlOutputPath='/Users/tischer/Desktop/bdv.xml',voxelUnit='micrometer',voxelSizeX='0.7',voxelSizeY='0.7',voxelSizeZ='10.0'"
```
