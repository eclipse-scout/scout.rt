/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
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
import java.util.LinkedHashMap;
import java.util.List;

import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.fields.ModelVariant;
import org.eclipse.scout.rt.ui.html.json.desktop.JsonDesktop;
import org.json.JSONObject;

public abstract class AbstractJsonAdapter<T> implements IJsonAdapter<T> {

  private final IJsonSession m_jsonSession;
  private final T m_model;
  private final String m_id;
  private boolean m_attached;
  private IJsonAdapter<?> m_parent;

  public AbstractJsonAdapter(T model, IJsonSession jsonSession, String id, IJsonAdapter<?> parent) {
    if (model == null) {
      throw new IllegalArgumentException("model must not be null");
    }
    m_model = model;
    m_jsonSession = jsonSession;
    m_id = id;
    m_parent = parent;
    m_jsonSession.registerJsonAdapter(this);
  }

  /**
   * Init method which is called by the factory <em>after</em> the constructor has been executed. The default
   * implementation calls <code>attach()</code> and <code>attachChildAdapters()</code>.
   */
  @Override
  public void init() {
    attach();
  }

  @Override
  public final String getId() {
    return m_id;
  }

  public IJsonSession getJsonSession() {
    return m_jsonSession;
  }

  protected JsonDesktop<IDesktop> getJsonDesktop() {
    return getJsonSession().getJsonClientSession().getJsonDesktop();
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
    List<IJsonAdapter<?>> childAdapters = getJsonSession().getJsonChildAdapters(this);
    for (IJsonAdapter<?> childAdapter : childAdapters) {
      childAdapter.dispose();
    }
  }

  @Override
  public final void attach() {
    if (m_attached) {
      throw new IllegalStateException("Adapter already attached");
    }
    attachModel();
    attachChildAdapters();
    m_attached = true;
  }

  /**
   * Override this method in order to attach listeners on the Scout model object.
   * At this point a JsonAdapter instance has been already created for the model object.
   * The default implementation does nothing.
   */
  protected void attachModel() {
  }

  @Override
  public final void dispose() {
    if (m_attached) {
      detachModel();
      disposeChildAdapters();
      m_attached = false;
    }
    m_jsonSession.unregisterJsonAdapter(m_id);
  }

  protected void detachModel() {
  }

  @Override
  public boolean isAttached() {
    return m_attached;
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = new JSONObject(new LinkedHashMap<>()); // TODO BSH Remove when JSONObject uses LinkedHashMap by default
    putProperty(json, "id", getId());
    putProperty(json, "objectType", getObjectTypeVariant());

    // Only send parent if its a global adapter. In the other cases the client may use its creator as parent.
    if (getParent() == getJsonSession().getRootJsonAdapter()) {
      putProperty(json, "parent", getParent().getId());
    }
    return json;
  }

  protected String getObjectTypeVariant() {
    String objectType = getObjectType();
    if (getModel().getClass().isAnnotationPresent(ModelVariant.class)) {
      ModelVariant modelVariant = getModel().getClass().getAnnotation(ModelVariant.class);
      return objectType + "." + modelVariant.value();
    }
    else {
      return objectType;
    }
  }

  @Override
  public void handleUiEvent(JsonEvent event, JsonResponse res) {
    throw new IllegalStateException("Event not handled. " + event);
  }

  @Override
  public final <A extends IJsonAdapter<?>> A attachAdapter(Object model) {
    if (model == null) {
      return null;
    }
    return attachAdapter(model, null);
  }

  protected final <A extends IJsonAdapter<?>> A attachAdapter(Object model, IJsonAdapterFactory adapterFactory) {
    return m_jsonSession.getOrCreateJsonAdapter(model, this, adapterFactory);
  }

  protected final List<IJsonAdapter<?>> attachAdapters(Collection<?> models) {
    List<IJsonAdapter<?>> adapters = new ArrayList<>(models.size());
    for (Object model : models) {
      adapters.add(attachAdapter(model));
    }
    return adapters;
  }

  /**
   * Returns an existing adapter for the given model. When no adapter is registered for the given model this method will
   * return null. This method is a shortcut for <code>getJsonSession().getJsonAdapter(model)</code>.
   */
  @Override
  public final <A extends IJsonAdapter<?>> A getAdapter(Object model) {
    return m_jsonSession.getJsonAdapter(model, this);
  }

  public final <A extends IJsonAdapter<?>> A getGlobalAdapter(Object model) {
    return m_jsonSession.getJsonAdapter(model, getJsonSession().getRootJsonAdapter());
  }

  @Override
  public final Collection<IJsonAdapter<?>> getAdapters(Collection<?> models) {
    List<IJsonAdapter<?>> adapters = new ArrayList<>(models.size());
    for (Object model : models) {
      adapters.add(getAdapter(model));
    }
    return adapters;
  }

  protected final List<IJsonAdapter<?>> attachGlobalAdapters(Collection<?> models) {
    List<IJsonAdapter<?>> adapters = new ArrayList<>(models.size());
    for (Object model : models) {
      adapters.add(attachAdapter(model));
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
  protected final IJsonAdapter<?> attachGlobalAdapter(Object model) {
    return attachGlobalAdapter(model, null);
  }

  protected final IJsonAdapter<?> attachGlobalAdapter(Object model, IJsonAdapterFactory adapterFactory) {
    if (model == null) {
      return null;
    }
    return m_jsonSession.getOrCreateJsonAdapter(model, getJsonSession().getRootJsonAdapter(), adapterFactory);
  }

  protected final JSONObject putAdapterIdProperty(JSONObject json, String key, Object model) {
    if (model == null) {
      return json;
    }
    return JsonObjectUtility.putProperty(json, key, JsonAdapterUtility.getAdapterIdForModel(getJsonSession(), model, this));
  }

  protected final JSONObject putAdapterIdsProperty(JSONObject json, String key, Collection<?> models) {
    return JsonObjectUtility.putProperty(json, key, JsonAdapterUtility.getAdapterIdsForModel(getJsonSession(), models, this));
  }

  protected final JSONObject putProperty(JSONObject json, String key, Object value) {
    return JsonObjectUtility.putProperty(json, key, value);
  }

  protected final void addActionEvent(String eventName, JSONObject eventData) {
    getJsonSession().currentJsonResponse().addActionEvent(getId(), eventName, eventData);
  }

  /**
   * Like {@link #addActionEvent(String, JSONObject)} but if there are already action events for the same
   * event in the current response, all existing events are removed before adding the new event.
   */
  protected final void replaceActionEvent(String eventName, JSONObject eventData) {
    getJsonSession().currentJsonResponse().replaceActionEvent(getId(), eventName, eventData);
  }

  protected void addPropertyChangeEvent(String propertyName, Object newValue) {
    if (newValue instanceof IJsonAdapter<?>) {
      throw new IllegalArgumentException("Cannot pass an adapter instance to a JSON response");
    }
    getJsonSession().currentJsonResponse().addPropertyChangeEvent(getId(), propertyName, newValue);
  }

  @Override
  public void cleanUpEventFilters() {
  }
}
