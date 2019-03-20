package tests.utils;

import com.oracle.oci.eclipse.account.PreferencesWrapper;

public class SetupConfig {
    private static String profileName = "IDE-ADMIN";
    private static String projectDir = System.getProperty("user.dir");
    private static String configFileName = projectDir + "/resources/internal/config";

    public static void init(){
        PreferencesWrapper.setConfigFileName(configFileName);
        PreferencesWrapper.setProfile(profileName);
    }

    public static void setProxy() {
        System.setProperty("java.net.useSystemProxies", "true");
    }

}
