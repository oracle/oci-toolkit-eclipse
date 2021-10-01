package com.oracle.oci.eclipse.ui.explorer.database;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.eclipse.equinox.security.storage.StorageException;
import org.osgi.service.component.annotations.Component;

import com.oracle.oci.eclipse.account.PreferencesWrapper;

@Component
public class SecurePasswordRetrievalService {

	public String getDBAdminPassword(File tnsFolder) throws IOException, StorageException
	{
		Properties props = DBUtils.readToolProperties(tnsFolder);
		String keyName = (String) props.get(DBUtils.TOOL_PROPERTIES_KEY_ADMIN_PASSWORD_SECURE_KEY_NAME);
		return PreferencesWrapper.getSecurePreferences().get(keyName, null);
	}
}
