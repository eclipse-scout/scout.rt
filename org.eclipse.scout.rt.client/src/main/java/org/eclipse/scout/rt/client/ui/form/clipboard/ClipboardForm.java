/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.clipboard;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.commons.annotations.FormData;
import org.eclipse.scout.commons.annotations.FormData.DefaultSubtypeSdkCommand;
import org.eclipse.scout.commons.annotations.FormData.SdkCommand;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.resource.MimeType;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.clipboard.ClipboardForm.MainBox.CancelButton;
import org.eclipse.scout.rt.client.ui.form.clipboard.ClipboardForm.MainBox.ClipboardBox.ClipboardField;
import org.eclipse.scout.rt.client.ui.form.clipboard.ClipboardForm.MainBox.ClipboardLabel;
import org.eclipse.scout.rt.client.ui.form.clipboard.ClipboardForm.MainBox.OkButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.clipboardfield.AbstractClipboardField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.labelfield.AbstractLabelField;
import org.eclipse.scout.rt.shared.TEXTS;

@FormData(sdkCommand = SdkCommand.IGNORE, defaultSubtypeSdkCommand = DefaultSubtypeSdkCommand.IGNORE)
public class ClipboardForm extends AbstractForm {

  private MimeType[] m_mimeTypes;

  public ClipboardForm() throws ProcessingException {
    this(true);
  }

  protected ClipboardForm(boolean callInitializer) throws ProcessingException {
    super(callInitializer);
  }

  public MimeType[] getMimeTypes() {
    return m_mimeTypes;
  }

  public void setMimeTypes(MimeType... mimeTypes) {
    m_mimeTypes = mimeTypes;

    List<String> allowedMimeTypesListAsString = new ArrayList<String>();
    if (mimeTypes != null) {
      for (MimeType mimeType : mimeTypes) {
        allowedMimeTypesListAsString.add(mimeType.getType());
      }
    }
    getClipboardField().setAllowedMimeTypes(allowedMimeTypesListAsString);
  }

  protected void checkOkButtonEnabled() {
    getOkButton().setEnabled(getHandler() instanceof CopyHandler || getClipboardField().getValue() != null && !getClipboardField().getValue().isEmpty());
  }

  @Override
  protected String getConfiguredTitle() {
    return TEXTS.get("Clipboard");
  }

  public void startCopy() throws ProcessingException {
    setHandler(new CopyHandler());
    start();
  }

  public void startPaste() throws ProcessingException {
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

  public class MainBox extends AbstractGroupBox {

    @Override
    protected int getConfiguredGridColumnCount() {
      return 1;
    }

    @Order(10.0)
    public class ClipboardLabel extends AbstractLabelField {

      @Override
      protected double getConfiguredGridWeightX() {
        return 1;
      }

      @Override
      protected int getConfiguredHeightInPixel() {
        return 40;
      }

      @Override
      protected boolean getConfiguredStatusVisible() {
        return false;
      }

      @Override
      protected void execInitField() throws ProcessingException {
        super.execInitField();
      }

      @Override
      protected boolean getConfiguredLabelVisible() {
        return false;
      }
    }

    @Order(20.0)
    public class ClipboardBox extends AbstractGroupBox {

      @Override
      protected int getConfiguredHeightInPixel() {
        return 220;
      }

      @Override
      protected int getConfiguredWidthInPixel() {
        return 705;
      }

      @Order(10.0)
      public class ClipboardField extends AbstractClipboardField {

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
        protected void execChangedValue() throws ProcessingException {
          checkOkButtonEnabled();
        }
      }

    }

    @Order(30.0)
    public class OkButton extends AbstractOkButton {
    }

    @Order(40.0)
    public class CancelButton extends AbstractCancelButton {
    }
  }

  public class CopyHandler extends AbstractFormHandler {

    @Override
    protected void execLoad() throws ProcessingException {
      super.execLoad();

      getClipboardLabel().setValue(TEXTS.get("CopyToClipboardFromFieldBelow"));

      getCancelButton().setVisible(false);
      checkOkButtonEnabled();
    }
  }

  public class PasteHandler extends AbstractFormHandler {

    @Override
    protected void execLoad() throws ProcessingException {
      super.execLoad();

      getClipboardLabel().setValue(TEXTS.get("PasteClipboardContentsInFieldBelow"));

      checkOkButtonEnabled();
    }
  }
}
