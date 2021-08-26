 package com.oracle.oci.eclipse.ui.explorer.dataflow.editor;

import java.text.CharacterIterator;
import java.text.SimpleDateFormat;
import java.text.StringCharacterIterator;
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
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.oracle.bmc.Region;
import com.oracle.bmc.dataflow.model.ApplicationSummary;
import com.oracle.bmc.dataflow.model.RunSummary;
import com.oracle.bmc.dataflow.requests.ListRunsRequest;
import com.oracle.bmc.dataflow.responses.ListRunsResponse;
import com.oracle.oci.eclipse.account.AuthProvider;
import com.oracle.oci.eclipse.sdkclients.IdentClient;
import com.oracle.oci.eclipse.ui.explorer.common.BaseTable;
import com.oracle.oci.eclipse.ui.explorer.common.BaseTableLabelProvider;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.DeleteRunAction;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.DetailsRunAction;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.GetRuns;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.OpenInConsoleAction;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.RunAction;
import com.oracle.oci.eclipse.ui.explorer.dataflow.actions.ShowLogsAction;

public class RunTable extends BaseTable {
    private int tableDataSize = 0;
    private static final int NAME_COL = 0;
    private static final int LANGUAGE_COL = 1;
    private static final int STATE_COL = 2;
    private static final int OWNER_COL = 3;
	private static final int CREATED_COL = 4;
	private static final int DURATION_COL = 5;
	private static final int TOTAL_OCPU_COL = 6;
	private static final int DATA_READ_COL = 8;
	private static final int DATA_WRITTEN_COL = 7;
	private List<RunSummary> runSummaryList = new ArrayList<RunSummary>();
	private ListRunsRequest.SortBy sortBy;
	private ListRunsRequest.SortOrder sortOrder;
	private String pagetoshow=null,nextPageStr;
	private ListRunsResponse listrunsresponse;
	private Button previousPage,nextPage;
	private static String compid=IdentClient.getInstance().getRootCompartment().getId();
	
    public RunTable(Composite parent, int style) {
        super(parent, style);

        viewer.setLabelProvider(new TableLabelProvider());
        viewer.setInput(getTableData());
        viewer.setItemCount(getTableDataSize());
        sortBy=ListRunsRequest.SortBy.TimeCreated;
        sortOrder=ListRunsRequest.SortOrder.Desc;
    }

    @Override
    public List<RunSummary> getTableData() {
                try {
                	String currentcompid=AuthProvider.getInstance().getCompartmentId();
                    if(currentcompid!=null&&!compid.equals(currentcompid)) {
                     	compid=currentcompid;
                     	pagetoshow=null;
                     }
                	IRunnableWithProgress op = new GetRuns(compid,sortBy,sortOrder,pagetoshow);
                    new ProgressMonitorDialog(Display.getDefault().getActiveShell()).run(true, true, op);
                    String errorMessage=((GetRuns)op).getErrorMessage();
                	if(errorMessage!=null) 
                		throw new Exception(errorMessage);
                    listrunsresponse=((GetRuns)op).listrunsresponse;
                    runSummaryList=((GetRuns)op).runSummaryList;
                    tableDataSize = runSummaryList.size();
                } catch (Exception e) {
                	MessageDialog.openError(getShell(), "Unable to get Runs list", e.getMessage());
                }
                
               nextPageStr=listrunsresponse.getOpcNextPage();
				if(nextPageStr!=null&&!nextPageStr.isEmpty()) {
					nextPage.setEnabled(true);
				}
				else {
					nextPage.setEnabled(false);
				}
                
                refresh(false);
        return runSummaryList;
    }
    @Override
    public List<RunSummary> getTableCachedData() {
        return runSummaryList;
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
                RunSummary s = (RunSummary) element;

                switch (columnIndex) {
                case NAME_COL:
                    return s.getDisplayName();
                case LANGUAGE_COL:
                    return s.getLanguage().toString();
                case STATE_COL:
                    return s.getLifecycleState().toString();
                case OWNER_COL:
                    return s.getOwnerUserName();
				case CREATED_COL:
					return (new SimpleDateFormat("dd-M-yyyy hh:mm:ss")).format(s.getTimeCreated());
				case DURATION_COL:
                    return formatSecondDateTime(s.getRunDurationInMilliseconds()/1000);
				case TOTAL_OCPU_COL:
                    return s.getTotalOCpu().toString();
				case DATA_WRITTEN_COL:
                    return humanReadableByteCountSI(s.getDataWrittenInBytes());
				case DATA_READ_COL:
                    return humanReadableByteCountSI(s.getDataReadInBytes());
                }
            } catch (Exception ex) {
                MessageDialog.openError(getShell(), "Unable to form table", ex.getMessage());
            }
            return "";
        }
    }

    @Override
    protected void createColumns(TableColumnLayout tableColumnLayout, Table tree) {
    	
    	tree.setSortDirection(SWT.UP);
    	
        TableColumn tc;
        
        tc=createColumn(tableColumnLayout,tree, "Name", 15);
        
        tc.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
            	pagetoshow=null;
                sortBy=ListRunsRequest.SortBy.DisplayName;
                sortOrder = changeSortOrder(sortOrder);
                refresh(true);
              }
            });
        
        
        tc=createColumn(tableColumnLayout,tree, "Language", 6);
        
        tc.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
            	pagetoshow=null;
            	sortBy=ListRunsRequest.SortBy.Language;
            	sortOrder = changeSortOrder(sortOrder);
                refresh(true);
              }
            });
        
        
        tc=createColumn(tableColumnLayout,tree, "State", 8);
        
        tc.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
            	pagetoshow=null;
            	sortBy=ListRunsRequest.SortBy.LifecycleState;
            	sortOrder = changeSortOrder(sortOrder);
                refresh(true);
              }
            });
        
        
        createColumn(tableColumnLayout,tree, "Owner", 15);
        
        
        tc=createColumn(tableColumnLayout,tree, "Created", 10);
        
        tc.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
            	pagetoshow=null;
            	sortBy=ListRunsRequest.SortBy.TimeCreated;
            	sortOrder = changeSortOrder(sortOrder);
                refresh(true);
              }
            });
        
        
        tc=createColumn(tableColumnLayout,tree, "Duration", 5);
        
        tc.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
            	pagetoshow=null;
            	sortBy=ListRunsRequest.SortBy.RunDurationInMilliseconds;
            	sortOrder = changeSortOrder(sortOrder);
                refresh(true);
              }
            });
        
        
        tc=createColumn(tableColumnLayout,tree, "Total OCPU", 5);
        
        tc.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
            	pagetoshow=null;
            	sortBy=ListRunsRequest.SortBy.TotalOCpu;
            	sortOrder = changeSortOrder(sortOrder);
                refresh(true);
              }
            });
        
        
        tc=createColumn(tableColumnLayout,tree, "Data Written", 10);
        
        tc.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
            	pagetoshow=null;
            	sortBy=ListRunsRequest.SortBy.DataWrittenInBytes;
            	sortOrder = changeSortOrder(sortOrder);
                refresh(true);
              }
            });
        
        
        tc=createColumn(tableColumnLayout,tree, "Data Read", 10);
        
        tc.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
            	pagetoshow=null;
            	sortBy=ListRunsRequest.SortBy.DataReadInBytes;
            	sortOrder = changeSortOrder(sortOrder);
                refresh(true);
              }
            });
    }

    @Override
    protected void fillMenu(IMenuManager manager) {
        if (getSelectedObjects().size() == 1) {
            manager.add(new ShowLogsAction(((RunSummary)getSelectedObjects().get(0))));
            manager.add(new DetailsRunAction(RunTable.this));
			manager.add(new RunAction((RunSummary)getSelectedObjects().get(0),RunTable.this));
			manager.add(new OpenInConsoleAction(((RunSummary)getSelectedObjects().get(0)).getId()));
			String lcs=((RunSummary)getSelectedObjects().get(0)).getLifecycleState().toString();
			if(lcs.equals("Failed")||lcs.equals("Succeeded")||lcs.equals("Canceling")||lcs.equals("Canceled"));
			else manager.add(new DeleteRunAction((RunSummary)getSelectedObjects().get(0),RunTable.this));
        }

    }
	
	@Override
    protected void addTableLabels(FormToolkit toolkit, Composite left, Composite right) {
		
		Link link = new Link(right, SWT.NONE);
        Region region = AuthProvider.getInstance().getRegion();
        link.setText("<a href=\"https://console."+region.getRegionId()+".oraclecloud.com/data-flow/runs\">Click to open in console</a>");
         
        // Event handling when users click on links.
        link.addSelectionListener(new SelectionAdapter()  {
         
            @Override
            public void widgetSelected(SelectionEvent e) {
                Program.launch("https://console."+region.getRegionId()+".oraclecloud.com/data-flow/runs");
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
                
				pagetoshow=listrunsresponse.getOpcNextPage();
				refresh(true);
				previousPage.setEnabled(true);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}
        });
        
        previousPage.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                
				pagetoshow=listrunsresponse.getOpcPrevPage();
				refresh(true);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}
        });
        
    }

	public void setSortBy(ListRunsRequest.SortBy sortBy) {
		this.sortBy=sortBy;
	}
	
	public void setSortOrder(ListRunsRequest.SortOrder sortOrder) {
		this.sortOrder=sortOrder;
	}
	
	public static String humanReadableByteCountSI(long bytes) {
	    if (-1000 < bytes && bytes < 1000) {
	        return bytes + " B";
	    }
	    CharacterIterator ci = new StringCharacterIterator("kMGTPE");
	    while (bytes <= -999_950 || bytes >= 999_950) {
	        bytes /= 1000;
	        ci.next();
	    }
	    return String.format("%.1f %cB", bytes / 1000.0, ci.current());
	}
	
	public static String formatSecondDateTime(long second) {
        if(second <= 0)return "";
        long h = second / 3600;
        long m = second % 3600 / 60;
        long s = second % 60;
        String value ="";
        if(h!=0)
        	value += h + "h";
        if(m!=0)
        	value += m + "m";
        value+= s + "s";
        return value;
    }
	
	public static ListRunsRequest.SortOrder changeSortOrder(ListRunsRequest.SortOrder sortOrder) {		
		if(sortOrder==ListRunsRequest.SortOrder.Desc) 
        	sortOrder=ListRunsRequest.SortOrder.Asc;
        else 
        	sortOrder=ListRunsRequest.SortOrder.Desc;		
		return sortOrder;
    }

}