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
scout.TableFilter = function() {};

scout.TableFilter.prototype.createKey = function() {
  return 'filterKey';
};

scout.TableFilter.prototype.accept = function(row) {
  // to be implemented by subclasses
};
