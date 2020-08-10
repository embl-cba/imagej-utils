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
package de.embl.cba.tables.command;

import de.embl.cba.tables.FileUtils;
import de.embl.cba.tables.Logger;
import de.embl.cba.tables.Tables;
import de.embl.cba.tables.table.ConcatenatedTableModel;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


@Plugin(type = Command.class, menuPath = "Plugins>Tables>Concatenate Tables" )
public class ConcatTablesCommand< R extends RealType< R > > implements Command
{
	@Parameter ( label = "Input directory", style = "directory" )
	public File directory;

	@Parameter ( label = "Output table file", style = "save" )
	public File outputTable;

	@Parameter ( label = "Regular expression" )
	public String regExp = ".*";

	@Override
	public void run()
	{
		final List< File > files = FileUtils.getFileList( directory, regExp, true );

		final ArrayList< TableModel > models = new ArrayList<>();
		for ( File file : files )
		{
			Logger.info( "Loading: " + file );
			models.add( Tables.loadTable( file.getAbsolutePath() ).getModel() );
		}

		final ConcatenatedTableModel concat = new ConcatenatedTableModel( models );

		Logger.info( "Saving: " + outputTable );
		Tables.saveTable( new JTable( concat ), outputTable );

		Logger.info( "Done!" );
	}


}
