public with sharing class SalPerms {
    
    /**
* @author Salesforce.org
* @date 2020
*
* @group ???
*
* @description Creates permissions automatically for SAL users
* 
*/
    
    /*
Anonymous block to kick off all methods creating 5 different permission sets:

====================
START ANONYMOUS BLOCK
====================


SalPerms cp = new SalPerms();
cp.SalBasePermsAdvisor();
cp.SalAdvisorOrAdviseePathwayPerms();
cp.SalBasePermsAdvisee();


====================
END ANONYMOUS BLOCK
====================


*/
    //variable to test with. This variable alters the perm set name so it's unqiue. 
    Integer   curNumlabel = 11;
    
    /*******************************************************************************************************
* @description Method that is used to do the logic of assigning the correct permissions on the permission
* set. This method is used by every other method in this class.
*  
* @param objectsAndCRUD Used to hold the objects that we will be assigning permissions to
* @param PermissionSet The permission set that we are altering
* @param existOP Used for future use cases. This is needed in case we are not inserting, but instead updating. However we will always be inserting with this class
* @param objPerms List used to hold all of the correct object CRUD permissions that we upsert 
* @param existFP Used for future use cases. This is needed in case we are not inserting, but instead updating. However we will always be inserting with this class
* @param fieldsAndCRUD Used to hold the Fields that we will be assigning permissions to
* @param fieldPerms List used to hold all of the correct field Read Edit permissions that we upsert 
* @return dmlWrapper.  always null.
********************************************************************************************************/
    public void logicPerms(Map<String, List<Boolean>> objectsAndCRUD, PermissionSet pr, 
                           Map<String, String> existOP, List<ObjectPermissions> objPerms,  
                           Map<String, String> existFP, Map<String, List<Boolean>> fieldsAndCRUD, 
                           List<FieldPermissions> fieldPerms)
    {       
        for(String objectName :objectsAndCRUD.keySet()) {
            ObjectPermissions objPermission = new ObjectPermissions();
            objPermission.ParentId = pr.Id;
            objPermission.SobjectType = objectName;
            objPermission.PermissionsRead = False;
            objPermission.PermissionsCreate = False;
            objPermission.PermissionsEdit = False;
            objPermission.PermissionsDelete = False;
            if (existOP.get(objectName) != null){
                objPermission.Id = existOP.get(objectName);
                system.debug('existOP:' + objPermission.Id); 
            }
            
            if (objectsAndCRUD.get(objectName)[0] == True)  
            {objPermission.PermissionsRead = true;}
            if (objectsAndCRUD.get(objectName)[1] == True)  
            {objPermission.PermissionsCreate = true;}
            if (objectsAndCRUD.get(objectName)[2] == True)  
            {objPermission.PermissionsEdit = true;}
            if (objectsAndCRUD.get(objectName)[3] == True)
            {objPermission.PermissionsDelete = true;}
            objPerms.add(objPermission);    
        }
        
        for(String fieldName :fieldsAndCRUD.keySet()) {
            FieldPermissions fldPermission = new FieldPermissions();
            fldPermission.ParentId = pr.Id;
            fldPermission.SobjectType = fieldName.substringBefore('.');
            fldPermission.Field = fieldName;
            fldPermission.PermissionsRead = False;
            fldPermission.PermissionsEdit = False;
            if (existFP.get(fieldName) != null){
                fldPermission.Id = existFP.get(fieldName);
                system.debug('existFP:' + fldPermission.Id); 
            }
            if (fieldsAndCRUD.get(fieldName)[0] == True)  
            {fldPermission.PermissionsRead = true;}
            if (fieldsAndCRUD.get(fieldName)[1] == True)  
            {fldPermission.PermissionsEdit = true;}
            fieldPerms.add(fldPermission);  
        }
        
        system.debug('object perms right before upsert:' + objPerms);
        upsert objPerms;
        
        system.debug('field perms right before upsert:' + fieldPerms);
        upsert fieldPerms;
        
    }
    
    
    /*******************************************************************************************************
* @description Method that is used to set the appropriate permissions for the base Avisor permissions found on the link below: 
* https://powerofus.force.com/s/article/SAL-Security-Configure-Advisor-Permissions
* 
* 
* @return Nothing
********************************************************************************************************/
    public void SalBasePermsAdvisor(){
        //Create the permission set and insert it so we can query on it. 
        PermissionSet pr = new PermissionSet(Name='SalAdvisorPermissions' + curNumlabel, Label='Sal Advisor Permissions' + curNumlabel);
        insert pr;
        String permSetName = pr.Id;
        //We query on the Id because we know what the ID will be. This gets us the correct newly created permission set to work with. 
        pr = [SELECT Id, Label, Name, Description FROM PermissionSet WHERE ID = :permSetName];
        system.debug('pr id-' + pr.Id);
        
        //Object Permissions Setup
        List<ObjectPermissions> op = new List<ObjectPermissions>();
        op = [SELECT ID, SObjectType, ParentID FROM ObjectPermissions WHERE ParentId = :pr.Id];
        Map<String, String> existOP = new Map<String, String>();
        //existOP is utilized so we can work on profiles if needed as well as permission sets. 
        for (ObjectPermissions eop : op){
            String objname = eop.SobjectType;
            String objid = eop.Id;
            existOp.put(objname, objid);
        }
        List<ObjectPermissions> objPerms = new List<ObjectPermissions>();
        Map<String, List<Boolean>> objectsAndCRUD = new Map<String, List<Boolean>>();
        List<Map<String, List<Boolean>>> objectsAndCRUDcollection = new List <Map<String, List<Boolean>>>();
        
        //Field Permissions Setup
        List<FieldPermissions> fp = new List<FieldPermissions>();
        fp = [SELECT ID, SObjectType, Field, ParentID FROM FieldPermissions WHERE ParentId = :pr.Id];
        Map<String, String> existFP = new Map<String, String>();
        for (FieldPermissions efp : fp){
            String fldname = efp.Field;
            String fldid = efp.Id;
            existFp.put(fldname, fldid);
        }
        List<FieldPermissions> fieldPerms = new List<FieldPermissions>();
        Map<String, List<Boolean>> fieldsAndCRUD = new Map<String, List<Boolean>>();
        List<Map<String, List<Boolean>>> FieldsAndFSLcollection = new List <Map<String, List<Boolean>>>();
        
        
        //========================================================
        //ENTER YOUR OBJECTS and CRUD ACCESS BELOW
        //========================================================   
        
        //READ - CREATE - EDIT - DELETE
        objectsAndCRUD.put('Account', new List<Boolean>{True, False, False, False});
        objectsAndCRUD.put('sfal__AdvisingPool__c', new List<Boolean>{True, True, True, False});
        objectsAndCRUD.put('sfal__Alert__c', new List<Boolean>{True, False, False, False});
        objectsAndCRUD.put('sfal__Appointment__c', new List<Boolean>{True, True, True, False});
        objectsAndCRUD.put('sfal__AppointmentAttendee__c', new List<Boolean>{True, True, True, False});
        objectsAndCRUD.put('Case', new List<Boolean>{True, True, True, False});
        objectsAndCRUD.put('Contact', new List<Boolean>{True, False, False, False});
        objectsAndCRUD.put('hed__Course__c', new List<Boolean>{True, False, False, False});
        objectsAndCRUD.put('hed__Course_Enrollment__c', new List<Boolean>{True, False, False, False});
        objectsAndCRUD.put('hed__Term__c', new List<Boolean>{True, False, False, False});
        objectsAndCRUD.put('hed__Course_Offering__c', new List<Boolean>{True, False, False, False});
        objectsAndCRUD.put('hed__Facility__c', new List<Boolean>{True, False, True, False});
        //???????Prediction Permissions?????????
        objectsAndCRUD.put('hed__Program_Enrollment__c', new List<Boolean>{True, False, True, False});
        //queueTopicSetting depends on topic so we do Topic first 
        objectsAndCRUD.put('sfal__Topic__c', new List<Boolean>{True, False, True, False});
        objectsAndCRUD.put('sfal__QueueTopicSetting__c', new List<Boolean>{True, False, False, False});
        objectsAndCRUD.put('sfal__QueueWaitingRoomResource__c', new List<Boolean>{True, False, True, False});
        objectsAndCRUD.put('hed__Relationship__c', new List<Boolean>{True, False, False, False}); //This one doesn't seem right. it grants read access to NPSP relationships. Maybe thats correct though. just an odd name when you view it in the permission set itself I guess.  
        objectsAndCRUD.put('sfal__RoleTopicSetting__c', new List<Boolean>{True, False, False, False});
        objectsAndCRUD.put('sfal__SuccessPlan__c', new List<Boolean>{True, True, True, False});
        objectsAndCRUD.put('sfal__SuccessPlanTemplate__c', new List<Boolean>{True, True, True, False});
        objectsAndCRUD.put('sfal__SuccessPlanTemplateTask__c', new List<Boolean>{True, True, True, False});
        //objectsAndCRUD.put('Task', new List<Boolean>{True, False, True, False});
        objectsAndCRUD.put('sfal__UserTopicSetting__c', new List<Boolean>{True, True, True, False});
        
        
        
        
        
        
        
        
        objectsAndCRUDcollection.add(objectsAndCRUD);
        system.debug('objectsAndCRUDcollection after we insert two objects' + objectsAndCRUDcollection);
        
        //========================================================
        //ENTER YOUR FIELD AND READ/EDIT ACCESS BELOW
        //========================================================   
        
        fieldsAndCRUD.put('Account.sfal__CaseTeamTemplateName__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('Account.Description', new List<Boolean>{True, False});
        fieldsAndCRUD.put('Account.Parent', new List<Boolean>{True, False});
        //      System.DmlException: Upsert failed. First exception on row 3; first error: INVALID_OR_NULL_FOR_RESTRICTED_PICKLIST, 
        //      Field Name: bad value for restricted picklist field: Account.RecordType: [Field] - Kolls note on this - I think its just already granted so its a doc bug/fix. 
        //  fieldsAndCRUD.put('Account.RecordType', new List<Boolean>{True, False});
        
        fieldsAndCRUD.put('sfal__AdvisingPool__c.sfal__Account__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('sfal__AdvisingPool__c.sfal__CaseTeamName__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('sfal__AdvisingPool__c.sfal__Description__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('sfal__AdvisingPool__c.sfal__PhotoURL__c', new List<Boolean>{True, True});
        //System.DmlException: Upsert failed. First exception on row 7; first error: INVALID_OR_NULL_FOR_RESTRICTED_PICKLIST,
        // Field Name: bad value for restricted picklist field: sfal__AdvisingPool__c.Name: [Field]
        // fieldsAndCRUD.put('sfal__AdvisingPool__c.Name', new List<Boolean>{True, True});
        
        fieldsAndCRUD.put('sfal__Alert__c.sfal__IsClosed__c ', new List<Boolean>{True, False});
        
        fieldsAndCRUD.put('sfal__Appointment__c.sfal__AdditionalConnectionInformation__c', new List<Boolean>{True, True});
        // System.DmlException: Upsert failed. First exception on row 9; first error: INVALID_OR_NULL_FOR_RESTRICTED_PICKLIST, 
        //  Field Name: bad value for restricted picklist field: sfal__Appointment__c.Name: [Field]
        //  fieldsAndCRUD.put('sfal__Appointment__c.Name', new List<Boolean>{True, True});
        fieldsAndCRUD.put('sfal__Appointment__c.sfal__Description__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('sfal__Appointment__c.sfal__Location__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('sfal__Appointment__c.sfal__RelatedCase__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('sfal__Appointment__c.sfal__RelatedSubtopic__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('sfal__Appointment__c.sfal__RelatedTopic__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('sfal__Appointment__c.sfal__Subtopic__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('sfal__Appointment__c.sfal__Topic__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('sfal__Appointment__c.sfal__Type__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('sfal__Appointment__c.sfal__IsWebMeeting__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('sfal__Appointment__c.sfal__WebMeetingLink__c', new List<Boolean>{True, True});
        
        fieldsAndCRUD.put('sfal__AppointmentAttendee__c.sfal__AdviseeRecord__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('sfal__AppointmentAttendee__c.sfal__Attendee__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('sfal__AppointmentAttendee__c.sfal__Comments__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('sfal__AppointmentAttendee__c.sfal__Role__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('sfal__AppointmentAttendee__c.sfal__StatusComments__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('sfal__AppointmentAttendee__c.sfal__StatusUpdatedBy__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('sfal__AppointmentAttendee__c.sfal__StatusUpdatedDate__c', new List<Boolean>{True, True});
        
        fieldsAndCRUD.put('Case.sfal__AdvisingPool__c', new List<Boolean>{True, True});
        //System.DmlException: Upsert failed. First exception on row 26; first error: INVALID_OR_NULL_FOR_RESTRICTED_PICKLIST,
        // Field Name: bad value for restricted picklist field: Case.Owner: [Field]
        //   fieldsAndCRUD.put('Case.Owner', new List<Boolean>{True, True});
        fieldsAndCRUD.put('Case.Contact', new List<Boolean>{True, False});
        fieldsAndCRUD.put('Case.Description', new List<Boolean>{True, True});
        fieldsAndCRUD.put('Case.sfal__Location__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('Case.sfal__Proxy__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('Case.sfal__ProxyName__c', new List<Boolean>{True, True});
        //System.DmlException: Upsert failed. First exception on row 31; first error: INVALID_OR_NULL_FOR_RESTRICTED_PICKLIST, Field Name: bad value for restricted picklist field: Case.Status: [Field]
        
        
        // fieldsAndCRUD.put('Case.Status', new List<Boolean>{True, True});
        fieldsAndCRUD.put('Case.sfal__StudentID__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('Case.sfal__Subtopic__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('Case.sfal__Topic__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('Case.SuppliedEmail', new List<Boolean>{True, True});
        fieldsAndCRUD.put('Case.SuppliedName', new List<Boolean>{True, True});
        fieldsAndCRUD.put('Case.SuppliedPhone', new List<Boolean>{True, True});
        
        fieldsAndCRUD.put('Contact.hed__Chosen_Full_Name__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('Contact.Email', new List<Boolean>{True, False});
        //System.DmlException: Upsert failed. First exception on row 39; first error: INVALID_OR_NULL_FOR_RESTRICTED_PICKLIST, Field Name: bad value for restricted picklist field: Contact.Name: [Field]
        //        fieldsAndCRUD.put('Contact.Name', new List<Boolean>{True, False});
        fieldsAndCRUD.put('Contact.Primary_Academic_Program__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('Contact.Primary_Department__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('Contact.Student_ID__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('Contact.Title', new List<Boolean>{True, False});
        
        fieldsAndCRUD.put('hed__Course__c.hed__Course_ID__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('hed__Course__c.hed__Credit_Hours__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('hed__Course__c.hed__Description__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('hed__Course__c.hed__Extended_Description__c', new List<Boolean>{True, False});
        
        
        fieldsAndCRUD.put('Event.Description', new List<Boolean>{True, True});
        fieldsAndCRUD.put('Event.Location', new List<Boolean>{True, True});
        fieldsAndCRUD.put('Event.IsVisibleInSelfService ', new List<Boolean>{True, True});
        fieldsAndCRUD.put('Event.What', new List<Boolean>{True, True});
        fieldsAndCRUD.put('Event.Type', new List<Boolean>{True, True});
        //for some reason event.name is being granted read and edit access. No idea why six fields get access when we only grant five. Plus we can manually remove event.name without error in the perm set.
        
        
        fieldsAndCRUD.put('hed__Term__c.hed__Start_Date__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('hed__Term__c.hed__End_Date__c', new List<Boolean>{True, False});
        
        fieldsAndCRUD.put('hed__Facility__c.hed__Description__c', new List<Boolean>{True, True});
        //System.DmlException: Upsert failed. First exception on row 55; first error: INVALID_OR_NULL_FOR_RESTRICTED_PICKLIST, 
        //Field Name: bad value for restricted picklist field: hed__Facility__c.Name: [Field]
        // fieldsAndCRUD.put('hed__Facility__c.Name', new List<Boolean>{True, True});
        
        
        //???????Prediction Permissions?????????
        
        fieldsAndCRUD.put('hed__Program_Enrollment__c.hed__Graduation_Year__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('hed__Program_Enrollment__c.hed__Contact__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('hed__Program_Enrollment__c.hed__GPA__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('hed__Program_Enrollment__c.hed__Account__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('hed__Program_Enrollment__c.hed__Program_Plan__c', new List<Boolean>{True, False});
        
        fieldsAndCRUD.put('sfal__Topic__c.sfal__Label__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('sfal__Topic__c.sfal__ParentTopic__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('sfal__Topic__c.sfal__SortOrder__c', new List<Boolean>{True, False});
        
        //System.DmlException: Upsert failed. First exception on row 63; first error: INVALID_OR_NULL_FOR_RESTRICTED_PICKLIST, 
        //Field Name: bad value for restricted picklist field: sfal__QueueTopicSetting__c.sfal__AdvisingPool__c: [Field]
        // fieldsAndCRUD.put('sfal__QueueTopicSetting__c.sfal__AdvisingPool__c', new List<Boolean>{True, True});
        // System.DmlException: Upsert failed. First exception on row 63; first error: 
        // INVALID_OR_NULL_FOR_RESTRICTED_PICKLIST, Field Name: bad value for restricted picklist field: sfal__QueueTopicSetting__c.sfal__Topic__c: [Field]
        // fieldsAndCRUD.put('sfal__QueueTopicSetting__c.sfal__Topic__c', new List<Boolean>{True, True});
        
        fieldsAndCRUD.put('sfal__QueueWaitingRoomResource__c.sfal__AdvisingPool__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('sfal__QueueWaitingRoomResource__c.sfal__SortOrder__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('sfal__QueueWaitingRoomResource__c.sfal__User__c', new List<Boolean>{True, True});
        //System.DmlException: Upsert failed. First exception on row 66; first error: 
        //INVALID_OR_NULL_FOR_RESTRICTED_PICKLIST, Field Name: bad value for restricted picklist field: sfal__QueueWaitingRoomResource__c.Name: [Field]
        //fieldsAndCRUD.put('sfal__QueueWaitingRoomResource__c.Name', new List<Boolean>{True, False});
        // Doc says "waiting room" for this object but i dont see this field??? Doc bug? it might mean facilities???
        
        //System.DmlException: Upsert failed. First exception on row 66; first error: 
        //INVALID_OR_NULL_FOR_RESTRICTED_PICKLIST, Field Name: bad value for restricted picklist field: sfal__RoleTopicSetting__c.sfal__RoleName__c: [Field]
        //fieldsAndCRUD.put('sfal__RoleTopicSetting__c.sfal__RoleName__c', new List<Boolean>{True, False});
        ////System.DmlException: Upsert failed. First exception on row 66; first error: INVALID_OR_NULL_FOR_RESTRICTED_PICKLIST, 
        //Field Name: bad value for restricted picklist field: sfal__RoleTopicSetting__c.sfal__Topic__c: [Field]
        //fieldsAndCRUD.put('sfal__RoleTopicSetting__c.sfal__Topic__c', new List<Boolean>{True, False});
        
        //System.DmlException: Upsert failed. First exception on row 66; 
        //first error: INVALID_OR_NULL_FOR_RESTRICTED_PICKLIST, Field Name: bad value for restricted picklist field: sfal__SuccessPlan__c.sfal__Advisee__c: [Field]
        //fieldsAndCRUD.put('sfal__SuccessPlan__c.sfal__Advisee__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('sfal__SuccessPlan__c.sfal__AdviseeRecord__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('sfal__SuccessPlan__c.sfal__AutoApply__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('sfal__SuccessPlan__c.sfal__IsCancelled__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('sfal__SuccessPlan__c.sfal__Comments__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('sfal__SuccessPlan__c.sfal__FromTemplate__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('sfal__SuccessPlan__c.sfal__IsCaseAdviseeRecord__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('sfal__SuccessPlan__c.sfal__NumberOfOpenTasks__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('sfal__SuccessPlan__c.sfal__NumberOfOverdueTasks__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('sfal__SuccessPlan__c.sfal__IsPublished__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('sfal__SuccessPlan__c.sfal__Status__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('sfal__SuccessPlan__c.sfal__NumberOfTasks__c', new List<Boolean>{True, False});
        
        fieldsAndCRUD.put('sfal__SuccessPlanTemplate__c.sfal__Active__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('sfal__SuccessPlanTemplate__c.sfal__TasksActive__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('sfal__SuccessPlanTemplate__c.sfal__Comments__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('sfal__SuccessPlanTemplate__c.sfal__UsageCount__c', new List<Boolean>{True, False});
        
        
        fieldsAndCRUD.put('sfal__SuccessPlanTemplateTask__c.sfal__Active__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('sfal__SuccessPlanTemplateTask__c.sfal__Comments__c', new List<Boolean>{True, True});
        //System.DmlException: Upsert failed. First exception on row 83; first error: 
        //INVALID_OR_NULL_FOR_RESTRICTED_PICKLIST, Field Name: bad value for restricted picklist field: sfal__SuccessPlanTemplateTask__c.sfal__Priority__c: [Field]
        // fieldsAndCRUD.put('sfal__SuccessPlanTemplateTask__c.sfal__Priority__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('sfal__SuccessPlanTemplateTask__c.sfal__RelativeDueDate__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('sfal__SuccessPlanTemplateTask__c.sfal__Type__c', new List<Boolean>{True, True});
        
        //Task is also granted extra access. I think this is by design though if some of the tasks parent objects fields already have access? 
        fieldsAndCRUD.put('Task.ActivityDate', new List<Boolean>{True, True});
        fieldsAndCRUD.put('Task.Who', new List<Boolean>{True, True});
        fieldsAndCRUD.put('Task.What', new List<Boolean>{True, True});
        
        
        fieldsAndCRUD.put('sfal__UserTopicSetting__c.sfal__DefaultAppointmentDuration__c', new List<Boolean>{True, True});
        //System.DmlException: Upsert failed. First exception on row 89; first error: INVALID_OR_NULL_FOR_RESTRICTED_PICKLIST, 
        //Field Name: bad value for restricted picklist field: sfal__UserTopicSetting__c.sfal__Topic__c: [Field]
        // fieldsAndCRUD.put('sfal__UserTopicSetting__c.sfal__Topic__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('sfal__UserTopicSetting__c.sfal__User__c', new List<Boolean>{True, True});
        
        FieldsAndFSLcollection.add(fieldsAndCRUD);
        //no idea currently how to grant perm set system permissons. Not gonna invest much time into this one
        //pr.EditEvents = True; 
        //pr.EditActivites = True;
        //
        //Call the method to preform the logic of inserting the permissions based on the above object and field level declarations
        logicPerms(objectsAndCRUD, pr, existOP, objPerms, existFP, fieldsAndCRUD, fieldPerms);
        
        
    }
    
    
    
    
    
    
    /*******************************************************************************************************
* @description Method that is used to set the appropriate permissions for the base Avisor permissions found on the link below: 
* https://powerofus.force.com/s/article/SAL-Security-Configure-Advisee-Permissions
* 
* 
* @return Nothing
********************************************************************************************************/
    public void SalBasePermsAdvisee(){
        //Create the permission set and insert it so we can query on it. 
        PermissionSet pr = new PermissionSet(Name='SalAdviseePermissions' + curNumlabel, Label='Sal Advisee Permissions' + curNumlabel);
        insert pr;
        String permSetName = pr.Id;
        //We query on the Id because we know what the ID will be. This gets us the correct newly created permission set to work with. 
        pr = [SELECT Id, Label, Name, Description FROM PermissionSet WHERE ID = :permSetName];
        system.debug('pr id-' + pr.Id);
        
        //Object Permissions Setup
        List<ObjectPermissions> op = new List<ObjectPermissions>();
        op = [SELECT ID, SObjectType, ParentID FROM ObjectPermissions WHERE ParentId = :pr.Id];
        Map<String, String> existOP = new Map<String, String>();
        //existOP is utilized so we can work on profiles if needed as well as permission sets. 
        for (ObjectPermissions eop : op){
            String objname = eop.SobjectType;
            String objid = eop.Id;
            existOp.put(objname, objid);
        }
        List<ObjectPermissions> objPerms = new List<ObjectPermissions>();
        Map<String, List<Boolean>> objectsAndCRUD = new Map<String, List<Boolean>>();
        List<Map<String, List<Boolean>>> objectsAndCRUDcollection = new List <Map<String, List<Boolean>>>();
        
        //Field Permissions Setup
        List<FieldPermissions> fp = new List<FieldPermissions>();
        fp = [SELECT ID, SObjectType, Field, ParentID FROM FieldPermissions WHERE ParentId = :pr.Id];
        Map<String, String> existFP = new Map<String, String>();
        for (FieldPermissions efp : fp){
            String fldname = efp.Field;
            String fldid = efp.Id;
            existFp.put(fldname, fldid);
        }
        List<FieldPermissions> fieldPerms = new List<FieldPermissions>();
        Map<String, List<Boolean>> fieldsAndCRUD = new Map<String, List<Boolean>>();
        List<Map<String, List<Boolean>>> FieldsAndFSLcollection = new List <Map<String, List<Boolean>>>();
        
        
        //========================================================
        //ENTER YOUR OBJECTS and CRUD ACCESS BELOW
        //========================================================   
        
        //READ - CREATE - EDIT - DELETE
        objectsAndCRUD.put('Account', new List<Boolean>{True, False, False, False});
        objectsAndCRUD.put('sfal__AdvisingPool__c', new List<Boolean>{True, False, False, False});
        objectsAndCRUD.put('sfal__Appointment__c', new List<Boolean>{True, True, True, False});
        objectsAndCRUD.put('sfal__AppointmentAttendee__c', new List<Boolean>{True, True, True, False});
        objectsAndCRUD.put('Case', new List<Boolean>{True, False, False, False});
        objectsAndCRUD.put('Contact', new List<Boolean>{True, False, False, False});
        objectsAndCRUD.put('hed__Course__c', new List<Boolean>{True, False, False, False});
        objectsAndCRUD.put('hed__Course_Enrollment__c', new List<Boolean>{True, False, False, False});
        objectsAndCRUD.put('hed__Term__c', new List<Boolean>{True, False, False, False});
        objectsAndCRUD.put(' hed__Course_Offering_Schedule__c', new List<Boolean>{True, False, False, False});
        objectsAndCRUD.put('hed__Course_Offering__c', new List<Boolean>{True, False, False, False});
        
        
        objectsAndCRUD.put('hed__Facility__c', new List<Boolean>{True, False, False, False});
        //???????Prediction Permissions?????????
        objectsAndCRUD.put('hed__Program_Enrollment__c', new List<Boolean>{True, False, False, False});
        //queueTopicSetting depends on topic so we do Topic first 
        objectsAndCRUD.put('sfal__Topic__c', new List<Boolean>{True, False, False, False});
        objectsAndCRUD.put('sfal__QueueTopicSetting__c', new List<Boolean>{True, False, False, False});
        objectsAndCRUD.put('sfal__QueueWaitingRoomResource__c', new List<Boolean>{True, False, True, False});
        objectsAndCRUD.put('hed__Relationship__c', new List<Boolean>{True, False, False, False}); //This one doesn't seem right. it grants read access to NPSP relationships. Maybe thats correct though. just an odd name when you view it in the permission set itself I guess.  
        objectsAndCRUD.put('sfal__RoleTopicSetting__c', new List<Boolean>{True, False, False, False});
        objectsAndCRUD.put('sfal__SuccessPlan__c', new List<Boolean>{True, False, False, False});
        objectsAndCRUD.put('sfal__SuccessPlanTemplate__c', new List<Boolean>{True, True, True, False});
        objectsAndCRUD.put('sfal__SuccessPlanTemplateTask__c', new List<Boolean>{True, True, True, False});
        //objectsAndCRUD.put('Task', new List<Boolean>{True, False, True, False});
        objectsAndCRUD.put('sfal__UserTopicSetting__c', new List<Boolean>{True, False, False, False});
        
        
        
        
        
        
        
        
        objectsAndCRUDcollection.add(objectsAndCRUD);
        system.debug('objectsAndCRUDcollection after we insert two objects' + objectsAndCRUDcollection);
        
        //========================================================
        //ENTER YOUR FIELD AND READ/EDIT ACCESS BELOW
        //========================================================   
        
        fieldsAndCRUD.put('Account.sfal__CaseTeamTemplateName__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('Account.Description', new List<Boolean>{True, False});
        fieldsAndCRUD.put('Account.Parent', new List<Boolean>{True, False});
        //      System.DmlException: Upsert failed. First exception on row 3; first error: INVALID_OR_NULL_FOR_RESTRICTED_PICKLIST, 
        //      Field Name: bad value for restricted picklist field: Account.RecordType: [Field] - Kolls note on this - I think its just already granted so its a doc bug/fix. 
        //  fieldsAndCRUD.put('Account.RecordType', new List<Boolean>{True, False});
        
        fieldsAndCRUD.put('sfal__AdvisingPool__c.sfal__CaseTeamName__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('sfal__AdvisingPool__c.sfal__Description__c', new List<Boolean>{True, False});
        //System.DmlException: Upsert failed. First exception on row 7; first error: INVALID_OR_NULL_FOR_RESTRICTED_PICKLIST,
        // Field Name: bad value for restricted picklist field: sfal__AdvisingPool__c.Name: [Field]
        // fieldsAndCRUD.put('sfal__AdvisingPool__c.Name', new List<Boolean>{True, True});
        
        
        fieldsAndCRUD.put('sfal__Appointment__c.sfal__AdditionalConnectionInformation__c', new List<Boolean>{True, False});
        // System.DmlException: Upsert failed. First exception on row 9; first error: INVALID_OR_NULL_FOR_RESTRICTED_PICKLIST, 
        //  Field Name: bad value for restricted picklist field: sfal__Appointment__c.Name: [Field]
        //  fieldsAndCRUD.put('sfal__Appointment__c.Name', new List<Boolean>{True, True});
        fieldsAndCRUD.put('sfal__Appointment__c.sfal__Description__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('sfal__Appointment__c.sfal__Location__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('sfal__Appointment__c.sfal__RelatedSubtopic__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('sfal__Appointment__c.sfal__RelatedTopic__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('sfal__Appointment__c.sfal__Subtopic__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('sfal__Appointment__c.sfal__Topic__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('sfal__Appointment__c.sfal__Type__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('sfal__Appointment__c.sfal__IsWebMeeting__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('sfal__Appointment__c.sfal__WebMeetingLink__c', new List<Boolean>{True, False});
        
        
        
        fieldsAndCRUD.put('sfal__AppointmentAttendee__c.sfal__AdviseeRecord__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('sfal__AppointmentAttendee__c.sfal__Attendee__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('sfal__AppointmentAttendee__c.sfal__Comments__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('sfal__AppointmentAttendee__c.sfal__Role__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('sfal__AppointmentAttendee__c.sfal__StatusComments__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('sfal__AppointmentAttendee__c.sfal__StatusUpdatedBy__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('sfal__AppointmentAttendee__c.sfal__StatusUpdatedDate__c', new List<Boolean>{True, True});
        
        
        
        fieldsAndCRUD.put('Case.Contact', new List<Boolean>{True, False});
        

        
        
        fieldsAndCRUD.put('Contact.hed__Chosen_Full_Name__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('Contact.Email', new List<Boolean>{True, False});
        fieldsAndCRUD.put('Contact.Primary_Academic_Program__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('Contact.Primary_Department__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('Contact.Student_ID__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('Contact.Title', new List<Boolean>{True, False});
        
        
        
        fieldsAndCRUD.put('hed__Course__c.hed__Course_ID__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('hed__Course__c.hed__Credit_Hours__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('hed__Course__c.hed__Description__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('hed__Course__c.hed__Extended_Description__c', new List<Boolean>{True, False});
        
        
        fieldsAndCRUD.put('hed__Course_Enrollment__c.hed__Contact__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('hed__Course_Enrollment__c.hed__Course_Offering__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('hed__Course_Enrollment__c.hed__Status__c', new List<Boolean>{True, False});

        
        
        fieldsAndCRUD.put('Event.IsAllDayEvent', new List<Boolean>{True, False});
        fieldsAndCRUD.put('Event.Description', new List<Boolean>{True, True});
        fieldsAndCRUD.put('Event.Location', new List<Boolean>{True, True});
        fieldsAndCRUD.put('Event.Who', new List<Boolean>{True, False});
        fieldsAndCRUD.put('Event.IsVisibleInSelfService ', new List<Boolean>{True, True});
        fieldsAndCRUD.put('Event.What', new List<Boolean>{True, True});
        fieldsAndCRUD.put('Event.Type', new List<Boolean>{True, True});
        
        
        fieldsAndCRUD.put('hed__Term__c.hed__Start_Date__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('hed__Term__c.hed__End_Date__c', new List<Boolean>{True, False});
        
       
        fieldsAndCRUD.put('hed__Course_Offering_Schedule__c.hed__Course_Offering__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('hed__Course_Offering_Schedule__c.hed__End_Time__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('hed__Course_Offering_Schedule__c.hed__Facility__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('hed__Course_Offering_Schedule__c.hed__Friday__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('hed__Course_Offering_Schedule__c.hed__Monday__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('hed__Course_Offering_Schedule__c.hed__Saturday__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('hed__Course_Offering_Schedule__c.hed__Start_Time__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('hed__Course_Offering_Schedule__c.hed__Sunday__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('hed__Course_Offering_Schedule__c.hed__Thursday__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('hed__Course_Offering_Schedule__c.hed__Tuesday__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('hed__Course_Offering_Schedule__c.hed__Wednesday__c', new List<Boolean>{True, False});

        
        //System.DmlException: Upsert failed. First exception on row 42; first error: INVALID_OR_NULL_FOR_RESTRICTED_PICKLIST, 
        //Field Name: bad value for restricted picklist field: hed__Course_Offering__c.hed__Course__c: [Field]
        // fieldsAndCRUD.put('hed__Course_Offering__c.hed__Course__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('hed__Course_Offering__c.hed__End_Date__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('hed__Course_Offering__c.hed__Faculty__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('hed__Course_Offering__c.hed__Start_Date__c', new List<Boolean>{True, False});
        //System.DmlException: Upsert failed. First exception on row 45; first error: INVALID_OR_NULL_FOR_RESTRICTED_PICKLIST,
        // Field Name: bad value for restricted picklist field: hed__Course_Offering__c.hed__Term__c: [Field]

        
        
        //System.DmlException: Upsert failed. First exception on row 55; first error: INVALID_OR_NULL_FOR_RESTRICTED_PICKLIST, 
        //Field Name: bad value for restricted picklist field: hed__Facility__c.Name: [Field]
        // fieldsAndCRUD.put('hed__Facility__c.Name', new List<Boolean>{True, False});
        
        
        //???????Prediction Permissions?????????
        
        fieldsAndCRUD.put('hed__Program_Enrollment__c.hed__Contact__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('hed__Program_Enrollment__c.hed__Account__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('hed__Program_Enrollment__c.hed__Program_Plan__c', new List<Boolean>{True, False});
        
        fieldsAndCRUD.put('sfal__Topic__c.sfal__Label__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('sfal__Topic__c.sfal__ParentTopic__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('sfal__Topic__c.sfal__SortOrder__c', new List<Boolean>{True, False});
        
        //System.DmlException: Upsert failed. First exception on row 63; first error: INVALID_OR_NULL_FOR_RESTRICTED_PICKLIST, 
        //Field Name: bad value for restricted picklist field: sfal__QueueTopicSetting__c.sfal__AdvisingPool__c: [Field]
        // fieldsAndCRUD.put('sfal__QueueTopicSetting__c.sfal__AdvisingPool__c', new List<Boolean>{True, True});
        // System.DmlException: Upsert failed. First exception on row 63; first error: 
        // INVALID_OR_NULL_FOR_RESTRICTED_PICKLIST, Field Name: bad value for restricted picklist field: sfal__QueueTopicSetting__c.sfal__Topic__c: [Field]
        // fieldsAndCRUD.put('sfal__QueueTopicSetting__c.sfal__Topic__c', new List<Boolean>{True, True});
        
        fieldsAndCRUD.put('sfal__QueueWaitingRoomResource__c.sfal__AdvisingPool__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('sfal__QueueWaitingRoomResource__c.sfal__SortOrder__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('sfal__QueueWaitingRoomResource__c.sfal__User__c', new List<Boolean>{True, True});
        //System.DmlException: Upsert failed. First exception on row 66; first error: 
        //INVALID_OR_NULL_FOR_RESTRICTED_PICKLIST, Field Name: bad value for restricted picklist field: sfal__QueueWaitingRoomResource__c.Name: [Field]
        //fieldsAndCRUD.put('sfal__QueueWaitingRoomResource__c.Name', new List<Boolean>{True, False});
        // Doc says "waiting room" for this object but i dont see this field??? Doc bug? it might mean facilities???
        
        
        
        //System.DmlException: Upsert failed. First exception on row 66; first error: 
        //INVALID_OR_NULL_FOR_RESTRICTED_PICKLIST, Field Name: bad value for restricted picklist field: sfal__RoleTopicSetting__c.sfal__RoleName__c: [Field]
        //fieldsAndCRUD.put('sfal__RoleTopicSetting__c.sfal__RoleName__c', new List<Boolean>{True, False});
        ////System.DmlException: Upsert failed. First exception on row 66; first error: INVALID_OR_NULL_FOR_RESTRICTED_PICKLIST, 
        //Field Name: bad value for restricted picklist field: sfal__RoleTopicSetting__c.sfal__Topic__c: [Field]
        //fieldsAndCRUD.put('sfal__RoleTopicSetting__c.sfal__Topic__c', new List<Boolean>{True, False});
        
        
        
        
        //System.DmlException: Upsert failed. First exception on row 66; 
        //first error: INVALID_OR_NULL_FOR_RESTRICTED_PICKLIST, Field Name: bad value for restricted picklist field: sfal__SuccessPlan__c.sfal__Advisee__c: [Field]
        //fieldsAndCRUD.put('sfal__SuccessPlan__c.sfal__Advisee__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('sfal__SuccessPlan__c.sfal__AdviseeRecord__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('sfal__SuccessPlan__c.sfal__IsCancelled__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('sfal__SuccessPlan__c.sfal__Comments__c', new List<Boolean>{True, False});
        //System.DmlException: Upsert failed. First exception on row 69; 
        //first error: INVALID_OR_NULL_FOR_RESTRICTED_PICKLIST, Field Name: bad value for restricted picklist field: sfal__SuccessPlan__c.CreatedBy: [Field]
       // fieldsAndCRUD.put('sfal__SuccessPlan__c.CreatedBy', new List<Boolean>{True, False});
        fieldsAndCRUD.put('sfal__SuccessPlan__c.sfal__IsCaseAdviseeRecord__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('sfal__SuccessPlan__c.sfal__NumberOfOpenTasks__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('sfal__SuccessPlan__c.sfal__NumberOfOverdueTasks__c', new List<Boolean>{True, False});
        //System.DmlException: Upsert failed. First exception on row 72; 
        //first error: INVALID_OR_NULL_FOR_RESTRICTED_PICKLIST, Field Name: bad value for restricted picklist field: sfal__SuccessPlan__c.Owner: [Field]
        //fieldsAndCRUD.put('sfal__SuccessPlan__c.Owner', new List<Boolean>{True, False});
        fieldsAndCRUD.put('sfal__SuccessPlan__c.sfal__IsPublished__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('sfal__SuccessPlan__c.sfal__Status__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('sfal__SuccessPlan__c.sfal__NumberOfTasks__c', new List<Boolean>{True, False});
        
        
        fieldsAndCRUD.put('sfal__SuccessPlanTemplate__c.sfal__Active__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('sfal__SuccessPlanTemplate__c.sfal__TasksActive__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('sfal__SuccessPlanTemplate__c.sfal__Comments__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('sfal__SuccessPlanTemplate__c.sfal__UsageCount__c', new List<Boolean>{True, False});
        
        
        
        fieldsAndCRUD.put('sfal__SuccessPlanTemplateTask__c.sfal__Active__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('sfal__SuccessPlanTemplateTask__c.sfal__Comments__c', new List<Boolean>{True, True});
        //System.DmlException: Upsert failed. First exception on row 83; first error: 
        //INVALID_OR_NULL_FOR_RESTRICTED_PICKLIST, Field Name: bad value for restricted picklist field: sfal__SuccessPlanTemplateTask__c.sfal__Priority__c: [Field]
        // fieldsAndCRUD.put('sfal__SuccessPlanTemplateTask__c.sfal__Priority__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('sfal__SuccessPlanTemplateTask__c.sfal__RelativeDueDate__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('sfal__SuccessPlanTemplateTask__c.sfal__Type__c', new List<Boolean>{True, True});
        

        fieldsAndCRUD.put('Task.Description', new List<Boolean>{True, True});
        fieldsAndCRUD.put('Task.ActivityDate', new List<Boolean>{True, False});
        fieldsAndCRUD.put('Task.Who', new List<Boolean>{True, False});
        fieldsAndCRUD.put('Task.IsVisibleInSelfService', new List<Boolean>{True, True});
        fieldsAndCRUD.put('Task.What', new List<Boolean>{True, True});
        fieldsAndCRUD.put('Task.Type', new List<Boolean>{True, True});


        
        fieldsAndCRUD.put('sfal__UserTopicSetting__c.sfal__DefaultAppointmentDuration__c', new List<Boolean>{True, False});
        //System.DmlException: Upsert failed. First exception on row 89; first error: INVALID_OR_NULL_FOR_RESTRICTED_PICKLIST, 
        //Field Name: bad value for restricted picklist field: sfal__UserTopicSetting__c.sfal__Topic__c: [Field]
        // fieldsAndCRUD.put('sfal__UserTopicSetting__c.sfal__Topic__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('sfal__UserTopicSetting__c.sfal__User__c', new List<Boolean>{True, False});
        
        FieldsAndFSLcollection.add(fieldsAndCRUD);
        //no idea currently how to grant perm set system permissons. Not gonna invest much time into this one
        //pr.EditEvents = True; 
        //pr.EditActivites = True;
        //
        //Call the method to preform the logic of inserting the permissions based on the above object and field level declarations
        logicPerms(objectsAndCRUD, pr, existOP, objPerms, existFP, fieldsAndCRUD, fieldPerms);
        
        
    }
    
    
    
    
    
    
    
    
    
    /*******************************************************************************************************
* @description Method that is used to set the appropriate permissions for the base Avisor permissions found on the link below: 
* https://powerofus.force.com/s/article/SAL-Configure-Advisor-Permissions-Pathways
* 
* 
* @return Nothing
********************************************************************************************************/
    public void SalAdvisorOrAdviseePathwayPerms(){
        //Create the permission set and insert it so we can query on it. 
        PermissionSet pr = new PermissionSet(Name='SalPathWayPerms' + curNumlabel, Label='Sal PathWay Permissions' + curNumlabel);
        insert pr;
        String permSetName = pr.Id;
        //We query on the Id because we know what the ID will be. This gets us the correct newly created permission set to work with. 
        pr = [SELECT Id, Label, Name, Description FROM PermissionSet WHERE ID = :permSetName];
        system.debug('pr id-' + pr.Id);
        
        //Object Permissions Setup
        List<ObjectPermissions> op = new List<ObjectPermissions>();
        op = [SELECT ID, SObjectType, ParentID FROM ObjectPermissions WHERE ParentId = :pr.Id];
        Map<String, String> existOP = new Map<String, String>();
        //existOP is utilized so we can work on profiles if needed as well as permission sets. 
        for (ObjectPermissions eop : op){
            String objname = eop.SobjectType;
            String objid = eop.Id;
            existOp.put(objname, objid);
        }
        List<ObjectPermissions> objPerms = new List<ObjectPermissions>();
        Map<String, List<Boolean>> objectsAndCRUD = new Map<String, List<Boolean>>();
        List<Map<String, List<Boolean>>> objectsAndCRUDcollection = new List <Map<String, List<Boolean>>>();
        
        //Field Permissions Setup
        List<FieldPermissions> fp = new List<FieldPermissions>();
        fp = [SELECT ID, SObjectType, Field, ParentID FROM FieldPermissions WHERE ParentId = :pr.Id];
        Map<String, String> existFP = new Map<String, String>();
        for (FieldPermissions efp : fp){
            String fldname = efp.Field;
            String fldid = efp.Id;
            existFp.put(fldname, fldid);
        }
        List<FieldPermissions> fieldPerms = new List<FieldPermissions>();
        Map<String, List<Boolean>> fieldsAndCRUD = new Map<String, List<Boolean>>();
        List<Map<String, List<Boolean>>> FieldsAndFSLcollection = new List <Map<String, List<Boolean>>>();
        
        
        //========================================================
        //ENTER YOUR OBJECTS and CRUD ACCESS BELOW
        //========================================================   
        
        //READ - CREATE - EDIT - DELETE
        objectsAndCRUD.put('sfpw__Course_Bookmark__c', new List<Boolean>{True, True, True, True});
        objectsAndCRUD.put('sfpw__Personal_Program_Plan__c', new List<Boolean>{True, True, True, True});
        objectsAndCRUD.put('sfpw__Personal_Program_Plan_Course__c', new List<Boolean>{True, True, True, True});
        objectsAndCRUD.put('sfpw__Personal_Program_Plan_Term__c', new List<Boolean>{True, True, True, True});
        objectsAndCRUD.put('hed__Plan_Requirement__c', new List<Boolean>{True, False, False, False});
        objectsAndCRUD.put('hed__Program_Plan__c', new List<Boolean>{True, False, False, False});
        
        
        objectsAndCRUDcollection.add(objectsAndCRUD);
        system.debug('objectsAndCRUDcollection after we insert two objects' + objectsAndCRUDcollection);
        
        //========================================================
        //ENTER YOUR FIELD AND READ/EDIT ACCESS BELOW
        //========================================================   
        
        fieldsAndCRUD.put('sfpw__Course_Bookmark__c.sfpw__Bookmark_Type__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('sfpw__Course_Bookmark__c.sfpw__Bookmarked_For__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('sfpw__Course_Bookmark__c.sfpw__Course__c', new List<Boolean>{True, True});
        
        
        fieldsAndCRUD.put('sfpw__Personal_Program_Plan__c.sfpw__Assigned_To__c', new List<Boolean>{True, True});
        
        
        fieldsAndCRUD.put('sfpw__Personal_Program_Plan_Course__c.sfpw__Course__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('sfpw__Personal_Program_Plan_Course__c.sfpw__Credit_Hours__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('sfpw__Personal_Program_Plan_Course__c.sfpw__Placeholder_Name__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('sfpw__Personal_Program_Plan_Course__c.sfpw__Sort_Order__c', new List<Boolean>{True, True});
        
        
        fieldsAndCRUD.put('sfpw__Personal_Program_Plan_Term__c.sfpw__Sequence__c', new List<Boolean>{True, True});
        fieldsAndCRUD.put('sfpw__Personal_Program_Plan_Term__c.sfpw__Term__c', new List<Boolean>{True, True});
        
        fieldsAndCRUD.put('hed__Plan_Requirement__c.hed__Category__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('hed__Plan_Requirement__c.hed__Course__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('hed__Plan_Requirement__c.hed__Credits__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('hed__Plan_Requirement__c.hed__Description__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('hed__Plan_Requirement__c.hed__Plan_Requirement__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('hed__Plan_Requirement__c.hed__Program_Plan__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('hed__Plan_Requirement__c.hed__Sequence__c', new List<Boolean>{True, False});
        
        
        fieldsAndCRUD.put('hed__Program_Plan__c.hed__Account__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('hed__Program_Plan__c.hed__Description__c', new List<Boolean>{True, False});
        fieldsAndCRUD.put('hed__Program_Plan__c.hed__Total_Required_Credits__c', new List<Boolean>{True, False});
        
        
        FieldsAndFSLcollection.add(fieldsAndCRUD);
        
        //Call the method to preform the logic of inserting the permissions based on the above object and field level declarations
        logicPerms(objectsAndCRUD, pr, existOP, objPerms, existFP, fieldsAndCRUD, fieldPerms);
        
        
    }
    
    
}