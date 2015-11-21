package ch.usi.dag.profiler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import ch.usi.dag.disl.Reflection.Class;
import ch.usi.dag.disl.Reflection.MissingClassException;
import ch.usi.dag.disl.annotation.GuardMethod;
import ch.usi.dag.disl.guardcontext.ReflectionStaticContext;

public abstract class GuardActors {


	static Set <String> classesToCheck = new HashSet<> ();
	
	static String[] classesToCheckArray = {
			"akka/actor/Actor", "akka/actor/UntypedActor",
	        "scala/actors/Actor", "org/jetlang/fibers/Fiber", "fj/control/parallel/Actor",
	        "groovyx/gpars/actor/Actor", "edu/rice/hj/api/HjActor", "fi/jumi/actors/Actors",
	        "net/liftweb/actor/LiftActor","scalaz/consurrent/Actor"};


	static Map <String, Boolean> cachedResults = new HashMap <> ();
	static Set <String> reportedMissing = new HashSet <> ();

	
	static {
		for (int i = 0; i < classesToCheckArray.length; i++)
			classesToCheck.add(classesToCheckArray[i]);
	}

	@GuardMethod
	public static boolean isActor (final ReflectionStaticContext rsc) {
		return __checkClasses (rsc.thisClass (), cachedResults, classesToCheck, reportedMissing);
	}
	
	private static boolean __checkClasses (
			final Class leafClass, final Map <String, Boolean> resultCache,
			final Set <String> classesToCheck, final Set <String> reportedMissing
			) {
		//
		// Check if we handled this class before.
		// If yes, return the cached result, otherwise perform the check.
		// If a class is missing, return false but do not cache the result.
		//
		final String className = leafClass.internalName ();

		final Boolean cachedResult = resultCache.get (className);
		if (cachedResult == null) {
			try {
				final boolean matches = __doCheckClasses (leafClass, classesToCheck);
				if (matches) {
					//System.err.println ("matched!!  "+ leafClass.internalName ());
				}

				resultCache.put (className, matches);
				return matches;

			} catch (final MissingClassException cnle) {
				if (!reportedMissing.contains (cnle.classInternalName ())) {
					reportedMissing.add (cnle.classInternalName ());
					//System.err.println ("warning: "+ cnle.getMessage ());
				}
				return false;
			}

		} else {
			return cachedResult;
		}
	}
	
	private static boolean __doCheckClasses (
			final Class leafClass, final Set <String> classes
			) {
		//
		// Check the leaf class first, and continue up the inheritance
		// hierarchy until either a match is found, or we hit the roof.
		//
		Optional <Class> nextClass = Optional.of (leafClass);
		do {
			final Class checkClass = nextClass.get ();
			if (__isAnyOf (checkClass, classes)) {
				return true;
			}

			nextClass = checkClass.superClass ();
		} while (nextClass.isPresent ());

		// No match found.
		return false;
	}

	
	private static boolean __isAnyOf (
			final Class cl, final Set <String> classesToCheck
			) {
		
		return classesToCheck.contains(cl.internalName());
	}

}
