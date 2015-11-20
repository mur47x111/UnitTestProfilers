package ch.usi.dag.profiler.metricsPlusID;

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
	private static final Map<String, AtomicLong> invocationsID;

	static {
		currentTest = null;
		allocations = new ConcurrentHashMap<>();
		invocations = new ConcurrentHashMap<>();
		invocationsID = new ConcurrentHashMap<>();
		Runtime.getRuntime().addShutdownHook(new Thread(Profiler::dump));
	}

	public static void dump() {
		dump(allocations, "allocations-");
		dump(invocations, "invocations-");
		dump(invocationsID, "invocationsID-");
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
		try {
			if (globalLock.isLocked()) 
				allocations.computeIfAbsent(currentTest, k -> new AtomicLong()).incrementAndGet();
			}
		catch (NullPointerException e) {
				e.printStackTrace();
			}
			
	}

	public static void profileInvocation() {
		try {
			if (globalLock.isLocked()) 
				invocations.computeIfAbsent(currentTest, k -> new AtomicLong()).incrementAndGet();
			}
		catch (NullPointerException e) {
			e.printStackTrace();
		}
	}
	
	public static void profileInvocationInvokeDynamic() {
		try {
			if (globalLock.isLocked()) 
				invocationsID.computeIfAbsent(currentTest, k -> new AtomicLong()).incrementAndGet();
			}
		catch (NullPointerException e) {
				e.printStackTrace();
			}
	}

}
