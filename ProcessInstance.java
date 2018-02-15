import java.util.*;
import java.lang.ClassLoader;

public class ProcessInstance {

   String workflowName;
   Map<String,String> processVariables = new HashMap<String,String>();
   Map<String,String> localVariables = new HashMap<String,String>();
   BPMNProcessDefinition processdef;
   String status;
   String userTaskId;

   // This is the constructor of the class Employee
   public ProcessInstance(String workflowName, Map<String,String> processVariables) {
      this.workflowName = workflowName;
      this.processVariables.putAll(processVariables);
      this.processdef = extractProcessDefinition(workflowName);
      this.status = "in-progress";
      this.execute(this.processdef, this.processVariables, this.localVariables, null)
   }

   // Assign the age of the Employee  to the variable age.
   public void execute(String currentFlowObjectId) {
      if (currentFlowObjectId == null) {
         String startId = this.processdef.getStartNode();
         String nextFlowObjectId = this.processdef.getNextFlowObjectId(startId);
         execute(nextFlowObjectId);
      } else {
         String Nodetype = this.processdef.getNodetype(currentFlowObjectId);
         if (Nodetype == "script") {
           this.scriptExecution(currentFlowObjectId);
         } else if(Nodetype == "service") {

         } else if(Nodetype == "user") {
           this.UserTaskExecution(currentFlowObjectId);
         } else if(Nodetype == "exclusive") {

         }
      }
   }

   public void scriptExecution(String currentFlowObjectId) {
     BPMNFlowObject scriptTask = this.processdef.getFlowObjectById(currentFlowObjectId);
     if (!scriptTask.isInlineScript) {
       String className = scriptTask.getJavaClassName();
       ClassLoader classLoader = MainClass.class.getClassLoader();
           try {
               Class  scriptClass = classLoader.loadClass(className);
           } catch (ClassNotFoundException e) {
               e.printStackTrace();
           }

       Map<String,String> result = scriptClass.executeScript(this.processVariables, this.localVariables);
       this.localVariables.clear();
       this.localVariables.putAll(result);
       String nextFlowObjectId = this.processdef.getNextFlowObjectId(currentFlowObjectId);
       this.execute(nextFlowObjectId);
       return;
     }
   }

   public void UserTaskExecution(String currentFlowObjectId) {
     this.status = "user-wait";
     this.userTaskId = currentFlowObjectId;
     return;
   }

   public void UserTaskCompletion(Map<String,String> userVariables) {
     this.processVariables.putAll(userVariables);
     this.localVariables.putAll(userVariables);
     String nextFlowObjectId = this.processdef.getNextFlowObjectId(this.UserTaskId);
     this.execute(nextFlowObjectId);
     return;
   }




}
