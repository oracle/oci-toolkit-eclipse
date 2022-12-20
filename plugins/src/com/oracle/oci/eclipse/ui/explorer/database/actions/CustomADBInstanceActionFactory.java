package com.oracle.oci.eclipse.ui.explorer.database.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.oracle.bmc.database.model.AutonomousDatabaseSummary;
import com.oracle.oci.eclipse.ui.explorer.database.editor.ADBInstanceTable;

public abstract class CustomADBInstanceActionFactory {

   public Optional<CustomADBInstanceAction> create(ADBInstanceTable adbTable) {
       List<AutonomousDatabaseSummary> selectedDatabases = getSelectedDatabases(adbTable);
       return doCreate(selectedDatabases);
   }

   protected abstract Optional<CustomADBInstanceAction> doCreate(List<AutonomousDatabaseSummary> selectedDatabases);

   protected final List<AutonomousDatabaseSummary> getSelectedDatabases(final ADBInstanceTable adbTable)
   {
       List<AutonomousDatabaseSummary> selected = new ArrayList<AutonomousDatabaseSummary>();
       List<?> selectedObjects = adbTable.getSelectedObjects();
       if (selectedObjects != null)
       {
           for (Object obj : selectedObjects)
           {
               if (obj instanceof AutonomousDatabaseSummary)
               {
                   selected.add((AutonomousDatabaseSummary) obj);
               }
           }
       }
       return selected;
   }
}
