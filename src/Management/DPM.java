package Management;

import java.util.List;

import org.apache.log4j.Logger;

import Instances.VHost;
import Instances.VM;
import Management.Manager;

import com.vmware.vim25.mo.ServiceInstance;

public class DPM extends Manager implements Runnable{

	private static final Logger log = Logger.getLogger(DPM.class);

	public DPM(ServiceInstance si) throws Exception {
		super(si);

	}
	public void start() throws Exception  {
		while (true) {
			// find the vhost that cpu load is lower than the threshold
			VHost underloadHost = getUnderloadVhost();
			if (underloadHost != null) {
				VHost targetVHost = null;
				// find the target vhost that can consolidate the vms on this vhost
				targetVHost = getTargetVHost(underloadHost, getAdjustment(underloadHost));
				if (targetVHost != null) {
					// migrate all the vms on this vhost to the target vhost
					List<VM> vms = underloadHost.getVMs();
					try {
						for (VM vm : vms)
							vm.migrate(targetVHost);
					} catch (Exception e) {
						e.printStackTrace();
						log.warn("migration failed, please go to check your vCenter.");
					}
					// shut down this vhost
					powerOff(underloadHost);
				}
			}
			
			try {
				Thread.sleep(1000 * 3);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private boolean powerOff(VHost vhost) throws Exception{
		return vhost.powerOff();
	}

	private VHost getTargetVHost(VHost underloadVHostint, int adjustment) throws Exception {
		List<VHost> vHosts = getPoweredOnHosts();
		for (int i = 0; i < vHosts.size(); ++i) {
			if(!underloadVHostint.getName().equals(vHosts.get(i).getName())){
				if (!isOverloadAfterMigrate(vHosts.get(i), adjustment)) {
					return vHosts.get(i);
				}
			}		
		}
		return null;
	}

	private int getAdjustment(VHost host) throws Exception {
		int adm = 0;
		for (VM vm : host.getVMs()) {
			adm += vm.cpuUsageMhz();
		}
		return adm;
	}

	private VHost getUnderloadVhost() throws Exception {
		List<VHost> vHosts = getPoweredOnHosts();
		for (int i = 0; i < vHosts.size(); ++i) {
			if (this.isUnderload(vHosts.get(i))) {
				return vHosts.get(i);
			}
		}
		return null;
	}
	@Override
	public void run() {
		try {
			start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
