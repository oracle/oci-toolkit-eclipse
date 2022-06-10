package tests.utils;

import com.oracle.oci.eclipse.account.PreferencesWrapper;

public class SetupConfig {
    private static final String profileName = "DEFAULT";
    private static final String projectDir = System.getProperty("user.dir");
    private static final String configFileName = projectDir + "/resources/internal/config";

    public static final String COMPARTMENT_NAME = "ideplugin";
    public static final String COMPARTMENT_ID = "ocid1.compartment.oc1..aaaaaaaasrbmmnzhuhtcutbfnn52pswbxwao5n7x7zkpg52eklahfcgbtw6q"; 

    public static void init(){
        PreferencesWrapper.setConfigFileName(configFileName);
        PreferencesWrapper.setProfile(profileName);
    }

    public static void setProxy() {
        System.setProperty("java.net.useSystemProxies", "true");
    }

    
}
