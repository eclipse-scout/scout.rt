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
import java.util.Collection;
import java.util.List;

import org.eclipse.scout.rt.client.ui.form.fields.ModelVariant;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.filter.IFilter;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractJsonAdapter<T> implements IJsonAdapter<T> {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractJsonAdapter.class);

  private final IUiSession m_uiSession;
  private final T m_model;
  private final String m_id;
  private boolean m_initialized;
  private boolean m_disposed;
  private IJsonAdapter<?> m_parent;

  public AbstractJsonAdapter(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    if (model == null) {
      throw new IllegalArgumentException("model must not be null");
    }
    m_model = model;
    m_uiSession = uiSession;
    m_id = id;
    m_parent = parent;
    m_uiSession.registerJsonAdapter(this);
  }

  @Override
  public final String getId() {
    return m_id;
  }

  @Override
  public IUiSession getUiSession() {
    return m_uiSession;
  }

  @Override
  public IJsonAdapter<?> getParent() {
    return m_parent;
  }

  @Override
  public T getModel() {
    return m_model;
  }

  /**
   * Attach child adapters of this adapter instance here by calling the <code>attachAdapter[s](model[s])</code> methods.
   * This will also <em>create</em> a new JSON adapter instance when the adapter does not yet exist for the given model.
   */
  protected void attachChildAdapters() {
  }

  protected void disposeChildAdapters() {
    List<IJsonAdapter<?>> childAdapters = getUiSession().getJsonChildAdapters(this);
    for (IJsonAdapter<?> childAdapter : childAdapters) {
      if (!childAdapter.isDisposed()) {
        childAdapter.dispose();
      }
    }
  }

  @Override
  public void init() {
    if (m_initialized) {
      throw new IllegalStateException("Adapter already initialized");
    }
    attachModel();
    attachChildAdapters();
    m_initialized = true;
  }

  /**
   * Override this method in order to attach listeners on the Scout model object. At this point a JsonAdapter instance
   * has been already created for the model object. The default implementation does nothing.
   */
  protected void attachModel() {
  }

  @Override
  public final void dispose() {
    if (m_disposed) {
      throw new IllegalStateException("Adapter already disposed");
    }
    detachModel();
    disposeChildAdapters();
    m_uiSession.unregisterJsonAdapter(m_id);
    m_disposed = true;
  }

  protected void detachModel() {
  }

  @Override
  public boolean isDisposed() {
    return m_disposed;
  }

  @Override
  public boolean isInitialized() {
    return m_initialized;
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = new JSONObject();
    putProperty(json, "id", getId());
    putProperty(json, "objectType", getObjectTypeVariant());
    BEANS.get(InspectorInfo.class).put(getUiSession(), json, getModel());

    // Only send parent if its a global adapter. In the other cases the client may use its creator as parent.
    // Note: The parent adapter is called "owner" in the UI, whereas "parent" refers to the "outer field".
    // FIXME bsh, cgu: What if owner is different from creator but not the root adapter? How to check???
    if (getParent() == getUiSession().getRootJsonAdapter()) {
      putProperty(json, "owner", getParent().getId());
    }
    return json;
  }

  /**
   * Returns a string used to identify the object-type in the JSON output (JSON attribute 'objectType'). Unlike
   * {@link #getObjectType()}, this method returns the object type <b>including the model variant</b> (if set). The
   * resulting string looks like this: <code>"&lt;ObjectType&gt;.&lt;ModelVariant&gt;"</code>.
   *
   * @see {@link ModelVariant}
   */
  protected String getObjectTypeVariant() {
    return JsonAdapterUtility.getObjectType(getObjectType(), getModel());
  }

  @Override
  public void handleUiEvent(JsonEvent event) {
    if (JsonEventType.PROPERTY.matches(event)) {
      handleUiPropertyChange(event);
    }
    else {
      throw new IllegalStateException("Event not handled. " + event);
    }
  }

  protected void handleUiPropertyChange(JsonEvent event) {
    JSONObject data = event.getData();
    for (String propertyName : data.keySet()) {
      handleUiPropertyChange(propertyName, data);
    }
  }

  /**
   * Override this method to handle property changes from the UI. You should use the get[Type] methods of the data
   * JSONObject to retrieve the value. The default impl. does nothing.
   */
  protected void handleUiPropertyChange(String propertyName, JSONObject data) {

  }

  public final <A extends IJsonAdapter<?>, M> A attachAdapter(M model) {
    return attachAdapter(model, null);
  }

  @Override
  public final <A extends IJsonAdapter<?>, M> A attachAdapter(M model, IFilter<M> filter) {
    if (model == null) {
      return null;
    }
    if (filter != null && !filter.accept(model)) {
      return null;
    }
    return m_uiSession.getOrCreateJsonAdapter(model, this);
  }

  @Override
  public final <M> List<IJsonAdapter<?>> attachAdapters(Collection<M> models) {
    return attachAdapters(models, null);
  }

  @Override
  public final <M> List<IJsonAdapter<?>> attachAdapters(Collection<M> models, IFilter<M> filter) {
    List<IJsonAdapter<?>> adapters = new ArrayList<>(models.size());
    for (M model : models) {
      IJsonAdapter<?> adapter = attachAdapter(model, filter);
      if (adapter != null) {
        adapters.add(adapter);
      }
    }
    return adapters;
  }

  /**
   * Returns an existing adapter for the given model. When no adapter is registered for the given model this method will
   * return null. This method is a shortcut for <code>getUiSession().getJsonAdapter(model)</code>.
   */
  @Override
  public final <A extends IJsonAdapter<?>> A getAdapter(Object model) {
    return getAdapter(model, null);
  }

  @Override
  public <A extends IJsonAdapter<?>, MODEL> A getAdapter(MODEL model, IFilter<MODEL> filter) {
    if (model == null) {
      return null;
    }
    if (filter != null && !filter.accept(model)) {
      return null;
    }
    return m_uiSession.getJsonAdapter(model, this);
  }

  @Override
  public final Collection<IJsonAdapter<?>> getAdapters(Collection<?> models) {
    return getAdapters(models, null);
  }

  @Override
  public <MODEL> Collection<IJsonAdapter<?>> getAdapters(Collection<MODEL> models, IFilter<MODEL> filter) {
    List<IJsonAdapter<?>> adapters = new ArrayList<>(models.size());
    for (MODEL model : models) {
      IJsonAdapter<?> adapter = getAdapter(model, filter);
      if (adapter != null) {
        adapters.add(adapter);
      }
    }
    return adapters;
  }

  public final <A extends IJsonAdapter<?>> A getGlobalAdapter(Object model) {
    return getGlobalAdapter(model, null);
  }

  public final <A extends IJsonAdapter<?>, MODEL> A getGlobalAdapter(MODEL model, IFilter<MODEL> filter) {
    if (model == null) {
      return null;
    }
    if (filter != null && !filter.accept(model)) {
      return null;
    }
    return m_uiSession.getJsonAdapter(model, getUiSession().getRootJsonAdapter());
  }

  protected final <MODEL> List<IJsonAdapter<?>> attachGlobalAdapters(Collection<MODEL> models) {
    return attachGlobalAdapters(models, null);
  }

  protected final <MODEL> List<IJsonAdapter<?>> attachGlobalAdapters(Collection<MODEL> models, IFilter<MODEL> filter) {
    List<IJsonAdapter<?>> adapters = new ArrayList<>(models.size());
    for (MODEL model : models) {
      IJsonAdapter<?> adapter = attachGlobalAdapter(model, filter);
      if (adapter != null) {
        adapters.add(adapter);
      }
    }
    return adapters;
  }

  /**
   * A global adapter is registered under the root json adapter and may be used by other adapters.
   * <p>
   * Rule: Always create a global adapter if the model is able to dispose itself (Form, MessageBox, etc). In every other
   * case you have to be very careful. If you dispose a global adapter it may influence others which are using it.
   * <p>
   * Global adapters (like every other) get disposed on session disposal.
   */
  protected final <A extends IJsonAdapter<?>, M> A attachGlobalAdapter(M model) {
    return attachGlobalAdapter(model, null);
  }

  protected final <A extends IJsonAdapter<?>, M> A attachGlobalAdapter(M model, IFilter<M> filter) {
    if (model == null) {
      return null;
    }
    if (filter != null && !filter.accept(model)) {
      return null;
    }
    return m_uiSession.getOrCreateJsonAdapter(model, getUiSession().getRootJsonAdapter());
  }

  protected final <M> JSONObject putAdapterIdProperty(JSONObject json, String key, M model) {
    return putAdapterIdProperty(json, key, model, null);
  }

  protected final <M> JSONObject putAdapterIdProperty(JSONObject json, String key, M model, IFilter<M> filter) {
    if (model == null) {
      return json;
    }
    if (filter != null && !filter.accept(model)) {
      return null;
    }
    return json.put(key, JsonAdapterUtility.getAdapterIdForModel(getUiSession(), model, this));
  }

  protected final <M> JSONObject putAdapterIdsProperty(JSONObject json, String key, Collection<M> models) {
    return putAdapterIdsProperty(json, key, models, null);
  }

  protected final <M> JSONObject putAdapterIdsProperty(JSONObject json, String key, Collection<M> models, IFilter<M> filter) {
    return json.put(key, JsonAdapterUtility.getAdapterIdsForModel(getUiSession(), models, this, filter));
  }

  protected final JSONObject putProperty(JSONObject json, String key, Object value) {
    return json.put(key, value);
  }

  protected final void addActionEvent(String eventName) {
    addActionEvent(eventName, null);
  }

  protected final void addActionEvent(String eventName, JSONObject eventData) {
    addActionEvent(eventName, null, eventData);
  }

  protected final void addActionEvent(String eventName, IJsonAdapter referenceAdapter, JSONObject eventData) {
    if (referenceAdapter == null) {
      getUiSession().currentJsonResponse().addActionEvent(getId(), eventName, eventData);
      LOG.debug("Added action event '{}' for {} with id {}. Model: {}", eventName, getObjectType(), getId(), getModel());
    }
    else {
      getUiSession().currentJsonResponse().addActionEvent(getId(), eventName, referenceAdapter.getId(), eventData);
      LOG.debug("Added action event '{}' for {} with id {} and reference {}. Model: {}", eventName, getObjectType(), getId(), referenceAdapter.getId(), getModel());
    }
  }

  protected final void registerAsBufferedEventsAdapter() {
    getUiSession().currentJsonResponse().registerBufferedEventsAdapter(this);
  }

  protected final void unregisterAsBufferedEventsAdapter() {
    getUiSession().currentJsonResponse().unregisterBufferedEventsAdapter(this);
  }

  /**
   * Like {@link #addActionEvent(String, JSONObject)} but if there are already action events for the same event in the
   * current response, all existing events are removed before adding the new event.
   */
  protected final void replaceActionEvent(String eventName, JSONObject eventData) {
    getUiSession().currentJsonResponse().replaceActionEvent(getId(), eventName, eventData);
  }

  protected void addPropertyChangeEvent(String propertyName, Object newValue) {
    if (newValue instanceof IJsonAdapter<?>) {
      throw new IllegalArgumentException("Cannot pass an adapter instance to a JSON response");
    }
    getUiSession().currentJsonResponse().addPropertyChangeEvent(getId(), propertyName, newValue);
  }

  @Override
  public void cleanUpEventFilters() {
  }

  @Override
  public void processBufferedEvents() {
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(getObjectType());
    sb.append("[id=" + getId());
    sb.append(", modelClass=");
    sb.append(getModel() == null ? "null" : getModel().getClass().getName());
    sb.append(", parentId=");
    sb.append(getParent() == null ? "null" : getParent().getId());
    sb.append("]");
    return sb.toString();
  }
}
