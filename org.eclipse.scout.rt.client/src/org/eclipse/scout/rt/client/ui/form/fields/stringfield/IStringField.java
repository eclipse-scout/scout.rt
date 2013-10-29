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
package org.eclipse.scout.rt.client.ui.form.fields.stringfield;

import org.eclipse.scout.rt.client.ui.IDNDSupport;
import org.eclipse.scout.rt.client.ui.form.fields.IBasicField;

public interface IStringField extends IBasicField<String>, IDNDSupport {

  /**
   * @see IDNDSupport
   */
  String PROP_DECORATION_LINK = "decorationLink";
  String PROP_INPUT_MASKED = "inputMasked";
  String PROP_WRAP_TEXT = "wrapText";
  String PROP_MULTILINE_TEXT = "multilineText";
  String PROP_SELECT_ALL_ON_FOCUS = "selectAllOnFocus";
  String PROP_FORMAT = "format";
  String PROP_MAX_LENGTH = "maxLength";
  String PROP_INSERT_TEXT = "insertText";
  String PROP_SELECTION_START = "selectionStart";
  String PROP_SELECTION_END = "selectionEnd";

  /* enum for formats */
  String FORMAT_UPPER = "A";
  String FORMAT_LOWER = "a";

  void setMaxLength(int len);

  int getMaxLength();

  void setInputMasked(boolean b);

  boolean isInputMasked();

  void setFormat(String s);

  String getFormat();

  void setFormatUpper();

  boolean isFormatUpper();

  void setFormatLower();

  boolean isFormatLower();

  void setDecorationLink(boolean b);

  boolean isDecorationLink();

  void setWrapText(boolean b);

  boolean isWrapText();

  void setMultilineText(boolean b);

  boolean isMultilineText();

  void insertText(String s);

  int getSelectionStart();

  int getSelectionEnd();

  void select(int startIndex, int endIndex);

  boolean isSelectAllOnFocus();

  void setSelectAllOnFocus(boolean b);

  @Override
  IStringFieldUIFacade getUIFacade();

  /**
   * Returns whether this field is spell checkable.
   */
  boolean isSpellCheckEnabled();

  /**
   * Returns whether this field should be monitored for spelling errors in the
   * background ("check as you type"). If it is not defined, null is returned,
   * then the application default is used.
   */
  Boolean isSpellCheckAsYouTypeEnabled();

  /**
   * Sets whether to monitor this field for spelling errors in the background
   * ("check as you type"). Use null for application default.
   */
  void setSpellCheckAsYouTypeEnabled(boolean monitorSpelling);

}
