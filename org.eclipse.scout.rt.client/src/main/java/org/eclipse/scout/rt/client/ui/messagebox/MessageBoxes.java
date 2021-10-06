/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui.messagebox;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.text.TEXTS;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;

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
   * Do not forget to call {@link IMessageBox#show()} at the end.
   */
  public static IMessageBox create() {
    return BEANS.get(IMessageBox.class);
  }

  /**
   * Creates a message box with one button labeled OK.
   * <p>
   * Do not forget to call {@link IMessageBox#show()} at the end.
   */
  public static IMessageBox createOk() {
    return create().withYesButtonText(TEXTS.get("OkButton"));
  }

  /**
   * Creates e message box with yes and not buttons.
   * <p>
   * Do not forget to call {@link IMessageBox#show()} at the end.
   */
  public static IMessageBox createYesNo() {
    return create().withYesButtonText(TEXTS.get("YesButton")).withNoButtonText(TEXTS.get("NoButton"));
  }

  /**
   * Creates a message box with yes, no and cancel buttons.
   * <p>
   * Do not forget to call {@link IMessageBox#show()} at the end.
   */
  public static IMessageBox createYesNoCancel() {
    return createYesNo().withCancelButtonText(TEXTS.get("CancelButton"));
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
    int result = createDeleteConfirmationMessage(itemType, items).show();
    return result == IMessageBox.YES_OPTION;
  }

  /**
   * Convenience function for simple delete confirmation message box
   *
   * @param items
   *          a list of multiple items
   * @return <code>IMessageBox</code>
   * @since Scout 22.0
   */
  public static IMessageBox createDeleteConfirmationMessage(Collection<?> items) {
    return createDeleteConfirmationMessage(null, items);
  }

  /**
   * Convenience function for simple delete confirmation message box
   *
   * @param itemType
   *          display text in plural such as "Persons", "Relations", "Tickets", ...
   * @param items
   *          a list of multiple items
   * @return <code>IMessageBox</code>
   * @since Scout 22.0
   */
  public static IMessageBox createDeleteConfirmationMessage(String itemType, Collection<?> items) {
    final boolean hasItems = !CollectionUtility.isEmpty(items);
    String header = null;
    if (itemType != null) {
      header = (hasItems ? TEXTS.get("DeleteConfirmationTextX", itemType) : TEXTS.get("DeleteConfirmationTextNoItemListX", itemType));
    }
    else {
      header = (hasItems ? TEXTS.get("DeleteConfirmationText") : TEXTS.get("DeleteConfirmationTextNoItemList"));
    }
    return createYesNo().withHeader(header).withBody(createDeleteConfirmationMessageBody(items));
  }

  private static String createDeleteConfirmationMessageBody(Collection<?> items) {
    if (CollectionUtility.isEmpty(items)) {
      return null;
    }
    final int maxVisibleItemsCount = 10;
    final int excessItemsMessageLines = 2;
    final int hiddenItemsCount = items.size() - maxVisibleItemsCount;
    final boolean showExcessItemsEntry = hiddenItemsCount > excessItemsMessageLines;
    String body = items.stream()
        .limit(maxVisibleItemsCount + (showExcessItemsEntry ? 0 : excessItemsMessageLines))
        .map(item -> "- " + StringUtility.emptyIfNull(item))
        .collect(Collectors.joining("\n"));
    if (showExcessItemsEntry) {
      body += "\n...\n" + TEXTS.get("XAdditional", Integer.toString(hiddenItemsCount)) + "\n";
    }
    return body;
  }
}
