package com.oracle.oci.eclipse.ui.explorer.dataflow.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import com.oracle.bmc.dataflow.model.RunSummary;
import com.oracle.oci.eclipse.sdkclients.DataflowClient;

public class RunLogWizard extends Wizard implements INewWizard{

	private IStructuredSelection selection;
	private RunLogWizardPage page;
	private RunSummary runSum;
	protected boolean canFinish=true;
	
	public RunLogWizard(RunSummary runSum) {
		super();
		this.runSum=runSum;
		setNeedsProgressMonitor(true);
	}
	
	@Override
    public void addPages() {	   
	   page=new RunLogWizardPage(selection,runSum.getId());
	   addPage(page);
    }
	
	@Override
	public boolean performFinish() {
		DataflowClient.getInstance().downloadRunLog(runSum,page.getSelectedLogs());
		return false;
	}
	
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
		
	}
	
	@Override
	public boolean canFinish() {
		return canFinish;
	}
}
