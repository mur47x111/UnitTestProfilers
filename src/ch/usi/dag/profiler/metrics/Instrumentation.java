package ch.usi.dag.profiler.metrics;

import ch.usi.dag.disl.annotation.After;
import ch.usi.dag.disl.annotation.AfterReturning;
import ch.usi.dag.disl.annotation.Before;
import ch.usi.dag.disl.marker.BodyMarker;
import ch.usi.dag.disl.marker.BytecodeMarker;
import ch.usi.dag.disl.staticcontext.MethodStaticContext;
import ch.usi.dag.profiler.GuardUnitTest;

public class Instrumentation {

	@Before(marker = BodyMarker.class, guard = GuardUnitTest.class)
	static void onMethodEntry(MethodStaticContext msc) {
		Profiler.startTest(msc.thisMethodFullName());
	}

	@After(marker = BodyMarker.class, guard = GuardUnitTest.class)
	static void onMethodExit(MethodStaticContext msc) {
		Profiler.startTest(msc.thisMethodFullName());
	}

	@AfterReturning(marker = BytecodeMarker.class, args = "new")
	static void profileAllocation() {
		Profiler.profileAllocation();
	}

	@AfterReturning(marker = BytecodeMarker.class, args = "invokevirtual, invokespecial, invokestatic, invokeinterface, invokedynamic")
	public static void profileInvocation() {
		Profiler.profileInvocation();
	}

}
