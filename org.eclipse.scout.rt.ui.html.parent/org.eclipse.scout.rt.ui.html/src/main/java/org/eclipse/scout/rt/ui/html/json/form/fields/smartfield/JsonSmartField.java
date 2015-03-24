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

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.CachingEnabled;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.IContentAssistField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.IContentAssistFieldUIFacade;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.IProposalChooser;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.IProposalField;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.JsonResponse;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonValueField;
import org.json.JSONObject;

public class JsonSmartField<K, V, T extends IContentAssistField<K, V>> extends JsonValueField<T> {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JsonSmartField.class);
  private static final String PROP_LOOKUP_STRATEGY = "lookupStrategy";
  private static final String PROP_PROPOSAL = "proposal";

  private boolean m_proposal;

  public JsonSmartField(T model, IJsonSession jsonSession, String id, IJsonAdapter<?> parent) {
    super(model, jsonSession, id, parent);
    m_proposal = model instanceof IProposalField;
  }

  protected final boolean isProposal() {
    return m_proposal;
  }

  protected final void setProposal(boolean proposal) {
    m_proposal = proposal;
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonAdapterProperty<IContentAssistField<K, V>>(IContentAssistField.PROP_PROPOSAL_CHOOSER, model, getJsonSession()) {
      @Override
      protected Object modelValue() {
        return getModel().getProposalChooser();
      }
    });
    putJsonProperty(new JsonProperty<IContentAssistField<K, V>>(PROP_LOOKUP_STRATEGY, model) {
      @Override
      protected String modelValue() {
        return getLookupStrategy();
      }
    });
  }

  @Override
  public String getObjectType() {
    if (getModel().isMultilineText()) {
      return "SmartFieldMultiline";
    }
    else {
      return "SmartField";
    }
  }

  // TODO AWE: (smart-field) event 'code-type neu laden' behandeln.
  // browser-seitige felder mit cachingEnabled neu laden evtl. nur wenn der Code-Type passt

  /**
   * Returns whether or not it is allowed to cache all options on the browser-side.
   * When allowed, the client does not send any requests to the server while the
   * smart-field is used until a value is selected.
   */
  protected boolean isCachingEnabled() {
    return getModel().getClass().isAnnotationPresent(CachingEnabled.class);
  }

  protected String getLookupStrategy() {
    return isCachingEnabled() ? "cached" : "remote";
  }

  @Override
  public void handleUiEvent(JsonEvent event, JsonResponse res) {
    if ("openProposal".equals(event.getType())) {
      handleUiOpenProposal(event);
    }
    else if ("closeProposal".equals(event.getType())) {
      handleUiCloseProposal();
    }
    else if ("acceptProposal".equals(event.getType())) {
      handleUiAcceptProposal(event);
    }
    else {
      super.handleUiEvent(event, res);
    }
  }

  private void handleUiAcceptProposal(JsonEvent event) {
    String searchText = event.getData().optString("searchText");
    IProposalChooser proposalChooser = getModel().getProposalChooser();
    LOG.debug("handle acceptProposal -> setTextFromUI. searchText=" + searchText + " proposalChooser=" + (proposalChooser != null));
    if (proposalChooser != null) {
      IContentAssistFieldUIFacade uiFacade = getModel().getUIFacade();
      if (proposalChooser.getAcceptedProposal() != null) {
        uiFacade.acceptProposalFromUI();
      }
      else {
        uiFacade.setTextFromUI(searchText);
        uiFacade.closeProposalFromUI();
      }
    }
  }

  private void handleUiCloseProposal() {
    LOG.debug("handle closeProposal");
    getModel().getUIFacade().closeProposalFromUI();
  }

  private void handleUiOpenProposal(JsonEvent event) {
    String searchText = event.getData().optString("searchText");
    boolean selectCurrentValue = event.getData().optBoolean("selectCurrentValue", false);
    LOG.debug("handle openProposal -> openProposalFromUI. searchText=" + searchText + " selectCurrentValue=" + selectCurrentValue);
    getModel().getUIFacade().openProposalFromUI(searchText, selectCurrentValue);
  }

  @Override
  protected void handleUiDisplayTextChangedImpl(String displayText, boolean whileTyping) {
    LOG.debug("handle displayText changed -> setTextFromUI. displayText=" + displayText + " whileTyping=" + whileTyping);
    getModel().getUIFacade().setTextFromUI(displayText);
  }

  @Override
  public JSONObject toJson() {
    return putProperty(super.toJson(), PROP_PROPOSAL, m_proposal);
  }
}
