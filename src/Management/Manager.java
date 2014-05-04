package Management;

import java.util.ArrayList;
import java.util.List;

import Instances.VHost;
import Instances.VM;

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
	
	protected boolean isOverload (VHost host) throws Exception {
		long total = host.totalCpuMhz();
		long usage = host.cpuUsageMhz();
		return (usage * 100.0 / total) > high;
	}
	
	protected boolean isUnderload (VHost host) throws Exception {
		long total = host.totalCpuMhz();
		long usage = host.cpuUsageMhz();
		return (usage * 100.0 / total) < low;
	}
	
	protected boolean isOverloadAfterMigrate (VHost host, long adjustment) throws Exception {
		long total = host.totalCpuMhz();
		long usage = host.cpuUsageMhz() + adjustment;
		return (usage * 100.0 / total) > high;
	}
	
	protected List<VHost> tempHost (VHost host) {
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
	
	public void start() throws Exception{
		
	}

}
