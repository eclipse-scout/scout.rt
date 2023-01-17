/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop.bookmark.internal;

import java.security.Permission;

import org.eclipse.scout.rt.client.services.common.bookmark.BookmarkServiceEvent;
import org.eclipse.scout.rt.client.services.common.bookmark.BookmarkServiceListener;
import org.eclipse.scout.rt.client.services.common.bookmark.IBookmarkService;
import org.eclipse.scout.rt.client.ui.desktop.bookmark.AbstractBookmarkTreeField;
import org.eclipse.scout.rt.client.ui.desktop.bookmark.internal.ManageBookmarksForm.MainBox.CancelButton;
import org.eclipse.scout.rt.client.ui.desktop.bookmark.internal.ManageBookmarksForm.MainBox.GlobalBox;
import org.eclipse.scout.rt.client.ui.desktop.bookmark.internal.ManageBookmarksForm.MainBox.GlobalBox.GlobalBookmarkTreeField;
import org.eclipse.scout.rt.client.ui.desktop.bookmark.internal.ManageBookmarksForm.MainBox.OkButton;
import org.eclipse.scout.rt.client.ui.desktop.bookmark.internal.ManageBookmarksForm.MainBox.UserBox;
import org.eclipse.scout.rt.client.ui.desktop.bookmark.internal.ManageBookmarksForm.MainBox.UserBox.UserBookmarkTreeField;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.text.TEXTS;
import org.eclipse.scout.rt.shared.security.DeleteGlobalBookmarkPermission;
import org.eclipse.scout.rt.shared.security.DeleteUserBookmarkPermission;
import org.eclipse.scout.rt.shared.security.ReadUserBookmarkPermission;
import org.eclipse.scout.rt.shared.security.UpdateGlobalBookmarkPermission;
import org.eclipse.scout.rt.shared.security.UpdateUserBookmarkPermission;

@ClassId("e233756c-9f3e-40bf-918d-a869b6321af8")
public class ManageBookmarksForm extends AbstractForm implements BookmarkServiceListener {

  @Override
  protected String getConfiguredTitle() {
    return TEXTS.get("Bookmarks");
  }

  @Override
  public void bookmarksChanged(BookmarkServiceEvent e) {
    getGlobalBookmarkTreeField().setBookmarkRootFolder(e.getBookmarkService().getBookmarkData().getGlobalBookmarks());
    getUserBookmarkTreeField().setBookmarkRootFolder(e.getBookmarkService().getBookmarkData().getUserBookmarks());
  }

  public void startModify() {
    startInternal(new ModifyHandler());
  }

  public MainBox getMainBox() {
    return (MainBox) getRootGroupBox();
  }

  public GlobalBox getGlobalBox() {
    return getFieldByClass(GlobalBox.class);
  }

  public UserBox getUserBox() {
    return getFieldByClass(UserBox.class);
  }

  public OkButton getOkButton() {
    return getFieldByClass(OkButton.class);
  }

  public CancelButton getCancelButton() {
    return getFieldByClass(CancelButton.class);
  }

  public UserBookmarkTreeField getUserBookmarkTreeField() {
    return getFieldByClass(UserBookmarkTreeField.class);
  }

  public GlobalBookmarkTreeField getGlobalBookmarkTreeField() {
    return getFieldByClass(GlobalBookmarkTreeField.class);
  }

  @Order(10)
  @ClassId("2970a2e1-2461-4154-869e-f5ee3884adfc")
  public class MainBox extends AbstractGroupBox {

    @Order(11)
    @ClassId("f7849d3b-3ed1-4eec-884b-d7fa33010702")
    public class GlobalBox extends AbstractGroupBox {
      @Override
      protected String getConfiguredLabel() {
        return TEXTS.get("GlobalBookmarks");
      }

      @Override
      protected void execInitField() {
        setVisiblePermission(new UpdateGlobalBookmarkPermission());
      }

      @Order(10)
      @ClassId("10eaebc7-785a-4dd2-91b2-0052fa20cf21")
      public class GlobalBookmarkTreeField extends AbstractBookmarkTreeField {
        @Override
        protected int getConfiguredGridW() {
          return 2;
        }

        @Override
        protected int getConfiguredGridH() {
          return 10;
        }

        @Override
        protected Permission getDeletePermission() {
          return new DeleteGlobalBookmarkPermission();
        }

        @Override
        protected Permission getUpdatePermission() {
          return new UpdateGlobalBookmarkPermission();
        }
      }
    }

    @Order(20)
    @ClassId("ba0c7a0f-db77-42ce-90ac-86caea9f2c1e")
    public class UserBox extends AbstractGroupBox {
      @Override
      protected String getConfiguredLabel() {
        return TEXTS.get("Bookmarks");
      }

      @Override
      protected void execInitField() {
        setVisiblePermission(new ReadUserBookmarkPermission());
      }

      @Order(10)
      @ClassId("34f55c13-dd28-41a5-a619-9ad9aa5a35ea")
      public class UserBookmarkTreeField extends AbstractBookmarkTreeField {
        @Override
        protected int getConfiguredGridW() {
          return 2;
        }

        @Override
        protected int getConfiguredGridH() {
          return 10;
        }

        @Override
        protected Permission getDeletePermission() {
          return new DeleteUserBookmarkPermission();
        }

        @Override
        protected Permission getUpdatePermission() {
          return new UpdateUserBookmarkPermission();
        }
      }
    }// end group box

    @Order(40)
    @ClassId("57108978-7b2f-41c6-b89f-dd1558cbb935")
    public class OkButton extends AbstractOkButton {
    }

    @Order(50)
    @ClassId("f8d566ec-58b6-4499-b435-e43bc3bd5268")
    public class CancelButton extends AbstractCancelButton {
    }

  }// end main box

  public class ModifyHandler extends AbstractFormHandler {

    @Override
    protected void execLoad() {
      IBookmarkService service = BEANS.get(IBookmarkService.class);
      //get notified about changes
      service.addBookmarkServiceListener(ManageBookmarksForm.this);
      service.loadBookmarks();//load most recent state
      getGlobalBookmarkTreeField().setBookmarkRootFolder(service.getBookmarkData().getGlobalBookmarks());
      getUserBookmarkTreeField().setBookmarkRootFolder(service.getBookmarkData().getUserBookmarks());
      getGlobalBookmarkTreeField().populateTree();
      getUserBookmarkTreeField().populateTree();
    }

    @Override
    protected void execPostLoad() {
      touch();
    }

    @Override
    protected void execStore() {
      BEANS.get(IBookmarkService.class).storeBookmarks();
    }

    @Override
    protected void execDiscard() {
      //revert all changes
      BEANS.get(IBookmarkService.class).loadBookmarks();
    }

    @Override
    protected void execFinally() {
      IBookmarkService service = BEANS.get(IBookmarkService.class);
      service.removeBookmarkServiceListener(ManageBookmarksForm.this);
    }
  }

}
