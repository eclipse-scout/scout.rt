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
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.scout.commons.exception.ProcessingException;

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
    if (actionNodes == null) {
      return Collections.emptyList();
    }

    // only visible
    ArrayList<T> cleanedActions = getVisibleActions(actionNodes);

    // remove multiple and leading separators
    T prevSeparator = null;
    T prevAction = null;
    ListIterator<T> it = cleanedActions.listIterator();
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

    return cleanedActions;
  }

  public static <T extends IAction> ArrayList<T> getVisibleActions(List<T> actions) {
    ArrayList<T> result = new ArrayList<T>();
    if (actions != null) {
      for (T a : actions) {
        if (a.isVisible()) {
          result.add(a);
        }
      }
    }
    return result;
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

}
