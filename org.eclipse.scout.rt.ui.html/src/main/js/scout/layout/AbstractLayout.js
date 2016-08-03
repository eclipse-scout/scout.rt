/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
/**
 * Abstract layout class with functions used by all layout algorithms.
 * Subclasses of AbstactLayout.js must implement the following functions:
 * - layout
 * - preferredLayoutSize
 */
scout.AbstractLayout = function() { //
};

/**
 * Called when layout is invalidated. An implementation should delete cached layout-information
 * when it is invalidated.
 *
 * May be implemented by sub-class.
 */
scout.AbstractLayout.prototype.invalidate = function() { //
};

/**
 * Layouts children of the given $container, according to the implemented layout algorithm.
 * The implementation should call setSize or setBounds on its children.
 *
 * Must be implemented by sub-class.
 */
scout.AbstractLayout.prototype.layout = function($container) { //
};

/**
 * Returns the preferred size of the given $container.
 *
 * @return scout.Dimension preferred size
 */
scout.AbstractLayout.prototype.preferredLayoutSize = function($container, options) {
  return scout.graphics.prefSize($container, options);
};
