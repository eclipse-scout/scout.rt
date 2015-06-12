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
package org.eclipse.scout.rt.ui.html.json.form.fields.browserfield;

import java.util.Set;

import org.eclipse.scout.rt.client.ui.form.fields.browserfield.IBrowserField;
import org.eclipse.scout.rt.client.ui.form.fields.browserfield.IBrowserField.SandboxValues;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonValueField;

public class JsonBrowserField<T extends IBrowserField> extends JsonValueField<T> {

  public JsonBrowserField(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "BrowserField";
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<IBrowserField>(IBrowserField.PROP_LOCATION, model) {
      @Override
      protected String modelValue() {
        return getModel().getLocation();
      }
    });
    putJsonProperty(new JsonProperty<IBrowserField>(IBrowserField.PROP_SCROLLBARS_ENABLED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isScrollBarEnabled();
      }
    });
    putJsonProperty(new JsonProperty<IBrowserField>(IBrowserField.PROP_SANDBOX, model) {
      @Override
      protected Set<SandboxValues> modelValue() {
        return getModel().getSandbox();
      }

      @Override
      @SuppressWarnings("unchecked")
      public Object prepareValueForToJson(Object value) {
        Set<SandboxValues> sandbox = (Set<SandboxValues>) value;
        if (sandbox.isEmpty()) {
          return "";
        }
        else if (SandboxValues.hasAllRestrictions(sandbox)) {
          // not a valid attribute-value for the HTML sandbox attribute
          // must be processed by the UI into 'attribute only, without a value'.
          return "deny-all";
        }
        else {
          StringBuilder sb = new StringBuilder();
          for (SandboxValues sandboxValue : sandbox) {
            sb.append(sandboxValue.getAttribute() + " ");
          }
          sb.deleteCharAt(sb.length() - 1); // delete last space
          return sb.toString();
        }
      }
    });

  }
}
