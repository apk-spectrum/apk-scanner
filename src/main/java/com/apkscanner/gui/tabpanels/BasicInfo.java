package com.apkscanner.gui.tabpanels;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton.ToggleButtonModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.text.Element;
import javax.swing.text.html.Option;

import com.apkscanner.gui.dialog.PermissionHistoryPanel;
import com.apkscanner.gui.dialog.SdkVersionInfoDlg;
import com.apkscanner.resource.RComp;
import com.apkscanner.resource.RFile;
import com.apkscanner.resource.RImg;
import com.apkscanner.resource.RProp;
import com.apkscanner.resource.RStr;
import com.apkspectrum.core.permissionmanager.PermissionGroupInfoExt;
import com.apkspectrum.core.permissionmanager.PermissionManager;
import com.apkspectrum.core.permissionmanager.PermissionRepository.SourceCommit;
import com.apkspectrum.core.scanner.ApkScanner;
import com.apkspectrum.data.apkinfo.ApkInfo;
import com.apkspectrum.data.apkinfo.ApkInfoHelper;
import com.apkspectrum.data.apkinfo.CompatibleScreensInfo;
import com.apkspectrum.data.apkinfo.ResourceInfo;
import com.apkspectrum.data.apkinfo.SupportsGlTextureInfo;
import com.apkspectrum.data.apkinfo.SupportsScreensInfo;
import com.apkspectrum.data.apkinfo.UsesConfigurationInfo;
import com.apkspectrum.data.apkinfo.UsesFeatureInfo;
import com.apkspectrum.data.apkinfo.UsesLibraryInfo;
import com.apkspectrum.data.apkinfo.UsesSdkInfo;
import com.apkspectrum.logback.Log;
import com.apkspectrum.plugin.PackageSearcher;
import com.apkspectrum.plugin.PlugIn;
import com.apkspectrum.plugin.PlugInEventAdapter;
import com.apkspectrum.plugin.PlugInManager;
import com.apkspectrum.resource._RFile;
import com.apkspectrum.swing.HtmlEditorPane;
import com.apkspectrum.swing.HtmlEditorPane.HyperlinkClickEvent;
import com.apkspectrum.swing.HtmlEditorPane.HyperlinkClickListener;
import com.apkspectrum.swing.MessageBoxPane;
import com.apkspectrum.util.Base64;
import com.apkspectrum.util.FileUtil;
import com.apkspectrum.util.FileUtil.FSStyle;
import com.apkspectrum.util.SystemUtil;
import com.apkspectrum.util.XmlPath;

public class BasicInfo extends AbstractTabbedPanel
        implements HyperlinkClickListener, IProgressListener, ListDataListener, ItemListener {
    private static final long serialVersionUID = 6431995641984509482L;

    private static final String CARD_LODING_PAGE = "CARD_LODING_PROCESS";
    private static final String CARD_APK_INFORMATION = "CARD_APK_INFORMATION";

    private HtmlEditorPane apkInfoPanel;
    private JPanel lodingPanel;
    private JLabel messageLabel;
    private CardLayout cardLayout;

    private PermissionManager permissionManager = new PermissionManager();

    public BasicInfo() {
        setTitle(RComp.TABBED_BASIC_INFO);
        setTabbedEnabled(true);

        initialize();
        showAbout();

        RProp.VISIBLE_TO_BASIC.addPropertyChangeListener(this);

        PlugInManager.addPlugInEventListener(new PlugInEventAdapter() {
            @Override
            public void onPluginLoaded() {
                setPluginSearcher();
            }
        });
    }

    @Override
    public void initialize() {
        if (apkInfoPanel != null) return;

        apkInfoPanel = new HtmlEditorPane();
        apkInfoPanel.setEditable(false);
        apkInfoPanel.setOpaque(true);
        apkInfoPanel.setBackground(Color.white);
        apkInfoPanel.addHyperlinkClickListener(this);

        JLabel logo = new JLabel(RImg.APK_LOGO.getImageIcon(400, 250));
        logo.setOpaque(true);
        logo.setBackground(Color.white);

        JLabel gif = new JLabel(RImg.WAIT_BAR.getImageIcon());
        gif.setOpaque(true);
        gif.setBackground(Color.white);
        gif.setPreferredSize(new Dimension(RImg.WAIT_BAR.getImageIcon().getIconWidth(),
                RImg.WAIT_BAR.getImageIcon().getIconHeight()));

        messageLabel = new JLabel();
        messageLabel.setOpaque(true);
        messageLabel.setBackground(Color.WHITE);
        messageLabel.setBorder(new EmptyBorder(0, 0, 50, 0));
        messageLabel.setHorizontalAlignment(JLabel.CENTER);

        lodingPanel = new JPanel();
        lodingPanel.setLayout(new BorderLayout());
        lodingPanel.setOpaque(false);
        lodingPanel.setBackground(Color.white);
        lodingPanel.add(logo, BorderLayout.NORTH);
        lodingPanel.add(gif, BorderLayout.CENTER);
        lodingPanel.add(messageLabel, BorderLayout.SOUTH);

        setLayout(cardLayout = new CardLayout());
        add(CARD_LODING_PAGE, lodingPanel);
        add(CARD_APK_INFORMATION, apkInfoPanel);
        cardLayout.show(this, CARD_APK_INFORMATION);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Log.v("Change property : " + evt);
        super.propertyChange(evt);
        setPluginSearcher();
    }

    private void showAbout() {
        String exePath = RFile.ETC_APKSCANNER_EXE.getPath();
        apkInfoPanel.setText(RFile.RAW_ABUOT_HTML.getString());
        apkInfoPanel.insertElementFirst("apkscanner-icon-td", String
                .format("<img src=\"%s\" width=\"150\" height=\"150\">", RImg.APP_ICON.getPath()));
        apkInfoPanel.setInnerHTMLById("apkscanner-title",
                RStr.APP_NAME.get() + " " + RStr.APP_VERSION.get());
        apkInfoPanel.setOuterHTMLById("programmer-email",
                String.format("<a href=\"mailto:%s\" title=\"%s\">%s</a>",
                        RStr.APP_MAKER_EMAIL.get(), RStr.APP_MAKER_EMAIL.get(),
                        RStr.APP_MAKER.get()));
        if (!SystemUtil.hasShortCut(exePath, RStr.APP_NAME.get())) {
            apkInfoPanel.insertElementLast("apkscanner-icon-td",
                    String.format("<div id=\"create-shortcut\" class=\"div-button\">%s</div>",
                            makeHyperEvent("function-create-shortcut",
                                    RStr.BTN_CREATE_SHORTCUT.get(), null)));
        }
        if (!SystemUtil.isAssociatedWithFileType(".apk", exePath)) {
            apkInfoPanel.insertElementLast("apkscanner-icon-td", String.format(
                    "<div id=\"associate-file\" class=\"div-button\">%s</div>",
                    makeHyperEvent("function-assoc-apk", RStr.BTN_ASSOC_FTYPE.get(), null)));
        }
        apkInfoPanel.insertElementLast("apkscanner-icon-td", String.format(
                "<div class=\"div-button\">%s</div>",
                makeHyperEvent("function-show-permission", RStr.BTN_SHOW_PERMISSIONS.get(), null)));
        apkInfoPanel.insertElementLast("apkscanner-icon-td",
                String.format("<div class=\"div-button\">%s</div>",
                        makeHyperEvent("function-show-sdk-info", RStr.BTN_SHOW_SDK_INFO.get(), null,
                                Integer.valueOf(-1))));
    }

    public void onProgress(String message) {
        if (lodingPanel == null) initialize();
        if (!lodingPanel.isVisible()) {
            apkInfoPanel.setText("");
            cardLayout.show(this, CARD_LODING_PAGE);
        }
        if (message == null || message.isEmpty()) message = "Standby for extracting.";
        messageLabel.setText(message);
    }

    @Override
    public void setData(ApkInfo apkInfo, int status) {
        if (apkInfoPanel == null) initialize();

        if (apkInfo == null) {
            showAbout();
            setSeletected();
            return;
        }

        switch (status) {
            case ApkScanner.STATUS_BASIC_INFO_COMPLETED:
                RComp res = apkInfo.type != ApkInfo.PACKAGE_TYPE_APEX ? RComp.TABBED_BASIC_INFO
                        : RComp.TABBED_APEX_INFO;
                res.set(this);
                setBasicInfo(apkInfo);
                cardLayout.show(this, CARD_APK_INFORMATION);
                setSeletected();
                break;
            case ApkScanner.STATUS_CERT_COMPLETED:
                if (apkInfoPanel.getElementById("basic-info") != null) {
                    if (ApkInfoHelper.isTestPlatformSign(apkInfo)
                            || ApkInfoHelper.isSamsungSign(apkInfo)) {
                        permissionManager.setPlatformSigned(true);
                        updateFeatureRelateSign(apkInfo);
                        setPermissionList(null);
                    }
                    if (apkInfo.signatureScheme != null) {
                        apkInfoPanel.setOuterHTMLById("signature-scheme",
                                ", " + makeHyperEvent("signature-scheme",
                                        "Scheme " + apkInfo.signatureScheme,
                                        "APK Signature Scheme " + apkInfo.signatureScheme,
                                        apkInfo.signatureScheme));
                    }
                }
            default:
        }
    }

    private void setBasicInfo(ApkInfo apkInfo) {
        if (apkInfo.type != ApkInfo.PACKAGE_TYPE_APEX) {
            apkInfoPanel.setText(RFile.RAW_BASIC_INFO_LAYOUT_HTML.getString());
            setAppIcon(apkInfo.manifest.application.icons);
            setAppLabel(apkInfo.manifest.application.labels, apkInfo.manifest.packageName);
            setPackageName(apkInfo.manifest.packageName);
            setVersion(apkInfo.manifest.versionName, apkInfo.manifest.versionCode);
            setSdkVersion(apkInfo.manifest.usesSdk);
            setFileSize(apkInfo.filePath);
            setFeatures(apkInfo);
            setPermissionList(apkInfo);
            setPluginSearcher();
        } else {
            apkInfoPanel.setText(RFile.RAW_APEX_INFO_LAYOUT_HTML.getString());
            setPlatformIcon(apkInfo.manifest.usesSdk.targetSdkVersion);
            setAppLabel(apkInfo.manifest.application.labels, apkInfo.manifest.packageName);
            setPackageName(apkInfo.manifest.packageName);
            setApexVersion(apkInfo.manifest.versionName, apkInfo.manifest.versionCode);
            setSdkVersion(apkInfo.manifest.usesSdk);
            setFileSize(apkInfo.filePath);
        }
    }

    private void setAppIcon(ResourceInfo[] icons) {
        String iconPath = null;
        if (icons != null && icons.length > 0) {
            ResourceInfo[] iconList = icons;
            for (int i = iconList.length - 1; i >= 0; i--) {
                if (iconList[i] == null || iconList[i].name == null
                        || iconList[i].name.endsWith(".xml"))
                    continue;
                iconPath = iconList[i].name;
                if (iconPath != null) break;
            }
        }
        if (iconPath == null) {
            iconPath = RImg.DEF_APP_ICON.getPath();
        } else if (iconPath.toLowerCase().endsWith(".qmg")) {
            iconPath = RImg.QMG_IMAGE_ICON.getPath();
        }
        apkInfoPanel.setOuterHTMLById("icon",
                String.format("<img src=\"%s\" width=\"150\" height=\"150\">", iconPath));
    }

    private void setPlatformIcon(int apiLevel) {
        String iconPath = null;
        try (InputStream xml = _RFile.RAW_SDK_INFO_FILE.getResourceAsStream()) {
            XmlPath sdkXmlPath = new XmlPath(xml);
            XmlPath sdkInfo =
                    sdkXmlPath.getNode("/resources/sdk-info[@apiLevel='" + apiLevel + "']");
            if (sdkInfo != null) {
                iconPath = getClass().getResource(sdkInfo.getAttribute("icon")).toExternalForm();
            }
        } catch (IOException e) {
        }
        if (iconPath == null) {
            iconPath = getClass().getResource("/icons/logo/base.png").toExternalForm();
        }
        apkInfoPanel.setOuterHTMLById("icon",
                String.format("<img src=\"%s\" width=\"150\" height=\"150\">", iconPath));
    }

    private void setAppLabel(ResourceInfo[] labels, String packageName) {
        String appName = null;
        StringBuilder labelBuilder = new StringBuilder();
        if (labels != null && labels.length > 0) {
            appName = ApkInfoHelper.getResourceValue(labels, RProp.S.PREFERRED_LANGUAGE.get());
            if (appName != null && appName.isEmpty()) appName = null;

            for (ResourceInfo r : labels) {
                if (r.configuration == null || r.configuration.isEmpty()
                        || "default".equals(r.configuration)) {
                    labelBuilder.append(r.name != null ? r.name : packageName);
                    if (appName == null && r.name != null) appName = r.name;
                } else {
                    labelBuilder.append("[").append(r.configuration).append("] ").append(r.name);
                }
                labelBuilder.append("\n");
            }
        }
        String mutiLabels = labelBuilder.toString();
        if (appName == null) appName = packageName;

        if (labels != null && labels.length > 1) {
            apkInfoPanel.setInnerHTMLById("label",
                    makeHyperEvent("other-lang", appName, mutiLabels));
            apkInfoPanel.insertElementLast("label", "<font>" + makeHyperEvent("other-lang",
                    "&nbsp;[" + labels.length + "]", mutiLabels, mutiLabels) + "</font>");
        } else {
            apkInfoPanel.setInnerHTMLById("label", appName);
        }
    }

    private void setPackageName(String packageName) {
        apkInfoPanel.setOuterHTMLById("package", packageName);
    }

    private void setVersion(String versionName, Integer versionCode) {
        if (versionName == null) versionName = "";
        StringBuilder text = new StringBuilder("Ver. ").append(versionName).append(" / ")
                .append((versionCode != null ? versionCode : "0"));
        StringBuilder descripton = new StringBuilder("VersionName : ").append(versionName)
                .append("\n").append("VersionCode : ")
                .append((versionCode != null ? versionCode : "Unspecified"));
        String versionDesc = descripton.toString();

        apkInfoPanel.setInnerHTMLById("version",
                makeHyperEvent("app-version", text.toString(), versionDesc, versionDesc));
    }

    private void setApexVersion(String versionName, Integer versionCode) {
        if (versionName == null) versionName = "";
        StringBuilder text = new StringBuilder("Ver. ")
                .append((versionCode != null ? versionCode : versionName));
        StringBuilder descripton = new StringBuilder("AndroidManifest:versionCode : ")
                .append(versionName).append("\n").append("apex_manifest:version : ")
                .append((versionCode != null ? versionCode : ""));
        String versionDesc = descripton.toString();

        apkInfoPanel.setInnerHTMLById("version",
                makeHyperEvent("app-version", text.toString(), versionDesc, versionDesc));
    }

    private void setSdkVersion(UsesSdkInfo usesSdk) {
        StringBuilder sdkVersion = new StringBuilder();
        if (usesSdk.minSdkVersion != null) {
            sdkVersion.append(", ").append(makeHyperEvent("min-sdk-info",
                    usesSdk.minSdkVersion + " (Min)", "Min SDK version", usesSdk.minSdkVersion));
        }
        if (usesSdk.targetSdkVersion != null) {
            sdkVersion.append(", ")
                    .append(makeHyperEvent("target-sdk-info",
                            usesSdk.targetSdkVersion + " (Target)", "Targer SDK version",
                            usesSdk.targetSdkVersion));
        }
        if (usesSdk.maxSdkVersion != null) {
            sdkVersion.append(", ").append(makeHyperEvent("max-sdk-info",
                    usesSdk.maxSdkVersion + " (Max)", "Max SDK version", usesSdk.maxSdkVersion));
        }
        if (sdkVersion.length() == 0) {
            sdkVersion.append(", Unspecified");
        }
        apkInfoPanel.setOuterHTMLById("sdk-version", sdkVersion.substring(2));
    }

    private void setFileSize(String filePath) {
        File apkFile = new File(filePath);
        String text = FileUtil.getFileSize(apkFile.length(), FSStyle.FULL);
        String description = "MD5: " + FileUtil.getMessageDigest(apkFile, "MD5");
        apkInfoPanel.setInnerHTMLById("file-size",
                makeHyperEvent("file-checksum", text, description, filePath));
    }

    private void setFeatures(ApkInfo apkInfo) {
        StringBuilder feature = new StringBuilder("[" + RStr.FEATURE_LAB.get() + "] ");
        if ("internalOnly".equals(apkInfo.manifest.installLocation)) {
            feature.append(makeHyperEvent("feature-install-location-internal",
                    RStr.FEATURE_ILOCATION_INTERNAL_LAB.get(),
                    RStr.FEATURE_ILOCATION_INTERNAL_DESC.get()));
        } else if ("auto".equals(apkInfo.manifest.installLocation)) {
            feature.append(makeHyperEvent("feature-install-location-auto",
                    RStr.FEATURE_ILOCATION_AUTO_LAB.get(), RStr.FEATURE_ILOCATION_AUTO_DESC.get()));
        } else if ("preferExternal".equals(apkInfo.manifest.installLocation)) {
            feature.append(makeHyperEvent("feature-install-location-external",
                    RStr.FEATURE_ILOCATION_EXTERNAL_LAB.get(),
                    RStr.FEATURE_ILOCATION_EXTERNAL_DESC.get()));
        }
        feature.append("<span id=\"signature-scheme\">&nbsp;</span>");
        feature.append("<br/>");

        if (ApkInfoHelper.isHidden(apkInfo)) {
            feature.append(makeHyperEvent("feature-hidden", RStr.FEATURE_HIDDEN_LAB.get(),
                    RStr.FEATURE_HIDDEN_DESC.get()));
        } else {
            feature.append(makeHyperEvent("feature-launcher", RStr.FEATURE_LAUNCHER_LAB.get(),
                    RStr.FEATURE_LAUNCHER_DESC.get()));
        }
        if (ApkInfoHelper.isStartup(apkInfo)) {
            feature.append(", ");
            feature.append(makeHyperEvent("feature-startup", RStr.FEATURE_STARTUP_LAB.get(),
                    RStr.FEATURE_STARTUP_DESC.get()));
        }
        String sharedUserId = apkInfo.manifest.sharedUserId;
        if (sharedUserId != null && !sharedUserId.startsWith("android.uid.system")) {
            feature.append(", ");
            feature.append(
                    makeHyperEvent("feature-shared-user-id", RStr.FEATURE_SHAREDUSERID_LAB.get(),
                            RStr.FEATURE_SHAREDUSERID_DESC.get(), sharedUserId));
        }
        String deviceRequirements = makeDeviceRequirements(apkInfo);
        if (deviceRequirements != null && !deviceRequirements.isEmpty()) {
            feature.append(", ");
            feature.append(
                    makeHyperEvent("feature-device-requirements", RStr.FEATURE_DEVICE_REQ_LAB.get(),
                            RStr.FEATURE_DEVICE_REQ_DESC.get(), deviceRequirements));
        }

        boolean isSamsungSign = ApkInfoHelper.isSamsungSign(apkInfo);
        boolean isPlatformSign = ApkInfoHelper.isTestPlatformSign(apkInfo);
        String certSummary = makeCertSummary(apkInfo.certificates);

        StringBuilder particularFeatures = new StringBuilder();
        if (sharedUserId != null && sharedUserId.startsWith("android.uid.system")) {
            particularFeatures.append(", ");
            if (!isSamsungSign && !isPlatformSign)
                particularFeatures.append("<span id=\"system-uid\">");
            particularFeatures.append(makeHyperEvent("feature-system-user-id",
                    RStr.FEATURE_SYSTEM_UID_LAB.get(), RStr.FEATURE_SYSTEM_UID_DESC.get()));
            if (!isSamsungSign && !isPlatformSign) particularFeatures.append("</span>");
        }
        if (isPlatformSign) {
            particularFeatures.append(", ");
            particularFeatures.append(
                    makeHyperEvent("feature-platform-sign", RStr.FEATURE_PLATFORM_SIGN_LAB.get(),
                            RStr.FEATURE_PLATFORM_SIGN_DESC.get(), certSummary));
        }
        if (isSamsungSign) {
            particularFeatures.append(", ");
            particularFeatures.append(
                    makeHyperEvent("feature-samsung-sign", RStr.FEATURE_SAMSUNG_SIGN_LAB.get(),
                            RStr.FEATURE_SAMSUNG_SIGN_DESC.get(), certSummary));
        }
        if (ApkInfoHelper.isDebuggable(apkInfo)) {
            particularFeatures.append(", ");
            particularFeatures.append(makeHyperEvent("feature-debuggable",
                    RStr.FEATURE_DEBUGGABLE_LAB.get(), RStr.FEATURE_DEBUGGABLE_DESC.get()));
        }
        if (ApkInfoHelper.isInstrumentation(apkInfo)) {
            particularFeatures.append(", ");
            particularFeatures.append(makeHyperEvent("feature-instrumentation",
                    RStr.FEATURE_INSTRUMENTATION_LAB.get(),
                    RStr.FEATURE_INSTRUMENTATION_DESC.get()));
        }

        apkInfoPanel.setInnerHTMLById("features", feature.toString());
        if (particularFeatures.length() > 0) {
            apkInfoPanel.insertElementAfter("features", "<h4 id=\"particular-features\"></h4>");
            apkInfoPanel.setInnerHTMLById("particular-features", particularFeatures.substring(2));
        }
    }

    private void updateFeatureRelateSign(ApkInfo apkInfo) {
        if (apkInfo.certificates == null) {
            Log.v("certificates is empty");
            return;
        }

        boolean isSamsungSign = ApkInfoHelper.isSamsungSign(apkInfo);
        boolean isPlatformSign = ApkInfoHelper.isTestPlatformSign(apkInfo);
        String certSummary = makeCertSummary(apkInfo.certificates);

        boolean isOtherEmpty = false;
        Element elem = apkInfoPanel.getElementById("particular-features");
        if (elem == null) {
            apkInfoPanel.insertElementAfter("features", "<h4 id=\"particular-features\"></h4>");
            isOtherEmpty = true;
        }
        StringBuilder particularFeatures = new StringBuilder();
        elem = apkInfoPanel.getElementById("system-uid");
        if (elem != null) {
            particularFeatures.append(", ");
            particularFeatures.append(makeHyperEvent("feature-system-user-id",
                    RStr.FEATURE_SYSTEM_UID_LAB.get(), RStr.FEATURE_SYSTEM_UID_DESC.get()));
        }
        if (isPlatformSign && apkInfoPanel.getElementById("feature-platform-sign") == null) {
            particularFeatures.append(", ");
            particularFeatures.append(
                    makeHyperEvent("feature-platform-sign", RStr.FEATURE_PLATFORM_SIGN_LAB.get(),
                            RStr.FEATURE_PLATFORM_SIGN_DESC.get(), certSummary));
        }
        if (isSamsungSign && apkInfoPanel.getElementById("feature-samsung-sign") == null) {
            particularFeatures.append(", ");
            particularFeatures.append(
                    makeHyperEvent("feature-samsung-sign", RStr.FEATURE_SAMSUNG_SIGN_LAB.get(),
                            RStr.FEATURE_SAMSUNG_SIGN_DESC.get(), certSummary));
        }
        if (particularFeatures.length() > 0) {
            if (elem != null) {
                apkInfoPanel.setOuterHTML(elem, particularFeatures.substring(2));
            } else {
                if (!isOtherEmpty) particularFeatures.append(",&nbsp;");
                apkInfoPanel.insertElementFirst("particular-features",
                        particularFeatures.substring(2));
            }
        }
    }

    private void setPermissionList(final ApkInfo apkInfo) {
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                if (apkInfo != null) {
                    boolean isPlatformSign = ApkInfoHelper.isTestPlatformSign(apkInfo)
                            || ApkInfoHelper.isSamsungSign(apkInfo);
                    permissionManager.clearPermissions();
                    permissionManager.setPlatformSigned(isPlatformSign);
                    permissionManager
                            .setTreatSignAsRevoked(RProp.B.PERM_TREAT_SIGN_AS_REVOKED.get());
                    permissionManager.addUsesPermission(apkInfo.manifest.usesPermission);
                    permissionManager.addUsesPermission(apkInfo.manifest.usesPermissionSdk23);
                    permissionManager.addDeclarePemission(apkInfo.manifest.permission);
                    if (!permissionManager.isEmpty()) {
                        publish((Void) null);
                        synchronized (this) {
                            wait();
                        }
                    }
                }
                return makePermGroup();
            }

            @Override
            protected void process(List<Void> chunks) {
                int selectSdkVer = makeSdkOptions(apkInfo.manifest.usesSdk.targetSdkVersion);
                permissionManager.setSdkVersion(selectSdkVer);
                synchronized (this) {
                    notify();
                }
            }

            @Override
            protected void done() {
                String permGroupHtml = null;
                try {
                    permGroupHtml = get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
                int groupCount = 0;
                if (permGroupHtml == null || permissionManager.isEmpty()) {
                    apkInfoPanel.setInnerHTMLById("perm-group-title",
                            RStr.LABEL_NO_PERMISSION.get());
                } else {
                    StringBuilder titlebar = new StringBuilder();
                    titlebar.append("[").append(RStr.BASIC_PERMISSIONS.get()).append("] - ");
                    titlebar.append(makeHyperEvent("display-list",
                            String.format("<u>%s</u>", RStr.BASIC_PERMLAB_DISPLAY.get()),
                            RStr.BASIC_PERMDESC_DISPLAY.get()));
                    if (apkInfoPanel.getElementById("perm-settings") == null) {
                        titlebar.append(makeHyperEvent("show-perm-setting",
                                String.format("<img src=\"%s\" width=\"16\" height=\"16\">",
                                        RImg.PERM_MARKER_SETTING.getPath()),
                                null));
                    } else {
                        titlebar.append(makeHyperEvent("close-perm-setting",
                                String.format("<img src=\"%s\" width=\"16\" height=\"16\">",
                                        RImg.PERM_MARKER_CLOSE.getPath()),
                                null));
                    }
                    apkInfoPanel.removeElementById("show-perm-setting");
                    apkInfoPanel.removeElementById("close-perm-setting");
                    apkInfoPanel.setInnerHTMLById("perm-group-title", titlebar.toString());
                    apkInfoPanel.setOuterHTMLById("perm-groups",
                            String.format("<div id=\"perm-groups\">%s</div>", permGroupHtml));
                    groupCount = permissionManager.getPermissionGroups().length;
                }
                setInfoAreaHeight(groupCount);
            };
        }.execute();
        setInfoAreaHeight(1);
    }

    private void setInfoAreaHeight(int groupCount) {
        int infoHeight = groupCount > 15 ? 220 : (groupCount > 0 ? 260 : 280);
        if (apkInfoPanel.getElementById("perm-settings") != null) infoHeight -= 20;
        apkInfoPanel.setOuterHTMLById("basic-info-height-td",
                String.format("<td id=\"basic-info-height-td\" height=\"%d\"></td>", infoHeight));
    }

    private void setPluginSearcher() {
        if (apkInfoPanel == null) return;
        String packageSearchers = "";
        String appLabelSearchers = "";
        String hashSearchers = "";
        if (RProp.B.VISIBLE_TO_BASIC.get()) {
            PackageSearcher[] searchers = PlugInManager.getPackageSearchers();
            String defaultSearchIcon = RImg.TOOLBAR_SEARCH.getPath();
            for (PackageSearcher searcher : searchers) {
                searcher.addPropertyChangeListener(this);
                if (!searcher.isVisibleToBasic()) continue;
                URL icon = searcher.getIconURL();
                String iconPath = icon != null ? icon.toString() : defaultSearchIcon;
                String tag = makeHyperEvent("PLUGIN:" + searcher.hashCode(),
                        String.format("<img src=\"%s\" width=\"16\" height=\"16\">", iconPath),
                        null, searcher.getActionCommand());
                switch (searcher.getSupportType()) {
                    case PackageSearcher.SEARCHER_TYPE_PACKAGE_NAME:
                        packageSearchers += tag;
                        break;
                    case PackageSearcher.SEARCHER_TYPE_APP_NAME:
                        appLabelSearchers += tag;
                        break;
                    case PackageSearcher.SEARCHER_TYPE_PACKAGE_HASH:
                        hashSearchers += tag;
                        break;
                };
            }
        }
        apkInfoPanel.removeElementById("name-searcher");
        if (!appLabelSearchers.isEmpty()) {
            apkInfoPanel.insertElementLast("label",
                    String.format("<span id=\"name-searcher\">%s</span>", appLabelSearchers));
        }
        if (!packageSearchers.isEmpty()) {
            apkInfoPanel.setOuterHTMLById("package-searcher",
                    String.format("<span id=\"package-searcher\">%s</span>", packageSearchers));
        } else {
            apkInfoPanel.setOuterHTMLById("package-searcher",
                    "<span id=\"package-searcher\">&nbsp;</span>");
        }
        if (!hashSearchers.isEmpty()) {
            apkInfoPanel.insertElementLast("file-size",
                    String.format("<span id=\"hash-searcher\">%s</span>", hashSearchers));
        }
    }

    private String makeCertSummary(String[] certificates) {
        if (certificates == null || certificates.length == 0) return null;
        StringBuilder summary = new StringBuilder();
        for (String sign : certificates) {
            String[] line = sign.split("\n");
            if (line.length >= 3) {
                summary.append(line[0]).append("\n").append(line[1]).append("\n").append(line[2])
                        .append("\n\n");
            } else {
                summary.append("error\n");
            }
        }
        return summary.toString();
    }

    private String makeDeviceRequirements(ApkInfo apkInfo) {
        StringBuilder deviceReqData = new StringBuilder();
        if (apkInfo.manifest.compatibleScreens != null) {
            for (CompatibleScreensInfo info : apkInfo.manifest.compatibleScreens) {
                deviceReqData.append(info.getReport());
            }
            deviceReqData.append("\n");
        }
        if (apkInfo.manifest.supportsScreens != null) {
            for (SupportsScreensInfo info : apkInfo.manifest.supportsScreens) {
                deviceReqData.append(info.getReport());
            }
            deviceReqData.append("\n");
        }
        if (apkInfo.manifest.usesFeature != null) {
            for (UsesFeatureInfo info : apkInfo.manifest.usesFeature) {
                deviceReqData.append(info.getReport());
            }
            deviceReqData.append("\n");
        }
        if (apkInfo.manifest.usesConfiguration != null) {
            for (UsesConfigurationInfo info : apkInfo.manifest.usesConfiguration) {
                deviceReqData.append(info.getReport());
            }
            deviceReqData.append("\n");
        }
        if (apkInfo.manifest.usesLibrary != null) {
            deviceReqData.append("uses library :\n");
            for (UsesLibraryInfo info : apkInfo.manifest.usesLibrary) {
                deviceReqData.append(info.getReport());
            }
            deviceReqData.append("\n");
        }
        if (apkInfo.manifest.supportsGlTexture != null) {
            for (SupportsGlTextureInfo info : apkInfo.manifest.supportsGlTexture) {
                deviceReqData.append(info.getReport());
            }
            deviceReqData.append("\n");
        }
        return deviceReqData.toString();
    }

    private String makePermGroup() {
        StringBuilder permGroup = new StringBuilder("");
        int cnt = 0;
        int sdk = permissionManager.getSdkVersion();
        boolean isDanger;
        for (PermissionGroupInfoExt g : permissionManager.getPermissionGroups()) {
            isDanger = sdk >= 23 && g.hasDangerous();
            if (cnt % 15 != 0) permGroup.append("&nbsp;");
            permGroup.append(makeHyperEvent("PermGroup:" + g.hashCode(),
                    makeGroupImage(g.getIconPath(), g.permissions.size(), isDanger), g.getSummary(),
                    g.name));
            if (++cnt % 15 == 0) permGroup.append("<font style=\"font-size:1px\"><br><br></font>");
        }
        return permGroup.toString();
    }

    private String makeGroupImage(String src, int count, boolean isDanger) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(new URL(src));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (image == null) {
            return String.format("<img src=\"%s\" width=\"36\" height=\"36\">", src);
        }

        Map<?, ?> desktopHints =
                (Map<?, ?>) Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints");
        Graphics2D g2 = image.createGraphics();
        if (desktopHints != null) {
            g2.setRenderingHints(desktopHints);
        } else {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        }
        Font font = UIManager.getFont("Label.font");
        if (font != null) g2.setFont(font.deriveFont(10f));

        if (isDanger && RProp.B.PERM_MARK_RUNTIME.get()) {
            g2.setColor(Color.WHITE);
            if (isDanger) g2.fillOval(0, 0, 12, 12);
            g2.setColor(Color.RED);
            g2.drawString("R", 3, 10);
        }

        if (RProp.B.PERM_MARK_COUNT.get()) {
            g2.setColor(Color.WHITE);
            g2.fillOval(24, 24, 12, 12);
            g2.setColor(Color.BLACK);
            if (count < 10) {
                g2.drawString(Integer.toString(count), 28, 34);
            } else {
                if (font != null) g2.setFont(font.deriveFont(9.2f));
                g2.drawString(Integer.toString(count), 26, 34);
            }
        }

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", os);
            String base64Image = Base64.getEncoder().encodeToString(os.toByteArray());
            if (base64Image != null) {
                src = "data:image/png;base64, " + base64Image;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return String.format("<img src=\"%s\" width=\"36\" height=\"36\">", src);
    }

    private int makeSdkOptions(Integer targetSdkVersion) {
        int tarSdk = targetSdkVersion != null ? targetSdkVersion : -1;
        int permSdk = -1;
        boolean matchedSdk = false;
        StringBuilder sdkOptions = new StringBuilder("<select id=\"perm-group-sdks\">");
        for (SourceCommit sdk : PermissionManager.getPermissionRepository().sources) {
            if (sdk.getCommitId() != null) {
                permSdk = sdk.getSdkVersion();
                if (!matchedSdk) matchedSdk = permSdk == tarSdk;
                sdkOptions.append(String.format("<option value=%d%s>API Level %d</option>", permSdk,
                        permSdk == tarSdk ? " selected" : "", permSdk));
            }
        }
        sdkOptions.append("</select>");
        apkInfoPanel.setOuterHTMLById("perm-group-sdks", sdkOptions.toString());

        if (targetSdkVersion != null) permSdk = tarSdk;

        Object object = apkInfoPanel.getElementModelById("perm-group-sdks");
        if (object instanceof DefaultComboBoxModel) {
            DefaultComboBoxModel<?> model = (DefaultComboBoxModel<?>) object;
            if (!matchedSdk) {
                if (targetSdkVersion == null) {
                    Option option = (Option) model.getElementAt(model.getSize() - 1);
                    model.setSelectedItem(option);
                    permSdk = Integer.parseInt(option.getValue());
                } else {
                    model.setSelectedItem("API Level " + permSdk);
                }
            }
            model.removeListDataListener(this);
            model.addListDataListener(this);
        }

        return permSdk;
    }

    @Override
    public void contentsChanged(ListDataEvent evt) {
        DefaultComboBoxModel<?> model = (DefaultComboBoxModel<?>) evt.getSource();
        Object item = model.getSelectedItem();
        if (item instanceof Option) {
            String sdkVersion = ((Option) item).getValue();
            if (sdkVersion == null || sdkVersion.isEmpty()) sdkVersion = "-1";
            Log.v("change sdkVersion " + sdkVersion);
            permissionManager.setSdkVersion(Integer.parseInt(sdkVersion));
            setPermissionList(null);
        }
    }

    @Override
    public void intervalAdded(ListDataEvent evt) {}

    @Override
    public void intervalRemoved(ListDataEvent evt) {}

    @Override
    public void itemStateChanged(ItemEvent evt) {
        Object source = evt.getSource();
        if (source instanceof ToggleButtonModel) {
            ToggleButtonModel checkbox = (ToggleButtonModel) source;
            String elemId = checkbox.getActionCommand();
            boolean selected = evt.getStateChange() == ItemEvent.SELECTED;
            RProp.setPropData(elemId, selected);
            if (RProp.PERM_TREAT_SIGN_AS_REVOKED.name().equals(elemId)) {
                permissionManager.setTreatSignAsRevoked(selected);
            }
            setPermissionList(null);
        }
    }

    private String makeHyperEvent(String id, String text, String title) {
        return makeHyperEvent(id, text, title, null);
    }

    private String makeHyperEvent(String id, String text, String title, Object userData) {
        String style = null;
        if (id != null && (id.startsWith("PLUGIN:") || id.contains("-perm-setting")))
            style = "color:white";
        return apkInfoPanel.makeHyperLink("@event", text, title, id, style, userData);
    }

    @Override
    public void hyperlinkClick(HyperlinkClickEvent evt) {
        String id = evt.getId();
        switch (id) {
            case "other-lang":
                String mutiLabels = (String) evt.getUserData();
                if (mutiLabels == null || mutiLabels.isEmpty()) return;
                showDialog(mutiLabels, RStr.LABEL_APP_NAME_LIST.get(), new Dimension(300, 200),
                        null);
                break;
            case "app-version":
                String versionDesc = (String) evt.getUserData();
                showDialog(versionDesc, "App version info", new Dimension(300, 50), null);
                break;
            case "display-list":
            case "function-show-permission":
                showPermList();
                break;
            case "function-show-sdk-info":
            case "min-sdk-info":
            case "target-sdk-info":
            case "max-sdk-info":
                SdkVersionInfoDlg sdkDlg = new SdkVersionInfoDlg(
                        SwingUtilities.getWindowAncestor(this), (Integer) evt.getUserData());
                sdkDlg.setLocationRelativeTo(this);
                sdkDlg.setVisible(true);
                break;
            case "function-create-shortcut":
                SystemUtil.createShortCut(RFile.ETC_APKSCANNER_EXE.getPath(), RStr.APP_NAME.get());
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        if (SystemUtil.hasShortCut(RFile.ETC_APKSCANNER_EXE.getPath(),
                                RStr.APP_NAME.get())) {
                            apkInfoPanel.removeElementById("create-shortcut");
                        }
                    }
                });
                break;
            case "function-assoc-apk":
                SystemUtil.setAssociateFileType(".apk", RFile.ETC_APKSCANNER_EXE.getPath());
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        if (SystemUtil.isAssociatedWithFileType(".apk",
                                RFile.ETC_APKSCANNER_EXE.getPath())) {
                            apkInfoPanel.removeElementById("associate-file");
                        }
                    }
                });
                break;
            case "file-checksum":
                String apkPath = (String) evt.getUserData();
                File apkFile = new File(apkPath);
                StringBuilder checksum = new StringBuilder(apkPath).append("\n")
                        .append(FileUtil.getFileSize(apkFile.length(), FSStyle.FULL))
                        .append("\n\nMD5: ").append(FileUtil.getMessageDigest(apkFile, "MD5"))
                        .append("\n").append("SHA1: ")
                        .append(FileUtil.getMessageDigest(apkFile, "SHA-1")).append("\n")
                        .append("SHA256: ").append(FileUtil.getMessageDigest(apkFile, "SHA-256"));
                showDialog(checksum.toString(), "APK Checksum", new Dimension(650, 150), null);
                break;
            case "show-perm-setting":
                apkInfoPanel.removeElementById("show-perm-setting");
                apkInfoPanel.insertElementBefore("perm-groups", "<div id=\"perm-settings\"><div>");
                StringBuilder settings = new StringBuilder();
                settings.append(" <input id=\"PERM_MARK_RUNTIME\" type=\"checkbox\">");
                settings.append(RStr.LABEL_MARK_A_RUNTIME.get());
                settings.append(" <input id=\"PERM_MARK_COUNT\" type=\"checkbox\">");
                settings.append(RStr.LABEL_MARK_A_COUNT.get());
                settings.append(" <input id=\"PERM_TREAT_SIGN_AS_REVOKED\" type=\"checkbox\">");
                settings.append(RStr.LABEL_TREAT_SIGN_AS_REVOKED.get());
                apkInfoPanel.setInnerHTMLById("perm-settings", settings.toString());
                apkInfoPanel.insertElementLast("perm-group-title",
                        makeHyperEvent("close-perm-setting",
                                String.format("<img src=\"%s\" width=\"16\" height=\"16\">",
                                        RImg.PERM_MARKER_CLOSE.getPath()),
                                null));
                for (String elemId : new String[] {RProp.PERM_MARK_RUNTIME.name(),
                        RProp.PERM_MARK_COUNT.name(), RProp.PERM_TREAT_SIGN_AS_REVOKED.name()}) {
                    Object object = apkInfoPanel.getElementModelById(elemId);
                    if (object instanceof ToggleButtonModel) {
                        ToggleButtonModel checkbox = (ToggleButtonModel) object;
                        if (RProp.PERM_TREAT_SIGN_AS_REVOKED.name().equals(elemId)
                                && permissionManager.isPlatformSigned()) {
                            checkbox.setSelected(false);
                            checkbox.setEnabled(false);
                        } else {
                            checkbox.setSelected((boolean) RProp.getPropData(elemId, true));
                        }
                        checkbox.setActionCommand(elemId);
                        checkbox.addItemListener(this);
                    }
                }
                setInfoAreaHeight(permissionManager.getPermissionGroups().length);
                break;
            case "close-perm-setting":
                apkInfoPanel.removeElementById("close-perm-setting");
                apkInfoPanel.removeElementById("perm-settings");
                apkInfoPanel.insertElementLast("perm-group-title",
                        makeHyperEvent("show-perm-setting",
                                String.format("<img src=\"%s\" width=\"16\" height=\"16\">",
                                        RImg.PERM_MARKER_SETTING.getPath()),
                                null));
                setInfoAreaHeight(permissionManager.getPermissionGroups().length);
                break;
            case "signature-scheme":
                String signScheme = "APK Signature Scheme " + ((String) evt.getUserData());
                showDialog(signScheme, signScheme, new Dimension(300, 50), null);
                break;
            default:
                if (id.startsWith("feature-")) {
                    showFeatureInfo(id, evt.getUserData());
                } else if (id.startsWith("PermGroup:")) {
                    showPermDetailDesc(evt);
                } else if (id.startsWith("PLUGIN:")) {
                    PlugIn plugin =
                            PlugInManager.getPlugInByActionCommand((String) evt.getUserData());
                    if (plugin != null) {
                        plugin.launch();
                    }
                } else {
                    Log.w("Unknown id " + id);
                }
        }
    }

    private void showFeatureInfo(String id, Object userData) {
        String feature = null;
        Dimension size = new Dimension(400, 100);

        switch (id) {
            case "feature-hidden":
                feature = RStr.FEATURE_HIDDEN_DESC.get();
                break;
            case "feature-launcher":
                feature = RStr.FEATURE_LAUNCHER_DESC.get();
                break;
            case "feature-startup":
                feature = RStr.FEATURE_STARTUP_DESC.get();
                feature += "\nandroid.permission.RECEIVE_BOOT_COMPLETED";
                break;
            case "feature-shared-user-id":
                feature = "sharedUserId=" + userData + "\n※ ";
                feature += RStr.FEATURE_SHAREDUSERID_DESC.get();
                break;
            case "feature-system-user-id":
                feature = "sharedUserId=android.uid.system\n※ ";
                feature += RStr.FEATURE_SYSTEM_UID_DESC.get();
                break;
            case "feature-platform-sign":
                feature = "※ " + RStr.FEATURE_PLATFORM_SIGN_DESC.get();
                feature += "\n\n" + userData;
                size = new Dimension(500, 150);
                break;
            case "feature-samsung-sign":
                feature = "※ " + RStr.FEATURE_SAMSUNG_SIGN_DESC.get();
                feature += "\n\n" + userData;
                size = new Dimension(500, 150);
                break;
            case "feature-debuggable":
                feature = RStr.FEATURE_DEBUGGABLE_DESC.get();
                break;
            case "feature-instrumentation":
                feature = RStr.FEATURE_INSTRUMENTATION_DESC.get();
                break;
            case "feature-device-requirements":
                feature = (String) userData;
                size = new Dimension(500, 250);
                break;
            case "feature-install-location-internal":
                feature = RStr.FEATURE_ILOCATION_INTERNAL_DESC.get();
                break;
            case "feature-install-location-auto":
                feature = RStr.FEATURE_ILOCATION_AUTO_DESC.get();
                break;
            case "feature-install-location-external":
                feature = RStr.FEATURE_ILOCATION_EXTERNAL_DESC.get();
                break;
        }
        showDialog(feature, "Feature info", size, null);
    }

    private void showPermList() {
        PermissionHistoryPanel historyView = new PermissionHistoryPanel();
        historyView.setPermissionManager(permissionManager);
        historyView.showDialog(SwingUtilities.getWindowAncestor(this));
    }

    private void showPermDetailDesc(HyperlinkClickEvent evt) {
        PermissionHistoryPanel historyView = new PermissionHistoryPanel();
        historyView.setPermissionManager(permissionManager);
        historyView.setFilterText((String) evt.getUserData());
        historyView.showDialog(SwingUtilities.getWindowAncestor(this));
    }

    private void showDialog(String content, String title, Dimension size, Icon icon) {
        MessageBoxPane.showTextAreaDialog(SwingUtilities.getWindowAncestor(this), content, title,
                MessageBoxPane.INFORMATION_MESSAGE, icon, size);
    }
}
