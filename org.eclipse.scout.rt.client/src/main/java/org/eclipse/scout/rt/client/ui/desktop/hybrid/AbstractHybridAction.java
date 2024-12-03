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

import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNull;
import static org.eclipse.scout.rt.platform.util.TypeCastUtility.getGenericsParameterClass;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;

public abstract class AbstractHybridAction<DO_ENTITY extends IDoEntity> implements IHybridAction<DO_ENTITY> {

  private final Class<DO_ENTITY> m_doEntityClass;
  private String m_id;
  private HybridActionContextElements m_contextElements;
  private boolean m_initialized;

  public AbstractHybridAction() {
    //noinspection unchecked
    m_doEntityClass = assertNotNull(getGenericsParameterClass(getClass(), IHybridAction.class));
  }

  @Override
  public Class<DO_ENTITY> getDoEntityClass() {
    return m_doEntityClass;
  }

  protected String getHybridActionType() {
    return getClass().getAnnotation(HybridActionType.class).value();
  }

  @Override
  public void init(String id, HybridActionContextElements contextElements) {
    m_id = id;
    m_contextElements = contextElements;
    m_initialized = true;
  }

  protected String getId() {
    return m_id;
  }

  /**
   * Returns all context elements associated with this action, or {@code null} if the action has no context elements.
   */
  protected HybridActionContextElements getContextElements() {
    return m_contextElements;
  }

  /**
   * Returns a list of context elements associated with this action for the given key. If the given key does not exist,
   * an {@link AssertionException} is thrown. Use {@link #optContextElements(String)} to return {@code null} instead.
   * Use {@link #getContextElement(String)} if only a single context element is expected.
   */
  protected List<HybridActionContextElement> getContextElements(String key) {
    HybridActionContextElements contextElements = assertNotNull(m_contextElements, "Missing context elements");
    return contextElements.getList(key);
  }

  /**
   * Returns a list of context elements associated with this action for the given key, or {@code null} if the action
   * has no context elements or the given key does not exist.
   */
  protected List<HybridActionContextElement> optContextElements(String key) {
    return m_contextElements == null ? null : m_contextElements.optList(key);
  }

  /**
   * Returns the context element associated with this action for the given key. If the given key does not exist, an
   * {@link AssertionException} is thrown. Use {@link #optContextElement(String)} to return {@code null} instead. Use
   * {@link #getContextElements(String)} if more than one context element is expected.
   */
  protected HybridActionContextElement getContextElement(String key) {
    HybridActionContextElements contextElements = assertNotNull(m_contextElements, "Missing context elements");
    return contextElements.getSingle(key);
  }

  /**
   * Returns the context element associated with this action for the given key, or {@code null} if the action has no
   * context elements or the given key does not exist. Use {@link #optContextElements(String)} if more than one context
   * element is expected.
   */
  protected HybridActionContextElement optContextElement(String key) {
    return m_contextElements == null ? null : m_contextElements.optSingle(key);
  }

  protected boolean isInitialized() {
    return m_initialized;
  }

  protected void assertInitialized() {
    Assertions.assertTrue(isInitialized(), "{} is not initialized", this);
  }

  protected HybridManager hybridManager() {
    return HybridManager.get();
  }

  /**
   * @see HybridManager#addWidgets(Map)
   */
  protected void addWidget(IWidget widget) {
    assertInitialized();
    hybridManager().addWidget(getId(), widget);
  }

  /**
   * @see HybridManager#addWidgets(Map)
   */
  protected void addWidgets(Map<String, ? extends IWidget> widgets) {
    assertInitialized();
    hybridManager().addWidgets(Optional.ofNullable(widgets).orElse(Map.of())
        .entrySet().stream()
        .collect(Collectors.toMap(entry -> getId() + entry.getKey(), Entry::getValue)));
  }

  /**
   * @see HybridManager#removeWidgets(Collection)
   */
  protected void removeWidget() {
    assertInitialized();
    hybridManager().removeWidgetById(getId());
  }

  /**
   * @see HybridManager#removeWidgets(Collection)
   */
  protected void removeWidgetsById(Collection<String> ids) {
    assertInitialized();
    hybridManager().removeWidgetsById(Optional.ofNullable(ids).orElse(Collections.emptySet())
        .stream()
        .map(id -> getId() + id)
        .collect(Collectors.toSet()));
  }

  /**
   * @see HybridManager#removeWidgets(Collection)
   */
  protected void removeWidget(IWidget widget) {
    hybridManager().removeWidget(widget);
  }

  /**
   * @see HybridManager#removeWidgets(Collection)
   */
  protected void removeWidgets(Collection<IWidget> widgets) {
    hybridManager().removeWidgets(widgets);
  }

  protected void fireHybridEvent(String eventType) {
    assertInitialized();
    hybridManager().fireHybridEvent(getId(), eventType);
  }

  protected void fireHybridEvent(String eventType, IDoEntity data) {
    assertInitialized();
    hybridManager().fireHybridEvent(getId(), eventType, data);
  }

  protected void fireHybridEvent(String eventType, HybridActionContextElements contextElements) {
    assertInitialized();
    hybridManager().fireHybridEvent(getId(), eventType, contextElements);
  }

  protected void fireHybridEvent(String eventType, IDoEntity data, HybridActionContextElements contextElements) {
    assertInitialized();
    hybridManager().fireHybridEvent(getId(), eventType, data, contextElements);
  }

  protected void fireHybridActionEndEvent() {
    assertInitialized();
    hybridManager().fireHybridActionEndEvent(getId());
  }

  protected void fireHybridActionEndEvent(IDoEntity data) {
    assertInitialized();
    hybridManager().fireHybridActionEndEvent(getId(), data);
  }

  protected void fireHybridActionEndEvent(HybridActionContextElements contextElements) {
    assertInitialized();
    hybridManager().fireHybridActionEndEvent(getId(), contextElements);
  }

  protected void fireHybridActionEndEvent(IDoEntity data, HybridActionContextElements contextElements) {
    assertInitialized();
    hybridManager().fireHybridActionEndEvent(getId(), data, contextElements);
  }

  protected void fireHybridWidgetEvent(String eventType) {
    assertInitialized();
    hybridManager().fireHybridWidgetEvent(getId(), eventType);
  }

  protected void fireHybridWidgetEvent(String eventType, IDoEntity data) {
    assertInitialized();
    hybridManager().fireHybridWidgetEvent(getId(), eventType, data);
  }

  protected void fireHybridWidgetEvent(String id, String eventType) {
    assertInitialized();
    hybridManager().fireHybridWidgetEvent(getId() + id, eventType);
  }

  protected void fireHybridWidgetEvent(String id, String eventType, IDoEntity data) {
    assertInitialized();
    hybridManager().fireHybridWidgetEvent(getId() + id, eventType, data);
  }

  protected void fireHybridWidgetEvent(IWidget widget, String eventType) {
    hybridManager().fireHybridWidgetEvent(widget, eventType);
  }

  protected void fireHybridWidgetEvent(IWidget widget, String eventType, IDoEntity data) {
    hybridManager().fireHybridWidgetEvent(widget, eventType, data);
  }
}
