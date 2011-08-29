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
package org.eclipse.scout.rt.client.ui.desktop.bookmark.view;

import java.security.Permission;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.services.common.bookmark.BookmarkServiceEvent;
import org.eclipse.scout.rt.client.services.common.bookmark.BookmarkServiceListener;
import org.eclipse.scout.rt.client.services.common.bookmark.IBookmarkService;
import org.eclipse.scout.rt.client.services.common.clientnotification.ClientNotificationConsumerEvent;
import org.eclipse.scout.rt.client.services.common.clientnotification.IClientNotificationConsumerListener;
import org.eclipse.scout.rt.client.services.common.clientnotification.IClientNotificationConsumerService;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNodeFilter;
import org.eclipse.scout.rt.client.ui.desktop.bookmark.AbstractBookmarkTreeField;
import org.eclipse.scout.rt.client.ui.desktop.bookmark.BookmarkForm;
import org.eclipse.scout.rt.client.ui.desktop.bookmark.view.BookmarkViewForm.MainBox.TabBox.BookmarksBox;
import org.eclipse.scout.rt.client.ui.desktop.bookmark.view.BookmarkViewForm.MainBox.TabBox.BookmarksBox.AddBookmarksLinkButton;
import org.eclipse.scout.rt.client.ui.desktop.bookmark.view.BookmarkViewForm.MainBox.TabBox.BookmarksBox.ClearStartPageLinkButton;
import org.eclipse.scout.rt.client.ui.desktop.bookmark.view.BookmarkViewForm.MainBox.TabBox.BookmarksBox.SetStartPageLinkButton;
import org.eclipse.scout.rt.client.ui.desktop.bookmark.view.BookmarkViewForm.MainBox.TabBox.BookmarksBox.UserBookmarkSearchField;
import org.eclipse.scout.rt.client.ui.desktop.bookmark.view.BookmarkViewForm.MainBox.TabBox.BookmarksBox.UserBookmarkTreeField;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractLinkButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.AbstractTabBox;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.security.CreateUserBookmarkPermission;
import org.eclipse.scout.rt.shared.security.DeleteUserBookmarkPermission;
import org.eclipse.scout.rt.shared.security.UpdateUserBookmarkPermission;
import org.eclipse.scout.rt.shared.services.common.bookmark.Bookmark;
import org.eclipse.scout.rt.shared.services.common.bookmark.BookmarkChangedClientNotification;
import org.eclipse.scout.rt.shared.services.common.bookmark.BookmarkFolder;
import org.eclipse.scout.service.SERVICES;

public class BookmarkViewForm extends AbstractForm {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(BookmarkViewForm.class);

  public BookmarkViewForm() throws ProcessingException {
    super();
  }

  @Override
  protected String getConfiguredTitle() {
    return ScoutTexts.get("Bookmarks");
  }

  public void startView() throws ProcessingException {
    startInternal(new ViewHandler());
  }

  public MainBox getMainBox() {
    return (MainBox) getRootGroupBox();
  }

  public BookmarksBox getBookmarksBox() {
    return getFieldByClass(BookmarksBox.class);
  }

  public UserBookmarkTreeField getUserBookmarkTreeField() {
    return getFieldByClass(UserBookmarkTreeField.class);
  }

  public UserBookmarkSearchField getUserBookmarkSearchField() {
    return getFieldByClass(UserBookmarkSearchField.class);
  }

  public AddBookmarksLinkButton getOkButton() {
    return getFieldByClass(AddBookmarksLinkButton.class);
  }

  public SetStartPageLinkButton getSetStartPageLinkButton() {
    return getFieldByClass(SetStartPageLinkButton.class);
  }

  public ClearStartPageLinkButton getClearStartPageLinkButton() {
    return getFieldByClass(ClearStartPageLinkButton.class);
  }

  @Order(10)
  public class MainBox extends AbstractGroupBox {

    @Order(10)
    public class TabBox extends AbstractTabBox {

      @Order(10)
      public class BookmarksBox extends AbstractGroupBox {
        @Override
        protected String getConfiguredLabel() {
          return ScoutTexts.get("Bookmarks");
        }

        @Override
        protected int getConfiguredGridColumnCount() {
          return 1;
        }

        @Order(20)
        public class UserBookmarkSearchField extends AbstractStringField implements ITreeNodeFilter {
          private Pattern m_lowercaseFilterPattern;

          @Override
          protected String getConfiguredLabel() {
            return ScoutTexts.get("FilterBookmarkTree");
          }

          @Override
          protected String getConfiguredTooltipText() {
            return ScoutTexts.get("SmartFindLabel");
          }

          @Override
          protected boolean getConfiguredLabelVisible() {
            return false;
          }

          @Override
          public int getConfiguredLabelPosition() {
            return IFormField.LABEL_POSITION_ON_FIELD;
          }

          @Override
          protected boolean getConfiguredValidateOnAnyKey() {
            return true;
          }

          @Override
          protected void execChangedValue() throws ProcessingException {
            String s = StringUtility.emptyIfNull(getValue()).trim();
            if (s.length() > 0) {
              if (!s.endsWith("*")) {
                s = s + "*";
              }
              m_lowercaseFilterPattern = Pattern.compile(StringUtility.toRegExPattern(s.toLowerCase()));
              getUserBookmarkTreeField().getTree().addNodeFilter(this);
            }
            else {
              getUserBookmarkTreeField().getTree().removeNodeFilter(this);
            }
          }

          /**
           * Implementation of ITreeNodeFilter
           */
          @Override
          public boolean accept(ITreeNode node, int level) {
            String text = node.getCell().getText();
            return text == null || m_lowercaseFilterPattern == null || m_lowercaseFilterPattern.matcher(text.toLowerCase()).matches();
          }
        }

        @Order(20)
        public class UserBookmarkTreeField extends AbstractBookmarkTreeField {
          @Override
          protected int getConfiguredGridW() {
            return 1;
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

        @Order(100)
        public class AddBookmarksLinkButton extends AbstractLinkButton {
          @Override
          protected String getConfiguredLabel() {
            return ScoutTexts.get("BookmarksAddMenu");
          }

          @Override
          protected boolean getConfiguredProcessButton() {
            return false;
          }

          @Override
          protected void execInitField() throws ProcessingException {
            setVisiblePermission(new CreateUserBookmarkPermission());
          }

          @Override
          protected void execClickAction() throws ProcessingException {
            //createNewBookmark
            int kind = Bookmark.USER_BOOKMARK;
            Bookmark b = ClientSyncJob.getCurrentSession().getDesktop().createBookmark();
            if (b != null) {
              b.setKind(kind);
              BookmarkForm form = new BookmarkForm();
              form.setBookmarkRootFolder(getUserBookmarkTreeField().getBookmarkRootFolder());
              form.getTitleField().setValue(b.getTitle());
              form.getKeyStrokeField().setValue(b.getKeyStroke());
              form.getDescriptionField().setValue(b.getText());
              if (form.getBookmarkRootFolder() != form.getBookmarkRootFolder()) {
                form.getFolderField().setValue(form.getBookmarkRootFolder());
              }
              form.startNew();
              form.waitFor();
              if (form.isFormStored()) {
                b.setTitle(form.getTitleField().getValue());
                b.setKeyStroke(form.getKeyStrokeField().getValue());
                BookmarkFolder folder = form.getFolderField().getValue();
                if (folder == null) {
                  folder = form.getBookmarkRootFolder();
                }
                folder.getBookmarks().add(b);
                IBookmarkService service = SERVICES.getService(IBookmarkService.class);
                service.storeBookmarks();
              }
            }
          }
        }

        @Order(110)
        public class SetStartPageLinkButton extends AbstractLinkButton {
          @Override
          protected String getConfiguredLabel() {
            return ScoutTexts.get("BookmarksStartPageMenuSet");
          }

          @Override
          protected boolean getConfiguredProcessButton() {
            return false;
          }

          @Override
          protected void execInitField() throws ProcessingException {
            setVisiblePermission(new CreateUserBookmarkPermission());
          }

          @Override
          protected void execClickAction() throws ProcessingException {
            IBookmarkService service = SERVICES.getService(IBookmarkService.class);
            service.setStartBookmark();
            service.storeBookmarks();
          }
        }

        @Order(120)
        public class ClearStartPageLinkButton extends AbstractLinkButton {
          @Override
          protected String getConfiguredLabel() {
            return ScoutTexts.get("BookmarksStartPageMenuClear");
          }

          @Override
          protected boolean getConfiguredProcessButton() {
            return false;
          }

          @Override
          protected void execInitField() throws ProcessingException {
            setVisiblePermission(new CreateUserBookmarkPermission());
          }

          @Override
          protected void execClickAction() throws ProcessingException {
            IBookmarkService service = SERVICES.getService(IBookmarkService.class);
            service.deleteStartBookmark();
            service.storeBookmarks();
          }
        }
      }
    }
  }

  private void refreshFormState() {
    IBookmarkService bmService = SERVICES.getService(IBookmarkService.class);
    getUserBookmarkTreeField().setBookmarkRootFolder(bmService.getBookmarkData().getUserBookmarks());
    getUserBookmarkTreeField().populateTree();
  }

  @Order(20f)
  public class ViewHandler extends AbstractFormHandler {

    private final IClientNotificationConsumerListener m_cncListener = new IClientNotificationConsumerListener() {
      @Override
      public void handleEvent(ClientNotificationConsumerEvent e, boolean sync) {
        if (e.getClientNotification() instanceof BookmarkChangedClientNotification) {
          new ClientSyncJob("Bookmarks changed", ClientSyncJob.getCurrentSession()) {
            @Override
            protected void runVoid(IProgressMonitor monitor) throws Throwable {
              SERVICES.getService(IBookmarkService.class).loadBookmarks();
            }
          }.schedule();
        }
      }
    };

    private final BookmarkServiceListener m_bmListener = new BookmarkServiceListener() {
      @Override
      public void bookmarksChanged(BookmarkServiceEvent e) {
        switch (e.getType()) {
          case BookmarkServiceEvent.TYPE_CHANGED: {
            refreshFormState();
            break;
          }
        }
      }
    };

    @Override
    protected void execLoad() throws ProcessingException {
      //add listeners
      IClientNotificationConsumerService cncService = SERVICES.getService(IClientNotificationConsumerService.class);
      if (cncService != null) {
        cncService.removeClientNotificationConsumerListener(ClientSyncJob.getCurrentSession(), m_cncListener);
        cncService.addClientNotificationConsumerListener(ClientSyncJob.getCurrentSession(), m_cncListener);
      }
      IBookmarkService bmService = SERVICES.getService(IBookmarkService.class);
      if (bmService != null) {
        bmService.removeBookmarkServiceListener(m_bmListener);
        bmService.addBookmarkServiceListener(m_bmListener);
        bmService.loadBookmarks();
      }
    }

    @Override
    protected void execFinally() throws ProcessingException {
      IBookmarkService bmService = SERVICES.getService(IBookmarkService.class);
      IClientNotificationConsumerService cncService = SERVICES.getService(IClientNotificationConsumerService.class);
      if (bmService != null) {
        bmService.removeBookmarkServiceListener(m_bmListener);
      }
      if (cncService != null) {
        cncService.removeClientNotificationConsumerListener(ClientSyncJob.getCurrentSession(), m_cncListener);
      }
    }
  }
}
