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
package org.eclipse.scout.rt.client.ui.desktop.bookmark.menu;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.services.common.bookmark.BookmarkServiceEvent;
import org.eclipse.scout.rt.client.services.common.bookmark.BookmarkServiceListener;
import org.eclipse.scout.rt.client.services.common.bookmark.IBookmarkService;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenuSeparator;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.MenuSeparator;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.bookmark.BookmarkForm;
import org.eclipse.scout.rt.client.ui.desktop.bookmark.IBookmarkForm;
import org.eclipse.scout.rt.client.ui.desktop.bookmark.internal.ManageBookmarksForm;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.security.CreateGlobalBookmarkPermission;
import org.eclipse.scout.rt.shared.security.CreateUserBookmarkPermission;
import org.eclipse.scout.rt.shared.services.common.bookmark.Bookmark;
import org.eclipse.scout.rt.shared.services.common.bookmark.BookmarkFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maintain a menu per bookmark and its key stroke on the desktop
 */
@Order(1)
public abstract class AbstractBookmarkMenu extends AbstractMenu {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractBookmarkMenu.class);

  public AbstractBookmarkMenu() {
  }

  public AbstractBookmarkMenu(IDesktop desktop) {
  }

  @Override
  @ConfigProperty(ConfigProperty.TEXT)
  @Order(10)
  protected String getConfiguredText() {
    return ScoutTexts.get("BookmarksMainMenu");
  }

  @Override
  @ConfigOperation
  @Order(10)
  protected void execInitAction() {
    BEANS.get(IBookmarkService.class).addBookmarkServiceListener(
        new BookmarkServiceListener() {
          @Override
          public void bookmarksChanged(BookmarkServiceEvent e) {
            handleBookmarksChanged();
          }
        });
    handleBookmarksChanged();
  }

  @ConfigProperty(ConfigProperty.FORM)
  @Order(20)
  protected Class<? extends IBookmarkForm> getConfiguredBookmarkForm() {
    return BookmarkForm.class;
  }

  private void createNewBookmark(int kind) {
    Bookmark b = ClientSessionProvider.currentSession().getDesktop().createBookmark();
    if (b != null) {
      IBookmarkService service = BEANS.get(IBookmarkService.class);
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
      if (kind == Bookmark.GLOBAL_BOOKMARK) {
        form.setBookmarkRootFolder(service.getBookmarkData().getGlobalBookmarks());
      }
      else if (kind == Bookmark.USER_BOOKMARK) {
        form.setBookmarkRootFolder(service.getBookmarkData().getUserBookmarks());
      }
      form.setBookmark(b);
      form.startNew();
      form.waitFor();
      if (form.isFormStored()) {
        b.setTitle(form.getBookmark().getTitle());
        b.setKeyStroke(form.getBookmark().getKeyStroke());
        b.setOrder(form.getBookmark().getOrder());
        BookmarkFolder folder = form.getFolder();
        if (folder == null) {
          folder = form.getBookmarkRootFolder();
        }
        folder.getBookmarks().add(b);
        service.storeBookmarks();
      }
    }
  }

  private void handleBookmarksChanged() {
    IBookmarkService service = BEANS.get(IBookmarkService.class);
    List<IMenu> oldList = getChildActions();
    List<IMenu> newList = new ArrayList<IMenu>();
    for (IMenu m : oldList) {
      if (m.getClass() == AddUserBookmarkMenu.class) {
        newList.add(m);
      }
      else if (m.getClass() == AddGlobalBookmarkMenu.class) {
        newList.add(m);
      }
      else if (m.getClass() == ManageBookmarksMenu.class) {
        newList.add(m);
      }
      else if (m.getClass() == StartBookmarkMenu.class) {
        newList.add(m);
      }
      else if (m.getClass() == Separator1Menu.class) {
        newList.add(m);
      }
      else {
        // ignore the rest
        break;
      }
    }
    //add global bookmarks
    newList.add(new MenuSeparator());
    addBookmarkTreeInternal(service.getBookmarkData().getGlobalBookmarks(), newList);
    //add user bookmarks
    newList.add(new MenuSeparator());
    addBookmarkTreeInternal(service.getBookmarkData().getUserBookmarks(), newList);
    setChildActions(newList);
  }

  @Order(1)
  public class AddUserBookmarkMenu extends AbstractMenu {
    @Override
    protected String getConfiguredText() {
      return ScoutTexts.get("BookmarksAddMenu");
    }

    @Override
    protected void execInitAction() {
      setVisiblePermission(new CreateUserBookmarkPermission());
    }

    @Override
    protected void execAction() {
      createNewBookmark(Bookmark.USER_BOOKMARK);
    }
  }

  @Order(2)
  public class AddGlobalBookmarkMenu extends AbstractMenu {
    @Override
    protected String getConfiguredText() {
      return ScoutTexts.get("GlobalBookmarksAddMenu");
    }

    @Override
    protected void execInitAction() {
      setVisiblePermission(new CreateGlobalBookmarkPermission());
    }

    @Override
    protected void execAction() {
      BEANS.get(IBookmarkService.class).loadBookmarks();
      createNewBookmark(Bookmark.GLOBAL_BOOKMARK);
    }
  }

  @Order(3)
  public class ManageBookmarksMenu extends AbstractMenu {
    @Override
    protected String getConfiguredText() {
      return ScoutTexts.get("BookmarksManageMenu");
    }

    @Override
    protected void execAction() {
      ManageBookmarksForm form = new ManageBookmarksForm();
      form.startModify();
    }
  }

  @Order(4)
  public class StartBookmarkMenu extends AbstractMenu {
    @Override
    protected String getConfiguredText() {
      return ScoutTexts.get("BookmarksStartPageMenu");
    }

    @Order(1)
    public class ActivateStartBookmarkMenu extends AbstractMenu {
      @Override
      protected String getConfiguredText() {
        return ScoutTexts.get("BookmarksStartPageMenuGoto");
      }

      @Override
      protected void execAction() {
        IBookmarkService service = BEANS.get(IBookmarkService.class);
        Bookmark b = service.getStartBookmark();
        if (b != null) {
          try {
            ClientSessionProvider.currentSession().getDesktop().activateBookmark(b);
          }
          catch (Exception e) {
            LOG.error("Error activating bookmark", e);
          }
        }
      }
    }

    @Order(2)
    public class Separator1Menu extends AbstractMenu {
      @Override
      protected boolean getConfiguredSeparator() {
        return true;
      }
    }

    @Order(3)
    public class SetStartBookmarkMenu extends AbstractMenu {
      @Override
      protected String getConfiguredText() {
        return ScoutTexts.get("BookmarksStartPageMenuSet");
      }

      @Override
      protected void execAction() {
        IBookmarkService service = BEANS.get(IBookmarkService.class);
        service.setStartBookmark();
        service.storeBookmarks();
      }
    }

    @Order(4)
    public class DeleteStartBookmarkMenu extends AbstractMenu {
      @Override
      protected String getConfiguredText() {
        return ScoutTexts.get("BookmarksStartPageMenuClear");
      }

      @Override
      protected void execAction() {
        IBookmarkService service = BEANS.get(IBookmarkService.class);
        service.deleteStartBookmark();
        service.storeBookmarks();
      }
    }

  }

  @Order(5)
  public class Separator1Menu extends AbstractMenuSeparator {
  }

  private void addBookmarkTreeInternal(BookmarkFolder folder, List<IMenu> actionList) {
    for (BookmarkFolder f : folder.getFolders()) {
      IMenu group = new MenuSeparator();
      group.setSeparator(false);
      group.setText(f.getTitle());
      group.setIconId(f.getIconId());
      List<IMenu> childActionList = new ArrayList<IMenu>();
      addBookmarkTreeInternal(f, childActionList);
      group.setChildActions(childActionList);
      actionList.add(group);
    }
    for (Bookmark b : folder.getBookmarks()) {
      actionList.add(new ActivateBookmarkMenu(b));
    }
  }

}
