package ch.usi.dag.profiler.threadpool;

import ch.usi.dag.disl.annotation.After;
import ch.usi.dag.disl.annotation.Before;
import ch.usi.dag.disl.marker.BodyMarker;
import ch.usi.dag.disl.staticcontext.MethodStaticContext;
import ch.usi.dag.profiler.GuardThreadPool;
import ch.usi.dag.profiler.GuardUnitTest;

public class Instrumentation {

	@Before(marker = BodyMarker.class, guard = GuardUnitTest.class)
	static void onMethodEntry(MethodStaticContext msc) {
		Profiler.startTest(msc.thisMethodFullName());
	}

	@After(marker = BodyMarker.class, guard = GuardUnitTest.class)
	static void onMethodExit(MethodStaticContext msc) {
		Profiler.endTest(msc.thisMethodFullName());
	}

	@Before(marker = BodyMarker.class, guard = GuardThreadPool.class)
	static void intercepted() {
		Profiler.validate();
	}

}
