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

import org.eclipse.scout.rt.platform.index.AbstractMultiValueIndex;
import org.eclipse.scout.rt.platform.index.AbstractSingleValueIndex;
import org.eclipse.scout.rt.platform.index.IndexedStore;
import org.eclipse.scout.rt.platform.util.CompositeObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents a per session registry for {@link IJsonAdapter} instances.
 */
public class JsonAdapterRegistry {

  private static final Logger LOG = LoggerFactory.getLogger(JsonAdapterRegistry.class);

  private final IndexedStore<IJsonAdapter<?>> m_store;

  private final P_IndexById m_idxById;
  private final P_IndexByModelAndParent m_idxByModelAndParent;
  private final P_IndexByParent m_idxByParent;
  private final P_IndexByModel m_idxByModel;

  public JsonAdapterRegistry() {
    m_store = new IndexedStore<>();

    m_idxById = m_store.registerIndex(new P_IndexById());
    m_idxByModelAndParent = m_store.registerIndex(new P_IndexByModelAndParent());
    m_idxByParent = m_store.registerIndex(new P_IndexByParent());
    m_idxByModel = m_store.registerIndex(new P_IndexByModel());
  }

  /**
   * Adds the given adapter to this registry.
   */
  public void add(final IJsonAdapter<?> adapter) {
    m_store.add(adapter);
  }

  /**
   * Removes the adapter with the given <code>adapterId</code> from this registry.
   */
  public void remove(final String adapterId) {
    final IJsonAdapter<?> adapter = getById(adapterId);
    if (adapter != null) {
      m_store.remove(adapter);
    }
  }

  /**
   * Returns the number of adapters contained in this registry.
   */
  public int size() {
    return m_store.size();
  }

  /**
   * Returns the adapter with the given <code>adapterId</code>.
   */
  public IJsonAdapter<?> getById(final String adapterId) {
    return m_idxById.get(adapterId);
  }

  /**
   * Returns the adapter which belongs to the given model object and has the given adapter as its parent adapter.
   */
  @SuppressWarnings("unchecked")
  public <MODEL, ADAPTER extends IJsonAdapter<? super MODEL>> ADAPTER getByModelAndParentAdapter(final MODEL model, final IJsonAdapter<?> parent) {
    return (ADAPTER) m_idxByModelAndParent.get(createModelAndParentAdapterPair(model, parent));
  }

  /**
   * Returns all adapters that have the given adapter as their parent adapter.
   */
  public List<IJsonAdapter<?>> getByParentAdapter(final IJsonAdapter<?> parentAdapter) {
    return m_idxByParent.get(parentAdapter);
  }

  /**
   * Returns all adapters for the given model object.
   */
  @SuppressWarnings("unchecked")
  public <MODEL> List<IJsonAdapter<MODEL>> getByModel(final MODEL model) {
    final List<IJsonAdapter<?>> adaptersRaw = m_idxByModel.get(model);

    final List<IJsonAdapter<MODEL>> adapters = new ArrayList<>(adaptersRaw.size());
    for (final IJsonAdapter<?> adapterRaw : adaptersRaw) {
      adapters.add((IJsonAdapter<MODEL>) adapterRaw);
    }
    return adapters;
  }

  /**
   * Clears and disposes all contained adapters.
   */
  public void disposeAdapters() {
    for (final IJsonAdapter<?> adapter : m_store.values()) {
      if (!adapter.isDisposed()) {
        adapter.dispose();
      }
    }

    // "Memory leak detection". After disposing all adapters and flushing the session, no adapters should be remaining.
    if (!m_store.isEmpty()) {
      LOG.error("Memory leak detected: JsonAdapterRegistry expected to be empty after disposing all adapters [adapterCount={}]", m_store.size());
    }
  }

  // ====  Index definitions ==== //

  private class P_IndexById extends AbstractSingleValueIndex<String, IJsonAdapter<?>> {

    @Override
    protected String calculateIndexFor(final IJsonAdapter<?> adapter) {
      return adapter.getId();
    }
  }

  private class P_IndexByModelAndParent extends AbstractSingleValueIndex<CompositeObject, IJsonAdapter<?>> {

    @Override
    protected CompositeObject calculateIndexFor(final IJsonAdapter<?> adapter) {
      return createModelAndParentAdapterPair(adapter.getModel(), adapter.getParent());
    }
  }

  private class P_IndexByParent extends AbstractMultiValueIndex<IJsonAdapter<?>, IJsonAdapter<?>> {

    @Override
    protected IJsonAdapter<?> calculateIndexFor(final IJsonAdapter<?> adapter) {
      return adapter.getParent();
    }
  }

  private class P_IndexByModel extends AbstractMultiValueIndex<Object, IJsonAdapter<?>> {

    @Override
    protected Object calculateIndexFor(final IJsonAdapter<?> adapter) {
      return adapter.getModel();
    }
  }

  private <MODEL> CompositeObject createModelAndParentAdapterPair(final MODEL model, final IJsonAdapter<?> parentAdapter) {
    return new CompositeObject(model, parentAdapter);
  }
}
