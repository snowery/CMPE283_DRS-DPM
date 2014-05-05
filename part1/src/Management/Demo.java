package Management;

import java.net.URL;

import PerfStatCollect.PerfMgr;
import PerfStatCollect.Setting;

import com.vmware.vim25.mo.ServiceInstance;

public class Demo {
	
	public static void main(String[] args) throws Exception {
		
		ServiceInstance si = new ServiceInstance(new URL(Setting.VcenterUrl),
				Setting.UserName, Setting.Password, true);
		
		Manager drs = new DRS(si);
		Manager dpm = new DPM(si);
		Manager drs1 = new DRS1(si);
		PerfMgr.setUp(si);
		
		//drs.start();
		//dpm.start();
		//drs1.start();
	}

}
