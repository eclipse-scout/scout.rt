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
scout.ClipboardField = function() {
  scout.ClipboardField.parent.call(this);

  this._fileUploadWaitRetryCountTimeout = 99;
};
scout.inherits(scout.ClipboardField, scout.ValueField);

/**
 * @override Widget.js
 */
scout.ClipboardField.prototype._createKeyStrokeContext = function() {
  return new scout.InputFieldKeyStrokeContext();
};

scout.ClipboardField.prototype._render = function($parent) {
  // We don't use makeDiv() here intentionally because the DIV created must
  // not have the 'unselectable' attribute. Otherwise clipboard-field will
  // not work in IE9.
  this.addContainer($parent, 'clipboard-field');
  this.addLabel();
  this.addField($parent.makeElement('<div>'));
  this.addStatus();

  this.$field
    .attr('contenteditable', 'true')
    .attr('tabindex', '0')
    .on('paste', this._onPaste.bind(this))
    .on('copy', this._onCopy.bind(this))
    .on('cut', this._onCopy.bind(this));

  $parent.on('click', function(event) {
    this.session.focusManager.requestFocus(this.$field);
  }.bind(this));

  if (this.rendered) {
    this.session.focusManager.requestFocus(this.$field);
  }
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

scout.ClipboardField.prototype._renderDisplayText = function(displayText) {
  if (scout.strings.hasText(displayText)) {
    this.$field.html(scout.strings.nl2br(displayText, true));
    scout.scrollbars.install(this.$field, {
      parent: this
    });
    this.$field.selectAllText();
  } else {
    this.$field.empty();
  }
};

scout.ClipboardField.prototype._onCopy = function(event) {
  var dataTransfer, myWindow = this.$container.window(true);
  if (event.originalEvent.clipboardData) {
    dataTransfer = event.originalEvent.clipboardData;
  } else if (myWindow.clipboardData) {
    dataTransfer = myWindow.clipboardData;
  } else {
    // unable to obtain data transfer object
    throw new Error('Unable to access clipboard data.');
  }

  // scroll bar must not be in field when copying
  scout.scrollbars.uninstall(this.$field, this.session);

  // do not use this.$field.text(), it suppresses new lines
  var text = scout.strings.plainText(this.$field.html());
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
  var dataTransfer, myWindow = this.$container.window(true);
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
  var textContent;
  textContent = dataTransfer.getData('Text');
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

  if (dataTransfer.items) {
    $.each(dataTransfer.items, function(idx, item) {
      if (item.type === scout.mimeTypes.TEXT_PLAIN) {
        item.getAsString(function(str) {
          filesArgument.push(new Blob([str], {
            type: scout.mimeTypes.TEXT_PLAIN
          }));
          contentCount++;
        });
      } else if (scout.isOneOf(item.type, [scout.mimeTypes.IMAGE_PNG, scout.mimeTypes.IMAGE_JPG, scout.mimeTypes.IMAGE_JPEG, scout.mimeTypes.IMAGE_GIF])) {
        filesArgument.push(item.getAsFile());
        contentCount++;
      }
    });
  }

  var waitForFileReaderEvents = 0;
  if (dataTransfer.files) {
    $.each(dataTransfer.files, function(idx, item) {
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
        $.log.error('Error during file upload ' + item.name + ' / ' + event.target.error.code);
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
      $.each(this.$field.children(), function(idx, elem) {
        if (elem.nodeName === 'IMG') {
          var $elem = $(elem),
            srcAttr = $elem.attr('src');

          if (srcAttr && scout.strings.startsWith(srcAttr, 'data:')) {
            var srcDataMatch = /data:(.*);base64/g.exec(srcAttr);
            if (srcDataMatch) {
              var srcArray = srcAttr.split(','),
                encData = window.atob(srcArray[1]),
                byteNumbers = new Array(encData.length);

              if (scout.isOneOf(srcDataMatch[1], [scout.mimeTypes.IMAGE_PNG, scout.mimeTypes.IMAGE_JPG, scout.mimeTypes.IMAGE_JPEG, scout.mimeTypes.IMAGE_GIF])) {
                for (var i = 0; i < encData.length; i++) {
                  byteNumbers[i] = encData.charCodeAt(i);
                }
                var byteArray = new Uint8Array(byteNumbers);

                var f = new Blob([byteArray], {
                  type: srcDataMatch[1]
                });
                f.name = '';
                filesArgument.push(f);
              }
            }
          }
        }
      }.bind(this));

      restoreOldHtmlContent();
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
