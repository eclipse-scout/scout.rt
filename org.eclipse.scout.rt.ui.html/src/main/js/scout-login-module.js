/*!
* Eclipse Scout Login
* https://eclipse.org/scout/
*
* Copyright (c) BSI Business Systems Integration AG. All rights reserved.
* Released under the Eclipse Public License v1.0
* http://www.eclipse.org/legal/epl-v10.html
*/
// protects $ and undefined from being redefined by another library
(function(scout, $, undefined) {
  __include("jquery/jquery-scout.js");
  __include("scout/App.js");
  __include("scout/main.js");
  __include("scout/TypeDescriptor.js");
  __include("scout/ObjectFactory.js");
  __include("scout/box/Box.js");
  __include("scout/text/TextMap.js");
  __include("scout/text/texts.js");
  __include("scout/util/strings.js");
  __include("scout/util/Device.js");
  __include("scout/util/strings.js");
  __include("scout/util/objects.js");
  __include("scout/logging/logging.js");
  __include("scout/logging/NullLogger.js");
  __include("scout/util/arrays.js");
  __include("scout/util/URL.js");
  __include("scout/util/EventSupport.js");
  __include("scout/login/LoginApp.js");
  __include("scout/login/LoginBox.js");
  __include("scout/login/LogoutApp.js");
  __include("scout/login/LogoutBox.js");
}(window.scout = window.scout || {}, jQuery)); // NOSONAR
