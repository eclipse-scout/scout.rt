/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.form.fields.treefield;

import org.eclipse.scout.rt.client.ui.form.fields.treefield.ITreeField;
import org.eclipse.scout.rt.ui.rap.form.fields.IRwtScoutFormField;
import org.eclipse.swt.widgets.Tree;

/**
 * <h3>IRwtScoutTreeField</h3> ...
 * 
 * @since 3.7.0 June 2011
 */
public interface IRwtScoutTreeField extends IRwtScoutFormField<ITreeField> {

  /**
   * Custom variant for a tree's container field when placed inside a TreeField.
   */
  String VARIANT_TREE_CONTAINER = "treeField";

  /**
   * Custom variant like {@link #VARIANT_TREE_CONTAINER}, but for disabled state.
   * (Workaround, because RAP does not seem to apply the ":disabled" state correctly.)
   */
  String VARIANT_TREE_CONTAINER_DISABLED = "treeFieldDisabled";

  @Override
  Tree getUiField();
}
