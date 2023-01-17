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
 * Assigns an id to an object that should be <b>unique</b>.
 * <p>
 * It is sometimes necessary to identify a class by a unique id other than the class name. E.g. an id for a scout model
 * entity could be used in test tools and should therefore not change when a class is moved to another package or
 * another place in the inner class hierarchy. <br>
 * It should still be possible set this id dynamically when creating scout model entities on the fly with anonymous
 * inner classes.
 * </p>
 *
 * @see ClassId ClassId
 */
@FunctionalInterface
public interface ITypeWithClassId {

  String ID_CONCAT_SYMBOL = "_";

  /**
   * @return a unique id
   */
  String classId();

}
