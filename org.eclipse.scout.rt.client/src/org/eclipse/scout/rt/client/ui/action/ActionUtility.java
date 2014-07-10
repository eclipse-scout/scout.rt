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
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.BooleanHolder;
import org.eclipse.scout.rt.client.ui.action.menu.ActivityMapMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.CalendarMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TreeMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.ValueFieldMenuType;
import org.eclipse.scout.rt.client.ui.action.tree.IActionNode;
import org.eclipse.scout.rt.client.ui.basic.activitymap.ActivityCell;
import org.eclipse.scout.rt.client.ui.basic.calendar.CalendarComponent;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;

/**
 *
 */
public final class ActionUtility {

  public static IActionFilter FALSE_FILTER = new IActionFilter() {
    @Override
    public boolean accept(IAction action) {
      return false;
    }
  };

  public static IActionFilter TRUE_FILTER = new IActionFilter() {
    @Override
    public boolean accept(IAction action) {
      return true;
    }
  };

  /**
   * Removes invisible actions. Also removes leading and trailing separators as well as multiple consecutive separators.
   * 
   * @since 3.8.1
   */
  public static <T extends IAction> List<T> visibleNormalizedActions(List<T> actionNodes) {
    return normalizedActions(actionNodes, createVisibleFilter());
  }

  public static <T extends IAction> List<T> normalizedActions(List<T> actionNodes, IActionFilter filter) {
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

  public static IActionFilter createMenuFilterForActivityMapSelection(ActivityCell<?, ?> selectedCell) {
    final ActivityMapMenuType menuType;
    if (selectedCell == null) {
      menuType = ActivityMapMenuType.Selection;
    }
    else {
      menuType = ActivityMapMenuType.Activity;
    }
    return createMenuFilterMenuTypes(CollectionUtility.hashSet(menuType));

  }

  public static IActionFilter createMenuFilterForCalendarSelection(CalendarComponent selectedComponent) {
    final CalendarMenuType menuType;
    if (selectedComponent == null) {
      menuType = CalendarMenuType.EmptySpace;
    }
    else {
      menuType = CalendarMenuType.CalendarComponent;
    }
    return createMenuFilterMenuTypes(CollectionUtility.hashSet(menuType));

  }

  public static IActionFilter createMenuFilterForTableSelection(List<? extends ITableRow> selection) {
    boolean allEnabled = true;
    if (!CollectionUtility.isEmpty(selection)) {
      allEnabled = true;
      for (ITableRow n : selection) {
        if (!n.isEnabled()) {
          allEnabled = false;
          break;
        }
      }
    }
    if (allEnabled) {
      final TableMenuType menuType;
      if (CollectionUtility.isEmpty(selection)) {
        menuType = TableMenuType.EmptySpace;
      }
      else if (CollectionUtility.size(selection) == 1) {
        menuType = TableMenuType.SingleSelection;
      }
      else {
        menuType = TableMenuType.MultiSelection;
      }
      return createMenuFilterMenuTypes(CollectionUtility.hashSet(menuType));
    }
    else {
      return FALSE_FILTER;
    }
  }

  public static IActionFilter createMenuFilterForValueFieldValue(Object value) {
    final ValueFieldMenuType menuType;
    if (value == null) {
      menuType = ValueFieldMenuType.Null;
    }
    else {
      menuType = ValueFieldMenuType.NotNull;
    }
    return createMenuFilterMenuTypes(menuType);
  }

  public static IActionFilter createVisibleFilter() {
    return new IActionFilter() {

      @Override
      public boolean accept(IAction action) {
        return action.isVisible();
      }
    };
  }

  public static IActionFilter createMenuFilterForTreeSelection(Set<? extends ITreeNode> selection) {
    boolean allEnabled = true;
    if (!CollectionUtility.isEmpty(selection)) {
      allEnabled = true;
      for (ITreeNode n : selection) {
        if (!n.isEnabled()) {
          allEnabled = false;
          break;
        }
      }
    }
    if (allEnabled) {
      final TreeMenuType menuType;
      if (CollectionUtility.isEmpty(selection)) {
        menuType = TreeMenuType.EmptySpace;
      }
      else if (CollectionUtility.size(selection) == 1) {
        menuType = TreeMenuType.SingleSelection;
      }
      else {
        menuType = TreeMenuType.MultiSelection;
      }
      return createMenuFilterMenuTypes(CollectionUtility.hashSet(menuType));
    }
    else {
      return FALSE_FILTER;
    }
  }

  public static IActionFilter createMenuFilterVisibleAndMenuTypes(IMenuType... menuTypes) {
    return createMenuFilterVisibleAndMenuTypes(CollectionUtility.hashSet(menuTypes));
  }

  public static IActionFilter createMenuFilterVisibleAndMenuTypes(final Set<? extends IMenuType> menuTypes) {
    return new IActionFilter() {

      @Override
      public boolean accept(IAction action) {
        if (action.isVisible() && action instanceof IMenu) {
          IMenu menu = (IMenu) action;
          for (IMenuType t : menuTypes) {
            if (menu.getMenuTypes().contains(t)) {
              return true;
            }
          }
        }
        return false;
      }
    };
  }

  public static IActionFilter createMenuFilterMenuTypes(IMenuType... menuTypes) {
    return createMenuFilterMenuTypes(CollectionUtility.hashSet(menuTypes));

  }

  public static IActionFilter createMenuFilterMenuTypes(final Set<? extends IMenuType> menuTypes) {
    return new IActionFilter() {

      @Override
      public boolean accept(IAction action) {
        if (action instanceof IMenu) {
          IMenu menu = (IMenu) action;
          for (IMenuType t : menuTypes) {
            if (menu.getMenuTypes().contains(t)) {
              return true;
            }
          }
        }
        return false;
      }
    };
  }

  public static IActionFilter createCombinedFilter(final IActionFilter... actionFilters) {
    if (actionFilters != null) {
      return new IActionFilter() {

        @Override
        public boolean accept(IAction action) {
          for (IActionFilter f : actionFilters) {
            if (!f.accept(action)) {
              return false;
            }
          }
          return true;
        }
      };
    }
    return TRUE_FILTER;
  }

}
