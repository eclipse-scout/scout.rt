/*
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
   *          of the text in this field. Negative values are automatically converted to 0.
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
