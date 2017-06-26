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
package org.eclipse.scout.rt.client.ui.messagebox;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.TEXTS;

/**
 * Factory for message boxes.
 * <p>
 * Example usages:
 * <ul>
 * <li>MessageBoxes.createOk().header(TEXTS.get("HeaderText)).show()</li>
 * <li>MessageBoxes.createOk().header(TEXTS.get("HeaderText)).body(TEXTS.get("BodyText)).show()</li>
 * <li>MessageBoxes.createYesNo().header(TEXTS.get("HeaderText)).body(TEXTS.get("BodyText)).show()</li>
 * <li>MessageBoxes.showDeleteConfirmationMessage(getNameColumn().getSelectedDisplayTexts())</li>
 * </ul>
 *
 * @since 6.0.0
 */
public final class MessageBoxes {

  private MessageBoxes() {
    // factory, private constructor
  }

  /**
   * Creates a message box with no defined buttons.
   * <p>
   * Do not forget to call {@link #show()} at the end.
   */
  public static IMessageBox create() {
    return BEANS.get(IMessageBox.class);
  }

  /**
   * Creates a message box with one button labeled OK.
   * <p>
   * Do not forget to call {@link #show()} at the end.
   */
  public static IMessageBox createOk() {
    return MessageBoxes.create().withYesButtonText(TEXTS.get("OkButton"));
  }

  /**
   * Creates e message box with yes and not buttons.
   */
  public static IMessageBox createYesNo() {
    return MessageBoxes.create().withYesButtonText(TEXTS.get("YesButton")).withNoButtonText(TEXTS.get("NoButton"));
  }

  /**
   * Creates a message box with yes, no and cancel buttons.
   */
  public static IMessageBox createYesNoCancel() {
    return MessageBoxes.createYesNo().withCancelButtonText(TEXTS.get("CancelButton"));
  }

  /**
   * Convenience function for simple delete confirmation message box
   *
   * @param items
   *          one item or array of multiple items
   * @return <code>true</code> if the user confirmed the deletion, <code>false</code> otherwise
   */
  public static boolean showDeleteConfirmationMessage(Object items) {
    return showDeleteConfirmationMessage(null, items);
  }

  /**
   * Convenience function for simple delete confirmation message box
   *
   * @param items
   *          a list of multiple items
   * @return <code>true</code> if the user confirmed the deletion, <code>false</code> otherwise
   * @since Scout 4.0.1
   */
  public static boolean showDeleteConfirmationMessage(Collection<?> items) {
    return showDeleteConfirmationMessage(null, items);
  }

  /**
   * Convenience function for simple delete confirmation message box
   *
   * @param itemType
   *          display text in plural such as "Persons", "Relations", "Tickets", ...
   * @param items
   *          one item or array of multiple items
   * @return <code>true</code> if the user confirmed the deletion, <code>false</code> otherwise
   */
  public static boolean showDeleteConfirmationMessage(String itemType, Object items) {
    if (items == null) {
      return showDeleteConfirmationMessage(itemType, Collections.emptyList());
    }
    else if (items instanceof Object[]) {
      return showDeleteConfirmationMessage(itemType, Arrays.asList((Object[]) items));
    }
    else if (items instanceof Collection) {
      return showDeleteConfirmationMessage(itemType, (Collection) items);
    }
    else {
      return showDeleteConfirmationMessage(itemType, Collections.singletonList(items));
    }
  }

  /**
   * Convenience function for simple delete confirmation message box
   *
   * @param itemType
   *          display text in plural such as "Persons", "Relations", "Tickets", ...
   * @param items
   *          a list of multiple items
   * @return <code>true</code> if the user confirmed the deletion, <code>false</code> otherwise
   * @since Scout 4.0.1
   */
  public static boolean showDeleteConfirmationMessage(String itemType, Collection<?> items) {
    StringBuilder t = new StringBuilder();

    int n = 0;
    if (items != null) {
      n = items.size();
      int i = 0;
      for (Object item : items) {
        if (i < 10 || i == n - 1) {
          t.append("- ");
          t.append(StringUtility.emptyIfNull(item));
          t.append("\n");
        }
        else if (i == 10) {
          t.append("  ...\n");
        }
        i++;
      }
    }
    //
    String header = null;
    String body = null;
    if (itemType != null) {
      header = (n > 0 ? TEXTS.get("DeleteConfirmationTextX", itemType) : TEXTS.get("DeleteConfirmationTextNoItemListX", itemType));
      body = (n > 0 ? t.toString() : null);
    }
    else {
      header = (n > 0 ? TEXTS.get("DeleteConfirmationText") : TEXTS.get("DeleteConfirmationTextNoItemList"));
      body = (n > 0 ? t.toString() : null);
    }

    int result = createYesNo().withHeader(header).withBody(body).show();
    return result == IMessageBox.YES_OPTION;
  }
}
