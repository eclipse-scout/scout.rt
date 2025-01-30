/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.desktop.hybrid.converter;

import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNull;
import static org.eclipse.scout.rt.platform.util.TypeCastUtility.getGenericsParameterClass;

import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;

/**
 * Convenience base implementation of an {@link IHybridActionContextElementConverter} which uses the values of the
 * generic parameters to determine if it can convert the requested input. Subclasses only need to implement the actual
 * conversion ({@link #jsonToElement(IJsonAdapter, Object)} and {@link #elementToJson(IJsonAdapter, Object)}).
 */
public abstract class AbstractHybridActionContextElementConverter<ADAPTER extends IJsonAdapter<?>, JSON_ELEMENT, MODEL_ELEMENT> implements IHybridActionContextElementConverter<ADAPTER, JSON_ELEMENT, MODEL_ELEMENT> {

  private final Class<ADAPTER> m_adapterClass;
  private final Class<JSON_ELEMENT> m_jsonElementClass;
  private final Class<MODEL_ELEMENT> m_modelElementClass;

  @SuppressWarnings("unchecked")
  protected AbstractHybridActionContextElementConverter() {
    m_adapterClass = assertNotNull(getGenericsParameterClass(getClass(), AbstractHybridActionContextElementConverter.class, 0));
    m_jsonElementClass = assertNotNull(getGenericsParameterClass(getClass(), AbstractHybridActionContextElementConverter.class, 1));
    m_modelElementClass = assertNotNull(getGenericsParameterClass(getClass(), AbstractHybridActionContextElementConverter.class, 2));
  }

  protected Class<ADAPTER> getAdapterClass() {
    return m_adapterClass;
  }

  protected Class<JSON_ELEMENT> getJsonElementClass() {
    return m_jsonElementClass;
  }

  protected Class<MODEL_ELEMENT> getModelElementClass() {
    return m_modelElementClass;
  }

  @Override
  public Object tryConvertFromJson(IJsonAdapter<?> adapter, Object jsonElement) {
    if (acceptAdapter(adapter) && acceptJsonElement(jsonElement)) {
      return jsonToElement(getAdapterClass().cast(adapter), getJsonElementClass().cast(jsonElement));
    }
    return null;
  }

  @Override
  public Object tryConvertToJson(IJsonAdapter<?> adapter, Object modelElement) {
    if (acceptAdapter(adapter) && acceptModelElement(modelElement)) {
      return elementToJson(getAdapterClass().cast(adapter), getModelElementClass().cast(modelElement));
    }
    return null;
  }

  protected boolean acceptAdapter(IJsonAdapter<?> adapter) {
    return getAdapterClass().isInstance(adapter) && acceptAdapterImpl(getAdapterClass().cast(adapter));
  }

  protected boolean acceptAdapterImpl(ADAPTER adapter) {
    return true;
  }

  protected boolean acceptJsonElement(Object jsonElement) {
    return getJsonElementClass().isInstance(jsonElement) && acceptJsonElementImpl(getJsonElementClass().cast(jsonElement));
  }

  protected boolean acceptJsonElementImpl(JSON_ELEMENT jsonElement) {
    return true;
  }

  protected boolean acceptModelElement(Object modelElement) {
    return getModelElementClass().isInstance(modelElement) && acceptModelElementImpl(getModelElementClass().cast(modelElement));
  }

  protected boolean acceptModelElementImpl(MODEL_ELEMENT element) {
    return true;
  }

  /**
   * @param adapter
   *     owner widget (e.g. TreeAdapter)
   * @param jsonElement
   *     JSON representation of the element (e.g. String)
   * @return model representation of the element (e.g. TreeNode)
   */
  protected abstract MODEL_ELEMENT jsonToElement(ADAPTER adapter, JSON_ELEMENT jsonElement);

  /**
   * @param adapter
   *     owner widget (e.g. TreeAdapter)
   * @param element
   *     model representation of the element (e.g. TreeNode)
   * @return JSON representation of the element (e.g. String)
   */
  protected abstract JSON_ELEMENT elementToJson(ADAPTER adapter, MODEL_ELEMENT element);
}
