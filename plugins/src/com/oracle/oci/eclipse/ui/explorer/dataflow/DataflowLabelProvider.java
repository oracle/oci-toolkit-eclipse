package com.oracle.oci.eclipse.ui.explorer.dataflow;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.navigator.IDescriptionProvider;
import com.oracle.oci.eclipse.Activator;
import com.oracle.oci.eclipse.Icons;

public class DataflowLabelProvider extends LabelProvider implements ILabelProvider, IDescriptionProvider
{
    @Override
    public String getText(Object element)
    {
        if ( element instanceof DataflowRootElement ) {
            return DataflowRootElement.getName();
        } else if ( element instanceof DataflowApplicationElement ){
            return DataflowApplicationElement.getName();
        }
        else if ( element instanceof DataflowRunElement ){
            return DataflowRunElement.getName();
        }  
        else if ( element instanceof DataflowPrivateEndPointsElement ){
            return DataflowPrivateEndPointsElement.getName();
        }  
        else if( element instanceof DataflowSettingUpElement) {
        	return DataflowSettingUpElement.getName();
        }
        return null;
    }

    @Override
    public String getDescription(Object element)
    {
        String text = getText(element);
        return "Double click to open " + text;
    }

    @Override
    public Image getImage(Object element)
    {
        if (element instanceof DataflowRootElement)
        {
            return Activator.getImage(Icons.COMPUTE.getPath());
        }
        else if (element instanceof DataflowApplicationElement)
        {
            return Activator.getImage(Icons.COMPUTE_INSTANCE.getPath());
        }
        else if (element instanceof DataflowRunElement)
        {
            return Activator.getImage(Icons.COMPUTE_INSTANCE.getPath());
        }
        else if (element instanceof DataflowPrivateEndPointsElement)
        {
            return Activator.getImage(Icons.COMPUTE_INSTANCE.getPath());
        }
        else if (element instanceof DataflowSettingUpElement)
        {
            return Activator.getImage(Icons.COMPUTE_INSTANCE.getPath());
        }
        return null;
    }
}