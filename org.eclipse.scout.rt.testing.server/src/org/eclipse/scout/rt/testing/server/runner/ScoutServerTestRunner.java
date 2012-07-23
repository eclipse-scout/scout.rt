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
package org.eclipse.scout.rt.testing.server.runner;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.testing.server.DefaultTestServerSessionProvider;
import org.eclipse.scout.rt.testing.server.ITestServerSessionProvider;
import org.eclipse.scout.rt.testing.shared.services.common.exceptionhandler.ProcessingRuntimeExceptionUnwrappingStatement;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

/**
 * JUnit test runner that runs the annotated test class within a Scout server job. Test cases executed by this runner
 * may be configured with {@link ServerTest} annotation.
 * <p/>
 * <h3>Sessions and Transactions</h3> The different methods of a test class driven by this runner are invoked in
 * different Scout server sessions and therefore in different Scout transactions (because a Scout transaction is
 * attached to one particular Scout session)>
 * <table border="1">
 * <tr>
 * <th>Element</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td><b>@BeforeClass</b></td>
 * <td>all methods annotated with {@link BeforeClass} are invoked in the very same Scout server session. Therefore they
 * share the same Scout transaction.</td>
 * </tr>
 * <tr>
 * <td><b>@Before, @Test, @After</b></td>
 * <td>each test case is invoked in a separate Scout server session and therefore in a separate Scout transaction. A
 * particular test cas consists of the following methods:
 * <ul>
 * <li>all methdos annotated with {@link Before}</li>
 * <li>the test case itself annotated with {@link Test}</li>
 * <li>all methdos annotated with {@link After}</li>
 * </ul>
 * </td>
 * </tr>
 * <tr>
 * <td><b>@AfterClass</b></td>
 * <td>all methods annotated with {@link AfterClass} are invoked in the very same Scout server session. Therefore they
 * share the same Scout transaction.</td>
 * </tr>
 * </table>
 */
public class ScoutServerTestRunner extends BlockJUnit4ClassRunner {

  private static Class<? extends IServerSession> s_defaultServerSessionClass;
  private static String s_defaultPrincipalName;
  private static ITestServerSessionProvider s_defaultServerSessionProvider = new DefaultTestServerSessionProvider();

  private LoginInfo m_loginInfo;

  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.TYPE, ElementType.METHOD})
  public @interface ServerTest {

    public Class<? extends IServerSession> serverSessionClass() default IServerSession.class;

    public Class<? extends ITestServerSessionProvider> sessionProvider() default NullTestServerSessionProvider.class;

    public String runAs() default "";
  }

  /**
   * Null-provider used as default value in the {@link ServerTest} annotation (since annotation values must not be
   * <code>null</code>).
   */
  public static interface NullTestServerSessionProvider extends ITestServerSessionProvider {
  }

  /**
   * @param klass
   * @throws InitializationError
   */
  public ScoutServerTestRunner(Class<?> klass) throws InitializationError {
    super(klass);
    try {
      m_loginInfo = getOrCreateServerSession(klass.getAnnotation(ServerTest.class), null);
    }
    catch (InitializationError e) {
      throw e;
    }
    catch (Exception e) {
      List<Throwable> errors = new ArrayList<Throwable>();
      errors.add(e);
      throw new InitializationError(errors);
    }
  }

  public static Class<? extends IServerSession> getDefaultServerSessionClass() {
    return s_defaultServerSessionClass;
  }

  public static void setDefaultServerSessionClass(Class<? extends IServerSession> defaultServerSessionClass) {
    s_defaultServerSessionClass = defaultServerSessionClass;
  }

  public static ITestServerSessionProvider getDefaultServerSessionProvider() {
    return s_defaultServerSessionProvider;
  }

  public static void setDefaultServerSessionProvider(ITestServerSessionProvider defaultServerSessionProvider) {
    s_defaultServerSessionProvider = defaultServerSessionProvider;
  }

  public static String getDefaultPrincipalName() {
    return s_defaultPrincipalName;
  }

  public static void setDefaultPrincipalName(String defaultPrincipalName) {
    s_defaultPrincipalName = defaultPrincipalName;
  }

  /**
   * @return Returns the default server session class used by this test runner. Defaults to
   *         {@link #getDefaultServerSessionClass()}. Subclasses may override this method to provide another default
   *         value.
   */
  protected Class<? extends IServerSession> defaultServerSessionClass() {
    return getDefaultServerSessionClass();
  }

  /**
   * @return Returns the default server session provider used by this test runner. Defaults to
   *         {@link #getDefaultServerSessionProvider()}. Subclasses may override this method to provide another default
   *         value.
   */
  protected ITestServerSessionProvider defaultServerSessionProvider() {
    return getDefaultServerSessionProvider();
  }

  /**
   * @return Returns the default principal name used by this test runner. Defaults to {@link #getDefaultPrincipalName()}
   *         Subclasses may override this method to provide another default value.
   */
  protected String defaultPrincipalName() {
    return getDefaultPrincipalName();
  }

  @Override
  protected Statement withBeforeClasses(Statement statement) {
    // run all methods annotated with @BeforeClass in a separate ServerSession
    List<FrameworkMethod> befores = getTestClass().getAnnotatedMethods(BeforeClass.class);
    return befores.isEmpty() ? statement : new RunBeforesInSeparateScoutServerSession(m_loginInfo.getServerSession(), m_loginInfo.getSubject(), statement, befores, null);
  }

  @Override
  protected Statement withAfterClasses(Statement statement) {
    // run all methods annotated with @AfterClass in a separate ServerSession
    List<FrameworkMethod> afters = getTestClass().getAnnotatedMethods(AfterClass.class);
    return afters.isEmpty() ? statement : new RunAftersInSeparateScoutServerSession(m_loginInfo.getServerSession(), m_loginInfo.getSubject(), statement, afters, null);
  }

  @Override
  protected Statement methodBlock(FrameworkMethod method) {
    LoginInfo loginInfo = m_loginInfo;
    ServerTest methodLevelClientTest = method.getAnnotation(ServerTest.class);
    if (methodLevelClientTest != null) {
      try {
        ServerTest classLevelClientTest = getTestClass().getJavaClass().getAnnotation(ServerTest.class);
        loginInfo = getOrCreateServerSession(classLevelClientTest, methodLevelClientTest);
      }
      catch (final Throwable e) {
        return new Statement() {
          @Override
          public void evaluate() throws Throwable {
            throw e;
          }
        };
      }
    }

    // run each test method in a separate ServerSession
    return new ScoutServerJobWrapperStatement(loginInfo.getServerSession(), loginInfo.getSubject(), super.methodBlock(method));
  }

  @Override
  @SuppressWarnings("deprecation")
  protected Statement possiblyExpectingExceptions(FrameworkMethod method, Object test, Statement next) {
    // unpack wrapped ProcessingExceptions and rethrow them
    return super.possiblyExpectingExceptions(method, test, new ProcessingRuntimeExceptionUnwrappingStatement(next));
  }

  protected LoginInfo getOrCreateServerSession(ServerTest classLevelServerTest, ServerTest methodLevelServerTest) throws Exception {
    // process default values
    Class<? extends IServerSession> serverSessionClass = defaultServerSessionClass();
    ITestServerSessionProvider sessionProvider = defaultServerSessionProvider();
    String runAs = defaultPrincipalName();

    // process class-level server test configuration
    if (classLevelServerTest != null) {
      serverSessionClass = extractSessionClass(classLevelServerTest, serverSessionClass);
      sessionProvider = extractSessionProvider(classLevelServerTest, sessionProvider);
      runAs = extractRunAs(classLevelServerTest, runAs);
    }

    // process method-level server test configuration
    if (methodLevelServerTest != null) {
      serverSessionClass = extractSessionClass(methodLevelServerTest, serverSessionClass);
      sessionProvider = extractSessionProvider(methodLevelServerTest, sessionProvider);
      runAs = extractRunAs(methodLevelServerTest, runAs);
    }

    // sanity check
    if (serverSessionClass == null) {
      throw new InitializationError("Server session class is not set. Either set the default server session using '"
          + ScoutServerTestRunner.class.getSimpleName()
          + ".setDefaultServerSessionClass' or annotate your test class and/or method with '"
          + ServerTest.class.getSimpleName() + "'");
    }

    // return existing or create new server session
    Subject subject = sessionProvider.login(runAs);
    IServerSession serverSession = sessionProvider.createServerSession(serverSessionClass, subject);
    return new LoginInfo(subject, serverSession);
  }

  /**
   * @param serverTest
   * @return
   */
  protected Class<? extends IServerSession> extractSessionClass(ServerTest serverTest, Class<? extends IServerSession> defaultValue) {
    if (serverTest == null || serverTest.serverSessionClass() == null || serverTest.serverSessionClass() == IServerSession.class) {
      return defaultValue;
    }
    return serverTest.serverSessionClass();
  }

  /**
   * @param serverTest
   * @return
   */
  protected ITestServerSessionProvider extractSessionProvider(ServerTest serverTest, ITestServerSessionProvider defaultValue) throws Exception {
    if (serverTest == null || serverTest.sessionProvider() == null || serverTest.sessionProvider() == NullTestServerSessionProvider.class) {
      return defaultValue;
    }
    return serverTest.sessionProvider().newInstance();
  }

  /**
   * @param serverTest
   * @return
   */
  protected String extractRunAs(ServerTest serverTest, String defaultValue) {
    String runAs = defaultValue;
    if (serverTest != null && serverTest.runAs() != null) {
      String s = serverTest.runAs().trim();
      if (s.length() > 0) {
        runAs = s;
      }
    }
    return runAs;
  }

  public static class LoginInfo {
    private final Subject m_subject;
    private final IServerSession m_serverSession;

    public LoginInfo(Subject subject, IServerSession serverSession) {
      m_serverSession = serverSession;
      m_subject = subject;
    }

    public Subject getSubject() {
      return m_subject;
    }

    public IServerSession getServerSession() {
      return m_serverSession;
    }
  }
}
