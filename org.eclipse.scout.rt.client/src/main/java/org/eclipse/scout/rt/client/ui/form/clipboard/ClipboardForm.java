/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.clipboard;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.dto.FormData.DefaultSubtypeSdkCommand;
import org.eclipse.scout.rt.client.dto.FormData.SdkCommand;
import org.eclipse.scout.rt.client.ui.action.keystroke.AbstractKeyStroke;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.clipboard.ClipboardForm.MainBox.CancelButton;
import org.eclipse.scout.rt.client.ui.form.clipboard.ClipboardForm.MainBox.ClipboardField;
import org.eclipse.scout.rt.client.ui.form.clipboard.ClipboardForm.MainBox.ClipboardLabel;
import org.eclipse.scout.rt.client.ui.form.clipboard.ClipboardForm.MainBox.OkButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.clipboardfield.AbstractClipboardField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.labelfield.AbstractLabelField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.resource.MimeType;
import org.eclipse.scout.rt.platform.text.TEXTS;

@ClassId("5aaf757b-4a82-4ffb-bc7e-05f4e5cc1964")
@FormData(sdkCommand = SdkCommand.IGNORE, defaultSubtypeSdkCommand = DefaultSubtypeSdkCommand.IGNORE)
public class ClipboardForm extends AbstractForm {

  private MimeType[] m_mimeTypes;

  public ClipboardForm() {
    this(true);
  }

  protected ClipboardForm(boolean callInitializer) {
    super(callInitializer);
  }

  public MimeType[] getMimeTypes() {
    return m_mimeTypes;
  }

  public void setMimeTypes(MimeType... mimeTypes) {
    m_mimeTypes = mimeTypes;

    List<String> allowedMimeTypesListAsString = new ArrayList<>();
    if (mimeTypes != null) {
      for (MimeType mimeType : mimeTypes) {
        allowedMimeTypesListAsString.add(mimeType.getType());
      }
    }
    getClipboardField().setAllowedMimeTypes(allowedMimeTypesListAsString);
  }

  protected void checkOkButtonEnabled() {
    boolean okButtonEnabled = getHandler() instanceof CopyHandler || getClipboardField().getValue() != null && !getClipboardField().getValue().isEmpty();
    getOkButton().setEnabled(okButtonEnabled, true);
  }

  @Override
  protected String getConfiguredTitle() {
    return TEXTS.get("Clipboard");
  }

  public void startCopy() {
    setHandler(new CopyHandler());
    start();
  }

  public void startPaste() {
    setHandler(new PasteHandler());
    start();
  }

  public ClipboardLabel getClipboardLabel() {
    return getFieldByClass(ClipboardLabel.class);
  }

  public ClipboardField getClipboardField() {
    return getFieldByClass(ClipboardField.class);
  }

  public OkButton getOkButton() {
    return getFieldByClass(OkButton.class);
  }

  public CancelButton getCancelButton() {
    return getFieldByClass(CancelButton.class);
  }

  @ClassId("520d5971-438b-4b81-8ca3-6201f0181b27")
  public class MainBox extends AbstractGroupBox {

    @Override
    protected int getConfiguredGridColumnCount() {
      return 1;
    }

    @Override
    protected String getConfiguredBorderDecoration() {
      return BORDER_DECORATION_EMPTY;
    }

    @Order(10)
    @ClassId("7f2f4401-7bc8-45aa-9cf5-e23129aafd44")
    public class ClipboardLabel extends AbstractLabelField {

      @Override
      protected String getConfiguredCssClass() {
        return "clipboard-form-label";
      }

      @Override
      protected boolean getConfiguredGridUseUiHeight() {
        return true;
      }

      @Override
      protected boolean getConfiguredStatusVisible() {
        return false;
      }

      @Override
      protected boolean getConfiguredLabelVisible() {
        return false;
      }

      @Override
      protected boolean getConfiguredWrapText() {
        return true;
      }
    }

    @Order(20)
    @ClassId("5c81521d-f16d-411a-85f1-c349d8b71a28")
    public class ClipboardField extends AbstractClipboardField {

      @Override
      protected int getConfiguredHeightInPixel() {
        return 190;
      }

      @Override
      protected int getConfiguredWidthInPixel() {
        return 705;
      }

      @Override
      protected double getConfiguredGridWeightX() {
        return 1;
      }

      @Override
      protected double getConfiguredGridWeightY() {
        return 1;
      }

      @Override
      protected boolean getConfiguredLabelVisible() {
        return false;
      }

      @Override
      protected boolean getConfiguredStatusVisible() {
        return false;
      }

      @Override
      protected String getConfiguredFieldStyle() {
        return FIELD_STYLE_CLASSIC;
      }

      @Override
      protected void execChangedValue() {
        checkOkButtonEnabled();
      }
    }

    @Order(30)
    @ClassId("818d7930-126e-481e-ac47-b7e8654e8060")
    public class OkButton extends AbstractOkButton {
    }

    @Order(40)
    @ClassId("a54fc3fc-ee5a-4351-b82f-3168e96c2f34")
    public class CancelButton extends AbstractCancelButton {
    }

    /**
     * Escape keyStroke is required in CopyHandler case, when form has no cancel-button but user should be able to close
     * form with ESC anyway.
     */
    @Order(50)
    @ClassId("18fc9914-495f-4523-b75c-41259de828db")
    public class EscapeKeyStroke extends AbstractKeyStroke {

      @Override
      protected String getConfiguredKeyStroke() {
        return IKeyStroke.ESCAPE;
      }

      @Override
      protected void execAction() {
        doClose();
      }
    }
  }

  public class CopyHandler extends AbstractFormHandler {

    @Override
    protected void execLoad() {
      // use setVisibleGranted here because we don't want to send the cancel-button (incl. ESC keyStroke) to the UI
      super.execLoad();
      getClipboardLabel().setValue(TEXTS.get("CopyToClipboardFromFieldBelow"));
      getCancelButton().setVisibleGranted(false);
      getOkButton().setLabel(TEXTS.get("CloseButton"));
      checkOkButtonEnabled();
      getClipboardField().setReadOnly(true);
    }
  }

  public class PasteHandler extends AbstractFormHandler {

    @Override
    protected void execLoad() {
      super.execLoad();
      getClipboardLabel().setValue(TEXTS.get("PasteClipboardContentsInFieldBelow"));
      checkOkButtonEnabled();
    }
  }
}
