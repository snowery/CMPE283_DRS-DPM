package PerfStatCollect;

import com.vmware.vim25.PerfCounterInfo;
import com.vmware.vim25.mo.ManagedEntity;

public class EventEntry {

	private String machineType;

	private String machineName;

	private String metricType;

	private String metricName;

	private String metricUnit;

	private String value;

	public boolean isValid() {
		return (value != null && !value.startsWith("-"));
	}

	public String getEventKey() {
		return String.format("%s %s %s", machineType, machineName, metricType);
	}

	public EventEntry(ManagedEntity me, PerfCounterInfo pci, String value) {
		machineType = me.getClass().getSimpleName();
		machineName = me.getName();
		metricType = pci.getGroupInfo().getKey();
		metricName = pci.getNameInfo().getKey();
		metricUnit = pci.getUnitInfo().getKey();
		this.value = value;
		
		if(isValid() && metricUnit.equals("percent")) {
			try {
				this.value = Float.toString(Float.parseFloat(value) / 100f);
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	@Override
	public String toString() {
		return String.format("%s %s", metricName, value);
	}

}
