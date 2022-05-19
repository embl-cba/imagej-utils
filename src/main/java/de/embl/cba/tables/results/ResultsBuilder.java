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
package de.embl.cba.tables.results;

import ij.measure.ResultsTable;

/**
 * Builder-type class to hold results
 * Basically it will keep appending results to a larger results table
 * Built in a way where we can chain them together
 * @author oburri
 *
 */

public class ResultsBuilder
{
	/** Results table with all the accumulated results */
	private ResultsTable allResults;
	
	/**
	 * Basic constructor. It creates a default empty table.
	 */
	public ResultsBuilder() { this.allResults = new ResultsTable();	}
	/**
	 * Constructs a ResultsBuilder initializing the default table to the input one.
	 * @param rt initial results table
	 */
	public ResultsBuilder( ResultsTable rt) { this.allResults = rt; }
	
	/**
	 * Add a results table to the already existing table.
	 * @param rt table to add
	 * @return current results builder
	 */
	public ResultsBuilder addResult (ResultsTable rt) {
		// Keep the label and everything in the same order as before, but just append whatever columns do not exist yet
		if(allResults.size() == rt.size() ) {
			for(int c=0; c<=rt.getLastColumn(); c++) {
				String colName = rt.getColumnHeading(c);
				if( !allResults.columnExists(colName)) {
					for(int i=0; i<rt.getCounter(); i++) {
						allResults.setValue(colName, i, rt.getValue(colName, i)); // Currently only supports numbered results...
					}
				}
			}
		} else { // Overwrite
			this.allResults = rt;
		}
		
		return this;
	}
	
	/**
	 * Get the current results table.
	 * @return current results table
	 */
	public ResultsTable getResultsTable() {
		return this.allResults;
	}
}
