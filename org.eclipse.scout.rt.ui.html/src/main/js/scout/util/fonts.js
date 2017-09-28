/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.fonts = {

  _deferred: $.Deferred(),

  /**
   * Indicates whether all fonts have been loaded successfully. Check this variable before
   * waiting for the promise object returned by preloader().
   */
  loadingComplete: true,

  /**
   * Start preloading the specified fonts.
   * @param fonts (optional) array of fonts
   */
  bootstrap: function(fonts) {
    if (!fonts || fonts.length === 0) {
      this.loadingComplete = true;
      return $.resolvedPromise();
    }

    // Start preloading
    this.loadingComplete = false;
    this.preload({
      fonts: fonts,
      onComplete: function(success) {
        this.loadingComplete = true;
        this._deferred.resolve();
      }.bind(this)
    });

    return $.resolvedPromise();
  },

  /**
   * @return  a promise object that is notified when the font preloading was completed.
   *          Important: Before waiting for this promise, always check that value of
   *          this.loadingComplete first! Do not wait for the promise when loadingComplete
   *          is true, because the promise will never be resolved.
   */
  preloader: function() {
    return this._deferred.promise();
  },

  DEFAULT_TEST_FONTS: 'monospace',

  DEFAULT_TEST_STRING: 'ABC abc 123 .,_ LlIi1 oO0 !#@',

  /**
   * Loads the specified fonts in a hidden div, forcing the browser to load them.
   *
   * Options:
   *   [fonts]
   *     A single string or object (or an array of them) specifying which fonts should
   *     be preloaded. A string is interpreted as font-family. If the style is relevant,
   *     too, an object with the properties 'font-family' and 'style' should be provided.
   *     Alternatively, the style can be specified in the string after the font name,
   *     separated by a pipe character ('|').
   *     The property 'testString' (or a third component in a '|' separated string) may
   *     be specified to set the characters to measure for this specific font (can be
   *     useful for icon fonts).
   *   [onComplete]
   *     Mandatory function to be called when all of the specified fonts have been
   *     loaded or if a timeout occurs. An argument 'success' is given to indicate
   *     whether loading was completed successfully or execution was interrupted by
   *     a timeout. If this option is omitted, the call to this method returns immediately.
   *   [timeout]
   *     Optional timeout in milliseconds. If fonts could not be loaded within this time,
   *     loading is stopped and the onComplete method is called with argument 'false'.
   *     Default is 30 seconds.
   *   [testFonts]
   *     Optional. Test fonts (string separated by commas) to used as baseline when checking
   *     if the specified fonts have been loaded. Defaults to scout.fonts.DEFAULT_TEST_FONTS.
   *   [testString]
   *     Optional. The test string to use when checking if the specified fonts have been
   *     loaded. Should not be empty, because the empty string has always the width 0.
   *     The default is scout.fonts.DEFAULT_TEST_STRING. The test string may also be
   *     specified individually per font
   *
   *
   * Examples:
   *   preload({fonts: 'Sauna Pro'});
   *   preload({fonts: 'Sauna Pro|font-style:italic'});
   *   preload({fonts: 'Sauna Pro|font-style:italic|The quick brown fox jumps over the lazy dog'});
   *   preload({fonts: 'Sauna Pro | font-style: italic; font-weight: 700'});
   *   preload({fonts: 'Sauna Pro', onComplete: handleLoadFinished});
   *   preload({fonts: ['Sauna Pro', 'Dolly Pro']});
   *   preload({fonts: {family:'Sauna', style: 'font-style:italic; font-weight:700', testString: 'MyString012345'}, timeout: 999});
   *   preload({fonts: ['Fakir-Black', {family:'Fakir-Italic', style:'font-style:italic'}], timeout: 2500, onComplete: function() { setCookie('fakir','loaded') }});
   *
   * Inspired by Zenfonts (https://github.com/zengabor/zenfonts, public domain).
   */
  preload: function(options) {
    options = options || {};
    var fonts = options.fonts || [];
    if (!Array.isArray(fonts)) {
      fonts = [fonts];
    }
    if (!options.onComplete) {
      // preloading is not useful, because there is no callback on success
      return;
    }

    // these fonts are compared to the custom fonts, strings separated by comma
    var testFonts = options.testFonts || scout.fonts.DEFAULT_TEST_FONTS;

    // Create a DIV for each font
    var divs = [];
    fonts.forEach(function(font) {
      // Convert to object
      if (typeof font === 'string') {
        var fontParts = scout.strings.splitMax(font, '|', 3).map(function(s) {
          return s.trim();
        });
        font = {
          family: fontParts[0],
          style: fontParts[1],
          testString: fontParts[2]
        };
      }
      font.family = font.family || '';
      font.style = font.style || '';
      font.testString = font.testString || options.testString || scout.fonts.DEFAULT_TEST_STRING;

      // Create DIV with default fonts
      // (Because preloader functionality should not depend on a CSS stylesheet we set the required properties programmatically.)
      var $div = $('body').appendDiv('font-preloader')
          .text(font.testString)
          .css('display', 'block')
          .css('visibility', 'hidden')
          .css('position', 'absolute')
          .css('top', 0)
          .css('left', 0)
          .css('width', 'auto')
          .css('height', 'auto')
          .css('margin', 0)
          .css('padding', 0)
          .css('white-space', 'nowrap')
          .css('line-height', 'normal')
          .css('font-variant', 'normal')
          .css('font-size', '20em')
          .css('font-family', testFonts);

      // Remember size, set new font, and then measure again
      var originalWidth = $div.outerWidth();
      $div.data('original-width', originalWidth);
      $div.css('font-family', '\'' + font.family + '\',' + testFonts);
      if (font.style) {
        var style = ($div.attr('style') || '').trim();
        var sep = (style.substr(-1) === ';' ? '' : ';') + (style ? ' ' : '');
        $div.attr('style', style + sep + font.style);
      }

      if ($div.outerWidth() !== originalWidth) {
        // Font already loaded, nothing to do
        $div.remove();
      } else {
        // Remember DIV
        divs.push($div);
      }
    }.bind(this));
    if (divs.length === 0) {
      // No fonts need to be watched, everything is loaded already
      complete(true);
      return;
    }

    var onFinished = complete;
    var timeout = scout.nvl(options.timeout, 30 * 1000); // default timeout is 30 sec
    var watchTimerId, timeoutTimerId;
    if (timeout && timeout >= 0) {
      // Add timeout
      timeoutTimerId = setTimeout(function() {
        clearTimeout(watchTimerId);
        complete(false);
      }.bind(this), timeout);
      onFinished = function() {
        clearTimeout(timeoutTimerId);
        complete(true);
      };
    }

    // Start watching (initially 50ms delay)
    watchWidthChange(50, onFinished);

    // ----- Helper functions -----

    function watchWidthChange(delay, onFinished) {
      // Check each DIV
      var i = divs.length;
      while (i--) {
        var $div = divs[i];
        if ($div.outerWidth() !== $div.data('original-width')) {
          divs.splice(i, 1);
          $div.remove();
        }
      }
      if (divs.length === 0) {
        // All completed
        onFinished(true);
        return;
      }

      // Watch again after a small delay
      watchTimerId = setTimeout(function() {
        // Slowly increase delay up to 1 second
        if (delay < 1000) {
          delay = delay * 1.2;
        }
        watchWidthChange(delay, onFinished);
      }, delay);
    }

    function complete(success) {
      options.onComplete(success);
    }
  }
};
