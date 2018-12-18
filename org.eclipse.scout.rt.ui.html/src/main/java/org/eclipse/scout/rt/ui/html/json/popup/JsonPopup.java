/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.popup;

import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.popup.IPopup;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonWidget;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonAdapterRefProperty;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;

/**
 * @since 9.0
 */
public class JsonPopup<T extends IPopup> extends AbstractJsonWidget<T> {

  // UI events
  private static final String EVENT_CLOSE = "close";

  public JsonPopup(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "Popup";
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonAdapterRefProperty<T>(IPopup.PROP_ANCHOR, model, getUiSession().getRootJsonAdapter()) {
      @Override
      protected IWidget modelValue() {
        return getModel().getAnchor();
      }
    });
    putJsonProperty(new JsonProperty<T>(IPopup.PROP_ANIMATE_OPENING, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isAnimateOpening();
      }
    });
    putJsonProperty(new JsonProperty<T>(IPopup.PROP_WITH_GLASS_PANE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isWithGlassPane();
      }
    });
    putJsonProperty(new JsonProperty<T>(IPopup.PROP_SCROLL_TYPE, model) {
      @Override
      protected String modelValue() {
        return getModel().getScrollType();
      }
    });
    putJsonProperty(new JsonProperty<T>(IPopup.PROP_TRIM_WIDTH, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isTrimWidth();
      }
    });
    putJsonProperty(new JsonProperty<T>(IPopup.PROP_TRIM_HEIGHT, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isTrimHeight();
      }
    });
    putJsonProperty(new JsonProperty<T>(IPopup.PROP_HORIZONTAL_ALIGNMENT, model) {
      @Override
      protected String modelValue() {
        return getModel().getHorizontalAlignment();
      }
    });
    putJsonProperty(new JsonProperty<T>(IPopup.PROP_VERTICAL_ALIGNMENT, model) {
      @Override
      protected String modelValue() {
        return getModel().getVerticalAlignment();
      }
    });
    putJsonProperty(new JsonProperty<T>(IPopup.PROP_WITH_ARROW, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isWithArrow();
      }
    });
    putJsonProperty(new JsonProperty<T>(IPopup.PROP_CLOSE_ON_ANCHOR_MOUSE_DOWN, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isCloseOnAnchorMouseDown();
      }
    });
    putJsonProperty(new JsonProperty<T>(IPopup.PROP_CLOSE_ON_MOUSE_DOWN_OUTSIDE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isCloseOnMouseDownOutside();
      }
    });
    putJsonProperty(new JsonProperty<T>(IPopup.PROP_CLOSE_ON_OTHER_POPUP_OPEN, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isCloseOnOtherPopupOpen();
      }
    });
    putJsonProperty(new JsonProperty<T>(IPopup.PROP_HORIZONTAL_SWITCH, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isHorizontalSwitch();
      }
    });
    putJsonProperty(new JsonProperty<T>(IPopup.PROP_VERTICAL_SWITCH, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isVerticalSwitch();
      }
    });
  }

  @Override
  public void handleUiEvent(JsonEvent event) {
    if (EVENT_CLOSE.equals(event.getType())) {
      handleUiClose(event);
    }
    else {
      super.handleUiEvent(event);
    }
  }

  protected void handleUiClose(JsonEvent event) {
    getModel().getUIFacade().firePopupClosingFromUI();
  }
}
