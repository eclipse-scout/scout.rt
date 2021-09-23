/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.ui.html.json.action;

import java.beans.PropertyChangeEvent;

import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.tree.IActionNode;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonWidget;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.JsonResponse;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterPropertyConfig;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterPropertyConfigBuilder;
import org.eclipse.scout.rt.ui.html.res.BinaryResourceHolder;
import org.eclipse.scout.rt.ui.html.res.BinaryResourceUrlUtility;
import org.eclipse.scout.rt.ui.html.res.IBinaryResourceProvider;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("squid:S00118")
public abstract class JsonAction<ACTION extends IAction> extends AbstractJsonWidget<ACTION> implements IBinaryResourceProvider {

  private static final Logger LOG = LoggerFactory.getLogger(JsonAction.class);

  public static final String EVENT_ACTION = "action";

  public JsonAction(ACTION model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "Action";
  }

  @Override
  protected void initJsonProperties(ACTION model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<ACTION>(IAction.PROP_TEXT, model) {
      @Override
      protected String modelValue() {
        return getModel().getText();
      }
    });

    putJsonProperty(new JsonProperty<ACTION>(IAction.PROP_TEXT_POSITION, model) {
      @Override
      protected String modelValue() {
        return getModel().getTextPosition();
      }
    });

    putJsonProperty(new JsonProperty<ACTION>(IAction.PROP_HTML_ENABLED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isHtmlEnabled();
      }
    });

    putJsonProperty(new JsonProperty<ACTION>(IAction.PROP_TOOLTIP_TEXT, model) {
      @Override
      protected String modelValue() {
        return getModel().getTooltipText();
      }
    });

    putJsonProperty(new JsonProperty<ACTION>("toggleAction", model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isToggleAction();
      }
    });

    putJsonProperty(new JsonProperty<ACTION>(IAction.PROP_SELECTED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isSelected();
      }
    });

    putJsonProperty(new JsonProperty<ACTION>(IAction.PROP_VISIBLE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isVisible();
      }
    });

    putJsonProperty(new JsonProperty<ACTION>(IAction.PROP_KEY_STROKE, model) {
      @Override
      protected String modelValue() {
        return getModel().getKeyStroke();
      }
    });

    putJsonProperty(new JsonProperty<ACTION>(IAction.PROP_KEYSTROKE_FIRE_POLICY, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getKeyStrokeFirePolicy();
      }
    });

    putJsonProperty(new JsonProperty<ACTION>(IAction.PROP_HORIZONTAL_ALIGNMENT, model) {
      @Override
      protected Integer modelValue() {
        return (int) getModel().getHorizontalAlignment();
      }
    });

    putJsonProperty(new JsonProperty<ACTION>(IAction.PROP_ACTION_STYLE, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getActionStyle();
      }
    });

    putJsonProperty(new JsonAdapterProperty<ACTION>(IActionNode.PROP_CHILD_ACTIONS, model, getUiSession()) {
      @Override
      protected JsonAdapterPropertyConfig createConfig() {
        return new JsonAdapterPropertyConfigBuilder().filter(new DisplayableActionFilter<>()).build();
      }

      @Override
      protected Object modelValue() {
        if (getModel() instanceof IActionNode) {
          return ((IActionNode<?>) getModel()).getChildActions();
        }
        return null;
      }
    });
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    json.put(IAction.PROP_ICON_ID, getIconDesc());
    return json;
  }

  @Override
  public void handleUiEvent(JsonEvent event) {
    if (EVENT_ACTION.equals(event.getType())) {
      handleUiAction(event);
    }
    else {
      super.handleUiEvent(event);
    }
  }

  protected void handleUiAction(JsonEvent event) {
    getModel().getUIFacade().fireActionFromUI();
  }

  @Override
  protected void handleUiPropertyChange(String propertyName, JSONObject data) {
    if (IAction.PROP_SELECTED.equals(propertyName)) {
      boolean selected = data.getBoolean(propertyName);
      addPropertyEventFilterCondition(propertyName, selected);
      getModel().getUIFacade().setSelectedFromUI(selected);
    }
    else {
      super.handleUiPropertyChange(propertyName, data);
    }
  }

  @Override
  protected void handleModelPropertyChange(PropertyChangeEvent event) {
    // If a menu is set to visibleGranted=false, a PROP_VISIBLE property change event is fired. In most cases,
    // the JsonAdapter is not yet attached, so this event will not be received here. The adapter will not be
    // attached because of the DisplayableFormFieldFilter. There are however rare cases, where the adapter
    // is already attached when visibleGranted is set to false. If the adapter is not yet sent to the UI,
    // we still have the chance to dispose the adapter and pretend it was never attached in the first place.
    // [Similar code exist in JsonFormField]
    if (IAction.PROP_VISIBLE.equals(event.getPropertyName()) && !getModel().isVisibleGranted()) {
      JsonResponse response = getUiSession().currentJsonResponse();
      if (response.containsAdapter(this) && response.isWritable()) {
        dispose();
        return;
      }
      LOG.warn("Setting visibleGranted=false has no effect, because JsonAdapter {} ({}) is already sent to the UI.", getId(), getModel());
    }
    super.handleModelPropertyChange(event);
  }

  @Override
  protected void handleModelPropertyChange(String propertyName, Object oldValue, Object newValue) {
    if (ObjectUtility.isOneOf(propertyName,
        IAction.PROP_ICON_ID,
        IAction.PROP_IMAGE)) {
      handleModelIIconSourceChanged();
    }
    else {
      super.handleModelPropertyChange(propertyName, oldValue, newValue);
    }
  }

  protected void handleModelIIconSourceChanged() {
    addPropertyChangeEvent(IAction.PROP_ICON_ID, getIconDesc());
  }

  /**
   * Returns an URL for the image or iconId, respectively (first one that is not <code>null</code>). If no image is set,
   * <code>null</code> is returned.
   */
  protected String getIconDesc() {
    if (getModel().getImage() != null) {
      // We don't send the image via JSON to the client, we only set a flag that this adapter has an image
      // The client will request the image in a separate http request. See: ResourceRequestHandler
      BinaryResource imageResource = BinaryResourceUrlUtility.extractBinaryResource(getModel().getImage(), "image", "jpg");
      if (imageResource != null && imageResource.getContent() != null) {
        return BinaryResourceUrlUtility.createDynamicAdapterResourceUrl(this, imageResource);
      }
    }
    return BinaryResourceUrlUtility.createIconUrl(getModel().getIconId());
  }

  @Override
  public BinaryResourceHolder provideBinaryResource(String requestFilename) {
    BinaryResource image = BinaryResourceUrlUtility.extractBinaryResource(getModel().getImage(), "image", "jpg");
    if (image == null || image.getContent() == null) {
      return null;
    }

    String imageFilenameWithFingerprint = BinaryResourceUrlUtility.getFilenameWithFingerprint(image);
    if (imageFilenameWithFingerprint.equals(requestFilename)) {
      return new BinaryResourceHolder(image);
    }

    return null;
  }
}
