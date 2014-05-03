package PerfStatCollect;

public class Setting {

	public final static String UserName = "administrator";
	public final static String Password = "12!@qwQW";
	public final static String VcenterUrl = "https://130.65.132.150/sdk";

	public final static String[] PerfCounters = { "cpu.usage.average",
		"mem.usage.average", "disk.usage.average",
		"datastore.datastoreReadBytes.latest",
		"virtualDisk.readOIO.latest", "virtualDisk.writeOIO.latest" };
}
