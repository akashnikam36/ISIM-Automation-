package com.setUpOrgTree;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import com.ibm.itim.ws.model.WSAttribute;
import com.ibm.itim.ws.model.WSOrganizationalContainer;
import com.ibm.itim.ws.model.WSSession;
import com.ibm.itim.ws.services.ArrayOfTns1WSAttribute;
import com.ibm.itim.ws.services.ArrayOfTns1WSOrganizationalContainer;
import com.ibm.itim.ws.services.ArrayOfXsdString;
import com.ibm.itim.ws.services.WSLoginServiceException;
import com.ibm.itim.ws.services.WSOrganizationalContainerService;
import com.ibm.itim.ws.services.WSSessionService;

import com.ibm.itim.ws.model.WSOrganizationalContainer;
import com.ibm.itim.ws.model.WSSession;
import com.ibm.itim.ws.services.WSLoginServiceException;
import com.ibm.itim.ws.services.WSSessionService;
import com.utilities.GenericWSClient;
import com.ibm.itim.ws.services.*;
import com.utilities.Utils;

public class OrgnizationUnit extends GenericWSClient{
	private static final String principal = "itim manager";
	private static final String credential = "secret";
	private static final String ORG_CONTAINER_PROFILE_NAME = "Organization";
	//private static final String PARAM_PARENT_ORG_CONTAINER = null;
	static final String WS_CONFIG_FILE = "Properties/OrgChart.properties";
	
	static {		
				
		
	}
	
	public boolean CreateOrganzationContainer(String orgnization,String Orgunit) throws WSLoginServiceException, Exception{
		boolean executedSuccessfully = false;
	/*	java.util.Properties properties = new java.util.Properties();
		try {
			properties.load(new FileInputStream(WS_CONFIG_FILE));
			//properties.getClass().getResourceAsStream(WS_CONFIG_FILE);
			System.out.println();
		} catch (Exception e1) {
			System.out.println("Error occurred while reading ("+ WS_CONFIG_FILE +")");
			e1.printStackTrace();
		}
		boolean executedSuccessfully = false;
		String ou[]=properties.getProperty("organizationalUnit").split(",");
		String parentOrg=properties.getProperty("organization")*/;
		String parentOrg=orgnization;
		WSSession wsSession = this.loginIntoITIM(principal, credential);
		Map<String, Object> map =new HashMap<String, Object>();
		
		map.put("ou", Orgunit);
		System.out.println("ou"+ Orgunit);
		map.put("description", "This is Organizational unit");
		
		WSOrganizationalContainer newWSContainer = createWSOrganizationalContainerFromAttributes(map);
		newWSContainer.setProfileName("OrganizationalUnit");
		WSOrganizationalContainerService service = getOrganizationalContainerService();
		//String parentOrgSearch=properties.getProperty("organization");
		String parentOrgSearch=orgnization;
		List<WSOrganizationalContainer> lstOrgContainers = service.searchContainerByName(wsSession, null, ORG_CONTAINER_PROFILE_NAME, parentOrgSearch);
		System.out.println(lstOrgContainers);
		WSOrganizationalContainer parent = null;
		if(lstOrgContainers != null && lstOrgContainers.size() > 0){
			parent = lstOrgContainers.get(0);
		}else{
			System.out.println(" Not able to locate the parent container with name " + parentOrg);
		}
		
		WSOrganizationalContainer wsOrgContainer = service.createContainer(wsSession, parent, newWSContainer);
		
		if(wsOrgContainer != null){
			executedSuccessfully = true;
			printWSOrgContainerDetails(wsOrgContainer);
		}
		
		
		return executedSuccessfully;
	}
	//--------------------------------------------------------------------------------------------------
	WSOrganizationalContainer createWSOrganizationalContainerFromAttributes(Map<String, Object> mpParams) {
		WSOrganizationalContainer wsOrgContainer = new WSOrganizationalContainer();
		
		java.util.Collection attrList = new ArrayList();
		java.util.Iterator<String> itrParams = mpParams.keySet().iterator();
		WSAttribute wsAttr = null;
		List<WSAttribute> lstWSAttributes = new ArrayList<WSAttribute>();

		while (itrParams.hasNext()) {
			String paramName = itrParams.next();
			Object paramValue = mpParams.get(paramName);
			wsAttr = new WSAttribute();
			wsAttr.setName(paramName);
			ArrayOfXsdString arrStringValues = new ArrayOfXsdString();

			if (paramValue instanceof String) {
				arrStringValues.getItem().addAll(Arrays.asList((String) paramValue));
			} else if (paramValue instanceof Vector) {
				Vector paramValues = (Vector) paramValue;
				arrStringValues.getItem().addAll(paramValues);
			} else {
				Utils.printMsg(Organization.class.getName(), "createWSPersonFromAttributes", "The parameter value datatype is not supported.");
				//System.out.println(" The parameter value datatype is not supported.");
			}
			wsAttr.setValues(arrStringValues);
			lstWSAttributes.add(wsAttr);
		}
		ArrayOfTns1WSAttribute attrs = new ArrayOfTns1WSAttribute();
		attrs.getItem().addAll(lstWSAttributes);
		wsOrgContainer.setAttributes(attrs);

		return wsOrgContainer;
	}
	//------------------------------------------------------------------------------------------------
	void printWSOrgContainerDetails(WSOrganizationalContainer wsOrgContainer){
		String name = wsOrgContainer.getName();
		String DN = wsOrgContainer.getItimDN();
		String supervisorDN = wsOrgContainer.getSupervisorDN();
		
		System.out.println(" The Organization Sub Tree Details are : " );
		System.out.println(" Name : " + name);
		System.out.println(" Distinguished Name : " + DN);
		System.out.println(" Supervisor Distinguished Name : " + supervisorDN);
	}
	
	
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
	
	public static void main(String args[]) throws WSLoginServiceException, Exception{
		OrgnizationUnit org=new OrgnizationUnit();
		//org.CreateOrganzationContainer();
		java.util.Properties properties = new java.util.Properties();
		try {
			properties.load(new FileInputStream(WS_CONFIG_FILE));
			//properties.getClass().getResourceAsStream(WS_CONFIG_FILE);
			System.out.println();
		} catch (Exception e1) {
			System.out.println("Error occurred while reading ("+ WS_CONFIG_FILE +")");
			e1.printStackTrace();
		}
		String orgnization=properties.getProperty("organization");
		String ou[]= properties.getProperty("organizationalUnit").split(",");
		int len=ou.length;
		System.out.println("LENTH="+len);
		for(int i=0;i<len;i++){
			org.CreateOrganzationContainer(orgnization,ou[i]);
		}
	}

}
