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
			System.out.println("\n==============================================");
			System.out.println("Current Host: " + currentHost.getName());
			System.out.println("Current host usage: " + currentHost.cpuUsageMhz() + "Mhz");
			//List<VHost> restHosts = new ArrayList<VHost>();
			//restHosts = tempHost(vHosts.get(i));
			VHost lowestUsageHost = getLowestUsageHost(vHosts);
			System.out.println("Lowest host: " + lowestUsageHost.getName());
			System.out.println("Lowest host usage: " + lowestUsageHost.cpuUsageMhz() + "Mhz");
			System.out.println();
			
			if(isOverload(currentHost)) {
				System.out.println("Current host usage is high!");
				List<VM> vms = currentHost.getVMs();
				VM lowestUsageVm = getLowestUsageVm(vms);
				
				for(VM vm : vms) {
					System.out.println("Virtual machine: " + vm.getName());
					System.out.println("Virtual machine usage: " + vm.cpuUsageMhz() + "Mhz");
					
					if(currentHost==lowestUsageHost) {
						System.out.println("Current host already the lowest usage host, no need to do migrate\n");
					} else {
						
						if(!isOverloadAfterMigrate(lowestUsageHost, vm.cpuUsageMhz())) {
							//&& isOverloadAfterMigrate(lowestUsageHost, (Math.abs(vm.cpuUsageMhz()) * -1))) {
						System.out.println("Simulation migrate pass");
						System.out.println("Virtual machine: " + vm.getName() + " start migrate to new host: " + lowestUsageHost.getName());
						vm.migrate(lowestUsageHost);
						} else {
							System.out.println("Simulation migrate not pass");
							System.out.println("Virtual machine : " + vm.getName() + " : no need to do migrate\n");
						}
					}
				}
			} else {
				System.out.println("System fine");
			}
		}
	}
}
