package ch.usi.dag.profiler;

public interface Dumper extends AutoCloseable {
    public void close();
    public void println(String s);
}
