package ch.usi.dag.profiler.time;

import java.io.FileNotFoundException;
import java.util.concurrent.ConcurrentHashMap;

import ch.usi.dag.profiler.Dumper;
import ch.usi.dag.profiler.FileDumper;

public class Profiler {

	public static ConcurrentHashMap<String, Long> startTimes;
	public static ConcurrentHashMap<String, Long> executionTimes;

	static {
		startTimes = new ConcurrentHashMap<>();
		executionTimes = new ConcurrentHashMap<>();
		Runtime.getRuntime().addShutdownHook(new Thread(Profiler::dump));
	}

	public static void dump() {
		try (Dumper dumper = new FileDumper(
				"results" + java.lang.management.ManagementFactory.getRuntimeMXBean().getName())) {
			executionTimes.entrySet().forEach(r -> dumper.println("TIME " + r.getKey() + " " + r.getValue()));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void startTimer(String m) {
		startTimes.put(m, Long.valueOf(System.nanoTime()));
	}

	public static void endTimer(String m) {
		long startTime = startTimes.get(m);
		executionTimes.put(m, Long.valueOf(System.nanoTime() - startTime));
	}
}
