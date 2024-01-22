/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.services.common.code;

import java.util.List;

import org.eclipse.scout.rt.api.data.code.CodeDo;
import org.eclipse.scout.rt.platform.IOrdered;
import org.eclipse.scout.rt.platform.classid.ITypeWithClassId;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;

public interface ICode<T> extends ITypeWithClassId, IOrdered {

  /**
   * The id represented by this code, this is <b>not</b> the same as {@link #getValue()}
   */
  T getId();

  ICode<T> getParentCode();

  /**
   * do not use this internal method
   */
  void setParentCodeInternal(ICode<T> c);

  List<? extends ICode<T>> getChildCodes();

  List<? extends ICode<T>> getChildCodes(boolean activeOnly);

  ICode<T> getChildCode(T codeId);

  ICode<T> getChildCodeByExtKey(Object extKey);

  /**
   * do not use this internal method unless the intention is in fact to change the structure of the possibly shared
   * {@link ICodeType}
   * <p>
   * Add a new code as child of this code, owerwrite (drop) existing code
   *
   * @since 4.0
   * @param index
   *          if index is -1 and the codeId existed before, then it is replaced at the same position. If index is -1 and
   *          the codeId did not exist, then the code is appended to the end.
   */
  void addChildCodeInternal(int index, ICode<T> code);

  /**
   * do not use this internal method unless the intention is in fact to change the structure of the possibly shared
   * {@link ICodeType}
   * <p>
   * Remove a child code of this code
   *
   * @since 4.0
   * @return the index the code had in the list or -1
   */
  int removeChildCodeInternal(T codeId);

  ICodeType<?, T> getCodeType();

  /**
   * do not use this internal method
   */
  void setCodeTypeInternal(ICodeType<?, T> type);

  String getText();

  boolean isActive();

  boolean isEnabled();

  String getIconId();

  String getTooltipText();

  String getBackgroundColor();

  String getForegroundColor();

  FontSpec getFont();

  String getCssClass();

  /**
   * Used for primary key mappings to external systems
   */
  String getExtKey();

  /**
   * The value represented by this code, this is <b>not</b> the same as {@link #getId()}
   */
  Number getValue();

  /**
   * see {@link ICodeType}
   */
  long getPartitionId();

  /**
   * Visit all codes in the subtree of this code <b>excluding</b> this code.
   */
  <CODE extends ICode<T>> boolean visit(ICodeVisitor<CODE> visitor, int level, boolean activeOnly);

  /**
   * @return a value copy of the code state in form a {@link CodeRow}
   */
  ICodeRow<T> toCodeRow();

  /**
   * Converts this Code to a {@link CodeDo}.
   *
   * @return The {@link CodeDo} with the attributes of this Code copied to the data object. Child Codes are converted
   * and added to the resulting data object recursively. May return {@code null} if this Code does not support
   * conversion to a data object.
   * @see ICodeToDoFunction
   */
  CodeDo toDo();
}
