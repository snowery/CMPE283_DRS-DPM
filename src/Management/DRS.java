package Management;


import java.util.ArrayList;
import java.util.List;

import Instances.VHost;
import Instances.VM;

import com.vmware.vim25.mo.ServiceInstance;

public class DRS extends Manager{
	public DRS(ServiceInstance si) throws Exception {
		super(si);
	}
	
	public void start() throws Exception {
		System.out.println("List all virtual hosts: ");
		for(int i=0; i<vHosts.size(); i++) {
			VHost currentHost = vHosts.get(i);
			System.out.println("\nCurrent Host: " + currentHost.getName());
			System.out.println("Host current usage: " + currentHost.cpuUsageMhz() + "Mhz");
			
			if(isOverload(currentHost)) {
				List<VM> vms = currentHost.getVMs();
				//System.out.println(vms);
				
				for(VM vm : vms) {
					System.out.println("For VM: " + vm.getName());
					List<VHost> restHosts = new ArrayList<VHost>();
					restHosts = tempHost(vHosts.get(i));
					VHost lowestUsageHost = getLowestUsageHost(restHosts);

							System.out.println("Lowest usage host: " + lowestUsageHost.getName());
							System.out.println("Lowest host usage: " + lowestUsageHost.cpuUsageMhz() + "Mhz");
							if(!isOverloadAfterMigrate(lowestUsageHost, vm.cpuUsageMhz()) && 
									isOverloadAfterMigrate(lowestUsageHost, (Math.abs(vm.cpuUsageMhz()) * -1))) {
								System.out.println("New Host " + lowestUsageHost.getName() + " is good distination to migrate\n");
								vm.migrate(lowestUsageHost);
							} else {
								System.out.println("Virtual machine: " + vm.getName() + " : no need to migrate\n");
							}
				}
			} else {
				System.out.println("All system fine");
			}
		}
	}
}
