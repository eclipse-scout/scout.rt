/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {DesktopTab, DesktopTabArea, Form, scout, SimpleTabBoxController} from '../index';

export class DesktopTabBoxController extends SimpleTabBoxController<Form> {

  override createTabArea(): DesktopTabArea {
    return scout.create(DesktopTabArea, {
      parent: this.tabBox
    });
  }

  protected override _createTab(view: Form): DesktopTab {
    return scout.create(DesktopTab, {
      parent: this.tabArea,
      view: view
    });
  }

  protected override _shouldCreateTabForView(view: Form): boolean {
    // Don't create a tab if the view itself already has a header.
    return !view.headerVisible;
  }
}
