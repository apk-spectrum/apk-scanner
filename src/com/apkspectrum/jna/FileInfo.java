package com.apkspectrum.jna;

/**
 * <p>
 * Provides access methods to information found within a Windows-based 
 * executable or library file.
 * </p>
 * 
 * @author Roman Vottner
 */
public interface FileInfo
{
    /**
     * <p>
     * Returns the specified language defined for this file info segment.
     * </p>
     * @return The specified language for this file info segment
     */
    public String getLanguage();
    
    /**
     * <p>
     * Returns the specified code page defined for this file info
     * </p>
     * 
     * @return The specified code page for this file info segment
     */
    public String getCodePage();
    
    /**
     * <p>
     * Returns the path to the executable including the executable file itself.
     * </p>
     * 
     * @return The full path to the executable
     */
    public String getFilePath();
    
    /**
     * <p>
     * Returns the version number as extracted from the version field defined in
     * the executable or library file.
     * </p>
     * @return The extracted version number
     */
    public String getVersion();
    
    /**
     * <p>
     * Returns the original file name a defined within the executable or library
     * file.
     * </p>
     * 
     * @return The extracted original file name 
     */
    public String getOriginalFileName();
    
    /**
     * <p>
     * Returns the name of the company that released the executable or library
     * file.
     * </p>
     * 
     * @return The extracted company name
     */
    public String getCompanyName();
    
    /**
     * <p>
     * Returns the description of the executable or library file as viewable in
     * the description of the <em>Windows Task Manager</em>.
     * </p>
     * 
     * @return The extracted file description
     */
    public String getFileDescription();
    
    /**
     * <p>
     * Returns the file version found within the respective language and code 
     * page segment of the executable or library file.
     * </p>
     * 
     * @return The extracted file version based on the language and code page
     */
    public String getFileVersion();
    
    /**
     * <p>
     * Returns the product version number of the executable or library file.
     * </p>
     * 
     * @return The extracted product version
     */
    public String getProductVersion();
    
    /**
     * <p>
     * Returns the product name of the executable or library file.
     * </p>
     * 
     * @return The extracted product name
     */
    public String getProductName();
    
    /**
     * <p>
     * Returns the internal name of the executable or library file.
     * </p>
     * 
     * @return The extracted internal name
     */
    public String getInternalName();
    
    /**
     * <p>
     * Returns the private build number of the executable or libray file.
     * </p>
     * 
     * @return The extracted private build number
     */
    public String getPrivateBuild();
    
    /**
     * <p>
     * Returns the special build number of the executable or library file.
     * </p>
     * 
     * @return The extracted special build number
     */
    public String getSpecialBuild();
    
    /**
     * <p>
     * Returns the legal copyright of the executable or library file.
     * </p>
     * 
     * @return The extracted legal copyright note
     */
    public String getLegalCopyright();
    
    /**
     * <p>
     * Returns the legal trademark note of the executable or library file.
     * </p>
     * 
     * @return The extracted legal trademarke note
     */
    public String getLegalTrademark();
    
    /**
     * <p>
     * Returns the comment specified within the respective language and code 
     * page segment of the executable or library file.
     * </p>
     * 
     * @return The extracted comment
     */
    public String getComment();
}
