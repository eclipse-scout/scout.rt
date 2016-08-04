package org.eclipse.scout.rt.testing.platform.runner;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

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
  private final Class<?> m_annotatedClass;
  private final RunnerBuilder m_builder;

  public InteractiveTestSuite(Class<?> klass, RunnerBuilder builder) throws Throwable {
    m_annotatedClass = klass;
    m_builder = builder;
  }

  @Override
  public Description getDescription() {
    return Description.createSuiteDescription(InteractiveTestSuite.class.getSimpleName());
  }

  @Override
  public void run(final RunNotifier notifier) {
    System.out.println("Started interactive test console. (Auto-closing in 30 seconds when no input is entered, assuming it is a ci-test-run)");
    String lastLine = "";
    while (true) {
      try {
        //com.bsiag.bsicrm.server.test.MyTest
        //com.bsiag.bsicrm.server.task.Sql92ExtendedEntityTest
        System.out.println("********");
        System.out.println("[Enter fully qualified test class name, enter to repeat last test, '.' to exit]");
        if (!checkIfHumanInterface()) {
          return;
        }
        String line = new BufferedReader(new InputStreamReader(System.in)).readLine();
        if (line.isEmpty()) {
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
        try {
          runMethod = m_annotatedClass.getMethod("run", Runner.class, RunNotifier.class);
        }
        catch (Throwable t) { // NOSONAR
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
        System.out.println("Cannot load test " + lastLine + ": " + ex);
      }
    }
  }

  public static void run(Runner runner, RunNotifier notifier) throws Exception {
    RunListenerImpl listener = new RunListenerImpl();
    try {
      notifier.addListener(listener);
      runner.run(notifier);
    }
    finally {
      notifier.removeListener(listener);
    }
  }

  private boolean m_humanInterface;

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

  private static class RunListenerImpl extends RunListener {
    private Failure m_failure;

    @Override
    public void testStarted(Description description) throws Exception {
      m_failure = null;
    }

    @Override
    public void testFailure(Failure failure) throws Exception {
      m_failure = failure;
    }

    @Override
    public void testFinished(Description description) throws Exception {
      if (m_failure == null) {
        System.out.println(String.format("%s#%s - SUCCESS", description.getClassName(), description.getMethodName()));
      }
      else {
        System.err.println(String.format("%s#%s - FAILED - %s", description.getClassName(), description.getMethodName(), m_failure));
      }
    }
  }

}
