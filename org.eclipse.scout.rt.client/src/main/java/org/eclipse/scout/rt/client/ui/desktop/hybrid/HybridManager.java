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
import static org.eclipse.scout.rt.platform.util.ObjectUtility.nvl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.basic.filechooser.IFileChooser;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.messagebox.IMessageBox;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.reflect.AbstractPropertyObserver;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.event.FastListenerList;

@Bean
public class HybridManager extends AbstractPropertyObserver {

  public static final String PROP_WIDGETS = "widgets";

  private final IHybridManagerUIFacade m_uiFacade;
  private final P_WidgetDisposeListener m_widgetDisposeListener;
  private final FastListenerList<HybridEventListener> m_listeners = new FastListenerList<>();

  private Map<String, Class<? extends IHybridAction>> m_hybridActionMap = null;

  public HybridManager() {
    m_uiFacade = createUIFacade();
    m_widgetDisposeListener = createWidgetDisposeListener();
    setWidgetsInternal(emptyMap());
  }

  // static helpers

  public static HybridManager get() {
    return IDesktop.CURRENT.get().getAddOn(HybridManager.class);
  }

  // general

  public void clear() {
    clearWidgets();
  }

  // widgets

  public void clearWidgets() {
    Collection<IWidget> widgets = getWidgets().values().stream().filter(Objects::nonNull).collect(Collectors.toSet());
    setWidgetsInternal(emptyMap());
    widgets.forEach(this::disposeWidget);
  }

  protected void disposeWidget(IWidget widget) {
    if (widget == null) {
      return;
    }
    if (widget instanceof IForm) {
      ((IForm) widget).doClose();
    }
    else if (widget instanceof IFileChooser) {
      ((IFileChooser) widget).doClose();
    }
    else if (widget instanceof IMessageBox) {
      ((IMessageBox) widget).doClose();
    }
    else {
      widget.dispose();
    }
  }

  /**
   * @see #addWidgets(Map)
   */
  public void addWidget(String id, IWidget widget) {
    addWidgets(singletonMap(id, widget));
  }

  /**
   * Adds widgets to the hybrid manager to make them available remotely. This will remotely trigger a `widgetAdd:${id}`
   * event for each added widget.
   * <p>
   * <b>Important</b>: Added widgets need to be removed again when they are no longer needed, so they will be removed
   * remotely as well. If a widget is disposed it will be removed automatically. Normally, a widget is disposed by its
   * parent. If your widget doesn't have a parent, you need to call ({@link IWidget#dispose}) manually. You can also
   * initiate the disposing remotely by using the {@link DisposeWidgetsHybridAction}.
   *
   * @see #removeWidgets(Collection)
   */
  public void addWidgets(Map<String, IWidget> widgets) {
    Map<String, IWidget> result = new HashMap<>(getWidgets());
    widgets.forEach((id, widget) -> {
      if (widget == null) {
        throw new IllegalArgumentException("Widget for id '" + id + "' is null.");
      }
      IWidget previousWidget = result.put(id, widget);
      if (previousWidget != null && previousWidget != widget) {
        throw new IllegalArgumentException("Widget for id '" + id + "' already exists.");
      }
    });
    setWidgetsInternal(result);
  }

  /**
   * @see #removeWidgets(Collection)
   */
  public void removeWidgetById(String id) {
    removeWidgetsById(List.of(id));
  }

  /**
   * @see #removeWidgets(Collection)
   */
  public void removeWidgetsById(Collection<String> ids) {
    Map<String, IWidget> result = new HashMap<>(getWidgets());
    ids.forEach(result::remove);
    setWidgetsInternal(result);
  }

  /**
   * @see #removeWidgets(Collection)
   */
  public void removeWidget(IWidget widget) {
    removeWidgets(List.of(widget));
  }

  /**
   * Removes widgets from the hybrid manager to remove and destroy them remotely as well. This will remotely trigger a
   * `widgetRemove:${id}` event for each removed widget.
   *
   * @see #addWidgets(Map)
   */
  public void removeWidgets(Collection<IWidget> widgets) {
    setWidgetsInternal(getWidgets().entrySet().stream()
        .filter(entry -> !widgets.contains(entry.getValue()))
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue)));
  }

  protected void setWidgetsInternal(Map<String, IWidget> widgets) {
    disarmWidgetDisposeListeners();
    propertySupport.setProperty(PROP_WIDGETS, widgets);
    armWidgetDisposeListeners();
  }

  protected void disarmWidgetDisposeListeners() {
    getWidgets().values().stream().filter(Objects::nonNull).forEach(this::disarmWidgetDisposeListener);
  }

  protected void disarmWidgetDisposeListener(IWidget widget) {
    widget.removePropertyChangeListener(IWidget.PROP_DISPOSE_DONE, getWidgetDisposeListener());
  }

  protected void armWidgetDisposeListeners() {
    getWidgets().values().stream().filter(Objects::nonNull).forEach(this::armWidgetDisposeListener);
  }

  protected void armWidgetDisposeListener(IWidget widget) {
    widget.addPropertyChangeListener(IWidget.PROP_DISPOSE_DONE, getWidgetDisposeListener());
  }

  protected String getWidgetId(IWidget widget) {
    return getWidgets().entrySet().stream()
        .filter(entry -> ObjectUtility.equals(entry.getValue(), widget))
        .map(Entry::getKey)
        .findAny()
        .orElse(null);
  }

  public IWidget getWidgetById(String id) {
    return getWidgets().get(id);
  }

  public Map<String, IWidget> getWidgets() {
    return Collections.unmodifiableMap(nvl(getWidgetsInternal(), emptyMap()));
  }

  protected Map<String, IWidget> getWidgetsInternal() {
    // noinspection unchecked
    return (Map<String, IWidget>) propertySupport.getProperty(PROP_WIDGETS, Map.class);
  }

  // listeners

  public void addHybridEventListener(HybridEventListener listener) {
    m_listeners.add(listener);
  }

  public void removeHybridEventListener(HybridEventListener listener) {
    m_listeners.remove(listener);
  }

  // hybrid events (java to js)

  protected void fireHybridEvent(HybridEvent event) {
    m_listeners.list().forEach(listener -> listener.handle(event));
  }

  public void fireHybridEvent(String id, String eventType) {
    fireHybridEvent(id, eventType, null, null);
  }

  public void fireHybridEvent(String id, String eventType, IDoEntity data, Map<String, HybridActionContextElement> contextElements) {
    fireHybridEvent(HybridEvent.createHybridEvent(this, id, eventType, data, contextElements));
  }

  public void fireHybridActionEndEvent(String id) {
    fireHybridActionEndEvent(id, null, null);
  }

  public void fireHybridActionEndEvent(String id, IDoEntity data, Map<String, HybridActionContextElement> contextElements) {
    fireHybridEvent(HybridEvent.createHybridActionEndEvent(this, id, data, contextElements));
  }

  public void fireHybridWidgetEvent(String id, String eventType) {
    fireHybridWidgetEvent(id, eventType, null);
  }

  public void fireHybridWidgetEvent(String id, String eventType, IDoEntity data) {
    fireHybridEvent(HybridEvent.createHybridWidgetEvent(this, id, eventType, data));
  }

  public void fireHybridWidgetEvent(IWidget widget, String eventType) {
    fireHybridWidgetEvent(widget, eventType, null);
  }

  public void fireHybridWidgetEvent(IWidget widget, String eventType, IDoEntity data) {
    fireHybridWidgetEvent(getWidgetId(widget), eventType, data);
  }

  // hybrid actions (js to java)

  private void handleHybridAction(String id, String actionType, IDoEntity data, Map<String, HybridActionContextElement> contextElements) {
    if (m_hybridActionMap == null) {
      m_hybridActionMap = BEANS.getBeanManager().getBeans(IHybridAction.class).stream()
          .filter(bean -> bean.hasAnnotation(HybridActionType.class))
          .collect(Collectors.toMap(bean -> bean.getBeanAnnotation(HybridActionType.class).value(), IBean::getBeanClazz));
    }
    Optional.ofNullable(m_hybridActionMap.get(actionType))
        .map(BEANS::get)
        .map(hybridAction -> (IHybridAction<?>) hybridAction) // FIXME bsh [js-bookmark] Why is this needed?
        .ifPresent(hybridAction -> hybridAction.execute(id, data, contextElements));
  }

  public IHybridManagerUIFacade getUIFacade() {
    return m_uiFacade;
  }

  protected P_UIFacade createUIFacade() {
    return new P_UIFacade();
  }

  protected class P_UIFacade implements IHybridManagerUIFacade {

    @Override
    public void handleHybridActionFromUI(String id, String actionType, IDoEntity data, Map<String, HybridActionContextElement> contextElements) {
      handleHybridAction(id, actionType, data, contextElements);
    }
  }

  protected P_WidgetDisposeListener getWidgetDisposeListener() {
    return m_widgetDisposeListener;
  }

  protected P_WidgetDisposeListener createWidgetDisposeListener() {
    return new P_WidgetDisposeListener();
  }

  protected class P_WidgetDisposeListener implements PropertyChangeListener {

    @Override
    public void propertyChange(PropertyChangeEvent event) {
      if (IWidget.PROP_DISPOSE_DONE.equals(event.getPropertyName()) && event.getSource() instanceof IWidget) {
        removeWidget((IWidget) event.getSource());
      }
    }
  }
}
