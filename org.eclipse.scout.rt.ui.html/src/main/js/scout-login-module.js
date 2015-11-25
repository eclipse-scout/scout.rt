/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
__include("jquery/jquery-scout.js");
// protects $ and undefined from being redefined by another library
(function(scout, $, undefined) {
__include("scout/main.js");
__include("scout/text/Texts.js");
__include("scout/util/strings.js");
__include("scout/util/Device.js");
__include("scout/util/strings.js");
__include("scout/login/login.js");
__include("scout/login/logout.js");
}(window.scout = window.scout || {}, jQuery));
