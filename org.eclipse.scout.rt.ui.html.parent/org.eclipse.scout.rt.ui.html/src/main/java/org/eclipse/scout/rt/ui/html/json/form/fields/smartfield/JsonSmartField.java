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

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.CachingEnabled;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.IContentAssistField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.ISmartField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.Multiline;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonException;
import org.eclipse.scout.rt.ui.html.json.JsonResponse;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonValueField;
import org.json.JSONArray;
import org.json.JSONObject;

public class JsonSmartField extends JsonValueField<ISmartField> {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JsonSmartField.class);

  private static final String PROP_CACHING_ENABLED = "cachingEnabled";

  private static final String PROP_OPTIONS = "options";

  private static final String PROP_MULTI_LINE = "multiline";

  private static final int MAX_OPTIONS = 100;

  private List<? extends ILookupRow<?>> m_options = new ArrayList<>();

  public JsonSmartField(ISmartField model, IJsonSession session, String id) {
    super(model, session, id);

  }

  @Override
  protected void initJsonProperties(ISmartField model) {
    super.initJsonProperties(model);
    // TODO AWE: (smartfield) prüfen ob wir die properties brauchen oder
    // ob wir's über den objectType lösen wollen
    putJsonProperty(new JsonProperty<ISmartField<?>>(PROP_MULTI_LINE, model) {
      @Override
      protected Boolean modelValue() {
        return isMultiline();
      }
    });
    putJsonProperty(new JsonProperty<ISmartField<?>>(PROP_CACHING_ENABLED, model) {
      @Override
      protected Boolean modelValue() {
        return isCachingEnabled();
      }
    });
    putJsonProperty(new JsonProperty<ISmartField<?>>(PROP_OPTIONS, model) {
      @Override
      protected List<? extends ILookupRow<?>> modelValue() {
        return m_options;
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return optionsToJson((List<? extends ILookupRow<?>>) value);
      }
    });
  }

  @Override
  public String getObjectType() {
    if (isMultiline()) {
      return "SmartFieldMultiline";
    }
    else if (isCachingEnabled()) {
      return "SmartField";
    }
    else {
      return "SmartFieldRemote";
    }
  }

  @Override
  protected void attachModel() {
    super.attachModel();
    m_options = isCachingEnabled() ?
        loadOptions(IContentAssistField.BROWSE_ALL_TEXT) :
          Collections.<ILookupRow<?>> emptyList();
  }

  private JSONArray optionsToJson(List<? extends ILookupRow<?>> options) {
    JSONArray optionsArray = new JSONArray();
    for (ILookupRow<?> lr : options) {
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
  private boolean isCachingEnabled() {
    return getModel().getClass().isAnnotationPresent(CachingEnabled.class);
  }

  private boolean isMultiline() {
    return getModel().getClass().isAnnotationPresent(Multiline.class);
  }

  private JSONObject addOptions(JSONObject json) {
    // TODO AWE: (smartfield) überlegen ob wir die optionen wirklich hier laden wollen oder besser erst,
    // wenn das popup im UI zum ersten mal geöffnet wird.
    return putProperty(json, "options", loadOptions(IContentAssistField.BROWSE_ALL_TEXT));
  }

  private List<? extends ILookupRow<?>> loadOptions(final String query) {
    try {
      m_options = getModel().callBrowseLookup(query, MAX_OPTIONS);
      return m_options;
    }
    catch (ProcessingException e) {
      throw new JsonException(e);
    }
  }

  @Override
  public void handleUiEvent(JsonEvent event, JsonResponse res) {
    if ("loadOptions".equals(event.getType())) {
      handleLoadOptions(event);
    }
    else {
      super.handleUiEvent(event, res);
    }
  }

  private void handleLoadOptions(JsonEvent event) {
    String query = event.getData().optString("query");
    LOG.debug("load options for query=" + query);
    JSONArray options = optionsToJson(loadOptions(query));
    addActionEvent("optionsLoaded", putProperty(new JSONObject(), "options", options));
  }

  @Override
  protected void handleUiDisplayTextChangedImpl(String displayText, boolean whileTyping) {
    if (StringUtility.isNullOrEmpty(displayText)) {
      getModel().setValue(null);
    }
    else {
      for (ILookupRow<?> lr : m_options) {
        if (displayText.equals(lr.getText())) {
          getModel().setValue(lr.getKey());
          break;
        }
      }
    }
  }
}
