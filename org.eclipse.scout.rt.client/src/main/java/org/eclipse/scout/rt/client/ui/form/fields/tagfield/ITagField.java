/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.tagfield;

import java.util.Collection;
import java.util.Set;

import org.eclipse.scout.rt.client.services.lookup.ILookupCallResult;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;

public interface ITagField extends IValueField<Set<String>> {

  String PROP_RESULT = "result";
  String PROP_MAX_LENGTH = "maxLength";

  void addTag(String tag);

  void removeTag(String tag);

  void setTags(Collection<String> tags);

  void removeAllTags();

  /**
   * @param maxLength
   *     of the text in this field. Negative values are automatically converted to 0.
   */
  void setMaxLength(int maxLength);

  /**
   * @return the maximum length of text.
   */
  int getMaxLength();

  void lookupByText(String proposal);

  ITagFieldUIFacade getUIFacade();

  ILookupCallResult<String> getResult();
}
