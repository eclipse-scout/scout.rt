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
package org.eclipse.scout.rt.ui.swing.ext;

/**
 * Defines additional constants a Swing L&F may support. Most L&F will most likely not support these constants. So you
 * always have to check if a property referenced by a constant of this class really exists.
 */
public interface LookAndFeelConstantsEx {

  /**
   * Expected values are Boolean.TRUE, Boolean.FALSE
   */
  String PROP_TREE_NODE_ROLLOVER = "treeNodeRollover";

}
