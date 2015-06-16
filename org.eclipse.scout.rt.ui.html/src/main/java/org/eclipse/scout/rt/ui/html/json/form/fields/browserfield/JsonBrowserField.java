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

import org.eclipse.scout.rt.client.ui.form.fields.browserfield.BrowserFieldEvent;
import org.eclipse.scout.rt.client.ui.form.fields.browserfield.BrowserFieldListener;
import org.eclipse.scout.rt.client.ui.form.fields.browserfield.IBrowserField;
import org.eclipse.scout.rt.client.ui.form.fields.browserfield.IBrowserField.SandboxPermissions;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonFormField;
import org.eclipse.scout.rt.ui.html.res.BinaryResourceHolder;
import org.eclipse.scout.rt.ui.html.res.BinaryResourceUrlUtility;
import org.eclipse.scout.rt.ui.html.res.IBinaryResourceProvider;
import org.json.JSONObject;

public class JsonBrowserField<T extends IBrowserField> extends JsonFormField<T> implements IBinaryResourceProvider {

  private BrowserFieldListener m_browserFieldListener;

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
    putJsonProperty(new JsonProperty<IBrowserField>(IBrowserField.PROP_SCROLLBARS_ENABLED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isScrollBarEnabled();
      }
    });
    putJsonProperty(new JsonProperty<IBrowserField>(IBrowserField.PROP_SANDBOX_ENABLED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isSandboxEnabled();
      }
    });
    putJsonProperty(new JsonProperty<IBrowserField>(IBrowserField.PROP_SANDBOX_PERMISSIONS, model) {
      @Override
      protected Set<SandboxPermissions> modelValue() {
        return getModel().getSandboxPermissions();
      }

      @Override
      @SuppressWarnings("unchecked")
      public Object prepareValueForToJson(Object value) {
        Set<SandboxPermissions> sandbox = (Set<SandboxPermissions>) value;
        if (sandbox == null || sandbox.isEmpty()) {
          return "";
        }
        // Build string for HTML "sandbox" attribute
        StringBuilder sb = new StringBuilder();
        for (SandboxPermissions sandboxValue : sandbox) {
          sb.append(sandboxValue.getAttribute() + " ");
        }
        sb.deleteCharAt(sb.length() - 1); // delete last space
        return sb.toString();
      }
    });
  }

  @Override
  protected void attachModel() {
    super.attachModel();
    if (m_browserFieldListener != null) {
      throw new IllegalStateException();
    }
    m_browserFieldListener = new P_BrowserFieldListener();
    getModel().addBrowserFieldListener(m_browserFieldListener);
  }

  @Override
  protected void detachModel() {
    super.detachModel();
    if (m_browserFieldListener == null) {
      throw new IllegalStateException();
    }
    getModel().removeBrowserFieldListener(m_browserFieldListener);
    m_browserFieldListener = null;
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    putProperty(json, IBrowserField.PROP_LOCATION, getLocation());
    return json;
  }

  @Override
  public BinaryResourceHolder provideBinaryResource(String filename) {
    // TODO BSH UIFacade required?
    return new BinaryResourceHolder(getModel().getUIFacade().requestBinaryResourceFromUI(filename));
  }

  protected void handleModelContentChanged() {
    addPropertyChangeEvent(IBrowserField.PROP_LOCATION, getLocation());
  }

  protected String getLocation() {
    String location = getModel().getLocation();
    if (location == null && getModel().getBinaryResource() != null) {
      location = BinaryResourceUrlUtility.createDynamicAdapterResourceUrl(this, getModel().getBinaryResource().getFilename());
    }
    return location;
  }

  protected class P_BrowserFieldListener implements BrowserFieldListener {

    @Override
    public void browserFieldChanged(BrowserFieldEvent e) {
      handleModelContentChanged();
    }
  }
}
