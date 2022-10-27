/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {DesktopTab, DesktopTabArea, Form, scout, SimpleTabBoxController} from '../index';

export default class DesktopTabBoxController extends SimpleTabBoxController<Form> {

  constructor() {
    super();
  }

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
