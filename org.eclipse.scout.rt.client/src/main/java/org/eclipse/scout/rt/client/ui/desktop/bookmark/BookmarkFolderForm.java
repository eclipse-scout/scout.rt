/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.desktop.bookmark;

import org.eclipse.scout.rt.client.ui.desktop.bookmark.BookmarkFolderForm.MainBox.GroupBox.NameField;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.shared.ScoutTexts;

@ClassId("605749af-29e7-47a8-a929-5102da1ab6b4")
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
  @ClassId("b40ce120-1a20-40ee-94c1-1f89ea319690")
  public class MainBox extends AbstractGroupBox {

    @Override
    protected int getConfiguredGridColumnCount() {
      return 1;
    }

    @Order(10)
    @ClassId("9341bc40-6cc9-4259-8de1-3a49c4eaaa3e")
    public class GroupBox extends AbstractGroupBox {

      @Order(10)
      @ClassId("cd6c194a-4205-43c3-8c0b-cb66c29b66b1")
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
    @ClassId("1edb51b0-c7cf-44f0-92e8-57e18365af6f")
    public class OkButton extends AbstractOkButton {
    }

    @Order(50)
    @ClassId("4a611674-c998-4e15-971e-693d9afdbd8a")
    public class CancelButton extends AbstractCancelButton {
    }
  }

  public class NewHandler extends AbstractFormHandler {
  }

  public class ModifyHandler extends AbstractFormHandler {
  }
}
