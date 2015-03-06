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
package org.eclipse.scout.rt.ui.html.json.form.fields.splitbox;

import java.util.List;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.splitbox.ISplitBox;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonFormField;
import org.json.JSONObject;

public class JsonSplitBox<T extends ISplitBox> extends JsonFormField<T> {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JsonSplitBox.class);

  private final IFormField m_firstField;
  private final IFormField m_secondField;

  public JsonSplitBox(T model, IJsonSession jsonSession, String id, IJsonAdapter<?> parent) {
    super(model, jsonSession, id, parent);
    List<IFormField> fields = model.getFields();
    IFormField firstField = null;
    IFormField secondField = null;
    if (fields.size() > 0) {
      firstField = fields.get(0);
      if (fields.size() > 1) {
        secondField = fields.get(1);
        if (fields.size() > 2) {
          LOG.warn("Split box only supports two fields. " + (fields.size() - 2) + " surplus fields are ignored in " + model + ".");
        }
      }
    }
    m_firstField = firstField;
    m_secondField = secondField;
  }

  @Override
  public String getObjectType() {
    return "SplitBox";
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);

    putJsonProperty(new JsonProperty<ISplitBox>(ISplitBox.PROP_SPLITTER_POSITION, model) {
      @Override
      protected Double modelValue() {
        return getModel().getSplitterPosition();
      }
    });
  }

  @Override
  protected void attachChildAdapters() {
    super.attachChildAdapters();
    attachAdapter(m_firstField);
    attachAdapter(m_secondField);
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    putProperty(json, "splitHorizontal", getModel().isSplitHorizontal());
    putAdapterIdProperty(json, "firstField", m_firstField);
    putAdapterIdProperty(json, "secondField", m_secondField);
    return json;
  }

  protected IFormField getFirstField() {
    return m_firstField;
  }

  protected IFormField getSecondField() {
    return m_secondField;
  }
}
