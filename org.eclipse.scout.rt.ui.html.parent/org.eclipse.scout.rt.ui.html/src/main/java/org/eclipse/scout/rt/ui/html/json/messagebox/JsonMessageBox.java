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
package org.eclipse.scout.rt.ui.html.json.messagebox;

import org.eclipse.scout.rt.client.ui.messagebox.IMessageBox;
import org.eclipse.scout.rt.client.ui.messagebox.MessageBoxEvent;
import org.eclipse.scout.rt.client.ui.messagebox.MessageBoxListener;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonPropertyObserver;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonObjectUtility;
import org.eclipse.scout.rt.ui.html.json.JsonResponse;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonProperty;
import org.json.JSONObject;

public class JsonMessageBox extends AbstractJsonPropertyObserver<IMessageBox> {
  public String EVENT_ACTION = "action";
  public String EVENT_CLOSED = "closed";

  private MessageBoxListener m_messageBoxListener;

  public JsonMessageBox(IMessageBox model, IJsonSession jsonSession, String id) {
    super(model, jsonSession, id);

    putJsonProperty(new JsonProperty<IMessageBox>("title", model) {
      @Override
      protected String modelValue() {
        return getModel().getTitle();
      }
    });
    putJsonProperty(new JsonProperty<IMessageBox>("iconId", model) {
      @Override
      protected String modelValue() {
        return getModel().getIconId();
      }
    });
//    putJsonProperty(new JsonProperty<IMessageBox>("severity", model) {
//      @Override
//      protected String modelValue() {
//        return getModel().getIconId();//FIXME implement
//      }
//    });
    putJsonProperty(new JsonProperty<IMessageBox>("introText", model) {
      @Override
      protected String modelValue() {
        return getModel().getIntroText();
      }
    });
    putJsonProperty(new JsonProperty<IMessageBox>("actionText", model) {
      @Override
      protected String modelValue() {
        return getModel().getActionText();
      }
    });
    putJsonProperty(new JsonProperty<IMessageBox>("yesButtonText", model) {
      @Override
      protected String modelValue() {
        return getModel().getYesButtonText();
      }
    });
    putJsonProperty(new JsonProperty<IMessageBox>("noButtonText", model) {
      @Override
      protected String modelValue() {
        return getModel().getNoButtonText();
      }
    });
    putJsonProperty(new JsonProperty<IMessageBox>("cancelButtonText", model) {
      @Override
      protected String modelValue() {
        return getModel().getCancelButtonText();
      }
    });
//  putJsonProperty(new JsonProperty<IMessageBox>("hiddenText", model) {
//  @Override
//  protected String modelValue() {
//    return getModel().getHiddenText(); //FIXME implement
//  }
//});

  }

  @Override
  public String getObjectType() {
    return "MessageBox";
  }

  @Override
  protected void attachModel() {
    super.attachModel();
    if (m_messageBoxListener == null) {
      m_messageBoxListener = new P_MessageBoxListener();
      getModel().addMessageBoxListener(m_messageBoxListener);
    }
  }

  @Override
  protected void detachModel() {
    super.detachModel();
    if (m_messageBoxListener != null) {
      getModel().removeMessageBoxListener(m_messageBoxListener);
      m_messageBoxListener = null;
    }
  }

  protected void handleModelClosed() {
    dispose();
    addActionEvent(EVENT_CLOSED, new JSONObject());
  }

  protected void handleModelMessageBoxEvent(MessageBoxEvent event) {
    switch (event.getType()) {
      case MessageBoxEvent.TYPE_CLOSED:
        handleModelClosed();
        break;
    }
  }

  @Override
  public void handleUiEvent(JsonEvent event, JsonResponse res) {
    if (EVENT_ACTION.equals(event.getType())) {
      handleUiAction(event);
    }
    else {
      super.handleUiEvent(event, res);
    }
  }

  private void handleUiAction(JsonEvent event) {
    String option = JsonObjectUtility.getString(event.getData(), "option");
    int resultOption = -1;
    if ("yes".equals(option)) {
      resultOption = IMessageBox.YES_OPTION;
    }
    else if ("no".equals(option)) {
      resultOption = IMessageBox.NO_OPTION;
    }
    else if ("cancel".equals(option)) {
      resultOption = IMessageBox.CANCEL_OPTION;
    }
    if (resultOption == -1) {
      throw new IllegalStateException("Undefined option" + option);
    }

    getModel().getUIFacade().setResultFromUI(resultOption);
  }

  private class P_MessageBoxListener implements MessageBoxListener {
    @Override
    public void messageBoxChanged(MessageBoxEvent event) {
      handleModelMessageBoxEvent(event);
    }
  }
}
