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
package org.eclipse.scout.rt.client.ui.desktop.bookmark;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.rt.client.ui.desktop.bookmark.BookmarkFolderForm.MainBox.GroupBox.NameField;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.shared.ScoutTexts;

public class BookmarkFolderForm extends AbstractForm {

  public BookmarkFolderForm() {
    super();
  }

  @Override
  protected String getConfiguredTitle() {
    return ScoutTexts.get("Folders");
  }

  public void startNew() {
    startInternal(new NewHandler());
  }

  public void startModify() {
    startInternal(new ModifyHandler());
  }

  public MainBox getMainBox() {
    return (MainBox) getRootGroupBox();
  }

  public NameField getNameField() {
    return getFieldByClass(NameField.class);
  }

  @Order(10)
  public class MainBox extends AbstractGroupBox {

    @Override
    protected int getConfiguredGridColumnCount() {
      return 1;
    }

    @Order(10)
    public class GroupBox extends AbstractGroupBox {

      @Order(10)
      public class NameField extends AbstractStringField {
        @Override
        protected String getConfiguredLabel() {
          return ScoutTexts.get("Name");
        }

        @Override
        protected boolean getConfiguredMandatory() {
          return true;
        }

        @Override
        protected int getConfiguredMaxLength() {
          return 4000;
        }

        @Override
        protected String execValidateValue(String rawValue) {
          return rawValue.replaceAll("[\\/]", " ").trim();
        }
      }
    }

    @Order(40)
    public class OkButton extends AbstractOkButton {
    }

    @Order(50)
    public class CancelButton extends AbstractCancelButton {
    }
  }

  public class NewHandler extends AbstractFormHandler {
  }

  public class ModifyHandler extends AbstractFormHandler {
  }
}
