/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gathers every {@link IJsonObjectFactory} and delegates the creation to these factories, considering the registration
 * order (see {@link Order}).
 */
@ApplicationScoped
public class MainJsonObjectFactory implements IJsonObjectFactory {
  private static final Logger LOG = LoggerFactory.getLogger(MainJsonObjectFactory.class);

  private List<IJsonObjectFactory> m_factories;

  private List<IJsonObjectFactory> getFactories() {
    if (m_factories == null) {
      m_factories = createFactories();
      if (m_factories == null) {
        throw new IllegalStateException("No json object factories found.");
      }
      LOG.info("Using following object factories: {}", m_factories);
    }
    return m_factories;
  }

  private List<IJsonObjectFactory> createFactories() {
    List<IJsonObjectFactory> factories = new ArrayList<>();
    for (IJsonObjectFactory factory : BEANS.all(IJsonObjectFactory.class)) {
      if (factory != this) {
        factories.add(factory);
      }
    }
    return factories;
  }

  @Override
  public IJsonAdapter<?> createJsonAdapter(Object model, IUiSession session, String id, IJsonAdapter<?> parent) {
    for (IJsonObjectFactory factory : getFactories()) {
      IJsonAdapter adapter = factory.createJsonAdapter(model, session, id, parent);
      if (adapter != null) {
        return adapter;
      }
    }
    throw new IllegalArgumentException("No factory found for model " + model);
  }

  @Override
  public IJsonObject createJsonObject(Object object) {
    for (IJsonObjectFactory factory : getFactories()) {
      IJsonObject jsonObject = factory.createJsonObject(object);
      if (jsonObject != null) {
        return jsonObject;
      }
    }
    return new JsonBean(object, this);
  }

  public static MainJsonObjectFactory get() {
    return BEANS.get(MainJsonObjectFactory.class);
  }
}
