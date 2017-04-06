package com.driver;

import java.io.FileInputStream;
import java.util.*;

import com.CreateRoles.Role;
import com.ibm.itim.ws.model.WSSession;
import com.ibm.itim.ws.services.WSLoginServiceException;
import com.person.personEntity;
import com.policy.ProvisioningPolicy;
import com.servicesSetup.Service;
import com.setUpOrgTree.*;

public class Driver {
	private static final String principal = "itim manager";
	private static final String credential = "secret";
	static final String WS_CONFIG_FILE_ORGCHART = "Properties/OrgChart.properties";
	static final String WS_CONFIG_FILE_ROLE = "Properties/StaticRole.properties";
	public static void main(String args[]) throws WSLoginServiceException, Exception{
		
		Scanner in=new Scanner(System.in);
		loop:while(true){
		System.out.println("-----------------------------------------");
		System.out.println("1.Orgnization Structure\n"
							+ "2.Roles\n"
							+ "3.Services\n"
							+ "4.Policies\n"
							+ "5.Person\n"
							+ "6.EXIT");
		System.out.println("--------------------------------------------");
		int choice=in.nextInt();
		
			switch(choice){
			case 1: Organization org=new Organization();
					org.CreateOrganzationContainer();
					OrgnizationUnit orgUnit=new OrgnizationUnit();
				//org.CreateOrganzationContainer();
				java.util.Properties properties = new java.util.Properties();
				try {
					properties.load(new FileInputStream(WS_CONFIG_FILE_ORGCHART));
					//properties.getClass().getResourceAsStream(WS_CONFIG_FILE);
					System.out.println();
				} catch (Exception e1) {
					System.out.println("Error occurred while reading ("+ WS_CONFIG_FILE_ORGCHART +")");
					e1.printStackTrace();
				}
				String orgnization=properties.getProperty("organization");
				String ou[]= properties.getProperty("organizationalUnit").split(",");
				int len=ou.length;
				System.out.println("LENTH="+len);
				for(int i=0;i<len;i++){
					orgUnit.CreateOrganzationContainer(orgnization,ou[i]);
				}
				continue loop;
			case 2:Role role=new Role();
				role.mainMethod();
				continue loop;
			case 3:Service service=new Service();
				service.mainServiceMethodRunner();
				continue loop;
			case 4:ProvisioningPolicy policy=new ProvisioningPolicy();
				policy.mainMethodPolicy();
				continue loop;
			case 5:personEntity person=new personEntity();
				person.PersonRunner();
				continue loop;
			case 6: break loop;
			default:
				System.out.println("Worng Choice............");
												
			//--------------------------------------------------------------------------	
			}
		}
	}

}
