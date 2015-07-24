scout.ClipboardField = function() {
  scout.ClipboardField.parent.call(this);
};
scout.inherits(scout.ClipboardField, scout.ValueField);

scout.ClipboardField.prototype._render = function($parent) {
  this.addContainer($parent, 'clipboard-field');
  this.addLabel();
  this.addField($('<div>'));
  this.addStatus();

  this.$field.attr('contenteditable', '');
  this.$field.css('background-color', 'lightyellow');

  this.$field.focus();

  $parent.on('click', function(event) { this.$field.focus(); }.bind(this));

  this.$field.on('copy', this._onPaste.bind(this));
  this.$field.on('paste', this._onPaste.bind(this));
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

  // options to be uploaded, arguments for this.session.uploadFiles
  var filesArgument = [];
  var additionalOptions = {};

  // counter for additional options
  var additionalOptionsCompatibilityIndex = 0;

  // some browsers (e.g. IE) specify text content simply as data of type 'Text', it is not listed in list of types
  var textContent;
  textContent = dataTransfer.getData('Text');
  if (textContent) {
    if (window.Blob) {
      filesArgument.push(new Blob([textContent], {type: 'text/plain'}));
    } else {
      // compatibility workaround
      additionalOptions['textTransferObject' + additionalOptionsCompatibilityIndex++] = textContent;
    }
  }

  if (dataTransfer.items) {
    $.each(dataTransfer.items, function(idx, item) {
      if (item.type === 'text/plain') {
        item.getAsString(function(str) {
          filesArgument.push(new Blob([str], {type: 'text/plain'}));
        });
      }
      else if (['image/png', 'image/jpg', 'image/jpeg','image/gif'].indexOf(item.type) != -1) {
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

    // upload paste event as files
    if (filesArgument.length > 0 || Object.keys(additionalOptions).length > 0) {
      this.session.uploadFiles(this, filesArgument, additionalOptions, this.maximumSize, this.allowedMimeTypes);
    }
  }.bind(this);

  uploadFunction.call(this);

  // do not trigger any other actions
  event.preventDefault();
  event.stopPropagation();
};
