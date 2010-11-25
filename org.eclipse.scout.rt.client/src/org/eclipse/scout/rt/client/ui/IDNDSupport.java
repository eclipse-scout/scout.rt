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
package org.eclipse.scout.rt.client.ui;

import org.eclipse.scout.commons.dnd.FileListTransferObject;
import org.eclipse.scout.commons.dnd.ImageTransferObject;
import org.eclipse.scout.commons.dnd.JavaTransferObject;
import org.eclipse.scout.commons.dnd.TextTransferObject;

/**
 * <h3>IDNDSupport</h3>
 * 
 * @since 3.1.12 24.07.2008
 * @see TextTransferObject
 * @see JavaTransferObject
 * @see ImageTransferObject
 * @see FileListTransferObject
 */
public interface IDNDSupport {
  /*
   * Properties
   */
  String PROP_DRAG_TYPE = "dragType";
  String PROP_DROP_TYPE = "dropType";
  /*
   * drag n drop types
   */
  int TYPE_FILE_TRANSFER = 1 << 0;
  int TYPE_JAVA_ELEMENT_TRANSFER = 1 << 1;
  int TYPE_TEXT_TRANSFER = 1 << 2;
  int TYPE_IMAGE_TRANSFER = 1 << 3;

  /**
   * @param dragType
   *          one of {@link IDNDSupport#TYPE_FILE_TRANSFER}, {@link IDNDSupport#TYPE_IMAGE_TRANSFER},
   *          {@link IDNDSupport#TYPE_JAVA_ELEMENT_TRANSFER}, {@link IDNDSupport#TYPE_TEXT_TRANSFER}
   */
  void setDragType(int dragType);

  /**
   * @return one of {@link IDNDSupport#TYPE_FILE_TRANSFER}, {@link IDNDSupport#TYPE_IMAGE_TRANSFER},
   *         {@link IDNDSupport#TYPE_JAVA_ELEMENT_TRANSFER}, {@link IDNDSupport#TYPE_TEXT_TRANSFER}
   */
  int getDragType();

  /**
   * @param dropType
   *          one of {@link IDNDSupport#TYPE_FILE_TRANSFER}, {@link IDNDSupport#TYPE_IMAGE_TRANSFER},
   *          {@link IDNDSupport#TYPE_JAVA_ELEMENT_TRANSFER}, {@link IDNDSupport#TYPE_TEXT_TRANSFER}
   */
  void setDropType(int dropType);

  /**
   * @return one of {@link IDNDSupport#TYPE_FILE_TRANSFER}, {@link IDNDSupport#TYPE_IMAGE_TRANSFER},
   *         {@link IDNDSupport#TYPE_JAVA_ELEMENT_TRANSFER}, {@link IDNDSupport#TYPE_TEXT_TRANSFER}
   */
  int getDropType();

}
