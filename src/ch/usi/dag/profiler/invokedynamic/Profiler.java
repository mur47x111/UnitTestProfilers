package ch.usi.dag.profiler.invokedynamic;

import java.io.FileNotFoundException;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.locks.ReentrantLock;

import ch.usi.dag.profiler.Dumper;
import ch.usi.dag.profiler.FileDumper;

public class Profiler {

	private static final ReentrantLock globalLock = new ReentrantLock();

	private static volatile String currentTest;
	private static volatile Set<String> validated;
	
	static {
		currentTest = null;
		validated = new ConcurrentSkipListSet<>();
		Runtime.getRuntime().addShutdownHook(new Thread(Profiler::dump));
	}

	public static void dump() {
		try (Dumper dumper = new FileDumper(
				"results-validation" + java.lang.management.ManagementFactory.getRuntimeMXBean().getName())) {			
			for (String s: validated)
				dumper.println("INVOKEDYNAMIC " + s);
		
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

	public static void validate() {
			if (globalLock.isLocked() && globalLock.isHeldByCurrentThread()) {
				validated.add(currentTest);				
			}		
	}

}
