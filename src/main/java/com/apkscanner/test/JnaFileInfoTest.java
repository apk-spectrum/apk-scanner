/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.apkscanner.test;

import java.util.ArrayList;
import java.util.List;

import com.apkspectrum.jna.FileInfo;
import com.apkspectrum.jna.FileVersion;

/**
 *
 * @author Roman Vottner
 */
public class JnaFileInfoTest
{
    public static void main(String[] args)
    {
        List<String> fileNames = new ArrayList<>();
        fileNames.add("C:\\Windows\\explorer.exe");
        fileNames.add("C:\\Program Files\\Internet Explorer\\ielowutil.exe");
        fileNames.add("C:\\Program Files\\Java\\jre7\\bin\\java.exe");
        fileNames.add("C:\\Program Files (x86)\\IDM Computer Solutions\\UltraEdit\\Uedit32.exe");
        fileNames.add("c:\\windows\\system32\\notepad.exe");

        List<FileVersion> files = addFiles(fileNames);
        

    	try {
			new FileVersion("C:\\Program Files (x86)\\IDM Computer Solutions\\UltraEdit\\Uedit32.exe");
		} catch (Exception e1) {
			e1.printStackTrace();
		}
        
        
        try
        {
        	
            for (FileVersion fileVersion : files)
            {
                System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
                System.out.println("XXX Information contained in: "+fileVersion.getFileName());
                System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
                for(FileInfo info : fileVersion.getFileInfos())
                {
                    System.out.println(info);
                    System.out.println();
                }
            }
        }
        catch (Exception e)
        {
            System.err.println(e.getLocalizedMessage());
        }
    }
    
    /**
     * <p>
     * Creates a FileVersion instance for each passed file if the file does not
     * throw an exception.
     * </p>
     * 
     * @param names The names of the files to create FileVersion instances for
     * @return The list of created FileVersion instances
     */
    private static List<FileVersion> addFiles(List<String> names)
    {
        List<FileVersion> files = new ArrayList<>();
        for (String fileName : names)
        {
            try
            {
                files.add(new FileVersion(fileName));
            }
            catch (Exception e)
            {
                System.err.println(e.getLocalizedMessage());
            }
        }
        return files;
    }
}
