/* --------------------------------------------------------------------
 * Patches for jQuery 3.3.1
 *
 * Carefully re-examine this file after each jQuery release update!
 * -------------------------------------------------------------------- */

// jQuery 3.3.1 includes the fix for issue 3699 (https://github.com/jquery/jquery/issues/3699). See commit https://github.com/jquery/jquery/commit/20cdf4e7de60f515a7acf6c70228c52668301d9b
// The fix tries to detect if a browser wrongly calculates box sizes with overflow:scroll.
// The fix targets IE9 but is also wrongly active on e.g. Chrome if the web page is initially loaded using zoom (e.g. 90%).
// This was reported as issue 4029 (https://github.com/jquery/jquery/issues/4029) and fixed with commit https://github.com/jquery/jquery/commit/821bf34353a6baf97f7944379a6459afb16badae
// Unfortunately the fix for issue 4029 is not included in a jQuery release yet.
// As Scout no longer supports IE9 anyway and rarely makes use of overflow:scroll, disable fix 3699 completely for the moment.
// As soon as jQuery is updated again, this patch may be removed after good testing.

// The issue can be observed in the Scout UI e.g. in the Desktop header form-menus when starting Chrome with 90% zoom.
// It is important to refresh the page after the zoom has been set to 90% because jQuery only calculates the css-fixes once and caches the results!
$.support.scrollboxSize = function() {
  return true;
};
