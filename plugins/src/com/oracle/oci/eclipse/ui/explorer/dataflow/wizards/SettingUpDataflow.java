package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;

import com.oracle.oci.eclipse.ui.explorer.common.CustomWizardDialog;

public class SettingUpDataflow extends AbstractHandler implements IElementUpdater{

	@Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        if ( null == event || ! ( event.getTrigger() instanceof Event ) ) { return null;}

        Event eventWidget = (Event)event.getTrigger();
        if ( eventWidget.widget instanceof ToolItem )  {       	
        	DataTransferObject.filedir=null;
        	DataTransferObject.archivedir=null;               	
        	CustomWizardDialog dialog = new CustomWizardDialog(Display.getDefault().getActiveShell(),
        			new SettingUpDataflowWizard());
        	dialog.setFinishButtonText("Run");
        	if (Window.OK == dialog.open()) {
        	}
        }
        return null;
    }
	
	  @Override
	    public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
	    	
	    }
}
