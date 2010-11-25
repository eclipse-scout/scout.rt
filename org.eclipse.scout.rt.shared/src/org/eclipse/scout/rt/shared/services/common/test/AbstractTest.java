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
package org.eclipse.scout.rt.shared.services.common.test;

import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.ConfigPropertyValue;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.service.AbstractService;

/**
 * Abstract test implementation. <br>
 * Implements the abstract methods and add a test status (or even multiple ones)
 * using one of the create..Status(String) methods and {@link #addStatus(TestStatus)} <br>
 * <p>
 * Normally the following methods are implemented in sublcasses <br>
 * {@link #getConfiguredProduct()} <br>
 * {@link #getConfiguredTitle()} default value is the class simple name without suffixes "UnitTest" and "Test" <br>
 * {@link #getConfiguredSubTitle()} default value is null <br>
 * {@link #run()}
 * <p>
 * Normally the following methods are called from within {@link #run()} <br>
 * {@link #addOkStatus(String)} <br>
 * {@link #addWarningStatus(String, Throwable)} <br>
 * {@link #addErrorStatus(String, Throwable)} <br>
 * {@link #addFatalStatus(String, Throwable)}
 * <p>
 * Often also the following methods are called from within {@link #run()} <br>
 * {@link #startTiming()} <br>
 * {@link #stopTiming()}
 * <p>
 * In tests which contain multiple stati also the following methods are normally called to have different test titles
 * per status <br>
 * {@link #setSubTitle(String)} </pre>
 */
public abstract class AbstractTest extends AbstractService implements ITest {
  private ITestContext m_ctx;
  private String m_product;
  private String m_title;
  private String m_subTitle;
  private long m_startTime = -1;
  private long m_endTime = -1;

  public AbstractTest() {
    initConfig();
  }

  @Order(10)
  @ConfigProperty(ConfigProperty.STRING)
  @ConfigPropertyValue("null")
  protected String getConfiguredProduct() {
    return null;
  }

  @Order(10)
  @ConfigProperty(ConfigProperty.STRING)
  @ConfigPropertyValue("null")
  protected String getConfiguredTitle() {
    return null;
  }

  @Order(10)
  @ConfigProperty(ConfigProperty.STRING)
  @ConfigPropertyValue("null")
  protected String getConfiguredSubTitle() {
    return null;
  }

  protected void initConfig() {
    setProduct(getConfiguredProduct());
    setTitle(getConfiguredTitle());
    setSubTitle(getConfiguredSubTitle());
    if (getTitle() == null || getTitle().length() == 0) {
      String s = getClass().getSimpleName();
      s = s.replaceAll("UnitTest$", "");
      s = s.replaceAll("Test$", "");
      setTitle(s);
    }
  }

  public void setTestContext(ITestContext ctx) {
    m_ctx = ctx;
  }

  public ITestContext getTestContext() {
    return m_ctx;
  }

  protected final void startTiming() {
    m_startTime = System.currentTimeMillis();
  }

  /**
   * stop the current timer and return the delta
   * 
   * @return {@link #getLastTiming()}
   */
  protected final long stopTiming() {
    m_endTime = System.currentTimeMillis();
    return getLastTiming();
  }

  protected final long getLastTiming() {
    if (m_startTime > 0 && m_endTime >= m_startTime) {
      return m_endTime - m_startTime;
    }
    else {
      return -1;
    }
  }

  /**
   * @return a new instance of a test status with product, domain, title and
   *         duration set based on this test's state and with severity {@value IProcessingStatus#INFO}
   */
  protected TestStatus createStatus() {
    TestStatus s = new TestStatus(getProduct(), getTitle(), getSubTitle());
    return s;
  }

  protected final void addOkStatus() {
    addOkStatus(null);
  }

  /**
   * Add a new instance of a test status with product, domain, title and
   * duration set based on this test's state and with severity {@value IProcessingStatus#INFO}
   * 
   * @param message
   *          may be null
   */
  protected final void addOkStatus(String message) {
    TestStatus s = createStatus();
    s.setSeverity(TestStatus.INFO);
    s.setDuration(getLastTiming());
    if (message != null) s.setMessage(message);
    addStatus(s);
  }

  protected final void addWarningStatus(String message) {
    addWarningStatus(message, null);
  }

  protected final void addWarningStatus(Throwable t) {
    addWarningStatus(null, t);
  }

  /**
   * Add a new instance of a test status with product, domain, title and
   * duration set based on this test's state and with severity {@value IProcessingStatus#WARNING}
   * 
   * @param message
   *          may be null
   * @param t
   *          may be null
   */
  protected final void addWarningStatus(String message, Throwable t) {
    TestStatus s = createStatus();
    s.setSeverity(TestStatus.WARNING);
    s.setDuration(getLastTiming());
    if (message != null) s.setMessage(message);
    if (t != null) s.setException(t);
    addStatus(s);
  }

  protected final void addErrorStatus(String message) {
    addErrorStatus(message, null);
  }

  protected final void addErrorStatus(Throwable t) {
    addErrorStatus(null, t);
  }

  /**
   * Add a new instance of a test status with product, domain, title and
   * duration set based on this test's state and with severity {@value IProcessingStatus#ERROR}
   * 
   * @param message
   *          may be null
   * @param t
   *          may be null
   */
  protected final void addErrorStatus(String message, Throwable t) {
    TestStatus s = createStatus();
    s.setSeverity(TestStatus.ERROR);
    s.setDuration(getLastTiming());
    if (message != null) s.setMessage(message);
    if (t != null) s.setException(t);
    addStatus(s);
  }

  /**
   * Add a new instance of a test status with product, domain, title and
   * duration set based on this test's state and with severity {@value IProcessingStatus#FATAL}
   * 
   * @param message
   *          may be null
   * @param t
   *          may be null
   */
  protected final void addFatalStatus(String message, Throwable t) {
    TestStatus s = createStatus();
    s.setSeverity(TestStatus.FATAL);
    s.setDuration(getLastTiming());
    if (message != null) s.setMessage(message);
    if (t != null) s.setException(t);
    addStatus(s);
  }

  protected final void addStatus(TestStatus status) {
    m_ctx.addStatus(status);
  }

  public final String getProduct() {
    return m_product;
  }

  public final void setProduct(String s) {
    m_product = s;
  }

  public final String getTitle() {
    return m_title;
  }

  public final void setTitle(String s) {
    m_title = s;
  }

  public final String getSubTitle() {
    return m_subTitle;
  }

  public final void setSubTitle(String s) {
    m_subTitle = s;
  }

  public void setUp() throws Throwable {
  }

  public abstract void run() throws Throwable;

  public void tearDown() throws Throwable {
  }

}
