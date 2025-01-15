/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.smartfield;

/**
 * A proposal field provides two functions:
 * <ol>
 * <li>choose a proposal from a list of proposals (like smart-field), or...
 * <li>enter a custom text that does or does not exist in the list of proposals
 * </ol>
 * The value of a proposal field is always of type String. If a user coincidentally enters a text which also exists in
 * the proposal list, only the text is set and the lookupRow property is not s set. If a user has selected a proposal
 * from the proposal list the lookupRow property is set. That's how you can distinct between manually entered texts and
 * proposals chosen from the list.
 * <p>
 * There's no validation logic that checks for duplicates between manually entered text and the proposal list.
 *
 * @param <VALUE>
 *     generic parameter type of lookup key
 */
public interface IProposalField<VALUE> extends ISmartField<VALUE> {

  /**
   * {@link Boolean}
   *
   * @since 6.1
   */
  String PROP_TRIM_TEXT_ON_VALIDATE = "trimText";

  String getValueAsString();

  void setValueAsString(String value);

  /**
   * @param b
   *     true if the entered text should pass through {@link String#trim()}
   * @since 6.1
   */
  void setTrimText(boolean b);

  /**
   * @return true if {@link String#trim()} is applied to the text entered, default true
   * @since 6.1
   */
  boolean isTrimText();

  @Override
  IProposalFieldUIFacade<VALUE> getUIFacade();
}
