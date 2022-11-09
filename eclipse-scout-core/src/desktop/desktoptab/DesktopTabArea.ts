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
import {AbstractLayout, DesktopTab, DesktopTabAreaLayout, Form, SimpleTabArea} from '../../index';

export class DesktopTabArea extends SimpleTabArea<Form> {
  declare tabs: DesktopTab[];

  protected override _render() {
    super._render();
    this.$container.addClass('desktop-tab-area');
  }

  protected override _createLayout(): AbstractLayout {
    return new DesktopTabAreaLayout(this);
  }

  updateFirstTabSelected() {
    let desktop = this.session.desktop;
    if (!desktop.rendered) {
      return;
    }
    let firstTab = this.getVisibleTabs()[0];
    desktop.$container.toggleClass('first-tab-selected', firstTab && firstTab.selected);
  }

  override getTabs(): DesktopTab[] {
    return super.getTabs() as DesktopTab[];
  }
}
