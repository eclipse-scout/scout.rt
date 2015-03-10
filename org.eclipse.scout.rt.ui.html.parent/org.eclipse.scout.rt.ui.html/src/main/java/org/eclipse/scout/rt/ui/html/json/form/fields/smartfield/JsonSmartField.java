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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.CachingEnabled;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.IContentAssistField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.IProposalField;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonException;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.JsonResponse;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonValueField;
import org.json.JSONArray;
import org.json.JSONObject;

public class JsonSmartField<K, V, T extends IContentAssistField<K, V>> extends JsonValueField<T> {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JsonSmartField.class);
  private static final String PROP_LOOKUP_STRATEGY = "lookupStrategy";
  private static final String PROP_OPTIONS = "options";
  private static final String PROP_MULTI_LINE = "multiline";
  private static final int MAX_OPTIONS = 100;
  private static final String PROP_PROPOSAL = "proposal";

  private List<? extends ILookupRow<V>> m_options = new ArrayList<>();

  // FIXME AWE: (proposal) discuss with C.GU - better have class-hierarchy instead of property?
  private boolean m_proposal;

  public JsonSmartField(T model, IJsonSession jsonSession, String id, IJsonAdapter<?> parent) {
    super(model, jsonSession, id, parent);
    m_proposal = model instanceof IProposalField;
  }

  protected final List<? extends ILookupRow<V>> getOptions() {
    return m_options;
  }

  protected final void setOptions(List<? extends ILookupRow<V>> options) {
    m_options = options;
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
    putJsonProperty(new JsonProperty<IContentAssistField<K, V>>(PROP_MULTI_LINE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isMultilineText();
      }
    });
    putJsonProperty(new JsonProperty<IContentAssistField<K, V>>(PROP_LOOKUP_STRATEGY, model) {
      @Override
      protected String modelValue() {
        return getLookupStrategy();
      }
    });
    putJsonProperty(new JsonProperty<IContentAssistField<K, V>>(PROP_OPTIONS, model) {
      @Override
      protected List<? extends ILookupRow<V>> modelValue() {
        return m_options;
      }

      @Override
      @SuppressWarnings("unchecked")
      public Object prepareValueForToJson(Object value) {
        return optionsToJson((List<ILookupRow<V>>) value);
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

  @Override
  protected void attachModel() {
    super.attachModel();
    m_options = isCachingEnabled() ? loadOptions(IContentAssistField.BROWSE_ALL_TEXT) : Collections.<ILookupRow<V>> emptyList();
  }

  protected JSONArray optionsToJson(List<? extends ILookupRow<V>> options) {
    JSONArray optionsArray = new JSONArray();
    for (ILookupRow<V> lr : options) {
      optionsArray.put(lr.getText());
    }
    return optionsArray;
  }

  // TODO AWE: (smartfield) event 'code-type neu laden' behandeln. browser-seitige felder mit cachingEnabled neu laden
  // evtl. nur wenn der Code-Type passt

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

  protected List<? extends ILookupRow<V>> loadOptions(final String query) {
    try {
      // FIXME BSH: This logic is already present in ContentAssistFieldDataFetcher.update() Can we use LookupFetcher? What about async fetching? What about hierarchy?
      if (CompareUtility.isOneOf(query, IContentAssistField.BROWSE_ALL_TEXT, "")) {
        m_options = getModel().callBrowseLookup(query, MAX_OPTIONS);
      }
      else {
        m_options = getModel().callTextLookup(query, MAX_OPTIONS);
      }
      return m_options;
    }
    catch (ProcessingException e) {
      throw new JsonException(e);
    }
  }

  @Override
  public void handleUiEvent(JsonEvent event, JsonResponse res) {
    if ("requestProposal".equals(event.getType())) {
      handleUiRequestProposal(event);
    }
    else if ("closeProposal".equals(event.getType())) {
      handleUiCloseProposal();
    }
    else {
      super.handleUiEvent(event, res);
    }
  }

  private void handleUiCloseProposal() {
    LOG.debug("close proposal");
    getModel().getUIFacade().closeProposalFromUI();
  }

  private void handleUiRequestProposal(JsonEvent event) {
    String searchText = event.getData().optString("searchText");
    boolean browseAll = event.getData().optBoolean("browseAll", false);
    LOG.debug("handle request proposal -> openProposalFromUI. searchText=" + searchText + " browseAll=" + browseAll);
    getModel().getUIFacade().openProposalFromUI(searchText, browseAll);
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
