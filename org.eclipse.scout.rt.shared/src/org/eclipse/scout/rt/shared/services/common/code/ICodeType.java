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
package org.eclipse.scout.rt.shared.services.common.code;

import org.eclipse.scout.commons.ITypeWithClassId;

/**
 * Code types are dynamic enumerations used in front- and back-end. <br>
 * A code type may (must not) contain codes divided into different partitions
 * (german: Mandanten) using the partitionId. <br>
 * If partitions are used, a context contains to a certain partition and
 * receives only his codes.
 */
public interface ICodeType<T> extends ITypeWithClassId {

  /**
   * property into ISharedContextService's Map to get default partitionId of
   * current Subject / User
   */
  String PROP_PARTITION_ID = "partitionId";

  T getId();

  boolean isHierarchy();

  int getMaxLevel();

  String getText();

  String getIconId();

  /**
   * all active top-level (root) codes
   */
  ICode[] getCodes();

  /**
   * all top-level (root) codes
   */
  ICode[] getCodes(boolean activeOnly);

  /**
   * find the code with this id
   */
  ICode getCode(Object id);

  /**
   * find the code with this external reference
   */
  ICode getCodeByExtKey(Object extKey);

  /**
   * @return the index (starting at 0) of this code, -1 when not found <br>
   *         When the code type is a tree, the top-down-left-right traversal
   *         index is used
   */
  int getCodeIndex(final Object id);

  /**
   * @return the index (starting at 0) of this code, -1 when not found <br>
   *         When the code type is a tree, the top-down-left-right traversal
   *         index is used
   */
  int getCodeIndex(final ICode c);

  /**
   * visits per default only the active codes
   * 
   * @param visitor
   * @return
   */
  boolean visit(ICodeVisitor visitor);

  boolean visit(ICodeVisitor visitor, boolean activeOnly);

}
