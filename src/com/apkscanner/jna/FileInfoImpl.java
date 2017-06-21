package com.apkscanner.jna;

import java.util.HashMap;
import java.util.Map;

public final class FileInfoImpl implements FileInfo
{
    private final String filePath;
    private final String version;
    private final Language language;
    private final CodePage codePage;
    private final Map<Section, String> values;
    
    public FileInfoImpl(final String filePath, final String version, 
        final String language, final String codePage)
    {
        this.filePath = filePath;
        this.version = version;
        this.language = Language.map(language);
        this.codePage = CodePage.map(codePage);
        
        values = new HashMap<>();
    }
    
    public void setValue(Section section, String value)
    {
        values.put(section, value);
    }
    
    @Override
    public String getLanguage()
    {
        return this.language.toString();
    }
    @Override
    public String getCodePage()
    {
        return this.codePage.toString();
    }

    @Override
    public String getFilePath()
    {
        return this.filePath;
    }

    @Override
    public String getVersion()
    {
        return this.version;
    }
    
    @Override
    public String getFileDescription()
    {
        return this.values.get(Section.FILE_DESCRIPTION);
    }

    @Override
    public String getOriginalFileName()
    {
        return this.values.get(Section.ORIGINAL_FILENAME);
    }

    @Override
    public String getCompanyName()
    {
        return this.values.get(Section.COMPANY_NAME);
    }

    @Override
    public String getFileVersion()
    {
        return this.values.get(Section.FILE_VERSION);
    }

    @Override
    public String getProductVersion()
    {
        return this.values.get(Section.PRODUCT_VERSION);
    }

    @Override
    public String getProductName()
    {
        return this.values.get(Section.PRODUCT_NAME);
    }

    @Override
    public String getInternalName()
    {
        return this.values.get(Section.INTERNAL_NAME);
    }

    @Override
    public String getPrivateBuild()
    {
        return this.values.get(Section.PRIVATE_BUILD);
    }

    @Override
    public String getSpecialBuild()
    {
        return this.values.get(Section.SPECIAL_BUILD);
    }

    @Override
    public String getLegalCopyright()
    {
        return this.values.get(Section.LEGAL_COPYRIGHT);
    }

    @Override
    public String getLegalTrademark()
    {
        return this.values.get(Section.LEGAL_TRADEMARKS);
    }

    @Override
    public String getComment()
    {
        return this.values.get(Section.COMMENT);
    }
    
    /**
     * <p>
     * Returns all the information found within the Windows executable or 
     * library file.
     * </p>
     * 
     * @return The information found within the executable or library file
     */
    @Override
    public String toString()
    {
        StringBuilder sb;
        sb = new StringBuilder();
        sb.append("File: ");
        sb.append(this.filePath);
        sb.append("\nVersion: ");
        sb.append(this.version);
        sb.append("\nLanguage: ");
        sb.append(this.language);
        sb.append("\nCodePage: ");
        sb.append(this.codePage);
        sb.append("\nOriginal-Filename: ");
        sb.append(this.values.get(Section.ORIGINAL_FILENAME));
        sb.append("\nCompany-Name: ");
        sb.append(this.values.get(Section.COMPANY_NAME));
        sb.append("\nFile-Description: ");
        sb.append(this.values.get(Section.FILE_DESCRIPTION));
        sb.append("\nFile-Version: ");
        sb.append(this.values.get(Section.FILE_VERSION));
        sb.append("\nProduct-Version: ");
        sb.append(this.values.get(Section.PRODUCT_VERSION));
        sb.append("\nProduct-Name: ");
        sb.append(this.values.get(Section.PRODUCT_NAME));
        sb.append("\nInternal-Name: ");
        sb.append(this.values.get(Section.INTERNAL_NAME));
        sb.append("\nPrivate-Build: ");
        sb.append(this.values.get(Section.PRIVATE_BUILD));
        sb.append("\nSpecial-Build: ");
        sb.append(this.values.get(Section.SPECIAL_BUILD));
        sb.append("\nLegal-Copyright: ");
        sb.append(this.values.get(Section.LEGAL_COPYRIGHT));
        sb.append("\nLegal-Trademark: ");
        sb.append(this.values.get(Section.LEGAL_TRADEMARKS));
        sb.append("\nComment: ");
        sb.append(this.values.get(Section.COMMENT));
        
        return sb.toString();
    }
}
