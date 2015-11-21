package ch.usi.dag.profiler.metrics;

import java.io.FileNotFoundException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import ch.usi.dag.profiler.Dumper;
import ch.usi.dag.profiler.FileDumper;

public class Profiler {

	private static final ReentrantLock globalLock = new ReentrantLock();

	private static volatile String currentTest;
	private static final Map<String, AtomicLong> allocations;
	private static final Map<String, AtomicLong> invocations;

	static {
		currentTest = null;
		allocations = new ConcurrentHashMap<>();
		invocations = new ConcurrentHashMap<>();
		Runtime.getRuntime().addShutdownHook(new Thread(Profiler::dump));

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				dump();
			}
		}));
	}

	public static void dump() {
		dump(allocations, "allocations-");
		dump(invocations, "invocations-");
	}

	public static void dump(Map<String, AtomicLong> map, String prefix) {
		try (Dumper dumper = new FileDumper(
				prefix + "results" + java.lang.management.ManagementFactory.getRuntimeMXBean().getName())) {
			map.entrySet().forEach(r -> dumper.println(r.getKey() + " " + r.getValue()));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void startTest(String m) {
		globalLock.lock();
		currentTest = m;
	}

	public static void endTest(String m) {
		currentTest = null;
		globalLock.unlock();
	}

	public static void profileAllocation() {
		if (globalLock.isLocked() && currentTest != null) {
			String test = currentTest;
			if (test != null) {
				allocations.computeIfAbsent(test, k -> new AtomicLong()).incrementAndGet();
			}
		}
	}

	public static void profileInvocation() {
		if (globalLock.isLocked()) {
			String test = currentTest;
			if (test != null) {
				invocations.computeIfAbsent(test, k -> new AtomicLong()).incrementAndGet();
			}
		}
	}

}
