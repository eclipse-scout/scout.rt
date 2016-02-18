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
package org.eclipse.scout.rt.ui.html.json.form.fields.smartfield;

import org.eclipse.scout.rt.client.ui.form.fields.smartfield.IProposalChooser;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.util.TriState;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonPropertyObserver;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.JsonStatus;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;
import org.json.JSONArray;
import org.json.JSONObject;

public class JsonProposalChooser<PROPOSAL_CHOOSER extends IProposalChooser> extends AbstractJsonPropertyObserver<PROPOSAL_CHOOSER> {

  public JsonProposalChooser(PROPOSAL_CHOOSER model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "ProposalChooser";
  }

  @Override
  protected void initJsonProperties(PROPOSAL_CHOOSER model) {
    super.initJsonProperties(model);
    // We don't support that a smart-field could change the activeStateFilterEnabled property at runtime
    // Because very few smart-fields do have such a filter we'd rather not send the additional JSON data
    // used to render the filter in the UI for each smart-field.
    putJsonProperty(new JsonAdapterProperty<IProposalChooser>("model", model, getUiSession()) {
      @Override
      protected Object modelValue() {
        return getModel().getModel();
      }
    });
    putJsonProperty(new JsonProperty<IProposalChooser>(IProposalChooser.PROP_STATUS, model) {
      @Override
      protected IStatus modelValue() {
        return getModel().getStatus();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return JsonStatus.toJson((IStatus) value);
      }
    });
    putJsonProperty(new JsonProperty<IProposalChooser>(IProposalChooser.PROP_STATUS_VISIBLE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isStatusVisible();
      }
    });
  }

  @Override
  protected void attachChildAdapters() {
    super.attachChildAdapters();
    attachAdapter(getModel().getModel());
  }

  @Override
  public void handleUiEvent(JsonEvent event) {
    if ("activeFilterChanged".equals(event.getType())) {
      handleActiveFilterChanged(event);
    }
    else {
      super.handleUiEvent(event);
    }
  }

  private void handleActiveFilterChanged(JsonEvent event) {
    String state = event.getData().optString("state", null);
    getModel().updateActiveFilter(TriState.valueOf(state));
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    if (getModel().isActiveFilterEnabled()) {
      putProperty(json, "activeFilter", getModel().getActiveFilter().name());
      putProperty(json, "activeFilterLabels", new JSONArray(getModel().getActiveFilterLabels()));
    }
    return json;
  }

}
