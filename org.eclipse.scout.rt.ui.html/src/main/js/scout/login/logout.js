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
scout.logout = {

  /**
   * opts:
   * - loginUrl: URL to redirect after login again button click
   * - logoUrl: default points to 'res/logo.png'
   */
  init: function(opts) {
    scout.prepareDOM();

    var logoutBox = new scout.LogoutBox(opts);
    logoutBox.render($('body'));
  }

};
