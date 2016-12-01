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
  __include("scout/main.js");
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
  __include("scout/util/URL.js");
  __include("scout/login/login.js");
  __include("scout/login/LoginBox.js");
  __include("scout/login/logout.js");
  __include("scout/login/LogoutBox.js");
  // TODO [6.1] PBZ;CGU dependencies needed by ObjectFactory, would be nice to avoid this dependency since login box is not a widget
  __include("scout/widget/Widget.js");
  __include("scout/session/ModelAdapter.js");
}(window.scout = window.scout || {}, jQuery)); // NOSONAR
