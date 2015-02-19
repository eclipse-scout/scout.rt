/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons.status;

import java.util.List;

/**
 * A multi-status may have other status objects as children.
 */
public interface IMultiStatus extends IStatus {

  /**
   * Returns a list of status objects immediately contained in this
   * multi-status, or an empty list if there are no children available.
   * <p>
   * The list of children is sorted from highest to lowest severity. ({@link IStatus#ERROR} to {@link IStatus#OK})
   * </p>
   * *
   * <p>
   * The severity of a multi-status is defined to be the maximum severity of any of its children, or <code>OK</code> if
   * it has no children.
   * </p>
   *
   * @return SortedSet children
   * @see #isMultiStatus()
   */
  public List<IStatus> getChildren();

}
