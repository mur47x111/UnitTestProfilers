package ch.usi.dag.profiler;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class FileDumper implements Dumper {
	public PrintWriter p;

	public FileDumper(String fileName) throws FileNotFoundException {
		p = new PrintWriter(fileName);
	}

	public void close() {
		p.close();
	}

	public void println(String s) {
		p.println(s);
	}
}
