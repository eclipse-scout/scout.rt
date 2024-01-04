/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.services.common.code;

import java.util.List;

import org.eclipse.scout.rt.api.data.code.CodeTypeDo;
import org.eclipse.scout.rt.platform.classid.ITypeWithClassId;
import org.eclipse.scout.rt.shared.extension.IExtensibleObject;

/**
 * Code types are dynamic enumerations used in front- and back-end. <br>
 * Values are cached on client and server.
 *
 * @param <CODE_TYPE_ID>
 *          type of the codetype: see {@link #getId()}
 * @param <CODE_ID>
 *          type of the code: see {@link #getCode(Object)}
 */
public interface ICodeType<CODE_TYPE_ID, CODE_ID> extends IExtensibleObject, ITypeWithClassId {

  CODE_TYPE_ID getId();

  boolean isHierarchy();

  int getMaxLevel();

  /**
   * @return the name of the code type in singular form.
   */
  String getText();

  /**
   * @return the name of the code type in plural form.
   */
  String getTextPlural();

  String getIconId();

  /**
   * all active top-level (root) codes
   */
  List<? extends ICode<CODE_ID>> getCodes();

  /**
   * all top-level (root) codes
   */
  List<? extends ICode<CODE_ID>> getCodes(boolean activeOnly);

  /**
   * find the code with this id
   */
  ICode<CODE_ID> getCode(CODE_ID id);

  /**
   * find the code with this external reference
   */
  ICode<CODE_ID> getCodeByExtKey(Object extKey);

  /**
   * @return the index (starting at 0) of this code, -1 when not found <br>
   *         When the code type is a tree, the top-down-left-right traversal index is used
   */
  int getCodeIndex(final CODE_ID id);

  /**
   * @return the index (starting at 0) of this code, -1 when not found <br>
   *         When the code type is a tree, the top-down-left-right traversal index is used
   */
  int getCodeIndex(final ICode<CODE_ID> c);

  /**
   * visits per default only the active codes
   */
  <T extends ICode<CODE_ID>> boolean visit(ICodeVisitor<T> visitor);

  <T extends ICode<CODE_ID>> boolean visit(ICodeVisitor<T> visitor, boolean activeOnly);

  /**
   * @see CodeTypeDoConverter#convert(ICodeType)
   */
  CodeTypeDo toDo();
}
