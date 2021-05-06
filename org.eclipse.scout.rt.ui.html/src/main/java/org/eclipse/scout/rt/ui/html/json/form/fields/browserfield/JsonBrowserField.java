/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.ui.html.json.form.fields.browserfield;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.ui.form.fields.browserfield.BrowserFieldEvent;
import org.eclipse.scout.rt.client.ui.form.fields.browserfield.BrowserFieldListener;
import org.eclipse.scout.rt.client.ui.form.fields.browserfield.IBrowserField;
import org.eclipse.scout.rt.client.ui.form.fields.browserfield.IBrowserField.SandboxPermission;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.IDataObject;
import org.eclipse.scout.rt.dataobject.IDataObjectMapper;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonFormField;
import org.eclipse.scout.rt.ui.html.res.BinaryResourceHolder;
import org.eclipse.scout.rt.ui.html.res.BinaryResourceUrlUtility;
import org.eclipse.scout.rt.ui.html.res.IBinaryResourceProvider;
import org.json.JSONArray;
import org.json.JSONObject;

public class JsonBrowserField<BROWSER_FIELD extends IBrowserField> extends JsonFormField<BROWSER_FIELD> implements IBinaryResourceProvider {

  private BrowserFieldListener m_browserFieldListener;

  public static final String EVENT_POST_MESSAGE = "postMessage";
  public static final String EVENT_EXTERNAL_WINDOW_STATE_CHANGE = "externalWindowStateChange";

  public JsonBrowserField(BROWSER_FIELD model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "BrowserField";
  }

  @Override
  protected void initJsonProperties(BROWSER_FIELD model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<IBrowserField>(IBrowserField.PROP_SCROLL_BAR_ENABLED, model) {
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
      protected Set<SandboxPermission> modelValue() {
        return getModel().getSandboxPermissions();
      }

      @Override
      @SuppressWarnings("unchecked")
      public Object prepareValueForToJson(Object value) {
        Set<SandboxPermission> sandbox = (Set<SandboxPermission>) value;
        if (sandbox == null || sandbox.isEmpty()) {
          return "";
        }
        // Build string for HTML "sandbox" attribute
        StringBuilder sb = new StringBuilder();
        for (SandboxPermission sandboxValue : sandbox) {
          sb.append(sandboxValue.getAttribute()).append(" ");
        }
        sb.deleteCharAt(sb.length() - 1); // delete last space
        return sb.toString();
      }
    });
    putJsonProperty(new JsonProperty<IBrowserField>(IBrowserField.PROP_TRUSTED_MESSAGE_ORIGINS, model) {
      @Override
      protected List<String> modelValue() {
        return getModel().getTrustedMessageOrigins();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        if (value == null) {
          return JSONObject.NULL;
        }
        return new JSONArray((Collection<?>) value); // Do NOT remove the cast! It is required to use the correct constructor.
      }
    });
    putJsonProperty(new JsonProperty<IBrowserField>(IBrowserField.PROP_SHOW_IN_EXTERNAL_WINDOW, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isShowInExternalWindow();
      }
    });
    putJsonProperty(new JsonProperty<IBrowserField>(IBrowserField.PROP_EXTERNAL_WINDOW_BUTTON_TEXT, model) {
      @Override
      protected String modelValue() {
        return getModel().getExternalWindowButtonText();
      }
    });
    putJsonProperty(new JsonProperty<IBrowserField>(IBrowserField.PROP_EXTERNAL_WINDOW_FIELD_TEXT, model) {
      @Override
      protected String modelValue() {
        return getModel().getExternalWindowFieldText();
      }
    });
    putJsonProperty(new JsonProperty<IBrowserField>(IBrowserField.PROP_AUTO_CLOSE_EXTERNAL_WINDOW, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isAutoCloseExternalWindow();
      }
    });
    putJsonProperty(new JsonProperty<IBrowserField>(IBrowserField.PROP_TRACK_LOCATION, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isTrackLocation();
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
  public BinaryResourceHolder provideBinaryResource(String filenameWithFingerprint) {
    BinaryResourceHolder holder = BinaryResourceUrlUtility.provideBinaryResource(filenameWithFingerprint, getModel().getUIFacade()::requestBinaryResourceFromUI);
    holder.addHttpResponseInterceptor(new BrowserFieldContentHttpResponseInterceptor(getUiSession()));
    return holder;
  }

  protected void handleModelContentChanged() {
    addPropertyChangeEvent(IBrowserField.PROP_LOCATION, getLocation());
  }

  protected void handleModelPostMessage(Object message, String targetOrigin) {
    JSONObject eventData = new JSONObject();
    eventData.put("message", messageToJson(message));
    eventData.put("targetOrigin", targetOrigin);
    addActionEvent(EVENT_POST_MESSAGE, eventData);
  }

  protected Object messageToJson(Object message) {
    if (message == null) {
      return JSONObject.NULL;
    }
    if (message instanceof IDataObject) {
      IDataObjectMapper mapper = BEANS.get(IDataObjectMapper.class);
      String str = mapper.writeValue(message);
      if (message instanceof DoList) {
        return new JSONArray(str);
      }
      return new JSONObject(str);
    }
    if (message instanceof String || message instanceof Number || message instanceof Boolean) {
      return message;
    }
    // Unsupported (subclasses may override this method to change that)
    throw new IllegalArgumentException("Unsupported message type: " + message);
  }

  protected void handleModelBrowserFieldEvent(BrowserFieldEvent event) {
    if (BrowserFieldEvent.TYPE_CONTENT_CHANGED == event.getType()) {
      handleModelContentChanged();
    }
    else if (BrowserFieldEvent.TYPE_POST_MESSAGE == event.getType()) {
      handleModelPostMessage(event.getMessage(), event.getTargetOrigin());
    }
  }

  @Override
  public void handleUiEvent(JsonEvent event) {
    if (EVENT_POST_MESSAGE.equals(event.getType())) {
      handleUiPostMessage(event);
    }
    else if (EVENT_EXTERNAL_WINDOW_STATE_CHANGE.equals(event.getType())) {
      handleUiExternalWindowStateChange(event);
    }
    else {
      super.handleUiEvent(event);
    }
  }

  @Override
  protected void handleUiPropertyChange(String propertyName, JSONObject data) {
    if (IBrowserField.PROP_LOCATION.equals(propertyName)) {
      handleUiLocationChange(data);
    }
    else {
      super.handleUiPropertyChange(propertyName, data);
    }
  }

  protected void handleUiLocationChange(JSONObject data) {
    String location = data.optString(IBrowserField.PROP_LOCATION, null);
    addPropertyEventFilterCondition(IBrowserField.PROP_LOCATION, location);
    getModel().getUIFacade().setLocationFromUI(location);
  }

  protected void handleUiPostMessage(JsonEvent event) {
    Object data = event.getData().opt("data");
    String origin = event.getData().optString("origin", null);

    // Support for arbitrary objects (optional support, requires object mapper implementation)
    if (data instanceof JSONObject || data instanceof JSONArray) {
      // Convert "org.json" object to IDataObject
      IDataObjectMapper mapper = BEANS.opt(IDataObjectMapper.class);
      if (mapper != null) {
        data = mapper.readValue(data.toString(), IDataObject.class);
      }
      else {
        data = data.toString();
      }
    }
    getModel().getUIFacade().firePostMessageFromUI(data, origin);
  }

  protected void handleUiExternalWindowStateChange(JsonEvent event) {
    getModel().getUIFacade().firePostExternalWindowStateFromUI(event.getData().optBoolean("windowState"));
  }

  protected String getLocation() {
    String location = getModel().getLocation();
    BinaryResource binaryResource = getModel().getBinaryResource();
    if (location == null && binaryResource != null) {
      location = BinaryResourceUrlUtility.createDynamicAdapterResourceUrl(this, binaryResource);
    }
    return location;
  }

  protected class P_BrowserFieldListener implements BrowserFieldListener {
    @Override
    public void browserFieldChanged(BrowserFieldEvent e) {
      ModelJobs.assertModelThread();
      handleModelBrowserFieldEvent(e);
    }
  }
}
