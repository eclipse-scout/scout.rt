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
package org.eclipse.scout.rt.client.ui.form.fields;

import org.eclipse.scout.rt.client.ui.form.IFormFieldVisitor;

public interface ICompositeField extends IFormField {

  String PROP_VISIBLE_FIELD_COUNT = "visibleFieldCount";

  int getFieldIndex(IFormField comp);

  boolean visitFields(IFormFieldVisitor visitor, int startLevel);

  /**
   * recalculate and re-assign the logical x,y,w,h to each field in the groupbox
   */
  void rebuildFieldGrid();

  /**
   * direct child fields including groupboxes
   */
  int getFieldCount();

  /**
   * direct child fields including process buttons
   */
  IFormField[] getFields();

  /**
   * search all fields in this container and its subtree<br>
   * the field ID is the simple class name of a field without the suffixes
   * "Box", "Field", "Button"
   */
  IFormField getFieldById(String id);

  /**
   * search all fields in this container and its subtree<br>
   * the field ID is the simple class name of a field without the suffixes
   * "Box", "Field", "Button"
   * The field must be equal or a subtype of type
   */
  <T extends IFormField> T getFieldById(String id, Class<T> type);

  /**
   * @return the field with the exact type c in the subtree
   */
  <T extends IFormField> T getFieldByClass(Class<T> c);

  /**
   * for recursion
   */
  void setMandatory(boolean b);

  /**
   * The grid column count for fields in this composite box
   */
  int getGridColumnCount();

  /**
   * The grid row count for fields in this composite box
   */
  int getGridRowCount();

}
