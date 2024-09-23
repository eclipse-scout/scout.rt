/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop.hybrid.uicallback;

import static org.junit.Assert.*;

import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.eclipse.scout.rt.api.data.ObjectType;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.testenvironment.ui.desktop.TestEnvironmentDesktop;
import org.eclipse.scout.rt.client.ui.Coordinates;
import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.desktop.GeoLocationResponse;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.hybrid.uicallback.UiCallbacks.P_UiCallback;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.reflect.IPropertyObserver;
import org.eclipse.scout.rt.platform.util.ImmutablePair;
import org.eclipse.scout.rt.platform.util.Pair;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class UiCallbacksTest {

  @BeforeClass
  public static void setup() {
    TestEnvironmentDesktop desktop = (TestEnvironmentDesktop) IDesktop.CURRENT.get();
    desktop.getUIFacade().readyFromUI();
  }

  @Test
  public void testMultipleRequestsWithSameIdAreResolvedAtOnce() throws ExecutionException, InterruptedException {
    String callbackId = "callbackId1";
    IDesktop desktop = IDesktop.CURRENT.get();
    UiCallbacks uiCallbacks = UiCallbacks.get();
    int numListeners = getNumDisposeListeners(desktop);
    int numCallbacks = getNumCallbacks(callbackId);
    Future<GeoLocationResponse> future1 = uiCallbacks.send(desktop, "whatever", null, callbackId);
    Future<GeoLocationResponse> future2 = uiCallbacks.send(desktop, "whatever", null, callbackId);
    assertEquals(numListeners + 2, getNumDisposeListeners(desktop));
    assertEquals(numCallbacks + 2, getNumCallbacks(callbackId));

    GeoLocationResponse result = BEANS.get(GeoLocationResponse.class).withLatitude("100").withLongitude("100");
    uiCallbacks.getUIFacade().fireCallbackDone(callbackId, result);

    assertEquals(numListeners, getNumDisposeListeners(desktop));
    assertEquals(numCallbacks, getNumCallbacks(callbackId));
    assertSame(result, future1.get());
    assertSame(result, future2.get());
  }

  @Test(expected = ProcessingException.class)
  public void testHandlerCanModifyResultToError() throws Throwable {
    UiCallbacks uiCallbacks = UiCallbacks.get();
    String callbackId = "callbackId2";
    Future<IDoEntity> future1 = uiCallbacks.send(IDesktop.CURRENT.get(), new P_TestingUiCallbackHandlerThrowingException(), null, callbackId);
    uiCallbacks.getUIFacade().fireCallbackDone(callbackId, null);
    try {
      future1.get(); // actual exception is wrapped into an ExecutionException
    }
    catch (ExecutionException e) {
      throw e.getCause(); // must throw ProcessingException even fireCallbackDone was called
    }
  }

  @Test
  public void testHandlerCanModifyResult() throws ExecutionException, InterruptedException {
    UiCallbacks uiCallbacks = UiCallbacks.get();
    String callbackId = "callbackId3";
    GeoLocationResponse result = BEANS.get(GeoLocationResponse.class).withLatitude("100").withLongitude("100");
    Future<String> future1 = uiCallbacks.send(IDesktop.CURRENT.get(), new IUiCallbackHandler<>() {
      @Override
      public String uiCallbackHandlerObjectType() {
        return "whatever";
      }

      @Override
      public Pair<String, ? extends Throwable> onCallbackDone(IDoEntity doEntity) {
        assertSame(result, doEntity);
        return ImmutablePair.of("300", null);
      }
    }, null, callbackId);
    uiCallbacks.getUIFacade().fireCallbackDone(callbackId, result);
    assertEquals("300", future1.get());
  }

  @Test
  public void testHandlerCanModifyErrorToResult() throws ExecutionException, InterruptedException {
    UiCallbacks uiCallbacks = UiCallbacks.get();
    String callbackId = "callbackId4";
    Future<GeoLocationResponse> future1 = uiCallbacks.send(IDesktop.CURRENT.get(), new IUiCallbackHandler<>() {
      @Override
      public Pair<GeoLocationResponse, ? extends Throwable> onCallbackDone(IDoEntity doEntity) {
        throw new AssertionError(); // should not be called as fireCallbackFailed is invoked
      }

      @Override
      public String uiCallbackHandlerObjectType() {
        return "whatever";
      }

      @Override
      public Pair<GeoLocationResponse, ? extends Throwable> onCallbackFailed(ProcessingException exception, String message, String code) {
        assertEquals("err", message);
        assertEquals("1234", code);
        return ImmutablePair.of(null, null); // change error to null result
      }
    }, null, callbackId);
    uiCallbacks.getUIFacade().fireCallbackFailed(callbackId, "err", "1234");
    assertNull(future1.get());
  }

  @Test
  public void testWidgetDisposeRemovesCallbacks() {
    UiCallbacks uiCallbacks = UiCallbacks.get();
    IWidget w = new AbstractFormField() {
    };
    w.init();
    int numListeners = getNumDisposeListeners(w);
    int numCallbacks = getNumCallbacks();
    Future<IDoEntity> callback = uiCallbacks.send(w, "whatever");
    assertEquals(numListeners + 1, getNumDisposeListeners(w));
    assertEquals(numCallbacks + 1, getNumCallbacks());

    w.dispose();
    assertEquals(numCallbacks, getNumCallbacks()); // callback is removed if owner widget is disposed
    assertEquals(numListeners, getNumDisposeListeners(w)); // listener is removed
    assertTrue(callback.isCancelled());
  }

  @Test
  public void testCancelAllCallbacks() {
    String callbackId = "test-id";
    UiCallbacks uiCallbacks = UiCallbacks.get();
    IDesktop desktop = IDesktop.CURRENT.get();
    int numListeners = getNumDisposeListeners(desktop);
    int numCallbacks = getNumCallbacks();
    Future<IDoEntity> future1 = uiCallbacks.send(desktop, new P_TestingUiCallbackHandlerThrowingException());
    Future<IDoEntity> future2 = uiCallbacks.send(desktop, new P_TestingUiCallbackHandlerThrowingException());
    Future<IDoEntity> future3 = uiCallbacks.send(desktop, new P_TestingUiCallbackHandlerThrowingException(), null, callbackId);
    assertSame(future3, uiCallbacks.getCallbacksInternal(callbackId).get(0));
    assertTrue(uiCallbacks.getCallbacksInternal("does-not-exist").isEmpty());
    assertEquals(numCallbacks + 3, getNumCallbacks());
    assertEquals(numListeners + 3, getNumDisposeListeners(desktop));

    uiCallbacks.cancelAllUiCallbacks(true);
    assertTrue(future1.isCancelled());
    assertTrue(future2.isCancelled());
    assertTrue(future3.isCancelled());
    assertEquals(numCallbacks, getNumCallbacks());
    assertEquals(numListeners, getNumDisposeListeners(desktop));
  }

  @Test
  public void testEventBuffer() {
    IDesktop desktop = IDesktop.CURRENT.get();
    desktop.setProperty(IDesktop.PROP_READY, false);
    String geoLocationCallbackId = "requestGeolocation";
    int numCallbacks = getNumCallbacks(geoLocationCallbackId);
    int numListeners = getNumDisposeListeners(desktop);
    int bufferSize = UiCallbacks.get().m_eventBuffer.size();
    Future<Coordinates> future1 = desktop.requestGeolocation();
    Future<Coordinates> future2 = desktop.requestGeolocation();
    Future<Coordinates> future3 = desktop.requestGeolocation();
    assertEquals(numCallbacks + 3, getNumCallbacks(geoLocationCallbackId));
    assertEquals(numListeners + 3, getNumDisposeListeners(desktop));
    assertEquals(bufferSize + 1 /* one event pending for the three callbacks with same id */, UiCallbacks.get().m_eventBuffer.size());

    desktop.getUIFacade().readyFromUI();
    assertEquals(numCallbacks + 3, getNumCallbacks(geoLocationCallbackId));
    assertEquals(numListeners + 3, getNumDisposeListeners(desktop));
    assertEquals(bufferSize, UiCallbacks.get().m_eventBuffer.size()); // buffer has been sent and cleared

    GeoLocationResponse result = BEANS.get(GeoLocationResponse.class).withLatitude("100").withLongitude("100");
    UiCallbacks.get().getUIFacade().fireCallbackDone(geoLocationCallbackId, result);
    assertEquals(numCallbacks, getNumCallbacks(geoLocationCallbackId));
    assertEquals(numListeners, getNumDisposeListeners(desktop));
    assertEquals(bufferSize, UiCallbacks.get().m_eventBuffer.size());

    assertTrue(future1.isDone());
    assertTrue(future2.isDone());
    assertTrue(future3.isDone());
  }

  @Test
  public void testEventBufferWhenWidgetIsDisposedBeforeSending() {
    IDesktop desktop = IDesktop.CURRENT.get();
    desktop.setProperty(IDesktop.PROP_READY, false);
    IForm owner = new AbstractForm() {
    };
    int numCallbacks = getNumCallbacks();
    int numListeners = getNumDisposeListeners(owner);
    int bufferSize = UiCallbacks.get().m_eventBuffer.size();
    Future<IDoEntity> future1 = UiCallbacks.get().send(owner, "whatever", null, "id_0");
    Future<IDoEntity> future2 = UiCallbacks.get().send(owner, "whatever", null, "id_1");
    Future<IDoEntity> future3 = UiCallbacks.get().send(owner, "whatever", null, "id_2");
    assertEquals(numCallbacks + 3, getNumCallbacks());
    assertEquals(numListeners + 3, getNumDisposeListeners(owner));
    assertEquals(bufferSize + 3, UiCallbacks.get().m_eventBuffer.size());

    owner.dispose();
    assertEquals(numCallbacks, getNumCallbacks());
    assertEquals(numListeners, getNumDisposeListeners(owner));
    assertEquals(bufferSize, UiCallbacks.get().m_eventBuffer.size());
    assertTrue(future1.isCancelled());
    assertTrue(future2.isCancelled());
    assertTrue(future3.isCancelled());

    UiCallbacks.get().getUIFacade().fireCallbackDone("id_0", null);
    UiCallbacks.get().getUIFacade().fireCallbackDone("id_1", null);
    UiCallbacks.get().getUIFacade().fireCallbackDone("id_2", null);
    assertEquals(numCallbacks, getNumCallbacks());
    assertEquals(numListeners, getNumDisposeListeners(owner));
    assertEquals(bufferSize, UiCallbacks.get().m_eventBuffer.size());
    assertTrue(future1.isCancelled());
    assertTrue(future2.isCancelled());
    assertTrue(future3.isCancelled());
  }

  @Test
  public void testManualCancellationOfCallback() {
    String callbackId = "callbackId";
    IDesktop desktop = IDesktop.CURRENT.get();
    UiCallbacks uiCallbacks = UiCallbacks.get();
    int numListeners = getNumDisposeListeners(desktop);
    int numCallbacks = getNumCallbacks(callbackId);

    Future<GeoLocationResponse> future = uiCallbacks.send(desktop, "whatever", null, callbackId);
    assertEquals(numListeners + 1, getNumDisposeListeners(desktop));
    assertEquals(numCallbacks + 1, getNumCallbacks(callbackId));

    future.cancel(true); // manual cancellation
    assertEquals(numListeners, getNumDisposeListeners(desktop));
    assertEquals(numCallbacks, getNumCallbacks(callbackId));
  }

  protected static int getNumCallbacks() {
    return UiCallbacks.get().m_pendingUiCallbacks.size();
  }

  protected static int getNumCallbacks(String id) {
    Collection<P_UiCallback<?>> callbacks = UiCallbacks.get().m_pendingUiCallbacks.get(id);
    if (callbacks == null) {
      return 0;
    }
    return callbacks.size();
  }

  protected static int getNumDisposeListeners(IPropertyObserver o) {
    List<PropertyChangeListener> disposeListeners = o.getSpecificPropertyChangeListeners().get(IWidget.PROP_DISPOSE_DONE);
    if (disposeListeners == null) {
      return 0;
    }
    return disposeListeners.size();
  }

  @ObjectType("whatever")
  private static class P_TestingUiCallbackHandlerThrowingException implements IUiCallbackHandler<IDoEntity, IDoEntity> {
    @Override
    public Pair<IDoEntity, ? extends Throwable> onCallbackDone(IDoEntity doEntity) {
      return ImmutablePair.of(doEntity /* this value must be ignored as an exception is present */, new ProcessingException("test-error"));
    }
  }
}
