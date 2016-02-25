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
 * This class is used in place of scout.DesktopNavigation when desktop has no navigation.
 * Avoids a lot of messy if/else code.
 */
scout.NullDesktopNavigation = {
  render: function($parent) {},
  onOutlineChanged: function(outline) {},
  bringToFront: function() {},
  sendToBack: function() {},
  $container: $('<div>')
};
