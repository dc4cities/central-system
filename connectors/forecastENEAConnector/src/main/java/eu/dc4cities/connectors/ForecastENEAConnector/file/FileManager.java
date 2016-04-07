/*
 * Copyright 2016 The DC4Cities author.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.dc4cities.connectors.ForecastENEAConnector.file;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

/**
 *
 *
 */
public class FileManager
{
    private ArrayList<String> fileList;
    private File processedFiles;
    private PrintWriter pw;


    public FileManager(String rootDir, String processedFilesPath, boolean reset, boolean recursive) throws IOException
    {
        File root = new File(rootDir);
        if (!root.exists() && !root.isDirectory()) {
            if (!root.mkdirs()) {
                throw new IOException("Unable to create directory '" + rootDir + "'");
            }
        }
        this.processedFiles = new File(processedFilesPath);
        if (!this.processedFiles.exists()) {
            if (!processedFiles.getParentFile().mkdirs()) {
                throw new IOException("Unable to create directory '" + processedFiles.getParentFile() + "'");
            }
            if (!processedFiles.createNewFile()) {
                throw new IOException("Unable to create missing file '" + processedFilesPath + "'");
            }
        }
        this.fileList = getFileList(rootDir);
        this.removeInnerDirs();

        this.pw = new PrintWriter(new FileWriter(processedFiles, !reset), true);
    }
    
    public void sort(Comparator cm)
    {
    	Collections.sort(this.fileList, cm);
    }

    private ArrayList<String> getFileList(String rootDir) throws IOException
    {
        ArrayList<String> result = new ArrayList<String>();
        File f = new File(rootDir);
        if (!f.exists() || !f.isDirectory()) {
            throw new IOException("Directory '" + rootDir + "' not found");
        }
        String[] tmp = f.list(null);
        for (String s : tmp) {
    		if (new File(rootDir+File.separator+s).isFile())
    		{
    			result.add(rootDir+File.separator+s);
    		}
    		else
    		{  			
    			if (!s.equalsIgnoreCase("Gb"))
    			{
    				result.addAll(this.getFileList(rootDir+File.separator+s));
    			}    			
    		}
    	}
    	return result;
	}
    

	private void removeInnerDirs() 
    {
		for(String s : this.fileList)
		{
			if(new File(s).isDirectory())
			{
				this.fileList.remove(s);
			}
		}
		
	}

	public boolean hasNext()
    {
		return !this.fileList.isEmpty();
    }

    public File nextFile() throws IOException {
        File f = null;
        boolean found = false;

        for(int i=0 ; !found && i<this.fileList.size() ; i++)
        {
        	File tmp = new File(this.fileList.get(i));
            if(!yetProcessed(tmp))
            {
            	f=tmp;
                if (this.markAsProcessed(f)) {
                    found = true;
                } else {
                    throw new IOException("Unable to mark file '" + f.getName() + "' has processed");
                }
            }
        }
        return f;        
    }

    private boolean yetProcessed(File f) throws IOException
    {
        try (BufferedReader br = new BufferedReader(new FileReader(this.processedFiles)))
        {
            String line;
            while((line=br.readLine()) != null)
            {
                if(line.equalsIgnoreCase(f.getName()))
                {
                    return true;
                }
            }
            return false;
        }
    }

    public boolean markAsProcessed(File f) {
        this.fileList.remove(f.getAbsolutePath());
        this.pw.println(f.getName());
        this.pw.flush();
        return !pw.checkError();
    }
    
    public void removeFileFromList(String f)
    {    	
    	this.fileList.remove(f);
    }
        
    public void addFileList(String[] fileList)
    {
        this.fileList.addAll(Arrays.asList(fileList));
        Collections.sort(this.fileList);
    }

    public void closeFileWriter() throws IOException {
            this.pw.close();
    }
}
