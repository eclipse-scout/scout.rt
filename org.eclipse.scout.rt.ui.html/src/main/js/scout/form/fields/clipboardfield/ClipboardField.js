scout.ClipboardField = function() {
  scout.ClipboardField.parent.call(this);

  this._fileUploadWaitRetryCountTimeout = 99;
};
scout.inherits(scout.ClipboardField, scout.ValueField);

scout.ClipboardField.prototype._render = function($parent) {
  this.addContainer($parent, 'clipboard-field');
  this.addLabel();
  this.addField($('<div>'));
  this.addStatus();

  // add drag and drop support
  this.dragAndDropHandler = scout.dragAndDrop.handler(this,
      scout.dragAndDrop.SCOUT_TYPES.FILE_TRANSFER,
      function() { return this.dropType; }.bind(this),
      function() { return this.maximumSize; }.bind(this),
      undefined,
      function() { return this.allowedMimeTypes; }.bind(this));
  this.dragAndDropHandler.install(this.$field);

  this.$field.attr('contenteditable', 'true')
    .on('paste', this._onPaste.bind(this));

  $parent.on('click', function(event) {
    this.session.focusManager.requestFocus(this.$field);
    }.bind(this));

  if (this.rendered) {
    this.session.focusManager.requestFocus(this.$field);
  }
};

scout.ClipboardField.prototype._renderDisplayText = function(displayText) {
  if (scout.strings.hasText(displayText)) {
    this.$field.html(scout.strings.nl2br(displayText, true));
    scout.scrollbars.install(this.$field, this.session);
    this.$field.selectAllText();
  } else {
    this.$field.empty();
  }
};

scout.ClipboardField.prototype._onPaste = function(event) {
  var dataTransfer;
  if (event.originalEvent.clipboardData) {
    dataTransfer = event.originalEvent.clipboardData;
  } else if (window.clipboardData) {
    dataTransfer = window.clipboardData;
  } else {
    // unable to obtain data transfer object
    throw new Error('Unable to access clipboard data.');
  }

  var filesArgument = [],   // options to be uploaded, arguments for this.session.uploadFiles
    additionalOptions = {},
    additionalOptionsCompatibilityIndex = 0,   // counter for additional options
    contentCount = 0;

  // some browsers (e.g. IE) specify text content simply as data of type 'Text', it is not listed in list of types
  var textContent;
  textContent = dataTransfer.getData('Text');
  if (textContent) {
    if (window.Blob) {
      filesArgument.push(new Blob([textContent], {type: scout.mimeTypes.TEXT_PLAIN}));
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
          filesArgument.push(new Blob([str], {type: scout.mimeTypes.TEXT_PLAIN}));
          contentCount++;
        });
      }
      else if (scout.helpers.isOneOf(item.type, [scout.mimeTypes.IMAGE_PNG, scout.mimeTypes.IMAGE_JPG, scout.mimeTypes.IMAGE_JPEG, scout.mimeTypes.IMAGE_GIF])) {
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
        var f = new Blob([event.target.result], {type: item.type});
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
      scout.scrollbars.install(this.$field, this.session);
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

              if (scout.helpers.isOneOf(srcDataMatch[1], [scout.mimeTypes.IMAGE_PNG, scout.mimeTypes.IMAGE_JPG, scout.mimeTypes.IMAGE_JPEG, scout.mimeTypes.IMAGE_GIF])) {
                for (var i = 0; i < encData.length; i++) {
                    byteNumbers[i] = encData.charCodeAt(i);
                }
                var byteArray = new Uint8Array(byteNumbers);

                var f = new Blob([byteArray], {type: srcDataMatch[1]});
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
