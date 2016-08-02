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
package org.eclipse.scout.rt.client.ui.form.fields.stringfield;

import org.eclipse.scout.rt.client.ui.IHtmlCapable;
import org.eclipse.scout.rt.client.ui.dnd.IDNDSupport;
import org.eclipse.scout.rt.client.ui.form.fields.IBasicField;

public interface IStringField extends IBasicField<String>, IDNDSupport, IHtmlCapable {

  /**
   * @see IDNDSupport
   */
  String PROP_HAS_ACTION = "hasAction";
  String PROP_INPUT_MASKED = "inputMasked";
  String PROP_WRAP_TEXT = "wrapText";
  String PROP_TRIM_TEXT_ON_VALIDATE = "trimText";
  String PROP_MULTILINE_TEXT = "multilineText";
  String PROP_FORMAT = "format";
  String PROP_MAX_LENGTH = "maxLength";
  String PROP_INSERT_TEXT = "insertText";
  String PROP_SELECTION_START = "selectionStart";
  String PROP_SELECTION_END = "selectionEnd";
  String PROP_SPELL_CHECK_ENABLED = "spellCheckEnabled";
  String PROP_SELECTION_TRACKING_ENABLED = "selectionTrackingEnabled";

  /* enum for formats */
  String FORMAT_UPPER = "A";
  String FORMAT_LOWER = "a";

  /**
   * Sets the maximum length of this field. Negative values are automatically converted to 0.
   */
  void setMaxLength(int maxLength);

  int getMaxLength();

  void setInputMasked(boolean b);

  boolean isInputMasked();

  void setFormat(String s);

  String getFormat();

  void setFormatUpper();

  boolean isFormatUpper();

  void setFormatLower();

  boolean isFormatLower();

  void setHasAction(boolean b);

  boolean isHasAction();

  void setWrapText(boolean b);

  boolean isWrapText();

  void setTrimText(boolean b);

  boolean isTrimText();

  void setMultilineText(boolean b);

  boolean isMultilineText();

  void insertText(String s);

  int getSelectionStart();

  int getSelectionEnd();

  void select(int startIndex, int endIndex);

  boolean isSelectionTrackingEnabled();

  void setSelectionTrackingEnabled(boolean selectionTrackingEnabled);

  @Override
  IStringFieldUIFacade getUIFacade();

  boolean isSpellCheckEnabled();

  void setSpellCheckEnabled(boolean spellCheckEnabled);

  void doAction();
}
