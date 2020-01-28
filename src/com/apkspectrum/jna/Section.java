package com.apkspectrum.jna;

public enum Section 
{
    COMMENT("Comments"),
    INTERNAL_NAME("InternalName"), 
    PRODUCT_NAME("ProductName"), 
    COMPANY_NAME("CompanyName"),
    LEGAL_COPYRIGHT("LegalCopyright"), 
    PRODUCT_VERSION("ProductVersion"), 
    FILE_DESCRIPTION("FileDescription"),
    LEGAL_TRADEMARKS("LegalTrademarks"), 
    PRIVATE_BUILD("PrivateBuild"), 
    FILE_VERSION("FileVersion"),
    ORIGINAL_FILENAME("OriginalFilename"), 
    SPECIAL_BUILD("SpecialBuild");
    
    private final String name;
    
    private Section(String name)
    {
        this.name = name;
    }
    
    @Override
    public String toString()
    {
        return this.name;
    }
}
