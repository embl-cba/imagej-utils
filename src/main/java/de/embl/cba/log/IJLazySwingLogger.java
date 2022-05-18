/*-
 * #%L
 * Various Java code for ImageJ
 * %%
 * Copyright (C) 2018 - 2022 EMBL
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
package de.embl.cba.log;

import ij.IJ;
import org.scijava.log.LogService;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class IJLazySwingLogger implements Logger {

    private boolean showDebug = false;

    public boolean isIJLogWindowLogging = true;
    public boolean isFileLogging = false;
    private String logFileDirectory = null;
    private String logFileName = null;
    private String logFilePath = null;

    private LogService logService = null;

    final static Charset ENCODING = StandardCharsets.UTF_8;

    public IJLazySwingLogger()
    {
    }

    public void setLogService( LogService logService )
    {
        this.logService = logService;
    }

    public void setLogFileNameAndDirectory( String logFileName, String logFileDirectory )
    {
        this.logFileDirectory = logFileDirectory;
        this.logFileName = logFileName;
        this.logFilePath = logFileDirectory + File.separatorChar + logFileName;

        File directory = new File( logFileDirectory );

        if (! directory.exists())
        {

            directory.mkdir();
            // If you require it to make the entire directory path including parents,
            // use directory.mkdirs(); here instead.
        }

        List<String> logs = new ArrayList<>();
        logs.add( "Start logging to file." );

        writeSmallTextFile( logs, logFilePath );

    }

    List<String> readSmallTextFile(String aFileName)
    {
        try
        {
            Path path = Paths.get(aFileName);
            return Files.readAllLines(path, ENCODING);
        }
        catch ( IOException e )
        {
            String errorMessage = "Something went wrong accessing the log file " + aFileName;
            if ( isIJLogWindowLogging )
            {
                error( errorMessage );
            }

            List<String> logs = new ArrayList<>();
            logs.add( errorMessage );
            return ( logs );
        }
    }

    void writeSmallTextFile( List<String> aLines, String aFileName )
    {
        try
        {
            Path path = Paths.get( aFileName );
            Files.write(path, aLines, ENCODING);
        }
        catch ( IOException e )
        {

        }
    }

    @Override
    public void setShowDebug( boolean showDebug )
    {
        this.showDebug = showDebug;
    }

    @Override
    public boolean isShowDebug()
    {
        return ( showDebug );
    }

    @Override
    public synchronized void info( String message )
    {
        ijLazySwingAndFileLog( String.format("%s", message) );

        if ( logService != null )
        {
            logService.info( message );
        }
        else
        {
            System.out.print( "[INFO] " + message + "\n" );
        }
    }

    @Override
    public synchronized void progress( String progressId, String progress )
    {
        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {

                String text = String.format("[PROGRESS]: %s %s", progressId, progress);
                ArrayList < String > texts =  new ArrayList<>();
                texts.add( text );
                logProgress( progressId, texts );

            }
        });
    }

    AtomicInteger progressPos = new AtomicInteger(0 );

    @Override
    public synchronized void progress( String header,
                                       ArrayList< String > messages,
                                       long startTime,
                                       long counter, long counterMax)
    {
        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {

                // Tasks
                String countInfo = " " + counter + "/" + counterMax;

                // Time
                long milliseconds = ( System.currentTimeMillis() - startTime );
                double minutes = 1.0 * milliseconds / ( 1000 * 60 );
                double millisecondsPerTask = 1.0 * milliseconds / counter;
                double minutesPerTask = 1.0 * minutes / counter;
                long tasksLeft = counterMax - counter;
                double minutesLeft = 1.0 * tasksLeft * minutesPerTask;

                String timeInfo = String.format(
                        "Time (spent, to-go, per task) [min]: " +
                                "%.1f, %.1f, %.1f", minutes, minutesLeft, minutesPerTask);

                // Memory
                long megaBytes = IJ.currentMemory() / 1000000L;
                long megaBytesAvailable = IJ.maxMemory() / 1000000L;

                String memoryInfo = "Memory (current, avail) [MB]: "
                        + megaBytes + ", " + megaBytesAvailable;

                // Join messages
                ArrayList < String > texts = new ArrayList<>( );

                texts.add( header );
                texts.add( countInfo );
                texts.add( timeInfo );
                texts.add( memoryInfo );

                if ( messages != null )
                {
                    for ( String message : messages )
                    {
                        texts.add( message );
                    }
                }

                logProgress( header, texts );

            }
        });
    }



    private void logProgress( String message, ArrayList < String > texts )
    {

        String jointText = "";

        for ( String text : texts )
        {
            jointText += text + "; ";
        }

        int k = 1; //texts.size()

        if ( isIJLogWindowLogging )
        {
            String logWindowText = jointText;

            if ( IJ.getLog() != null )
            {
                String[] logs = IJ.getLog().split( "\n" );
                if ( logs.length > k )
                {
                    if ( logs[ logs.length - k ].contains( message ) )
                    {
                        logWindowText = "\\Update:" + logWindowText;
                    }
                }
            }
            IJ.log( logWindowText );
        }

        if ( logService != null )
        {
            logService.info( jointText );
        }
        else
        {
            System.out.print( "[INFO] " + jointText + "\n" );
        }

        if ( isFileLogging )
        {
            List< String > logs = readSmallTextFile( logFilePath );
            int i = logs.size() - k;
            if ( i >= 0 )
            {
                if ( logs.get( i ).contains( message ) )
                {
                    logs.set( i, jointText );
                }
                else
                {
                    logs.add( jointText );
                }
            }
            writeSmallTextFile( logs, logFilePath );
        }

    }

    @Override
    public void progressWheel( String message )
    {
        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
                String[] logs = IJ.getLog().split("\n");
                String lastLog = logs[logs.length - 1];

                int currentPos = progressPos.getAndIncrement();
                if (currentPos == bouncingChars.length)
                {
                    currentPos = 0;
                    progressPos.set(0);
                }

                String wheel = bouncingChars[currentPos];

                if (lastLog.contains(message))
                {
                    IJ.log(String.format("\\Update:[PROGRESS]: %s %s", message, wheel));
                }
                else
                {
                    IJ.log(String.format("[PROGRESS]: %s %s", message, wheel));
                }
            }
        });
    }

    @Override
    public synchronized void error(String message)
    {
        ijLazySwingAndFileLog( String.format("ERROR: %s", message) );

        if ( isIJLogWindowLogging )
        {
            IJ.showMessage(String.format("Error: %s", message));
        }

        if ( logService != null )
        {
            logService.error( message );
        }
        else
        {
            System.err.print( "[ERROR] " + message + "\n" );
            System.out.print( "[ERROR] " + message + "\n" );
        }

    }

    @Override
    public synchronized void warning( String message )
    {
        ijLazySwingAndFileLog( String.format("[WARNING]: %s", message) );

        if ( logService != null )
        {
            logService.warn( message );
        }
        else
        {
            System.out.print( "[WARNING] " + message + "\n" );
        }

    }

    @Override
    public synchronized void debug( String message ){
        if ( showDebug )
        {
            ijLazySwingAndFileLog( String.format("[DEBUG]: %s", message) );
        }

        if ( logService != null )
        {
            logService.debug( message );
        }
        else
        {
            System.out.print( "[DEBUG] " + message + "\n" );
        }
    }


    private void ijLazySwingAndFileLog( String message )
    {
        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
                if ( isIJLogWindowLogging )
                {
                    IJ.log( message );
                }


                if ( isFileLogging )
                {
                    List<String> lines = readSmallTextFile(logFilePath);
                    lines.add( message );
                    writeSmallTextFile(lines, logFilePath);
                }
            }
        });
    }


    private String[] wheelChars = new String[]{
            "|", "/", "-", "\\", "|", "/", "-", "\\"
    };

    private String[] bouncingChars = new String[] {

            "(*---------)", // moving -->
            "(-*--------)", // moving -->
            "(--*-------)", // moving -->
            "(---*------)", // moving -->
            "(----*-----)", // moving -->
            "(-----*----)", // moving -->
            "(------*---)", // moving -->
            "(-------*--)", // moving -->
            "(--------*-)", // moving -->
            "(---------*)", // moving -->
            "(--------*-)", // moving -->
            "(-------*--)", // moving -->
            "(------*---)", // moving -->
            "(-----*----)", // moving -->
            "(----*-----)", // moving -->
            "(---*------)", // moving -->
            "(--*-------)", // moving -->
            "(-*--------)", // moving -->

    };
}
