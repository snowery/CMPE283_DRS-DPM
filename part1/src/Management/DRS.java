package Management;


import java.util.List;

import Instances.VHost;
import Instances.VM;

import com.vmware.vim25.mo.ServiceInstance;

public class DRS extends Manager {
	public DRS(ServiceInstance si) throws Exception {
		super(si);																//contractor 
	}
	
	public void start() throws Exception {
		System.out.println("List all virtual hosts: ");
		List<VHost>vHosts = getPoweredOnHosts();
		for(int i=0; i<vHosts.size(); i++) {
			VHost currentHost = vHosts.get(i); 									//get the current host
			System.out.println("\n========================================================");
			System.out.println("Current Host: " + 
									currentHost.getName()); 					//print current host name
			System.out.println("Current host usage: " + 
									currentHost.cpuUsageMhz() + "Mhz");			//print current host cpu usage
			//List<VHost> restHosts = new ArrayList<VHost>();
			//restHosts = tempHost(vHosts.get(i));
			VHost lowestUsageHost = getLowestUsageHost(vHosts);					//get lowest usage host in poll
			System.out.println("Lowest host: " + 
									lowestUsageHost.getName());
			System.out.println("Lowest host usage: " + 
									lowestUsageHost.cpuUsageMhz() + "Mhz");
			System.out.println();
			
			if(isOverload(currentHost)) {										//check current host is overload or not
				System.out.println("Current host usage is high!");
				List<VM> vms = currentHost.getVMs();							//get vm list under current host
				VM lowestUsageVm = getLowestUsageVm(vms);						//get lowest usage vm
				//System.out.println(lowestUsageVm);
				//for(VM vm : vms) {											//loop all vms under this host
					System.out.println("Lowest usage virtual machine: " + 
											lowestUsageVm.getName());
					System.out.println("Lowest usage virtual machine usage: " + 
											lowestUsageVm.cpuUsageMhz(1) + "Mhz");
					
					if(currentHost==lowestUsageHost) {							//check if the current host already the 
						System.out.println("Current host already the "			//lowest usage host, do nothing
								+ "lowest usage host, no need to do migrate\n");
					} else {													//else doing simulation migrate
						
						if(!isOverloadAfterMigrate(lowestUsageHost, 
													lowestUsageVm.cpuUsageMhz())) {			//if the destination host's usage
						System.out.println("Simulation migrate pass");						//plus the vm's usage still
						System.out.println("Virtual machine: " + 							//under control, doing migrate
													lowestUsageVm.getName() + " start migrate to new host: " + 
													lowestUsageHost.getName());
						lowestUsageVm.migrate(lowestUsageHost);
						} 
						else if(isOverloadAfterMigrate(lowestUsageHost, 
													lowestUsageVm.cpuUsageMhz())			//if the destination host's usage
							&& isOverloadAfterMigrate(lowestUsageHost, 						//plus the vm's usage is high, and
													(Math.abs(lowestUsageVm.cpuUsageMhz()) * -1))) { 	//vm's current host's usage after
							System.out.println("Simulation migrate not pass");				//migrate still high, power on a 
							System.out.println("Current distination host usage also high");	//new host
							System.out.println("Power on new host, migrate vm to new host");
						} else {
							System.out.println("Simulation migrate not pass");				//for other situation, do not 
							System.out.println("Virtual machine : " + 						//doing any migrate
									lowestUsageVm.getName() + " : no need to do migrate\n");
						}
					}
				//}
			} else {
				System.out.println("System fine");								//if host is not overload, print system fine
			}
		}
	}
}
