package com.servicesSetup;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import com.ibm.itim.ws.model.WSAttribute;
import com.ibm.itim.ws.model.WSOrganizationalContainer;
import com.ibm.itim.ws.model.WSService;
import com.ibm.itim.ws.model.WSSession;
import com.ibm.itim.ws.services.ArrayOfXsdString;
import com.ibm.itim.ws.services.WSApplicationException;
import com.ibm.itim.ws.services.WSLoginServiceException;
import com.ibm.itim.ws.services.WSOrganizationalContainerService;
import com.ibm.itim.ws.services.WSServiceService;
import com.ibm.itim.ws.services.WSSessionService;
import com.utilities.GenericWSClient;
import com.utilities.Utils;

public class Service extends GenericWSClient{
	private static final String principal = "itim manager";
	private static final String credential = "secret";
	static final String WS_CONFIG_FILE = "Properties/Service_config.properties";
	static final String WS_CONFIG_FILE_POLICY = "Properties/Policy_config.properties";

	public void CreateService(WSSession session,String containerName,String profileName,String serviceName,String url,String username,String password) throws WSApplicationException, Exception{
		
		java.util.Properties properties = new java.util.Properties();
		try {
			properties.load(new FileInputStream(WS_CONFIG_FILE));
			//properties.getClass().getResourceAsStream(WS_CONFIG_FILE);
			System.out.println();
		} catch (Exception e1) {
			System.out.println("Error occurred while reading ("+ WS_CONFIG_FILE +")");
			e1.printStackTrace();
		}
		
		String Serviceprofile=profileName;
		if (profileName == null) {
			Utils.printMsg(Service.class.getName(),
					"execute",
					"No Filter parameter passed for the profile name.");
			String usage = this.getUsage("");
			System.out.println(usage);
			//return false;
		}
		
		//WSSession wsSession = loginIntoITIM(principal, credential);
		WSOrganizationalContainerService wsOrgContainerService = getOrganizationalContainerService();
		WSOrganizationalContainer wsContainer = null;
		WSService service=new WSService();
		
		
		String container = containerName;
		
		if (containerName != null) {
			List<WSOrganizationalContainer> lstWSOrgContainers = wsOrgContainerService
					.searchContainerByName(session, null,
							"Organization", container);
			if (lstWSOrgContainers != null
					&& !lstWSOrgContainers.isEmpty()) {
				
				wsContainer = lstWSOrgContainers.get(0);
			} else {
				System.out.println("No container found matching "
						+ container);
			//return false;
			}
		} else {
			System.out
					.println("No Filter parameter passed for the container name.");
			//return false;
		}
		String containerDN = wsContainer.getItimDN();
		// The remaining input parameters represents service attributes.
		// They will be passed
		// to the underlying web service as a list of WSAttributes.
		Map<String, Object> map =new HashMap<String, Object>();
		//map.put("profileName", "WinLocalProfile");
		if(profileName.equals("PosixLinuxProfile")){
			map.put("erservicename", serviceName);
			map.put("erUrl", url);
			map.put("eritdiurl",properties.getProperty("dispatcherurl"));
			map.put("erserviceuid",username);
			map.put("erposixuseshadow", "true");
			map.put("erposixhomedirremove", "false");
			
			
		}
		if(profileName.equals("WinLocalProfile")){
			map.put("erservicename", serviceName);
			map.put("erUrl", url);
			map.put("erUid", username);
			map.put("erPassword", password);
				
		}
		/*
		if (profileName=="PosixLinuxProfile"){
			map.put("erserviceuid", "root");
			map.put("erposixuseshadow", true);
			map.put("erposixhomedirremove", false);
		}*/
		map.put("description", "Description for TEST service");
		
		List<WSAttribute> serviceAttributes = getWSAttributesList(
				map, "CREATESERVICE", null);

		WSServiceService wsServiceServiceObj = getServiceService();
		String nameOfCreatedService = wsServiceServiceObj
				.createService(session, containerDN, profileName,
						serviceAttributes);
		Utils.printMsg(Service.class.getName(),
				"execute",
				"The Create Service request submitted successfully. Service Name: : "
						+ nameOfCreatedService);
		
		
	}
	//----------------------------------------------------------------------------------
	List<WSAttribute> getWSAttributesList(Map<String, Object> mpParams, String methodName, String enumName){
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
				Utils.printMsg(Service.class.getName(), methodName,  "The datatype of parameter '" + paramName + "' is not supported.");
			}
			wsAttr.setValues(arrStringValues);
			lstWSAttributes.add(wsAttr);
		}
		
		return lstWSAttributes;
	}
	//--------------------------------------------------------------------------------------
	public void SEARCH(WSSession session,String serviceName) throws WSLoginServiceException, Exception{
		FileWriter fileWritter = new FileWriter(WS_CONFIG_FILE_POLICY, true);
		BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
		
		String filter="(erservicename="+serviceName+")";
		//WSSession session = loginIntoITIM(principal, credential);
		
		WSServiceService wsService = getServiceService();
		List<WSService> listWsService = wsService.searchServices(session, null, filter);
		for (Iterator iterator = listWsService.iterator(); iterator
				.hasNext();) {
			WSService service = (WSService) iterator.next();
			Utils.printMsg(Service.class.getName(),
					"SEARCH","\n Service Name : "
							+ service.getName() + " \n "
							+ "\n Service Profile Name : "
							+ service.getProfileName() + " \n "
							+ "\n Service DN : " + service.getItimDN());
			
			bufferWritter.append("\n"+service.getName()+":"+service.getItimDN());

		}
		bufferWritter.close();
	}
	//----------------------------------------------------------------------------------
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
	
//--------------------------------------------------------------------------------------------------------
	public void mainServiceMethodRunner() throws WSLoginServiceException, Exception{

		Service service=new Service();
		java.util.Properties properties = new java.util.Properties();
		try {
			properties.load(new FileInputStream(WS_CONFIG_FILE));
			//properties.getClass().getResourceAsStream(WS_CONFIG_FILE);
			System.out.println();
		} catch (Exception e1) {
			System.out.println("Error occurred while reading ("+ WS_CONFIG_FILE +")");
			e1.printStackTrace();
		}
		String containerName=properties.getProperty("containername");
		String profileName[]=properties.getProperty("ProfileName").split(",");
		String serviceName[]=properties.getProperty("Servicename").split(",");
		String url[]=properties.getProperty("url").split(",");
		String username[]=properties.getProperty("username").split(",");
		String password[]=properties.getProperty("password").split(",");
		int len=profileName.length;
		
		WSSession session = loginIntoITIM(principal, credential);
		
		for(int i=0;i<len;i++){
			service.CreateService(session,containerName,profileName[i],serviceName[i],url[i],
					username[i],password[i]);
			service.SEARCH(session, serviceName[i]);
		}
	
		
	}
//--------------------------------------------------------------------------------------------------------

	public static void main(String args[]) throws WSApplicationException, Exception{/*
		Service service=new Service();
		java.util.Properties properties = new java.util.Properties();
		try {
			properties.load(new FileInputStream(WS_CONFIG_FILE));
			//properties.getClass().getResourceAsStream(WS_CONFIG_FILE);
			System.out.println();
		} catch (Exception e1) {
			System.out.println("Error occurred while reading ("+ WS_CONFIG_FILE +")");
			e1.printStackTrace();
		}
		String containerName=properties.getProperty("containername");
		String profileName[]=properties.getProperty("ProfileName").split(",");
		String serviceName[]=properties.getProperty("Servicename").split(",");
		String url[]=properties.getProperty("url").split(",");
		String username[]=properties.getProperty("username").split(",");
		String password[]=properties.getProperty("password").split(",");
		int len=profileName.length;
		
		WSSession session = loginIntoITIM(principal, credential);
		
		for(int i=0;i<len;i++){
			service.CreateService(session,containerName,profileName[i],serviceName[i],url[i],
					username[i],password[i]);
			service.SEARCH(session, serviceName[i]);
		}
	*/}
}
