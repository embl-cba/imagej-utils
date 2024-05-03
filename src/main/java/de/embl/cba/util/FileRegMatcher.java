/*-
 * #%L
 * Various Java code for ImageJ
 * %%
 * Copyright (C) 2018 - 2024 EMBL
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
package de.embl.cba.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FileRegMatcher
{
	List<File> outputFilesList=null;
	List< RegGroupRecord > groupRecordsList=null;
	
	Pattern fileNamePattern=null;
	String fileNamePatternString=null;
	String[] datasetGroups=null;
	int nGroups;
	
	
	
	public FileRegMatcher(){}
	
	public void setParameters (String fileNameRegularExpression,String[] datasetGroupTags){
		fileNamePatternString=fileNameRegularExpression;
		fileNamePattern=Pattern.compile(fileNamePatternString);
		datasetGroups=datasetGroupTags;
		nGroups=datasetGroups.length;
	}
	
	public void matchFiles(String rootFolder){
		File rootFile=new File(rootFolder);
		
		
		//create output file list
		outputFilesList=new ArrayList<File>();
		groupRecordsList=new ArrayList< RegGroupRecord >();
		
		fillOutputList(rootFile);
		
		return;
	}
	
	public List<File> getMatchedFilesList(){
		return outputFilesList;
	}
	
	private void fillOutputList(File rootFolderFile){
		for (final File fileEntry :rootFolderFile.listFiles()){
			if (fileEntry.isDirectory()) {
				fillOutputList(fileEntry);
			} else {
				checkFile(fileEntry);
		    }
		}
	}

	private void checkFile(File testFile) {
	   Matcher matcher=fileNamePattern.matcher(testFile.getName());
	   if (matcher.matches()){
		   RegGroupRecord newRegGroup=createRegRecordFromMatcher(matcher,testFile.getParentFile());
		   if (!groupRecordsList.contains(newRegGroup)){
			   groupRecordsList.add(newRegGroup);
			   outputFilesList.add(new File(newRegGroup.getParentFile(),replaceGroupsInPatternString(newRegGroup)));
		   }
	   }
	}
	
	RegGroupRecord createRegRecordFromMatcher( Matcher matcher, File parentFile){
		String[] recordValues=new String[nGroups];
		
		for (int groupIndex=0;groupIndex<nGroups;groupIndex++){
			recordValues[groupIndex]=matcher.group(datasetGroups[groupIndex]);
		}
		
		return new RegGroupRecord(recordValues,parentFile);
	}
	
	String replaceGroupInPatternWithValue(String originalString, String groupName, String replacement){
		Pattern replacementPattern=Pattern.compile(String.format("(.*)(\\(\\?\\<%s\\>[^\\)]+\\))(.*)", groupName));
		Matcher replacementMatcher=replacementPattern.matcher(originalString);
		
		String newValue="";
		
		if (replacementMatcher.find())
			newValue=replacementMatcher.replaceFirst(String.format("$1%s$3",replacement));
		
		return newValue;
	}
	
	String replaceGroupsInPatternString( RegGroupRecord targetGroup)
	{
		String newPatternString=fileNamePatternString;
		
		for (int groupIndex=0;groupIndex<nGroups;groupIndex++){
			newPatternString=replaceGroupInPatternWithValue(newPatternString, datasetGroups[groupIndex], targetGroup.getValueByIndex(groupIndex));
		}

		return newPatternString;
	}
	
}
