package com.oracle.oci.eclipse.ui.explorer.database;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.navigator.IDescriptionProvider;

import com.oracle.oci.eclipse.Activator;
import com.oracle.oci.eclipse.Icons;

public class ADBLabelProvider extends LabelProvider implements ILabelProvider, IDescriptionProvider {

	@Override
	public String getText(Object element) {
		if (element instanceof ADBRootElement) {
			return ADBRootElement.getName();
		}
		return null;
	}

	@Override
	public String getDescription(Object element) {
		String text = getText(element);
		if (element instanceof ADBRootElement) {
			return "Double click to see ADB Instances or Right click to create an ADB instance";
		}
		return null;
	}

	@Override
	public Image getImage(Object element) {
		if (element instanceof ADBRootElement) {
			return Activator.getImage(Icons.DATABASE.getPath());
		}
		return null;
	}

}
