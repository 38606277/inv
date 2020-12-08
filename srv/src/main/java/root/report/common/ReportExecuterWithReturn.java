package root.report.common;

@FunctionalInterface
public interface ReportExecuterWithReturn<T> {
	T execute() throws Exception;
}
