/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop.hybrid;

import static java.util.Collections.*;
import static java.util.stream.Collectors.toList;
import static org.eclipse.scout.rt.client.job.ModelJobs.assertModelThread;
import static org.eclipse.scout.rt.platform.util.Assertions.*;
import static org.eclipse.scout.rt.platform.util.CollectionUtility.isEmpty;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.exception.PlatformError;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.reflect.AbstractPropertyObserver;
import org.eclipse.scout.rt.platform.util.ImmutablePair;
import org.eclipse.scout.rt.platform.util.Pair;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.event.EventSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Bean
public class BrowserCallbacks extends AbstractPropertyObserver {

  private static final Logger LOG = LoggerFactory.getLogger(BrowserCallbacks.class);

  protected final Map<String, Collection<P_BrowserCallback<?>>> m_pendingBrowserCallbacks;
  protected final EventSupport<BrowserCallbackEvent> m_eventSupport;
  protected final IBrowserCallbacksUIFacade m_uiFacade;

  protected BrowserCallbacks() {
    m_pendingBrowserCallbacks = new HashMap<>();
    m_eventSupport = new EventSupport<>();
    m_uiFacade = createUIFacade();
    IDesktop.CURRENT.get().addPropertyChangeListener(IDesktop.PROP_READY, e -> this.onDesktopReadyChanged((Boolean) e.getOldValue(), (Boolean) e.getNewValue()));
  }

  public static BrowserCallbacks get() {
    IDesktop desktop = IDesktop.CURRENT.get();
    if (desktop == null) {
      return null;
    }
    // instance is created using BrowserCallbacksDesktopExtension during init of Desktop
    return desktop.getAddOn(BrowserCallbacks.class);
  }

  public <R extends IDoEntity> BrowserCallback<R> send(IWidget owner, String jsHandlerObjectType) {
    return send(owner, jsHandlerObjectType, null);
  }

  public <R extends IDoEntity> BrowserCallback<R> send(IWidget owner, String jsHandlerObjectType, IDoEntity dataToSendToBrowser) {
    return send(owner, jsHandlerObjectType, dataToSendToBrowser, null);
  }

  public <R extends IDoEntity> BrowserCallback<R> send(IWidget owner, String jsHandlerObjectType, IDoEntity dataToSendToBrowser, String callbackId) {
    return send(owner, new NoopBrowserCallback<>(jsHandlerObjectType), dataToSendToBrowser, callbackId);
  }

  public <R> BrowserCallback<R> send(IWidget owner, IBrowserCallback<?, R> handler) {
    return send(owner, handler, null);
  }

  public <R> BrowserCallback<R> send(IWidget owner, IBrowserCallback<?, R> handler, IDoEntity dataToSendToBrowser) {
    return send(owner, handler, null, null);
  }

  public <R> BrowserCallback<R> send(IWidget owner, IBrowserCallback<?, R> handler, IDoEntity dataToSendToBrowser, String callbackId) {
    assertNotNull(handler);
    assertNotNull(owner);
    assertModelThread();
    assertTrue(IDesktop.CURRENT.get().isReady(), "Can only request a BrowserCallback if the UI Desktop is ready.");

    if (StringUtility.isNullOrEmpty(callbackId)) {
      callbackId = UUID.randomUUID().toString();
    }

    boolean isNewId;
    P_BrowserCallback<R> callback;
    synchronized (m_pendingBrowserCallbacks) {
      isNewId = !m_pendingBrowserCallbacks.containsKey(callbackId);
      callback = registerCallback(callbackId, handler);
    }

    // automatically cancel the callback on widget disposal
    PropertyChangeListener listener = new P_OwnerDisposeListener(owner, callback); // listener is removed automatically from widget
    owner.addPropertyChangeListener(IWidget.PROP_DISPOSE_DONE, listener);

    if (isNewId) {
      // send only one event to the browser for each ID as one response will resolve all with the same ID.
      fireBrowserCallbackEvent(callback, dataToSendToBrowser, owner);
    }
    return callback;
  }

  protected <T> P_BrowserCallback<T> registerCallback(String callbackId, IBrowserCallback<?, T> handler) {
    P_BrowserCallback<T> uiCallback = new P_BrowserCallback<>(callbackId, handler);
    m_pendingBrowserCallbacks.computeIfAbsent(callbackId, id -> new ArrayList<>()).add(uiCallback);
    return uiCallback;
  }

  protected List<P_BrowserCallback<?>> getCallbacksInternal(Predicate<P_BrowserCallback<?>> filter) {
    synchronized (m_pendingBrowserCallbacks) {
      Stream<P_BrowserCallback<?>> callbackStream = m_pendingBrowserCallbacks.values().stream().flatMap(Collection::stream);
      if (filter != null) {
        callbackStream = callbackStream.filter(filter);
      }
      return callbackStream.collect(toList());
    }
  }

  protected List<P_BrowserCallback<?>> getCallbacksInternal(String callbackId) {
    synchronized (m_pendingBrowserCallbacks) {
      Collection<P_BrowserCallback<?>> callbacks = m_pendingBrowserCallbacks.get(callbackId);
      if (isEmpty(callbacks)) {
        return emptyList();
      }
      return new ArrayList<>(callbacks);
    }
  }

  public List<BrowserCallback<?>> getCallbacks(String callbackId) {
    return unmodifiableList(getCallbacksInternal(callbackId));
  }

  protected void removeCallback(P_BrowserCallback<?> callback) {
    synchronized (m_pendingBrowserCallbacks) {
      Collection<P_BrowserCallback<?>> callbacks = m_pendingBrowserCallbacks.get(callback.m_id);
      if (callbacks == null) {
        return;
      }
      callbacks.removeIf(o -> o.equals(callback));
      if (callbacks.isEmpty()) {
        m_pendingBrowserCallbacks.remove(callback.m_id);
      }
    }
  }

  public EventSupport<BrowserCallbackEvent> getEventSupport() {
    return m_eventSupport;
  }

  protected <R> void fireBrowserCallbackEvent(P_BrowserCallback<R> callback, IDoEntity dataToSendToBrowser, IWidget owner) {
    m_eventSupport.fireEvent(new BrowserCallbackEvent(this, callback.m_id, callback.m_handler.browserCallbackHandlerObjectType(), dataToSendToBrowser, owner));
  }

  protected void fireCallbackDone(String callbackId, IDoEntity response) {
    getCallbacksInternal(callbackId).forEach(c -> fireCallbackDone(c, response));
  }

  protected void onDesktopReadyChanged(boolean wasReady, boolean isNowReady) {
    // called as soon as the UI Desktop is ready or the UI Desktop is detached (e.g. on browser refresh or desktop close).
    if (wasReady && !isNowReady) {
      cancelAllBrowserCallbacks(true);
    }
  }

  protected void fireCallbackFailed(String callbackId, String message, String code) {
    ProcessingException t = new ProcessingException(StringUtility.hasText(message) ? message : "Error in BrowserCallback handler.");
    if (StringUtility.hasText(code)) {
      t.withContextInfo("code", code);
    }
    getCallbacksInternal(callbackId).forEach(c -> fireCallbackFailed(c, t, message, code));
  }

  protected <RES> void fireCallbackFailed(P_BrowserCallback<RES> callback, Throwable exception, String message, String code) {
    IBrowserCallback<?, RES> handler = callback.m_handler;
    Pair<RES, ? extends Throwable> result = handler.onCallbackFailed(exception, message, code);
    finishCallback(callback, result.getLeft(), result.getRight());
    callback.failed(exception);
  }

  protected <IN extends IDoEntity, RES> void fireCallbackDone(P_BrowserCallback<RES> callback, IN response) {
    //noinspection unchecked
    IBrowserCallback<IN, RES> handler = (IBrowserCallback<IN, RES>) callback.m_handler;
    Pair<RES, ? extends Throwable> result = handler.onCallbackDone(response);
    finishCallback(callback, result.getLeft(), result.getRight());
  }

  protected <IN extends IDoEntity, RES> void finishCallback(BrowserCallback<RES> callback, RES result, Throwable exception) {
    if (exception != null) {
      callback.failed(exception);
    }
    else {
      callback.done(result);
    }
  }

  public void cancelBrowserCallbacks(String callbackId, boolean mayInterruptIfRunning) {
    getCallbacks(callbackId).forEach(c -> c.cancel(mayInterruptIfRunning));
  }

  public void cancelAllBrowserCallbacks(boolean mayInterruptIfRunning) {
    getCallbacksInternal((Predicate<P_BrowserCallback<?>>) null).forEach(c -> cancelCallback(c, mayInterruptIfRunning));
  }

  protected void cancelCallback(BrowserCallback<?> c, boolean mayInterruptIfRunning) {
    try {
      c.cancel(mayInterruptIfRunning);
    }
    catch (RuntimeException | PlatformError e) {
      LOG.error("Exception while closing browser callback.", e);
    }
  }

  protected IBrowserCallbacksUIFacade createUIFacade() {
    return BEANS.get(ModelContextProxy.class).newProxy(new P_UIFacade(), ModelContext.copyCurrent());
  }

  public IBrowserCallbacksUIFacade getUIFacade() {
    return m_uiFacade;
  }

  protected class P_UIFacade implements IBrowserCallbacksUIFacade {
    @Override
    public void fireCallbackDone(String callbackId, IDoEntity response) {
      BrowserCallbacks.this.fireCallbackDone(callbackId, response);
    }

    @Override
    public void fireCallbackFailed(String callbackId, String message, String code) {
      BrowserCallbacks.this.fireCallbackFailed(callbackId, message, code);
    }
  }

  protected class P_BrowserCallback<T> extends BrowserCallback<T> {

    private final String m_id;
    private final IBrowserCallback<?, T> m_handler;

    public P_BrowserCallback(String id, IBrowserCallback<?, T> handler) {
      m_id = id;
      m_handler = handler;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
      removeCallback(this);
      return super.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean done(T result) {
      removeCallback(this);
      return super.done(result);
    }

    @Override
    public boolean failed(Throwable t) {
      removeCallback(this);
      return super.failed(t);
    }

    @Override
    protected void timedOut() {
      removeCallback(this);
    }
  }

  public static class NoopBrowserCallback<T extends IDoEntity> implements IBrowserCallback<T, T> {

    private final String m_browserCallbackHandlerObjectType;

    public NoopBrowserCallback(String browserCallbackHandlerObjectType) {
      m_browserCallbackHandlerObjectType = browserCallbackHandlerObjectType;
    }

    @Override
    public Pair<T, ? extends Throwable> onCallbackDone(T t) {
      return ImmutablePair.of(t, null);
    }

    @Override
    public String browserCallbackHandlerObjectType() {
      return m_browserCallbackHandlerObjectType;
    }
  }

  protected static class P_OwnerDisposeListener implements PropertyChangeListener {
    private final IWidget m_owner;
    private final BrowserCallback<?> m_callback;

    public P_OwnerDisposeListener(IWidget owner, BrowserCallback<?> callback) {
      m_owner = owner;
      m_callback = callback;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      boolean isDisposed = (Boolean) evt.getNewValue();
      boolean wasDisposed = (Boolean) evt.getOldValue();
      if (isDisposed && !wasDisposed) {
        // if the owner is disposed: cancel waiting callbacks
        m_callback.cancel(true); // will remove the callback in BrowserCallbacks
        m_owner.removePropertyChangeListener(this);
      }
    }
  }
}
