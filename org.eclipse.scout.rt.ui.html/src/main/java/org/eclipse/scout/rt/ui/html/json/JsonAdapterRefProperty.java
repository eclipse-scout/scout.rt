/*
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.ui.html.json;

import java.util.Collection;

import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.json.JSONArray;

/**
 * Used to reference an adapter without managing its lifecycle, meaning no adapter will be created or disposed. If the
 * referenced adapter does not exist when it is being prepared for JSON an exception will be thrown.
 */
@SuppressWarnings("squid:S00118")
public abstract class JsonAdapterRefProperty<MODEL> extends JsonProperty<MODEL> {
  private IJsonAdapter<?> m_rootAdapter;

  public JsonAdapterRefProperty(String propertyName, MODEL model, IJsonAdapter rootAdapter) {
    super(propertyName, model);
    m_rootAdapter = rootAdapter;
  }

  @Override
  public Object valueToJson() {
    // ValueToJson is called when adapter.toJson() is called (initial case). Because in that initial case IJsonObjects are not resolved
    // -> don't use IJsonObject but return the id directly.
    return toJsonInternal(modelValue());
  }

  @Override
  public Object prepareValueForToJson(Object value) {
    // The referenced adapter might not be created yet
    // (e.g. a detail table created by JsonOutline is not created because the EventBuffer has not been processed yet)
    // -> IJsonObject is resolved later by JsonPropertyChangeEvent
    return new IJsonObject() {
      @Override
      public Object toJson() {
        return toJsonInternal(value);
      }
    };
  }

  protected Object toJsonInternal(Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof Collection) {
      return elementsToJson((Collection) value);
    }
    return elementToJson(value);
  }

  protected JSONArray elementsToJson(Collection elements) {
    JSONArray jsonAdapterIds = new JSONArray();
    for (Object element : elements) {
      Object id = elementToJson(element);
      jsonAdapterIds.put(id);
    }
    return jsonAdapterIds;
  }

  protected Object elementToJson(Object element) {
    if (element == null) {
      return null;
    }
    if (element instanceof IWidget) {
      return findAdapterId((IWidget) element);
    }
    throw new ProcessingException("Unexpected data type for element " + element);
  }

  protected String findAdapterId(IWidget widget) {
    IJsonAdapter<?> adapter = JsonAdapterUtility.findChildAdapter(m_rootAdapter, widget);
    if (adapter == null) {
      throw new IllegalStateException("Adapter not found " + widget);
    }
    return adapter.getId();
  }

  public IJsonAdapter<?> getRootAdapter() {
    return m_rootAdapter;
  }
}
