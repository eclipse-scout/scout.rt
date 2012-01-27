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

import org.eclipse.scout.rt.shared.data.basic.FontSpec;

public interface ICode<T> {

  /**
   * The id represented by this code, this is <b>not</b> the same as {@link #getValue()}
   */
  T getId();

  ICode getParentCode();

  /**
   * do not use this internal method
   */
  void setParentCodeInternal(ICode c);

  ICode[] getChildCodes();

  ICode[] getChildCodes(boolean activeOnly);

  ICode getChildCode(Object codeId);

  ICode getChildCodeByExtKey(Object extKey);

  /**
   * do not use this internal method
   */
  void addChildCodeInternal(ICode code);

  ICodeType getCodeType();

  /**
   * do not use this internal method
   */
  void setCodeTypeInternal(ICodeType type);

  String getText();

  boolean isActive();

  boolean isEnabled();

  String getIconId();

  String getTooltipText();

  String getBackgroundColor();

  String getForegroundColor();

  FontSpec getFont();

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
  boolean visit(ICodeVisitor visitor, int level, boolean activeOnly);

  /**
   * @return a value copy of the code state in form a {@link CodeRow}
   */
  CodeRow toCodeRow();
}
