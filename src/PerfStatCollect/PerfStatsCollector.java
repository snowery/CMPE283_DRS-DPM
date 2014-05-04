package PerfStatCollect;

import java.net.URL;

import com.vmware.vim25.HostCpuInfo;
import com.vmware.vim25.ResourceAllocationInfo;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;

public class PerfStatsCollector {

	private static void collectStat(ServiceInstance si) throws Exception {
		Folder vCenterFolder = si.getRootFolder();
		ManagedEntity[] vHosts =
				new InventoryNavigator(vCenterFolder).searchManagedEntities("HostSystem");

		if (vHosts.length != 0) {
			for (int i = 0; i < vHosts.length; i++) {
				HostSystem vhost = (HostSystem) vHosts[i];
				HostCpuInfo cpuInfo = vhost.getHardware().getCpuInfo();
				System.out.println("Host total cpu(hz): " + cpuInfo.getHz()
						* cpuInfo.getNumCpuCores());
				PerfMgr.getPerf(vhost);

				ManagedEntity[] vms =
						new InventoryNavigator(vhost).searchManagedEntities("VirtualMachine");
				if (vms.length != 0)
					for (int j = 0; j < vms.length; j++) {
						VirtualMachine vm = (VirtualMachine) vms[j];
						ResourceAllocationInfo cpuAllocInfo = vm.getConfig().getCpuAllocation();
						System.out.println(String.format("VM cpi (mhz): Limit: %d Reservation: %d Share: %d", cpuAllocInfo.getLimit(), cpuAllocInfo.getReservation(), cpuAllocInfo.getShares().getShares()));
						PerfMgr.getPerf(vm);
					}
			}
		}
	}

	public static void main(String[] args) throws Exception {

		ServiceInstance si =
				new ServiceInstance(new URL(Setting.VcenterUrl), Setting.UserName, Setting.Password, true);
		PerfMgr.setUp(si);

		while (true) {
			try {
				collectStat(si);
				Thread.sleep(5000);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

	}

}