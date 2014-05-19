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
package org.eclipse.scout.rt.client.ui.action;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.BooleanHolder;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.ITableMenu;
import org.eclipse.scout.rt.client.ui.action.menu.ITreeMenu;
import org.eclipse.scout.rt.client.ui.action.tree.IActionNode;

/**
 *
 */
public final class ActionUtility {

  /**
   * Removes invisible actions. Also removes leading and trailing separators as well as multiple consecutive separators.
   * 
   * @since 3.8.1
   */
  public static <T extends IAction> List<T> visibleNormalizedActions(List<T> actionNodes) {
    return visibleNormalizedActions(actionNodes, createVisibleFilter());
  }

  public static <T extends IAction> List<T> visibleNormalizedActions(List<T> actionNodes, IActionFilter filter) {
    if (actionNodes == null) {
      return CollectionUtility.emptyArrayList();
    }

    // only visible
    List<T> cleanedActions = getActions(actionNodes, filter);

    normalizeSeparators(cleanedActions);
    return cleanedActions;
  }


  public static <T extends IAction> void normalizeSeparators(List<T> actions) {
    // remove multiple and leading separators
    T prevSeparator = null;
    T prevAction = null;
    ListIterator<T> it = actions.listIterator();
    while (it.hasNext()) {
      T actionNode = it.next();

      if (actionNode.isSeparator()) {
        if (prevAction == null || prevSeparator != null) {
          // remove leading
          it.remove();
        }
        prevAction = null;
        prevSeparator = actionNode;
      }
      else {
        prevSeparator = null;
        prevAction = actionNode;
      }
    }
    // remove ending separators
    while (it.hasPrevious()) {
      T previous = it.previous();
      if (previous.isSeparator()) {
        it.remove();

      }
      else {
        break;
      }
    }
  }

  public static <T extends IAction> List<T> getActions(List<T> actions, final IActionFilter filter) {

    if (actions != null) {
      List<T> result = new ArrayList<T>(actions.size());
      for (T a : actions) {
        // action nodes with subnodes are only visible when at least one leaf is visible
        if (a instanceof IActionNode<?>) {
          final BooleanHolder visibleHolder = new BooleanHolder(false);
          a.acceptVisitor(new IActionVisitor() {
            @Override
            public int visit(IAction action) {
              if (action instanceof IActionNode) {
                if (((IActionNode) action).hasChildActions()) {
                  return CONTINUE;
                }
              }
              if (action.isSeparator()) {
                return CONTINUE;
              }
              if (filter.accept(action)) {
                visibleHolder.setValue(true);
                return CANCEL;

              }
              return CONTINUE;
            }
          });
          if (a.isSeparator() || visibleHolder.getValue()) {
            result.add(a);
          }
        }
        else if (a.isSeparator()) {
          result.add(a);
        }
        else if (filter.accept(a)) {
          result.add(a);
        }
      }
      return result;
    }
    return CollectionUtility.emptyArrayList();
  }

  public static void initActions(List<? extends IAction> actions) throws ProcessingException {
    InitActionVisitor v = new InitActionVisitor();
    for (IAction a : actions) {
      a.acceptVisitor(v);
    }
    v.handleResult();
  }

  private static class InitActionVisitor implements IActionVisitor {
    private ProcessingException m_firstEx;

    @Override
    public int visit(IAction action) {
      try {
        action.initAction();
      }
      catch (ProcessingException e) {
        if (m_firstEx == null) {
          m_firstEx = e;
        }
      }
      catch (Throwable t) {
        if (m_firstEx == null) {
          m_firstEx = new ProcessingException("Unexpected", t);
        }
      }
      return CONTINUE;
    }

    public void handleResult() throws ProcessingException {
      if (m_firstEx != null) {
        throw m_firstEx;
      }
    }
  }

  public static IActionFilter createMenuFilterVisibleAndMenuTypes(final EnumSet<ITableMenu.TableMenuType> types) {
    return new IActionFilter() {

      @SuppressWarnings("deprecation")
      @Override
      public boolean accept(IAction action) {
        if (action.isVisible()) {
          if (action instanceof ITableMenu) {
            ITableMenu tableMenu = (ITableMenu) action;
            for (ITableMenu.TableMenuType t : types) {
              if (tableMenu.getMenuType().contains(t)) {
                return true;
              }
            }
          }
          else if (action instanceof IMenu) {
            IMenu menu = (IMenu) action;
            // legacy
            if (menu.isSingleSelectionAction() && types.contains(ITableMenu.TableMenuType.SingleSelection)) {
              return true;
            }
            if (menu.isMultiSelectionAction() && types.contains(ITableMenu.TableMenuType.MultiSelection)) {
              return true;
            }
            if (menu.isEmptySpaceAction() && types.contains(ITableMenu.TableMenuType.EmptySpace)) {
              return true;
            }
          }
        }
        return false;
      }
    };
  }

  public static IActionFilter createMenuFilterMenuTypes(final EnumSet<ITableMenu.TableMenuType> types) {
    return new IActionFilter() {

      @SuppressWarnings("deprecation")
      @Override
      public boolean accept(IAction action) {
        if (action instanceof ITableMenu) {
          ITableMenu tableMenu = (ITableMenu) action;
          for (ITableMenu.TableMenuType t : types) {
            if (tableMenu.getMenuType().contains(t)) {
              return true;
            }
          }
        }
        else if (action instanceof IMenu) {
          IMenu menu = (IMenu) action;
          // legacy
          if (menu.isSingleSelectionAction() && types.contains(ITableMenu.TableMenuType.SingleSelection)) {
            return true;
          }
          if (menu.isMultiSelectionAction() && types.contains(ITableMenu.TableMenuType.MultiSelection)) {
            return true;
          }
          if (menu.isEmptySpaceAction() && types.contains(ITableMenu.TableMenuType.EmptySpace)) {
            return true;
          }
        }
        return false;
      }
    };
  }

  public static IActionFilter createTreeMenuFilterMenuTypes(final EnumSet<ITreeMenu.TreeMenuType> types) {
    return new IActionFilter() {

      @SuppressWarnings("deprecation")
      @Override
      public boolean accept(IAction action) {
        if (action instanceof ITableMenu) {
          ITreeMenu tableMenu = (ITreeMenu) action;
          for (ITreeMenu.TreeMenuType t : types) {
            if (tableMenu.getMenuType().contains(t)) {
              return true;
            }
          }
        }
        else if (action instanceof IMenu) {
          IMenu menu = (IMenu) action;
          // legacy
          if (menu.isSingleSelectionAction() && types.contains(ITreeMenu.TreeMenuType.SingleSelection)) {
            return true;
          }
          if (menu.isMultiSelectionAction() && types.contains(ITreeMenu.TreeMenuType.MultiSelection)) {
            return true;
          }
          if (menu.isEmptySpaceAction() && types.contains(ITreeMenu.TreeMenuType.EmptySpace)) {
            return true;
          }
        }
        return false;
      }
    };
  }

  public static IActionFilter createTreeMenuFilterVisibleAndMenuTypes(final EnumSet<ITreeMenu.TreeMenuType> types) {
    return new IActionFilter() {

      @SuppressWarnings("deprecation")
      @Override
      public boolean accept(IAction action) {
        if (action.isVisible()) {
          if (action instanceof ITreeMenu) {
            ITreeMenu tableMenu = (ITreeMenu) action;
            for (ITreeMenu.TreeMenuType t : types) {
              if (tableMenu.getMenuType().contains(t)) {
                return true;
              }
            }
          }
          else if (action instanceof IMenu) {
            IMenu menu = (IMenu) action;
            // legacy
            if (menu.isSingleSelectionAction() && types.contains(ITreeMenu.TreeMenuType.SingleSelection)) {
              return true;
            }
            if (menu.isMultiSelectionAction() && types.contains(ITreeMenu.TreeMenuType.MultiSelection)) {
              return true;
            }
            if (menu.isEmptySpaceAction() && types.contains(ITreeMenu.TreeMenuType.EmptySpace)) {
              return true;
            }
          }
        }
        return false;
      }
    };
  }

  public static IActionFilter createVisibleFilter() {
    return new IActionFilter() {
      @Override
      public boolean accept(IAction action) {
        return action.isVisible();
      }
    };
  }

  public static IActionFilter createMenuFilterVisibleAvailable() {
    return new IActionFilter() {
      @Override
      public boolean accept(IAction action) {
        return action.isVisible() && action instanceof IMenu && ((IMenu) action).isAvailable();
      }
    };
  }

  public static IActionFilter createMenuFilterAvailable() {
    return new IActionFilter() {
      @Override
      public boolean accept(IAction action) {
        return action instanceof IMenu && ((IMenu) action).isAvailable();
      }
    };
  }
}
