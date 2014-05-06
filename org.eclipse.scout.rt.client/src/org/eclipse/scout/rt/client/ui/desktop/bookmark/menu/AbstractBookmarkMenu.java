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

import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.services.common.bookmark.BookmarkServiceEvent;
import org.eclipse.scout.rt.client.services.common.bookmark.BookmarkServiceListener;
import org.eclipse.scout.rt.client.services.common.bookmark.IBookmarkService;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.MenuSeparator;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.bookmark.BookmarkForm;
import org.eclipse.scout.rt.client.ui.desktop.bookmark.IBookmarkForm;
import org.eclipse.scout.rt.client.ui.desktop.bookmark.internal.ManageBookmarksForm;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.security.CreateGlobalBookmarkPermission;
import org.eclipse.scout.rt.shared.security.CreateUserBookmarkPermission;
import org.eclipse.scout.rt.shared.services.common.bookmark.Bookmark;
import org.eclipse.scout.rt.shared.services.common.bookmark.BookmarkFolder;
import org.eclipse.scout.service.SERVICES;

/**
 * Maintain a menu per bookmark and its key stroke on the desktop
 */
@Order(1f)
public abstract class AbstractBookmarkMenu extends AbstractMenu {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractBookmarkMenu.class);

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
    SERVICES.getService(IBookmarkService.class).addBookmarkServiceListener(
        new BookmarkServiceListener() {
          @Override
          public void bookmarksChanged(BookmarkServiceEvent e) {
            handleBookmarksChanged();
          }
        }
        );
    handleBookmarksChanged();
  }

  @ConfigProperty(ConfigProperty.FORM)
  @Order(20)
  protected Class<? extends IBookmarkForm> getConfiguredBookmarkForm() {
    return BookmarkForm.class;
  }

  private void createNewBookmark(int kind) throws ProcessingException {
    Bookmark b = ClientSyncJob.getCurrentSession().getDesktop().createBookmark();
    if (b != null) {
      IBookmarkService service = SERVICES.getService(IBookmarkService.class);
      b.setKind(kind);
      IBookmarkForm form = null;
      if (getConfiguredBookmarkForm() != null) {
        try {
          form = getConfiguredBookmarkForm().newInstance();
        }
        catch (Exception e) {
          LOG.warn(null, e);
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
    IBookmarkService service = SERVICES.getService(IBookmarkService.class);
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

  @Order(1f)
  public class AddUserBookmarkMenu extends AbstractMenu {
    @Override
    protected String getConfiguredText() {
      return ScoutTexts.get("BookmarksAddMenu");
    }

    @Override
    protected void execInitAction() throws ProcessingException {
      setVisiblePermission(new CreateUserBookmarkPermission());
    }

    @Override
    protected void execAction() throws ProcessingException {
      createNewBookmark(Bookmark.USER_BOOKMARK);
    }
  }

  @Order(2f)
  public class AddGlobalBookmarkMenu extends AbstractMenu {
    @Override
    protected String getConfiguredText() {
      return ScoutTexts.get("GlobalBookmarksAddMenu");
    }

    @Override
    protected void execInitAction() throws ProcessingException {
      setVisiblePermission(new CreateGlobalBookmarkPermission());
    }

    @Override
    protected void execAction() throws ProcessingException {
      SERVICES.getService(IBookmarkService.class).loadBookmarks();
      createNewBookmark(Bookmark.GLOBAL_BOOKMARK);
    }
  }

  @Order(3f)
  public class ManageBookmarksMenu extends AbstractMenu {
    @Override
    protected String getConfiguredText() {
      return ScoutTexts.get("BookmarksManageMenu");
    }

    @Override
    protected void execAction() throws ProcessingException {
      ManageBookmarksForm form = new ManageBookmarksForm();
      form.startModify();
    }
  }

  @Order(4f)
  public class StartBookmarkMenu extends AbstractMenu {
    @Override
    protected String getConfiguredText() {
      return ScoutTexts.get("BookmarksStartPageMenu");
    }

    @Order(1f)
    public class ActivateStartBookmarkMenu extends AbstractMenu {
      @Override
      protected String getConfiguredText() {
        return ScoutTexts.get("BookmarksStartPageMenuGoto");
      }

      @Override
      protected void execAction() throws ProcessingException {
        IBookmarkService service = SERVICES.getService(IBookmarkService.class);
        Bookmark b = service.getStartBookmark();
        if (b != null) {
          try {
            ClientSyncJob.getCurrentSession().getDesktop().activateBookmark(b, false);
          }
          catch (Throwable t) {
            LOG.error(null, t);
          }
        }
      }
    }

    @Order(2f)
    public class Separator1Menu extends AbstractMenu {
      @Override
      protected boolean getConfiguredSeparator() {
        return true;
      }
    }

    @Order(3f)
    public class SetStartBookmarkMenu extends AbstractMenu {
      @Override
      protected String getConfiguredText() {
        return ScoutTexts.get("BookmarksStartPageMenuSet");
      }

      @Override
      protected void execAction() throws ProcessingException {
        IBookmarkService service = SERVICES.getService(IBookmarkService.class);
        service.setStartBookmark();
        service.storeBookmarks();
      }
    }

    @Order(4f)
    public class DeleteStartBookmarkMenu extends AbstractMenu {
      @Override
      protected String getConfiguredText() {
        return ScoutTexts.get("BookmarksStartPageMenuClear");
      }

      @Override
      protected void execAction() throws ProcessingException {
        IBookmarkService service = SERVICES.getService(IBookmarkService.class);
        service.deleteStartBookmark();
        service.storeBookmarks();
      }
    }

  }

  @Order(5f)
  public class Separator1Menu extends MenuSeparator {
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
