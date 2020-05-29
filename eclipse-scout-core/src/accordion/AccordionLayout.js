/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {RowLayout, scrollbars} from '../index';

export default class AccordionLayout extends RowLayout {

  layout($container) {
    super.layout($container);
    scrollbars.update($container, true); // update immediately to prevent flickering when scrollbars become visible
  }
}
