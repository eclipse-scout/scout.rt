/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
