/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays, Device, InputFieldKeyStrokeContext, keys, mimeTypes, scout, Session, strings, ValueField} from '../../../index';
import $ from 'jquery';

export default class ClipboardField extends ValueField {

  constructor() {
    super();

    this.allowedMimeTypes = null;
    this.maximumSize = null;
    this._fileUploadWaitRetryCountTimeout = 99;
    this._fullSelectionLength = 0;
  }

  // Keys that don't alter the content of a text field and are therefore always allowed in the clipboard field
  static NON_DESTRUCTIVE_KEYS = [
    // Default form handling
    keys.ESC,
    keys.ENTER,
    keys.TAB,
    // Navigate and mark text
    keys.PAGE_UP,
    keys.PAGE_DOWN,
    keys.END,
    keys.HOME,
    keys.LEFT,
    keys.UP,
    keys.RIGHT,
    keys.DOWN,
    // Browser hotkeys (e.g. developer tools)
    keys.F1,
    keys.F2,
    keys.F3,
    keys.F4,
    keys.F5,
    keys.F6,
    keys.F7,
    keys.F8,
    keys.F9,
    keys.F10,
    keys.F11,
    keys.F12
  ];

  // Keys that always alter the content of a text field, independent from the modifier keys
  static ALWAYS_DESTRUCTIVE_KEYS = [
    keys.BACKSPACE,
    keys.DELETE
  ];

  /**
   * @override Widget.js
   */
  _createKeyStrokeContext() {
    return new InputFieldKeyStrokeContext();
  }

  _render() {
    // We don't use makeDiv() here intentionally because the DIV created must
    // not have the 'unselectable' attribute.
    this.addContainer(this.$parent, 'clipboard-field');
    this.addLabel();
    this.addField(this.$parent.makeElement('<div>').addClass('input-field'));
    this.addStatus();

    this.$field
      .disableSpellcheck()
      .attr('contenteditable', true)
      .attr('tabindex', '0')
      .on('keydown', this._onKeyDown.bind(this))
      .on('input', this._onInput.bind(this))
      .on('paste', this._onPaste.bind(this))
      .on('copy', this._onCopy.bind(this))
      .on('cut', this._onCopy.bind(this));
  }

  _getDragAndDropHandlerOptions() {
    let options = super._getDragAndDropHandlerOptions();
    options.allowedTypes = () => this.allowedMimeTypes;
    // use the smaller property (this.maximumSize for backwards compatibility) but ignore null values which would result in a maximum size of zero.
    options.dropMaximumSize = () => Math.min(scout.nvl(this.dropMaximumSize, Number.MAX_VALUE), scout.nvl(this.maximumSize, Number.MAX_VALUE));
    return options;
  }

  _renderDisplayText() {
    let displayText = this.displayText;
    let img;
    this.$field.children().each((idx, elem) => {
      if (!img && elem.nodeName === 'IMG') {
        img = elem;
      }
    });

    if (strings.hasText(displayText)) {
      this.$field.html(strings.nl2br(displayText, true));
      this._installScrollbars();

      setTimeout(() => {
        this.$field.selectAllText();
        // store length of full selection, in order to determine if the whole text is selected in "onCopy"
        let selection = this._getSelection();
        this._fullSelectionLength = (selection) ? selection.toString().length : 0;
      });
    } else {
      this.$field.empty();
    }
    // restore old img for firefox upload mechanism.
    if (img) {
      this.$field.prepend(img);
    }
  }

  // Because a <div> is used as field, jQuery's val() used in ValueField.js is not working here, so
  // the content of displayText variable is used instead.
  // (For reading the displayText innerHmtl() _could_ be used on the div-field, but some browsers
  // would collapse whitespaces which would also collapse multiple tabs when coping some table rows.
  // So instead of reading the effective displayText from the field, the internal displayText value
  // will be reused without actual reading. Parsing of pasted content is handled onPaste() and stored
  // in this.displayText.)
  _readDisplayText() {
    return this.displayText;
  }

  _getSelection() {
    let selection, myWindow = this.$container.window(true);
    if (myWindow.getSelection) {
      selection = myWindow.getSelection();
    } else if (document.getSelection) {
      selection = document.getSelection();
    }
    if (!selection || selection.toString().length === 0) {
      return null;
    }
    return selection;
  }

  _onKeyDown(event) {
    if (scout.isOneOf(event.which, ClipboardField.ALWAYS_DESTRUCTIVE_KEYS)) {
      return false; // never allowed
    }
    if (event.ctrlKey || event.altKey || event.metaKey || scout.isOneOf(event.which, ClipboardField.NON_DESTRUCTIVE_KEYS)) {
      return; // allow bubble to other event handlers
    }
    // do not allow to enter something manually
    return false;
  }

  _onInput(event) {
    // if the user somehow managed to fire to input something (e.g. "delete" menu in FF & IE), just reset the value to the previous content
    this._renderDisplayText();
    return false;
  }

  _onCopy(event) {
    if (Device.get().isIos() && this._onIosCopy(event) === false) {
      return;
    }

    let selection, text, dataTransfer, myWindow = this.$container.window(true);
    try {
      if (event.originalEvent.clipboardData) {
        dataTransfer = event.originalEvent.clipboardData;
      } else if (myWindow.clipboardData) {
        dataTransfer = myWindow.clipboardData;
      }
    } catch (e) {
      // Because windows forbids concurrent access to the clipboard, a possible exception is thrown on 'myWindow.clipboardData'
      // (see Remarks on https://msdn.microsoft.com/en-us/library/windows/desktop/ms649048(v=vs.85).aspx)
      // Because of this behavior a failed access will just be logged but not presented to the user.
      $.log.error('Error while reading "clipboardData"', e);
    }
    if (!dataTransfer) {
      $.log.error('Unable to access clipboard data.');
      return false;
    }

    // scroll bar must not be in field when copying
    this._uninstallScrollbars();

    selection = this._getSelection();
    if (!selection) {
      return;
    }

    // if the length of the selection is equals to the length of the (initial) full selection
    // use the internal 'displayText' value because some browsers are collapsing white spaces
    // which lead to problems when coping data form tables with empty cells ("\t\t").
    if (selection.toString().length === this._fullSelectionLength) {
      text = this.displayText;
    } else {
      text = selection.toString();
    }

    try {
      // Chrome, Firefox - causes an exception in IE
      dataTransfer.setData('text/plain', text);
    } catch (e) {
      // IE, see https://www.lucidchart.com/techblog/2014/12/02/definitive-guide-copying-pasting-javascript/
      dataTransfer.setData('Text', text);
    }

    // (re)install scroll bars
    this._installScrollbars();

    return false;
  }

  _onIosCopy(event) {
    // Setting custom clipboard data is not possible with iOS due to a WebKit bug.
    // The default behavior copies rich text. Since it is not expected to copy the style of the clipboard field, temporarily set color and background-color
    // https://bugs.webkit.org/show_bug.cgi?id=176980
    let oldStyle = this.$field.attr('style');
    this.$field.css({
      'color': '#000',
      'background-color': 'transparent'
    });
    setTimeout(() => {
      this.$field.attrOrRemove('style', oldStyle);
    });
    return false;
  }

  _onPaste(event) {
    if (this.readOnly) {
      // Prevent pasting in "copy" mode
      return false;
    }

    let startPasteTimestamp = Date.now();
    let dataTransfer, myWindow = this.$container.window(true);
    this.$field.selectAllText();
    if (event.originalEvent.clipboardData) {
      dataTransfer = event.originalEvent.clipboardData;
    } else if (myWindow.clipboardData) {
      dataTransfer = myWindow.clipboardData;
    } else {
      // unable to obtain data transfer object
      throw new Error('Unable to access clipboard data.');
    }

    let filesArgument = [], // options to be uploaded, arguments for this.session.uploadFiles
      additionalOptions = {},
      additionalOptionsCompatibilityIndex = 0, // counter for additional options
      contentCount = 0;

    // some browsers (e.g. IE) specify text content simply as data of type 'Text', it is not listed in list of types
    let textContent = dataTransfer.getData('Text');
    if (textContent) {
      if (window.Blob) {
        filesArgument.push(new Blob([textContent], {
          type: mimeTypes.TEXT_PLAIN
        }));
        contentCount++;
      } else {
        // compatibility workaround
        additionalOptions['textTransferObject' + additionalOptionsCompatibilityIndex++] = textContent;
        contentCount++;
      }
    }

    if (contentCount === 0 && dataTransfer.items) {
      Array.prototype.forEach.call(dataTransfer.items, item => {
        if (item.type === mimeTypes.TEXT_PLAIN) {
          item.getAsString(str => {
            filesArgument.push(new Blob([str], {
              type: mimeTypes.TEXT_PLAIN
            }));
            contentCount++;
          });
        } else if (scout.isOneOf(item.type, [mimeTypes.IMAGE_PNG, mimeTypes.IMAGE_JPG, mimeTypes.IMAGE_JPEG, mimeTypes.IMAGE_GIF])) {
          let file = item.getAsFile();
          if (file) {
            // When pasting an image from the clipboard, Chrome and Firefox create a File object with
            // a generic name such as "image.png" or "grafik.png" (hardcoded in Chrome, locale-dependent
            // in FF). It is therefore not possible to distinguish between a real file and a bitmap
            // from the clipboard. The following code measures the time between the start of the paste
            // event and the file's last modified timestamp. If it is "very small", the file is likely
            // a bitmap from the clipboard and not a real file. In that case, add a special "scoutName"
            // attribute to the file object that is then used as a filename in session.uploadFiles().
            let lastModifiedDiff = startPasteTimestamp - file.lastModified;
            if (lastModifiedDiff < 1000) {
              file.scoutName = Session.EMPTY_UPLOAD_FILENAME;
            }
            filesArgument.push(file);
            contentCount++;
          }
        }
      });
      this._cleanupFiles(filesArgument);
    }

    let waitForFileReaderEvents = 0;
    if (contentCount === 0 && dataTransfer.files) {
      Array.prototype.forEach.call(dataTransfer.files, item => {
        let reader = new FileReader();
        // register functions for file reader
        reader.onload = event => {
          let f = new Blob([event.target.result], {
            type: item.type
          });
          f.name = item.name;
          filesArgument.push(f);
          waitForFileReaderEvents--;
        };
        reader.onerror = event => {
          waitForFileReaderEvents--;
          $.log.error('Error while reading file ' + item.name + ' / ' + event.target.error.code);
        };
        // start file reader
        waitForFileReaderEvents++;
        contentCount++;
        reader.readAsArrayBuffer(item);
      });
    }

    // upload function needs to be called asynchronously to support real files
    let uploadFunctionTimeoutCount = 0;
    let uploadFunction = () => {
      if (waitForFileReaderEvents !== 0 && uploadFunctionTimeoutCount++ !== this._fileUploadWaitRetryCountTimeout) {
        setTimeout(uploadFunction, 150);
        return;
      }

      if (uploadFunctionTimeoutCount >= this._fileUploadWaitRetryCountTimeout) {
        let boxOptions = {
          entryPoint: this.$container.entryPoint(),
          header: this.session.text('ui.ClipboardTimeoutTitle'),
          body: this.session.text('ui.ClipboardTimeout'),
          yesButtonText: this.session.text('Ok')
        };

        this.session.showFatalMessage(boxOptions);
        return;
      }

      // upload paste event as files
      if (filesArgument.length > 0 || Object.keys(additionalOptions).length > 0) {
        this.session.uploadFiles(this, filesArgument, additionalOptions, this.maximumSize, this.allowedMimeTypes);
      }
    };

    // upload content function, if content can not be read from event
    // (e.g. "Allow programmatic clipboard access" is disabled in IE)
    let uploadContentFunction = () => {
      // store old inner html (will be replaced)
      this._uninstallScrollbars();
      let oldHtmlContent = this.$field.html();
      this.$field.html('');
      let restoreOldHtmlContent = () => {
        this.$field.html(oldHtmlContent);
        this._installScrollbars();
      };
      setTimeout(() => {
        let imgElementsFound = false;
        this.$field.children().each((idx, elem) => {
          if (elem.nodeName === 'IMG') {
            let srcAttr = $(elem).attr('src');
            let srcDataMatch = /^data:(.*);base64,(.*)/.exec(srcAttr);
            let mimeType = srcDataMatch && srcDataMatch[1];
            if (scout.isOneOf(mimeType, mimeTypes.IMAGE_PNG, mimeTypes.IMAGE_JPG, mimeTypes.IMAGE_JPEG, mimeTypes.IMAGE_GIF)) {
              let encData = window.atob(srcDataMatch[2]); // base64 decode
              let byteNumbers = [];
              for (let i = 0; i < encData.length; i++) {
                byteNumbers[i] = encData.charCodeAt(i);
              }
              let byteArray = new Uint8Array(byteNumbers);
              let f = new Blob([byteArray], {
                type: mimeType
              });
              f.name = '';
              filesArgument.push(f);
              imgElementsFound = true;
            }
          }
        });
        if (imgElementsFound) {
          restoreOldHtmlContent();
        } else {
          // try to read nativly pasted text from field
          let nativePasteContent = this.$field.text();
          if (strings.hasText(nativePasteContent)) {
            this.setDisplayText(nativePasteContent);
            filesArgument.push(new Blob([nativePasteContent], {
              type: mimeTypes.TEXT_PLAIN
            }));
          } else {
            restoreOldHtmlContent();
          }
        }
        uploadFunction();
      }, 0);
    };

    if (contentCount > 0) {
      uploadFunction();

      // do not trigger any other actions
      return false;
    }
    uploadContentFunction();

    // trigger other actions to catch content
    return true;
  }

  /**
   * Safari creates two files when pasting an image from clipboard, one PNG and one JPEG.
   * If that happens, remove the JPEG and only keep the PNG.
   */
  _cleanupFiles(files) {
    if (files.length !== 2) {
      return;
    }
    let pngImage;
    let jpgImage;
    files.forEach(file => {
      // Check for the scoutName because it will only be set if it is likely a paste from clipboard event
      if (file.name === 'image.png' && file.scoutName) {
        pngImage = file;
      } else if (file.name === 'image.jpeg' && file.scoutName) {
        jpgImage = file;
      }
    });
    if (pngImage && jpgImage) {
      arrays.remove(files, jpgImage);
    }
  }
}
