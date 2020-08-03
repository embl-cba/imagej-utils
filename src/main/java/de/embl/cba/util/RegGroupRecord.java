package de.embl.cba.util;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class RegGroupRecord
{
	String[] groupValues;
	File parentFile;
	Path parentPath;
	
	public RegGroupRecord( String[] newValues, File newParentFile) {
		groupValues=newValues;
		parentFile=newParentFile;
		parentPath=parentFile.toPath();
	}

	public String[] getValues(){
		return groupValues;
	}
	
	public String getValueByIndex(int index){
		return groupValues[index];
	}
	
	public int getNValues(){
		return groupValues.length;
	}
	
	public File getParentFile(){
		return parentFile;
	}

	public Path getParentPath(){
		return parentPath;
	}

	
	
	@Override
	public boolean equals(Object anotherValue){
		RegGroupRecord castedValue=(RegGroupRecord) anotherValue;
		
		int nGroups=getNValues();
		if (castedValue.getNValues()!=nGroups) 
			return false;
		
		for (int groupIndex=0;groupIndex<nGroups;groupIndex++){
			if (!this.getValueByIndex(groupIndex).equals(castedValue.getValueByIndex(groupIndex)))
				return false;
		}
		
		try{
			return Files.isSameFile(this.getParentPath(), castedValue.getParentPath());
		}catch (Exception ex){
			return false;
		}
	}
}
