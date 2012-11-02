/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.testing.shared;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Properties;
import java.util.regex.Pattern;

import junit.framework.JUnit4TestAdapter;
import junit.framework.TestResult;

import org.apache.tools.ant.taskdefs.optional.junit.JUnitResultFormatter;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest;
import org.apache.tools.ant.taskdefs.optional.junit.XMLJUnitResultFormatter;
import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.TypeCastUtility;
import org.eclipse.scout.commons.prefs.UserScope;
import org.eclipse.scout.commons.runtime.BundleBrowser;

/**
 * Runner for JUnit Plug-in tests.
 * <p>
 * <b>Note:</b>This class is similar to Eclipse's test framework. It is however built on top of JUnit4 and runs with
 * Eclipse version 3.5 and above. Additionally it does not require a test suite that lists all tests to be executed but
 * collects them using Scout's {@link BundleBrowser}.
 */
public class ScoutJUnitPluginTestExecutor {
  private static final String DUMMY_TESTSUITE_NAME = "FRAMEWORK.INIT";

  public static final String JUNIT_REPORTS_DIR_ARG_NAME = "junitReportsDir";
  public static final String FAIL_ON_ERROR_ARG_NAME = "failOnError";
  public static final String HALT_ON_FAILURE_ARG_NAME = "haltOnFailure";
  public static final String BUNDLE_NAME_INCLUDE_FILTER_ARG_NAME = "bundleNameIncludeFilter";
  public static final String BUNDLE_NAME_EXCLUDE_FILTER_ARG_NAME = "bundleNameExcludeFilter";
  public static final String CLASS_NAME_INCLUDE_FILTER_ARG_NAME = "classNameIncludeFilter";
  public static final String CLASS_NAME_EXCLUDE_FILTER_ARG_NAME = "classNameExcludeFilter";

  public static final Integer EXIT_CODE_OK = IApplication.EXIT_OK;
  public static final Integer EXIT_CODE_TESTS_FAILED = 1;
  public static final Integer EXIT_CODE_ERRORS_OCCURRED = 2;

  private final String m_reportsDir;
  private final boolean m_failOnError;
  private final boolean m_haltOnFailure;
  private final Pattern[] m_bundleNameIncludePatterns;
  private final Pattern[] m_bundleNameExcludePatterns;
  private final Pattern[] m_classNameIncludePatterns;
  private final Pattern[] m_classNameExcludePatterns;
  private final String m_launchingProductId;

  public ScoutJUnitPluginTestExecutor() {
    this(getReportsDirConfigParameter(), getFailOnErrorConfigParameter(), getHaltOnFailureConfigParameter(),
        getBundleNameIncludePatternsConfigParameter(), getBundleNameExcludePatternsConfigParameter(),
        getClassNameIncludePatternsConfigParameter(), getClassNameExcludePatternsConfigParameter());
  }

  public ScoutJUnitPluginTestExecutor(String reportsDir, boolean failOnError, boolean haltOnFailure,
      Pattern[] bundleNameIncludePatterns, Pattern[] bundleNameExcludePatterns,
      Pattern[] classNameIncludePatterns, Pattern[] classNameExcludePatterns) {

    m_failOnError = failOnError;
    m_haltOnFailure = haltOnFailure;
    m_bundleNameIncludePatterns = bundleNameIncludePatterns;
    m_bundleNameExcludePatterns = bundleNameExcludePatterns;
    m_classNameIncludePatterns = classNameIncludePatterns;
    m_classNameExcludePatterns = classNameExcludePatterns;

    if (reportsDir == null) {
      if (Platform.inDevelopmentMode()) {
        String s = System.getenv("APPDATA");
        if (s == null) {
          s = System.getProperty("user.home");
        }
        reportsDir = s + File.separator + "junit-james";
        System.out.println("In -dev mode: " + JUNIT_REPORTS_DIR_ARG_NAME + " defaults to " + reportsDir);
      }
      else {
        throw new IllegalArgumentException(JUNIT_REPORTS_DIR_ARG_NAME + " must not be null; check if argument '" + JUNIT_REPORTS_DIR_ARG_NAME + "' is set");
      }
    }

    m_reportsDir = reportsDir;
    checkAndCreateReportsDir(m_reportsDir);
    String productId = null;
    if (Platform.getProduct() != null) {
      productId = Platform.getProduct().getId();
    }
    m_launchingProductId = productId;
  }

  /**
   * Returns the configuration value for the given parameter that is either configured as
   * command line argument or as system property.
   * 
   * @param parameterName
   * @return
   */
  private static String getConfigParameter(String parameterName) {
    String commandLineArgumentName = "-" + parameterName + "=";
    for (String arg : Platform.getCommandLineArgs()) {
      if (arg != null && arg.startsWith(commandLineArgumentName)) {
        return arg.substring(commandLineArgumentName.length());
      }
    }
    return System.getProperty(parameterName);
  }

  /**
   * Returns the directory where JUnit reports are written to. This default implementation uses the following sources:
   * <ol>
   * <li>Plaltform's command line argument <code>-junitReportsDir=&lt;dir&gt;</code> (e.g.
   * <code>-reportsDir=C:\temp\junitreports</code>)</li>
   * <li>System property with name <code>reportsDir</code> (e.g. <code>-DjunitReportsDir=C:\temp\junitreports</code>)</li>
   * </ol>
   * 
   * @param context
   * @return
   */
  private static String getReportsDirConfigParameter() {
    return getConfigParameter(JUNIT_REPORTS_DIR_ARG_NAME);
  }

  private static void checkAndCreateReportsDir(String reportsDir) {
    File repDir = new File(reportsDir);
    if (repDir.exists() && repDir.isFile()) {
      throw new IllegalArgumentException("the given reports directory already exists and it is a file");
    }
    repDir.mkdirs();
  }

  /**
   * @return Returns an array with the configured bundle name patterns to include.
   * @see JUnitTestClassBrowser#parseFilterPatterns(String)
   */
  private static Pattern[] getBundleNameIncludePatternsConfigParameter() {
    return JUnitTestClassBrowser.parseFilterPatterns(getConfigParameter(BUNDLE_NAME_INCLUDE_FILTER_ARG_NAME));
  }

  /**
   * @return Returns an array with the configured bundle name patterns to exclude.
   * @see JUnitTestClassBrowser#parseFilterPatterns(String)
   */
  private static Pattern[] getBundleNameExcludePatternsConfigParameter() {
    return JUnitTestClassBrowser.parseFilterPatterns(getConfigParameter(BUNDLE_NAME_EXCLUDE_FILTER_ARG_NAME));
  }

  /**
   * @return Returns an array with the configured class name patterns to include.
   * @see JUnitTestClassBrowser#parseFilterPatterns(String)
   */
  private static Pattern[] getClassNameIncludePatternsConfigParameter() {
    return JUnitTestClassBrowser.parseFilterPatterns(getConfigParameter(CLASS_NAME_INCLUDE_FILTER_ARG_NAME));
  }

  /**
   * @return Returns an array with the configured class name patterns to exclude.
   * @see JUnitTestClassBrowser#parseFilterPatterns(String)
   */
  private static Pattern[] getClassNameExcludePatternsConfigParameter() {
    return JUnitTestClassBrowser.parseFilterPatterns(getConfigParameter(CLASS_NAME_EXCLUDE_FILTER_ARG_NAME));
  }

  private static boolean getFailOnErrorConfigParameter() {
    return TypeCastUtility.castValue(getConfigParameter(FAIL_ON_ERROR_ARG_NAME), boolean.class);
  }

  private static boolean getHaltOnFailureConfigParameter() {
    return TypeCastUtility.castValue(getConfigParameter(HALT_ON_FAILURE_ARG_NAME), boolean.class);
  }

  public boolean isFailOnError() {
    return m_failOnError;
  }

  public boolean isHaltOnFailure() {
    return m_haltOnFailure;
  }

  public String getReportsDir() {
    return m_reportsDir;
  }

  public int runAllTests() {
    int exitCode = EXIT_CODE_OK;
    try {
      JUnitTestClassBrowser browser = new JUnitTestClassBrowser();
      browser.setBundleNameIncludePatterns(m_bundleNameIncludePatterns);
      browser.setBundleNameExcludePatterns(m_bundleNameExcludePatterns);
      browser.setClassNameIncludePatterns(m_classNameIncludePatterns);
      browser.setClassNameExcludePatterns(m_classNameExcludePatterns);
      for (Class<?> test : browser.collectAllJUnitTestClasses()) {
        int testResultCode = runTest(test);
        exitCode = Math.max(exitCode, testResultCode);
        if (isHaltOnFailure() && exitCode != EXIT_CODE_OK) {
          break;
        }
      }
    }
    catch (Throwable t) {
      try {
        // create a dummy test suite so that the Exception is reported in the test results
        JUnitResultFormatter formatter = createJUnitResultFormatter(DUMMY_TESTSUITE_NAME);
        JUnitTest dummyTest = new JUnitTest(DUMMY_TESTSUITE_NAME);
        formatter.startTestSuite(dummyTest);
        formatter.addError(null, t);
        formatter.endTestSuite(dummyTest);
      }
      catch (FileNotFoundException e) {
        System.err.println(e);
      }
      exitCode = EXIT_CODE_ERRORS_OCCURRED;
    }
    if (isFailOnError()) {
      return exitCode;
    }
    return EXIT_CODE_OK;
  }

  public int runTest(Class<?> testClass) throws FileNotFoundException {
    int result = EXIT_CODE_OK;

    //clear preference cache of client
    try {
      new UserScope().getNode("org.eclipse.scout.rt.client").clear();
    }
    catch (Throwable t) {
      t.printStackTrace();
    }

    PrintStream sysOut = null;
    PrintStream sysErr = null;
    PrintStream oldSysOut = System.out;
    PrintStream oldSysErr = System.err;

    if (Platform.inDevelopmentMode()) {
      System.out.println(getFileNameFor(testClass.getName()));
    }

    try {
      // redirect sysout and syserr
      ByteArrayOutputStream outStrm = new ByteArrayOutputStream();
      sysOut = new PrintStream(outStrm);
      ByteArrayOutputStream errStrm = new ByteArrayOutputStream();
      sysErr = new PrintStream(errStrm);
      if (!Platform.inDevelopmentMode()) {
        System.setOut(sysOut);
        System.setErr(sysErr);
      }

      // create Ant JUnitTest that executes the test case
      JUnitTest junitTest = createJUnitTest(testClass.getName());
      JUnitResultFormatter formatter = createJUnitResultFormatter(testClass.getName());

      // run the test
      long start = System.currentTimeMillis();
      formatter.startTestSuite(junitTest);

      TestResult testResult = new TestResult();
      testResult.addListener(formatter);
      try {
        new JUnit4TestAdapter(testClass).run(testResult);
      }
      catch (Throwable t) {
        formatter.addError(null, t);
        result = EXIT_CODE_ERRORS_OCCURRED;
      }
      finally {
        formatter.setSystemOutput(new String(outStrm.toByteArray()));
        formatter.setSystemError(new String(errStrm.toByteArray()));
        junitTest.setCounts(testResult.runCount(), testResult.failureCount(), testResult.errorCount());
        junitTest.setRunTime(System.currentTimeMillis() - start);
        formatter.endTestSuite(junitTest);
        if (result == EXIT_CODE_OK) {
          if (testResult.errorCount() > 0) {
            result = EXIT_CODE_ERRORS_OCCURRED;
          }
          else if (testResult.failureCount() > 0) {
            result = EXIT_CODE_TESTS_FAILED;
          }
        }
      }
    }
    finally {
      if (sysOut != null) {
        sysOut.close();
        sysOut = null;
      }
      if (sysErr != null) {
        sysErr.close();
        sysErr = null;
      }
      System.setOut(oldSysOut);
      System.setErr(oldSysErr);
    }
    if (Platform.inDevelopmentMode()) {
      dumpResult(new File(getReportsDir() + File.separator + getFileNameFor(testClass.getName())), result);
    }
    return result;
  }

  /**
   * Creates a new Ant {@link JUnitTest} used to execute the test and for reporting its outcome.
   * 
   * @param testName
   * @return
   */
  private JUnitTest createJUnitTest(String testName) {
    JUnitTest junitTest = new JUnitTest(StringUtility.join("-", m_launchingProductId, testName));
    Properties props = new Properties();
    props.putAll(System.getProperties());
    junitTest.setProperties(props);
    return junitTest;
  }

  /**
   * Creates a {@link XMLJUnitResultFormatter} that writes its output to a file in the reports directory.
   * 
   * @param testName
   * @return
   * @throws FileNotFoundException
   */
  private XMLJUnitResultFormatter createJUnitResultFormatter(String testName) throws FileNotFoundException {
    XMLJUnitResultFormatter formatter = new XMLJUnitResultFormatter();
    formatter.setOutput(new FileOutputStream(getReportsDir() + File.separator + getFileNameFor(testName)));
    return formatter;
  }

  private String getFileNameFor(String testName) throws FileNotFoundException {
    if (m_launchingProductId != null) {
      return "TEST-" + m_launchingProductId + "-" + testName + ".xml";
    }
    else {
      return "TEST-" + testName + ".xml";
    }
  }

  private void dumpResult(File f, int exitCode) {
    if (f.isFile()) {
      if (exitCode == EXIT_CODE_OK) {
        //nop
      }
      else {
        System.out.println("FAILED " + f.getName());
        try {
          FileInputStream in = new FileInputStream(f);
          byte[] buf = new byte[(int) f.length()];
          in.read(buf);
          in.close();
          System.out.println(new String(buf, 0, buf.length));
        }
        catch (Throwable t) {
          System.out.println("ERROR: " + t);
        }
        if (isHaltOnFailure()) {
          System.exit(exitCode);
        }
      }
    }
  }
}
