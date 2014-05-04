package PerfStatCollect;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

import com.vmware.vim25.PerfCounterInfo;
import com.vmware.vim25.PerfEntityMetricBase;
import com.vmware.vim25.PerfEntityMetricCSV;
import com.vmware.vim25.PerfMetricId;
import com.vmware.vim25.PerfMetricSeriesCSV;
import com.vmware.vim25.PerfProviderSummary;
import com.vmware.vim25.PerfQuerySpec;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.PerformanceManager;
import com.vmware.vim25.mo.ServiceInstance;

public class PerfMgr {
	private static PerformanceManager perfMgr;
	private static HashMap<Integer, PerfCounterInfo> countersInfoMap;
	private static HashMap<String, Integer> countersMap;
	private static PerfMetricId[] pmis;
	private static Logger logger = Logger.getLogger(PerfMgr.class);

	public final static void setUp(ServiceInstance vCenter) throws Exception {
		loggerConfig();
		perfMgr = vCenter.getPerformanceManager();
		PerfCounterInfo[] pcis = perfMgr.getPerfCounter();

		countersInfoMap = new HashMap<Integer, PerfCounterInfo>();
		countersMap = new HashMap<String, Integer>();
		for (int i = 0; i < pcis.length; i++) {
			countersInfoMap.put(pcis[i].getKey(), pcis[i]);
			countersMap.put(pcis[i].getGroupInfo().getKey() + "." + pcis[i].getNameInfo().getKey()
					+ "." + pcis[i].getRollupType(), pcis[i].getKey());
		}

		pmis = createPerfMetricId(Setting.PerfCounters);
		System.out.println("Performance manager is set up.");
	}

	public static int getCpuAvg(ManagedEntity me, int mins) throws Exception {
		PerfProviderSummary pps = perfMgr.queryPerfProviderSummary(me);
		Integer counterId = countersMap.get("cpu.usagemhz.average");
		
		// set cpu metric
		PerfMetricId metricId = new PerfMetricId();
		metricId.setCounterId(counterId);
		metricId.setInstance("*");
		
		// set query spec
		int refreshRate = pps.getRefreshRate().intValue();
		Calendar endTime = Calendar.getInstance();
		Calendar startTime = (Calendar) endTime.clone();
		startTime.add(Calendar.MINUTE, -mins);
		
		PerfQuerySpec qSpec = new PerfQuerySpec();
		qSpec.setEntity(me.getMOR());
		qSpec.setMetricId(new PerfMetricId[] {metricId});
		qSpec.setFormat("csv");
		qSpec.setIntervalId(new Integer(refreshRate));
		// set the time from which statistics are to be retrieved
		qSpec.setStartTime(startTime);
		qSpec.setEndTime(endTime);
		
		// query cpu usage in last 5 minutes		
		PerfEntityMetricBase[] pValues = perfMgr
				.queryPerf(new PerfQuerySpec[] { qSpec });
		int total = 0;
		int n = 0;
		int avg = 0;
		if (pValues != null) {
			for (PerfEntityMetricBase pValue : pValues) {
				PerfEntityMetricCSV pem = (PerfEntityMetricCSV) pValue;
				PerfMetricSeriesCSV[] csvs = pem.getValue();
				for (PerfMetricSeriesCSV csv : csvs) {
					//System.out.println(csv.getValue());
					String[] samples = csv.getValue().split(",");
					
					for (String s: samples) {
						total += Integer.parseInt(s);
						n++;
					}
				}
			}
			avg = total/n;
		}
		return avg;
	}	
	public static void getPerf(ManagedEntity me) throws Exception {
		PerfProviderSummary pps = perfMgr.queryPerfProviderSummary(me);
		int refreshRate = pps.getRefreshRate().intValue();

		// only return the latest one sample
		PerfQuerySpec qSpec = createPerfQuerySpec(me, 1, refreshRate);

		PerfEntityMetricBase[] pValues = perfMgr.queryPerf(new PerfQuerySpec[] { qSpec });
		if (pValues != null) {
			getValues(me, pValues);
		}
	}

	private static PerfMetricId[] createPerfMetricId(String[] counters) {
		PerfMetricId[] metricIds = new PerfMetricId[counters.length];
		for (int i = 0; i < counters.length; i++) {
			PerfMetricId metricId = new PerfMetricId();
			metricId.setCounterId(countersMap.get(counters[i]));
			metricId.setInstance("*");
			metricIds[i] = metricId;
		}
		return metricIds;
	}

	private static PerfQuerySpec createPerfQuerySpec(ManagedEntity me, int maxSample, int interval) {

		PerfQuerySpec qSpec = new PerfQuerySpec();
		qSpec.setEntity(me.getMOR());
		// set the maximum of metrics to be return
		qSpec.setMaxSample(new Integer(maxSample));
		qSpec.setMetricId(pmis);
		qSpec.setFormat("csv");
		qSpec.setIntervalId(new Integer(interval));

		return qSpec;
	}

	private static void getValues(ManagedEntity me, PerfEntityMetricBase[] values) {
		for (int i = 0; i < values.length; ++i) {
			getPerfMetricCSV(me, (PerfEntityMetricCSV) values[i]);
		}
	}

	private static void getPerfMetricCSV(ManagedEntity me, PerfEntityMetricCSV pem) {
		PerfMetricSeriesCSV[] csvs = pem.getValue();

		HashMap<Integer, PerfMetricSeriesCSV> stats = new HashMap<Integer, PerfMetricSeriesCSV>();

		for (int i = 0; i < csvs.length; i++) {
			stats.put(csvs[i].getId().getCounterId(), csvs[i]);
		}
		System.out.println("Log stats " + me.getName());

		HashMap<String, List<EventEntry>> events = new HashMap<String, List<EventEntry>>();

		for (String counter : Setting.PerfCounters) {
			Integer counterId = countersMap.get(counter);
			PerfCounterInfo pci = countersInfoMap.get(counterId);
			String value = null;
			if (stats.containsKey(counterId))
				value = stats.get(counterId).getValue();

			EventEntry event = new EventEntry(me, pci, value);
			String key = event.getEventKey();
			if (!events.containsKey(key)) {
				events.put(key, new ArrayList<EventEntry>());
			}
			events.get(key).add(event);
		}

		// combine event entries by metric type
		for (String key : events.keySet()) {
			String message = key + " ";
			List<EventEntry> lst = events.get(key);
			for (EventEntry event : lst) {
				if (event.isValid()) {
					message += event.toString() + " ";
				}
			}

			if (!message.equals(key + " ")) {
				logger.info(message);
			}
		}
	}

	private static void loggerConfig() {
		RollingFileAppender fa = new RollingFileAppender();
		fa.setName("FileLogger");
		fa.setFile("perf.log");
		fa.setMaxFileSize("1MB");
		fa.setMaxBackupIndex(1);
		fa.setLayout(new PatternLayout("%d{yyyy-MM-dd HH:mm:ss} %-5p %m%n"));
		fa.setThreshold(Level.INFO);
		fa.setAppend(true);
		fa.activateOptions();

		logger.addAppender(fa);
	}
}