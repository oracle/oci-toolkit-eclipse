package com.oracle.oci.eclipse.sdkclients;

import java.beans.PropertyChangeEvent;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import com.oracle.bmc.keymanagement.KmsVaultClient;
import com.oracle.bmc.keymanagement.model.VaultSummary;
import com.oracle.bmc.keymanagement.requests.ListVaultsRequest;
import com.oracle.bmc.keymanagement.responses.ListVaultsResponse;
import com.oracle.bmc.secrets.SecretsClient;
import com.oracle.bmc.secrets.model.Base64SecretBundleContentDetails;
import com.oracle.bmc.secrets.model.SecretBundleContentDetails;
import com.oracle.bmc.secrets.requests.GetSecretBundleRequest;
import com.oracle.bmc.secrets.responses.GetSecretBundleResponse;
import com.oracle.bmc.vault.VaultsClient;
import com.oracle.bmc.vault.model.SecretSummary;
import com.oracle.bmc.vault.requests.ListSecretsRequest;
import com.oracle.bmc.vault.responses.ListSecretsResponse;
import com.oracle.oci.eclipse.ErrorHandler;
import com.oracle.oci.eclipse.account.AuthProvider;

public class VaultClient extends BaseClient {

    private KmsVaultClient kmsVaultsClient;
    private VaultsClient vaultsClient;
    private SecretsClient secretsClient;

    public VaultClient() {
        if (kmsVaultsClient == null) {
            kmsVaultsClient = createKmsVaultInstanceClient();
        }

        if (vaultsClient == null) {
            vaultsClient = createVaultsInstanceClient();
        }
        
        if (secretsClient == null) {
            secretsClient = createSecretInstanceClient();
        }
    }

    @Override
    public void updateClient() {
        close();
        createKmsVaultInstanceClient();
        createVaultsInstanceClient();
        createSecretInstanceClient();
    }

    private SecretsClient createSecretInstanceClient() {
        secretsClient = new SecretsClient(AuthProvider.getInstance().getProvider());
        secretsClient.setRegion(AuthProvider.getInstance().getRegion());
        return secretsClient;
    }

    private KmsVaultClient createKmsVaultInstanceClient() {
        kmsVaultsClient = new KmsVaultClient(AuthProvider.getInstance().getProvider());
        kmsVaultsClient.setRegion(AuthProvider.getInstance().getRegion());
        return kmsVaultsClient;
    }

    private VaultsClient createVaultsInstanceClient() {
        vaultsClient = new VaultsClient(AuthProvider.getInstance().getProvider());
        vaultsClient.setRegion(AuthProvider.getInstance().getRegion());
        return vaultsClient;
    }

    @Override
    public void close() {
        try {
            if (kmsVaultsClient != null) {
                kmsVaultsClient.close();
            }
            if (vaultsClient != null) {
                vaultsClient.close();
            }
            if (secretsClient != null) {
                secretsClient.close();
            }
        } catch (Exception e) {
            ErrorHandler.logErrorStack(e.getMessage(), e);
        }
    }

    public Map<String, VaultSummary> listVaults(String currentCompartmentId) {
        Map<String, VaultSummary> vaults = new LinkedHashMap<>();
        ListVaultsRequest request = ListVaultsRequest.builder().compartmentId(currentCompartmentId).build();
        ListVaultsResponse listVaults = kmsVaultsClient.listVaults(request);
        for (VaultSummary vaultSummary : listVaults.getItems()) {
            vaults.put(vaultSummary.getId(), vaultSummary);
        }
        return vaults;
    }

    public Map<String, SecretSummary> listSecretsInVault(VaultSummary vault, String compartmentId) {
        Map<String, SecretSummary> secrets = new LinkedHashMap<>();
        ListSecretsRequest request = ListSecretsRequest.builder().vaultId(vault.getId()).compartmentId(compartmentId)
                .build();
        ListSecretsResponse listSecrets = vaultsClient.listSecrets(request);
        for (SecretSummary secretSummary : listSecrets.getItems()) {
            secrets.put(secretSummary.getSecretName(), secretSummary);
        }
        return secrets;
    }

    public String getSecretContent(SecretSummary secret) {
        GetSecretBundleRequest req = GetSecretBundleRequest.builder().secretId(secret.getId()).build();
        GetSecretBundleResponse secretBundle = secretsClient.getSecretBundle(req);
        SecretBundleContentDetails content = secretBundle.getSecretBundle().getSecretBundleContent();
        if (content instanceof Base64SecretBundleContentDetails) {
            String strContent = ((Base64SecretBundleContentDetails) content).getContent();
            //System.out.println(strContent);
            //if (strContent)
            byte[] decode = Base64.getDecoder().decode(strContent);
            CharBuffer decode2 = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(decode));
            strContent = new String(decode2.array());
//            System.out.println(strContent);
            return strContent;
        }
        return null;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String newRegion = evt.getNewValue().toString();
        kmsVaultsClient.setRegion(newRegion);
        vaultsClient.setRegion(newRegion);
        secretsClient.setRegion(newRegion);
    }
}
