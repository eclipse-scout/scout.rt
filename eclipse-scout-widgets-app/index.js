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
import { WidgetsApp } from './src/widgets/index';
import * as $ from 'jquery';

$(document).ready(() => {
  new WidgetsApp().init({
    bootstrap: {
      modelsUrl: 'models/widgetsapp.json' // FIXME [awe] ES6: remove modelsUrl property, when all model-code is inlined (or migrated to .js)
    }
  });
});
