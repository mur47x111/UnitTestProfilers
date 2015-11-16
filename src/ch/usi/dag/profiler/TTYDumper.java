package ch.usi.dag.profiler;

public class TTYDumper implements Dumper {
    public void close() {}
    public void println(String s) {
        System.out.println(s);
    }
}