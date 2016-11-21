/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.messagebox;

import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.ui.messagebox.IMessageBox;
import org.eclipse.scout.rt.client.ui.messagebox.MessageBoxEvent;
import org.eclipse.scout.rt.client.ui.messagebox.MessageBoxListener;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonPropertyObserver;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.res.BinaryResourceUrlUtility;

public class JsonMessageBox<MESSAGE_BOX extends IMessageBox> extends AbstractJsonPropertyObserver<MESSAGE_BOX> {

  public static final String EVENT_ACTION = "action";

  private MessageBoxListener m_messageBoxListener;

  public JsonMessageBox(MESSAGE_BOX model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "MessageBox";
  }

  @Override
  protected void initJsonProperties(MESSAGE_BOX model) {
    super.initJsonProperties(model);

    putJsonProperty(new JsonProperty<IMessageBox>("iconId", model) {
      @Override
      protected String modelValue() {
        return getModel().getIconId();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return BinaryResourceUrlUtility.createIconUrl((String) value);
      }
    });
    putJsonProperty(new JsonProperty<IMessageBox>("severity", model) {
      @Override
      protected Integer modelValue() {
        return getModel().getSeverity();
      }
    });
    putJsonProperty(new JsonProperty<IMessageBox>("header", model) {
      @Override
      protected String modelValue() {
        return getModel().getHeader();
      }
    });
    putJsonProperty(new JsonProperty<IMessageBox>("body", model) {
      @Override
      protected String modelValue() {
        return getModel().getBody();
      }
    });
    putJsonProperty(new JsonProperty<IMessageBox>("html", model) {
      @Override
      protected String modelValue() {
        return getModel().getHtml() == null ? null : getModel().getHtml().toHtml();
      }
    });
    putJsonProperty(new JsonProperty<IMessageBox>("hiddenText", model) {
      @Override
      protected String modelValue() {
        return getModel().getHiddenText();
      }
    });
    putJsonProperty(new JsonProperty<IMessageBox>("copyPasteText", model) {
      @Override
      protected String modelValue() {
        return getModel().getCopyPasteText();
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
  }

  @Override
  protected void attachModel() {
    super.attachModel();
    if (m_messageBoxListener != null) {
      throw new IllegalStateException();
    }
    m_messageBoxListener = new P_MessageBoxListener();
    getModel().addMessageBoxListener(m_messageBoxListener);
  }

  @Override
  protected void detachModel() {
    super.detachModel();
    if (m_messageBoxListener == null) {
      throw new IllegalStateException();
    }
    getModel().removeMessageBoxListener(m_messageBoxListener);
    m_messageBoxListener = null;
  }

  protected void handleModelMessageBoxChanged(MessageBoxEvent event) {
    switch (event.getType()) {
      case MessageBoxEvent.TYPE_CLOSED:
        handleModelClosed();
        break;
      default:
        // NOP
    }
  }

  protected void handleModelClosed() {
    // ==> Same code in JsonForm, JsonFileChooser! <==

    // This removes the adapter from the adapter registry and the current JSON response.
    // Also, all events for this adapter in the current response are removed.
    dispose();

    // JSON adapter is now disposed. To dispose it on the UI, too, we have to send an explicit
    // event to the UI session. We do NOT send a "closed" event for this adapter, because the
    // adapter may not have been sent to the UI (e.g. opening and closing a message box in the
    // same request). If we would send a "closed" event for an adapter that does not exist on
    // the UI, an error would be thrown, because the previous dispose() call removed the adapter
    // from the response, and it will  never be sent to the UI. Only the 'disposeAdapter' event
    // handler on the session can handle that situation (see Session.js).
    getUiSession().sendDisposeAdapterEvent(this);

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
    String option = event.getData().getString("option");
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

  protected class P_MessageBoxListener implements MessageBoxListener {
    @Override
    public void messageBoxChanged(MessageBoxEvent event) {
      ModelJobs.assertModelThread();
      handleModelMessageBoxChanged(event);
    }
  }
}
