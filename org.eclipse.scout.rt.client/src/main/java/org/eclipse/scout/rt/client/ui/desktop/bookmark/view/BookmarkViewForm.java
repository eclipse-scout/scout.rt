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
package org.eclipse.scout.rt.client.ui.desktop.bookmark.view;

import java.security.Permission;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.client.services.common.bookmark.BookmarkClientNotificationHandler;
import org.eclipse.scout.rt.client.services.common.bookmark.BookmarkServiceEvent;
import org.eclipse.scout.rt.client.services.common.bookmark.BookmarkServiceListener;
import org.eclipse.scout.rt.client.services.common.bookmark.IBookmarkService;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNodeFilter;
import org.eclipse.scout.rt.client.ui.desktop.bookmark.AbstractBookmarkTreeField;
import org.eclipse.scout.rt.client.ui.desktop.bookmark.BookmarkForm;
import org.eclipse.scout.rt.client.ui.desktop.bookmark.IBookmarkForm;
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
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.notification.INotificationListener;
import org.eclipse.scout.rt.shared.security.CreateUserBookmarkPermission;
import org.eclipse.scout.rt.shared.security.DeleteUserBookmarkPermission;
import org.eclipse.scout.rt.shared.security.UpdateUserBookmarkPermission;
import org.eclipse.scout.rt.shared.services.common.bookmark.Bookmark;
import org.eclipse.scout.rt.shared.services.common.bookmark.BookmarkChangedClientNotification;
import org.eclipse.scout.rt.shared.services.common.bookmark.BookmarkFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ClassId("f75ab95f-f827-431b-b5e9-53c71b5eb965")
public class BookmarkViewForm extends AbstractForm {
  private static final Logger LOG = LoggerFactory.getLogger(BookmarkViewForm.class);

  public BookmarkViewForm() {
    super();
  }

  @ConfigProperty(ConfigProperty.FORM)
  @Order(10)
  protected Class<? extends IBookmarkForm> getConfiguredBookmarkForm() {
    return BookmarkForm.class;
  }

  @Override
  protected String getConfiguredTitle() {
    return ScoutTexts.get("Bookmarks");
  }

  public void startView() {
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

  public AddBookmarksLinkButton getAddBookmarksLinkButton() {
    return getFieldByClass(AddBookmarksLinkButton.class);
  }

  public SetStartPageLinkButton getSetStartPageLinkButton() {
    return getFieldByClass(SetStartPageLinkButton.class);
  }

  public ClearStartPageLinkButton getClearStartPageLinkButton() {
    return getFieldByClass(ClearStartPageLinkButton.class);
  }

  @Order(10)
  @ClassId("6cba98a1-570b-4cca-b85b-3a2408fccf1d")
  public class MainBox extends AbstractGroupBox {

    @Order(10)
    @ClassId("80d03507-9df7-448f-ba2f-4d35613d03cc")
    public class TabBox extends AbstractTabBox {

      @Order(10)
      @ClassId("549d76a2-9231-412f-8635-aedc0e2b801a")
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
        @ClassId("23af2442-38f6-4c74-a590-2180c39cff15")
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
          protected byte getConfiguredLabelPosition() {
            return IFormField.LABEL_POSITION_ON_FIELD;
          }

          @Override
          protected boolean getConfiguredUpdateDisplayTextOnModify() {
            return true;
          }

          @Override
          protected void execChangedDisplayText() {
            String s = StringUtility.emptyIfNull(getDisplayText()).trim();
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
        @ClassId("abaae7ed-f148-4368-94ac-e53710b25b24")
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
        @ClassId("10b52ed7-ce47-48ae-a45b-927c1e49fa55")
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
          protected void execInitField() {
            setVisiblePermission(new CreateUserBookmarkPermission());
          }

          @Override
          protected void execClickAction() {
            //createNewBookmark
            int kind = Bookmark.USER_BOOKMARK;
            Bookmark b = ClientSessionProvider.currentSession().getDesktop().createBookmark();
            if (b != null) {
              b.setKind(kind);
              IBookmarkForm form = null;
              if (getConfiguredBookmarkForm() != null) {
                try {
                  form = getConfiguredBookmarkForm().newInstance();
                }
                catch (Exception e) {
                  BEANS.get(ExceptionHandler.class).handle(new ProcessingException("error creating instance of class '" + getConfiguredBookmarkForm().getName() + "'.", e));
                }
              }
              if (form == null) {
                form = new BookmarkForm();
              }
              form.setBookmarkRootFolder(getUserBookmarkTreeField().getBookmarkRootFolder());
              form.setBookmark(b);
              if (form.getBookmarkRootFolder() != form.getBookmarkRootFolder()) {
                form.setFolder(form.getBookmarkRootFolder());
              }
              form.startNew();
              form.waitFor();
              if (form.isFormStored()) {
                b.setTitle(b.getTitle());
                b.setKeyStroke(b.getKeyStroke());
                b.setOrder(b.getOrder());
                BookmarkFolder folder = form.getFolder();
                if (folder == null) {
                  folder = form.getBookmarkRootFolder();
                }
                folder.getBookmarks().add(b);
                IBookmarkService service = BEANS.get(IBookmarkService.class);
                service.storeBookmarks();
              }
            }
          }
        }

        @Order(110)
        @ClassId("c055e650-5f0e-458c-a4f9-10ecf37e78d0")
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
          protected void execInitField() {
            setVisiblePermission(new CreateUserBookmarkPermission());
          }

          @Override
          protected void execClickAction() {
            IBookmarkService service = BEANS.get(IBookmarkService.class);
            service.setStartBookmark();
            service.storeBookmarks();
          }
        }

        @Order(120)
        @ClassId("3fdab4a7-3f4f-4cae-96bd-31b0d7cd77c5")
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
          protected void execInitField() {
            setVisiblePermission(new CreateUserBookmarkPermission());
          }

          @Override
          protected void execClickAction() {
            IBookmarkService service = BEANS.get(IBookmarkService.class);
            service.deleteStartBookmark();
            service.storeBookmarks();
          }
        }
      }
    }
  }

  private void refreshFormState() {
    IBookmarkService bmService = BEANS.get(IBookmarkService.class);
    getUserBookmarkTreeField().setBookmarkRootFolder(bmService.getBookmarkData().getUserBookmarks());
    getUserBookmarkTreeField().populateTree();
  }

  public class ViewHandler extends AbstractFormHandler {

    private final INotificationListener<BookmarkChangedClientNotification> m_cncListener = new INotificationListener<BookmarkChangedClientNotification>() {
      @Override
      public void handleNotification(BookmarkChangedClientNotification notification) {
        try {
          BEANS.get(IBookmarkService.class).loadBookmarks();
        }
        catch (RuntimeException e) {
          LOG.error("Could not reload bookmarks.", e);
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
    protected void execLoad() {
      //add listeners
      BookmarkClientNotificationHandler bookmarkClientNotificationHandler = BEANS.get(BookmarkClientNotificationHandler.class);
      bookmarkClientNotificationHandler.addListener(m_cncListener);
      IBookmarkService bmService = BEANS.get(IBookmarkService.class);
      if (bmService != null) {
        bmService.removeBookmarkServiceListener(m_bmListener);
        bmService.addBookmarkServiceListener(m_bmListener);
        bmService.loadBookmarks();
      }
    }

    @Override
    protected void execFinally() {
      IBookmarkService bmService = BEANS.get(IBookmarkService.class);
      if (bmService != null) {
        bmService.removeBookmarkServiceListener(m_bmListener);
      }
      BookmarkClientNotificationHandler bookmarkClientNotificationHandler = BEANS.get(BookmarkClientNotificationHandler.class);
      bookmarkClientNotificationHandler.removeListener(m_cncListener);
    }
  }
}
