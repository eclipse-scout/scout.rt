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
package org.eclipse.scout.rt.client.mobile.ui.form;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.scout.rt.client.mobile.ui.action.ActionButtonBarUtility;
import org.eclipse.scout.rt.client.mobile.ui.form.fields.button.IMobileButton;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts the system and custom process buttons of the main box to actions.
 * <p>
 * The custom process buttons will be placed on the right side, the placement of the system process buttons depend on
 * the system type (see {@link #getRelevantSystemTypesForLeftHeader()} and
 * {@link #getRelevantSystemTypesForRightHeader()})
 */
public class FormHeaderActionFetcher extends AbstractFormActionFetcher {
  private static final Logger LOG = LoggerFactory.getLogger(FormHeaderActionFetcher.class);

  public FormHeaderActionFetcher(IForm form) {
    super(form);
  }

  @Override
  public List<IMenu> fetch() {
    List<IMenu> formActions = new LinkedList<IMenu>();

    if (getForm().getRootGroupBox().getSystemProcessButtonCount() > 0) {
      List<IMobileAction> leftActions = createLeftActions();
      if (leftActions != null) {
        for (IMobileAction action : leftActions) {
          action.setHorizontalAlignment(IMobileAction.HORIZONTAL_ALIGNMENT_LEFT);
        }
        formActions.addAll(leftActions);
      }
    }
    if (getForm().getRootGroupBox().getSystemProcessButtonCount() > 0 || getForm().getRootGroupBox().getCustomProcessButtonCount() > 0) {
      List<IMobileAction> rightActions = createRightActions();
      if (rightActions != null) {
        for (IMobileAction action : rightActions) {
          action.setHorizontalAlignment(IMobileAction.HORIZONTAL_ALIGNMENT_RIGHT);
        }
      }
      formActions.addAll(rightActions);
    }

    return formActions;
  }

  /**
   * If there are multiple buttons with a matching system types the order given in the list is used to sort the buttons.
   */
  protected List<IMobileAction> convertSystemProcessButtons(final List<Integer> relevantSystemTypes) {
    if (relevantSystemTypes == null || relevantSystemTypes.size() == 0) {
      return null;
    }

    List<IButton> systemProcessButtons = getForm().getRootGroupBox().getSystemProcessButtons();
    if (!CollectionUtility.hasElements(systemProcessButtons)) {
      return null;
    }

    IButton[] array = systemProcessButtons.toArray(new IButton[systemProcessButtons.size()]);
    // sort
    Comparator<IButton> comparator = new Comparator<IButton>() {
      @Override
      public int compare(IButton button1, IButton button2) {
        int index1 = relevantSystemTypes.indexOf(button1.getSystemType());
        int index2 = relevantSystemTypes.indexOf(button2.getSystemType());
        if (index1 >= index2) {
          return 1;
        }
        else {
          return -1;
        }
      }
    };
    Arrays.sort(array, comparator);

    List<IMobileAction> sortedActions = new ArrayList<IMobileAction>(array.length);
    for (IButton scoutButton : array) {
      if (relevantSystemTypes.contains(scoutButton.getSystemType())) {
        try {
          sortedActions.add(ActionButtonBarUtility.convertButtonToAction(scoutButton));
        }
        catch (RuntimeException e) {
          LOG.error("could not initialize actions.", e);
        }
      }
    }
    return sortedActions;
  }

  protected List<IMobileAction> createLeftActions() {
    return convertSystemProcessButtons(getRelevantSystemTypesForLeftSide());
  }

  protected List<IMobileAction> createRightActions() {
    List<IMobileAction> actions = new LinkedList<IMobileAction>();

    List<IMobileAction> systemActions = convertSystemProcessButtons(getRelevantSystemTypesForRightSide());
    if (systemActions != null) {
      actions.addAll(systemActions);
    }

    try {
      actions.addAll(convertCustomProcessButtons());
    }
    catch (RuntimeException e) {
      LOG.error("could not initialze actions.", e);
    }

    return actions;
  }

  protected List<Integer> getRelevantSystemTypesForLeftSide() {
    List<Integer> systemTypesToConsider = new LinkedList<Integer>();
    systemTypesToConsider.add(IButton.SYSTEM_TYPE_CANCEL);
    systemTypesToConsider.add(IButton.SYSTEM_TYPE_CLOSE);
    systemTypesToConsider.add(IMobileButton.SYSTEM_TYPE_BACK);
    return systemTypesToConsider;
  }

  /**
   * Returns the system types which are relevant for the right button bar. The order of the list is taken into account
   * too.
   */
  protected List<Integer> getRelevantSystemTypesForRightSide() {
    List<Integer> systemTypesToConsider = new LinkedList<Integer>();
    systemTypesToConsider.add(IButton.SYSTEM_TYPE_OK);
    systemTypesToConsider.add(IButton.SYSTEM_TYPE_SAVE);
    systemTypesToConsider.add(IButton.SYSTEM_TYPE_SAVE_WITHOUT_MARKER_CHANGE);
    systemTypesToConsider.add(IButton.SYSTEM_TYPE_RESET);

    return systemTypesToConsider;
  }

}
