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
 * Assigns an id to an object that should be <b>unique</b>.
 * <p>
 * It is sometimes necessary to identify a class by a unique id other than the class name. E.g. an id for a scout model
 * entity could be used in test tools and should therefore not change when a class is moved to another package or
 * another place in the inner class hierarchy. <br/>
 * It should still be possible set this id dynamically when creating scout model entities on the fly with anonymous
 * inner classes.
 * </p>
 *
 * @see org.eclipse.scout.rt.platform.classid.ClassId ClassId
 */
public interface ITypeWithClassId {

  String ID_CONCAT_SYMBOL = "_";

  /**
   * @return a unique id
   */
  String classId();

}
