/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Widget} from '../widget/Widget';
import {InitModelOf} from '../scout';

export class ResourcesPanel extends Widget {

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
  }

  protected override _render() {
    this.$container = this.$parent.appendDiv('resources-panel-container');
  }
}
