package ch.usi.dag.profiler;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import org.objectweb.asm.Type;

import ch.usi.dag.disl.Reflection.Class;
import ch.usi.dag.disl.Reflection.MissingClassException;
import ch.usi.dag.disl.annotation.GuardMethod;
import ch.usi.dag.disl.guardcontext.ReflectionStaticContext;

public abstract class GuardThreadPool {


	static Set <Type> interfacesToCheck = Arrays.asList (
			Executor.class, ExecutorService.class).stream ().map (Type::getType).collect (Collectors.toSet ());


	static Map <String, Boolean> cachedResults = new HashMap <> ();
	static Set <String> reportedMissing = new HashSet <> ();


	@GuardMethod
	public static boolean isThreadPool (final ReflectionStaticContext rsc) {
		return __checkInterfaces (rsc.thisClass (), cachedResults, interfacesToCheck, reportedMissing);
	}
	
	private static boolean __checkInterfaces (
			final Class leafClass, final Map <String, Boolean> resultCache,
			final Set <Type> interfacesToCheck, final Set <String> reportedMissing
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
				final boolean matches = __doCheckInterfaces (leafClass, interfacesToCheck);
				if (matches) {
					System.err.println ("matched: "+ leafClass.internalName ());
				}

				resultCache.put (className, matches);
				return matches;

			} catch (final MissingClassException cnle) {
				if (!reportedMissing.contains (cnle.classInternalName ())) {
					reportedMissing.add (cnle.classInternalName ());
					System.err.println ("warning: "+ cnle.getMessage ());
				}
				return false;
			}

		} else {
			return cachedResult;
		}
	}
	
	private static boolean __doCheckInterfaces (
			final Class leafClass, final Set <Type> interfaces
			) {
		//
		// Check the leaf class first, and continue up the inheritance
		// hierarchy until either a match is found, or we hit the roof.
		//
		Optional <Class> nextClass = Optional.of (leafClass);
		do {
			final Class checkClass = nextClass.get ();
			if (__implementsAnyOf (checkClass, interfaces)) {
				return true;
			}

			nextClass = checkClass.superClass ();
		} while (nextClass.isPresent ());

		// No match found.
		return false;
	}

	
	private static boolean __implementsAnyOf (
			final Class cl, final Set <Type> interfacesToCheck
			) {
		return cl.interfaceTypes ().anyMatch (interfacesToCheck::contains);
	}

}
