/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
scout.SequenceBoxLayout = function(widget, layoutConfig) {
  scout.SequenceBoxLayout.parent.call(this, widget, layoutConfig);
};
scout.inherits(scout.SequenceBoxLayout, scout.LogicalGridLayout);

/**
 * @override LogicalGridLayout.js
 */
scout.SequenceBoxLayout.prototype._initDefaults = function() {
  scout.SequenceBoxLayout.parent.prototype._initDefaults.call(this);
  this.hgap = scout.htmlEnvironment.smallColumnGap;
};
