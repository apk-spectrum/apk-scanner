#include <stdio.h>
#include <stdlib.h>

#include <androidfw/ResourceTypes.h>

#include "com_apkscanner_core_scanner_AaptNativeScanner.h"
#include "JniCharacterSet.h"

#include "AaptXml.h"
#include "XMLNode.h"

static void printResolvedResourceAttribute(const ResTable& resTable, const ResXMLTree& tree,
        uint32_t attrRes, const String8& attrLabel, String8* outError)
{
    Res_value value;
    AaptXml::getResolvedResourceAttribute(resTable, tree, attrRes, &value, outError);
    if (*outError != "") {
        *outError = "error print resolved resource attribute";
        return;
    }
    if (value.dataType == Res_value::TYPE_STRING) {
        String8 result = AaptXml::getResolvedAttribute(resTable, tree, attrRes, outError);
        printf("%s='%s'", attrLabel.string(),
                ResTable::normalizeForOutput(result.string()).string());
    } else if (Res_value::TYPE_FIRST_INT <= value.dataType &&
            value.dataType <= Res_value::TYPE_LAST_INT) {
        printf("%s='%d'", attrLabel.string(), value.data);
    } else {
        printf("%s='0x%x'", attrLabel.string(), (int)value.data);
    }
}

// These are attribute resource constants for the platform, as found
// in android.R.attr
enum {
    LABEL_ATTR = 0x01010001,
    ICON_ATTR = 0x01010002,
    NAME_ATTR = 0x01010003,
    PERMISSION_ATTR = 0x01010006,
    EXPORTED_ATTR = 0x01010010,
    GRANT_URI_PERMISSIONS_ATTR = 0x0101001b,
    RESOURCE_ATTR = 0x01010025,
    DEBUGGABLE_ATTR = 0x0101000f,
    VALUE_ATTR = 0x01010024,
    VERSION_CODE_ATTR = 0x0101021b,
    VERSION_NAME_ATTR = 0x0101021c,
    SCREEN_ORIENTATION_ATTR = 0x0101001e,
    MIN_SDK_VERSION_ATTR = 0x0101020c,
    MAX_SDK_VERSION_ATTR = 0x01010271,
    REQ_TOUCH_SCREEN_ATTR = 0x01010227,
    REQ_KEYBOARD_TYPE_ATTR = 0x01010228,
    REQ_HARD_KEYBOARD_ATTR = 0x01010229,
    REQ_NAVIGATION_ATTR = 0x0101022a,
    REQ_FIVE_WAY_NAV_ATTR = 0x01010232,
    TARGET_SDK_VERSION_ATTR = 0x01010270,
    TEST_ONLY_ATTR = 0x01010272,
    ANY_DENSITY_ATTR = 0x0101026c,
    GL_ES_VERSION_ATTR = 0x01010281,
    SMALL_SCREEN_ATTR = 0x01010284,
    NORMAL_SCREEN_ATTR = 0x01010285,
    LARGE_SCREEN_ATTR = 0x01010286,
    XLARGE_SCREEN_ATTR = 0x010102bf,
    REQUIRED_ATTR = 0x0101028e,
    INSTALL_LOCATION_ATTR = 0x010102b7,
    SCREEN_SIZE_ATTR = 0x010102ca,
    SCREEN_DENSITY_ATTR = 0x010102cb,
    REQUIRES_SMALLEST_WIDTH_DP_ATTR = 0x01010364,
    COMPATIBLE_WIDTH_LIMIT_DP_ATTR = 0x01010365,
    LARGEST_WIDTH_LIMIT_DP_ATTR = 0x01010366,
    PUBLIC_KEY_ATTR = 0x010103a6,
    CATEGORY_ATTR = 0x010103e8,
    BANNER_ATTR = 0x10103f2,
    ISGAME_ATTR = 0x10103f4,
    REQUIRED_FEATURE_ATTR = 0x1010557,
    REQUIRED_NOT_FEATURE_ATTR = 0x1010558,
};

extern String8 getComponentName(String8 &pkgName, String8 &componentName);

static void printCompatibleScreens(ResXMLTree& tree, String8* outError) {
    size_t len;
    ResXMLTree::event_code_t code;
    int depth = 0;
    bool first = true;
    printf("compatible-screens:");
    while ((code=tree.next()) != ResXMLTree::END_DOCUMENT && code != ResXMLTree::BAD_DOCUMENT) {
        if (code == ResXMLTree::END_TAG) {
            depth--;
            if (depth < 0) {
                break;
            }
            continue;
        }
        if (code != ResXMLTree::START_TAG) {
            continue;
        }
        depth++;
        const char16_t* ctag16 = tree.getElementName(&len);
        if (ctag16 == NULL) {
            *outError = "failed to get XML element name (bad string pool)";
            return;
        }
        String8 tag(ctag16);
        if (tag == "screen") {
            int32_t screenSize = AaptXml::getIntegerAttribute(tree,
                    SCREEN_SIZE_ATTR);
            int32_t screenDensity = AaptXml::getIntegerAttribute(tree,
                    SCREEN_DENSITY_ATTR);
            if (screenSize > 0 && screenDensity > 0) {
                if (!first) {
                    printf(",");
                }
                first = false;
                printf("'%d/%d'", screenSize, screenDensity);
            }
        }
    }
    printf("\n");
}

static void printUsesPermission(const String8& name, bool optional=false, int maxSdkVersion=-1,
        const String8& requiredFeature = String8::empty(),
        const String8& requiredNotFeature = String8::empty()) {
    printf("uses-permission: name='%s'", ResTable::normalizeForOutput(name.string()).string());
    if (maxSdkVersion != -1) {
         printf(" maxSdkVersion='%d'", maxSdkVersion);
    }
    if (requiredFeature.length() > 0) {
         printf(" requiredFeature='%s'", requiredFeature.string());
    }
    if (requiredNotFeature.length() > 0) {
         printf(" requiredNotFeature='%s'", requiredNotFeature.string());
    }
    printf("\n");

    if (optional) {
        printf("optional-permission: name='%s'",
                ResTable::normalizeForOutput(name.string()).string());
        if (maxSdkVersion != -1) {
            printf(" maxSdkVersion='%d'", maxSdkVersion);
        }
        printf("\n");
    }
}

static void printUsesPermissionSdk23(const String8& name, int maxSdkVersion=-1) {
    printf("uses-permission-sdk-23: ");

    printf("name='%s'", ResTable::normalizeForOutput(name.string()).string());
    if (maxSdkVersion != -1) {
        printf(" maxSdkVersion='%d'", maxSdkVersion);
    }
    printf("\n");
}

static void printUsesImpliedPermission(const String8& name, const String8& reason,
        const int32_t maxSdkVersion = -1) {
    printf("uses-implied-permission: name='%s'",
            ResTable::normalizeForOutput(name.string()).string());
    if (maxSdkVersion != -1) {
        printf(" maxSdkVersion='%d'", maxSdkVersion);
    }
    printf(" reason='%s'\n", ResTable::normalizeForOutput(reason.string()).string());
}

extern Vector<String8> getNfcAidCategories(AssetManager& assets, const String8& xmlPath, bool offHost,
        String8 *outError = NULL);

static void printComponentPresence(const char* componentName) {
    printf("provides-component:'%s'\n", componentName);
}

/**
 * Represents a feature that has been automatically added due to
 * a pre-requisite or some other reason.
 */
struct ImpliedFeature {
    ImpliedFeature() : impliedBySdk23(false) {}
    ImpliedFeature(const String8& n, bool sdk23) : name(n), impliedBySdk23(sdk23) {}

    /**
     * Name of the implied feature.
     */
    String8 name;

    /**
     * Was this implied by a permission from SDK 23 (<uses-permission-sdk-23 />)?
     */
    bool impliedBySdk23;

    /**
     * List of human-readable reasons for why this feature was implied.
     */
    SortedVector<String8> reasons;
};

struct Feature {
    Feature() : required(false), version(-1) {}
    explicit Feature(bool required, int32_t version = -1) : required(required), version(version) {}

    /**
     * Whether the feature is required.
     */
    bool required;

    /**
     * What version of the feature is requested.
     */
    int32_t version;
};

/**
 * Represents a <feature-group> tag in the AndroidManifest.xml
 */
struct FeatureGroup {
    FeatureGroup() : openGLESVersion(-1) {}

    /**
     * Human readable label
     */
    String8 label;

    /**
     * Explicit features defined in the group
     */
    KeyedVector<String8, Feature> features;

    /**
     * OpenGL ES version required
     */
    int openGLESVersion;
};

static bool hasFeature(const char* name, const FeatureGroup& grp,
                       const KeyedVector<String8, ImpliedFeature>& implied) {
    String8 name8(name);
    ssize_t idx = grp.features.indexOfKey(name8);
    if (idx < 0) {
        idx = implied.indexOfKey(name8);
    }
    return idx >= 0;
}

static void addImpliedFeature(KeyedVector<String8, ImpliedFeature>* impliedFeatures,
                              const char* name, const String8& reason, bool sdk23) {
    String8 name8(name);
    ssize_t idx = impliedFeatures->indexOfKey(name8);
    if (idx < 0) {
        idx = impliedFeatures->add(name8, ImpliedFeature(name8, sdk23));
    }

    ImpliedFeature* feature = &impliedFeatures->editValueAt(idx);

    // A non-sdk 23 implied feature takes precedence.
    if (feature->impliedBySdk23 && !sdk23) {
        feature->impliedBySdk23 = false;
    }
    feature->reasons.add(reason);
}

static void printFeatureGroupImpl(const FeatureGroup& grp,
                                  const KeyedVector<String8, ImpliedFeature>* impliedFeatures) {
    printf("feature-group: label='%s'\n", grp.label.string());

    if (grp.openGLESVersion > 0) {
        printf("  uses-gl-es: '0x%x'\n", grp.openGLESVersion);
    }

    const size_t numFeatures = grp.features.size();
    for (size_t i = 0; i < numFeatures; i++) {
        const Feature& feature = grp.features[i];
        const bool required = feature.required;
        const int32_t version = feature.version;

        const String8& featureName = grp.features.keyAt(i);
        printf("  uses-feature%s: name='%s'", (required ? "" : "-not-required"),
                ResTable::normalizeForOutput(featureName.string()).string());

        if (version > 0) {
            printf(" version='%d'", version);
        }
        printf("\n");
    }

    const size_t numImpliedFeatures =
        (impliedFeatures != NULL) ? impliedFeatures->size() : 0;
    for (size_t i = 0; i < numImpliedFeatures; i++) {
        const ImpliedFeature& impliedFeature = impliedFeatures->valueAt(i);
        if (grp.features.indexOfKey(impliedFeature.name) >= 0) {
            // The feature is explicitly set, no need to use implied
            // definition.
            continue;
        }

        String8 printableFeatureName(ResTable::normalizeForOutput(
                    impliedFeature.name.string()));
        const char* sdk23Suffix = impliedFeature.impliedBySdk23 ? "-sdk-23" : "";

        printf("  uses-feature%s: name='%s'\n", sdk23Suffix, printableFeatureName.string());
        printf("  uses-implied-feature%s: name='%s' reason='", sdk23Suffix,
               printableFeatureName.string());
        const size_t numReasons = impliedFeature.reasons.size();
        for (size_t j = 0; j < numReasons; j++) {
            printf("%s", impliedFeature.reasons[j].string());
            if (j + 2 < numReasons) {
                printf(", ");
            } else if (j + 1 < numReasons) {
                printf(", and ");
            }
        }
        printf("'\n");
    }
}

static void printFeatureGroup(const FeatureGroup& grp) {
    printFeatureGroupImpl(grp, NULL);
}

static void printDefaultFeatureGroup(const FeatureGroup& grp,
                                     const KeyedVector<String8, ImpliedFeature>& impliedFeatures) {
    printFeatureGroupImpl(grp, &impliedFeatures);
}

static void addParentFeatures(FeatureGroup* grp, const String8& name) {
    if (name == "android.hardware.camera.autofocus" ||
            name == "android.hardware.camera.flash") {
        grp->features.add(String8("android.hardware.camera"), Feature(true));
    } else if (name == "android.hardware.location.gps" ||
            name == "android.hardware.location.network") {
        grp->features.add(String8("android.hardware.location"), Feature(true));
    } else if (name == "android.hardware.faketouch.multitouch") {
        grp->features.add(String8("android.hardware.faketouch"), Feature(true));
    } else if (name == "android.hardware.faketouch.multitouch.distinct" ||
            name == "android.hardware.faketouch.multitouch.jazzhands") {
        grp->features.add(String8("android.hardware.faketouch.multitouch"), Feature(true));
        grp->features.add(String8("android.hardware.faketouch"), Feature(true));
    } else if (name == "android.hardware.touchscreen.multitouch") {
        grp->features.add(String8("android.hardware.touchscreen"), Feature(true));
    } else if (name == "android.hardware.touchscreen.multitouch.distinct" ||
            name == "android.hardware.touchscreen.multitouch.jazzhands") {
        grp->features.add(String8("android.hardware.touchscreen.multitouch"), Feature(true));
        grp->features.add(String8("android.hardware.touchscreen"), Feature(true));
    } else if (name == "android.hardware.opengles.aep") {
        const int openGLESVersion31 = 0x00030001;
        if (openGLESVersion31 > grp->openGLESVersion) {
            grp->openGLESVersion = openGLESVersion31;
        }
    }
}

static void addImpliedFeaturesForPermission(const int targetSdk, const String8& name,
                                            KeyedVector<String8, ImpliedFeature>* impliedFeatures,
                                            bool impliedBySdk23Permission) {
    if (name == "android.permission.CAMERA") {
        addImpliedFeature(impliedFeatures, "android.hardware.camera",
                          String8::format("requested %s permission", name.string()),
                          impliedBySdk23Permission);
    } else if (name == "android.permission.ACCESS_FINE_LOCATION") {
        if (targetSdk < SDK_LOLLIPOP) {
            addImpliedFeature(impliedFeatures, "android.hardware.location.gps",
                              String8::format("requested %s permission", name.string()),
                              impliedBySdk23Permission);
            addImpliedFeature(impliedFeatures, "android.hardware.location.gps",
                              String8::format("targetSdkVersion < %d", SDK_LOLLIPOP),
                              impliedBySdk23Permission);
        }
        addImpliedFeature(impliedFeatures, "android.hardware.location",
                String8::format("requested %s permission", name.string()),
                impliedBySdk23Permission);
    } else if (name == "android.permission.ACCESS_COARSE_LOCATION") {
        if (targetSdk < SDK_LOLLIPOP) {
            addImpliedFeature(impliedFeatures, "android.hardware.location.network",
                              String8::format("requested %s permission", name.string()),
                              impliedBySdk23Permission);
            addImpliedFeature(impliedFeatures, "android.hardware.location.network",
                              String8::format("targetSdkVersion < %d", SDK_LOLLIPOP),
                              impliedBySdk23Permission);
        }
        addImpliedFeature(impliedFeatures, "android.hardware.location",
                          String8::format("requested %s permission", name.string()),
                          impliedBySdk23Permission);
    } else if (name == "android.permission.ACCESS_MOCK_LOCATION" ||
               name == "android.permission.ACCESS_LOCATION_EXTRA_COMMANDS" ||
               name == "android.permission.INSTALL_LOCATION_PROVIDER") {
        addImpliedFeature(impliedFeatures, "android.hardware.location",
                          String8::format("requested %s permission", name.string()),
                          impliedBySdk23Permission);
    } else if (name == "android.permission.BLUETOOTH" ||
               name == "android.permission.BLUETOOTH_ADMIN") {
        if (targetSdk > SDK_DONUT) {
            addImpliedFeature(impliedFeatures, "android.hardware.bluetooth",
                              String8::format("requested %s permission", name.string()),
                              impliedBySdk23Permission);
            addImpliedFeature(impliedFeatures, "android.hardware.bluetooth",
                              String8::format("targetSdkVersion > %d", SDK_DONUT),
                              impliedBySdk23Permission);
        }
    } else if (name == "android.permission.RECORD_AUDIO") {
        addImpliedFeature(impliedFeatures, "android.hardware.microphone",
                          String8::format("requested %s permission", name.string()),
                          impliedBySdk23Permission);
    } else if (name == "android.permission.ACCESS_WIFI_STATE" ||
               name == "android.permission.CHANGE_WIFI_STATE" ||
               name == "android.permission.CHANGE_WIFI_MULTICAST_STATE") {
        addImpliedFeature(impliedFeatures, "android.hardware.wifi",
                          String8::format("requested %s permission", name.string()),
                          impliedBySdk23Permission);
    } else if (name == "android.permission.CALL_PHONE" ||
               name == "android.permission.CALL_PRIVILEGED" ||
               name == "android.permission.MODIFY_PHONE_STATE" ||
               name == "android.permission.PROCESS_OUTGOING_CALLS" ||
               name == "android.permission.READ_SMS" ||
               name == "android.permission.RECEIVE_SMS" ||
               name == "android.permission.RECEIVE_MMS" ||
               name == "android.permission.RECEIVE_WAP_PUSH" ||
               name == "android.permission.SEND_SMS" ||
               name == "android.permission.WRITE_APN_SETTINGS" ||
               name == "android.permission.WRITE_SMS") {
        addImpliedFeature(impliedFeatures, "android.hardware.telephony",
                          String8("requested a telephony permission"),
                          impliedBySdk23Permission);
    }
}

/*
 * Handle the "dump" command, to extract select data from an archive.
 */
extern char CONSOLE_DATA[2925]; // see EOF
int doDump_copy(Bundle* bundle)
{
    status_t result = UNKNOWN_ERROR;

    if (bundle->getFileSpecCount() < 1) {
        fprintf(stderr, "ERROR: no dump option specified\n");
        return 1;
    }

    if (bundle->getFileSpecCount() < 2) {
        fprintf(stderr, "ERROR: no dump file specified\n");
        return 1;
    }

    const char* option = bundle->getFileSpecEntry(0);
    const char* filename = bundle->getFileSpecEntry(1);

    AssetManager assets;
    int32_t assetsCookie;
    if (!assets.addAssetPath(String8(filename), &assetsCookie)) {
        fprintf(stderr, "ERROR: dump failed because assets could not be loaded\n");
        return 1;
    }

    // Now add any dependencies passed in.
    for (size_t i = 0; i < bundle->getPackageIncludes().size(); i++) {
      const String8& assetPath = bundle->getPackageIncludes()[i];
      if (!assets.addAssetPath(assetPath, NULL)) {
        fprintf(stderr, "ERROR: included asset path %s could not be loaded\n", assetPath.string());
        return 1;
      }
    }

    // Make a dummy config for retrieving resources...  we need to supply
    // non-default values for some configs so that we can retrieve resources
    // in the app that don't have a default.  The most important of these is
    // the API version because key resources like icons will have an implicit
    // version if they are using newer config types like density.
    ResTable_config config;
    memset(&config, 0, sizeof(ResTable_config));
    config.language[0] = 'e';
    config.language[1] = 'n';
    config.country[0] = 'U';
    config.country[1] = 'S';
    config.orientation = ResTable_config::ORIENTATION_PORT;
    config.density = ResTable_config::DENSITY_MEDIUM;
    config.sdkVersion = 10000; // Very high.
    config.screenWidthDp = 320;
    config.screenHeightDp = 480;
    config.smallestScreenWidthDp = 320;
    config.screenLayout |= ResTable_config::SCREENSIZE_NORMAL;
    assets.setConfiguration(config);

    const ResTable& res = assets.getResources(false);
    if (res.getError() != NO_ERROR) {
        fprintf(stderr, "ERROR: dump failed because the resource table is invalid/corrupt.\n");
        return 1;
    }

    // Source for AndroidManifest.xml
    const String8 manifestFile("AndroidManifest.xml");

    // The dynamicRefTable can be null if there are no resources for this asset cookie.
    // This fine.
    const DynamicRefTable* dynamicRefTable = res.getDynamicRefTableForCookie(assetsCookie);

    Asset* asset = NULL;

    if (strcmp("resources", option) == 0) {
#ifndef __ANDROID__
        res.print(bundle->getValues());
#endif

    } else if (strcmp("strings", option) == 0) {
        const ResStringPool* pool = res.getTableStringBlock(0);
        printStringPool(pool);

    } else if (strcmp("xmltree", option) == 0) {
        if (bundle->getFileSpecCount() < 3) {
            fprintf(stderr, "ERROR: no dump xmltree resource file specified\n");
            goto bail;
        }

        for (int i=2; i<bundle->getFileSpecCount(); i++) {
            const char* resname = bundle->getFileSpecEntry(i);
            ResXMLTree tree(dynamicRefTable);
            asset = assets.openNonAsset(assetsCookie, resname, Asset::ACCESS_BUFFER);
            if (asset == NULL) {
                fprintf(stderr, "ERROR: dump failed because resource %s not found\n", resname);
                goto bail;
            }

            if (tree.setTo(asset->getBuffer(true),
                           asset->getLength()) != NO_ERROR) {
                fprintf(stderr, "ERROR: Resource %s is corrupt\n", resname);
                goto bail;
            }
            tree.restart();
            printXMLBlock(&tree);
            tree.uninit();
            delete asset;
            asset = NULL;
        }

    } else if (strcmp("xmlstrings", option) == 0) {
        if (bundle->getFileSpecCount() < 3) {
            fprintf(stderr, "ERROR: no dump xmltree resource file specified\n");
            goto bail;
        }

        for (int i=2; i<bundle->getFileSpecCount(); i++) {
            const char* resname = bundle->getFileSpecEntry(i);
            asset = assets.openNonAsset(assetsCookie, resname, Asset::ACCESS_BUFFER);
            if (asset == NULL) {
                fprintf(stderr, "ERROR: dump failed because resource %s found\n", resname);
                goto bail;
            }

            ResXMLTree tree(dynamicRefTable);
            if (tree.setTo(asset->getBuffer(true),
                           asset->getLength()) != NO_ERROR) {
                fprintf(stderr, "ERROR: Resource %s is corrupt\n", resname);
                goto bail;
            }
            printStringPool(&tree.getStrings());
            delete asset;
            asset = NULL;
        }

    } else {
        asset = assets.openNonAsset(assetsCookie, "AndroidManifest.xml", Asset::ACCESS_BUFFER);
        if (asset == NULL) {
            fprintf(stderr, "ERROR: dump failed because no AndroidManifest.xml found\n");
            goto bail;
        }

        ResXMLTree tree(dynamicRefTable);
        if (tree.setTo(asset->getBuffer(true),
                       asset->getLength()) != NO_ERROR) {
            fprintf(stderr, "ERROR: AndroidManifest.xml is corrupt\n");
            goto bail;
        }
        tree.restart();

        if (strcmp("permissions", option) == 0) {
            size_t len;
            ResXMLTree::event_code_t code;
            int depth = 0;
            while ((code=tree.next()) != ResXMLTree::END_DOCUMENT &&
                    code != ResXMLTree::BAD_DOCUMENT) {
                if (code == ResXMLTree::END_TAG) {
                    depth--;
                    continue;
                }
                if (code != ResXMLTree::START_TAG) {
                    continue;
                }
                depth++;
                const char16_t* ctag16 = tree.getElementName(&len);
                if (ctag16 == NULL) {
                    SourcePos(manifestFile, tree.getLineNumber()).error(
                            "ERROR: failed to get XML element name (bad string pool)");
                    goto bail;
                }
                String8 tag(ctag16);
                //printf("Depth %d tag %s\n", depth, tag.string());
                if (depth == 1) {
                    if (tag != "manifest") {
                        SourcePos(manifestFile, tree.getLineNumber()).error(
                                "ERROR: manifest does not start with <manifest> tag");
                        goto bail;
                    }
                    String8 pkg = AaptXml::getAttribute(tree, NULL, "package", NULL);
                    printf("package: %s\n", ResTable::normalizeForOutput(pkg.string()).string());
                } else if (depth == 2) {
                    if (tag == "permission") {
                        String8 error;
                        String8 name = AaptXml::getAttribute(tree, NAME_ATTR, &error);
                        if (error != "") {
                            SourcePos(manifestFile, tree.getLineNumber()).error(
                                    "ERROR getting 'android:name': %s", error.string());
                            goto bail;
                        }

                        if (name == "") {
                            SourcePos(manifestFile, tree.getLineNumber()).error(
                                    "ERROR: missing 'android:name' for permission");
                            goto bail;
                        }
                        printf("permission: %s\n",
                                ResTable::normalizeForOutput(name.string()).string());
                    } else if (tag == "uses-permission") {
                        String8 error;
                        String8 name = AaptXml::getAttribute(tree, NAME_ATTR, &error);
                        if (error != "") {
                            SourcePos(manifestFile, tree.getLineNumber()).error(
                                    "ERROR getting 'android:name' attribute: %s", error.string());
                            goto bail;
                        }

                        if (name == "") {
                            SourcePos(manifestFile, tree.getLineNumber()).error(
                                    "ERROR: missing 'android:name' for uses-permission");
                            goto bail;
                        }
                        printUsesPermission(name,
                                AaptXml::getIntegerAttribute(tree, REQUIRED_ATTR, 1) == 0,
                                AaptXml::getIntegerAttribute(tree, MAX_SDK_VERSION_ATTR));
                    } else if (tag == "uses-permission-sdk-23" || tag == "uses-permission-sdk-m") {
                        String8 error;
                        String8 name = AaptXml::getAttribute(tree, NAME_ATTR, &error);
                        if (error != "") {
                            SourcePos(manifestFile, tree.getLineNumber()).error(
                                    "ERROR getting 'android:name' attribute: %s", error.string());
                            goto bail;
                        }

                        if (name == "") {
                            SourcePos(manifestFile, tree.getLineNumber()).error(
                                    "ERROR: missing 'android:name' for uses-permission-sdk-23");
                            goto bail;
                        }
                        printUsesPermissionSdk23(
                                name,
                                AaptXml::getIntegerAttribute(tree, MAX_SDK_VERSION_ATTR));
                    }
                }
            }
        } else if (strcmp("badging", option) == 0) {
            Vector<String8> locales;
            res.getLocales(&locales);

            Vector<ResTable_config> configs;
            res.getConfigurations(&configs);
            SortedVector<int> densities;
            const size_t NC = configs.size();
            for (size_t i=0; i<NC; i++) {
                int dens = configs[i].density;
                if (dens == 0) {
                    dens = 160;
                }
                densities.add(dens);
            }

            size_t len;
            ResXMLTree::event_code_t code;
            int depth = 0;
            String8 error;
            bool withinActivity = false;
            bool isMainActivity = false;
            bool isLauncherActivity = false;
            bool isLeanbackLauncherActivity = false;
            bool isSearchable = false;
            bool withinApplication = false;
            bool withinSupportsInput = false;
            bool withinFeatureGroup = false;
            bool withinReceiver = false;
            bool withinService = false;
            bool withinProvider = false;
            bool withinIntentFilter = false;
            bool hasMainActivity = false;
            bool hasOtherActivities = false;
            bool hasOtherReceivers = false;
            bool hasOtherServices = false;
            bool hasIntentFilter = false;

            bool hasWallpaperService = false;
            bool hasImeService = false;
            bool hasAccessibilityService = false;
            bool hasPrintService = false;
            bool hasWidgetReceivers = false;
            bool hasDeviceAdminReceiver = false;
            bool hasPaymentService = false;
            bool hasDocumentsProvider = false;
            bool hasCameraActivity = false;
            bool hasCameraSecureActivity = false;
            bool hasLauncher = false;
            bool hasNotificationListenerService = false;
            bool hasDreamService = false;

            bool actMainActivity = false;
            bool actWidgetReceivers = false;
            bool actDeviceAdminEnabled = false;
            bool actImeService = false;
            bool actWallpaperService = false;
            bool actAccessibilityService = false;
            bool actPrintService = false;
            bool actHostApduService = false;
            bool actOffHostApduService = false;
            bool actDocumentsProvider = false;
            bool actNotificationListenerService = false;
            bool actDreamService = false;
            bool actCamera = false;
            bool actCameraSecure = false;
            bool catLauncher = false;
            bool hasMetaHostPaymentCategory = false;
            bool hasMetaOffHostPaymentCategory = false;

            // These permissions are required by services implementing services
            // the system binds to (IME, Accessibility, PrintServices, etc.)
            bool hasBindDeviceAdminPermission = false;
            bool hasBindInputMethodPermission = false;
            bool hasBindAccessibilityServicePermission = false;
            bool hasBindPrintServicePermission = false;
            bool hasBindNfcServicePermission = false;
            bool hasRequiredSafAttributes = false;
            bool hasBindNotificationListenerServicePermission = false;
            bool hasBindDreamServicePermission = false;

            // These two implement the implicit permissions that are granted
            // to pre-1.6 applications.
            bool hasWriteExternalStoragePermission = false;
            int32_t writeExternalStoragePermissionMaxSdkVersion = -1;
            bool hasReadPhoneStatePermission = false;

            // If an app requests write storage, they will also get read storage.
            bool hasReadExternalStoragePermission = false;

            // Implement transition to read and write call log.
            bool hasReadContactsPermission = false;
            bool hasWriteContactsPermission = false;
            bool hasReadCallLogPermission = false;
            bool hasWriteCallLogPermission = false;

            // If an app declares itself as multiArch, we report the
            // native libraries differently.
            bool hasMultiArch = false;

            // This next group of variables is used to implement a group of
            // backward-compatibility heuristics necessitated by the addition of
            // some new uses-feature constants in 2.1 and 2.2. In most cases, the
            // heuristic is "if an app requests a permission but doesn't explicitly
            // request the corresponding <uses-feature>, presume it's there anyway".

            // 2.2 also added some other features that apps can request, but that
            // have no corresponding permission, so we cannot implement any
            // back-compatibility heuristic for them. The below are thus unnecessary
            // (but are retained here for documentary purposes.)
            //bool specCompassFeature = false;
            //bool specAccelerometerFeature = false;
            //bool specProximityFeature = false;
            //bool specAmbientLightFeature = false;
            //bool specLiveWallpaperFeature = false;

            int targetSdk = 0;
            int smallScreen = 1;
            int normalScreen = 1;
            int largeScreen = 1;
            int xlargeScreen = 1;
            int anyDensity = 1;
            int requiresSmallestWidthDp = 0;
            int compatibleWidthLimitDp = 0;
            int largestWidthLimitDp = 0;
            String8 pkg;
            String8 activityName;
            String8 activityLabel;
            String8 activityIcon;
            String8 activityBanner;
            String8 receiverName;
            String8 serviceName;
            Vector<String8> supportedInput;

            FeatureGroup commonFeatures;
            Vector<FeatureGroup> featureGroups;
            KeyedVector<String8, ImpliedFeature> impliedFeatures;

            while ((code=tree.next()) != ResXMLTree::END_DOCUMENT &&
                    code != ResXMLTree::BAD_DOCUMENT) {
                if (code == ResXMLTree::END_TAG) {
                    depth--;
                    if (depth < 2) {
                        if (withinSupportsInput && !supportedInput.isEmpty()) {
                            printf("supports-input: '");
                            const size_t N = supportedInput.size();
                            for (size_t i=0; i<N; i++) {
                                printf("%s", ResTable::normalizeForOutput(
                                        supportedInput[i].string()).string());
                                if (i != N - 1) {
                                    printf("' '");
                                } else {
                                    printf("'\n");
                                }
                            }
                            supportedInput.clear();
                        }
                        withinApplication = false;
                        withinSupportsInput = false;
                        withinFeatureGroup = false;
                    } else if (depth < 3) {
                        if (withinActivity && isMainActivity) {
                            String8 aName(getComponentName(pkg, activityName));
                            if (isLauncherActivity) {
                                printf("launchable-activity:");
                                if (aName.length() > 0) {
                                    printf(" name='%s' ",
                                            ResTable::normalizeForOutput(aName.string()).string());
                                }
                                printf(" label='%s' icon='%s'\n",
                                       ResTable::normalizeForOutput(activityLabel.string())
                                                .string(),
                                       ResTable::normalizeForOutput(activityIcon.string())
                                                .string());
                            }
                            if (isLeanbackLauncherActivity) {
                                printf("leanback-launchable-activity:");
                                if (aName.length() > 0) {
                                    printf(" name='%s' ",
                                            ResTable::normalizeForOutput(aName.string()).string());
                                }
                                printf(" label='%s' icon='%s' banner='%s'\n",
                                       ResTable::normalizeForOutput(activityLabel.string())
                                                .string(),
                                       ResTable::normalizeForOutput(activityIcon.string())
                                                .string(),
                                       ResTable::normalizeForOutput(activityBanner.string())
                                                .string());
                            }
                        }
                        if (!hasIntentFilter) {
                            hasOtherActivities |= withinActivity;
                            hasOtherReceivers |= withinReceiver;
                            hasOtherServices |= withinService;
                        } else {
                            if (withinService) {
                                hasPaymentService |= (actHostApduService && hasMetaHostPaymentCategory &&
                                        hasBindNfcServicePermission);
                                hasPaymentService |= (actOffHostApduService && hasMetaOffHostPaymentCategory &&
                                        hasBindNfcServicePermission);
                            }
                        }
                        withinActivity = false;
                        withinService = false;
                        withinReceiver = false;
                        withinProvider = false;
                        hasIntentFilter = false;
                        isMainActivity = isLauncherActivity = isLeanbackLauncherActivity = false;
                    } else if (depth < 4) {
                        if (withinIntentFilter) {
                            if (withinActivity) {
                                hasMainActivity |= actMainActivity;
                                hasLauncher |= catLauncher;
                                hasCameraActivity |= actCamera;
                                hasCameraSecureActivity |= actCameraSecure;
                                hasOtherActivities |=
                                        !actMainActivity && !actCamera && !actCameraSecure;
                            } else if (withinReceiver) {
                                hasWidgetReceivers |= actWidgetReceivers;
                                hasDeviceAdminReceiver |= (actDeviceAdminEnabled &&
                                        hasBindDeviceAdminPermission);
                                hasOtherReceivers |=
                                        (!actWidgetReceivers && !actDeviceAdminEnabled);
                            } else if (withinService) {
                                hasImeService |= actImeService;
                                hasWallpaperService |= actWallpaperService;
                                hasAccessibilityService |= (actAccessibilityService &&
                                        hasBindAccessibilityServicePermission);
                                hasPrintService |=
                                        (actPrintService && hasBindPrintServicePermission);
                                hasNotificationListenerService |= actNotificationListenerService &&
                                        hasBindNotificationListenerServicePermission;
                                hasDreamService |= actDreamService && hasBindDreamServicePermission;
                                hasOtherServices |= (!actImeService && !actWallpaperService &&
                                        !actAccessibilityService && !actPrintService &&
                                        !actHostApduService && !actOffHostApduService &&
                                        !actNotificationListenerService);
                            } else if (withinProvider) {
                                hasDocumentsProvider |=
                                        actDocumentsProvider && hasRequiredSafAttributes;
                            }
                        }
                        withinIntentFilter = false;
                    }
                    continue;
                }
                if (code != ResXMLTree::START_TAG) {
                    continue;
                }
                depth++;

                const char16_t* ctag16 = tree.getElementName(&len);
                if (ctag16 == NULL) {
                    SourcePos(manifestFile, tree.getLineNumber()).error(
                            "ERROR: failed to get XML element name (bad string pool)");
                    goto bail;
                }
                String8 tag(ctag16);
                //printf("Depth %d,  %s\n", depth, tag.string());
                if (depth == 1) {
                    if (tag != "manifest") {
                        SourcePos(manifestFile, tree.getLineNumber()).error(
                                "ERROR: manifest does not start with <manifest> tag");
                        goto bail;
                    }
                    pkg = AaptXml::getAttribute(tree, NULL, "package", NULL);
                    printf("package: name='%s' ",
                            ResTable::normalizeForOutput(pkg.string()).string());
                    int32_t versionCode = AaptXml::getIntegerAttribute(tree, VERSION_CODE_ATTR,
                            &error);
                    if (error != "") {
                        SourcePos(manifestFile, tree.getLineNumber()).error(
                                "ERROR getting 'android:versionCode' attribute: %s",
                                error.string());
                        goto bail;
                    }
                    if (versionCode > 0) {
                        printf("versionCode='%d' ", versionCode);
                    } else {
                        printf("versionCode='' ");
                    }
                    String8 versionName = AaptXml::getResolvedAttribute(res, tree,
                            VERSION_NAME_ATTR, &error);
                    if (error != "") {
                        SourcePos(manifestFile, tree.getLineNumber()).error(
                                "ERROR getting 'android:versionName' attribute: %s",
                                error.string());
                        goto bail;
                    }
                    printf("versionName='%s'",
                            ResTable::normalizeForOutput(versionName.string()).string());

                    String8 splitName = AaptXml::getAttribute(tree, NULL, "split");
                    if (!splitName.isEmpty()) {
                        printf(" split='%s'", ResTable::normalizeForOutput(
                                    splitName.string()).string());
                    }

                    String8 platformVersionName = AaptXml::getAttribute(tree, NULL,
                            "platformBuildVersionName");
                    printf(" platformBuildVersionName='%s'", platformVersionName.string());
                    printf("\n");

                    int32_t installLocation = AaptXml::getResolvedIntegerAttribute(res, tree,
                            INSTALL_LOCATION_ATTR, &error);
                    if (error != "") {
                        SourcePos(manifestFile, tree.getLineNumber()).error(
                                "ERROR getting 'android:installLocation' attribute: %s",
                                error.string());
                        goto bail;
                    }

                    if (installLocation >= 0) {
                        printf("install-location:'");
                        switch (installLocation) {
                            case 0:
                                printf("auto");
                                break;
                            case 1:
                                printf("internalOnly");
                                break;
                            case 2:
                                printf("preferExternal");
                                break;
                            default:
                                fprintf(stderr, "Invalid installLocation %d\n", installLocation);
                                goto bail;
                        }
                        printf("'\n");
                    }
                } else if (depth == 2) {
                    withinApplication = false;
                    if (tag == "application") {
                        withinApplication = true;

                        String8 label;
                        const size_t NL = locales.size();
                        for (size_t i=0; i<NL; i++) {
                            const char* localeStr =  locales[i].string();
                            assets.setConfiguration(config, localeStr != NULL ? localeStr : "");
                            String8 llabel = AaptXml::getResolvedAttribute(res, tree, LABEL_ATTR,
                                    &error);
                            if (llabel != "") {
                                if (localeStr == NULL || strlen(localeStr) == 0) {
                                    label = llabel;
                                    printf("application-label:'%s'\n",
                                            ResTable::normalizeForOutput(llabel.string()).string());
                                } else {
                                    if (label == "") {
                                        label = llabel;
                                    }
                                    printf("application-label-%s:'%s'\n", localeStr,
                                           ResTable::normalizeForOutput(llabel.string()).string());
                                }
                            }
                        }

                        ResTable_config tmpConfig = config;
                        const size_t ND = densities.size();
                        for (size_t i=0; i<ND; i++) {
                            tmpConfig.density = densities[i];
                            assets.setConfiguration(tmpConfig);
                            String8 icon = AaptXml::getResolvedAttribute(res, tree, ICON_ATTR,
                                    &error);
                            if (icon != "") {
                                printf("application-icon-%d:'%s'\n", densities[i],
                                        ResTable::normalizeForOutput(icon.string()).string());
                            }
                        }
                        assets.setConfiguration(config);

                        String8 icon = AaptXml::getResolvedAttribute(res, tree, ICON_ATTR, &error);
                        if (error != "") {
                            SourcePos(manifestFile, tree.getLineNumber()).error(
                                    "ERROR getting 'android:icon' attribute: %s", error.string());
                            goto bail;
                        }
                        int32_t testOnly = AaptXml::getIntegerAttribute(tree, TEST_ONLY_ATTR, 0,
                                &error);
                        if (error != "") {
                            SourcePos(manifestFile, tree.getLineNumber()).error(
                                    "ERROR getting 'android:testOnly' attribute: %s",
                                    error.string());
                            goto bail;
                        }

                        String8 banner = AaptXml::getResolvedAttribute(res, tree, BANNER_ATTR,
                                                                       &error);
                        if (error != "") {
                            SourcePos(manifestFile, tree.getLineNumber()).error(
                                    "ERROR getting 'android:banner' attribute: %s", error.string());
                            goto bail;
                        }
                        printf("application: label='%s' ",
                                ResTable::normalizeForOutput(label.string()).string());
                        printf("icon='%s'", ResTable::normalizeForOutput(icon.string()).string());
                        if (banner != "") {
                            printf(" banner='%s'",
                                   ResTable::normalizeForOutput(banner.string()).string());
                        }
                        printf("\n");
                        if (testOnly != 0) {
                            printf("testOnly='%d'\n", testOnly);
                        }

                        int32_t isGame = AaptXml::getResolvedIntegerAttribute(res, tree,
                                ISGAME_ATTR, 0, &error);
                        if (error != "") {
                            SourcePos(manifestFile, tree.getLineNumber()).error(
                                    "ERROR getting 'android:isGame' attribute: %s", error.string());
                            goto bail;
                        }
                        if (isGame != 0) {
                            printf("application-isGame\n");
                        }

                        int32_t debuggable = AaptXml::getResolvedIntegerAttribute(res, tree,
                                DEBUGGABLE_ATTR, 0, &error);
                        if (error != "") {
                            SourcePos(manifestFile, tree.getLineNumber()).error(
                                    "ERROR getting 'android:debuggable' attribute: %s",
                                    error.string());
                            goto bail;
                        }
                        if (debuggable != 0) {
                            printf("application-debuggable\n");
                        }

                        // We must search by name because the multiArch flag hasn't been API
                        // frozen yet.
                        int32_t multiArchIndex = tree.indexOfAttribute(RESOURCES_ANDROID_NAMESPACE,
                                "multiArch");
                        if (multiArchIndex >= 0) {
                            Res_value value;
                            if (tree.getAttributeValue(multiArchIndex, &value) != NO_ERROR) {
                                if (value.dataType >= Res_value::TYPE_FIRST_INT &&
                                        value.dataType <= Res_value::TYPE_LAST_INT) {
                                    hasMultiArch = value.data;
                                }
                            }
                        }
                    } else if (tag == "uses-sdk") {
                        int32_t code = AaptXml::getIntegerAttribute(tree, MIN_SDK_VERSION_ATTR,
                                                                    &error);
                        if (error != "") {
                            error = "";
                            String8 name = AaptXml::getResolvedAttribute(res, tree,
                                    MIN_SDK_VERSION_ATTR, &error);
                            if (error != "") {
                                SourcePos(manifestFile, tree.getLineNumber()).error(
                                        "ERROR getting 'android:minSdkVersion' attribute: %s",
                                        error.string());
                                goto bail;
                            }
                            if (name == "Donut") targetSdk = 4;
                            printf("sdkVersion:'%s'\n",
                                    ResTable::normalizeForOutput(name.string()).string());
                        } else if (code != -1) {
                            targetSdk = code;
                            printf("sdkVersion:'%d'\n", code);
                        }
                        code = AaptXml::getIntegerAttribute(tree, MAX_SDK_VERSION_ATTR);
                        if (code != -1) {
                            printf("maxSdkVersion:'%d'\n", code);
                        }
                        code = AaptXml::getIntegerAttribute(tree, TARGET_SDK_VERSION_ATTR, &error);
                        if (error != "") {
                            error = "";
                            String8 name = AaptXml::getResolvedAttribute(res, tree,
                                    TARGET_SDK_VERSION_ATTR, &error);
                            if (error != "") {
                                SourcePos(manifestFile, tree.getLineNumber()).error(
                                        "ERROR getting 'android:targetSdkVersion' attribute: %s",
                                        error.string());
                                goto bail;
                            }
                            if (name == "Donut" && targetSdk < 4) targetSdk = 4;
                            printf("targetSdkVersion:'%s'\n",
                                    ResTable::normalizeForOutput(name.string()).string());
                        } else if (code != -1) {
                            if (targetSdk < code) {
                                targetSdk = code;
                            }
                            printf("targetSdkVersion:'%d'\n", code);
                        }
                    } else if (tag == "uses-configuration") {
                        int32_t reqTouchScreen = AaptXml::getIntegerAttribute(tree,
                                REQ_TOUCH_SCREEN_ATTR, 0);
                        int32_t reqKeyboardType = AaptXml::getIntegerAttribute(tree,
                                REQ_KEYBOARD_TYPE_ATTR, 0);
                        int32_t reqHardKeyboard = AaptXml::getIntegerAttribute(tree,
                                REQ_HARD_KEYBOARD_ATTR, 0);
                        int32_t reqNavigation = AaptXml::getIntegerAttribute(tree,
                                REQ_NAVIGATION_ATTR, 0);
                        int32_t reqFiveWayNav = AaptXml::getIntegerAttribute(tree,
                                REQ_FIVE_WAY_NAV_ATTR, 0);
                        printf("uses-configuration:");
                        if (reqTouchScreen != 0) {
                            printf(" reqTouchScreen='%d'", reqTouchScreen);
                        }
                        if (reqKeyboardType != 0) {
                            printf(" reqKeyboardType='%d'", reqKeyboardType);
                        }
                        if (reqHardKeyboard != 0) {
                            printf(" reqHardKeyboard='%d'", reqHardKeyboard);
                        }
                        if (reqNavigation != 0) {
                            printf(" reqNavigation='%d'", reqNavigation);
                        }
                        if (reqFiveWayNav != 0) {
                            printf(" reqFiveWayNav='%d'", reqFiveWayNav);
                        }
                        printf("\n");
                    } else if (tag == "supports-input") {
                        withinSupportsInput = true;
                    } else if (tag == "supports-screens") {
                        smallScreen = AaptXml::getIntegerAttribute(tree,
                                SMALL_SCREEN_ATTR, 1);
                        normalScreen = AaptXml::getIntegerAttribute(tree,
                                NORMAL_SCREEN_ATTR, 1);
                        largeScreen = AaptXml::getIntegerAttribute(tree,
                                LARGE_SCREEN_ATTR, 1);
                        xlargeScreen = AaptXml::getIntegerAttribute(tree,
                                XLARGE_SCREEN_ATTR, 1);
                        anyDensity = AaptXml::getIntegerAttribute(tree,
                                ANY_DENSITY_ATTR, 1);
                        requiresSmallestWidthDp = AaptXml::getIntegerAttribute(tree,
                                REQUIRES_SMALLEST_WIDTH_DP_ATTR, 0);
                        compatibleWidthLimitDp = AaptXml::getIntegerAttribute(tree,
                                COMPATIBLE_WIDTH_LIMIT_DP_ATTR, 0);
                        largestWidthLimitDp = AaptXml::getIntegerAttribute(tree,
                                LARGEST_WIDTH_LIMIT_DP_ATTR, 0);
                    } else if (tag == "feature-group") {
                        withinFeatureGroup = true;
                        FeatureGroup group;
                        group.label = AaptXml::getResolvedAttribute(res, tree, LABEL_ATTR, &error);
                        if (error != "") {
                            SourcePos(manifestFile, tree.getLineNumber()).error(
                                    "ERROR getting 'android:label' attribute: %s", error.string());
                            goto bail;
                        }
                        featureGroups.add(group);

                    } else if (tag == "uses-feature") {
                        String8 name = AaptXml::getAttribute(tree, NAME_ATTR, &error);
                        if (name != "" && error == "") {
                            const char* androidSchema =
                                    "http://schemas.android.com/apk/res/android";

                            int32_t req = AaptXml::getIntegerAttribute(tree, REQUIRED_ATTR, 1,
                                                                       &error);
                            if (error != "") {
                                SourcePos(manifestFile, tree.getLineNumber()).error(
                                        "failed to read attribute 'android:required': %s",
                                        error.string());
                                goto bail;
                            }

                            int32_t version = AaptXml::getIntegerAttribute(tree, androidSchema,
                                                                           "version", 0, &error);
                            if (error != "") {
                                SourcePos(manifestFile, tree.getLineNumber()).error(
                                        "failed to read attribute 'android:version': %s",
                                        error.string());
                                goto bail;
                            }

                            commonFeatures.features.add(name, Feature(req != 0, version));
                            if (req) {
                                addParentFeatures(&commonFeatures, name);
                            }
                        } else {
                            int vers = AaptXml::getIntegerAttribute(tree,
                                    GL_ES_VERSION_ATTR, &error);
                            if (error == "") {
                                if (vers > commonFeatures.openGLESVersion) {
                                    commonFeatures.openGLESVersion = vers;
                                }
                            }
                        }
                    } else if (tag == "uses-permission") {
                        String8 name = AaptXml::getAttribute(tree, NAME_ATTR, &error);
                        if (error != "") {
                            SourcePos(manifestFile, tree.getLineNumber()).error(
                                    "ERROR getting 'android:name' attribute: %s", error.string());
                            goto bail;
                        }

                        if (name == "") {
                            SourcePos(manifestFile, tree.getLineNumber()).error(
                                    "ERROR: missing 'android:name' for uses-permission");
                            goto bail;
                        }

                        addImpliedFeaturesForPermission(targetSdk, name, &impliedFeatures, false);

                        const int32_t maxSdkVersion =
                                AaptXml::getIntegerAttribute(tree, MAX_SDK_VERSION_ATTR, -1);
                        const String8 requiredFeature = AaptXml::getAttribute(tree,
                                REQUIRED_FEATURE_ATTR, &error);
                        const String8 requiredNotFeature = AaptXml::getAttribute(tree,
                                REQUIRED_NOT_FEATURE_ATTR, &error);

                        if (name == "android.permission.WRITE_EXTERNAL_STORAGE") {
                            hasWriteExternalStoragePermission = true;
                            writeExternalStoragePermissionMaxSdkVersion = maxSdkVersion;
                        } else if (name == "android.permission.READ_EXTERNAL_STORAGE") {
                            hasReadExternalStoragePermission = true;
                        } else if (name == "android.permission.READ_PHONE_STATE") {
                            hasReadPhoneStatePermission = true;
                        } else if (name == "android.permission.READ_CONTACTS") {
                            hasReadContactsPermission = true;
                        } else if (name == "android.permission.WRITE_CONTACTS") {
                            hasWriteContactsPermission = true;
                        } else if (name == "android.permission.READ_CALL_LOG") {
                            hasReadCallLogPermission = true;
                        } else if (name == "android.permission.WRITE_CALL_LOG") {
                            hasWriteCallLogPermission = true;
                        }

                        printUsesPermission(name,
                                AaptXml::getIntegerAttribute(tree, REQUIRED_ATTR, 1) == 0,
                                maxSdkVersion, requiredFeature, requiredNotFeature);

                    } else if (tag == "uses-permission-sdk-23" || tag == "uses-permission-sdk-m") {
                        String8 name = AaptXml::getAttribute(tree, NAME_ATTR, &error);
                        if (error != "") {
                            SourcePos(manifestFile, tree.getLineNumber()).error(
                                    "ERROR getting 'android:name' attribute: %s", error.string());
                            goto bail;
                        }

                        if (name == "") {
                            SourcePos(manifestFile, tree.getLineNumber()).error(
                                    "ERROR: missing 'android:name' for uses-permission-sdk-23");
                            goto bail;
                        }

                        addImpliedFeaturesForPermission(targetSdk, name, &impliedFeatures, true);

                        printUsesPermissionSdk23(
                                name, AaptXml::getIntegerAttribute(tree, MAX_SDK_VERSION_ATTR));

                    } else if (tag == "uses-package") {
                        String8 name = AaptXml::getAttribute(tree, NAME_ATTR, &error);
                        if (name != "" && error == "") {
                            printf("uses-package:'%s'\n",
                                    ResTable::normalizeForOutput(name.string()).string());
                        } else {
                            SourcePos(manifestFile, tree.getLineNumber()).error(
                                    "ERROR getting 'android:name' attribute: %s", error.string());
                            goto bail;
                        }
                    } else if (tag == "original-package") {
                        String8 name = AaptXml::getAttribute(tree, NAME_ATTR, &error);
                        if (name != "" && error == "") {
                            printf("original-package:'%s'\n",
                                    ResTable::normalizeForOutput(name.string()).string());
                        } else {
                            SourcePos(manifestFile, tree.getLineNumber()).error(
                                    "ERROR getting 'android:name' attribute: %s", error.string());
                            goto bail;
                        }
                    } else if (tag == "supports-gl-texture") {
                        String8 name = AaptXml::getAttribute(tree, NAME_ATTR, &error);
                        if (name != "" && error == "") {
                            printf("supports-gl-texture:'%s'\n",
                                    ResTable::normalizeForOutput(name.string()).string());
                        } else {
                            SourcePos(manifestFile, tree.getLineNumber()).error(
                                    "ERROR getting 'android:name' attribute: %s", error.string());
                            goto bail;
                        }
                    } else if (tag == "compatible-screens") {
                        printCompatibleScreens(tree, &error);
                        if (error != "") {
                            SourcePos(manifestFile, tree.getLineNumber()).error(
                                    "ERROR getting compatible screens: %s", error.string());
                            goto bail;
                        }
                        depth--;
                    } else if (tag == "package-verifier") {
                        String8 name = AaptXml::getAttribute(tree, NAME_ATTR, &error);
                        if (name != "" && error == "") {
                            String8 publicKey = AaptXml::getAttribute(tree, PUBLIC_KEY_ATTR,
                                                                      &error);
                            if (publicKey != "" && error == "") {
                                printf("package-verifier: name='%s' publicKey='%s'\n",
                                        ResTable::normalizeForOutput(name.string()).string(),
                                        ResTable::normalizeForOutput(publicKey.string()).string());
                            }
                        }
                    }
                } else if (depth == 3) {
                    withinActivity = false;
                    withinReceiver = false;
                    withinService = false;
                    withinProvider = false;
                    hasIntentFilter = false;
                    hasMetaHostPaymentCategory = false;
                    hasMetaOffHostPaymentCategory = false;
                    hasBindDeviceAdminPermission = false;
                    hasBindInputMethodPermission = false;
                    hasBindAccessibilityServicePermission = false;
                    hasBindPrintServicePermission = false;
                    hasBindNfcServicePermission = false;
                    hasRequiredSafAttributes = false;
                    hasBindNotificationListenerServicePermission = false;
                    hasBindDreamServicePermission = false;
                    if (withinApplication) {
                        if(tag == "activity") {
                            withinActivity = true;
                            activityName = AaptXml::getAttribute(tree, NAME_ATTR, &error);
                            if (error != "") {
                                SourcePos(manifestFile, tree.getLineNumber()).error(
                                        "ERROR getting 'android:name' attribute: %s",
                                        error.string());
                                goto bail;
                            }

                            activityLabel = AaptXml::getResolvedAttribute(res, tree, LABEL_ATTR,
                                    &error);
                            if (error != "") {
                                SourcePos(manifestFile, tree.getLineNumber()).error(
                                        "ERROR getting 'android:label' attribute: %s",
                                        error.string());
                                goto bail;
                            }

                            activityIcon = AaptXml::getResolvedAttribute(res, tree, ICON_ATTR,
                                    &error);
                            if (error != "") {
                                SourcePos(manifestFile, tree.getLineNumber()).error(
                                        "ERROR getting 'android:icon' attribute: %s",
                                        error.string());
                                goto bail;
                            }

                            activityBanner = AaptXml::getResolvedAttribute(res, tree, BANNER_ATTR,
                                    &error);
                            if (error != "") {
                                SourcePos(manifestFile, tree.getLineNumber()).error(
                                        "ERROR getting 'android:banner' attribute: %s",
                                        error.string());
                                goto bail;
                            }

                            int32_t orien = AaptXml::getResolvedIntegerAttribute(res, tree,
                                    SCREEN_ORIENTATION_ATTR, &error);
                            if (error == "") {
                                if (orien == 0 || orien == 6 || orien == 8) {
                                    // Requests landscape, sensorLandscape, or reverseLandscape.
                                    addImpliedFeature(
                                            &impliedFeatures, "android.hardware.screen.landscape",
                                            String8("one or more activities have specified a "
                                                    "landscape orientation"),
                                            false);
                                } else if (orien == 1 || orien == 7 || orien == 9) {
                                    // Requests portrait, sensorPortrait, or reversePortrait.
                                    addImpliedFeature(
                                            &impliedFeatures, "android.hardware.screen.portrait",
                                            String8("one or more activities have specified a "
                                                    "portrait orientation"),
                                            false);
                                }
                            }
                        } else if (tag == "uses-library") {
                            String8 libraryName = AaptXml::getAttribute(tree, NAME_ATTR, &error);
                            if (error != "") {
                                SourcePos(manifestFile, tree.getLineNumber()).error(
                                        "ERROR getting 'android:name' attribute for uses-library"
                                        " %s", error.string());
                                goto bail;
                            }
                            int req = AaptXml::getIntegerAttribute(tree,
                                    REQUIRED_ATTR, 1);
                            printf("uses-library%s:'%s'\n",
                                    req ? "" : "-not-required", ResTable::normalizeForOutput(
                                            libraryName.string()).string());
                        } else if (tag == "receiver") {
                            withinReceiver = true;
                            receiverName = AaptXml::getAttribute(tree, NAME_ATTR, &error);

                            if (error != "") {
                                SourcePos(manifestFile, tree.getLineNumber()).error(
                                        "ERROR getting 'android:name' attribute for receiver:"
                                        " %s", error.string());
                                goto bail;
                            }

                            String8 permission = AaptXml::getAttribute(tree, PERMISSION_ATTR,
                                    &error);
                            if (error == "") {
                                if (permission == "android.permission.BIND_DEVICE_ADMIN") {
                                    hasBindDeviceAdminPermission = true;
                                }
                            } else {
                                SourcePos(manifestFile, tree.getLineNumber()).error(
                                        "ERROR getting 'android:permission' attribute for"
                                        " receiver '%s': %s",
                                        receiverName.string(), error.string());
                            }
                        } else if (tag == "service") {
                            withinService = true;
                            serviceName = AaptXml::getAttribute(tree, NAME_ATTR, &error);

                            if (error != "") {
                                SourcePos(manifestFile, tree.getLineNumber()).error(
                                        "ERROR getting 'android:name' attribute for "
                                        "service:%s", error.string());
                                goto bail;
                            }

                            String8 permission = AaptXml::getAttribute(tree, PERMISSION_ATTR,
                                    &error);
                            if (error == "") {
                                if (permission == "android.permission.BIND_INPUT_METHOD") {
                                    hasBindInputMethodPermission = true;
                                } else if (permission ==
                                        "android.permission.BIND_ACCESSIBILITY_SERVICE") {
                                    hasBindAccessibilityServicePermission = true;
                                } else if (permission ==
                                        "android.permission.BIND_PRINT_SERVICE") {
                                    hasBindPrintServicePermission = true;
                                } else if (permission ==
                                        "android.permission.BIND_NFC_SERVICE") {
                                    hasBindNfcServicePermission = true;
                                } else if (permission ==
                                        "android.permission.BIND_NOTIFICATION_LISTENER_SERVICE") {
                                    hasBindNotificationListenerServicePermission = true;
                                } else if (permission == "android.permission.BIND_DREAM_SERVICE") {
                                    hasBindDreamServicePermission = true;
                                }
                            } else {
                                SourcePos(manifestFile, tree.getLineNumber()).error(
                                        "ERROR getting 'android:permission' attribute for "
                                        "service '%s': %s", serviceName.string(), error.string());
                            }
                        } else if (tag == "provider") {
                            withinProvider = true;

                            bool exported = AaptXml::getResolvedIntegerAttribute(res, tree,
                                    EXPORTED_ATTR, &error);
                            if (error != "") {
                                SourcePos(manifestFile, tree.getLineNumber()).error(
                                        "ERROR getting 'android:exported' attribute for provider:"
                                        " %s", error.string());
                                goto bail;
                            }

                            bool grantUriPermissions = AaptXml::getResolvedIntegerAttribute(
                                    res, tree, GRANT_URI_PERMISSIONS_ATTR, &error);
                            if (error != "") {
                                SourcePos(manifestFile, tree.getLineNumber()).error(
                                        "ERROR getting 'android:grantUriPermissions' attribute for "
                                        "provider: %s", error.string());
                                goto bail;
                            }

                            String8 permission = AaptXml::getResolvedAttribute(res, tree,
                                    PERMISSION_ATTR, &error);
                            if (error != "") {
                                SourcePos(manifestFile, tree.getLineNumber()).error(
                                        "ERROR getting 'android:permission' attribute for "
                                        "provider: %s", error.string());
                                goto bail;
                            }

                            hasRequiredSafAttributes |= exported && grantUriPermissions &&
                                permission == "android.permission.MANAGE_DOCUMENTS";

                        } else if (bundle->getIncludeMetaData() && tag == "meta-data") {
                            String8 metaDataName = AaptXml::getResolvedAttribute(res, tree,
                                    NAME_ATTR, &error);
                            if (error != "") {
                                SourcePos(manifestFile, tree.getLineNumber()).error(
                                        "ERROR getting 'android:name' attribute for "
                                        "meta-data: %s", error.string());
                                goto bail;
                            }
                            printf("meta-data: name='%s' ",
                                    ResTable::normalizeForOutput(metaDataName.string()).string());
                            printResolvedResourceAttribute(res, tree, VALUE_ATTR, String8("value"),
                                    &error);
                            if (error != "") {
                                // Try looking for a RESOURCE_ATTR
                                error = "";
                                printResolvedResourceAttribute(res, tree, RESOURCE_ATTR,
                                        String8("resource"), &error);
                                if (error != "") {
                                    SourcePos(manifestFile, tree.getLineNumber()).error(
                                            "ERROR getting 'android:value' or "
                                            "'android:resource' attribute for "
                                            "meta-data: %s", error.string());
                                    goto bail;
                                }
                            }
                            printf("\n");
                        } else if (withinSupportsInput && tag == "input-type") {
                            String8 name = AaptXml::getAttribute(tree, NAME_ATTR, &error);
                            if (name != "" && error == "") {
                                supportedInput.add(name);
                            } else {
                                SourcePos(manifestFile, tree.getLineNumber()).error(
                                        "ERROR getting 'android:name' attribute: %s",
                                        error.string());
                                goto bail;
                            }
                        }
                    } else if (withinFeatureGroup && tag == "uses-feature") {
                        const String8 androidSchema("http://schemas.android.com/apk/res/android");
                        FeatureGroup& top = featureGroups.editTop();

                        String8 name = AaptXml::getResolvedAttribute(res, tree, NAME_ATTR, &error);
                        if (name != "" && error == "") {
                            Feature feature(true);

                            int32_t featureVers = AaptXml::getIntegerAttribute(
                                    tree, androidSchema.string(), "version", 0, &error);
                            if (error == "") {
                                feature.version = featureVers;
                            } else {
                                SourcePos(manifestFile, tree.getLineNumber()).error(
                                        "failed to read attribute 'android:version': %s",
                                        error.string());
                                goto bail;
                            }

                            top.features.add(name, feature);
                            addParentFeatures(&top, name);

                        } else {
                            int vers = AaptXml::getIntegerAttribute(tree, GL_ES_VERSION_ATTR,
                                    &error);
                            if (error == "") {
                                if (vers > top.openGLESVersion) {
                                    top.openGLESVersion = vers;
                                }
                            }
                        }
                    }
                } else if (depth == 4) {
                    if (tag == "intent-filter") {
                        hasIntentFilter = true;
                        withinIntentFilter = true;
                        actMainActivity = false;
                        actWidgetReceivers = false;
                        actImeService = false;
                        actWallpaperService = false;
                        actAccessibilityService = false;
                        actPrintService = false;
                        actDeviceAdminEnabled = false;
                        actHostApduService = false;
                        actOffHostApduService = false;
                        actDocumentsProvider = false;
                        actNotificationListenerService = false;
                        actDreamService = false;
                        actCamera = false;
                        actCameraSecure = false;
                        catLauncher = false;
                    } else if (withinService && tag == "meta-data") {
                        String8 name = AaptXml::getAttribute(tree, NAME_ATTR, &error);
                        if (error != "") {
                            SourcePos(manifestFile, tree.getLineNumber()).error(
                                    "ERROR getting 'android:name' attribute for "
                                    "meta-data tag in service '%s': %s", serviceName.string(),
                                    error.string());
                            goto bail;
                        }

                        if (name == "android.nfc.cardemulation.host_apdu_service" ||
                                name == "android.nfc.cardemulation.off_host_apdu_service") {
                            bool offHost = true;
                            if (name == "android.nfc.cardemulation.host_apdu_service") {
                                offHost = false;
                            }

                            String8 xmlPath = AaptXml::getResolvedAttribute(res, tree,
                                    RESOURCE_ATTR, &error);
                            if (error != "") {
                                SourcePos(manifestFile, tree.getLineNumber()).error(
                                        "ERROR getting 'android:resource' attribute for "
                                        "meta-data tag in service '%s': %s",
                                        serviceName.string(), error.string());
                                goto bail;
                            }

                            Vector<String8> categories = getNfcAidCategories(assets, xmlPath,
                                    offHost, &error);
                            if (error != "") {
                                SourcePos(manifestFile, tree.getLineNumber()).error(
                                        "ERROR getting AID category for service '%s'",
                                        serviceName.string());
                                goto bail;
                            }

                            const size_t catLen = categories.size();
                            for (size_t i = 0; i < catLen; i++) {
                                bool paymentCategory = (categories[i] == "payment");
                                if (offHost) {
                                    hasMetaOffHostPaymentCategory |= paymentCategory;
                                } else {
                                    hasMetaHostPaymentCategory |= paymentCategory;
                                }
                            }
                        }
                    }
                } else if ((depth == 5) && withinIntentFilter) {
                    String8 action;
                    if (tag == "action") {
                        action = AaptXml::getAttribute(tree, NAME_ATTR, &error);
                        if (error != "") {
                            SourcePos(manifestFile, tree.getLineNumber()).error(
                                    "ERROR getting 'android:name' attribute: %s", error.string());
                            goto bail;
                        }

                        if (withinActivity) {
                            if (action == "android.intent.action.MAIN") {
                                isMainActivity = true;
                                actMainActivity = true;
                            } else if (action == "android.media.action.STILL_IMAGE_CAMERA" ||
                                    action == "android.media.action.VIDEO_CAMERA") {
                                actCamera = true;
                            } else if (action == "android.media.action.STILL_IMAGE_CAMERA_SECURE") {
                                actCameraSecure = true;
                            }
                        } else if (withinReceiver) {
                            if (action == "android.appwidget.action.APPWIDGET_UPDATE") {
                                actWidgetReceivers = true;
                            } else if (action == "android.app.action.DEVICE_ADMIN_ENABLED") {
                                actDeviceAdminEnabled = true;
                            }
                        } else if (withinService) {
                            if (action == "android.view.InputMethod") {
                                actImeService = true;
                            } else if (action == "android.service.wallpaper.WallpaperService") {
                                actWallpaperService = true;
                            } else if (action ==
                                    "android.accessibilityservice.AccessibilityService") {
                                actAccessibilityService = true;
                            } else if (action =="android.printservice.PrintService") {
                                actPrintService = true;
                            } else if (action ==
                                    "android.nfc.cardemulation.action.HOST_APDU_SERVICE") {
                                actHostApduService = true;
                            } else if (action ==
                                    "android.nfc.cardemulation.action.OFF_HOST_APDU_SERVICE") {
                                actOffHostApduService = true;
                            } else if (action ==
                                    "android.service.notification.NotificationListenerService") {
                                actNotificationListenerService = true;
                            } else if (action == "android.service.dreams.DreamService") {
                                actDreamService = true;
                            }
                        } else if (withinProvider) {
                            if (action == "android.content.action.DOCUMENTS_PROVIDER") {
                                actDocumentsProvider = true;
                            }
                        }
                        if (action == "android.intent.action.SEARCH") {
                            isSearchable = true;
                        }
                    }

                    if (tag == "category") {
                        String8 category = AaptXml::getAttribute(tree, NAME_ATTR, &error);
                        if (error != "") {
                            SourcePos(manifestFile, tree.getLineNumber()).error(
                                    "ERROR getting 'name' attribute: %s", error.string());
                            goto bail;
                        }
                        if (withinActivity) {
                            if (category == "android.intent.category.LAUNCHER") {
                                isLauncherActivity = true;
                            } else if (category == "android.intent.category.LEANBACK_LAUNCHER") {
                                isLeanbackLauncherActivity = true;
                            } else if (category == "android.intent.category.HOME") {
                                catLauncher = true;
                            }
                        }
                    }
                }
            }

            // Pre-1.6 implicitly granted permission compatibility logic
            if (targetSdk < 4) {
                if (!hasWriteExternalStoragePermission) {
                    printUsesPermission(String8("android.permission.WRITE_EXTERNAL_STORAGE"));
                    printUsesImpliedPermission(String8("android.permission.WRITE_EXTERNAL_STORAGE"),
                            String8("targetSdkVersion < 4"));
                    hasWriteExternalStoragePermission = true;
                }
                if (!hasReadPhoneStatePermission) {
                    printUsesPermission(String8("android.permission.READ_PHONE_STATE"));
                    printUsesImpliedPermission(String8("android.permission.READ_PHONE_STATE"),
                            String8("targetSdkVersion < 4"));
                }
            }

            // If the application has requested WRITE_EXTERNAL_STORAGE, we will
            // force them to always take READ_EXTERNAL_STORAGE as well.  We always
            // do this (regardless of target API version) because we can't have
            // an app with write permission but not read permission.
            if (!hasReadExternalStoragePermission && hasWriteExternalStoragePermission) {
                printUsesPermission(String8("android.permission.READ_EXTERNAL_STORAGE"),
                        false /* optional */, writeExternalStoragePermissionMaxSdkVersion);
                printUsesImpliedPermission(String8("android.permission.READ_EXTERNAL_STORAGE"),
                        String8("requested WRITE_EXTERNAL_STORAGE"),
                        writeExternalStoragePermissionMaxSdkVersion);
            }

            // Pre-JellyBean call log permission compatibility.
            if (targetSdk < 16) {
                if (!hasReadCallLogPermission && hasReadContactsPermission) {
                    printUsesPermission(String8("android.permission.READ_CALL_LOG"));
                    printUsesImpliedPermission(String8("android.permission.READ_CALL_LOG"),
                            String8("targetSdkVersion < 16 and requested READ_CONTACTS"));
                }
                if (!hasWriteCallLogPermission && hasWriteContactsPermission) {
                    printUsesPermission(String8("android.permission.WRITE_CALL_LOG"));
                    printUsesImpliedPermission(String8("android.permission.WRITE_CALL_LOG"),
                            String8("targetSdkVersion < 16 and requested WRITE_CONTACTS"));
                }
            }

            // If the app hasn't declared the touchscreen as a feature requirement (either
            // directly or implied, required or not), then the faketouch feature is implied.
            if (!hasFeature("android.hardware.touchscreen", commonFeatures, impliedFeatures)) {
                addImpliedFeature(&impliedFeatures, "android.hardware.faketouch",
                                  String8("default feature for all apps"), false);
            }

            const size_t numFeatureGroups = featureGroups.size();
            if (numFeatureGroups == 0) {
                // If no <feature-group> tags were defined, apply auto-implied features.
                printDefaultFeatureGroup(commonFeatures, impliedFeatures);

            } else {
                // <feature-group> tags are defined, so we ignore implied features and
                for (size_t i = 0; i < numFeatureGroups; i++) {
                    FeatureGroup& grp = featureGroups.editItemAt(i);

                    if (commonFeatures.openGLESVersion > grp.openGLESVersion) {
                        grp.openGLESVersion = commonFeatures.openGLESVersion;
                    }

                    // Merge the features defined in the top level (not inside a <feature-group>)
                    // with this feature group.
                    const size_t numCommonFeatures = commonFeatures.features.size();
                    for (size_t j = 0; j < numCommonFeatures; j++) {
                        if (grp.features.indexOfKey(commonFeatures.features.keyAt(j)) < 0) {
                            grp.features.add(commonFeatures.features.keyAt(j),
                                    commonFeatures.features[j]);
                        }
                    }

                    if (!grp.features.isEmpty()) {
                        printFeatureGroup(grp);
                    }
                }
            }


            if (hasWidgetReceivers) {
                printComponentPresence("app-widget");
            }
            if (hasDeviceAdminReceiver) {
                printComponentPresence("device-admin");
            }
            if (hasImeService) {
                printComponentPresence("ime");
            }
            if (hasWallpaperService) {
                printComponentPresence("wallpaper");
            }
            if (hasAccessibilityService) {
                printComponentPresence("accessibility");
            }
            if (hasPrintService) {
                printComponentPresence("print-service");
            }
            if (hasPaymentService) {
                printComponentPresence("payment");
            }
            if (isSearchable) {
                printComponentPresence("search");
            }
            if (hasDocumentsProvider) {
                printComponentPresence("document-provider");
            }
            if (hasLauncher) {
                printComponentPresence("launcher");
            }
            if (hasNotificationListenerService) {
                printComponentPresence("notification-listener");
            }
            if (hasDreamService) {
                printComponentPresence("dream");
            }
            if (hasCameraActivity) {
                printComponentPresence("camera");
            }
            if (hasCameraSecureActivity) {
                printComponentPresence("camera-secure");
            }

            if (hasMainActivity) {
                printf("main\n");
            }
            if (hasOtherActivities) {
                printf("other-activities\n");
            }
             if (hasOtherReceivers) {
                printf("other-receivers\n");
            }
            if (hasOtherServices) {
                printf("other-services\n");
            }

            // For modern apps, if screen size buckets haven't been specified
            // but the new width ranges have, then infer the buckets from them.
            if (smallScreen > 0 && normalScreen > 0 && largeScreen > 0 && xlargeScreen > 0
                    && requiresSmallestWidthDp > 0) {
                int compatWidth = compatibleWidthLimitDp;
                if (compatWidth <= 0) {
                    compatWidth = requiresSmallestWidthDp;
                }
                if (requiresSmallestWidthDp <= 240 && compatWidth >= 240) {
                    smallScreen = -1;
                } else {
                    smallScreen = 0;
                }
                if (requiresSmallestWidthDp <= 320 && compatWidth >= 320) {
                    normalScreen = -1;
                } else {
                    normalScreen = 0;
                }
                if (requiresSmallestWidthDp <= 480 && compatWidth >= 480) {
                    largeScreen = -1;
                } else {
                    largeScreen = 0;
                }
                if (requiresSmallestWidthDp <= 720 && compatWidth >= 720) {
                    xlargeScreen = -1;
                } else {
                    xlargeScreen = 0;
                }
            }

            // Determine default values for any unspecified screen sizes,
            // based on the target SDK of the package.  As of 4 (donut)
            // the screen size support was introduced, so all default to
            // enabled.
            if (smallScreen > 0) {
                smallScreen = targetSdk >= 4 ? -1 : 0;
            }
            if (normalScreen > 0) {
                normalScreen = -1;
            }
            if (largeScreen > 0) {
                largeScreen = targetSdk >= 4 ? -1 : 0;
            }
            if (xlargeScreen > 0) {
                // Introduced in Gingerbread.
                xlargeScreen = targetSdk >= 9 ? -1 : 0;
            }
            if (anyDensity > 0) {
                anyDensity = (targetSdk >= 4 || requiresSmallestWidthDp > 0
                        || compatibleWidthLimitDp > 0) ? -1 : 0;
            }
            printf("supports-screens:");
            if (smallScreen != 0) {
                printf(" 'small'");
            }
            if (normalScreen != 0) {
                printf(" 'normal'");
            }
            if (largeScreen != 0) {
                printf(" 'large'");
            }
            if (xlargeScreen != 0) {
                printf(" 'xlarge'");
            }
            printf("\n");
            printf("supports-any-density: '%s'\n", anyDensity ? "true" : "false");
            if (requiresSmallestWidthDp > 0) {
                printf("requires-smallest-width:'%d'\n", requiresSmallestWidthDp);
            }
            if (compatibleWidthLimitDp > 0) {
                printf("compatible-width-limit:'%d'\n", compatibleWidthLimitDp);
            }
            if (largestWidthLimitDp > 0) {
                printf("largest-width-limit:'%d'\n", largestWidthLimitDp);
            }

            printf("locales:");
            const size_t NL = locales.size();
            for (size_t i=0; i<NL; i++) {
                const char* localeStr =  locales[i].string();
                if (localeStr == NULL || strlen(localeStr) == 0) {
                    localeStr = "--_--";
                }
                printf(" '%s'", localeStr);
            }
            printf("\n");

            printf("densities:");
            const size_t ND = densities.size();
            for (size_t i=0; i<ND; i++) {
                printf(" '%d'", densities[i]);
            }
            printf("\n");

            AssetDir* dir = assets.openNonAssetDir(assetsCookie, "lib");
            if (dir != NULL) {
                if (dir->getFileCount() > 0) {
                    SortedVector<String8> architectures;
                    for (size_t i=0; i<dir->getFileCount(); i++) {
                        architectures.add(ResTable::normalizeForOutput(
                                dir->getFileName(i).string()));
                    }

                    bool outputAltNativeCode = false;
                    // A multiArch package is one that contains 64-bit and
                    // 32-bit versions of native code and expects 3rd-party
                    // apps to load these native code libraries. Since most
                    // 64-bit systems also support 32-bit apps, the apps
                    // loading this multiArch package's code may be either
                    // 32-bit or 64-bit.
                    if (hasMultiArch) {
                        // If this is a multiArch package, report the 64-bit
                        // version only. Then as a separate entry, report the
                        // rest.
                        //
                        // If we report the 32-bit architecture, this APK will
                        // be installed on a 32-bit device, causing a large waste
                        // of bandwidth and disk space. This assumes that
                        // the developer of the multiArch package has also
                        // made a version that is 32-bit only.
                        String8 intel64("x86_64");
                        String8 arm64("arm64-v8a");
                        ssize_t index = architectures.indexOf(intel64);
                        if (index < 0) {
                            index = architectures.indexOf(arm64);
                        }

                        if (index >= 0) {
                            printf("native-code: '%s'\n", architectures[index].string());
                            architectures.removeAt(index);
                            outputAltNativeCode = true;
                        }
                    }

                    const size_t archCount = architectures.size();
                    if (archCount > 0) {
                        if (outputAltNativeCode) {
                            printf("alt-");
                        }
                        printf("native-code:");
                        for (size_t i = 0; i < archCount; i++) {
                            printf(" '%s'", architectures[i].string());
                        }
                        printf("\n");
                    }
                }
                delete dir;
            }
        } else if (strcmp("badger", option) == 0) {
            printf("%s", CONSOLE_DATA);
        } else if (strcmp("configurations", option) == 0) {
            Vector<ResTable_config> configs;
            res.getConfigurations(&configs);
            const size_t N = configs.size();
            for (size_t i=0; i<N; i++) {
                printf("%s\n", configs[i].toString().string());
            }
        } else {
            fprintf(stderr, "ERROR: unknown dump option '%s'\n", option);
            goto bail;
        }
    }

    result = NO_ERROR;

bail:
    if (SourcePos::hasErrors()) {
        SourcePos::printErrors(stderr);
    }

    if (asset) {
        delete asset;
    }
    return (result != NO_ERROR);
}

JNIEXPORT jlong JNICALL Java_com_apkscanner_core_scanner_AaptNativeScanner_nativeCreateAssetManager
  (JNIEnv *, jclass)
{
    AssetManager *assetManager = new AssetManager();
    return reinterpret_cast<jlong>(assetManager);
}

JNIEXPORT void JNICALL Java_com_apkscanner_core_scanner_AaptNativeScanner_nativeRealeaseAssetManager
  (JNIEnv *, jclass, jlong handle)
{
    if(handle == 0) return;
    AssetManager *assetManager = reinterpret_cast<AssetManager*>(handle);
    delete assetManager;
}

JNIEXPORT jint JNICALL Java_com_apkscanner_core_scanner_AaptNativeScanner_nativeGetPackageId
  (JNIEnv * env, jclass, jstring path)
{
    if(path == NULL) {
        fprintf(stderr, "ERROR: path(%p) is null\n", path);
        return JNI_FALSE;
    }

    char *filepath = jstring2cstr(env, path);
    if(filepath == NULL) {
        fprintf(stderr, "Failure: encoding path is NULL\n");
        fflush(stderr);
        return JNI_FALSE;
    }

    AssetManager tmpAssets;
    int32_t assetsCookie;
    tmpAssets.addAssetPath(String8(filepath), &assetsCookie);
    const ResTable& res = tmpAssets.getResources(false);
    jint packId = res.getPackageId(assetsCookie);
    
    free(filepath);

    return packId;
}

JNIEXPORT jboolean JNICALL Java_com_apkscanner_core_scanner_AaptNativeScanner_nativeAddPackage
  (JNIEnv * env, jclass, jlong handle, jstring path)
{
    if(handle == 0 || path == 0) {
        fprintf(stderr, "ERROR: handle(%lld) or path(%p) is null\n", static_cast<long long>(handle), path);
        return JNI_FALSE;
    }

    char *filepath = jstring2cstr(env, path);
    if(filepath == NULL) {
        fprintf(stderr, "Failure: encoding path is NULL\n");
        fflush(stderr);
        return JNI_FALSE;
    }

    AssetManager *assetManager = reinterpret_cast<AssetManager*>(handle);
    int32_t assetsCookie;
    jboolean result = JNI_TRUE;
    if (!assetManager->addAssetPath(String8(filepath), &assetsCookie)) {
        fprintf(stderr, "ERROR: dump failed because assets could not be loaded : %s\n", filepath);
        fflush(stderr);
        result = JNI_FALSE;
    }
    free(filepath);

    return result;
}

JNIEXPORT jboolean JNICALL Java_com_apkscanner_core_scanner_AaptNativeScanner_nativeAddResPackage
  (JNIEnv * env, jclass, jlong handle, jstring path)
{
    if(handle == 0 || path == 0) {
        fprintf(stderr, "ERROR: handle(%lld) or path(%p) is null\n", static_cast<long long>(handle), path);
        return JNI_FALSE;
    }

    char *filepath = jstring2cstr(env, path);
    if(filepath == NULL) {
        fprintf(stderr, "Failure: encoding path is NULL\n");
        fflush(stderr);
        return JNI_FALSE;
    }

    int32_t packId = -1;
    {
        AssetManager tmpAssets;
        int32_t assetsCookie;
        tmpAssets.addAssetPath(String8(filepath), &assetsCookie);
        const ResTable& res = tmpAssets.getResources(false);
        packId = res.getPackageId(assetsCookie);
    }
    
    AssetManager *assetManager = reinterpret_cast<AssetManager*>(handle);
    
    const ResTable& res = assetManager->getResources(false);
    if (res.getError() != NO_ERROR) {
        fprintf(stderr, "ERROR: dump failed because the resource table is invalid/corrupt.\n");
        fflush(stderr);
        return JNI_FALSE;
    }
    
    jboolean result = JNI_TRUE;
    if(res.isExistPackageId(packId)) {
        fprintf(stderr, "WARRING: Existed packageId(%d) : %s\n", packId, filepath);
        result = JNI_FALSE;
    } else {
        int32_t assetsCookie;
        if (!assetManager->addAssetPath(String8(filepath), &assetsCookie)) {
            fprintf(stderr, "ERROR: dump failed because assets could not be loaded : %s\n", filepath);
            result = JNI_FALSE;
        }
    }
    
    free(filepath);
    fflush(stderr);

    return result;
}

JNIEXPORT jstring JNICALL Java_com_apkscanner_core_scanner_AaptNativeScanner_nativeGetResourceName
  (JNIEnv *env, jclass, jlong handle, jint resID)
{
    if(handle == 0) return NULL;

    AssetManager *assetManager = reinterpret_cast<AssetManager*>(handle);
    
    const ResTable& res = assetManager->getResources(false);
    if (res.getError() != NO_ERROR) {
        fprintf(stderr, "ERROR: dump failed because the resource table is invalid/corrupt.\n");
        fflush(stderr);
        return NULL;
    }

    jstring resName = NULL;
    android::ResTable::resource_name rname;
    if(res.getResourceName(resID, true, &rname)) {
        String8 name8;
        if (rname.name8 != NULL) {
            name8 = String8(rname.name8, rname.nameLen);
        } else {
            name8 = String8(rname.name, rname.nameLen);
        }
        resName = env->NewStringUTF(name8.string());
    }

    return resName;
}

JNIEXPORT jstring JNICALL Java_com_apkscanner_core_scanner_AaptNativeScanner_nativeGetResourceType
  (JNIEnv * env, jclass, jlong handle, jint resID)
{
    if(handle == 0) return NULL;

    AssetManager *assetManager = reinterpret_cast<AssetManager*>(handle);
    
    const ResTable& res = assetManager->getResources(false);
    if (res.getError() != NO_ERROR) {
        fprintf(stderr, "ERROR: dump failed because the resource table is invalid/corrupt.\n");
        fflush(stderr);
        return NULL;
    }
    
    jstring resType = NULL;
    android::ResTable::resource_name rname;
    if(res.getResourceName(resID, true, &rname)) {
        String8 type8;
        if (rname.type8 != NULL) {
            type8 = String8(rname.type8, rname.typeLen);
        } else {
            type8 = String8(rname.type, rname.typeLen);
        }
        resType = env->NewStringUTF(type8.string());
    }

    return resType;
}

JNIEXPORT jobjectArray JNICALL Java_com_apkscanner_core_scanner_AaptNativeScanner_nativeGetResourceValues
  (JNIEnv *env, jclass, jlong handle, jint resID)
{
    if(handle == 0) return NULL;

    jclass apkinfo_ResourceInfo = env->FindClass("com/apkscanner/data/apkinfo/ResourceInfo");
    if(apkinfo_ResourceInfo == NULL) {
        fprintf(stderr, "ERROR: failed find class \"com/apkscanner/data/apkinfo/ResourceInfo\"\n");
        fflush(stderr);
        return NULL;
    }
    
    jmethodID apkinfo_ResourceInfo_ = env->GetMethodID(apkinfo_ResourceInfo, "<init>", "(Ljava/lang/String;Ljava/lang/String;)V");
    if(apkinfo_ResourceInfo_ == NULL) {
        fprintf(stderr, "ERROR: failed GetMethodID ResourceInfo<init>\n");
        fflush(stderr);
        env->DeleteLocalRef(apkinfo_ResourceInfo);
        return NULL;
    }

    Vector<String8> resValues;
    Vector<String8> resConfigs;

    AssetManager *assetManager = reinterpret_cast<AssetManager*>(handle);
    const ResTable& res = assetManager->getResources(false);
    if (res.getError() != NO_ERROR) {
        fprintf(stderr, "ERROR: dump failed because the resource table is invalid/corrupt.\n");
        fflush(stderr);
        env->DeleteLocalRef(apkinfo_ResourceInfo);
        return NULL;
    }
    res.getResource(resID, &resValues, &resConfigs);

    int valCount = resValues.size();
    int confCount = resConfigs.size();
    
    if(valCount != confCount) {
        fprintf(stderr, "WARRING: resValues is different size with resConfigs\n");
        fflush(stderr);
    }

    jobjectArray outputArray = env->NewObjectArray(valCount, apkinfo_ResourceInfo, NULL);
    if(outputArray == NULL) {
        fprintf(stderr, "ERROR: Can't create to arrary of ResourceInfo\n");
        fflush(stderr);
        env->DeleteLocalRef(apkinfo_ResourceInfo);
        return NULL;
    }

    for(int i = 0; i < valCount; i++) {
        jobject item = env->NewObject(apkinfo_ResourceInfo, apkinfo_ResourceInfo_,
                env->NewStringUTF(resValues[i].string()),
                env->NewStringUTF(i < confCount ? resConfigs[i].string() : ""));
        if(item == NULL) {
            fprintf(stderr, "WARRING: Can't create to object of ResourceInfo\n");
            fflush(stderr);
            continue;
        }
        env->SetObjectArrayElement(outputArray, i, item);
        env->DeleteLocalRef(item);
    }

    return outputArray;
}

//int testMemStream(void);

JNIEXPORT jobject JNICALL Java_com_apkscanner_core_scanner_AaptNativeScanner_nativeGetResourceString
  (JNIEnv *, jclass, jlong handle, jint, jstring)
{
    if(handle == 0) return NULL;
    //testMemStream();
    return NULL; 
}
/*
int testMemStream(void)
{
#ifdef __WIN32__
   char buf[1024];  
   FILE *stream1, *stream2;  
  
   if( fopen_s( &stream1, "data1", "a" ) == 0 &&  
       fopen_s( &stream2, "data2", "w" ) == 0 )  
   {  
      if( setvbuf( stream1, buf, _IOFBF, sizeof( buf ) ) != 0 )  
         printf( "Incorrect type or size of buffer for stream1\n" );  
      else  
         printf( "'stream1' now has a buffer of 1024 bytes\n" );  
      if( setvbuf( stream2, NULL, _IONBF, 0 ) != 0 )  
         printf( "Incorrect type or size of buffer for stream2\n" );  
      else  
         printf( "'stream2' now has no buffer\n" );  
      _fcloseall();  
   }  
#else
   //fjdsklfj;
#endif
  FILE *pFile;

  pFile=fopen ("myfile.txt","w");

  setvbuf ( pFile , NULL , _IOFBF , 1024 );

  // File operations here

  fclose (pFile);

  return 0;
}
*/
//static JNINativeMethod sMethod[] = {
    /* name, signature, funcPtr */
//    {"com_apkscanner_core_scanner_AaptNativeScanner_run", "([Ljava/lang/String;)[Ljava/lang/String;", (jobjectArray*)com_apkscanner_core_scanner_AaptNativeScanner_run}
//};

/*
int jniRegisterNativMethod(JNIEnv* env, const char* className, const JNINativeMethod* gMethods, int numMethods ) {
    jclass clazz;
 
    clazz = env->FindClass(className);
 
    if(clazz == NULL){
        return -1;
    }
    if(env->RegisterNatives(clazz, gMethods, numMethods) < 0){
        return -1;
    }
    return 0;
}
*/
