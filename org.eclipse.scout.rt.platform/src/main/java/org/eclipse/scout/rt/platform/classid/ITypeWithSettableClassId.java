/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.classid;

/**
 * Extension for {@link ITypeWithClassId} which provides a setter for the classId.
 */
public interface ITypeWithSettableClassId extends ITypeWithClassId {

  /**
   * Setter for the classId.
   * <p>
   * If no classId was set with this setter or <code>null</code> is set, {@link #classId()} returns a default classId.
   */
  void setClassId(String classId);

}
