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
package org.eclipse.scout.rt.ui.html.json.desktop;

import static org.eclipse.scout.rt.ui.html.json.JsonObjectUtility.getString;

import java.util.Collection;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.mobile.navigation.BreadCrumbsEvent;
import org.eclipse.scout.rt.client.mobile.navigation.BreadCrumbsListener;
import org.eclipse.scout.rt.client.mobile.navigation.IBreadCrumb;
import org.eclipse.scout.rt.client.mobile.navigation.IBreadCrumbsNavigation;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.IJsonMapper;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonException;
import org.eclipse.scout.rt.ui.html.json.JsonResponse;
import org.eclipse.scout.rt.ui.html.json.form.JsonForm;
import org.json.JSONArray;
import org.json.JSONObject;

public class JsonBreadCrumbNavigation extends AbstractJsonAdapter<IBreadCrumbsNavigation> {
  public static final String EVENT_ACTIVATE = "activate";
  public static final String EVENT_CHANGED = "changed";
  public static final String PROP_BREAD_CRUMBS = "breadcrumbs";
  public static final String PROP_CURRENT_FORM_ID = "currentFormId";

  private BreadCrumbsListener m_breadCrumbsListener;

  public JsonBreadCrumbNavigation(IBreadCrumbsNavigation modelObject, IJsonSession jsonSession, String id) {
    super(modelObject, jsonSession, id);
  }

  @Override
  public String getObjectType() {
    return "BreadCrumbNavigation";
  }

  @Override
  protected void attachModel() {
    super.attachModel();
    if (m_breadCrumbsListener == null) {
      m_breadCrumbsListener = new P_BreadCrumbsListener();
      getModelObject().addBreadCrumbsListener(m_breadCrumbsListener);
    }
  }

  @Override
  protected void detachModel() {
    super.detachModel();
    if (m_breadCrumbsListener != null) {
      getModelObject().removeBreadCrumbsListener(m_breadCrumbsListener);
      m_breadCrumbsListener = null;
    }
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    putProperty(json, PROP_BREAD_CRUMBS, breadCrumbsToJson(getModelObject().getBreadCrumbs()));
    putProperty(json, PROP_CURRENT_FORM_ID, getCurrentJsonForm().getId());
    return json;
  }

  private JsonForm getCurrentJsonForm() {
    return getJsonForm(getModelObject().getCurrentNavigationForm());
  }

  private JsonForm getJsonForm(IForm form) {
    return (JsonForm) getJsonSession().getJsonAdapter(form);
  }

  protected JSONArray breadCrumbsToJson(Collection<IBreadCrumb> breadCrumbs) {
    JSONArray array = new JSONArray();
    for (IBreadCrumb breadCrumb : breadCrumbs) {
      array.put(new JsonBreadCrumb(breadCrumb).toJson());
    }
    return array;
  }

  @Override
  public void handleUiEvent(JsonEvent event, JsonResponse res) {
    if (EVENT_ACTIVATE.equals(event.getType())) {
      handleUiActivate(event, res);
    }
    else {
      throw new IllegalArgumentException("unsupported event type");
    }
  }

  public void handleUiActivate(JsonEvent event, JsonResponse res) {
    final String formId = getString(event.getJsonObject(), JsonForm.PROP_FORM_ID);
    JsonForm jsonForm = getCurrentJsonForm();
    while (!jsonForm.getId().equals(formId) || getModelObject().getCurrentNavigationForm() == null) {
      try {
        getModelObject().stepBack();
      }
      catch (ProcessingException e) {
        throw new JsonException(e);
      }
      jsonForm = getCurrentJsonForm();
    }
  }

  protected class P_BreadCrumbsListener implements BreadCrumbsListener {

    @Override
    public void breadCrumbsChanged(BreadCrumbsEvent e) {
      //FIXME CGU improve listener to notify single removals /addings and not always all crumbs
      getJsonSession().currentJsonResponse().addActionEvent(EVENT_CHANGED, getId(), toJson());
    }

  }

  //FIXME verify with awereally necessary to hold a state? Not extendable
  private class JsonBreadCrumb implements IJsonMapper {
    private IBreadCrumb m_breadCrumb;

    public JsonBreadCrumb(IBreadCrumb breadCrumb) {
      m_breadCrumb = breadCrumb;
    }

    @Override
    public JSONObject toJson() {
      JSONObject json = new JSONObject();
      putProperty(json, JsonForm.PROP_FORM_ID, getJsonForm(m_breadCrumb.getForm()).getId());
      return json;
    }

  }
}
