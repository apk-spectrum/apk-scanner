package com.apkspectrum.jna;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.ArrayList;
import java.util.List;

public final class LangAndCodePage extends Structure
{
    /** The language contained in the translation table **/
    public short wLanguage;
    /** The code page contained in the translation table **/
    public short wCodePage;
    
    public LangAndCodePage(Pointer p)
    {
        super(p);
    }
    
    public LangAndCodePage(Pointer p, int offset)
    {
        // super(p, offset);
        this.useMemory(p, offset);
    }
    
    /** The sizeof equivalent in Java **/
    public static int sizeOf()
    {
        return 4;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
    protected List getFieldOrder()
    {
        List fieldOrder = new ArrayList();
        fieldOrder.add("wLanguage");
        fieldOrder.add("wCodePage");
        return fieldOrder;
    }
}