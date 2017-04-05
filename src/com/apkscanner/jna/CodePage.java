package com.apkscanner.jna;

import java.util.HashMap;
import java.util.Map;

public enum CodePage
{
    // see http://msdn.microsoft.com/en-us/library/windows/desktop/aa381058.aspx
    // for proper values for lang and codePage
    
    CODEPAGE_NEUTRAL("0000", "7-bit ASCII"),
    JIS_X_0208("03A4", "Japan (Shift ? JIS X-0208)"),
    KSC_5601("03B5", "Korea (Shift ? KSC 5601)"),
    BIG5("03B6", "Taiwan (Big5)"),
    UNICODE("04B0", "Unicode"),
    LATIN2("04E2", "Latin-2 (Eastern European)"),
    CYRILLIC("04E3", "Cyrillic"),
    MULTILANGUAL("04E4", "Multilingual"),
    GREEK("04E5", "Greek"),
    TURKISH("04E6", "Turkish"),
    HEBREW("04E7", "Hebrew"),
    ARABIC("04E8", "Arabic");
        
    private final String humanReadableName;
    private static Map<String, CodePage> codePages;
    
    private CodePage(String hexCode, String codePage)
    {
        this.humanReadableName = codePage;
        this.addCodePage(hexCode);
    }
    
    private void addCodePage(String hexCode)
    {
        if (codePages == null)
            codePages = new HashMap<>();
        codePages.put(hexCode, this);
    }
    
    public static CodePage map(String hexCode)
    {
        CodePage cp = codePages.get(hexCode);
        if (cp != null)
            return cp;
        return CodePage.CODEPAGE_NEUTRAL;
    }
    
    @Override
    public String toString()
    {
        return this.humanReadableName;
    }
}
