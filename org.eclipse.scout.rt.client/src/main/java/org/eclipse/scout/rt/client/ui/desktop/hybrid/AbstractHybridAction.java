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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.platform.util.Assertions;

public abstract class AbstractHybridAction<DO_ENTITY extends IDoEntity> implements IHybridAction<DO_ENTITY> {

  private final Class<DO_ENTITY> m_doEntityClass;
  private String m_id;
  private HybridActionContextElement m_contextElement;
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
  public void init(String id, HybridActionContextElement contextElement) {
    m_id = id;
    m_contextElement = contextElement;
    m_initialized = true;
  }

  protected String getId() {
    return m_id;
  }

  protected HybridActionContextElement getContextElement() {
    return m_contextElement;
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

  protected void fireHybridActionEndEvent() {
    assertInitialized();
    hybridManager().fireHybridActionEndEvent(getId());
  }

  protected void fireHybridActionEndEvent(IDoEntity data) {
    assertInitialized();
    hybridManager().fireHybridActionEndEvent(getId(), data);
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
