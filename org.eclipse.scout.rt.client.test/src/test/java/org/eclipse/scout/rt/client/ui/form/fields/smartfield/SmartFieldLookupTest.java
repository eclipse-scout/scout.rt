/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.smartfield;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.fixture.ITestLookupService;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.fixture.TestLookupCall;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.job.IBlockingCondition;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.TriState;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRowFetchedCallback;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.mock.BeanMock;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;

/**
 * Tests the lookup in {@link AbstractSmartField} <br>
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class SmartFieldLookupTest {

  @BeanMock
  private ITestLookupService m_mockService;

  private ISmartField<Long> m_field;

  private long testMasterValue;

  @Before
  public void setup() {
    testMasterValue = 10;
    m_field = new AbstractSmartField<>() {
    };
    List<ILookupRow<Long>> childRows = new ArrayList<>();
    childRows.add(new LookupRow<>(0L, "Alice"));

    List<ILookupRow<Long>> inactiveRows = new ArrayList<>();
    inactiveRows.add(new LookupRow<>(1L, "A"));
    inactiveRows.add(new LookupRow<>(2L, "B"));
    inactiveRows.add(null);

    Mockito.doReturn(childRows).when(m_mockService).getDataByRec(argThat(hasRec(1L)));
    Mockito.doReturn(inactiveRows).when(m_mockService).getDataByRec(argThat(isInactive()));
    Mockito.doReturn(inactiveRows).when(m_mockService).getDataByRec(argThat(hasMaxRowCount(2)));
    Mockito.doReturn(inactiveRows).when(m_mockService).getDataByRec(argThat(hasMaster(testMasterValue)));
  }

  /**
   * Tests {@link ISmartField#callSubTreeLookup(Object)} in a smartfield without a lookup call defined.
   */
  @Test
  public void testSubtreeLookup_NoLookupCall() {
    List<? extends ILookupRow<Long>> rows = m_field.callSubTreeLookup(null);
    assertEquals(0, rows.size());
  }

  @Test
  public void testSubtreeLookup_NullParent() {
    m_field.setLookupCall(new TestLookupCall());
    List<? extends ILookupRow<Long>> rows = m_field.callSubTreeLookup(null);
    assertEquals(0, rows.size());
  }

  @Test
  public void testSubtreeLookup() {
    m_field.setLookupCall(new TestLookupCall());
    List<? extends ILookupRow<Long>> rows = m_field.callSubTreeLookup(1L);
    assertRecResult(rows);
  }

  /**
   * Tests {@link ISmartField#callSubTreeLookup(Object)} using an active filter with {@link TriState#FALSE}
   * <code>null</code> values should be filtered out
   */
  @Test
  public void testSubtreeLookup_ActiveFilter() {
    m_field.setLookupCall(new TestLookupCall());
    List<? extends ILookupRow<Long>> rows = m_field.callSubTreeLookup(1L, TriState.FALSE);
    assertEquals(2, rows.size());
  }

  @Test
  public void testSubtreeLookup_ActiveFilterInPrepare() {
    m_field.setLookupCall(new TestLookupCall());
    m_field.setActiveFilterEnabled(true);
    m_field.setActiveFilter(TriState.FALSE);
    List<? extends ILookupRow<Long>> rows = m_field.callSubTreeLookup(1L);
    assertEquals(2, rows.size());
  }

  @Test
  public void testSubtreeLookup_BrowseLimit() {
    m_field.setLookupCall(new TestLookupCall());
    m_field.setBrowseMaxRowCount(2);
    List<? extends ILookupRow<Long>> rows = m_field.callSubTreeLookup(1L, TriState.FALSE);
    assertEquals(2, rows.size());
  }

  @Test
  public void testSubtreeLookup_WithMasterField() {
    IValueField<Long> masterField = new AbstractValueField<>() {
    };
    masterField.setValue(testMasterValue);
    m_field.setLookupCall(new TestLookupCall());
    m_field.setMasterField(masterField);
    List<? extends ILookupRow<Long>> rows2 = m_field.callSubTreeLookup(1L);
    assertEquals(2, rows2.size());
  }

  @Test
  public void testSubtreeLookup_InBackground() {
    IValueField<Long> masterField = new AbstractValueField<>() {
    };
    masterField.setValue(testMasterValue);
    m_field.setLookupCall(new TestLookupCall());
    m_field.setMasterField(masterField);

    IFuture<List<ILookupRow<Long>>> futureRows = m_field.callSubTreeLookupInBackground(1L, TriState.TRUE, false);
    List<? extends ILookupRow<Long>> rows = awaitDoneAndGet(futureRows);
    assertEquals(2, rows.size());
  }

  /**
   * await result while freeing model thread
   */
  private <T> T awaitDoneAndGet(IFuture<T> futureRows) {
    final IBlockingCondition bc = Jobs.newBlockingCondition(true);
    futureRows.whenDone(event -> bc.setBlocking(false), ClientRunContexts.copyCurrent());
    bc.waitFor();
    return futureRows.awaitDoneAndGet();
  }

  @Test
  public void testSubtreeLookupNoLookupCall_InBackground() {
    IFuture<List<ILookupRow<Long>>> futureRows = m_field.callSubTreeLookupInBackground(1L, TriState.TRUE, false);
    List<? extends ILookupRow<Long>> rows = awaitDoneAndGet(futureRows);
    assertEquals(0, rows.size());
  }

  @Test(expected = PlatformException.class)
  public void testRecLookupWithExceptions() {
    m_field.setLookupCall(new TestLookupCall());
    throwOnRecLookup();
    m_field.callSubTreeLookup(1L, TriState.TRUE);
  }

  @SuppressWarnings("unchecked")
  @Test(expected = PlatformException.class)
  public void testTextLookupWithExceptions() {
    m_field.setLookupCall(new TestLookupCall());
    when(m_mockService.getDataByText(any(ILookupCall.class))).thenThrow(new PlatformException("lookup error"));
    m_field.callTextLookup("test", 10);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testSubtreeLookupExceptions_InBackground() {
    final IBlockingCondition bc = Jobs.newBlockingCondition(true);
    m_field.setLookupCall(new TestLookupCall());
    when(m_mockService.getDataByRec(any(ILookupCall.class))).thenThrow(new PlatformException("lookup error"));

    IFuture<List<ILookupRow<Long>>> rows = m_field.callSubTreeLookupInBackground(1L, TriState.TRUE, false);

    rows.whenDone(event -> {
      assertTrue(event.getException() instanceof RuntimeException);
      assertTrue(event.getException().getMessage().startsWith("lookup error"));
      bc.setBlocking(false);
    }, ClientRunContexts.copyCurrent());
    bc.waitFor();
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testTextLookupExceptions_InBackground() {
    m_field.setLookupCall(new TestLookupCall());
    final String errorText = "lookup error";
    when(m_mockService.getDataByText(any(ILookupCall.class))).thenThrow(new PlatformException(errorText));
    final IBlockingCondition bc = Jobs.newBlockingCondition(true);
    ILookupRowFetchedCallback callback = new ILookupRowFetchedCallback<Long>() {

      @Override
      public void onSuccess(List<? extends ILookupRow<Long>> rows) {
        Assert.fail("no exception thrown");
        bc.setBlocking(false);
      }

      @Override
      public void onFailure(RuntimeException exception) {
        assertTrue(exception instanceof PlatformException);
        assertEquals(errorText, exception.getMessage());
        bc.setBlocking(false);
      }
    };

    m_field.callTextLookupInBackground("", 10, callback);
    bc.waitFor();
  }

  @Test
  public void testSubteeLookup_WithPrepare() {
    ISmartField<Long> field = new AbstractSmartField<>() {

      @Override
      protected void execPrepareLookup(ILookupCall<Long> call) {
        call.setMaster(testMasterValue);
      }
    };
    field.setLookupCall(new TestLookupCall());
    List<? extends ILookupRow<Long>> rows2 = field.callSubTreeLookup(1L);
    assertEquals(2, rows2.size());
  }

  @Test
  public void testSubteeLookup_WithPrepareRec() {
    ISmartField<Long> field = new AbstractSmartField<>() {

      @Override
      protected void execPrepareRecLookup(ILookupCall<Long> call, Long parentKey) {
        call.setMaster(testMasterValue);
      }
    };
    field.setLookupCall(new TestLookupCall());
    List<? extends ILookupRow<Long>> rows2 = field.callSubTreeLookup(1L);
    assertEquals(2, rows2.size());
  }

  @Test
  public void testSubtreeLookup_FilterResult() {
    ISmartField<Long> field = new AbstractSmartField<>() {

      @Override
      protected void execFilterRecLookupResult(ILookupCall<Long> call, List<ILookupRow<Long>> result) {
        result.clear();
      }
    };
    field.setLookupCall(new TestLookupCall());
    List<? extends ILookupRow<Long>> rows2 = field.callSubTreeLookup(1L);
    assertEquals(0, rows2.size());
  }

  @SuppressWarnings("unchecked")
  private void throwOnRecLookup() {
    when(m_mockService.getDataByRec(any(ILookupCall.class))).thenThrow(new PlatformException("lookup error"));
  }

  /**
   * Creates a {@link ArgumentMatcher} checking for equal {@link ILookupCall#getRec()} values
   */
  private static ArgumentMatcher<ILookupCall<Long>> hasRec(final Long rec) {
    return argument -> {
      if (argument == null) {
        return false;
      }
      Object parent = argument.getRec();
      return parent instanceof Long && rec.equals(parent);
    };
  }

  private void assertRecResult(List<? extends ILookupRow<Long>> rows) {
    assertEquals(1, rows.size());
  }

  /**
   * Creates a {@link ArgumentMatcher} checking for inactive values using {@link ILookupCall#getActive()}
   */
  private static ArgumentMatcher<ILookupCall<Long>> isInactive() {
    return argument -> {
      if (argument == null) {
        return false;
      }
      TriState active = argument.getActive();
      return active.isFalse();
    };
  }

  private static ArgumentMatcher<ILookupCall<Long>> hasMaxRowCount(final int maxRowCount) {
    return argument -> {
      if (argument == null) {
        return false;
      }
      return argument.getMaxRowCount() == maxRowCount;
    };
  }

  private static ArgumentMatcher<ILookupCall<Long>> hasMaster(final Long masterValue) {
    return argument -> {
      if (argument == null) {
        return false;
      }
      return masterValue.equals(argument.getMaster());
    };
  }
}
