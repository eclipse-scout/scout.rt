/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {RowLayout, scrollbars} from '../index';

export class AccordionLayout extends RowLayout {

  protected override _getChildren($container: JQuery): JQuery {
    return $container.children('.group');
  }

  override layout($container: JQuery) {
    super.layout($container);
    scrollbars.update($container, true); // update immediately to prevent flickering when scrollbars become visible
  }
}
