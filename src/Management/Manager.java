package Management;

import java.util.ArrayList;
import java.util.List;

import Instances.VHost;
import Instances.VM;

import com.vmware.vim25.HostSystemPowerState;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ServiceInstance;

public class Manager {
	
	protected ServiceInstance si;
	private int high = 60;
	private int low = 30;
	private int overloadLasts = 5;
	private int underloadLasts = 10;
	
	public Manager(ServiceInstance si) throws Exception {
		this.si = si;
	}
	
	protected List<VHost> getPoweredOnHosts() throws Exception {
		List<VHost> vHosts = new ArrayList<VHost>();
		Folder vCenterFolder = si.getRootFolder();
		ManagedEntity[] mes = new InventoryNavigator(vCenterFolder)
				.searchManagedEntities("HostSystem");
		if (mes.length != 0) {
			for (int i = 0; i < mes.length; i++) {
				HostSystem host = (HostSystem) mes[i];
				if (host.getRuntime().getPowerState() == HostSystemPowerState.poweredOn)
					vHosts.add(new VHost(host));
			}
		} else {
			System.out.println("No host connected.");
		}
		return vHosts;
	}
	
	protected boolean isOverload (VHost host) throws Exception {
		long total = host.totalCpuMhz();
		long usage = host.cpuUsageMhz(overloadLasts);
		return (usage * 100.0 / total) > high;
	}
	
	protected boolean isUnderload (VHost host) throws Exception {
		long total = host.totalCpuMhz();
		long usage = host.cpuUsageMhz(underloadLasts);
		return (usage * 100.0 / total) < low;
	}
	
	protected boolean isOverloadAfterMigrate (VHost host, long adjustment) throws Exception {
		long total = host.totalCpuMhz();
		long usage = host.cpuUsageMhz(overloadLasts) + adjustment;
		return (usage * 100.0 / total) > high;
	}
	
	protected List<VHost> tempHost (VHost host) throws Exception {
		List<VHost> vHosts = getPoweredOnHosts();
		List<VHost> temp = new ArrayList<VHost>();
		for(int i=0; i<vHosts.size(); i++) {
			if(host!=vHosts.get(i)) {
				temp.add(vHosts.get(i));
			}
		}
		return temp;
	}
	
	protected VHost getLowestUsageHost(List<VHost> hosts) throws Exception {
		VHost h = null;
		int i = 0;
		long min = Long.MAX_VALUE;
		while(i < hosts.size()) {
			if(hosts.get(i).cpuUsageMhz() < min) {
				min = hosts.get(i).cpuUsageMhz();
				h = hosts.get(i);
			}
			i++;
		}
		return h;
	}
	
	protected VM getLowestUsageVm(List<VM> vms) throws Exception {
		VM v = null;
		int i = 0;
		long min = Long.MAX_VALUE;
		while(i < vms.size()) {
			if(vms.get(i).cpuUsageMhz() < min) {
				min = vms.get(i).cpuUsageMhz();
				v = vms.get(i);
			}
			i++;
		}
		return v;
	}
	
	public HostSystem getHostByName(String name) throws Exception {
		Folder rootFolder = si.getRootFolder();
		HostSystem vhost = (HostSystem) new InventoryNavigator(rootFolder).searchManagedEntity("HostSystem", name);
		if(vhost==null)
			return null;
		else
			return vhost;
	}
	
	public void start() throws Exception {
		
	}
	
	public void doClone(String url, String username, String password, String vmname, String clonename, HostSystem hostSystem) throws Exception {
		
	}
}
