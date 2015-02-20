/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons.status;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link MultiStatus}
 */
public class StatusTest {

  private IStatus m_infoStatus;
  private IStatus m_warningStatus;

  @Before
  public void setup() {
    m_infoStatus = new Status(IStatus.INFO);
    m_warningStatus = new Status(IStatus.WARNING);
  }

  @Test
  public void testMultistatusSeverity() {
    MultiStatus multiStatus = new MultiStatus();
    multiStatus.add(m_warningStatus);
    multiStatus.add(m_infoStatus);
    assertTrue(multiStatus.getSeverity() == IStatus.WARNING);
    assertTrue(multiStatus.isMultiStatus());
  }

  @Test
  public void testStatusHierarchy() {
    MultiStatus root = new MultiStatus();
    root.add(new Status("aaa", IStatus.INFO));
    MultiStatus multi = new MultiStatus();
    multi.add(new Status("aaa", IStatus.WARNING));
    MultiStatus multiError = new MultiStatus();
    multiError.add(new Status("bbb", IStatus.INFO));
    multiError.add(new Status("bbb"));
    multi.add(multiError);
    root.add(multi);

    final List<IStatus> children = root.getChildren();
    assertEquals(2, children.size());
    assertEquals(multi, children.get(0));
    assertEquals(IStatus.ERROR, root.getSeverity());
  }

  @Test
  public void testTextNotEquals() {
    assertNotEquals(m_warningStatus, new Status("new Warning", IStatus.WARNING));
  }

  /**
   * A status is only a multistatus, if it has children.
   */
  @Test
  public void testInitializeStatus() {
    final String message = "testMesage";
    final IStatus multiStatus = new MultiStatus(message);
    final IStatus status = new Status(message);
    assertEquals(message, multiStatus.getMessage());
    assertEquals(message, status.getMessage());
    assertEquals(IStatus.OK, multiStatus.getSeverity());
    assertEquals(IStatus.ERROR, status.getSeverity());
    assertTrue(multiStatus.isMultiStatus());
    assertFalse(status.isMultiStatus());
  }

  //multistatus

  @Test
  public void testChildOrder2() {
    MultiStatus multiStatus = new MultiStatus();
    final String first = "AAA";
    multiStatus.add(new Status(first));
    multiStatus.add(new Status("BBB"));
    assertEquals(first, multiStatus.getChildren().get(0).getMessage());
  }

  @Test
  public void testChildrenContains() {
    MultiStatus multiStatus = new MultiStatus();
    multiStatus.add(m_warningStatus);
    multiStatus.add(m_infoStatus);
    assertTrue(multiStatus.getChildren().contains(m_warningStatus));
    assertTrue(multiStatus.getChildren().contains(m_infoStatus));
    assertFalse(multiStatus.getChildren().contains(new Status("new Warning", IStatus.WARNING)));
  }

  @Test
  public void testEquals() {
    MultiStatus multiStatus = new MultiStatus();
    multiStatus.add(m_warningStatus);
    multiStatus.add(m_infoStatus);
    MultiStatus multiStatus2 = new MultiStatus();
    multiStatus2.add(m_infoStatus);
    multiStatus2.add(m_warningStatus);
    assertEquals(multiStatus, multiStatus2);
    assertEquals(new MultiStatus(), new MultiStatus());
    assertNotEquals(multiStatus, m_warningStatus);
    assertNotEquals(multiStatus, new Status("aaa"));
    assertNotEquals(multiStatus, new MultiStatus());
  }

  @Test
  public void testMultistatusTextNotEquals() throws Exception {
    MultiStatus multiStatus = new MultiStatus();
    multiStatus.add(m_warningStatus);
    multiStatus.add(m_infoStatus);

    MultiStatus multiStatus2 = new MultiStatus();
    multiStatus2.add(m_infoStatus);
    multiStatus2.add(new Status("new Warning", IStatus.WARNING));
    assertNotEquals(multiStatus, multiStatus2);
  }

}
