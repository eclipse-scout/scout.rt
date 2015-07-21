__include("jquery/jquery-scout.js");
// protects $ and undefined from being redefined by another library
(function(scout, $, undefined) {
__include("scout/text/Texts.js");
__include("scout/login/login.js");
__include("scout/login/logout.js");
__include("scout/util/Device.js");
}(window.scout = window.scout || {}, jQuery));
