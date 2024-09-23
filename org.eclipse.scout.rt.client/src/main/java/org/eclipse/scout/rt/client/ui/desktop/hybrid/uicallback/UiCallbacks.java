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

import static java.util.Collections.*;
import static java.util.stream.Collectors.toList;
import static org.eclipse.scout.rt.client.job.ModelJobs.assertModelThread;
import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNull;
import static org.eclipse.scout.rt.platform.util.CollectionUtility.isEmpty;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.desktop.DesktopEvent;
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

/**
 * Desktop addon (lazy created) to send {@link IDoEntity} instances to custom UI handlers on the browser and
 * asynchronously retrieve their results.
 * <p>
 * Use {@link UiCallbacks#get()} to request a UI callback.
 */
@Bean
public class UiCallbacks extends AbstractPropertyObserver {

  private static final Logger LOG = LoggerFactory.getLogger(UiCallbacks.class);

  protected final Map<String, Collection<P_UiCallback<?>>> m_pendingUiCallbacks;
  protected final EventSupport<UiCallbackEvent> m_eventSupport;
  protected final IUiCallbacksUIFacade m_uiFacade;
  protected final Map<String, UiCallbackEvent> m_eventBuffer;

  protected UiCallbacks() {
    m_pendingUiCallbacks = new HashMap<>();
    m_eventSupport = new EventSupport<>();
    m_uiFacade = createUIFacade();
    m_eventBuffer = new HashMap<>();
    IDesktop desktop = IDesktop.CURRENT.get();
    desktop.addPropertyChangeListener(IDesktop.PROP_READY, e -> this.onDesktopReadyChanged((Boolean) e.getOldValue(), (Boolean) e.getNewValue()));
    desktop.addDesktopListener(e -> this.onDesktopClosed(), DesktopEvent.TYPE_DESKTOP_CLOSED);
  }

  /**
   * @return The {@link UiCallbacks} instance for the current {@link IDesktop} or {@code null} if there is no current
   * {@link IDesktop}.
   */
  public static UiCallbacks get() {
    IDesktop desktop = IDesktop.CURRENT.get();
    if (desktop == null) {
      return null;
    }
    // instance is created using UiCallbacksDesktopExtension during init of Desktop
    return desktop.getAddOn(UiCallbacks.class);
  }

  /**
   * Calls the UI handler with given ObjectType.
   *
   * @param owner
   *          The {@link IWidget} this request belongs to. Pass the {@link IWidget} that specifies the lifetime of this
   *          UI callback. E.g. if the callback should be performed as long as the user is logged in, use
   *          {@code IDesktop.CURRENT.get()}. The callback will automatically be cancelled as soon as this
   *          {@link IWidget} is disposed.
   * @param jsHandlerObjectType
   *          The ObjectType of the handler in the browser to answer the callback.
   * @return The {@link IDoEntity} as returned by the UI handler on the browser.
   */
  public <R extends IDoEntity> Future<R> send(IWidget owner, String jsHandlerObjectType) {
    return send(owner, jsHandlerObjectType, null);
  }

  /**
   * Calls the UI handler with given ObjectType and input data.
   *
   * @param owner
   *          The {@link IWidget} this request belongs to. Pass the {@link IWidget} that specifies the lifetime of this
   *          UI callback. E.g. if the callback should be performed as long as the user is logged in, use
   *          {@code IDesktop.CURRENT.get()}. The callback will automatically be cancelled as soon as this
   *          {@link IWidget} is disposed.
   * @param jsHandlerObjectType
   *          The ObjectType of the handler in the browser to answer the callback.
   * @param dataToSendToUi
   *          An optional {@link IDoEntity} to send to the UI handler on the browser.
   * @return The {@link IDoEntity} as returned by the UI handler on the browser.
   */
  public <R extends IDoEntity> Future<R> send(IWidget owner, String jsHandlerObjectType, IDoEntity dataToSendToUi) {
    return send(owner, jsHandlerObjectType, dataToSendToUi, null);
  }

  /**
   * Calls the UI handler with given ObjectType and input data.
   *
   * @param owner
   *          The {@link IWidget} this request belongs to. Pass the {@link IWidget} that specifies the lifetime of this
   *          UI callback. E.g. if the callback should be performed as long as the user is logged in, use
   *          {@code IDesktop.CURRENT.get()}. The callback will automatically be cancelled as soon as this
   *          {@link IWidget} is disposed.
   * @param jsHandlerObjectType
   *          The ObjectType of the handler in the browser to answer the callback.
   * @param dataToSendToUi
   *          An optional {@link IDoEntity} to send to the UI handler on the browser.
   * @param callbackId
   *          An optional callbackId. All callbacks with the same ID are only sent once to the UI and on a response all
   *          pending callbacks with this ID are resolved at once. By default, each callback has its own unique ID.
   * @return The {@link IDoEntity} as returned by the UI handler on the browser.
   */
  public <R extends IDoEntity> Future<R> send(IWidget owner, String jsHandlerObjectType, IDoEntity dataToSendToUi, String callbackId) {
    return send(owner, new NoopUiCallbackHandler<>(jsHandlerObjectType), dataToSendToUi, callbackId);
  }

  /**
   * Calls the UI handler with ObjectType as returned by {@link IUiCallbackHandler#uiCallbackHandlerObjectType()} and
   * returns its result as processed by the given {@link IUiCallbackHandler}.
   *
   * @param owner
   *          The {@link IWidget} this request belongs to. Pass the {@link IWidget} that specifies the lifetime of this
   *          UI callback. E.g. if the callback should be performed as long as the user is logged in, use
   *          {@code IDesktop.CURRENT.get()}. The callback will automatically be cancelled as soon as this
   *          {@link IWidget} is disposed.
   * @param handler
   *          An {@link IUiCallbackHandler} implementation that specifies the UI handler ObjectType and the processing
   *          of the response.
   * @return The result as processed by the {@link IUiCallbackHandler}.
   */
  public <R> Future<R> send(IWidget owner, IUiCallbackHandler<?, R> handler) {
    return send(owner, handler, null);
  }

  /**
   * Calls the UI handler with ObjectType as returned by {@link IUiCallbackHandler#uiCallbackHandlerObjectType()} and
   * returns its result as processed by the given {@link IUiCallbackHandler}.
   *
   * @param owner
   *          The {@link IWidget} this request belongs to. Pass the {@link IWidget} that specifies the lifetime of this
   *          UI callback. E.g. if the callback should be performed as long as the user is logged in, use
   *          {@code IDesktop.CURRENT.get()}. The callback will automatically be cancelled as soon as this
   *          {@link IWidget} is disposed.
   * @param handler
   *          An {@link IUiCallbackHandler} implementation that specifies the UI handler ObjectType and the processing
   *          of the response.
   * @param dataToSendToUi
   *          An optional {@link IDoEntity} to send to the UI handler on the browser.
   * @return The result as processed by the {@link IUiCallbackHandler}.
   */
  public <R> Future<R> send(IWidget owner, IUiCallbackHandler<?, R> handler, IDoEntity dataToSendToUi) {
    return send(owner, handler, null, null);
  }

  /**
   * Calls the UI handler with ObjectType as returned by {@link IUiCallbackHandler#uiCallbackHandlerObjectType()} and
   * returns its result as processed by the given {@link IUiCallbackHandler}.
   *
   * @param owner
   *          The {@link IWidget} this request belongs to. Pass the {@link IWidget} that specifies the lifetime of this
   *          UI callback. E.g. if the callback should be performed as long as the user is logged in, use
   *          {@code IDesktop.CURRENT.get()}. The callback will automatically be cancelled as soon as this
   *          {@link IWidget} is disposed.
   * @param handler
   *          An {@link IUiCallbackHandler} implementation that specifies the UI handler ObjectType and the processing
   *          of the response.
   * @param dataToSendToUi
   *          An optional {@link IDoEntity} to send to the UI handler on the browser.
   * @param callbackId
   *          An optional callbackId. All callbacks with the same ID are only sent once to the UI and on a response all
   *          pending callbacks with this ID are resolved at once. By default, each callback has its own unique ID.
   * @return The result as processed by the {@link IUiCallbackHandler}.
   */
  public <R> Future<R> send(IWidget owner, IUiCallbackHandler<?, R> handler, IDoEntity dataToSendToUi, String callbackId) {
    assertNotNull(handler);
    assertNotNull(owner);
    assertModelThread();

    if (StringUtility.isNullOrEmpty(callbackId)) {
      callbackId = UUID.randomUUID().toString();
    }

    boolean isNewId;
    P_UiCallback<R> callback;
    synchronized (m_pendingUiCallbacks) {
      isNewId = !m_pendingUiCallbacks.containsKey(callbackId);
      callback = registerCallback(owner, handler, callbackId);
    }

    if (isNewId) {
      // send only one event to the UI for each ID as one response will resolve all with the same ID.
      fireUiCallbackEvent(callback, dataToSendToUi, owner);
    }
    return callback;
  }

  protected <T> P_UiCallback<T> registerCallback(IWidget owner, IUiCallbackHandler<?, T> handler, String callbackId) {
    P_UiCallback<T> uiCallback = new P_UiCallback<>(owner, handler, callbackId);
    m_pendingUiCallbacks.computeIfAbsent(callbackId, id -> new ArrayList<>()).add(uiCallback);
    return uiCallback;
  }

  protected List<P_UiCallback<?>> getCallbacksInternal(Predicate<P_UiCallback<?>> filter) {
    synchronized (m_pendingUiCallbacks) {
      Stream<P_UiCallback<?>> callbackStream = m_pendingUiCallbacks.values().stream().flatMap(Collection::stream);
      if (filter != null) {
        callbackStream = callbackStream.filter(filter);
      }
      return callbackStream.collect(toList());
    }
  }

  protected List<P_UiCallback<?>> getCallbacksInternal(String callbackId) {
    synchronized (m_pendingUiCallbacks) {
      Collection<P_UiCallback<?>> callbacks = m_pendingUiCallbacks.get(callbackId);
      if (isEmpty(callbacks)) {
        return emptyList();
      }
      return new ArrayList<>(callbacks);
    }
  }

  /**
   * @return An unmodifiable List with all pending callbacks having given ID.
   */
  public List<Future<?>> getCallbacks(String callbackId) {
    return unmodifiableList(getCallbacksInternal(callbackId));
  }

  protected void removeCallback(P_UiCallback<?> callback) {
    synchronized (m_pendingUiCallbacks) {
      String callbackId = callback.m_id;
      Collection<P_UiCallback<?>> callbacks = m_pendingUiCallbacks.get(callbackId);
      if (callbacks == null) {
        return;
      }
      callbacks.removeIf(o -> o.equals(callback));
      if (callbacks.isEmpty()) {
        // this was the last callback pending for this id.
        m_pendingUiCallbacks.remove(callbackId);

        // remove buffered events if still present.
        // this might happen e.g. if callbacks are registered but can never be sent until the owner Widget is disposed.
        m_eventBuffer.remove(callbackId);
      }
    }
  }

  public EventSupport<UiCallbackEvent> getEventSupport() {
    return m_eventSupport;
  }

  protected <R> void fireUiCallbackEvent(P_UiCallback<R> callback, IDoEntity dataToSendToUi, IWidget owner) {
    UiCallbackEvent event = new UiCallbackEvent(this, callback.m_id, callback.m_handler.uiCallbackHandlerObjectType(), dataToSendToUi, owner);
    if (IDesktop.CURRENT.get().isReady()) {
      sendUiCallbackEvent(event);
    }
    else {
      synchronized (m_pendingUiCallbacks) {
        m_eventBuffer.put(callback.m_id, event); // buffer to be sent as soon as the desktop is ready (see #onDesktopReadyChanged)
      }
    }
  }

  protected void sendUiCallbackEvent(UiCallbackEvent event) {
    getEventSupport().fireEvent(event);
  }

  protected void fireCallbackDone(String callbackId, IDoEntity response) {
    getCallbacksInternal(callbackId).forEach(c -> fireCallbackDone(c, response));
  }

  protected void onDesktopClosed() {
    cancelAllUiCallbacks(true);
  }

  protected void onDesktopReadyChanged(boolean wasReady, boolean isNowReady) {
    if (!wasReady && isNowReady) {
      // Desktop is ready (again) on the browser.
      // E.g. after browser refresh or after successful login: send buffered events if available
      consumeEventBuffer().forEach(this::sendUiCallbackEvent);
    }
  }

  protected List<UiCallbackEvent> consumeEventBuffer() {
    synchronized (m_pendingUiCallbacks) {
      if (m_eventBuffer.isEmpty()) {
        return emptyList();
      }
      List<UiCallbackEvent> events = new ArrayList<>(m_eventBuffer.values());
      m_eventBuffer.clear();
      return events;
    }
  }

  protected void fireCallbackFailed(String callbackId, String message, String code) {
    ProcessingException t = new ProcessingException(StringUtility.hasText(message) ? message : "Error in UiCallback handler.");
    if (StringUtility.hasText(code)) {
      t.withContextInfo("code", code);
    }
    getCallbacksInternal(callbackId).forEach(c -> fireCallbackFailed(c, t, message, code));
  }

  protected <RES> void fireCallbackFailed(P_UiCallback<RES> callback, ProcessingException exception, String message, String code) {
    IUiCallbackHandler<?, RES> handler = callback.m_handler;
    Pair<RES, ? extends Throwable> result = handler.onCallbackFailed(exception, message, code);
    finishCallback(callback, result.getLeft(), result.getRight());
  }

  protected <IN extends IDoEntity, RES> void fireCallbackDone(P_UiCallback<RES> callback, IN response) {
    //noinspection unchecked
    IUiCallbackHandler<IN, RES> handler = (IUiCallbackHandler<IN, RES>) callback.m_handler;
    Pair<RES, ? extends Throwable> result = handler.onCallbackDone(response);
    finishCallback(callback, result.getLeft(), result.getRight());
  }

  protected <IN extends IDoEntity, RES> void finishCallback(UiCallback<RES> callback, RES result, Throwable exception) {
    if (exception != null) {
      callback.failed(exception);
    }
    else {
      callback.done(result);
    }
  }

  /**
   * Cancels all callbacks with given ID.
   *
   * @param mayInterruptIfRunning
   *          {@code true} if the thread executing this task should be interrupted. Otherwise, in-progress tasks are
   *          allowed to complete.
   */
  public void cancelUiCallbacks(String callbackId, boolean mayInterruptIfRunning) {
    getCallbacksInternal(callbackId).forEach(c -> c.cancel(mayInterruptIfRunning));
  }

  /**
   * Cancels all callbacks.
   *
   * @param mayInterruptIfRunning
   *          {@code true} if the thread executing this task should be interrupted. Otherwise, in-progress tasks are
   *          allowed to complete.
   */
  public void cancelAllUiCallbacks(boolean mayInterruptIfRunning) {
    getCallbacksInternal((Predicate<P_UiCallback<?>>) null).forEach(c -> cancelCallback(c, mayInterruptIfRunning));
  }

  protected void cancelCallback(UiCallback<?> c, boolean mayInterruptIfRunning) {
    try {
      c.cancel(mayInterruptIfRunning);
    }
    catch (RuntimeException | PlatformError e) {
      LOG.error("Exception while closing UI callback.", e);
    }
  }

  protected IUiCallbacksUIFacade createUIFacade() {
    return BEANS.get(ModelContextProxy.class).newProxy(new P_UIFacade(), ModelContext.copyCurrent());
  }

  public IUiCallbacksUIFacade getUIFacade() {
    return m_uiFacade;
  }

  protected class P_UIFacade implements IUiCallbacksUIFacade {
    @Override
    public void fireCallbackDone(String callbackId, IDoEntity response) {
      UiCallbacks.this.fireCallbackDone(callbackId, response);
    }

    @Override
    public void fireCallbackFailed(String callbackId, String message, String code) {
      UiCallbacks.this.fireCallbackFailed(callbackId, message, code);
    }
  }

  protected class P_UiCallback<T> extends UiCallback<T> {

    private final String m_id;
    private final IUiCallbackHandler<?, T> m_handler;
    private final IWidget m_owner;
    private final P_OwnerDisposeListener m_ownerDisposeListener;

    public P_UiCallback(IWidget owner, IUiCallbackHandler<?, T> handler, String id) {
      m_id = id;
      m_handler = handler;
      m_owner = owner;
      m_ownerDisposeListener = new P_OwnerDisposeListener(this); // automatically cancel this callback on widget disposal
      owner.addPropertyChangeListener(IWidget.PROP_DISPOSE_DONE, m_ownerDisposeListener); // listener is removed on Widget disposal or if callback is done/cancelled/failed.
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
      dispose();
      return super.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean done(T result) {
      dispose();
      return super.done(result);
    }

    @Override
    public boolean failed(Throwable t) {
      dispose();
      return super.failed(t);
    }

    public void dispose() {
      m_owner.removePropertyChangeListener(IWidget.PROP_DISPOSE_DONE, m_ownerDisposeListener);
      removeCallback(this);
    }
  }

  /**
   * {@link IUiCallbackHandler} which returns error and results as it is sent from the browser.
   */
  public static class NoopUiCallbackHandler<T extends IDoEntity> implements IUiCallbackHandler<T, T> {

    private final String m_uiCallbackHandlerObjectType;

    public NoopUiCallbackHandler(String uiCallbackHandlerObjectType) {
      m_uiCallbackHandlerObjectType = uiCallbackHandlerObjectType;
    }

    @Override
    public Pair<T, ? extends Throwable> onCallbackDone(T t) {
      return ImmutablePair.of(t, null);
    }

    @Override
    public String uiCallbackHandlerObjectType() {
      return m_uiCallbackHandlerObjectType;
    }
  }

  protected static class P_OwnerDisposeListener implements PropertyChangeListener {
    private final P_UiCallback<?> m_callback;

    public P_OwnerDisposeListener(P_UiCallback<?> callback) {
      m_callback = callback;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      boolean isDisposed = (Boolean) evt.getNewValue();
      boolean wasDisposed = (Boolean) evt.getOldValue();
      if (isDisposed && !wasDisposed) {
        // if the owner is disposed: cancel associated callback
        m_callback.cancel(true); // will remove the callback in UiCallbacks
      }
    }
  }
}
