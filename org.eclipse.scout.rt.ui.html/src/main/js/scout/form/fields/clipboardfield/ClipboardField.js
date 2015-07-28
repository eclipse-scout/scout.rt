scout.ClipboardField = function() {
  scout.ClipboardField.parent.call(this);
};
scout.inherits(scout.ClipboardField, scout.ValueField);

scout.ClipboardField.prototype._render = function($parent) {
  this.addContainer($parent, 'clipboard-field');
  this.addLabel();
  this.addField($('<div>'));
  this.addStatus();

  this.$field.attr('contenteditable', '')
    .on('paste', this._onPaste.bind(this));
    //.on('copy', this._onPaste.bind(this));

  $parent.on('click', function(event) { this.$field.focus(); }.bind(this));

  if (this.rendered) {
    scout.focusManager.requestFocus(this.session.uiSessionId, this.$field);
  }
};

scout.ClipboardField.prototype._renderDisplayText = function(displayText) {
  this.$field.text(displayText);
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
    additionalOptionsCompatibilityIndex = 0;   // counter for additional options

  // some browsers (e.g. IE) specify text content simply as data of type 'Text', it is not listed in list of types
  var textContent;
  textContent = dataTransfer.getData('Text');
  if (textContent) {
    if (window.Blob) {
      filesArgument.push(new Blob([textContent], {type: scout.MimeTypes.TEXT_PLAIN}));
    } else {
      // compatibility workaround
      additionalOptions['textTransferObject' + additionalOptionsCompatibilityIndex++] = textContent;
    }
  }

  if (dataTransfer.items) {
    $.each(dataTransfer.items, function(idx, item) {
      if (item.type === scout.MimeTypes.TEXT_PLAIN) {
        item.getAsString(function(str) {
          filesArgument.push(new Blob([str], {type: scout.MimeTypes.TEXT_PLAIN}));
        });
      }
      else if (scout.helpers.isOneOf(item.type, [scout.MimeTypes.IMAGE_PNG, scout.MimeTypes.IMAGE_JPG, scout.MimeTypes.IMAGE_JPEG, scout.MimeTypes.IMAGE_GIF])) {
        filesArgument.push(item.getAsFile());
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
      reader.readAsArrayBuffer(item);
    });
  }

  var uploadFunctionTimeoutCount = 0;
  var uploadFunction = function() {
    if (waitForFileReaderEvents !== 0 && uploadFunctionTimeoutCount++ !== 99) {
      setTimeout(uploadFunction, 150);
      return;
    }

    if (uploadFunctionTimeoutCount >= 99) {
      var boxOptions = {
        header: this.session._texts.get('ui.ClipboardTimeoutTitle'),
        body: this.session._texts.get('ui.ClipboardTimeout'),
        yesButtonText: this.session.optText('Ok', 'Ok')
      };

      this.session.showFatalMessage(boxOptions);
      return;
    }

    // upload paste event as files
    if (filesArgument.length > 0 || Object.keys(additionalOptions).length > 0) {
      this.session.uploadFiles(this, filesArgument, additionalOptions, this.maximumSize, this.allowedMimeTypes);
    }
  }.bind(this);

  uploadFunction.call(this);

  // do not trigger any other actions
  return false;
};
