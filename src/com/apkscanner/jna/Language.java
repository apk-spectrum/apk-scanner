package com.apkscanner.jna;

import java.util.HashMap;
import java.util.Map;

public enum Language
{
    // see http://msdn.microsoft.com/en-us/library/windows/desktop/aa381058.aspx
    // for proper values for lang and codePage
    
    LANGUAGE_NEUTRAL("0000", "Language Neutral"),
    ARABIC("0401", "Arabic"),
    BULGARIAN("0402", "Bulgarian"),
    CATALAN("0403", "Catalan"),
    CHINESE_TRADITIONAL("0404", "Traditional Chinese"),
    CZECH("0405", "Czech"),
    DANISH("0406", "Danish"),
    GERMAN("0407", "German"), 
    GREEK("0408", "Greek"), 
    ENGLISH_US("0409", "U.S. English"), 
    SPANISH_CASTILIAN("040A", "Castilian Spanish"), 
    FINNISH("040B", "Finnish"), 
    FRENCH("040C", "French"), 
    HEBREW("040D", "Hebrew"), 
    HUNGARIAN("040E", "Hungarian"), 
    ICELANDIC("040F", "Icelandic"), 
    ITALIAN("0410", "Italian"), 
    JAPANESE("0411", "Japanese"), 	
    KOREAN("0412", "Korean"), 
    DUTCH("0413", "Dutch"), 	
    NOWEGIAN_BOKMAL("0414", "Norwegian ? Bokmal"), 	
    ITALIAN_SWISS("0810", "Swiss Italian"), 	
    DUTCH_BELGIAN("0813", "Belgian Dutch"), 	
    NOWEGIAN_NYORSK("0814", "Norwegian ? Nynorsk"),   
    POLISH("0415", "Polish"), 
    PORTUGESE_BRAZIL("0416", "Portuguese (Brazil)"), 
    RHAETO_ROMANIC("0417", "Rhaeto-Romanic"), 
    ROMANIAN("0418", "Romanian"), 
    RUSSIAN("0419", "Russian"), 
    SERBO_CROATIAN_LATIN("041A", "Croato-Serbian (Latin)"), 
    SLOVAK("041B", "Slovak"),
    ALBANIAN("041C", "Albanian"), 
    SWEDISH("041D", "Swedish"), 
    THAI("041E", "Thai"), 
    TURKISH("041F", "Turkish"), 
    URDU("0420", "Urdu"), 
    BAHASA("0421", "Bahasa"), 
    CHINESE_SIMPLIFIED("0804", "Simplified Chinese"), 
    GERMAN_SWISS("0807", "Swiss German"), 
    ENGLISH_UK("0809", "U.K. English"), 
    SPANISH_MEXICO("080A", "Spanish (Mexico)"), 
    FRENCH_BELGIAN("080C", "Belgian French"), 
    FRENCH_CANADIAN("0C0C", "Canadian French"), 
    FRENCH_SWISS("100C", "Swiss French"), 
    PORTUGUESE_PORTUGAL("0816", "Portuguese (Portugal)"), 
    SERBO_CROATIAN_CYRILLIC("081A", "Serbo-Croatian (Cyrillic)"); 
    
    private final String humanReadableName;
    private static Map<String, Language> languages;
    
    private Language(String hexCode, String humanReadableName)
    {
        this.humanReadableName = humanReadableName;
        addLanguage(hexCode);
    }
    
    /**
     * <p>
     * Due to the initialization order instance declarations are executed before
     * static classlevel declarations (as static declarations get executed in 
     * the order of appearance and enums are actually static fields) a check has 
     * to be done if the Map, holdig the mpping between hexCode and human 
     * readable output, is already initialized and initialize it if it was not. 
     * </p>
     * <p>Furhtermore, as the initialization of static class fields is not 
     * admissable within the constructor it has to be refactored to s method 
     * invoked by the constructor. This prevents moreover the declaration of the
     * Map as final.
     * </p>
     * 
     * @param hexCode 
     */
    private void addLanguage(String hexCode)
    {
        if (languages == null)
            languages = new HashMap<>();
        languages.put(hexCode, this);
    }
    
    public static Language map(String hexCode)
    {
        Language lang;
        lang = languages.get(hexCode);
        if (lang != null)
            return lang;
        return Language.LANGUAGE_NEUTRAL;
    }
    
    @Override
    public String toString()
    {
        return this.humanReadableName;
    }
}
