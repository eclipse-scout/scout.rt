/*******************************************************************************
 * Copyright (c) 2014-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
import { Scout, OutlineViewButton } from 'eclipse-scout';
import * as $ from 'jquery';
import MyOutlineViewButton from './outline/MyOutlineViewButton';


// FIXME [awe] ES6: better use an API function from Scout, instead of plain jQuery/object
Scout.objectFactories = $.extend(Scout.objectFactories, {
  OutlineViewButton: function() {
    return new MyOutlineViewButton();
  }
});
