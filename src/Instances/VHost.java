package Instances;

import java.util.ArrayList;
import java.util.List;

import com.vmware.vim25.HostCpuInfo;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.VirtualMachine;

public class VHost {
	private HostSystem host;
	private List<VM> vms;

	public VHost(HostSystem host) throws Exception {
		this.host = host;
		setVMs();
	}	
	
	public long cpuUsageMhz() {
		//TODO
		return 0;
	}
	
	public void setVMs() throws Exception {
		vms = new ArrayList<VM>();
		
		ManagedEntity[] mes = new InventoryNavigator(host)
				.searchManagedEntities("VirtualMachine");
		if (mes == null) return;	
		
		for (int i = 0; i < mes.length; i++) {
			vms.add(new VM((VirtualMachine) mes[i]));
		}
	}
	
	public long totalCpuMhz() {
		HostCpuInfo cpuInfo = host.getHardware().getCpuInfo();
		return cpuInfo.getHz() * cpuInfo.getNumCpuCores() / 1024;
	}
	
	public String getName() {
		return host.getName();
	}	
	
	public List<VM> getVMs() {
		return vms;
	}
	
	public HostSystem getHost() {
		return host;
	}
	
}
