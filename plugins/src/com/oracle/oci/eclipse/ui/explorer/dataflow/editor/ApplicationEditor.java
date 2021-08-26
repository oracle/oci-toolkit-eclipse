package com.oracle.oci.eclipse.ui.explorer.dataflow.editor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import com.oracle.oci.eclipse.ui.account.ClientUpdateManager;
import com.oracle.oci.eclipse.ui.explorer.common.BaseEditor;

public class ApplicationEditor extends BaseEditor implements PropertyChangeListener  {	
    public final static String ID = ApplicationEditor.class.getName();
    public final static String TITLE = "Dataflow Applications";
    private ApplicationTable applicationTable;
    private ApplicationEditor classReference = this;

    @Override
    public void createPartControl(Composite parent) {
        ScrolledForm form = super.initForm(parent);
        applicationTable = new ApplicationTable(form.getBody(), SWT.None);
        applicationTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        ClientUpdateManager.getInstance().addViewChangeListener(this);
        DisposeListener disposeListener = new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                ClientUpdateManager.getInstance().removeViewChangeListener(classReference);
            }};
            applicationTable.addDisposeListener(disposeListener);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
    	applicationTable.refresh(true);
    }
}
