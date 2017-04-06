package com.person;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import javax.xml.datatype.XMLGregorianCalendar;

import com.ibm.itim.dataservices.model.ObjectProfileCategory;
import com.ibm.itim.dataservices.model.domain.PersonEntity;
import com.ibm.itim.ws.model.WSAttribute;
import com.ibm.itim.ws.model.WSOrganizationalContainer;
import com.ibm.itim.ws.model.WSPerson;
import com.ibm.itim.ws.model.WSRequest;
import com.ibm.itim.ws.model.WSRole;
import com.ibm.itim.ws.model.WSSession;
import com.ibm.itim.ws.services.ArrayOfTns1WSAttribute;
import com.ibm.itim.ws.services.ArrayOfXsdString;
import com.ibm.itim.ws.services.PersonObjectFactory;
import com.ibm.itim.ws.services.WSLoginServiceException;
import com.ibm.itim.ws.services.WSOrganizationalContainerService;
import com.ibm.itim.ws.services.WSPersonService;
import com.ibm.itim.ws.services.WSRequestService;
import com.ibm.itim.ws.services.WSRoleService;
import com.ibm.itim.ws.services.WSSessionService;
import com.utilities.GenericWSClient;
import com.utilities.Utils;

public class personEntity extends GenericWSClient {
	private static final String principal = "itim manager";
	private static final String credential = "secret";
	static final String WS_CONFIG_FILE = "Properties/People2.csv";
	//------------------------------------------------------------------------------------------------------------
	public void CREATEPERSON(HashMap<String,String> separatedValues) throws Exception{
			
		//System.out.println(separatedValues[0]+"--"+separatedValues[1]);
		System.out.println(separatedValues);
		boolean executedSuccessfully = false;
		WSSession session = loginIntoITIM(principal, credential);
		WSPersonService personService = getPersonService();
		//Attributes mapping
		String PARAM_ORG_CONTAINER=separatedValues.get("OrganizationalUnit");
		// Dependency on OrganizationalContainerservice.
		WSOrganizationalContainerService port = getOrganizationalContainerService();
		
		
		
		
		List<WSOrganizationalContainer> lstWSOrgContainers = port.searchContainerByName(
				session, null, "OrganizationalUnit", PARAM_ORG_CONTAINER);
		System.out.println("--------------------");
				if (lstWSOrgContainers != null && !lstWSOrgContainers.isEmpty()){
					System.out.println(lstWSOrgContainers.get(0).toString());
					WSOrganizationalContainer wsContainer = lstWSOrgContainers.get(0);
					//System.out.println(wsContainer.toString());
		
		//----------Adding Attributes--------------------------		
				
		XMLGregorianCalendar date = Utils.long2Gregorian(new Date().getTime());
		
			
		Map<String, Object> map =new HashMap<String, Object>();
		//map.put("objectclass", "erPersonItem");
		/*Set<String> mapVal=separatedValues.keySet();
		 Iterator iterator = mapVal.iterator();
		 while (iterator.hasNext()){
			// count++;
			 String attrName=(String) iterator.next();
			 map.put(attrName, separatedValues.get(attrName));
			   }*/
		map.put("cn", separatedValues.get("cn"));
		map.put("sn", separatedValues.get("sn"));
	//	map.put("erRole", separatedValues.get("erRole"));
		map.put("title", separatedValues.get("title"));
		
		WSPerson wsPerson = createWSPersonAttributes(map);
		boolean isCreatePersonAllowed = personService.isCreatePersonAllowed(session);
		if(isCreatePersonAllowed){
		WSRequest wsRequest = personService.createPerson(session, wsContainer, wsPerson, date);
		Utils.printWSRequestDetails("create person", wsRequest);
		//System.out.println("Create Person"+ wsRequest);
		}else{
			Utils.printMsg(personEntity.class.getName(), "CREATEPERSON", "The user " + principal + " is not authorized to create a person");
			//System.out.println("Not allowed to create person------");
		}
		executedSuccessfully = true;
	
		}
				if(executedSuccessfully == true){
					WSRequestService requestService=getRequestService();
					personEntity.ADDROLETOPERSON(separatedValues.get("erRole"),separatedValues.get("cn"));
					System.out.println("Person Created Sucessfully----");
				}
				
	}
	//------------------create Person Method()----------------
		WSPerson createWSPersonAttributes(Map <String, Object> mpParams){
			
			WSPerson wsPerson=new WSPerson();
			wsPerson.setProfileName(ObjectProfileCategory.PERSON);
			System.out.println("*******************");
			java.util.Collection attrList = new ArrayList();
			java.util.Iterator<String> itrParams = mpParams.keySet().iterator();
			WSAttribute wsAttr = null;
		//	WSAttribute wsAttr = new WSAttribute();
			List<WSAttribute> lstWSAttributes = new ArrayList<WSAttribute>();
			while(itrParams.hasNext()){
				String paramName = itrParams.next();
				Object paramValue = mpParams.get(paramName);
				//System.out.println("ParamName ="+paramName);
				//System.out.println("paramValue ="+paramValue);
				wsAttr = new WSAttribute();
				wsAttr.setName(paramName);
				ArrayOfXsdString arrStringValues = new ArrayOfXsdString();
				
				if(paramValue instanceof String) {
					arrStringValues.getItem().addAll(Arrays.asList((String) paramValue));
				}else if(paramValue instanceof Vector){
					Vector paramValues = (Vector) paramValue;
					arrStringValues.getItem().addAll(paramValues); 
				}else{
					System.out.println(personEntity.class.getName()+"The parameter value datatype is not supported.");
				}
				wsAttr.setValues(arrStringValues);
				lstWSAttributes.add(wsAttr);
			}
			ArrayOfTns1WSAttribute attrs = new ArrayOfTns1WSAttribute();
			attrs.getItem().addAll(lstWSAttributes);
			wsPerson.setAttributes(attrs);
			return wsPerson;
			
		}
	//------------------------------------ADD ROLE TO PERSON------------------------------------------------------
		public static void ADDROLETOPERSON(String PARAM_ROLE,String PARAM_PERSON) throws WSLoginServiceException, Exception{
			String sRoleFilterParam = "(errolename="+PARAM_ROLE+")"; //	PARAM_ROLE_FILTER = "(errolename=dev)";
			String sPersonFilterParam ="(cn="+PARAM_PERSON+")";//PARAM_PERSON; //PARAM_PERSON_FILTER = "(cn=JAVA 1.8)";
			WSSession wsSession = loginIntoITIM(principal, credential);
			WSPersonService personService = getPersonService();
			
			List<String> roleDNlist = new ArrayList<String>();
			WSRoleService roleService = getRoleService();
			XMLGregorianCalendar date = Utils.long2Gregorian(new Date().getTime());
			List<WSRole> lstWSRoles = roleService.searchRoles(wsSession, sRoleFilterParam);
			if(lstWSRoles != null && lstWSRoles.size() > 0){
				Utils.printMsg(PersonEntity.class.getName(), "ADDROLESTOPERSON", "Role(s) matching the filter, "+ sRoleFilterParam+ ", are: ");
				for (Iterator<WSRole> iter = lstWSRoles.iterator(); iter.hasNext();) {
					WSRole wsRole = (WSRole) iter.next();
					String roleDN = wsRole.getItimDN();
					roleDNlist.add(roleDN);
					Utils.printMsg(personEntity.class.getName(), "ADDROLESTOPERSON", roleDN);
				}
			}else{
				Utils.printMsg(personEntity.class.getName(), "ADDROLESTOPERSON", "No role found matching the filter : " + sRoleFilterParam);
				
			}
			
			List<WSPerson> lstWSPersons = personService.searchPersonsFromRoot(wsSession, sPersonFilterParam, null);
			if(lstWSPersons != null && lstWSPersons.size() > 0){
				//This means the requested person exists. If there are more than one person then we select the first one.
				WSPerson person = lstWSPersons.get(0);
				String personDN = person.getItimDN();
				Utils.printMsg(personEntity.class.getName(), "ADDROLESTOPERSON", "Adding role(s) to a person, " + person.getName() + ", having DN " + person.getItimDN());

				WSRequest wsRequest = personService.addRolesToPerson(wsSession, personDN, roleDNlist, date);
				Utils.printWSRequestDetails("add roles to person", wsRequest);
				System.out.println("Add role to person request submitted successfully. Request ID: "+ wsRequest.getRequestId());
				
			}else{
				//Output a message which says that the no person found matching the filter criteria
				Utils.printMsg(personEntity.class.getName(), "ADDROLESTOPERSON", "No person found matching the filter : " + sPersonFilterParam);
				
			}
			
		}
	//-------------------------------------------------------------------------------------------------------------
		//---------------------DELET PERSON METHOD-----------------------------------
		public void DELETEPERSON(HashMap<String,String> separatedValues) throws Exception{
			String PARAM_PERSON_FILTER = "(cn="+separatedValues.get("cn")+")";
			//Search person from root using the cn and sn attribute
			String sFilterParam = PARAM_PERSON_FILTER;
			
			WSSession wsSession = loginIntoITIM(principal, credential);
			WSPersonService personService = getPersonService();
			XMLGregorianCalendar date = Utils.long2Gregorian(new Date().getTime());
			
			List<WSPerson> lstWSPersons = personService.searchPersonsFromRoot(wsSession, sFilterParam, null);
			if(lstWSPersons != null && lstWSPersons.size() > 0){
				WSPerson personToBeDeleted = lstWSPersons.get(0);
				String personDN = personToBeDeleted.getItimDN();
				
				Utils.printMsg(personEntity.class.getName(), "DELETEPERSON", "Deleting the person " + personToBeDeleted.getName() + " having DN " + personToBeDeleted.getItimDN());
			
				//DELETE person method
				WSRequest wsRequest = personService.deletePerson(wsSession, personDN, date);
				Utils.printWSRequestDetails("delete person", wsRequest);
				
			}else{
				Utils.printMsg(personEntity.class.getName(),"DELETEPERSON", "No person found matching the filter : " + sFilterParam);
			}
			
				
		}
	//-------------------------------------------------------------------------------------------------------------
	private static WSSession loginIntoITIM(String principal2, String credential) throws Exception, WSLoginServiceException {
		WSSessionService proxy = getSessionService();
		WSSession session = proxy.login(principal, credential);
		return session;
	}
	@Override
	public boolean executeOperation(Map<String, Object> mpParams) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getUsage(String errMessage) {
		// TODO Auto-generated method stub
		return null;
	}
//-------------------------------------------------------------------------------------------------------
	public void PersonRunner() throws Exception{
		 
		personEntity person=new personEntity();
		BufferedReader bufferedReader = new BufferedReader(new FileReader(WS_CONFIG_FILE));
		String separator = ",";
		String singleLine;
		int lineCount = 0;
		String columnName[] = null;
		while ((singleLine = bufferedReader.readLine()) != null) {
			if (lineCount == 0) {
				columnName = singleLine.split(separator);
				lineCount++;
				continue;
			}
			String[] separatedValues = singleLine.split(separator);
			//System.out.println(separatedValues[0]+"--"+columnName[0]);
			HashMap<String, String> entryHashMap = CSVParser.hashMap(separatedValues, columnName);
			//System.out.println("------"+entryHashMap);
			person.CREATEPERSON(entryHashMap);
			//person.DELETEPERSON(entryHashMap);
		}		
				
	
	}
//-------------------------------------------------------------------------------------------------------
	public static void main(String args[]) throws Exception{/* 
		personEntity person=new personEntity();
		BufferedReader bufferedReader = new BufferedReader(new FileReader(WS_CONFIG_FILE));
		String separator = ",";
		String singleLine;
		int lineCount = 0;
		String columnName[] = null;
		while ((singleLine = bufferedReader.readLine()) != null) {
			if (lineCount == 0) {
				columnName = singleLine.split(separator);
				lineCount++;
				continue;
			}
			String[] separatedValues = singleLine.split(separator);
			//System.out.println(separatedValues[0]+"--"+columnName[0]);
			HashMap<String, String> entryHashMap = CSVParser.hashMap(separatedValues, columnName);
			//System.out.println("------"+entryHashMap);
			person.CREATEPERSON(entryHashMap);
			//person.DELETEPERSON(entryHashMap);
		}		
				
	*/}
}
