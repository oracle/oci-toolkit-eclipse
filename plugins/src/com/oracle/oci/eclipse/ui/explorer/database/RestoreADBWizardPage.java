/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.eclipse.ui.explorer.database;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.oracle.bmc.database.model.AutonomousDatabaseBackupSummary;
import com.oracle.bmc.database.model.AutonomousDatabaseSummary;
import com.oracle.oci.eclipse.Activator;
import com.oracle.oci.eclipse.Icons;

public class RestoreADBWizardPage extends WizardPage {
    AutonomousDatabaseSummary instance;
    private Label fromDateLabel;
	private Label toDateLabel;
	private DateTime fromDateTime;
	private DateTime toDateTime;
	private Date fromDate;
	private Date toDate;
	private Table table;
	List<AutonomousDatabaseBackupSummary> backupList;

    public RestoreADBWizardPage(ISelection selection, AutonomousDatabaseSummary instance, List<AutonomousDatabaseBackupSummary> backupList) {
        super("wizardPage");
        setTitle("Restore Autonomous Database");
        setDescription("");
        this.instance = instance;
        this.backupList = backupList;
    }

    @Override
    public void createControl(Composite parent) {
    	Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
        
        final String restoreMsg = "Restore the Autonomous Database from a specified backup.";
        Label restoreMsgLabel = new Label(container, SWT.NULL);
        restoreMsgLabel.setText(restoreMsg);

        Composite innerTopContainer = new Composite(container, SWT.NONE);
        GridLayout innerTopLayout = new GridLayout();
        innerTopLayout.makeColumnsEqualWidth = true;
        innerTopLayout.numColumns =2;
        innerTopLayout.horizontalSpacing =200;
        innerTopLayout.marginLeft = 70;
        innerTopContainer.setLayout(innerTopLayout);
        
        fromDateLabel = new Label(innerTopContainer, SWT.NULL);
        fromDateLabel.setText("From:");
        toDateLabel = new Label(innerTopContainer, SWT.NULL);
        toDateLabel.setText("To:");
        fromDateTime = new DateTime(innerTopContainer, SWT.DATE | SWT.DROP_DOWN);
        fromDateTime.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        toDateTime = new DateTime(innerTopContainer, SWT.DATE | SWT.DROP_DOWN);
        toDateTime.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        toDate = new Date();
        fromDate = DateUtils.addDays(toDate, -7);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fromDate);
        // set default value for From date widget
        fromDateTime.setDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        
		fromDateTime.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				System.out.println("From date changed");
				handleDateTimeChange(container, fromDateTime, true);
			}
		});
        
		toDateTime.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				System.out.println("To date changed");
				handleDateTimeChange(container, toDateTime, false);
			}
		});
        
        Label label = new Label(innerTopContainer, SWT.NULL);
        label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        label.setText("");
        label = new Label(innerTopContainer, SWT.NULL);
        label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        label.setText("");
        
        Composite innerContainer = new Composite(container, SWT.NONE);
        GridLayout innerLayout = new GridLayout();
        innerLayout.marginLeft = 70;
        innerContainer.setLayout(innerLayout);
        
        table = new Table(innerContainer, SWT.BORDER | SWT.SINGLE | SWT.RADIO | SWT.RESIZE | SWT.V_SCROLL);
        GridData gd_table = new GridData(SWT.FILL, SWT.FILL, false, false);
        gd_table.heightHint = 300;
        gd_table.widthHint = 632;
        table.setLayoutData(gd_table);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        TableColumn tblclmnNewColumn = new TableColumn(table, SWT.NONE);
        tblclmnNewColumn.setWidth(37);        
        TableColumn tblclmnNewColumn_1 = new TableColumn(table, SWT.NONE);
        tblclmnNewColumn_1.setWidth(305);
        tblclmnNewColumn_1.setText("Backup Name");
        TableColumn tblclmnNewColumn_2 = new TableColumn(table, SWT.NONE);
        tblclmnNewColumn_2.setWidth(130);
        tblclmnNewColumn_2.setText("State");
        TableColumn tblclmnNewColumn_3 = new TableColumn(table, SWT.NONE);
        tblclmnNewColumn_3.setWidth(170);
        tblclmnNewColumn_3.setText("Type");

		populateTableData();
		
		// to avoid overlapping of controls
		for (int i = 0; i < 3; i++) {
			label = new Label(innerContainer, SWT.NULL);
	        label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	        label.setText("");
		}
		
        setControl(innerContainer);
    }
    
    private void populateTableData() {
    	for (AutonomousDatabaseBackupSummary backup : backupList) {
			if(backup.getTimeEnded().compareTo(DateUtils.addDays(fromDate, -1))>0 && backup.getTimeEnded().compareTo(toDate)<=0) {
				TableItem item = new TableItem(table, SWT.NONE);
	            item.setText(1,backup.getDisplayName());
	            item.setImage(2, Activator.getImage(Icons.BACKUP_ACTIVE_STATE.getPath()));
	            item.setText(2,backup.getLifecycleState().getValue());
	            item.setText(3,backup.getType().getValue());
			}
		}
    }
    
	private void handleDateTimeChange(Composite container, DateTime newDateTime, boolean isFromDateChanged) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_MONTH, newDateTime.getDay());
		calendar.set(Calendar.MONTH, newDateTime.getMonth());
		calendar.set(Calendar.YEAR, newDateTime.getYear());
		if (isFromDateChanged) {
			fromDate = calendar.getTime();
			if (fromDate.compareTo(toDate) > 0) {
				toDate = fromDate;
				toDateTime.setDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
						calendar.get(Calendar.DAY_OF_MONTH));
			}
		} else {
			toDate = calendar.getTime();
			if (toDate.compareTo(fromDate) < 0) {
				fromDate = toDate;
				fromDateTime.setDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
						calendar.get(Calendar.DAY_OF_MONTH));
			}
		}
		table.removeAll();
		populateTableData();
		container.layout();
	}

	public Date getRestoreTimeStamp() {
		final TableItem item[] = table.getSelection();
		String backupName = null;
		if (item != null && item.length > 0) {
			backupName = item[0].getText(1);
		}

		if (backupName == null)
			return null;

		for (AutonomousDatabaseBackupSummary backup : backupList) {
			if (backup.getDisplayName().equalsIgnoreCase(backupName))
				return backup.getTimeEnded();
		}
		return null;
	}

}
