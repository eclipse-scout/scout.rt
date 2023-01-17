/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields;

import java.util.List;
import java.util.Map;

/**
 * A {@link IFormField} that contains other {@link IFormField}s.
 */
public interface ICompositeField extends IFormField {

  String PROP_HAS_VISIBLE_FIELDS = "hasVisibleFields";
  String PROP_FIELDS = "fields";

  /**
   * Returns the index of the first occurrence of the {@link IFormField} element, or -1 if the element can't be found.
   */
  int getFieldIndex(IFormField comp);

  /**
   * recalculate and re-assign the logical x,y,w,h to each field in the groupbox
   */
  void rebuildFieldGrid();

  ICompositeFieldGrid<? extends ICompositeField> getFieldGrid();

  /**
   * direct child fields including groupboxes
   */
  int getFieldCount();

  /**
   * direct child fields including process buttons
   */
  List<IFormField> getFields();

  /**
   * direct child fields including process buttons
   */
  void setFields(List<IFormField> fields);

  /**
   * search all fields in this container and its subtree<br>
   * the field ID is the simple class name of a field without the suffixes "Box", "Field", "Button"
   */
  IFormField getFieldById(String id);

  /**
   * search all fields in this container and its subtree<br>
   * the field ID is the simple class name of a field without the suffixes "Box", "Field", "Button" The field must be
   * equal or a subtype of type
   */
  <T extends IFormField> T getFieldById(String id, Class<T> type);

  /**
   * @return the field with the exact type c in the subtree
   */
  <T extends IFormField> T getFieldByClass(Class<T> c);

  /**
   * for recursion
   */
  @Override
  void setMandatory(boolean b);

  /**
   * Removes the given field from this container. This operation is supported only as long as the form has not been
   * started.
   */
  void removeField(IFormField f);

  /**
   * Adds the given field to this container. This operation is supported only as long as the form has not been started.
   */
  void addField(IFormField f);

  /**
   * Moves a field of this composite field into another one.
   * <p/>
   * Implementing classes are required to keep track of moved fields so that the following methods still return moved
   * fields: {@link #getFieldByClass(Class)}, {@link #getFieldById(String)} and {@link #getFieldById(String, Class)}.
   */
  void moveFieldTo(IFormField f, ICompositeField newContainer);

  /**
   * @return Returns an <b>unmodifiable</b> map with all fields by their actual class that were moved from this
   *         composite field into another one. Never returns null.
   */
  Map<Class<? extends IFormField>, IFormField> getMovedFields();
}
