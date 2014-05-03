package Management;

import java.util.ArrayList;
import java.util.List;

import Instances.VHost;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ServiceInstance;

public class Manager {
	
	protected ServiceInstance si;
	protected List<VHost> vHosts;
	private int high = 60;
	private int low = 30;
	
	public Manager(ServiceInstance si) throws Exception {
		// TODO Auto-generated constructor stub
		this.si = si;
		setHosts();
	}
	
	protected void setHosts() throws Exception {
		this.vHosts = new ArrayList<VHost>();
		Folder vCenterFolder = si.getRootFolder();
		ManagedEntity[] vHosts = new InventoryNavigator(vCenterFolder)
				.searchManagedEntities("HostSystem");
		if (vHosts.length != 0) {
			for (int i = 0; i < vHosts.length; i++) {
				this.vHosts.add(new VHost((HostSystem) vHosts[i]));
			}
			System.out.println("All connected hosts retrieved.");
		} else {
			System.out.println("No host connected.");
		}
	}
	
	protected boolean isOverload (VHost host) {
		long total = host.totalCpuMhz();
		long usage = host.cpuUsageMhz();
		return (usage * 100.0 / total) > high;
	}
	
	protected boolean isUnderload (VHost host) {
		long total = host.totalCpuMhz();
		long usage = host.cpuUsageMhz();
		return (usage * 100.0 / total) < low;
	}
	
	protected boolean isOverloadAfterMigrate (VHost host, long adjustment) {
		long total = host.totalCpuMhz();
		long usage = host.cpuUsageMhz() + adjustment;
		return (usage * 100.0 / total) > high;
	}
	
	public void start(){
		
	}

}
