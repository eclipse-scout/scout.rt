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
scout.ClipboardField = function() {
  scout.ClipboardField.parent.call(this);

  this.dropType = 0;
  this.dropMaximumSize = scout.dragAndDrop.DEFAULT_DROP_MAXIMUM_SIZE;
  this._fileUploadWaitRetryCountTimeout = 99;
  this._fullSelectionLength = 0;
};
scout.inherits(scout.ClipboardField, scout.ValueField);

// Keys that don't alter the content of a text field and are therefore always allowed in the clipboard field
scout.ClipboardField.NON_DESTRUCTIVE_KEYS = [
  // Default form handling
  scout.keys.ESC,
  scout.keys.ENTER,
  scout.keys.TAB,
  // Navigate and mark text
  scout.keys.PAGE_UP,
  scout.keys.PAGE_DOWN,
  scout.keys.END,
  scout.keys.HOME,
  scout.keys.LEFT,
  scout.keys.UP,
  scout.keys.RIGHT,
  scout.keys.DOWN,
  // Browser hotkeys (e.g. developer tools)
  scout.keys.F1,
  scout.keys.F2,
  scout.keys.F3,
  scout.keys.F4,
  scout.keys.F5,
  scout.keys.F6,
  scout.keys.F7,
  scout.keys.F8,
  scout.keys.F9,
  scout.keys.F10,
  scout.keys.F11,
  scout.keys.F12
];

// Keys that always alter the content of a text field, independent from the modifier keys
scout.ClipboardField.ALWAYS_DESTRUCTIVE_KEYS = [
  scout.keys.BACKSPACE,
  scout.keys.DELETE
];

/**
 * @override Widget.js
 */
scout.ClipboardField.prototype._createKeyStrokeContext = function() {
  return new scout.InputFieldKeyStrokeContext();
};

scout.ClipboardField.prototype._render = function() {
  // We don't use makeDiv() here intentionally because the DIV created must
  // not have the 'unselectable' attribute. Otherwise clipboard-field will
  // not work in IE9.
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

  this.$parent.on('click', function(event) {
    this.focus();
  }.bind(this));
};

scout.ClipboardField.prototype._renderProperties = function() {
  scout.ClipboardField.parent.prototype._renderProperties.call(this);
  this._renderDropType();
};

scout.ClipboardField.prototype._createDragAndDropHandler = function() {
  return scout.dragAndDrop.handler(this, {
    supportedScoutTypes: scout.dragAndDrop.SCOUT_TYPES.FILE_TRANSFER,
    dropType: function() {
      return this.dropType;
    }.bind(this),
    dropMaximumSize: function() {
      return this.maximumSize;
    }.bind(this),
    allowedTypes: function() {
      return this.allowedMimeTypes;
    }.bind(this)
  });
};

scout.ClipboardField.prototype._renderDisplayText = function() {
  var displayText = this.displayText;
  var img;
  this.$field.children().each(function(idx, elem) {
    if (!img && elem.nodeName === 'IMG') {
      img = elem;
    }
  });

  if (scout.strings.hasText(displayText)) {
    this.$field.html(scout.strings.nl2br(displayText, true));
    scout.scrollbars.install(this.$field, {
      parent: this
    });

    setTimeout(function() {
      this.$field.selectAllText();
      // store length of full selection, in order to determine if the whole text is selected in "onCopy"
      var selection = this._getSelection();
      this._fullSelectionLength = (selection) ? selection.toString().length : 0;
    }.bind(this));
  } else {
    this.$field.empty();
  }
  // restore old img for firefox upload mechanism.
  if (img) {
    this.$field.prepend(img);
  }
};

// Because a <div> is used as field, jQuery's val() used in ValueField.js is not working here, so
// the content of displayText variable is used instead.
// (For reading the displayText innerHmtl() _could_ be used on the div-field, but some browsers
// would collapse whitespaces which would also collapse multiple tabs when coping some table rows.
// So instead of reading the effective displayText from the field, the internal displayText value
// will be reused without actual reading. Parsing of pasted content is handled onPaste() and stored
// in this.displayText.)
scout.ClipboardField.prototype._readDisplayText = function() {
  return this.displayText;
};

scout.ClipboardField.prototype._getSelection = function() {
  var selection, myWindow = this.$container.window(true);
  if (myWindow.getSelection) {
    selection = myWindow.getSelection();
  } else if (document.getSelection) {
    selection = document.getSelection();
  }
  if (!selection || selection.toString().length === 0) {
    return null;
  }
  return selection;
};

  scout.ClipboardField.prototype._onKeyDown = function(event) {
  if (scout.isOneOf(event.which, scout.ClipboardField.ALWAYS_DESTRUCTIVE_KEYS)) {
    return false; // never allowed
  }
  if (event.ctrlKey || event.altKey || event.metaKey || scout.isOneOf(event.which, scout.ClipboardField.NON_DESTRUCTIVE_KEYS)) {
    return; // allow bubble to other event handlers
  }
  // do not allow to enter something manually
  return false;
};

scout.ClipboardField.prototype._onInput = function(event) {
  // if the user somehow managed to fire to input something (e.g. "delete" menu in FF & IE), just reset the value to the previous content
    this._renderDisplayText();
  return false;
};

scout.ClipboardField.prototype._onCopy = function(event) {
  var selection, text, dataTransfer, myWindow = this.$container.window(true);
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
  scout.scrollbars.uninstall(this.$field, this.session);

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
  scout.scrollbars.install(this.$field, {
    parent: this
  });

  return false;
};

scout.ClipboardField.prototype._onPaste = function(event) {
  if (this.readOnly) {
    // Prevent pasting in "copy" mode
    return false;
  }

  var startPasteTimestamp = Date.now();
  var dataTransfer, myWindow = this.$container.window(true);
  this.$field.selectAllText();
  if (event.originalEvent.clipboardData) {
    dataTransfer = event.originalEvent.clipboardData;
  } else if (myWindow.clipboardData) {
    dataTransfer = myWindow.clipboardData;
  } else {
    // unable to obtain data transfer object
    throw new Error('Unable to access clipboard data.');
  }

  var filesArgument = [], // options to be uploaded, arguments for this.session.uploadFiles
    additionalOptions = {},
    additionalOptionsCompatibilityIndex = 0, // counter for additional options
    contentCount = 0;

  // some browsers (e.g. IE) specify text content simply as data of type 'Text', it is not listed in list of types
  var textContent = dataTransfer.getData('Text');
  if (textContent) {
    if (window.Blob) {
      filesArgument.push(new Blob([textContent], {
        type: scout.mimeTypes.TEXT_PLAIN
      }));
      contentCount++;
    } else {
      // compatibility workaround
      additionalOptions['textTransferObject' + additionalOptionsCompatibilityIndex++] = textContent;
      contentCount++;
    }
  }

  if (contentCount === 0 && dataTransfer.items) {
    Array.prototype.forEach.call(dataTransfer.items, function(item) {
      if (item.type === scout.mimeTypes.TEXT_PLAIN) {
        item.getAsString(function(str) {
          filesArgument.push(new Blob([str], {
            type: scout.mimeTypes.TEXT_PLAIN
          }));
          contentCount++;
        });
      } else if (scout.isOneOf(item.type, [scout.mimeTypes.IMAGE_PNG, scout.mimeTypes.IMAGE_JPG, scout.mimeTypes.IMAGE_JPEG, scout.mimeTypes.IMAGE_GIF])) {
        var file = item.getAsFile();
        if (file) {
          // When pasting an image from the clipboard, Chrome and Firefox create a File object with
          // a generic name such as "image.png" or "grafik.png" (hardcoded in Chrome, locale-dependent
          // in FF). It is therefore not possible to distinguish between a real file and a bitmap
          // from the clipboard. The following code measures the time between the start of the paste
          // event and the file's last modified timestamp. If it is "very small", the file is likely
          // a bitmap from the clipbaord and not a real file. In that case, add a special "scoutName"
          // attribute to the file object that is then used as a filename in session.uploadFiles().
          var lastModifiedDiff = startPasteTimestamp - file.lastModified;
          if (lastModifiedDiff < 1000) {
            file.scoutName = '';
          }
          filesArgument.push(file);
          contentCount++;
        }
      }
    });
  }

  var waitForFileReaderEvents = 0;
  if (contentCount === 0 && dataTransfer.files) {
    Array.prototype.forEach.call(dataTransfer.files, function(item) {
      var reader = new FileReader();
      // register functions for file reader
      reader.onload = function(event) {
        var f = new Blob([event.target.result], {
          type: item.type
        });
        f.name = item.name;
        filesArgument.push(f);
        waitForFileReaderEvents--;
      };
      reader.onerror = function(event) {
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
  var uploadFunctionTimeoutCount = 0;
  var uploadFunction = function() {
    if (waitForFileReaderEvents !== 0 && uploadFunctionTimeoutCount++ !== this._fileUploadWaitRetryCountTimeout) {
      setTimeout(uploadFunction, 150);
      return;
    }

    if (uploadFunctionTimeoutCount >= this._fileUploadWaitRetryCountTimeout) {
      var boxOptions = {
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
  }.bind(this);

  // upload content function, if content can not be read from event
  // (e.g. "Allow programmatic clipboard access" is disabled in IE)
  var uploadContentFunction = function() {
    // store old inner html (will be replaced)
    scout.scrollbars.uninstall(this.$field, this.session);
    var oldHtmlContent = this.$field.html();
    this.$field.html('');
    var restoreOldHtmlContent = function() {
      this.$field.html(oldHtmlContent);
      scout.scrollbars.install(this.$field, {
        parent: this
      });
    }.bind(this);
    setTimeout(function() {
      var imgElementsFound = false;
      this.$field.children().each(function(idx, elem) {
        if (elem.nodeName === 'IMG') {
          var srcAttr = $(elem).attr('src');
          var srcDataMatch = /^data:(.*);base64,(.*)/.exec(srcAttr);
          var mimeType = srcDataMatch && srcDataMatch[1];
          if (scout.isOneOf(mimeType, scout.mimeTypes.IMAGE_PNG, scout.mimeTypes.IMAGE_JPG, scout.mimeTypes.IMAGE_JPEG, scout.mimeTypes.IMAGE_GIF)) {
            var encData = window.atob(srcDataMatch[2]); // base64 decode
            var byteNumbers = [];
            for (var i = 0; i < encData.length; i++) {
              byteNumbers[i] = encData.charCodeAt(i);
            }
            var byteArray = new Uint8Array(byteNumbers);
            var f = new Blob([byteArray], {
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
        var nativePasteContent = this.$field.text();
        if (scout.strings.hasText(nativePasteContent)) {
          this.setDisplayText(nativePasteContent);
          filesArgument.push(new Blob([nativePasteContent], {
            type: scout.mimeTypes.TEXT_PLAIN
          }));
        } else {
          restoreOldHtmlContent();
        }
      }
      uploadFunction();
    }.bind(this), 0);
  }.bind(this);

  if (contentCount > 0) {
    uploadFunction();

    // do not trigger any other actions
    return false;
  } else {
    uploadContentFunction();

    // trigger other actions to catch content
    return true;
  }
};
