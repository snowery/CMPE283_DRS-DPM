package Management;

import java.util.List;

import org.apache.log4j.Logger;

import Instances.VHost;
import Instances.VM;

import com.vmware.vim25.mo.ServiceInstance;

public class DPM extends Manager implements Runnable{

	private static final Logger log = Logger.getLogger(DPM.class);

	public DPM(ServiceInstance si) throws Exception {
		super(si);

	}
	public void start()  {
		while (true) {
			VHost underloadHost = getUnderloadVhost();
			if (underloadHost != null) {
				VHost targetVHost = null;
				targetVHost = getTargetVHost(underloadHost, getAdjustment(underloadHost));
				if (targetVHost != null) {
					List<VM> vms = underloadHost.getVMs();
					try {
						for (VM vm : vms)
							vm.migrate(targetVHost);
					} catch (Exception e) {
						e.printStackTrace();
						log.warn("migration failed, please go to check your vCenter.");
					}
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

	private VHost getTargetVHost(VHost underloadVHostint, int adjustment) {
		for (int i = 0; i < vHosts.size(); ++i) {
			if(underloadVHostint != vHosts.get(i)){
				if (!isOverloadAfterMigrate(vHosts.get(i), adjustment)) {
					return vHosts.get(i);
				}
			}		
		}
		return null;
	}

	private int getAdjustment(VHost host) {
		int adm = 0;
		for (VM vm : host.getVMs()) {
			adm += vm.cpuUsageMhz();
		}
		return adm;
	}

	private VHost getUnderloadVhost() {
		for (int i = 0; i < vHosts.size(); ++i) {
			if (this.isUnderload(vHosts.get(i))) {
				return vHosts.get(i);
			}
		}
		return null;
	}
	@Override
	public void run() {
		start();
	}
}
