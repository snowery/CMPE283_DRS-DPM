package Management;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import Instances.VHost;
import Instances.VM;

import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.VirtualMachineCloneSpec;
import com.vmware.vim25.VirtualMachineRelocateSpec;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

public class DRS1 extends Manager {
	public DRS1(ServiceInstance si) throws Exception {
		super(si);
	}
	
	public void start() throws Exception {
		System.out.println("List all virtual hosts: ");
		List<VHost>vHosts = getPoweredOnHosts();
		VHost lowestUsageHost = getLowestUsageHost(vHosts);					//get lowest usage host in poll
		System.out.println("\n************************************************");
		System.out.println("Lowest host: " + 
								lowestUsageHost.getName());
		System.out.println("Lowest host usage: " + 
								lowestUsageHost.cpuUsageMhz() + "Mhz");
		System.out.println();
		
		List<VM>vms = new ArrayList<VM>();
		for(int i=0; i<vHosts.size(); i++) {
			for(int j=0; j<vHosts.get(i).getVMs().size(); j++) {
				vms.add(vHosts.get(i).getVMs().get(j));
			}
		}
		
		VM lowestUsageVm = getLowestUsageVm(vms);
		System.out.println("Lowest vm: " + lowestUsageVm.getName());
		System.out.println("Lowest vm usage: " + lowestUsageVm.cpuUsageMhz() + "Mhz");
		
		System.out.println("\nClone lowest usage vm to lowest usage host");
		lowestUsageVm.clone(lowestUsageHost);
	}
	
	public void doClone(String url, String username, String password, String vmname, String clonename, HostSystem vhost) throws Exception
	{
	    ServiceInstance si = new ServiceInstance(new URL(url), username, password, true);
	 
	    Folder rootFolder = si.getRootFolder();
	    VirtualMachine vm = (VirtualMachine) new InventoryNavigator(rootFolder).searchManagedEntity("VirtualMachine", vmname);
	 
	    if(vm==null)
	    {
	        System.out.println("VM " + vmname + " not found");
	        si.getServerConnection().logout();
	        throw new Exception("Source Virtual Machine Not Found");
	    }
	 
	    VirtualMachineCloneSpec cloneSpec = new VirtualMachineCloneSpec();
	    VirtualMachineRelocateSpec relSpec = new VirtualMachineRelocateSpec();
	    relSpec.host = vhost.getMOR();
	    relSpec.diskMoveType = "createNewChildDiskBacking";
	    cloneSpec.setLocation(relSpec);
	    cloneSpec.setPowerOn(false);
	    cloneSpec.setTemplate(false);
	    cloneSpec.snapshot = vm.getCurrentSnapShot().getMOR();
	 
	    System.out.println("Cloning " + vmname + " into " + clonename);
	    Task task = vm.cloneVM_Task((Folder) vm.getParent(), clonename, cloneSpec);
	 
	    String status = task.waitForMe();
	    if(status==Task.SUCCESS)
	    {
	        System.out.println("VM cloned successfully.");
	    }
	    else
	    {
	        throw new Exception("Error while cloning VM");
	    }
	}
}
