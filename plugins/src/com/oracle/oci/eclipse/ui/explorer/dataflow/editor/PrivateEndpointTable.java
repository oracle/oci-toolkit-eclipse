package com.oracle.oci.eclipse.ui.explorer.dataflow.editor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.operation.IRunnableWithProgress;
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
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.oracle.bmc.Region;
import com.oracle.bmc.dataflow.model.PrivateEndpointSummary;
import com.oracle.bmc.dataflow.responses.ListPrivateEndpointsResponse;
import com.oracle.oci.eclipse.account.AuthProvider;
import com.oracle.oci.eclipse.sdkclients.IdentClient;
import com.oracle.oci.eclipse.ui.explorer.common.BaseTable;
import com.oracle.oci.eclipse.ui.explorer.common.BaseTableLabelProvider;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.CreatePrivateEndpointAction;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.DeletePrivateEndpointAction;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.DetailsPrivateEndpointAction;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.EditPrivateEndpointAction;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.GetPrivateEndpoints;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.OpenInConsoleAction;

public class PrivateEndpointTable extends BaseTable {
    private int tableDataSize = 0;
    private static final int NAME_COL = 0;
    private static final int STATE_COL = 1;
	private static final int CREATED_COL = 2;
	private List<PrivateEndpointSummary> pepSummaryList = new ArrayList<PrivateEndpointSummary>();
	private String pagetoshow=null,nextPageStr;
	private ListPrivateEndpointsResponse listpepsresponse;
	private Button previousPage,nextPage;
	public static String compid=IdentClient.getInstance().getRootCompartment().getId();

    public PrivateEndpointTable(Composite parent, int style) {
        super(parent, style);
        viewer.setLabelProvider(new TableLabelProvider());
        viewer.setInput(getTableData());
        viewer.setItemCount(getTableDataSize());
    }
    
    @Override
    public List<PrivateEndpointSummary> getTableData() {
    	
    	 try {
    		 String currentcompid=AuthProvider.getInstance().getCompartmentId();
    	        if(currentcompid!=null&&!compid.equals(currentcompid)) {
    	         	compid=currentcompid;
    	         	pagetoshow=null;
    	         }
         	IRunnableWithProgress op = new GetPrivateEndpoints(compid,pagetoshow);
            new ProgressMonitorDialog(Display.getDefault().getActiveShell()).run(true, true, op);
            String errorMessage=((GetPrivateEndpoints)op).getErrorMessage();
        	if(errorMessage!=null) 
        		throw new Exception(errorMessage);
            listpepsresponse=((GetPrivateEndpoints)op).listpepsresponse;
            pepSummaryList=((GetPrivateEndpoints)op).pepSummaryList;
            tableDataSize = pepSummaryList.size();
         } catch (Exception e) {
         	MessageDialog.openError(getShell(), "Unable to get Private Endpoints list", e.getMessage());
         }
    	 
    	 nextPageStr=listpepsresponse.getOpcNextPage();
		 if(nextPageStr!=null&&!nextPageStr.isEmpty()) {
			 nextPage.setEnabled(true);
		 }
		 else {
		     nextPage.setEnabled(false);
		 }
    	 
         refresh(false);
        
        return pepSummaryList;
    }
    
    @Override
    public List<PrivateEndpointSummary> getTableCachedData() {
        return pepSummaryList;
    }

    @Override
    public int getTableDataSize() {
        return tableDataSize;
    }

    /* Label provider */
    private final class TableLabelProvider extends BaseTableLabelProvider {

        @Override
        public String getColumnText(Object element, int columnIndex) {
            try {
                PrivateEndpointSummary s = (PrivateEndpointSummary) element;

                switch (columnIndex) {
                case NAME_COL:
                    return s.getDisplayName();
                case STATE_COL:
                    return s.getLifecycleState().toString();
				case CREATED_COL:
					return (new SimpleDateFormat("dd-M-yyyy hh:mm:ss")).format(s.getTimeCreated());
                }
            } 
            catch (Exception ex) {
            	MessageDialog.openError(getShell(), "Error forming table", ex.getMessage());
            }
            return "";
        }
    }

    @Override
    protected void createColumns(TableColumnLayout tableColumnLayout, Table tree) {
    	
        createColumn(tableColumnLayout,tree, "Name", 15);
        createColumn(tableColumnLayout,tree, "State", 8);
		createColumn(tableColumnLayout,tree, "Created", 10);
    }

    @Override
    protected void fillMenu(IMenuManager manager) {
		manager.add(new CreatePrivateEndpointAction(PrivateEndpointTable.this));
        manager.add(new Separator());

        if (getSelectedObjects().size() == 1) {
           String pepState=((PrivateEndpointSummary)getSelectedObjects().get(0)).getLifecycleState().toString();
           manager.add(new Separator());
           if(!pepState.equals("Creating")) 
        	   manager.add(new DetailsPrivateEndpointAction(PrivateEndpointTable.this));
           if(!(pepState.equals("Creating") || pepState.equals("Deleting") || pepState.equals("Updating"))) 
        	   manager.add(new DeletePrivateEndpointAction(PrivateEndpointTable.this,(PrivateEndpointSummary)getSelectedObjects().get(0)));
		   if((pepState.equals("Active")||pepState.equals("Inactive")))
			   manager.add(new EditPrivateEndpointAction((PrivateEndpointSummary)getSelectedObjects().get(0),PrivateEndpointTable.this));
		   manager.add(new OpenInConsoleAction(((PrivateEndpointSummary)getSelectedObjects().get(0)).getId()));
        }

    }
	
	@Override
    protected void addTableLabels(FormToolkit toolkit, Composite left, Composite right) {
		
        Link link = new Link(right, SWT.NONE);
        Region region = AuthProvider.getInstance().getRegion();
        link.setText("<a href=\"https://console."+region.getRegionId()+".oraclecloud.com/data-flow/privateEndpoints\">Click to open in console</a>");
         
        // Event handling when users click on links.
        link.addSelectionListener(new SelectionAdapter()  {
         
            @Override
            public void widgetSelected(SelectionEvent e) {
                Program.launch("https://console."+region.getRegionId()+".oraclecloud.com/data-flow/privateEndpoints");
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
        GridData gdpage = new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_END);
        page.setLayoutData(gdpage);
        
        GridLayout gl=new GridLayout();
        gl.numColumns=2;
        page.setLayout(gl);
        previousPage=new Button(page,SWT.TRAVERSE_PAGE_PREVIOUS);
        nextPage=new Button(page,SWT.TRAVERSE_PAGE_NEXT);
        previousPage.setText("<");
        nextPage.setText(">");
        previousPage.setLayoutData(new GridData());
        nextPage.setLayoutData(new GridData());
        previousPage.setEnabled(false);
        
        nextPage.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                
				pagetoshow=listpepsresponse.getOpcNextPage();
				refresh(true);
				previousPage.setEnabled(true);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}
        });
        
        previousPage.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                
				pagetoshow=listpepsresponse.getOpcPrevPage();
				refresh(true);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}
        });
    }
}