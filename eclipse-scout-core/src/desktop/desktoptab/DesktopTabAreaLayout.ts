/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {DesktopTabArea, SimpleTabAreaLayout} from '../../index';

export class DesktopTabAreaLayout extends SimpleTabAreaLayout {
  declare tabArea: DesktopTabArea;

  constructor(tabArea: DesktopTabArea) {
    super(tabArea);
  }

  override layout($container: JQuery) {
    super.layout($container);
    this.tabArea.updateFirstTabSelected();
  }
}
