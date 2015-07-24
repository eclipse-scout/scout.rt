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
package org.eclipse.scout.rt.client.ui.form.fields.clipboardfield;

import java.util.Collection;
import java.util.List;

import org.eclipse.scout.commons.resource.BinaryResource;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;

/**
 * Optional marker interface for clipboard fields
 */
public interface IClipboardField extends IValueField<Collection<BinaryResource>> {

  String PROP_MAXIMUM_SIZE = "maximumSize";
  String PROP_ALLOWED_MIME_TYPES = "allowedMimeTypes";

  /*
   * Runtime
   */

  long getMaximumSize();

  void setMaximumSize(long maximumSize);

  List<String> getAllowedMimeTypes();

  void setAllowedMimeTypes(List<String> allowedMimeTypes);

}
