package org.eclipse.scout.rt.testing.platform.runner;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.scout.rt.platform.util.StringUtility;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.RunnerBuilder;

/**
 * Suite used to launch interactive text runs in the Console view.
 * <p>
 * A class annotated with {@link InteractiveTestSuite} launches a JUnit test runner that waits for user console input.
 * <p>
 * The class annotated with {@link InteractiveTestSuite} may implement a custom runner method that runs these tests.
 * <code>public void run(Runner runner, RunNotifier notifier){...}</code>
 * <p>
 * By default {@link InteractiveTestSuite#run(RunNotifier, Runner)} is called.
 * <p>
 * Note that unfortunately the eclipse junit integration doesn't support dynamic test runs in its GUI view ( "Unroooted
 * Tests") . However, the console output will inform you correctly about the test results.
 */
public class InteractiveTestSuite extends Runner {
  private boolean m_humanInterface;

  private final Class<?> m_annotatedClass;
  private final RunnerBuilder m_builder;

  public InteractiveTestSuite(Class<?> klass, RunnerBuilder builder) {
    m_annotatedClass = klass;
    m_builder = builder;
  }

  private boolean checkIfHumanInterface() {
    if (m_humanInterface) {
      return true;
    }
    try {
      long ts = System.currentTimeMillis();
      while (true) {
        if (System.in.available() > 0) {
          m_humanInterface = true;
          return true;
        }
        Thread.sleep(100L);
        if (System.currentTimeMillis() - ts > 30000L) {
          System.out.println("Auto-close.");
          throw new InterruptedException();
        }
      }
    }
    catch (Exception e) { // NOSONAR
      return false;
    }
  }

  @Override
  public Description getDescription() {
    return Description.createSuiteDescription(InteractiveTestSuite.class.getSimpleName());
  }

  @Override
  @SuppressWarnings({"squid:S1181", "squid:S1166"})
  public void run(final RunNotifier notifier) {
    System.out.println("Started interactive test console. (Auto-closing in 30 seconds when no input is entered, assuming it is a ci-test-run)");
    String lastLine = "";
    while (true) {
      try {
        System.out.println("********");
        System.out.println("[Enter fully qualified test class name, enter to repeat last test, '.' to exit]");
        if (!checkIfHumanInterface()) {
          return;
        }
        String line = new BufferedReader(new InputStreamReader(System.in)).readLine();
        if (StringUtility.isNullOrEmpty(line)) {
          if (!lastLine.isEmpty()) {
            line = lastLine;
          }
          else {
            continue;
          }
        }
        if (".".equalsIgnoreCase(line)) {
          return;
        }
        lastLine = line;
        Method runMethod;
        try { // NOSONAR
          runMethod = m_annotatedClass.getMethod("run", Runner.class, RunNotifier.class);
        }
        catch (Throwable t) {
          runMethod = InteractiveTestSuite.class.getMethod("run", Runner.class, RunNotifier.class);
        }
        Class<?> testClass = Class.forName(line, true, m_annotatedClass.getClassLoader());
        Runner runner = m_builder.safeRunnerForClass(testClass);
        if (Modifier.isStatic(runMethod.getModifiers())) {
          runMethod.invoke(null, runner, notifier);
        }
        else {
          runMethod.invoke(m_annotatedClass.newInstance(), runner, notifier);
        }
      }
      catch (Throwable ex) {
        System.out.println("Cannot load test " + lastLine);
        ex.printStackTrace(System.out);
      }
    }
  }

  public static void run(Runner runner, RunNotifier notifier) {
    RunListenerImpl listener = new RunListenerImpl();
    try {
      notifier.addListener(listener);
      runner.run(notifier);
    }
    finally {
      notifier.removeListener(listener);
      listener.showSummary();
    }
  }

  private static class RunListenerImpl extends RunListener {
    private Failure m_failure;
    private final Set<String> m_failedList = new LinkedHashSet<>();
    private final Set<String> m_successList = new LinkedHashSet<>();

    @Override
    public void testStarted(Description description) throws Exception {
      m_failure = null;
    }

    @Override
    public void testFailure(Failure failure) throws Exception {
      m_failure = failure;
      Description description = failure.getDescription();
      String msg = String.format("FAILED %s#%s - %s", description.getClassName(), description.getMethodName(), m_failure.getMessage());
      m_failedList.add(msg);
      System.err.println(msg);
      m_failure.getException().printStackTrace(System.err);
      System.err.flush();
      System.out.flush();
    }

    @Override
    public void testFinished(Description description) throws Exception {
      if (m_failure == null) {
        String msg = String.format("SUCCESS %s#%s", description.getClassName(), description.getMethodName());
        m_successList.add(msg);
        System.out.println(msg);
      }
      else {
        String msg = String.format("FAILED %s#%s - %s", description.getClassName(), description.getMethodName(), m_failure.getMessage());
        m_failedList.add(msg);
        System.err.println(msg);
        m_failure.getException().printStackTrace(System.err);
        System.err.flush();
        System.out.flush();
      }
    }

    private void showSummary() {
      if (m_failedList.isEmpty()) {
        System.out.println("SUMMARY: ALL " + m_successList.size() + " PASSED");
        return;
      }
      System.out.println("SUMMARY: " + (m_failedList.isEmpty() ? "" : m_failedList.size() + " FAILED ") + (m_successList.isEmpty() ? "" : m_successList.size() + " PASSED"));
      for (String s : m_failedList) {
        System.out.println(s);
      }
      for (String s : m_successList) {
        System.out.println(s);
      }
    }
  }

}
