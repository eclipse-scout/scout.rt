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
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.IProposalField;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonValueField;
import org.json.JSONObject;

public class JsonSmartField<K, V, T extends IContentAssistField<K, V>> extends JsonValueField<T> {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JsonSmartField.class);
  private static final String PROP_LOOKUP_STRATEGY = "lookupStrategy";
  private static final String PROP_PROPOSAL = "proposal";

  private boolean m_proposal;

  public JsonSmartField(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
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
    putJsonProperty(new JsonAdapterProperty<IContentAssistField<K, V>>(IContentAssistField.PROP_PROPOSAL_CHOOSER, model, getUiSession()) {
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

  // FIXME AWE: (smart-field) event 'code-type neu laden' behandeln.
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
  public void handleUiEvent(JsonEvent event) {
    if ("openProposal".equals(event.getType())) {
      handleUiOpenProposal(event);
    }
    else if ("proposalTyped".equals(event.getType())) {
      handleUiProposalTyped(event);
    }
    else if ("cancelProposal".equals(event.getType())) {
      handleUiCancelProposal();
    }
    else if ("acceptProposal".equals(event.getType())) {
      handleUiAcceptProposal(event);
    }
    else {
      super.handleUiEvent(event);
    }
  }

  protected void handleUiProposalTyped(JsonEvent event) {
    String text = event.getData().optString("searchText", null);
    getModel().getUIFacade().proposalTypedFromUI(text);
  }

  protected void handleUiAcceptProposal(JsonEvent event) {
    String text = event.getData().optString("searchText", null);
    getModel().getUIFacade().acceptProposalFromUI(text);
  }

  protected void handleUiCancelProposal() {
    getModel().getUIFacade().cancelProposalChooserFromUI();
  }

  protected void handleUiOpenProposal(JsonEvent event) {
    String searchText = event.getData().optString("searchText", null);
    boolean selectCurrentValue = event.getData().optBoolean("selectCurrentValue");
    LOG.debug("handle openProposal -> openProposalFromUI. searchText=" + searchText + " selectCurrentValue=" + selectCurrentValue);
    getModel().getUIFacade().openProposalChooserFromUI(searchText, selectCurrentValue);
  }

  @Override
  public JSONObject toJson() {
    return putProperty(super.toJson(), PROP_PROPOSAL, m_proposal);
  }
}
