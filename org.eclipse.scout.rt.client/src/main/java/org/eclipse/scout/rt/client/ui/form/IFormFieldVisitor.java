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
package org.eclipse.scout.rt.client.ui.form;

import org.eclipse.scout.rt.client.ui.form.fields.ICompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;

/**
 * <h3>{@link IFormFieldVisitor}</h3><br>
 * Visitor for {@link IFormField} trees.
 */
@FunctionalInterface
public interface IFormFieldVisitor {
  /**
   * Callback that is invoked for every {@link IFormField}.
   *
   * @param field
   *          The field that is currently visited.
   * @param level
   *          The level in the field tree. The field on which the visit was started has a level of zero. Each child
   *          level adds one to the level and each parent level subtracts one level.
   * @param fieldIndex
   *          The index the currently visited field has in the list of its parent field. Note that the first visited
   *          field has an index of zero even if it is not the first {@link IFormField} of its parent.
   * @return <code>true</code> if more {@link IFormField}s should be visited. <code>false</code> if the visit should be
   *         aborted.
   * @see ICompositeField#visitFields(IFormFieldVisitor)
   * @see IFormField#acceptVisitor(IFormFieldVisitor, int, int, boolean)
   * @see IFormField#visitParents(IFormFieldVisitor)
   */
  boolean visitField(IFormField field, int level, int fieldIndex);
}
