/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop.bookmark;

import java.math.BigDecimal;

import org.eclipse.scout.rt.client.ui.desktop.bookmark.BookmarkForm.MainBox.GroupBox.DescriptionField;
import org.eclipse.scout.rt.client.ui.desktop.bookmark.BookmarkForm.MainBox.GroupBox.FolderField;
import org.eclipse.scout.rt.client.ui.desktop.bookmark.BookmarkForm.MainBox.GroupBox.KeyStrokeField;
import org.eclipse.scout.rt.client.ui.desktop.bookmark.BookmarkForm.MainBox.GroupBox.SortOrderField;
import org.eclipse.scout.rt.client.ui.desktop.bookmark.BookmarkForm.MainBox.GroupBox.TitleField;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.bigdecimalfield.AbstractBigDecimalField;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractSmartField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.text.TEXTS;
import org.eclipse.scout.rt.shared.services.common.bookmark.Bookmark;
import org.eclipse.scout.rt.shared.services.common.bookmark.BookmarkFolder;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;

@ClassId("036f030f-784a-4b55-be08-c95746acd664")
public class BookmarkForm extends AbstractForm implements IBookmarkForm {
  private BookmarkFolder m_bookmarkRootFolder;
  private Bookmark m_bookmark;

  @Override
  public BookmarkFolder getBookmarkRootFolder() {
    return m_bookmarkRootFolder;
  }

  @Override
  public void setBookmarkRootFolder(BookmarkFolder bookmarkRootFolder) {
    m_bookmarkRootFolder = bookmarkRootFolder;
    ((BookmarkFolderLookupCall) getFolderField().getLookupCall()).setRootFolder(m_bookmarkRootFolder);
  }

  @Override
  public Bookmark getBookmark() {
    if (m_bookmark != null) {
      m_bookmark.setTitle(getTitleField().getValue());
      m_bookmark.setKeyStroke(getKeyStrokeField().getValue());
      m_bookmark.setText(getDescriptionField().getValue());
      m_bookmark.setOrder(getSortOrderField().getValue().doubleValue());
    }
    return m_bookmark;
  }

  @Override
  public void setBookmark(Bookmark bookmark) {
    m_bookmark = bookmark;
    if (bookmark != null) {
      getTitleField().setValue(bookmark.getTitle());
      getKeyStrokeField().setValue(bookmark.getKeyStroke());
      getDescriptionField().setValue(bookmark.getText());
      getSortOrderField().setValue(BigDecimal.valueOf(bookmark.getOrder()));
    }
  }

  @Override
  protected String getConfiguredTitle() {
    return TEXTS.get("Bookmark");
  }

  @Override
  public void startModify() {
    startInternal(new ModifyHandler());
  }

  @Override
  public void startNew() {
    startInternal(new NewHandler());
  }

  public MainBox getMainBox() {
    return (MainBox) getRootGroupBox();
  }

  public TitleField getTitleField() {
    return getFieldByClass(TitleField.class);
  }

  public KeyStrokeField getKeyStrokeField() {
    return getFieldByClass(KeyStrokeField.class);
  }

  public DescriptionField getDescriptionField() {
    return getFieldByClass(DescriptionField.class);
  }

  public FolderField getFolderField() {
    return getFieldByClass(FolderField.class);
  }

  public SortOrderField getSortOrderField() {
    return getFieldByClass(SortOrderField.class);
  }

  @Order(10)
  @ClassId("c9263068-999d-4127-9974-2617fd52991b")
  public class MainBox extends AbstractGroupBox {
    @Override
    protected int getConfiguredGridColumnCount() {
      return 1;
    }

    @Order(10)
    @ClassId("1a433540-f1f6-4dfd-be5b-44865e448c49")
    public class GroupBox extends AbstractGroupBox {

      @Order(10)
      @ClassId("30c9307c-093a-4d15-83d6-51ea2312a657")
      public class TitleField extends AbstractStringField {
        @Override
        protected String getConfiguredLabel() {
          return TEXTS.get("Name");
        }

        @Override
        protected boolean getConfiguredMandatory() {
          return true;
        }

        @Override
        protected int getConfiguredMaxLength() {
          return 4000;
        }
      }

      @Order(20)
      @ClassId("44e332c0-62ac-4474-80c9-88d85994ba06")
      public class FolderField extends AbstractSmartField<BookmarkFolder> {
        @Override
        protected String getConfiguredLabel() {
          return TEXTS.get("BookmarkFolder");
        }

        @Override
        protected boolean getConfiguredBrowseHierarchy() {
          return true;
        }

        @Override
        protected Class<? extends ILookupCall<BookmarkFolder>> getConfiguredLookupCall() {
          return BookmarkFolderLookupCall.class;
        }
      }

      @Order(30)
      @ClassId("68d3442e-32f4-4755-abd8-98f67e6013cd")
      public class KeyStrokeField extends AbstractSmartField<String> {
        @Override
        protected String getConfiguredLabel() {
          return TEXTS.get("KeyStroke");
        }

        @Override
        protected Class<? extends ILookupCall<String>> getConfiguredLookupCall() {
          return KeyStrokeLookupCall.class;
        }

        @Override
        protected void execPrepareLookup(ILookupCall<String> call) {
          ((KeyStrokeLookupCall) call).setCurrentKeyStroke(getValue());
        }
      }

      @Order(35)
      @ClassId("8687aa9e-3322-4aa3-90ab-090b33c7168c")
      public class SortOrderField extends AbstractBigDecimalField {
        @Override
        protected String getConfiguredLabel() {
          return TEXTS.get("ColumnSorting");
        }

        @Override
        protected BigDecimal getConfiguredMinValue() {
          return BigDecimal.ZERO;
        }

        @Override
        protected int getConfiguredMinFractionDigits() {
          return 0;
        }
      }

      @Order(40)
      @ClassId("8313302a-12a9-4170-8d69-da4bfa3e2815")
      public class DescriptionField extends AbstractStringField {
        @Override
        protected String getConfiguredLabel() {
          return TEXTS.get("Path");
        }

        @Override
        protected boolean getConfiguredLabelVisible() {
          return false;
        }

        @Override
        protected boolean getConfiguredMultilineText() {
          return true;
        }

        @Override
        protected boolean getConfiguredEnabled() {
          return false;
        }

        @Override
        protected int getConfiguredMaxLength() {
          return 4000;
        }

        @Override
        protected int getConfiguredGridW() {
          return 1;
        }

        @Override
        protected int getConfiguredGridH() {
          return 6;
        }
      }
    }

    @Order(40)
    @ClassId("d97f8aa9-03df-4545-ab09-50dd2925459b")
    public class OkButton extends AbstractOkButton {
    }

    @Order(50)
    @ClassId("b5ff8950-1f7d-4b5f-a66d-2de8b7d25bae")
    public class CancelButton extends AbstractCancelButton {
    }
  }

  public class ModifyHandler extends AbstractFormHandler {
  }

  public class NewHandler extends AbstractFormHandler {
    @Override
    protected void execPostLoad() {
      touch();
    }
  }

  @Override
  public BookmarkFolder getFolder() {
    return getFolderField().getValue();
  }

  @Override
  public void setFolder(BookmarkFolder folder) {
    getFolderField().setValue(folder);
  }
}
