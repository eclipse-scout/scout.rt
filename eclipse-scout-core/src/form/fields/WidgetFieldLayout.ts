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
import {FormFieldLayout, scrollbars, WidgetField} from '../../index';

export default class WidgetFieldLayout extends FormFieldLayout {
  declare formField: WidgetField;

  constructor(formField: WidgetField) {
    super(formField);
  }

  override layout($container: JQuery) {
    super.layout($container);
    scrollbars.update(this.formField.$fieldContainer);
  }
}
