/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {SimpleTabAreaLayout} from '../../index';

export default class DesktopTabAreaLayout extends SimpleTabAreaLayout {
  constructor(tabArea) {
    super(tabArea);
  }

  layout($container) {
    super.layout($container);
    this.tabArea.updateFirstTabSelected();
  }
}
