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

import java.security.Permission;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.dnd.JavaTransferObject;
import org.eclipse.scout.commons.dnd.TransferObject;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.services.common.bookmark.IBookmarkService;
import org.eclipse.scout.rt.client.ui.action.keystroke.AbstractKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.MenuSeparator;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTree;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeVisitor;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.bookmark.view.IPublishBookmarkCommand;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.treefield.AbstractTreeField;
import org.eclipse.scout.rt.client.ui.messagebox.MessageBox;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.security.PublishUserBookmarkPermission;
import org.eclipse.scout.rt.shared.services.common.bookmark.AbstractPageState;
import org.eclipse.scout.rt.shared.services.common.bookmark.Bookmark;
import org.eclipse.scout.rt.shared.services.common.bookmark.BookmarkFolder;
import org.eclipse.scout.rt.shared.services.common.bookmark.TablePageState;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.eclipse.scout.service.SERVICES;

public abstract class AbstractBookmarkTreeField extends AbstractTreeField {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractBookmarkTreeField.class);

  private BookmarkFolder m_bookmarkRootFolder;
  private IPublishBookmarkCommand m_publishBookmarkCommand;

  public AbstractBookmarkTreeField() {
    super();
  }

  public BookmarkFolder getBookmarkRootFolder() {
    return m_bookmarkRootFolder;
  }

  public void setBookmarkRootFolder(BookmarkFolder bookmarkRootFolder) {
    m_bookmarkRootFolder = bookmarkRootFolder;
  }

  private static boolean isBookmarkNode(ITreeNode node) {
    return node instanceof BookmarkNode;
  }

  private static boolean isFolderNode(ITreeNode node) {
    return node instanceof FolderNode;
  }

  public void injectPublishBookmarkCommand(IPublishBookmarkCommand command) {
    m_publishBookmarkCommand = command;
  }

  private BookmarkFolder getParentBookmarkFolder(ITreeNode node) {
    BookmarkFolder folder = getBookmarkRootFolder();
    ITreeNode parentNode = node.getParentNode();
    if (isFolderNode(parentNode)) {
      folder = (BookmarkFolder) parentNode.getCell().getValue();
    }
    return folder;
  }

  @Override
  protected int getConfiguredGridW() {
    return 1;
  }

  @Override
  protected int getConfiguredGridH() {
    return 8;
  }

  @Override
  protected boolean getConfiguredAutoLoad() {
    return false;
  }

  @Override
  protected boolean getConfiguredLabelVisible() {
    return false;
  }

  protected abstract Permission getDeletePermission();

  protected abstract Permission getUpdatePermission();

  public void populateTree() {
    try {
      getTree().setTreeChanging(true);
      getTree().removeAllChildNodes(getTree().getRootNode());
      populateFolderContentRec(getTree().getRootNode(), getBookmarkRootFolder());
    }
    catch (ProcessingException e) {
      LOG.error(null, e);
    }
    finally {
      getTree().setTreeChanging(false);
    }
    getTree().expandAll(getTree().getRootNode());
  }

  /**
   * @return true if populate of delta was successful
   */
  private void populateFolderContentRec(ITreeNode parent, BookmarkFolder newParent) throws ProcessingException {
    for (BookmarkFolder newFolder : newParent.getFolders()) {
      FolderNode newNode = new FolderNode();
      newNode.getCellForUpdate().setValue(newFolder);
      getTree().addChildNode(parent, newNode);
      populateFolderContentRec(newNode, newFolder);
    }
    for (Bookmark b : newParent.getBookmarks()) {
      BookmarkNode newNode = new BookmarkNode();
      newNode.getCellForUpdate().setValue(b);
      getTree().addChildNode(parent, newNode);
    }
  }

  /**
   * The structure of the folders has changed, completely rebuild the model
   * 
   * @throws ProcessingException
   */
  private void rebuildBookmarkModel() throws ProcessingException {
    getTree().visitTree(new ITreeVisitor() {
      public boolean visit(ITreeNode node) {
        BookmarkFolder bmFolder = null;
        if (node == getTree().getRootNode()) {
          bmFolder = getBookmarkRootFolder();
        }
        else if (isFolderNode(node)) {
          bmFolder = (BookmarkFolder) node.getCell().getValue();
        }
        if (bmFolder != null) {
          bmFolder.getFolders().clear();
          bmFolder.getBookmarks().clear();
          //sort folders
          ArrayList<BookmarkFolder> folderList = new ArrayList<BookmarkFolder>();
          for (ITreeNode n : node.getChildNodes()) {
            if (isFolderNode(n)) {
              BookmarkFolder f = (BookmarkFolder) n.getCell().getValue();
              folderList.add(f);
            }
          }
          Collections.sort(folderList, new Comparator<BookmarkFolder>() {
            public int compare(BookmarkFolder f1, BookmarkFolder f2) {
              return StringUtility.compareIgnoreCase(f1.getTitle(), f2.getTitle());
            }
          });
          bmFolder.getFolders().addAll(folderList);
          for (ITreeNode n : node.getChildNodes()) {
            if (isBookmarkNode(n)) {
              bmFolder.getBookmarks().add((Bookmark) n.getCell().getValue());
            }
          }
        }
        return true;
      }
    });
    //save
    SERVICES.getService(IBookmarkService.class).storeBookmarks();
  }

  /**
   * Only some values have changed, just save the model
   * 
   * @throws ProcessingException
   */
  private void refreshBookmarkModel() throws ProcessingException {
    getTree().visitTree(new ITreeVisitor() {
      public boolean visit(ITreeNode node) {
        BookmarkFolder bmFolder = null;
        if (node == getTree().getRootNode()) {
          bmFolder = getBookmarkRootFolder();
        }
        else if (isFolderNode(node)) {
          bmFolder = (BookmarkFolder) node.getCell().getValue();
        }
        if (bmFolder != null) {
          bmFolder.getFolders().clear();
          bmFolder.getBookmarks().clear();
          for (ITreeNode n : node.getChildNodes()) {
            if (isFolderNode(n)) {
              bmFolder.getFolders().add((BookmarkFolder) n.getCell().getValue());
            }
            else if (isBookmarkNode(n)) {
              bmFolder.getBookmarks().add((Bookmark) n.getCell().getValue());
            }
          }
        }
        return true;
      }
    });
    //save
    SERVICES.getService(IBookmarkService.class).storeBookmarks();
  }

  @Order(10)
  public class Tree extends AbstractTree {

    @Override
    protected boolean getConfiguredMultiSelect() {
      return true;
    }

    @Override
    protected boolean getConfiguredDragEnabled() {
      return true;
    }

    @Override
    protected int getConfiguredDragType() {
      return TYPE_JAVA_ELEMENT_TRANSFER;
    }

    @Override
    protected int getConfiguredDropType() {
      return TYPE_JAVA_ELEMENT_TRANSFER;
    }

    @Override
    protected void execNodeAction(ITreeNode node) throws ProcessingException {
      if (isBookmarkNode(node)) {
        Bookmark bm = (Bookmark) node.getCell().getValue();
        ClientSyncJob.getCurrentSession().getDesktop().activateBookmark(bm, false);
      }
    }

    @Override
    protected TransferObject execDrag(ITreeNode node) {
      if (ACCESS.check(getUpdatePermission())) {
        if (isBookmarkNode(node)) {
          return new JavaTransferObject(node);
        }
        else if (isFolderNode(node)) {
          return new JavaTransferObject(node);
        }
      }
      return null;
    }

    @Override
    protected void execDrop(ITreeNode dropNode, TransferObject t) {
      if (t instanceof JavaTransferObject) {
        try {
          getTree().setTreeChanging(true);
          //
          if (((JavaTransferObject) t).getLocalObject() instanceof ITreeNode) {
            ITreeNode dragNode = (ITreeNode) ((JavaTransferObject) t).getLocalObject();
            if (dragNode != dropNode && dragNode.getTree() == getTree()) {
              if (isFolderNode(dragNode)) {
                if (isBookmarkNode(dropNode)) {
                  dropNode = dropNode.getParentNode();
                }
              }
              //
              if (isBookmarkNode(dragNode) && isFolderNode(dropNode)) {
                //append to folder
                getTree().removeNode(dragNode);
                getTree().addChildNode(dropNode, dragNode);
                refreshBookmarkModel();
              }
              else if (isBookmarkNode(dragNode) && isBookmarkNode(dropNode)) {
                //insert before dropNode
                getTree().removeNode(dragNode);
                int pos = dropNode.getChildNodeIndex();
                getTree().addChildNode(pos, dropNode.getParentNode(), dragNode);
              }
              else if (isFolderNode(dragNode) && dropNode == null) {
                //move to top
                getTree().removeNode(dragNode);
                getTree().addChildNode(getTree().getRootNode(), dragNode);
              }
              else if (isFolderNode(dragNode) && isFolderNode(dropNode)) {
                //append to folder, NOTE: the drag node may be an ancestor of the drop node!
                if (getTree().isAncestorNodeOf(dragNode, dropNode)) {
                  ITreeNode dragParent = dragNode.getParentNode();
                  if (dragParent != null) {
                    int dragPos = dragNode.getChildNodeIndex();
                    ITreeNode dropAncestor = dropNode;
                    while (dropAncestor.getParentNode() != dragNode) {
                      dropAncestor = dropAncestor.getParentNode();
                    }
                    getTree().removeNode(dropAncestor);
                    getTree().removeNode(dragNode);
                    getTree().addChildNode(dragPos, dragParent, dropAncestor);
                    getTree().addChildNode(dropNode, dragNode);
                  }
                }
                else {
                  getTree().removeNode(dragNode);
                  getTree().addChildNode(dropNode, dragNode);
                }
              }
            }
          }
        }
        catch (ProcessingException e) {
          e.printStackTrace();
        }
        finally {
          getTree().setTreeChanging(false);
        }
        try {
          rebuildBookmarkModel();
        }
        catch (ProcessingException e) {
          LOG.error(null, e);
        }
      }
    }

    private void addNewFolder(ITreeNode parentNode) throws ProcessingException {
      if (parentNode == null) parentNode = getRootNode();
      BookmarkFolderForm form = new BookmarkFolderForm();
      form.startModify();
      form.waitFor();
      if (form.isFormStored()) {
        ITreeNode newNode = new FolderNode();
        BookmarkFolder bmFolder = new BookmarkFolder();
        bmFolder.setTitle(form.getNameField().getValue());
        newNode.getCellForUpdate().setValue(bmFolder);
        //append after last folder
        ITreeNode lastFolderNode = null;
        for (ITreeNode tmp : parentNode.getChildNodes()) {
          if (isFolderNode(tmp)) {
            lastFolderNode = tmp;
          }
          else {
            break;
          }
        }
        int pos = lastFolderNode != null ? lastFolderNode.getChildNodeIndex() + 1 : 0;
        getTree().addChildNode(pos, parentNode, newNode);
        rebuildBookmarkModel();
      }
    }

    @Order(10)
    public class NewRootFolderMenu extends AbstractMenu {
      @Override
      protected String getConfiguredText() {
        return ScoutTexts.get("NewBookmarkFolderMenu");
      }

      @Override
      protected boolean getConfiguredSingleSelectionAction() {
        return false;
      }

      @Override
      protected boolean getConfiguredEmptySpaceAction() {
        return true;
      }

      @Override
      protected void execPrepareAction() {
        setVisiblePermission(getUpdatePermission());
      }

      @Override
      protected void execAction() throws ProcessingException {
        addNewFolder(null);
      }
    }

    @Order(11)
    public class NewChildFolderMenu extends AbstractMenu {
      @Override
      protected String getConfiguredText() {
        return ScoutTexts.get("NewBookmarkFolderMenu");
      }

      @Override
      protected void execPrepareAction() {
        setVisiblePermission(getUpdatePermission());
      }

      @Override
      protected void execAction() throws ProcessingException {
        addNewFolder(getSelectedNode());
      }
    }

    @Order(20)
    public class Separator1Menu extends MenuSeparator {
    }

    @Order(40)
    public class DeleteKeyStroke extends AbstractKeyStroke {
      @Override
      protected String getConfiguredKeyStroke() {
        return "DELETE";
      }

      @Override
      protected void execAction() throws ProcessingException {
        ITreeNode node = getSelectedNode();
        if (node != null) {
          for (IMenu m : node.getMenus()) {
            if (m.getClass().getSimpleName().equals("DeleteMenu")) {
              m.prepareAction();
              if (m.isVisible() && m.isEnabled()) {
                m.doAction();
              }
              break;
            }
          }
        }
      }
    }

  }

  private class FolderNode extends AbstractTreeNode {
    @Override
    protected void execDecorateCell(Cell cell) {
      BookmarkFolder bmFolder = (BookmarkFolder) getCell().getValue();
      String title = bmFolder.getTitle();
      if (Bookmark.INBOX_FOLDER_NAME.equals(title)) {
        title = ScoutTexts.get("GlobalBookmarks");
      }
      cell.setText(title);
      cell.setIconId(AbstractIcons.Folder);
    }

    @Order(30)
    public class RenameMenu extends AbstractMenu {
      @Override
      protected String getConfiguredText() {
        return ScoutTexts.get("RenameBookmarkFolderMenu");
      }

      @Override
      protected void execPrepareAction() {
        setVisiblePermission(getUpdatePermission());
      }

      @Override
      protected void execAction() throws ProcessingException {
        ITreeNode node = FolderNode.this;
        BookmarkFolder bmFolder = (BookmarkFolder) node.getCell().getValue();
        BookmarkFolderForm form = new BookmarkFolderForm();
        form.getNameField().setValue(bmFolder.getTitle());
        form.startModify();
        form.waitFor();
        if (form.isFormStored()) {
          bmFolder.setTitle(form.getNameField().getValue());
          getTree().updateNode(node);
          refreshBookmarkModel();
        }
      }
    }

    @Order(40)
    public class DeleteMenu extends AbstractMenu {
      @Override
      protected String getConfiguredText() {
        return ScoutTexts.get("DeleteFolderMenu");
      }

      @Override
      protected boolean getConfiguredMultiSelectionAction() {
        return true;
      }

      @Override
      protected void execPrepareAction() {
        setVisiblePermission(getDeletePermission());
      }

      @Override
      protected void execAction() throws ProcessingException {
        ITree tree = getTree();
        ArrayList<String> items = new ArrayList<String>();
        ArrayList<ITreeNode> filteredNodes = new ArrayList<ITreeNode>();
        for (ITreeNode node : tree.getSelectedNodes()) {
          if (isFolderNode(node)) {
            items.add(node.getCell().getText());
            filteredNodes.add(node);
          }
        }
        if (items.size() <= 1 || MessageBox.showDeleteConfirmationMessage(items.toArray(new String[0]))) {
          for (ITreeNode node : filteredNodes) {
            tree.removeNode(node);
          }
          rebuildBookmarkModel();
        }
      }
    }
  }

  private class BookmarkNode extends AbstractTreeNode {
    @Override
    protected boolean getConfiguredLeaf() {
      return true;
    }

    @Override
    protected void execDecorateCell(Cell cell) {
      Bookmark bm = (Bookmark) cell.getValue();
      if (bm != null) {
        if (bm.getKeyStroke() != null) {
          cell.setText(bm.getTitle() + " [" + bm.getKeyStroke() + "]");
        }
        else {
          cell.setText(bm.getTitle());
        }
        cell.setTooltipText(bm.getText());
        cell.setIconId(AbstractIcons.Bookmark);
      }
    }

    @Order(40)
    public class OpenMenu extends AbstractMenu {
      @Override
      protected String getConfiguredText() {
        return ScoutTexts.get("ActivateBookmarkMenu");
      }

      @Override
      protected void execAction() throws ProcessingException {
        ITreeNode node = BookmarkNode.this;
        Bookmark bm = (Bookmark) node.getCell().getValue();
        ClientSyncJob.getCurrentSession().getDesktop().activateBookmark(bm, false);
      }
    }

    @Order(50)
    public class EditMenu extends AbstractMenu {
      @Override
      protected String getConfiguredText() {
        return ScoutTexts.get("EditBookmarkMenu");
      }

      @Override
      protected void execPrepareAction() {
        setVisiblePermission(getUpdatePermission());
      }

      @Override
      protected void execAction() throws ProcessingException {
        ITreeNode node = BookmarkNode.this;
        Bookmark bm = (Bookmark) node.getCell().getValue();
        BookmarkForm form = new BookmarkForm();
        form.setBookmarkRootFolder(getBookmarkRootFolder());
        form.getTitleField().setValue(bm.getTitle());
        form.getKeyStrokeField().setValue(bm.getKeyStroke());
        form.getDescriptionField().setValue(bm.getText());
        BookmarkFolder oldBmFolder = getParentBookmarkFolder(BookmarkNode.this);
        if (oldBmFolder != form.getBookmarkRootFolder()) {
          form.getFolderField().setValue(oldBmFolder);
        }
        form.startModify();
        form.waitFor();
        if (form.isFormStored()) {
          ITree tree = getTree();
          bm.setTitle(form.getTitleField().getValue());
          bm.setKeyStroke(form.getKeyStrokeField().getValue());
          final BookmarkFolder newBmFolder = form.getFolderField().getValue() != null ? form.getFolderField().getValue() : form.getBookmarkRootFolder();
          if (!CompareUtility.equals(oldBmFolder, newBmFolder)) {
            //find new folder node
            final AtomicReference<ITreeNode> newContainerNode = new AtomicReference<ITreeNode>(getTree().getRootNode());
            tree.visitTree(new ITreeVisitor() {
              public boolean visit(ITreeNode n) {
                if (isFolderNode(n) && n.getCell().getValue() == newBmFolder) {
                  newContainerNode.set(n);
                  return false;
                }
                return true;
              }
            });
            tree.removeNode(node);
            tree.addChildNode(newContainerNode.get(), node);
            rebuildBookmarkModel();
          }
          else {
            tree.updateNode(node);
            refreshBookmarkModel();
          }
        }
      }
    }

    @Order(60)
    public class DeleteMenu extends AbstractMenu {
      @Override
      protected String getConfiguredText() {
        return ScoutTexts.get("DeleteBookmarkMenu");
      }

      @Override
      protected boolean getConfiguredMultiSelectionAction() {
        return true;
      }

      @Override
      protected void execPrepareAction() {
        setVisiblePermission(getDeletePermission());
      }

      @Override
      protected void execAction() throws ProcessingException {
        ArrayList<String> items = new ArrayList<String>();
        ArrayList<ITreeNode> filteredNodes = new ArrayList<ITreeNode>();
        ITree tree = getTree();
        for (ITreeNode node : tree.getSelectedNodes()) {
          if (isBookmarkNode(node)) {
            items.add(node.getCell().getText());
            filteredNodes.add(node);
          }
        }
        if (items.size() <= 1 || MessageBox.showDeleteConfirmationMessage(items.toArray(new String[0]))) {
          for (ITreeNode node : filteredNodes) {
            tree.removeNode(node);
          }
          rebuildBookmarkModel();
        }
      }
    }

    @Order(70)
    public class SeparatorMenu1 extends MenuSeparator {
    }

    @Order(80)
    public class PublishMenu extends AbstractMenu {
      @Override
      protected String getConfiguredText() {
        return ScoutTexts.get("BookmarksPublishMenu");
      }

      @Override
      protected void execPrepareAction() throws ProcessingException {
        setVisiblePermission(new PublishUserBookmarkPermission());
      }

      @Override
      protected void execAction() throws ProcessingException {
        if (m_publishBookmarkCommand != null) {
          ITreeNode node = BookmarkNode.this;
          Bookmark bm = (Bookmark) node.getCell().getValue();
          BookmarkFolder spoolFolder = new BookmarkFolder();
          spoolFolder.getBookmarks().add(bm);
          m_publishBookmarkCommand.publishBookmark(spoolFolder);
        }
      }
    }

    @Order(90)
    public class ApplyToSearchMenu extends AbstractMenu {
      @Override
      protected String getConfiguredText() {
        return ScoutTexts.get("BookmarksApplyToCurrentSearch");
      }

      @Override
      protected void execAction() throws ProcessingException {
        ITreeNode node = BookmarkNode.this;
        Bookmark bm = (Bookmark) node.getCell().getValue();
        IDesktop desktop = ClientSyncJob.getCurrentSession().getDesktop();
        Boolean success = null;
        if (desktop != null) {
          IForm searchForm = desktop.getPageSearchForm();
          if (searchForm != null) {
            for (AbstractPageState state : bm.getPath()) {
              if (state instanceof TablePageState) {
                TablePageState tablePageState = (TablePageState) state;
                try {
                  searchForm.setXML(tablePageState.getSearchFormState());
                  searchForm.doSaveWithoutMarkerChange();
                  success = true;
                  break;
                }
                catch (ProcessingException e) {
                  success = false;
                }
              }
            }
          }
        }
        if (success != null && !success) {
          MessageBox.showOkMessage(null, null, ScoutTexts.get("ApplyBookmarkToSearchFailedMessage"));
        }
      }
    }

  }

}
