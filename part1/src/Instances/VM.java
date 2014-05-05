package Instances;

import PerfStatCollect.PerfMgr;

import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.TaskInfo;
import com.vmware.vim25.VirtualMachineCloneSpec;
import com.vmware.vim25.VirtualMachineMovePriority;
import com.vmware.vim25.VirtualMachineRelocateSpec;
import com.vmware.vim25.mo.ComputeResource;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

public class VM {

	private VirtualMachine vm;
	
	public VM(VirtualMachine vm) {
		this.vm = vm;
	}
	
	public long cpuUsageMhz(int mins) throws Exception {
		return PerfMgr.getCpuAvg(vm, mins);
	}
	
	//defualt method return the average cpu usage in last five minutes
	public long cpuUsageMhz() throws Exception {
		return PerfMgr.getCpuAvg(vm, 5);
	}
	
	public long getLimit() {
		return vm.getConfig().getCpuAllocation().getLimit();
	}
	
	public long getReservation() {
		return vm.getConfig().getCpuAllocation().getReservation();
	}
	
	public int getShares() {
		return vm.getConfig().getCpuAllocation().getShares().getShares();
	}
	
	public String getName() {
		return vm.getName();
	}
	
	public VirtualMachine getVM() {
		return vm;
	}
	
	public boolean migrate(VHost newhost) throws Exception {
		HostSystem newHost = newhost.getHost();
		ComputeResource cr = (ComputeResource) newHost.getParent();

		System.out.println("Start migration......");
		// migrate no matter the vm's power state is on or off
		Task task = vm.migrateVM_Task(cr.getResourcePool(), newHost,
				VirtualMachineMovePriority.highPriority, null);

		if (task.waitForTask() == Task.SUCCESS) {
			System.out.println(vm.getName() + " is migrated to host "
					+ newHost.getName());
			return true;
		} else {
			System.out.println(vm.getName() + " migration failed!");
			TaskInfo info = task.getTaskInfo();
			System.out.println(info.getError().getFault());
		}
		return false;
	}
	
	public boolean clone(VHost newhost) throws Exception {
		VirtualMachineCloneSpec cloneSpec = new VirtualMachineCloneSpec();
		VirtualMachineRelocateSpec locationSpec = new VirtualMachineRelocateSpec();
		locationSpec.setHost(newhost.getHost().getMOR());
		locationSpec.setPool((ManagedObjectReference)newhost.getHost().getParent().getPropertyByPath("resourcePool"));
		
		cloneSpec.setLocation(locationSpec);
		cloneSpec.setPowerOn(false);
		cloneSpec.setTemplate(false);

		Task task = vm.cloneVM_Task((Folder) vm.getParent(), vm.getName()
				+ "-Clone", cloneSpec);
		System.out.println("Launching the VM clone task. " + "Please wait ...");

		if (task.waitForTask() == Task.SUCCESS) {
			System.out.println(getName() + ": VM got cloned successfully.");
			return true;
		} else {
			System.out.println(getName() + ": VM cannot be cloned");
			TaskInfo info = task.getTaskInfo();
			System.out.println(info.getError().getFault());
			return false;
		}
	}
}
