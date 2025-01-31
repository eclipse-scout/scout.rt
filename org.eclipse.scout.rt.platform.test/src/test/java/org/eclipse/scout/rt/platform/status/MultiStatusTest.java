/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.status;

import static org.eclipse.scout.rt.platform.util.StringUtility.startsWith;
import static org.junit.Assert.*;

import java.util.List;
import java.util.function.Predicate;

import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link MultiStatus}
 */
public class MultiStatusTest {

  private IStatus m_infoStatus;
  private IStatus m_warningStatus;

  @Before
  public void setup() {
    m_infoStatus = new Status(IStatus.INFO);
    m_warningStatus = new Status(IStatus.WARNING);
  }

  @Test
  public void testMultiStatusSeverity() {
    MultiStatus multiStatus = new MultiStatus();
    multiStatus.add(m_warningStatus);
    multiStatus.add(m_infoStatus);
    assertTrue(multiStatus.getSeverity() == IStatus.WARNING);
    assertTrue(multiStatus.isMultiStatus());
  }

  @Test
  public void testMultiStatusOk() {
    MultiStatus s = new MultiStatus();
    s.add(Status.OK_STATUS);
    s.add(Status.OK_STATUS);
    assertTrue(s.isOK());
  }

  @Test
  public void testMultiStatusNok() {
    MultiStatus s = new MultiStatus();
    s.add(Status.OK_STATUS);
    s.add(new Status("error", IStatus.ERROR));
    assertFalse(s.isOK());
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
    assertEquals("bbb", root.getMessage());
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
    final IStatus multiStatus = new MultiStatus();
    final IStatus status = new Status(message);
    assertEquals("", multiStatus.getMessage());
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
    assertEquals("AAA\nBBB", multiStatus.getMessage());
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
  public void testMultistatusTextNotEquals() {
    MultiStatus multiStatus = new MultiStatus();
    multiStatus.add(m_warningStatus);
    multiStatus.add(m_infoStatus);

    MultiStatus multiStatus2 = new MultiStatus();
    multiStatus2.add(m_infoStatus);
    multiStatus2.add(new Status("new Warning", IStatus.WARNING));
    assertNotEquals(multiStatus, multiStatus2);
  }

  @Test
  public void testContainsStatus() {
    MultiStatus multiStatus = new MultiStatus();
    multiStatus.add(new TestStatus());
    assertTrue(multiStatus.containsStatus(TestStatus.class));
  }

  @Test
  public void testContainsStatusNoChildren() {
    MultiStatus multiStatus = new MultiStatus();
    assertFalse(multiStatus.containsStatus(TestStatus.class));
  }

  @Test
  public void testContainsStatusHierarchy() {
    MultiStatus multiStatus = new MultiStatus();
    MultiStatus sub = new MultiStatus();
    sub.add(new TestStatus());
    multiStatus.add(sub);
    assertTrue(multiStatus.containsStatus(TestStatus.class));
  }

  @Test
  public void testContainsStatusHierarchyInvalid() {
    MultiStatus multiStatus = new MultiStatus();
    MultiStatus sub = new MultiStatus();
    sub.add(new TestStatus2());
    multiStatus.add(sub);
    assertFalse(multiStatus.containsStatus(TestStatus.class));
  }

  @Test
  public void testFindChildStatuses() {
    MultiStatus rootMs = new MultiStatus();
    rootMs.add(new Status("Level 1 - OK", IStatus.OK));
    rootMs.add(new Status("Level 1 - ERROR", IStatus.ERROR));
    MultiStatus level1Ms = new MultiStatus();
    rootMs.add(level1Ms);
    level1Ms.add(new Status("Level 2 - INFO", IStatus.INFO));
    level1Ms.add(new Status("Level 2 - WARNING", IStatus.WARNING));
    MultiStatus level2Ms = new MultiStatus();
    level1Ms.add(level2Ms);
    level2Ms.add(new Status("Level 3 - OK", IStatus.OK));
    level2Ms.add(new Status("Level 3 - ERROR", IStatus.ERROR));
    level2Ms.add(new Status("Level 3 - WARNING", IStatus.WARNING));

    Predicate<IStatus> okPredicate = s -> s.getSeverity() == IStatus.OK;
    Predicate<IStatus> infoPredicate = s -> s.getSeverity() == IStatus.INFO;
    Predicate<IStatus> warningPredicate = s -> s.getSeverity() == IStatus.WARNING;
    Predicate<IStatus> errorPredicate = s -> s.getSeverity() == IStatus.ERROR;

    Predicate<IStatus> level1Predicate = s -> startsWith(s.getMessage(), "Level 1");
    Predicate<IStatus> level2Predicate = s -> startsWith(s.getMessage(), "Level 2");
    Predicate<IStatus> level3Predicate = s -> startsWith(s.getMessage(), "Level 3");

    // root of search is not considered a child

    // rootMs
    assertEquals(2, rootMs.findChildStatuses(okPredicate).size());
    assertEquals(1, rootMs.findChildStatuses(infoPredicate).size());
    assertEquals(3, rootMs.findChildStatuses(warningPredicate).size()); // level1Ms has severity WARNING
    assertEquals(3, rootMs.findChildStatuses(errorPredicate).size()); // level2Ms has severity ERROR

    assertEquals(2, rootMs.findChildStatuses(level1Predicate).size());
    assertEquals(3, rootMs.findChildStatuses(level2Predicate).size()); // level1Ms has message Level 2 - WARNING
    assertEquals(4, rootMs.findChildStatuses(level3Predicate).size()); // level2Ms has message Level 3 - ERROR

    assertEquals(2, rootMs.findChildStatuses(level3Predicate.and(errorPredicate)).size());
    assertEquals(5, rootMs.findChildStatuses(level2Predicate.or(okPredicate)).size());

    // level1Ms
    assertEquals(1, level1Ms.findChildStatuses(okPredicate).size());
    assertEquals(1, level1Ms.findChildStatuses(infoPredicate).size());
    assertEquals(2, level1Ms.findChildStatuses(warningPredicate).size()); // level1Ms is no longer a child
    assertEquals(2, level1Ms.findChildStatuses(errorPredicate).size()); // level2Ms has severity ERROR

    assertEquals(0, level1Ms.findChildStatuses(level1Predicate).size());
    assertEquals(2, level1Ms.findChildStatuses(level2Predicate).size()); // level1Ms is no longer a child
    assertEquals(4, level1Ms.findChildStatuses(level3Predicate).size()); // level2Ms has message Level 3 - ERROR

    assertEquals(2, level1Ms.findChildStatuses(level3Predicate.and(errorPredicate)).size());
    assertEquals(3, level1Ms.findChildStatuses(level2Predicate.or(okPredicate)).size());

    // level2Ms
    assertEquals(1, level2Ms.findChildStatuses(okPredicate).size());
    assertEquals(0, level2Ms.findChildStatuses(infoPredicate).size());
    assertEquals(1, level2Ms.findChildStatuses(warningPredicate).size()); // level1Ms is no longer a child
    assertEquals(1, level2Ms.findChildStatuses(errorPredicate).size()); // level2Ms is no longer a child

    assertEquals(0, level2Ms.findChildStatuses(level1Predicate).size());
    assertEquals(0, level2Ms.findChildStatuses(level2Predicate).size()); // level1Ms is no longer a child
    assertEquals(3, level2Ms.findChildStatuses(level3Predicate).size()); // level2Ms is no longer a child

    assertEquals(1, level2Ms.findChildStatuses(level3Predicate.and(errorPredicate)).size());
    assertEquals(1, level2Ms.findChildStatuses(level2Predicate.or(okPredicate)).size());
  }

  @Test(expected = AssertionException.class)
  public void testThrowsOnNullStatus() {
    MultiStatus multiStatus = new MultiStatus();
    multiStatus.containsStatus((Class<IStatus>) null);
  }

  @Test
  public void testMessage() {
    MultiStatus multiStatus = new MultiStatus();
    multiStatus.add(new TestStatus());
    multiStatus.add(new TestStatus2());
    assertEquals(TestStatus.class.getSimpleName(), multiStatus.getMessage());
  }

  @Test
  public void testCode() {
    MultiStatus multiStatus = new MultiStatus();
    multiStatus.add(new TestStatus());
    multiStatus.add(new TestStatus2());
    assertEquals("must return code from first child status", 6, multiStatus.getCode());
  }

  @Order(10)
  class TestStatus extends Status {
    private static final long serialVersionUID = 1L;

    public TestStatus() {
      super(TestStatus.class.getSimpleName());
      setCode(6);
    }
  }

  @Order(20)
  class TestStatus2 extends Status {
    private static final long serialVersionUID = 1L;

    public TestStatus2() {
      super(TestStatus2.class.getSimpleName());
      setCode(7);
    }
  }
}
