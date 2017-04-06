package com.CreateRoles;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.ibm.itim.ws.model.WSOrganizationalContainer;
import com.ibm.itim.ws.model.WSRole;
import com.ibm.itim.ws.model.WSSession;
import com.ibm.itim.ws.services.ArrayOfTns1WSAttribute;
import com.ibm.itim.ws.services.WSLoginServiceException;
import com.ibm.itim.ws.services.WSOrganizationalContainerService;
import com.ibm.itim.ws.services.WSRoleService;
import com.ibm.itim.ws.services.WSSessionService;
import com.utilities.GenericWSClient;
import com.utilities.Utils;

public class Role extends GenericWSClient {
	private static final String principal = "itim manager";
	private static final String credential = "secret";
	static int count=0;
	static final String WS_CONFIG_FILE = "Properties/StaticRole.properties";
	static final String WS_CONFIG_FILE_POLICY = "Properties/Policy_config.properties";
	
	
	public void CreateRoles(WSSession wsSession,String role,String desc,String parent) throws WSLoginServiceException, Exception{
		
//		FileWriter fileWritter = new FileWriter(WS_CONFIG_FILE, true);
//		BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
		//bufferWritter.append("PROPERTES_YOUR_KEY=PROPERTES_YOUR_VALUE");
		
		
		String roleName = role;
		WSRole wsRole = new WSRole();
		wsRole.setName(roleName);
		
		//String descriptions[] = properties.getProperty("description").split(",");
		String description= desc;
		if (description != null) {
			wsRole.setDescription(description);
		}
		
		ArrayOfTns1WSAttribute attr = new ArrayOfTns1WSAttribute();
		wsRole.setAttributes(attr);
		//WSSession wsSession = loginIntoITIM(principal, credential);
		WSRoleService roleService = getRoleService();
		WSOrganizationalContainerService port = getOrganizationalContainerService();
		WSOrganizationalContainer wsContainer = null;
		
		//String containerName = properties.getProperty("parent");
		String containerName = parent;
		
		if (containerName != null) {
			List<WSOrganizationalContainer> lstWSOrgContainers = port
					.searchContainerByName(wsSession, null,
							"Organization", containerName);

			if (lstWSOrgContainers != null
					&& !lstWSOrgContainers.isEmpty()) {
				wsContainer = lstWSOrgContainers.get(0);
			} else {
				System.out.println("No container found matching "
						+ containerName);
				//return false;
			}
		}
		
		roleService.createStaticRole(wsSession, wsContainer, wsRole);
		System.out.println("ROLE NAME="+wsRole.getName());
		Utils.printMsg(Role.class.getName(), "execute",
				"Create static role request submitted successfully.");
		
		//WSRole wsRoleSearch = roleService.lookupRole(wsSession, roleDN);
		
//		bufferWritter.append("DN:"+wsRole.getItimDN());
//		bufferWritter.close();
		
		
	}
//-------------------------------------------------------------------------------------------------------------
	public boolean LOOKUPROLE(WSSession wsSession,String parent,String name) throws Exception{
		//Properties prop=new Properties();
		count++;
		FileWriter fileWritter = new FileWriter(WS_CONFIG_FILE_POLICY, true);
		BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
		
		String sFilterParam = parent;
		String rolename=name;
		String PARAM_ROLE_FILTER = "(errolename="+name+")"; // "(errolename=*"+name+"*)";
		if (sFilterParam == null) {
			Utils
					.printMsg(Role.class.getName(),
							"LOOKUPROLE", 
							"No Filter parameter passed to search for the role.");
			String usage = this.getUsage("");
			System.out.println(usage);
			//return false;
		}
		
		//WSSession wsSession = loginIntoITIM(principal, credential);
		WSRoleService roleService = getRoleService();
		
		List<WSRole> lstWSRole = roleService.searchRoles(wsSession, PARAM_ROLE_FILTER);
		if(lstWSRole != null && lstWSRole.size() > 0){
			WSRole role = lstWSRole.get(0);
			String roleDN = role.getItimDN();
			//System.out.println("ROLE = "+role.getName()+"ROLE DN = "+roleDN);
			Utils.printMsg(Role.class.getName(),
					"LOOKUPROLE", "Getting roles for user  "
							+ role.getName());
			
			//System.out.println("NAME = "+wsRole.getName()+"DN = "+wsRole.getItimDN());
			WSRole wsRole = roleService.lookupRole(wsSession, roleDN);
			Utils.printMsg(Role.class.getName(),
					"LOOKUPROLE",  "\n Role Name : "
							+ wsRole.getName() + "\n Person DN : "
							+ wsRole.getItimDN());
			//writer.write("DN="+wsRole.getItimDN());
			System.out.println(wsRole.getItimDN());
		
				bufferWritter.append("\n"+wsRole.getName()+":"+wsRole.getItimDN()+"\n");
				//System.out.println();
			
			
			bufferWritter.close();
						
			return role.getName().equals(wsRole.getName());
			
		}else {

			Utils.printMsg(Role.class.getName(),
					"LOOKUPROLE",
					"No role found matching the filter : "
							+ sFilterParam);
			return false;
		}
		
	}
//-------------------------LOGIN TO ITIM------------------------------------------------------------------------
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
//------------------------------------MAIN Method-----------------------------------------------------
	public void mainMethod() throws Exception{

		WSSession wsSession = loginIntoITIM(principal, credential);
		
		Role role=new Role();
		//role.CreateRoles();
		java.util.Properties properties = new java.util.Properties();
		try {
			properties.load(new FileInputStream(WS_CONFIG_FILE));
			//properties.getClass().getResourceAsStream(WS_CONFIG_FILE);
			System.out.println();
		} catch (Exception e1) {
			System.out.println("Error occurred while reading ("+ WS_CONFIG_FILE +")");
			e1.printStackTrace();
		}
		String roles[]=properties.getProperty("Roles").split(",");
		String descriptions[] = properties.getProperty("description").split(",");
		String containerName = properties.getProperty("parent");
		int len=roles.length;
		for(int i=0;i<len;i++){
			role.CreateRoles(wsSession,roles[i],descriptions[i],containerName);
			role.LOOKUPROLE(wsSession, containerName, roles[i]);
			
		}
	
	}
//----------------------------------------------------------------------------------------------------	
	public static void main(String args[]) throws Exception{}
	
}
