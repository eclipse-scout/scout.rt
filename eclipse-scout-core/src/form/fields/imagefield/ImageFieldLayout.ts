/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {FormFieldLayout, scrollbars} from '../../../index';

export default class ImageFieldLayout extends FormFieldLayout {

  constructor(imageField) {
    super(imageField);
  }

  layout($container) {
    super.layout($container);
    scrollbars.update(this.formField.$fieldContainer);
  }
}
