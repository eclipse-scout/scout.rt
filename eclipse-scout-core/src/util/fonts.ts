/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, graphics, scout, strings} from '../index';
import $ from 'jquery';

export interface FontDescriptor {
  family: string;
  style?: string;
  testString?: string;
  testFonts?: string;
}

export interface FontPreloadOptions {
  /**
   * A single string or object (or an array of them) specifying which fonts should be preloaded.
   * A string is interpreted as font-family.
   * If the style is relevant too, an object with the properties 'family' and 'style' should be provided.
   * Alternatively, the style can be specified in the string after the font name, separated by a pipe character ('|').
   * The property {@link testString} (or a third component in a '|' separated string) may be specified to set the characters to measure for this specific font (can be useful for icon fonts).
   */
  fonts: string | string[] | FontDescriptor | FontDescriptor[];

  /**
   * Mandatory function to be called when all of the specified fonts have been loaded or if a timeout occurs. If this option is omitted, the call to this method returns immediately.
   * @param success indicate whether loading was completed successfully or execution was interrupted by a timeout.
   * @param badFonts The bad fonts
   */
  onComplete?: (success: boolean, badFonts: string[]) => void;

  /**
   * Optional timeout in milliseconds. If fonts could not be loaded within this time, loading is stopped and the {@link onComplete} method is called with argument 'false'.
   * Defaults to {@link TEST_TIMEOUT}.
   */
  timeout?: number;

  /**
   * Optional. Test fonts (string separated by commas) to used as baseline when checking if the specified fonts have been loaded. Defaults to {@link TEST_FONTS}.
   */
  testFonts?: string;

  /**
   * Optional. The test string to use when checking if the specified fonts have been loaded. Should not be empty, because the empty string has always the width 0.
   * The default is {@link TEST_STRING}. The test string may also be specified individually per font.
   */
  testString?: string;
}

export const fonts = {
  _deferred: $.Deferred(),

  /**
   * Indicates whether all fonts have been loaded successfully. Check this variable before
   * waiting for the promise object returned by preloader().
   */
  loadingComplete: true,

  /**
   * Start preloading the specified fonts. If no fonts are specified, the list of fonts
   * to preload is automatically calculated from the available CSS "@font-face" definitions.
   * To disable preloading entirely, pass an empty array to this function.
   *
   * @param fontArr (optional) array of fonts
   * @returns promise that is resolved when all fonts are loaded
   */
  bootstrap(fontArr: FontDescriptor[]): JQuery.Promise<void> {
    fontArr = fontArr || fonts.autoDetectFonts();

    if (fontArr.length === 0) {
      fonts.loadingComplete = true;
      return $.resolvedPromise();
    }

    // Start preloading
    fonts.loadingComplete = false;
    fonts.preload({
      fonts: fontArr,
      onComplete: (success, badFonts) => {
        if (!success && badFonts && badFonts.length) {
          $.log.warn('Timeout occurred while pre-loading the following fonts:\n\n- ' + badFonts.join('\n- ') + '\n\n' +
            'Rendering will now continue, but font measurements may be inaccurate. ' +
            'To prevent unnecessary startup delays and layout problems, check the @font-face ' +
            'definitions and the referenced "src" URLs or programmatically add additional font-specific ' +
            'characters to TEST_STRING before calling app.init().');
        }
        fonts.loadingComplete = true;
        fonts._deferred.resolve();
      }
    });

    return $.resolvedPromise();
  },

  /**
   * @returns  a promise object that is notified when the font preloading was completed.
   *          Important: Before waiting for this promise, always check that value of
   *          loadingComplete first! Do not wait for the promise when loadingComplete
   *          is true, because the promise will never be resolved.
   */
  preloader(): JQuery.Promise<void> {
    return fonts._deferred.promise();
  },

  TEST_FONTS: 'monospace',

  /**
   * Test string used for font measurements. Used to detect when a font is fully loaded
   * and available in the browser.
   *
   * Custom characters may be added to this test string if a font is not detected correctly
   * because it does not contain any of the default characters.
   *
   * U+E000 = Start of Unicode private use zone (e.g. scoutIcons)
   * U+F118 = Font Awesome: "smile"
   */
  TEST_STRING: 'ABC abc 123 .,_ LlIi1 oO0 !#@ \uE000\uE001\uE002 \uF118',

  /**
   * Time in milliseconds to wait for the fonts to be loaded.
   */
  TEST_TIMEOUT: 12 * 1000, // 12 sec

  /**
   * Loads the specified fonts in a hidden div, forcing the browser to load them.
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
  preload(options?: FontPreloadOptions) {
    options = options || {fonts: null};
    let fontArr = arrays.ensure(options.fonts);
    if (!options.onComplete) {
      // preloading is not useful, because there is no callback on success
      return;
    }

    // Create a DIV for each font
    let divs = [];
    fontArr.forEach(font => {
      // Convert to object
      if (typeof font === 'string') {
        let fontParts = strings.splitMax(font, '|', 3).map(s => {
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
      font.testString = font.testString || options.testString || fonts.TEST_STRING;

      // these fonts are compared to the custom fonts, strings separated by comma
      let testFonts = font.testFonts || options.testFonts || fonts.TEST_FONTS;

      // Create DIV with default fonts
      // (Because preloader functionality should not depend on a CSS style sheet we set the required properties programmatically.)
      let $div = $('body').appendDiv('font-preloader')
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
      let originalSize = fonts.measureSize($div);
      $div.data('original-size', originalSize);
      $div.data('font-family', font.family);
      $div.css('font-family', '\'' + font.family + '\',' + testFonts);
      if (font.style) {
        let style = ($div.attr('style') || '').trim();
        let sep = (style.substr(-1) === ';' ? '' : ';') + (style ? ' ' : '');
        $div.attr('style', style + sep + font.style);
      }

      if (fonts.measureSize($div) !== originalSize) {
        // Font already loaded, nothing to do
        $div.remove();
      } else {
        // Remember DIV
        divs.push($div);
      }
    });
    if (divs.length === 0) {
      // No fonts need to be watched, everything is loaded already
      complete(true);
      return;
    }

    let onFinished = complete;
    let timeout = scout.nvl(options.timeout, fonts.TEST_TIMEOUT);
    let watchTimerId, timeoutTimerId;
    if (timeout && timeout >= 0) {
      // Add timeout
      timeoutTimerId = setTimeout(() => {
        clearTimeout(watchTimerId);
        complete(false);
      }, timeout);
      onFinished = () => {
        clearTimeout(timeoutTimerId);
        complete(true);
      };
    }

    // Start watching (initially 50ms delay)
    watchWidthChange(50, onFinished);

    // ----- Helper functions -----

    function watchWidthChange(delay, onFinished) {
      // Check each DIV
      let i = divs.length;
      while (i--) {
        let $div = divs[i];
        if (fonts.measureSize($div) !== $div.data('original-size')) {
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
      watchTimerId = setTimeout(() => {
        // Slowly increase delay up to 1 second
        if (delay < 1000) {
          delay = delay * 1.2;
        }
        watchWidthChange(delay, onFinished);
      }, delay);
    }

    function complete(success) {
      options.onComplete(success, divs.map($div => {
        return $div.data('font-family');
      }));
    }
  },

  measureSize($div: JQuery): string {
    let size = graphics.size($div, {
      exact: true
    });
    return size.width + 'x' + size.height;
  },

  /**
   * Reads all "@font-face" CSS rules from the current document and returns an array of
   * font definition objects, suitable for passing to the preload() function (see above).
   */
  autoDetectFonts(): FontDescriptor[] {
    let fontArr = [];
    // Implementation note: "styleSheets" and "cssRules" are not arrays (they only look like arrays)
    let styleSheets = document.styleSheets;
    for (let i = 0; i < styleSheets.length; i++) {
      let styleSheet = styleSheets[i];
      let cssRules;
      try {
        cssRules = styleSheet.cssRules;
      } catch (error) {
        // In some browsers, access to style sheets of other origins is blocked:
        // https://developer.mozilla.org/en-US/docs/Web/API/CSSStyleSheet#Notes
        $.log.info('Skipped automatic font detection for style sheet ' + styleSheet.href +
          ' (access blocked by browser). Use the bootstrap argument "fonts" to manually list fonts to pre-load.');
        continue;
      }
      for (let j = 0; j < styleSheet.cssRules.length; j++) {
        let cssRule = styleSheet.cssRules[j];
        if (cssRule.type === window.CSSRule.FONT_FACE_RULE) {
          // @ts-expect-error
          let style = cssRule.style;
          let ff = style.getPropertyValue('font-family');
          let fw = style.getPropertyValue('font-weight');
          let fs = style.getPropertyValue('font-style');
          let fv = style.getPropertyValue('font-variant');
          let ft = style.getPropertyValue('font-stretch');
          if (ff) {
            ff = ff.replace(/^["']|["']$/g, ''); // Unquote strings, they will be quoted again automatically
            let s = [];
            if (fw && fw !== 'normal') {
              s.push('font-weight:' + fw);
            }
            if (fs && fs !== 'normal') {
              s.push('font-style:' + fs);
            }
            if (fv && fv !== 'normal') {
              s.push('font-variant:' + fv);
            }
            if (ft && ft !== 'normal') {
              s.push('font-stretch:' + ft);
            }
            let font: FontDescriptor = {
              family: ff
            };
            if (s.length) {
              font.style = s.join(';');
            }
            fontArr.push(font);
          }
        }
      }
    }
    return fontArr;
  }
};
