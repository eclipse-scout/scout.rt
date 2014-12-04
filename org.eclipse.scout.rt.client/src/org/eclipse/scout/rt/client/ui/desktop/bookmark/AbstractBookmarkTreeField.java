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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.dnd.JavaTransferObject;
import org.eclipse.scout.commons.dnd.TransferObject;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.services.common.bookmark.IBookmarkService;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenuSeparator;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TreeMenuType;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTree;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeVisitor;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.bookmark.view.IOpenBookmarkCommand;
import org.eclipse.scout.rt.client.ui.desktop.bookmark.view.IPublishBookmarkCommand;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.treefield.AbstractTreeField;
import org.eclipse.scout.rt.client.ui.messagebox.MessageBox;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.shared.security.PublishUserBookmarkPermission;
import org.eclipse.scout.rt.shared.services.common.bookmark.AbstractPageState;
import org.eclipse.scout.rt.shared.services.common.bookmark.Bookmark;
import org.eclipse.scout.rt.shared.services.common.bookmark.BookmarkFolder;
import org.eclipse.scout.rt.shared.services.common.bookmark.TablePageState;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.eclipse.scout.service.SERVICES;

public abstract class AbstractBookmarkTreeField extends AbstractTreeField {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractBookmarkTreeField.class);

  private BookmarkFolder m_bookmarkRootFolder;
  private IPublishBookmarkCommand m_publishBookmarkCommand;
  private IOpenBookmarkCommand m_openBookmarkCommand;

  public AbstractBookmarkTreeField() {
    this(true);
  }

  public AbstractBookmarkTreeField(boolean callInitializer) {
    super(callInitializer);
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

  /**
   * It's up to the Eclipse Scout implementation to handle bookmark publishing,
   * e.g. selection of receivers of the published bookmark
   *
   * @param command
   *          the command to publish a bookmark
   */
  public void injectPublishBookmarkCommand(IPublishBookmarkCommand command) {
    m_publishBookmarkCommand = command;
  }

  /**
   * If an application needs to execute additional code when a bookmark is opened,
   * it can inject a command here.
   * Please note: The command should at least open the bookmark, by calling {@link IDesktop#activateBookmark()}
   *
   * @param command
   *          the command to be executed when a user opens a bookmark
   */
  public void injectOpenBookmarkCommand(IOpenBookmarkCommand command) {
    m_openBookmarkCommand = command;
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

  @ConfigProperty(ConfigProperty.TEXT)
  @Order(10)
  protected String getConfiguredGlobalBookmarkLabel() {
    return ScoutTexts.get("GlobalBookmarks");
  }

  @ConfigProperty(ConfigProperty.TEXT)
  @Order(20)
  protected String getConfiguredPrivateBookmarkLabel() {
    return ScoutTexts.get("PrivateBookmarks");
  }

  @ConfigProperty(ConfigProperty.FORM)
  @Order(30)
  protected Class<? extends IBookmarkForm> getConfiguredBookmarkForm() {
    return BookmarkForm.class;
  }

  /**
   * @param bookmarks
   *          selected for deletion
   * @return the row-level permission to delete bookmarks, default is {@link getDeletePermission()}
   * @throws ProcessingException
   */
  protected Permission getDeletePermission(List<Bookmark> bookmarks) throws ProcessingException {
    return getDeletePermission();
  }

  /**
   * @param bookmarks
   *          selected for update
   * @return the row-level permission to update bookmarks, default is {@link getUpdatePermission()}
   * @throws ProcessingException
   */
  protected Permission getUpdatePermission(List<Bookmark> bookmarks) throws ProcessingException {
    return getUpdatePermission();
  }

  /**
   * @param bookmark
   *          selected for publishing
   * @return the row-level permission to publish this bookmark, default is {@link getPublishPermission()}
   * @throws ProcessingException
   */
  protected Permission getPublishPermission(Bookmark bookmark) throws ProcessingException {
    return getPublishPermission();
  }

  protected Permission getDeletePermission() {
    return null;
  }

  protected Permission getUpdatePermission() {
    return null;
  }

  protected Permission getPublishPermission() {
    return new PublishUserBookmarkPermission();
  }

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
      @Override
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
            @Override
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
      @Override
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
        ClientSyncJob.getCurrentSession().getDesktop().activateBookmark(bm);
      }
    }

    @Override
    protected TransferObject execDrag(Collection<ITreeNode> nodes) throws ProcessingException {
      if (ACCESS.check(getUpdatePermission())) {
        return new JavaTransferObject(nodes);
      }
      return null;
    }

    @Override
    protected void execDrop(ITreeNode dropNode, TransferObject transfer) {
      if (transfer instanceof JavaTransferObject) {
        try {
          getTree().setTreeChanging(true);
          //
          List<ITreeNode> elements = ((JavaTransferObject) transfer).getLocalObjectAsList(ITreeNode.class);
          if (CollectionUtility.hasElements(elements)) {
            boolean updateTree = false;
            HashSet<ITreeNode> draggedFolders = new HashSet<ITreeNode>();
            for (ITreeNode source : elements) {
              if (source != dropNode && source.getTree() == getTree()) {
                ITreeNode target = dropNode;
                if (isFolderNode(source)) {
                  if (isBookmarkNode(target)) {
                    target = target.getParentNode();
                  }
                }
                //
                if (isBookmarkNode(source) && isFolderNode(target)) {
                  //append to folder
                  getTree().removeNode(source);
                  getTree().addChildNode(target, source);
                  updateTree = true;
                }
                else if (isBookmarkNode(source) && isBookmarkNode(target)) {
                  //insert before dropNode
                  getTree().removeNode(source);
                  int pos = target.getChildNodeIndex();
                  getTree().addChildNode(pos, target.getParentNode(), source);
                }
                else if (isFolderNode(source) && target == null) {
                  //move to top
                  getTree().removeNode(source);
                  getTree().addChildNode(getTree().getRootNode(), source);
                }
                else if (isFolderNode(source) && isFolderNode(target)) {
                  boolean parentWasDragged = false;
                  for (ITreeNode parent : draggedFolders) {
                    if (getTree().isAncestorNodeOf(parent, source)) {
                      parentWasDragged = true;
                      draggedFolders.add(source);
                    }
                  }

                  if (!parentWasDragged) {
                    //append to folder, NOTE: the drag node may be an ancestor of the drop node!
                    if (getTree().isAncestorNodeOf(source, target)) {
                      ITreeNode sourceParent = source.getParentNode();
                      if (sourceParent != null) {
                        int dragPos = source.getChildNodeIndex();
                        ITreeNode targetAncestor = target;
                        ITreeNode targetAncestorWalkThrough = target;
                        while (targetAncestorWalkThrough.getParentNode() != source) {
                          if (!elements.contains(targetAncestor.getParentNode())) {
                            targetAncestor = targetAncestorWalkThrough.getParentNode();
                          }
                          targetAncestorWalkThrough = targetAncestorWalkThrough.getParentNode();
                        }

                        getTree().removeNode(targetAncestor);
                        getTree().removeNode(source);
                        getTree().addChildNode(dragPos, sourceParent, targetAncestor);
                        getTree().addChildNode(target, source);
                      }
                    }
                    else {
                      getTree().removeNode(source);
                      getTree().addChildNode(target, source);
                    }
                  }
                }
              }
            }
            if (updateTree) {
              refreshBookmarkModel();
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
      if (parentNode == null) {
        parentNode = getRootNode();
      }
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
      protected Set<? extends IMenuType> getConfiguredMenuTypes() {
        return CollectionUtility.hashSet(TableMenuType.EmptySpace);
      }

      @Override
      protected void execInitAction() throws ProcessingException {
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
      protected void execInitAction() throws ProcessingException {
        setVisiblePermission(getUpdatePermission());
      }

      @Override
      protected void execOwnerValueChanged(Object newOwnerValue) throws ProcessingException {
        ITreeNode node = getSelectedNode();
        setVisible(!isBookmarkNode(node));
      }

      @Override
      protected void execAction() throws ProcessingException {
        addNewFolder(getSelectedNode());
      }
    }

    @Order(20)
    public class Separator1Menu extends AbstractMenuSeparator {

    }
  }

  private class FolderNode extends AbstractTreeNode {
    @Override
    protected void execDecorateCell(Cell cell) {
      BookmarkFolder bmFolder = (BookmarkFolder) getCell().getValue();
      String title = bmFolder.getTitle();
      if (Bookmark.INBOX_FOLDER_NAME.equals(title)) {
        title = getConfiguredGlobalBookmarkLabel();
      }
      cell.setText(title);
      cell.setIconId(AbstractIcons.TreeNode);
    }

    @Order(30)
    public class RenameMenu extends AbstractMenu {
      @Override
      protected String getConfiguredText() {
        return ScoutTexts.get("RenameBookmarkFolderMenu");
      }

      @Override
      protected void execInitAction() throws ProcessingException {
        setVisiblePermission(getUpdatePermission());
      }

      @Override
      protected void execAboutToShow() throws ProcessingException {
        setEnabled(!isProtected());
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
      protected String getConfiguredKeyStroke() {
        return "delete";
      }

      @Override
      protected Set<? extends IMenuType> getConfiguredMenuTypes() {
        return CollectionUtility.hashSet(TreeMenuType.SingleSelection, TreeMenuType.MultiSelection);
      }

      @Override
      protected void execInitAction() throws ProcessingException {
        setVisiblePermission(getDeletePermission());
      }

      @Override
      protected void execAboutToShow() throws ProcessingException {
        setEnabled(!isProtected());
        setText(getConfiguredText());
        for (ITreeNode node : getTree().getSelectedNodes()) {
          if (!(node instanceof FolderNode)) {
            setText(ScoutTexts.get("DeleteMenu"));
          }
        }
      }

      @Override
      protected void execAction() throws ProcessingException {
        ITree tree = getTree();
        Set<ITreeNode> folders = new HashSet<ITreeNode>();
        Set<ITreeNode> bookmarks = new HashSet<ITreeNode>();
        for (ITreeNode node : tree.getSelectedNodes()) {
          addNodeFoldersToSet(folders, node);
          addNodeBookmarksToSet(bookmarks, node);
        }
        ArrayList<String> names = new ArrayList<String>();
        for (ITreeNode folder : folders) {
          names.add(((BookmarkFolder) folder.getCell().getValue()).getTitle());
        }
        for (ITreeNode bookmark : bookmarks) {
          ArrayList<Bookmark> check = new ArrayList<Bookmark>();
          check.add(((Bookmark) bookmark.getCell().getValue()));
          if (ACCESS.check(getDeletePermission(check))) {
            names.add(((Bookmark) bookmark.getCell().getValue()).getTitle());
          }
        }
        if (MessageBox.showDeleteConfirmationMessage(names.toArray(new String[names.size()]))) {
          // delete bookmarks
          for (ITreeNode bookmark : bookmarks) {
            tree.removeNode(bookmark);
          }
          // delete folders
          for (ITreeNode bookmark : folders) {
            tree.removeNode(bookmark);
          }
          refreshBookmarkModel();
        }
      }

      private void addNodeBookmarksToSet(Set<ITreeNode> items, ITreeNode node) {
        if (isFolderNode(node)) {
          for (ITreeNode child : node.getChildNodes()) {
            addNodeBookmarksToSet(items, child);
          }
        }
        else if (isBookmarkNode(node)) {
          items.add(node);
        }
      }

      private void addNodeFoldersToSet(Set<ITreeNode> items, ITreeNode node) {
        if (isFolderNode(node)) {
          items.add(node);
          for (ITreeNode child : node.getChildNodes()) {
            addNodeFoldersToSet(items, child);
          }
        }
      }

    }

    private boolean isProtected() {
      ITreeNode node = FolderNode.this;
      BookmarkFolder bmFolder = (BookmarkFolder) node.getCell().getValue();
      if (Bookmark.INBOX_FOLDER_NAME.equals(bmFolder.getTitle())
          || Bookmark.SPOOL_FOLDER_NAME.equals(bmFolder.getTitle())) {
        return true;
      }
      return false;
    }

  }

  private class BookmarkNode extends AbstractTreeNode implements IBookmarkNode {
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
        if (StringUtility.isNullOrEmpty(bm.getIconId())) {
          cell.setIconId(AbstractIcons.Bookmark);
        }
        else {
          cell.setIconId(bm.getIconId());
        }
        if (bm.isNew()) {
          cell.setFont(new FontSpec(null, FontSpec.STYLE_BOLD, 0));
        }
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
        if (m_openBookmarkCommand != null) {
          m_openBookmarkCommand.openBookmark(bm);
        }
        else {
          ClientSyncJob.getCurrentSession().getDesktop().activateBookmark(bm);
        }
      }
    }

    @Order(50)
    public class EditMenu extends AbstractMenu {
      @Override
      protected String getConfiguredText() {
        return ScoutTexts.get("EditBookmarkMenu");
      }

      @Override
      protected void execOwnerValueChanged(Object newOwnerValue) throws ProcessingException {
        super.execOwnerValueChanged(newOwnerValue);
      }

      @Override
      protected void execAboutToShow() throws ProcessingException {
        List<Bookmark> bookmarks = new ArrayList<Bookmark>();
        ITree tree = getTree();
        for (ITreeNode node : tree.getSelectedNodes()) {
          if (isBookmarkNode(node)) {
            bookmarks.add((Bookmark) node.getCell().getValue());
          }
        }
        setEnabledPermission(getUpdatePermission(bookmarks));
      }

      @Override
      protected void execAction() throws ProcessingException {
        ITreeNode node = BookmarkNode.this;
        Bookmark bm = (Bookmark) node.getCell().getValue();
        IBookmarkForm form = null;
        if (getConfiguredBookmarkForm() != null) {
          try {
            form = getConfiguredBookmarkForm().newInstance();
          }
          catch (Exception e) {
            SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("error creating instance of class '" + getConfiguredBookmarkForm().getName() + "'.", e));
          }
        }
        if (form == null) {
          form = new BookmarkForm();
        }
        form.setBookmark(bm);
        form.setBookmarkRootFolder(getBookmarkRootFolder());
        BookmarkFolder oldBmFolder = getParentBookmarkFolder(BookmarkNode.this);
        boolean oldIsNew = bm.isNew();
        if (oldBmFolder != form.getBookmarkRootFolder()) {
          form.setFolder(oldBmFolder);
        }
        form.startModify();
        form.waitFor();
        if (form.isFormStored()) {
          ITree tree = getTree();
          bm.setTitle(form.getBookmark().getTitle());
          bm.setKeyStroke(form.getBookmark().getKeyStroke());
          bm.setKind(form.getBookmark().getKind());
          bm.setIconId(form.getBookmark().getIconId());
          final BookmarkFolder newBmFolder = form.getFolder() != null ? form.getFolder() : form.getBookmarkRootFolder();
          if (!CompareUtility.equals(oldBmFolder, newBmFolder)) {
            //find new folder node
            final AtomicReference<ITreeNode> newContainerNode = new AtomicReference<ITreeNode>(getTree().getRootNode());
            tree.visitTree(new ITreeVisitor() {
              @Override
              public boolean visit(ITreeNode n) {
                if (isFolderNode(n) && newBmFolder.equals(n.getCell().getValue())) {
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
        else if (oldIsNew != form.getBookmark().isNew()) {
          refreshBookmarkModel();
        }
      }
    }

    @Order(60)
    public class UpdateWithCurrentMenu extends AbstractMenu {

      @Override
      protected String getConfiguredText() {
        return ScoutTexts.get("UpdateBookmarkMenu");
      }

      @Override
      protected void execAboutToShow() throws ProcessingException {
        // The permission to use the "update bookmark" function
        // is the same as to delete one:
        ArrayList<Bookmark> bookmarks = new ArrayList<Bookmark>();
        ITree tree = getTree();
        for (ITreeNode node : tree.getSelectedNodes()) {
          if (isBookmarkNode(node)) {
            bookmarks.add((Bookmark) node.getCell().getValue());
          }
        }
        setEnabledPermission(getDeletePermission(bookmarks));
      }

      @Override
      protected void execAction() throws ProcessingException {
        ITreeNode node = BookmarkNode.this;
        Bookmark bm = (Bookmark) node.getCell().getValue();

        IBookmarkService service = SERVICES.getService(IBookmarkService.class);
        service.updateBookmark(bm);
        service.storeBookmarks();
      }
    }

    @Order(70)
    public class DeleteMenu extends AbstractMenu {
      @Override
      protected String getConfiguredText() {
        return ScoutTexts.get("DeleteBookmarkMenu");
      }

      @Override
      protected String getConfiguredKeyStroke() {
        return "delete";
      }

      @Override
      protected Set<? extends IMenuType> getConfiguredMenuTypes() {
        return CollectionUtility.hashSet(TreeMenuType.SingleSelection, TreeMenuType.MultiSelection);
      }

      @Override
      protected void execAboutToShow() throws ProcessingException {
        List<Bookmark> bookmarks = new ArrayList<Bookmark>();
        ITree tree = getTree();
        for (ITreeNode node : tree.getSelectedNodes()) {
          if (isBookmarkNode(node)) {
            bookmarks.add((Bookmark) node.getCell().getValue());
          }
        }
        setEnabledPermission(getDeletePermission(bookmarks));
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

    @Order(80)
    public class SeparatorMenu1 extends AbstractMenuSeparator {
    }

    @Order(90)
    public class PublishMenu extends AbstractMenu {
      @Override
      protected String getConfiguredText() {
        return ScoutTexts.get("BookmarksPublishMenu");
      }

      @Override
      protected void execOwnerValueChanged(Object newOwnerValue) throws ProcessingException {
        Bookmark bookmark = null;
        ITreeNode selectedNode = getTree().getSelectedNode();
        if (isBookmarkNode(selectedNode)) {
          bookmark = (Bookmark) selectedNode.getCell().getValue();
        }
        setVisiblePermission(getPublishPermission(bookmark));
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

    @Order(100)
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

    @Override
    public Bookmark getBookmark() {
      ITreeNode node = BookmarkNode.this;
      return (Bookmark) node.getCell().getValue();
    }

    @Override
    public BookmarkFolder getParentFolder() {
      ITreeNode node = BookmarkNode.this;
      return (BookmarkFolder) node.getParentNode().getCell().getValue();
    }

  }
}
