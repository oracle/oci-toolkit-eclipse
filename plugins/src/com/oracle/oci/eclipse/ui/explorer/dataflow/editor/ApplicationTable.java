package com.oracle.oci.eclipse.ui.explorer.dataflow.editor;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.forms.widgets.FormToolkit;
import com.oracle.bmc.Region;
import com.oracle.bmc.dataflow.model.ApplicationSummary;
import com.oracle.bmc.dataflow.requests.ListApplicationsRequest;
import com.oracle.bmc.dataflow.responses.ListApplicationsResponse;
import com.oracle.oci.eclipse.account.AuthProvider;
import com.oracle.oci.eclipse.sdkclients.IdentClient;
import com.oracle.oci.eclipse.ui.explorer.common.BaseTable;
import com.oracle.oci.eclipse.ui.explorer.common.BaseTableLabelProvider;
import com.oracle.oci.eclipse.ui.explorer.common.CustomWizardDialog;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.DeleteApplicationAction;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.DetailsApplicationAction;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.EditApplicationAction;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.GetApplications;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.OpenInConsoleAction;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.RunApplicationAction;
import com.oracle.oci.eclipse.ui.explorer.dataflow.wizards.CreateApplicationWizard;

public class ApplicationTable extends BaseTable{
   
	private int tableDataSize = 0;
	
    private static final int ID_COL = 1;
    private static final int NAME_COL = 0;
    private static final int LANGUAGE_COL = 2;
    private static final int OWNER_COL = 3;
    private static final int CREATED_COL = 4;
    private static final int UPDATED_COL = 5;
    
    private static String COMPARTMENT_ID;
	private String pageToShow=null;
    private ListApplicationsRequest.SortBy sortBy;
	private ListApplicationsRequest.SortOrder sortOrder;
	private ListApplicationsResponse listApplicationsResponse;
	private Button previouspage,nextpage;
	private String nextPageStr;
	
    public ApplicationTable(Composite parent, int style) {
        super(parent, style);
        COMPARTMENT_ID = AuthProvider.getInstance().getCompartmentId();
        viewer.setLabelProvider(new TableLabelProvider());
        viewer.setInput(getTableData());
        viewer.setItemCount(getTableDataSize());
        sortBy=ListApplicationsRequest.SortBy.TimeCreated;
        sortOrder=ListApplicationsRequest.SortOrder.Desc;
    }
    
    List<ApplicationSummary> applicationList = new ArrayList<ApplicationSummary>();
    
    @Override
    public List<ApplicationSummary> getTableData() {  
    	if(COMPARTMENT_ID != AuthProvider.getInstance().getCompartmentId()) {
    		COMPARTMENT_ID = AuthProvider.getInstance().getCompartmentId();
    		pageToShow=null;
    	}
    	if(COMPARTMENT_ID== null) {
    		COMPARTMENT_ID = IdentClient.getInstance().getRootCompartment().getCompartmentId();
    	}
        try {
        	
        	IRunnableWithProgress op = new GetApplications(COMPARTMENT_ID,sortBy,sortOrder,pageToShow);
        	new ProgressMonitorDialog(Display.getDefault().getActiveShell()).run(true, true, op);
        	String errorMessage=((GetApplications)op).getErrorMessage();
        	if(errorMessage!=null) 
        		throw new Exception(errorMessage);
            listApplicationsResponse = ((GetApplications)op).listApplicationsResponse;
            applicationList=((GetApplications)op).applicationSummaryList;
            tableDataSize = applicationList.size();
       } catch (Exception e) {
           MessageDialog.openError(Display.getDefault().getActiveShell(),"Unable to get applications: ",e.getMessage());               
        }
        
        nextPageStr=listApplicationsResponse.getOpcNextPage();
		if(nextPageStr!=null&&!nextPageStr.isEmpty()) {
			nextpage.setEnabled(true);
		}
		else {
			nextpage.setEnabled(false);
		}
        refresh(false);            
        return applicationList;
    }

    @Override
    public List<ApplicationSummary> getTableCachedData() {
        return applicationList;
    }

    @Override
    public int getTableDataSize() {
        return tableDataSize;
    }

    private final class TableLabelProvider extends BaseTableLabelProvider {
        @Override
        public String getColumnText(Object element, int columnIndex) {
            try {
                ApplicationSummary s = (ApplicationSummary) element;
  
                switch (columnIndex) {
                case ID_COL:
                    return s.getId();
                case NAME_COL:
                    return s.getDisplayName();
                case LANGUAGE_COL:
                    return s.getLanguage().toString();
                case OWNER_COL:
                    return s.getOwnerUserName();
                case CREATED_COL:
                    return s.getTimeCreated().toString();
                case UPDATED_COL:
                    return s.getTimeUpdated().toString();
                }
            } catch (Exception ex) {
            	MessageDialog.openError(Display.getDefault().getActiveShell(),"Unable to set Application table details: ",ex.getMessage());
            }
            return "";
        }
    }

    @Override
    protected void createColumns(TableColumnLayout tableColumnLayout, Table tree) {
       	
    	tree.setSortDirection(SWT.UP);
        TableColumn tc;        
        tc = createColumn(tableColumnLayout,tree, "Name", 10);
        tc.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
            	pageToShow=null;
            	sortBy=ListApplicationsRequest.SortBy.DisplayName;
                if(sortOrder == ListApplicationsRequest.SortOrder.Desc)
                	sortOrder=ListApplicationsRequest.SortOrder.Asc;
                else
                	sortOrder=ListApplicationsRequest.SortOrder.Desc;
                refresh(true);
              }
            });
        
        tc = createColumn(tableColumnLayout,tree, "OCI ID", 10);
        
        tc = createColumn(tableColumnLayout,tree, "Language", 6);
        tc.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
            	pageToShow=null;
            	sortBy=ListApplicationsRequest.SortBy.Language;
                if(sortOrder == ListApplicationsRequest.SortOrder.Desc) 
                	sortOrder = ListApplicationsRequest.SortOrder.Asc;
                else 
                	sortOrder = ListApplicationsRequest.SortOrder.Desc;
                refresh(true);
              }
            });
        
        tc = createColumn(tableColumnLayout,tree, "Owner", 10);
        
        tc = createColumn(tableColumnLayout,tree, "Created", 10);
        tc.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
            	pageToShow=null;
            	sortBy=ListApplicationsRequest.SortBy.TimeCreated;
                if(sortOrder == ListApplicationsRequest.SortOrder.Desc) 
                	sortOrder = ListApplicationsRequest.SortOrder.Asc;
                else 
                	sortOrder = ListApplicationsRequest.SortOrder.Desc;
                refresh(true);
              }
            });
        
        tc = createColumn(tableColumnLayout,tree, "Updated", 10);

    }

    @Override
    protected void fillMenu(IMenuManager manager) {
        if (getSelectedObjects().size() == 1) {
            manager.add(new DetailsApplicationAction(ApplicationTable.this));
            manager.add(new EditApplicationAction(ApplicationTable.this));
            manager.add(new RunApplicationAction(ApplicationTable.this));
            manager.add(new DeleteApplicationAction(ApplicationTable.this));
            manager.add(new OpenInConsoleAction(((ApplicationSummary)getSelectedObjects().get(0)).getId()));
        }        
    }

    @Override
    protected void addTableLabels(FormToolkit toolkit, Composite left, Composite right) {
        
        Button createApplicationButton = toolkit.createButton(right,"Create Application", SWT.PUSH);
        createApplicationButton.setText("Create Application");
        createApplicationButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	CustomWizardDialog dialog = new CustomWizardDialog(Display.getDefault().getActiveShell(), new CreateApplicationWizard(COMPARTMENT_ID));
     	       	dialog.setFinishButtonText("Create");
   	        	if (Window.OK == dialog.open()) {
   	        	refresh(true);
   	        	}      	          	
            }
            public void widgetDefaultSelected(SelectionEvent e) {}
        });	
        
        Link link = new Link(right, SWT.NONE);
        Region region = AuthProvider.getInstance().getRegion();
        link.setText("<a href=\"https://console."+region.getRegionId()+".oraclecloud.com/data-flow/apps\">Click to open in console</a>");
         
        // Event handling when users click on links.
        link.addSelectionListener(new SelectionAdapter()  {
         
            @Override
            public void widgetSelected(SelectionEvent e) {
                Program.launch("https://console."+region.getRegionId()+".oraclecloud.com/data-flow/apps");
            }
             
        });
        
		Button refreshTable=new Button(right.getParent(),SWT.PUSH);
		refreshTable.setText("Refresh Table");
		refreshTable.setLayoutData(new GridData());
		refreshTable.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
				refresh(true);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}
        });
        
        Composite page=new Composite(right.getParent(),SWT.NONE);
        GridLayout gl=new GridLayout();
        gl.numColumns=2;
        page.setLayout(gl);
        GridData gdpage = new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_END);
        page.setLayoutData(gdpage);
        
        previouspage=new Button(page,SWT.TRAVERSE_PAGE_PREVIOUS);
        nextpage=new Button(page,SWT.TRAVERSE_PAGE_NEXT);
        previouspage.setText("<");
        nextpage.setText(">");
        previouspage.setLayoutData(new GridData());
        nextpage.setLayoutData(new GridData());
        previouspage.setEnabled(false);
        
        nextpage.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	pageToShow = listApplicationsResponse.getOpcNextPage();
				refresh(true);
				previouspage.setEnabled(true);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}
        });
        
        previouspage.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {                
            	pageToShow = listApplicationsResponse.getOpcPrevPage();
				refresh(true);
            }
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}
        });        
    } 
}
