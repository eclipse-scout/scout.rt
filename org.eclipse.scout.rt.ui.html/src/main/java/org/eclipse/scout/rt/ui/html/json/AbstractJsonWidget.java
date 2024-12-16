/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json;

import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.ScrollOptions;
import org.eclipse.scout.rt.client.ui.WidgetEvent;
import org.eclipse.scout.rt.client.ui.WidgetListener;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.json.JSONObject;

/**
 * @since 8.0
 */
public abstract class AbstractJsonWidget<T extends IWidget> extends AbstractJsonPropertyObserver<T> {

  protected static final String EVENT_SCROLL_TO_TOP = "scrollToTop";
  protected static final String EVENT_REVEAL = "reveal";

  private WidgetListener m_listener;

  public AbstractJsonWidget(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "Widget";
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<>(IWidget.PROP_CSS_CLASS, model) {
      @Override
      protected String modelValue() {
        return getModel().getCssClass();
      }
    });
    putJsonProperty(new JsonProperty<>(IWidget.PROP_ENABLED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isEnabled();
      }
    });
    putJsonProperty(new JsonProperty<>(IWidget.PROP_INHERIT_ACCESSIBILITY, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isInheritAccessibility();
      }
    });
    putJsonProperty(new JsonProperty<>(IWidget.PROP_LOADING, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isLoading();
      }
    });
  }

  @Override
  protected void attachModel() {
    super.attachModel();
    if (m_listener != null) {
      throw new IllegalStateException();
    }
    m_listener = new P_WidgetListener();
    getModel().addWidgetListener(m_listener);
  }

  @Override
  protected void detachModel() {
    super.detachModel();
    if (m_listener == null) {
      throw new IllegalStateException();
    }
    getModel().removeWidgetListener(m_listener);
    m_listener = null;
  }

  protected void handleModelWidgetEvent(WidgetEvent event) {
    if (event.getType() == WidgetEvent.TYPE_SCROLL_TO_TOP) {
      handleModelScrollTopTop(event.getScrollOptions());
    }
    else if (event.getType() == WidgetEvent.TYPE_REVEAL) {
      handleModelReveal(event.getScrollOptions());
    }
  }

  protected void handleModelScrollTopTop(ScrollOptions options) {
    addActionEvent(EVENT_SCROLL_TO_TOP, scrollOptionsToJson(options));
  }

  protected void handleModelReveal(ScrollOptions options) {
    addActionEvent(EVENT_REVEAL, scrollOptionsToJson(options));
  }

  protected JSONObject scrollOptionsToJson(ScrollOptions options) {
    if (options == null) {
      return null;
    }
    return (JSONObject) MainJsonObjectFactory.get().createJsonObject(options).toJson();
  }

  protected class P_WidgetListener implements WidgetListener {

    @Override
    public void widgetChanged(WidgetEvent e) {
      ModelJobs.assertModelThread();
      handleModelWidgetEvent(e);
    }
  }
}
