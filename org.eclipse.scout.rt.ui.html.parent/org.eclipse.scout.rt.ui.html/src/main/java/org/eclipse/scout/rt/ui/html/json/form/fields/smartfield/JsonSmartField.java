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
package org.eclipse.scout.rt.ui.html.json.form.fields.smartfield;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.IContentAssistField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.IContentAssistFieldProposalForm;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.IContentAssistFieldUIFacade;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.ISmartField;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonException;
import org.eclipse.scout.rt.ui.html.json.JsonResponse;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonValueField;
import org.json.JSONArray;
import org.json.JSONObject;

public class JsonSmartField extends JsonValueField<ISmartField> {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JsonSmartField.class);

  private PropertyChangeListener m_pcl = new PropertyChangeListener() {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if (IContentAssistField.PROP_PROPOSAL_FORM.equals(evt.getPropertyName())) {
        LOG.debug("form opened " + evt.getNewValue());
      }
    }
  };

  public JsonSmartField(ISmartField model, IJsonSession session, String id) {
    super(model, session, id);
    getModel().addPropertyChangeListener(m_pcl);
  }

  @Override
  public String getObjectType() {
    return "SmartField";
  }

  @Override
  public void handleUiEvent(JsonEvent event, JsonResponse res) {
    if ("proposal".equals(event.getType())) {
      // TODO AWE: (smartfield) wir müssen unterscheiden zwischen:
      // 1. proposal form ist noch nicht offen
      // 2. proposal form ist offen und der text ändert sich
      String text = event.getData().optString("text");
      ISmartField model = getModel();
      IContentAssistFieldUIFacade uiFacade = model.getUIFacade();
      IContentAssistFieldProposalForm<?> proposalForm = model.getProposalForm();
      if (proposalForm == null) {
        uiFacade.openProposalFromUI(text, false);
        proposalForm = model.getProposalForm();
      }
      JSONArray searchResults = new JSONArray();
      if (proposalForm.isFormOpen()) {
        List<? extends ILookupRow<?>> searchResultsLr = proposalForm.getSearchResult().getLookupRows();
        for (ILookupRow<?> lr : searchResultsLr) {
          searchResults.put(lr.getText());
        }
        System.out.println("text=" + text + " searchResults=" + searchResults);
      }
      try {
        proposalForm.doClose();
//      uiFacade.acceptProposalFromUI();
      }
      catch (ProcessingException e) {
        throw new JsonException(e);
      }
      // TODO AWE: (json) wir sollten die methode so erweitern, dass man auch ein JSONArray übergeben kann
      // ohne in ein JSONObject zu verpacken. Blöd ist, dass es keine gemeinsame Basis-Klasse dafür gibt.
      JSONObject json = new JSONObject();
      putProperty(json, "searchResults", searchResults);
      res.addActionEvent(getId(), "searchResultsUpdated", json);
    }
    else {
      super.handleUiEvent(event, res);
    }
  }

}
