package com.apkspectrum.jna;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.VerRsrc.VS_FIXEDFILEINFO;
import com.sun.jna.platform.win32.Version;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class FileVersion 
{        
    /** This will hold the file version info data **/
    private final Pointer lpData;
    
    /** The name of the file a file version info should be extracted for **/
    private final String fileName;
    
    private final String version;
    
    /** Holds the file info for language codepage found within the file **/
    private final Map<String, FileInfo> langCodePageMapping = new HashMap<>();
    
    /**
     * <p>
     * Creates a new instance of this class and extracts a file info  from the 
     * file passed to this instance.
     * </p>
     * 
     * @param filePath The path of the file to inspect
     * @throws Exception If the file could not be found or if the file does not 
     *                   contain a file info section
     */
    public FileVersion(String filePath) throws Exception
    {
        // http://msdn.microsoft.com/en-us/library/ms647464%28v=vs.85%29.aspx
        //
        // VerQueryValue will take two input and two output parameters
        // 1. parameter: is a pointer to the version-information returned 
        //              by GetFileVersionInfo
        // 2. parameter: will take a string and return an output depending on 
        //               the string:
        //     "\\"
        //         Is the root block and retrieves a VS_FIXEDFILEINFO struct
        //     "\\VarFileInfo\Translation"
        //         will return an array of Var variable information structure 
        //         holding the language and code page identifier
        //     "\\StringFileInfo\\{lang-codepage}\\string-name"
        //         will return a string value of the language and code page 
        //         requested. {lang-codepage} is a concatenation of a language 
        //         and the codepage identifier pair found within the translation 
        //         array in a hexadecimal string! string-name must be one of the
        //         following values:
        //             Comments, InternalName, ProductName, CompanyName, 
        //             LegalCopyright, ProductVersion, FileDescription, 
        //             LegalTrademarks, PrivateBuild, FileVersion, 
        //             OriginalFilename, SpecialBuild
        // 3. parameter: contains the address of a pointer to the requested 
        //               version information in the buffer of the 1st parameter.
        // 4. parameter: contains a pointer to the size of the requested data
        //               pointed to by the 3rd parameter. The length depends on
        //               the input of the 2nd parameter:
        //               *) For root block, the size in bytes of the structure
        //               *) For translation array values, the size in bytes of 
        //                  the array stored at lplpBuffer; 
        //               *) For version information values, the length in 
        //                  character of the string stored at lplpBuffer; 
        
        this.fileName = filePath;
        
        IntByReference dwDummy = new IntByReference();
        dwDummy.setValue(0);

        int versionlength =
                Version.INSTANCE.GetFileVersionInfoSize(filePath, dwDummy);
        
        if (versionlength > 0)
        {
            // will hold the bytes of the FileVersionInfo struct
            byte[] bufferarray = new byte[versionlength];
            // allocates space on the heap (== malloc in C/C++)
            this.lpData = new Memory(bufferarray.length);

            // reads versionLength bytes from the executable file into the 
            // FileVersionInfo struct buffer
            boolean fileInfoResult =
                    Version.INSTANCE.GetFileVersionInfo(
                            filePath, 0, versionlength, lpData);
            
            if (!fileInfoResult)
                throw new Exception("Could not find any file version info inside file "
                    + this.fileName);
            
            // extract the version number
            this.version = getVersionInfo();
            
            // creates a (reference) pointer
            PointerByReference lpTranslate = new PointerByReference();
            IntByReference cbTranslate = new IntByReference();
            // Read the list of languages and code pages
            boolean verQueryVal = Version.INSTANCE.VerQueryValue(
                lpData, "\\VarFileInfo\\Translation", lpTranslate, cbTranslate);
            if (!verQueryVal)
            {
                System.err.println("No translation found!");
            }
            else
            {
                for (Section section : Section.values())
                {
                    this.fillSectionInFile(lpTranslate, cbTranslate, section);
                }
            }
        }
        else
            throw new Exception("No version info found for file "+this.fileName);
    }
    
    /**
     * <p>
     * Returns the name of the file this instance was created for.
     * </p>
     * 
     * @return The name of the file this instance refers to
     */
    public String getFileName()
    {
        return this.fileName;
    }
    
    /**
     * <p>
     * Returns extracted information found within the Windows executable or 
     * library file. For each found language and code page pair found within
     * the provided file an entry will be returned.
     * </p>
     * 
     * @return The extracted information per language and code page definition
     */
    public Collection<FileInfo> getFileInfos()
    {
        return this.langCodePageMapping.values();
    }
    
    /**
     * <p>
     * Returns the version of the file inspected by this instance.
     * </p>
     * 
     * @return The version info of the inspected file
     */
    private String getVersionInfo()
    {
        // will contain the address of a pointer to the requested version 
        // information
        PointerByReference lplpBuffer = new PointerByReference();
        // will contain a pointer to the size of the requested data pointed 
        // to by lplpBuffer. 
        IntByReference puLen = new IntByReference();
        
        // retrieve file description for language and code page "i"
        boolean verQueryVal = Version.INSTANCE.VerQueryValue(
                            this.lpData, "\\", lplpBuffer, puLen);

        if (verQueryVal)
        {
            // contains version information for a file. This information is 
            // language and code page independent
            VS_FIXEDFILEINFO lplpFixedFieldInfo = 
                    new VS_FIXEDFILEINFO(lplpBuffer.getValue());
            lplpFixedFieldInfo.read();

            int v1 = (lplpFixedFieldInfo.dwFileVersionMS).intValue() >> 16;
            int v2 = (lplpFixedFieldInfo.dwFileVersionMS).intValue() & 0xffff;
            int v3 = (lplpFixedFieldInfo.dwFileVersionLS).intValue() >> 16;
            int v4 = (lplpFixedFieldInfo.dwFileVersionLS).intValue() & 0xffff;

            StringBuilder builder = new StringBuilder();
            builder.append(String.valueOf(v1));
            builder.append(".");
            builder.append(String.valueOf(v2));
            builder.append(".");
            builder.append(String.valueOf(v3));
            builder.append(".");
            builder.append(String.valueOf(v4));

            return builder.toString();
        }
        else
            return "No version found";
    }
    
    /**
     * <p>
     * Returns all file descriptions contained in the file inspected by this 
     * instance.
     * </p>
     * 
     * @return A list of found file descriptions 
     */
    private void fillSectionInFile(PointerByReference lpTranslate, IntByReference cbTranslate, Section section)
    {        
        // Read the file description
        // msdn has this example here:
        // for( i=0; i < (cbTranslate/sizeof(struct LANGANDCODEPAGE)); i++ )
        // where LANGANDCODEPAGE is a struct holding two WORDS. A word is
        // 16 bits (2x 8 bit = 2 bytes) long and as the struct contains two
        // words the length of the struct should be 4 bytes long
        for (int i=0; i < (cbTranslate.getValue()/LangAndCodePage.sizeOf()); i++)
        {
            // writes formatted data to the specified string
            // out: pszDest - destination buffer which receives the formatted, null-terminated string created from pszFormat
            // in: ccDest - the size of the destination buffer, in characters. This value must be sufficiently large to accomodate the final formatted string plus 1 to account for the terminating null character.
            // in: pszFormat - the format string. This string must be null-terminated
            // in: ... The arguments to be inserted into the pszFormat string
            // hr = StringCchPrintf(SubBlock, 50,
            //                      TEXT("\\StringFileInfo\\%04x%04x\\FileDescription"),
            //                      lpTranslate[i].wLanguage,
            //                      lpTranslate[i].wCodePage);
            
            // fill the structure with the appropriate values
            LangAndCodePage langCodePage;
            langCodePage = new LangAndCodePage(
                        lpTranslate.getValue(), i*LangAndCodePage.sizeOf());
            langCodePage.read();
                        
            // convert bytes to hex-string: 
            // http://stackoverflow.com/questions/923863/converting-a-string-to-hexadecimal-in-java
            String lang = String.format("%04x", langCodePage.wLanguage);
            String codePage = String.format("%04x",langCodePage.wCodePage);
            
            // store the file info
            String lcp = lang+codePage;
            FileInfo info = langCodePageMapping.get(lcp);
            if (info == null)
            {
                info = new FileInfoImpl(this.fileName, this.version, 
                        lang.toUpperCase(), codePage.toUpperCase());
                langCodePageMapping.put(lcp, info);
            }
                                    
            // build the string for querying the file description stored in 
            // the executable file
            StringBuilder subBlock = new StringBuilder();
            subBlock.append("\\StringFileInfo\\");
            subBlock.append(lang);
            subBlock.append(codePage);
            subBlock.append("\\");
            subBlock.append(section);
            
            String value = getSection(lpData, subBlock.toString());
            ((FileInfoImpl)info).setValue(section, value);
        }
    }
    
    /**
     * <p>
     * Extracts the file description from the file version info struture and
     * returns the found descripton as Java String.
     * </p>
     * 
     * @param lpData The structure holding the data
     * @param subBlock The language and code page whose description should be 
     *                 extracted
     * @return The extracted file description as Java String
     */
    private String getSection(Pointer lpData, String subBlock)
    {
        PointerByReference lpBuffer = new PointerByReference();
        IntByReference dwBytes = new IntByReference();
            
        // Retrieve file description for language and code page "i"
        boolean verQueryVal = Version.INSTANCE.VerQueryValue(
            lpData, subBlock, lpBuffer, dwBytes);
        
        if (verQueryVal && dwBytes.getValue() > 0)
        {
            // a single character is represented by 2 bytes!
            // the last character is the terminating "\n"
            byte[] description = 
                lpBuffer.getValue().getByteArray(0, (dwBytes.getValue()-1)*2);

            return new String(description, StandardCharsets.UTF_16LE);
        }
        else
            return "";
    }
}
