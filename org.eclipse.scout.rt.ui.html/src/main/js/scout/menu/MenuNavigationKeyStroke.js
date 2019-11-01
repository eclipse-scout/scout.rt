/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {KeyStroke} from '../index';

export default class MenuNavigationKeyStroke extends KeyStroke {

constructor(popup) {
  super();
  this.field = popup;
}


_accept(event) {
  var accepted = super._accept( event);
  if (!accepted || this.field.bodyAnimating) {
    return false;
  }
  return accepted;
}
}
