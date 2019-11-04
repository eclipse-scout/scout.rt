/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {CollapseHandle, EnlargeNavigationKeyStroke, KeyStrokeContext, ShrinkNavigationKeyStroke} from '../../index';

export default class DesktopNavigationHandle extends CollapseHandle {

  constructor() {
    super();
  }

  _initKeyStrokeContext() {
    super._initKeyStrokeContext();

    // Bound to desktop
    this.desktopKeyStrokeContext = new KeyStrokeContext();
    this.desktopKeyStrokeContext.$bindTarget = this.session.desktop.$container;
    this.desktopKeyStrokeContext.$scopeTarget = this.session.desktop.$container;
    this.desktopKeyStrokeContext.registerKeyStroke([
      new ShrinkNavigationKeyStroke(this),
      new EnlargeNavigationKeyStroke(this)
    ]);
  }

  _render() {
    super._render();
    this.$container.addClass('desktop-navigation-handle');
    this.session.keyStrokeManager.installKeyStrokeContext(this.desktopKeyStrokeContext);
  }

  _remove() {
    super._remove();
    this.session.keyStrokeManager.uninstallKeyStrokeContext(this.desktopKeyStrokeContext);
  }
}
