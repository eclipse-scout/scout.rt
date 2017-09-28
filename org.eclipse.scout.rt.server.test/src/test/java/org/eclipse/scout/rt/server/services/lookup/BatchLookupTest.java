/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.services.lookup;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.shared.services.lookup.BatchLookupCall;
import org.eclipse.scout.rt.shared.services.lookup.BatchLookupResultCache;
import org.eclipse.scout.rt.shared.services.lookup.IBatchLookupService;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.ILookupService;
import org.eclipse.scout.rt.shared.services.lookup.LocalLookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;
import org.eclipse.scout.rt.testing.platform.mock.BeanMock;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Test {@link IBatchLookupService} and caching with {@link BatchLookupResultCache}
 *
 * @since 4.3.0 (Mars-M5)
 */
@RunWith(PlatformTestRunner.class)
public class BatchLookupTest {
  @BeanMock
  private IFlowerLookupService m_lookupService;
  private static long m_localInvocations;

  @Before
  public void setUp() throws Exception {
    Answer answer = new Answer<List<ILookupRow<Object>>>() {

      @Override
      public List<ILookupRow<Object>> answer(InvocationOnMock invocation) throws Throwable {
        Object[] args = invocation.getArguments();
        ILookupCall<?> call = (ILookupCall<?>) args[0];
        return createCallResult(call);
      }

    };
    Mockito.doAnswer(answer).when(m_lookupService).getDataByKey(Mockito.<ILookupCall<Object>> any());
    Mockito.doAnswer(answer).when(m_lookupService).getDataByAll(Mockito.<ILookupCall<Object>> any());
    Mockito.doAnswer(answer).when(m_lookupService).getDataByText(Mockito.<ILookupCall<Object>> any());
    Mockito.doAnswer(answer).when(m_lookupService).getDataByRec(Mockito.<ILookupCall<Object>> any());
  }

  @Test
  public void testNoMembersNoEqualsOverride() throws Exception {
    testInternal(FlowerLookupCallNoMembersNoEqualsOverride.class, 0, 10);
  }

  @Test
  public void testNoMembersWithEqualsOverride() throws Exception {
    testInternal(FlowerLookupCallNoMembersWithEqualsOverride.class, 0, 10);
  }

  @Test
  public void testWithMembersNoEqualsOverride() throws Exception {
    testInternal(FlowerLookupCallWithMembersNoEqualsOverride.class, 0, 1000);
  }

  @Test
  public void testWithMembersWithEqualsOverride() throws Exception {
    testInternal(FlowerLookupCallWithMembersWithEqualsOverride.class, 0, 100);
  }

  @Test
  public void testSubclassWithMembersNoEqualsOverride() throws Exception {
    testInternal(SubclassedFlowerLookupCallWithMembersNoEqualsOverride.class, 0, 1000);
  }

  @Test
  public void testSubclassWithMembersWithEqualsOverride() throws Exception {
    testInternal(SubclassedFlowerLookupCallWithMembersWithEqualsOverride.class, 0, 100);
  }

  @Test
  public void testLocalNoMembersNoEqualsOverride() throws Exception {
    testInternal(LocalFlowerLookupCallNoMembersNoEqualsOverride.class, 10, 0);
  }

  @Test
  public void testLocalNoMembersWithEqualsOverride() throws Exception {
    testInternal(LocalFlowerLookupCallNoMembersWithEqualsOverride.class, 10, 0);
  }

  @Test
  public void testLocalWithMembersNoEqualsOverride() throws Exception {
    testInternal(LocalFlowerLookupCallWithMembersNoEqualsOverride.class, 1000, 0);
  }

  @Test
  public void testLocalWithMembersWithEqualsOverride() throws Exception {
    testInternal(LocalFlowerLookupCallWithMembersWithEqualsOverride.class, 100, 0);
  }

  @Test
  public void testSubclassLocalWithMembersNoEqualsOverride() throws Exception {
    testInternal(SubclassedLocalFlowerLookupCallWithMembersNoEqualsOverride.class, 1000, 0);
  }

  @Test
  public void testSubclassLocalWithMembersWithEqualsOverride() throws Exception {
    testInternal(SubclassedLocalFlowerLookupCallWithMembersWithEqualsOverride.class, 100, 0);
  }

  private void testInternal(Class<? extends IFlowerLookupCall> callClazz, int expectedLocalInvocations, int expectedServerInvocations) throws Exception {
    m_localInvocations = 0;
    BatchLookupCall batchCall = new BatchLookupCall();
    for (int i = 0; i < 1000; i++) {
      IFlowerLookupCall call = callClazz.newInstance();
      call.setKey((i / 100) + 1L);
      call.setLatinId((i / 10) + 1L);
      batchCall.addLookupCall((LookupCall) call);
    }
    //
    List<ILookupCall<?>> callArray = batchCall.getCallBatch();
    List<List<ILookupRow<?>>> resultArray = new BatchLookupService().getBatchDataByKey(batchCall);
    assertEquals(resultArray.size(), callArray.size());
    assertEquals(expectedLocalInvocations, m_localInvocations);
    Mockito.verify(m_lookupService, Mockito.times(expectedServerInvocations)).getDataByKey(Mockito.<ILookupCall<Object>> any());
    for (int i = 0; i < resultArray.size(); i++) {
      assertEquals(1, resultArray.get(i).size());
      assertEquals(callArray.get(i).getKey(), resultArray.get(i).get(0).getKey());
      assertEquals(dumpCall(callArray.get(i)), resultArray.get(i).get(0).getText());
    }
  }

  private static List<ILookupRow<Object>> createCallResult(ILookupCall<?> call) {
    List<ILookupRow<Object>> result = new ArrayList<ILookupRow<Object>>();
    result.add(new LookupRow<Object>(call.getKey(), dumpCall(call)));
    return result;
  }

  private static String dumpCall(ILookupCall<?> call) {
    IFlowerLookupCall f = (IFlowerLookupCall) call;
    return "Flower[key=" + call.getKey() + ", text=" + call.getText() + ", latin=" + f.getLatinId() + "]";
  }

  public static class FlowerLookupCallNoMembersNoEqualsOverride extends LookupCall implements IFlowerLookupCall {
    private static final long serialVersionUID = 1L;

    @Override
    protected final Class<? extends ILookupService> getConfiguredService() {
      return IFlowerLookupService.class;
    }

    @Override
    public void setLatinId(Long id) {
      //nop
    }

    @Override
    public Long getLatinId() {
      return null;
    }

    @Override
    public String toString() {
      return super.toString();
    }
  }

  public static class FlowerLookupCallNoMembersWithEqualsOverride extends LookupCall implements IFlowerLookupCall {
    private static final long serialVersionUID = 1L;

    @Override
    protected final Class<? extends ILookupService> getConfiguredService() {
      return IFlowerLookupService.class;
    }

    @Override
    public void setLatinId(Long id) {
      //nop
    }

    @Override
    public Long getLatinId() {
      return null;
    }

    @Override
    public boolean equals(Object obj) {
      return super.equals(obj);
    }
  }

  public static class FlowerLookupCallWithMembersNoEqualsOverride extends LookupCall implements IFlowerLookupCall {
    private static final long serialVersionUID = 1L;

    private Long m_latinId;

    @Override
    protected final Class<? extends ILookupService> getConfiguredService() {
      return IFlowerLookupService.class;
    }

    @Override
    public Long getLatinId() {
      return m_latinId;
    }

    @Override
    public void setLatinId(Long latinId) {
      m_latinId = latinId;
    }
  }

  public static class FlowerLookupCallWithMembersWithEqualsOverride extends LookupCall implements IFlowerLookupCall {
    private static final long serialVersionUID = 1L;

    private Long m_latinId;

    @Override
    protected final Class<? extends ILookupService> getConfiguredService() {
      return IFlowerLookupService.class;
    }

    @Override
    public Long getLatinId() {
      return m_latinId;
    }

    @Override
    public void setLatinId(Long latinId) {
      m_latinId = latinId;
    }

    @Override
    public boolean equals(Object obj) {
      if (!super.equals(obj)) {
        return false;
      }
      IFlowerLookupCall other = (IFlowerLookupCall) obj;
      return this.m_latinId == other.getLatinId() || (this.m_latinId != null && this.m_latinId.equals(other.getLatinId()));
    }
  }

  public static class SubclassedFlowerLookupCallWithMembersNoEqualsOverride extends FlowerLookupCallWithMembersNoEqualsOverride implements IFlowerLookupCall {
    private static final long serialVersionUID = 1L;
  }

  public static class SubclassedFlowerLookupCallWithMembersWithEqualsOverride extends FlowerLookupCallWithMembersWithEqualsOverride implements IFlowerLookupCall {
    private static final long serialVersionUID = 1L;
  }

  public static class LocalFlowerLookupCallNoMembersNoEqualsOverride extends LocalLookupCall<Object> implements IFlowerLookupCall {
    private static final long serialVersionUID = 1L;

    @Override
    protected List<ILookupRow<Object>> execCreateLookupRows() {
      m_localInvocations++;
      return createCallResult(this);
    }

    @Override
    public void setLatinId(Long id) {
      //nop
    }

    @Override
    public Long getLatinId() {
      return null;
    }

    @Override
    public String toString() {
      return super.toString();
    }
  }

  public static class LocalFlowerLookupCallNoMembersWithEqualsOverride extends LocalLookupCall<Object> implements IFlowerLookupCall {
    private static final long serialVersionUID = 1L;

    @Override
    protected List<ILookupRow<Object>> execCreateLookupRows() {
      m_localInvocations++;
      return createCallResult(this);
    }

    @Override
    public void setLatinId(Long id) {
      //nop
    }

    @Override
    public Long getLatinId() {
      return null;
    }

    @Override
    public boolean equals(Object obj) {
      return super.equals(obj);
    }
  }

  public static class LocalFlowerLookupCallWithMembersNoEqualsOverride extends LocalLookupCall implements IFlowerLookupCall {
    private static final long serialVersionUID = 1L;

    private Long m_latinId;

    @Override
    protected List<ILookupRow<Object>> execCreateLookupRows() {
      m_localInvocations++;
      return createCallResult(this);
    }

    @Override
    public Long getLatinId() {
      return m_latinId;
    }

    @Override
    public void setLatinId(Long latinId) {
      m_latinId = latinId;
    }
  }

  public static class LocalFlowerLookupCallWithMembersWithEqualsOverride extends LocalLookupCall<Object> implements IFlowerLookupCall {
    private static final long serialVersionUID = 1L;

    private Long m_latinId;

    @Override
    protected List<ILookupRow<Object>> execCreateLookupRows() {
      m_localInvocations++;
      return createCallResult(this);
    }

    @Override
    public Long getLatinId() {
      return m_latinId;
    }

    @Override
    public void setLatinId(Long latinId) {
      m_latinId = latinId;
    }

    @Override
    public boolean equals(Object obj) {
      if (!super.equals(obj)) {
        return false;
      }
      IFlowerLookupCall other = (IFlowerLookupCall) obj;
      return this.m_latinId == other.getLatinId() || (this.m_latinId != null && this.m_latinId.equals(other.getLatinId()));
    }
  }

  public static class SubclassedLocalFlowerLookupCallWithMembersNoEqualsOverride extends LocalFlowerLookupCallWithMembersNoEqualsOverride implements IFlowerLookupCall {
    private static final long serialVersionUID = 1L;
  }

  public static class SubclassedLocalFlowerLookupCallWithMembersWithEqualsOverride extends LocalFlowerLookupCallWithMembersWithEqualsOverride implements IFlowerLookupCall {
    private static final long serialVersionUID = 1L;
  }

  public interface IFlowerLookupCall {
    void setKey(Object key);

    void setLatinId(Long id);

    Long getLatinId();
  }

  public interface IFlowerLookupService extends ILookupService<Object> {
  }
}
