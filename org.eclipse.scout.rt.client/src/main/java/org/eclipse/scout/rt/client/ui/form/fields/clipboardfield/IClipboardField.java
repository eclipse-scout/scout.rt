/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.clipboardfield;

import java.util.Collection;
import java.util.List;

import org.eclipse.scout.rt.client.ui.dnd.IDNDSupport;
import org.eclipse.scout.rt.client.ui.form.clipboard.ClipboardForm;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.platform.resource.BinaryResource;

/**
 * Clipboard field to catch arbitrary clipboard paste events.
 * <p/>
 * Some ui technologies might not be able to access the clipboard by themselves (e.g. web browsers), this field is
 * supposed to act as man-in-the-middle. The user pastes the clipboard contents to this field, they are considered the
 * new value of the field.
 *
 * @see ClipboardForm ClipboardForm for an example implementation.
 * @since 5.1
 */
public interface IClipboardField extends IValueField<Collection<BinaryResource>>, IDNDSupport {

  String PROP_MAXIMUM_SIZE = "maximumSize";
  String PROP_ALLOWED_MIME_TYPES = "allowedMimeTypes";
  String PROP_READ_ONLY = "readOnly";

  /*
   * Runtime
   */

  long getMaximumSize();

  void setMaximumSize(long maximumSize);

  List<String> getAllowedMimeTypes();

  void setAllowedMimeTypes(List<String> allowedMimeTypes);

  boolean isReadOnly();

  void setReadOnly(boolean b);
}
