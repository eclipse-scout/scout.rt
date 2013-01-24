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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.commons.exception.IProcessingStatus;

/**
 * @deprecated Use Scout JUnit Testing Support: {@link org.eclipse.scout.testing.client.runner.ScoutClientTestRunner} or
 *             {@link org.eclipse.scout.rt.testing.server.runner.ScoutServerTestRunner} to run Unit tests.
 */
@Deprecated
@SuppressWarnings("deprecation")
public class BasicTestContext implements ITestContext {
  private ArrayList<TestStatus> m_list;
  private int[] m_severityCount;

  public BasicTestContext() {
  }

  @Override
  public void begin() {
    m_list = new ArrayList<TestStatus>();
    m_severityCount = new int[IProcessingStatus.FATAL + 1];
  }

  @Override
  public void end() {
  }

  @Override
  public void addStatus(TestStatus s) {
    m_list.add(s);
    m_severityCount[s.getSeverity()]++;
  }

  /**
   * @return the life list with all test stati
   */
  @Override
  public List<TestStatus> getStatusList() {
    return m_list;
  }

  /**
   * @return the severity count for the severity
   */
  public int getSeverityCount(int severity) {
    return m_severityCount[severity];
  }

}
