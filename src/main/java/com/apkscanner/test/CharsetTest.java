package com.apkscanner.test;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class CharsetTest {
    @SuppressWarnings("rawtypes")
    public void printCharsetList() {

        Map map = Charset.availableCharsets();
        Set set = map.entrySet();
        Iterator it = set.iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String name = (String) entry.getKey();
            Charset chs = (Charset) entry.getValue();
            System.out.println(name);
            Set aliases = chs.aliases();
            for (Iterator it2 = aliases.iterator(); it2.hasNext();) {
                System.out.println("\t" + it2.next());
            }
        }

        Map availcs = Charset.availableCharsets();
        Set keys = availcs.keySet();
        for (Iterator iter = keys.iterator(); iter.hasNext();) {
            String key = (String) iter.next();
            System.out.println(key);
        }

        FileWriter filewriter;
        try {
            filewriter = new FileWriter("out");
            String encname = filewriter.getEncoding();
            filewriter.close();
            System.out.println("default charset is: " + encname);

            Charset charset1 = Charset.forName("ms949");
            Charset charset2 = Charset.forName("x-windows-949");
            System.out.println("charset1 : " + charset1);
            if (charset1.equals(charset2)) {
                System.out.println("Cp1252/windows-1252 equal");
            } else {
                System.out.println("Cp1252/windows-1252 unequal");
            }
        } catch (IOException e2) {
            e2.printStackTrace();
        }

    }
}
