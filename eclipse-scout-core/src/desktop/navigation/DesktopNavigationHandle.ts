/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {CollapseHandle, EnlargeNavigationKeyStroke, KeyStrokeContext, ShrinkNavigationKeyStroke} from '../../index';

export class DesktopNavigationHandle extends CollapseHandle {

  desktopKeyStrokeContext: KeyStrokeContext;

  protected override _initKeyStrokeContext() {
    super._initKeyStrokeContext();

    // Bound to desktop
    this.desktopKeyStrokeContext = new KeyStrokeContext();
    this.desktopKeyStrokeContext.$bindTarget = this.session.desktop.$container;
    this.desktopKeyStrokeContext.$scopeTarget = this.session.desktop.$container;
    this.desktopKeyStrokeContext.registerKeyStrokes([
      new ShrinkNavigationKeyStroke(this),
      new EnlargeNavigationKeyStroke(this)
    ]);
  }

  protected override _render() {
    super._render();
    this.$container.addClass('desktop-navigation-handle');
    this.session.keyStrokeManager.installKeyStrokeContext(this.desktopKeyStrokeContext);
  }

  protected override _remove() {
    super._remove();
    this.session.keyStrokeManager.uninstallKeyStrokeContext(this.desktopKeyStrokeContext);
  }
}
