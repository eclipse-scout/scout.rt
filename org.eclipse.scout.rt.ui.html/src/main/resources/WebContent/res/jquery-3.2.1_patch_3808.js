/* --------------------------------------------------------------------
 * Patch for jQuery 3.2.1 issue #3803
 * https://github.com/jquery/jquery/issues/3808
 *
 * Carefully re-examine this file after each jQuery release update!
 * -------------------------------------------------------------------- */

// The value for "box sizing reliable" is not calculated correctly in Chrome when browser zoom is not 100%.
// To fix this, assume box sizing is always reliable (may break some older Firefox or Android versions).
if (navigator.userAgent.indexOf('Chrome') > -1) {
  jQuery.support._boxSizingReliable = jQuery.support.boxSizingReliable;
  jQuery.support.boxSizingReliable = function() {
    return true;
  };
}
