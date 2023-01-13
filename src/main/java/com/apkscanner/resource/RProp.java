package com.apkscanner.resource;

import java.awt.Font;
import java.beans.PropertyChangeListener;
import java.io.File;

import com.apkspectrum.resource.DefaultResProp;
import com.apkspectrum.resource.ResProp;
import com.apkspectrum.resource._RProp;
import com.apkspectrum.swing.tabbedpaneui.TabbedPaneUIManager;

public enum RProp implements ResProp<Object> {
    LANGUAGE,                   // see _RProp
    USE_EASY_UI,                // see _RProp
    SKIP_STARTUP_EASY_UI_DLG,   // see _RProp
    EDITOR,                     // see _RProp
    ADB_PATH,                   // see _RProp
    ADB_POLICY_SHARED,          // see _RProp
    ADB_DEVICE_MONITORING,      // see _RProp
    TRY_UNLOCK_AF_LAUNCH,       // see _RProp
    LAUNCH_AF_INSTALLED,        // see _RProp
    FRAMEWORK_RES,              // see _RProp
    LAST_FILE_OPEN_PATH,        // see _RProp
    LAST_FILE_SAVE_PATH,        // see _RProp
    CURRENT_THEME,              // see _RProp
    SAVE_WINDOW_SIZE,           // see _RProp
    PREFERRED_LANGUAGE,         // see _RProp
    PRINT_MULTILINE_ATTR,       // see _RProp

    BASE_FONT        (""),      // S
    BASE_FONT_SIZE   (12),      // I
    BASE_FONT_STYLE  (Font.PLAIN),  // I

    USE_UI_BOOSTER,             // B
    ESC_ACTION,                 // I
    RECENT_EDITOR,              // S
    LAUNCH_ACTIVITY_OPTION,     // I
    RECENT_ADB_INFO,            // S
    SOVE_LEAD_TIME,             // S
    TABBED_UI_THEME,            /* see getDefualtValue() */
    PEM_FILE_PATH,              /* see getDefualtValue() */
    PK8_FILE_PATH,              /* see getDefualtValue() */
    AXML_VIEWER_TYPE,           // S
    COMP_FILTER_TYPE,           // S

    EASY_GUI_TOOLBAR,           // S

    EASY_GUI_WINDOW_POSITION_X, // I
    EASY_GUI_WINDOW_POSITION_Y, // I

    DEFAULT_DECORDER,           // S
    DEFAULT_SEARCHER,           // S
    DEFAULT_EXPLORER,           // S
    DEFAULT_LAUNCH_MODE,        // S
    VISIBLE_TO_BASIC,           // B
    ALWAYS_TOOLBAR_EXTENDED,    // B

    PERM_MARK_RUNTIME,          // B
    PERM_MARK_COUNT,            // B
    PERM_TREAT_SIGN_AS_REVOKED, // B
    ; // ENUM END

    public enum B implements ResProp<Boolean> {
        USE_EASY_UI,
        SKIP_STARTUP_EASY_UI_DLG,
        USE_UI_BOOSTER              (false),
        ADB_POLICY_SHARED,
        ADB_DEVICE_MONITORING,
        TRY_UNLOCK_AF_LAUNCH,
        LAUNCH_AF_INSTALLED,
        SAVE_WINDOW_SIZE,
        PRINT_MULTILINE_ATTR,
        VISIBLE_TO_BASIC            (true),
        ALWAYS_TOOLBAR_EXTENDED     (false),
        PERM_MARK_RUNTIME           (true),
        PERM_MARK_COUNT             (true),
        PERM_TREAT_SIGN_AS_REVOKED  (true),
        ; // ENUM END

        private B() {}

        private B(boolean defValue) {
            getProp(name()).setDefaultValue(Boolean.valueOf(defValue));
        }

        @Override
        public Boolean get() {
            return getProp(name()).getBoolean();
        }

        @Override
        public void set(Boolean data) {
            getProp(name()).setData(data);
        }

        @Override
        public String getValue() {
            return getProp(name()).getValue();
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener l) {
            getProp(name()).addPropertyChangeListener(l);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener l) {
            getProp(name()).removePropertyChangeListener(l);
        }
    }

    public enum S implements ResProp<String> {
        LANGUAGE,
        SKIP_STARTUP_EASY_UI_DLG,
        EDITOR,
        RECENT_EDITOR           (""),
        ADB_PATH,
        RECENT_ADB_INFO         (""),
        FRAMEWORK_RES,
        LAST_FILE_OPEN_PATH,
        LAST_FILE_SAVE_PATH,
        SOVE_LEAD_TIME,
        CURRENT_THEME,
        TABBED_UI_THEME         (TabbedPaneUIManager.DEFAULT_TABBED_UI),
        BASE_FONT,
        PREFERRED_LANGUAGE,
        PEM_FILE_PATH,
        PK8_FILE_PATH,
        EASY_GUI_TOOLBAR        ("1,2,3,4,5,7"),
        DEFAULT_DECORDER        (RConst.STR_DECORDER_JADX_GUI),
        DEFAULT_SEARCHER        (RConst.STR_DEFAULT_SEARCHER),
        DEFAULT_EXPLORER        (RConst.STR_EXPLORER_ARCHIVE),
        DEFAULT_LAUNCH_MODE     (RConst.STR_LAUNCH_LAUNCHER),
        AXML_VIEWER_TYPE        (RConst.AXML_VEIWER_TYPE_XML),
        COMP_FILTER_TYPE        (RConst.COMPONENT_FILTER_TYPE_XML),
        ; // ENUM END

        private S() {}

        private S(String defValue) {
            getProp(name()).setDefaultValue(defValue);
        }

        @Override
        public String get() {
            return getProp(name()).getString();
        }

        @Override
        public void set(String data) {
            getProp(name()).setData(data);
        }

        @Override
        public String getValue() {
            return getProp(name()).getValue();
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener l) {
            getProp(name()).addPropertyChangeListener(l);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener l) {
            getProp(name()).removePropertyChangeListener(l);
        }
    }

    public enum I implements ResProp<Integer> {
        ESC_ACTION                (RConst.INT_ESC_ACT_NONE),
        LAUNCH_ACTIVITY_OPTION    (RConst.INT_LAUNCH_LAUNCHER_OR_MAIN_ACTIVITY),
        BASE_FONT_STYLE,
        BASE_FONT_SIZE,
        EASY_GUI_WINDOW_POSITION_X,
        EASY_GUI_WINDOW_POSITION_Y,
        ; // ENUM END

        private I() {}

        private I(int defValue) {
            getProp(name()).setDefaultValue(Integer.valueOf(defValue));
        }

        @Override
        public Integer get() {
            return getProp(name()).getInt();
        }

        @Override
        public void set(Integer data) {
            getProp(name()).setData(data);
        }

        @Override
        public String getValue() {
            return getProp(name()).getValue();
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener l) {
            getProp(name()).addPropertyChangeListener(l);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener l) {
            getProp(name()).removePropertyChangeListener(l);
        }
    }

    private DefaultResProp res;

    private RProp() {
        Object defValue = getDefaultValue();
        res = _RProp.getProp(name());
        if (defValue != null) {
            res.setDefaultValue(defValue);
        }
    }

    private RProp(Object defValue) {
        res = _RProp.getProp(name());
        res.setDefaultValue(defValue);
    }

    public Object getDefaultValue() {
        Object obj = res != null ? res.getDefaultValue() : null;
        if (obj != null) return obj;

        switch (name()) {
            case "PEM_FILE_PATH":
                String pem = RFile.DATA_CERT_PEM_FILE.getPath();
                if (new File(pem).isFile()) {
                    obj = pem;
                }
                break;
            case "PK8_FILE_PATH":
                String pk8 = RFile.DATA_CERT_PK8_FILE.getPath();
                if (new File(pk8).isFile()) {
                    obj = pk8;
                }
                break;
            default:
                break;
        };

        if (res != null && obj != null) res.setDefaultValue(obj);
        return obj;
    }

    @Override
    public String getValue() {
        return res.getValue();
    }

    @Override
    public String toString() {
        return res.toString();
    }

    @Override
    public Object get() {
        return res.get();
    }

    @Override
    public void set(Object data) {
        res.set(data);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        res.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        res.removePropertyChangeListener(l);
    }

    public Object getData() {
        return res.getData();
    }

    public Object getData(Object ref) {
        return res.getData(ref);
    }

    public String getString() {
        return res.getString();
    }

    public int getInt() {
        return res.getInt();
    }

    public int getInt(int ref) {
        return res.getInt(ref);
    }

    public boolean getBoolean() {
        return res.getBoolean();
    }

    public boolean getBoolean(boolean ref) {
        return res.getBoolean();
    }

    public void setData(Object data) {
        res.setData(data);
    }

    public DefaultResProp getProp() {
        return res;
    }

    public static DefaultResProp getProp(String name) {
        RProp prop = null;
        try {
            prop = valueOf(name);
        } catch (Exception e) {
        }
        return prop != null ? prop.res : _RProp.getProp(name);
    }

    public static Object getPropData(String key) {
        return DefaultResProp.getPropData(key);
    }

    public static Object getPropData(String key, Object ref) {
        return DefaultResProp.getPropData(key, ref);
    }

    public static void setPropData(String key, Object data) {
        DefaultResProp.setPropData(key, data);
    }

    public static void addPropertyChangeListener(ResProp<?> prop, PropertyChangeListener l) {
        DefaultResProp.addPropertyChangeListener(prop, l);
    }

    public static void removePropertyChangeListener(ResProp<?> prop, PropertyChangeListener l) {
        DefaultResProp.removePropertyChangeListener(prop, l);
    }
}
