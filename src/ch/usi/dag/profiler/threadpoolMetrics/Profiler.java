package ch.usi.dag.profiler.threadpoolMetrics;

import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import ch.usi.dag.profiler.Dumper;
import ch.usi.dag.profiler.FileDumper;

public class Profiler {

	private static final ReentrantLock globalLock = new ReentrantLock();

	private static volatile String currentTest;
	private static final Map<String, AtomicLong> allocations;
	private static final Map<String, AtomicLong> invocations;
	private static volatile Set<String> validated;

	static {
		currentTest = null;
		allocations = new ConcurrentHashMap<>();
		invocations = new ConcurrentHashMap<>();
		validated = new ConcurrentSkipListSet<>();
		Runtime.getRuntime().addShutdownHook(new Thread(Profiler::dump));
	}

	public static void dump() {
		dump(allocations, "allocations-");
		dump(invocations, "invocations-");
		dump(validated, "validated-");
	}

	public static void dump(Map<String, AtomicLong> map, String prefix) {
		try (Dumper dumper = new FileDumper(
				prefix + "results" + java.lang.management.ManagementFactory.getRuntimeMXBean().getName())) {
			map.entrySet().forEach(r -> dumper.println(r.getKey() + " " + r.getValue()));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void dump(Set<String> set, String prefix) {
		try (Dumper dumper = new FileDumper(
				prefix + "results" + java.lang.management.ManagementFactory.getRuntimeMXBean().getName())) {
			for (String s: validated)
				dumper.println("THREADPOOL " + s);			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void startTest(String m) {
		globalLock.lock();
		currentTest = m;
	}

	public static void endTest(String m) {
		if (!validated.contains(currentTest)) {
			allocations.remove(currentTest);
			invocations.remove(currentTest);
		}			
		currentTest = null;
		globalLock.unlock();
	}

	public static void profileAllocation() {
		if (globalLock.isLocked()) {
			allocations.computeIfAbsent(currentTest, k -> new AtomicLong()).incrementAndGet();
		}
	}

	public static void profileInvocation() {
		if (globalLock.isLocked()) {
			invocations.computeIfAbsent(currentTest, k -> new AtomicLong()).incrementAndGet();
		}
	}

	public static void validate() {

		/* This method is called in any Object implementing a ThreadPool-related interface.
		 * We just need to check that the global lock is locked, and that was locked by the same thread as the current one */  

		//System.out.println("Validate() entered! Method: " +currentTest + " locked: "+ globalLock.isLocked()+ " thread: "+ globalLock.isHeldByCurrentThread());
		try {

			if (globalLock.isLocked() && globalLock.isHeldByCurrentThread()) {
				//if (globalLock.isLocked()) {
				// here we assume no invocation from the underlying framework
				validated.add(currentTest);
				//System.out.println("YES!");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
}
