scout.RichTextField = function() {
  scout.RichTextField.parent.call(this);
};
scout.inherits(scout.RichTextField, scout.ValueField);

scout.RichTextField.prototype._createKeyStrokeAdapter = function() {
  return new scout.RichTextFieldKeyStrokeAdapter(this);
};

scout.RichTextField.prototype._renderProperties = function() {
  scout.RichTextField.parent.prototype._renderProperties.call(this);
};

scout.RichTextField.prototype._render = function($parent) {
  this.addContainer($parent, 'rich-text-field');
  this.addLabel();
  this.addMandatoryIndicator();

  this.addFieldContainer($.makeDiv('rich-text-editor'));
  var $editorContent = this.$fieldContainer.appendDiv('rich-text-editor-content');

  scout.scrollbars.install($editorContent, {
    updateScrollbarPos: false
  });
  this.session.detachHelper.pushScrollable($editorContent);

  // create editable div
  $editorContent.attr('contentEditable', 'true')
    .on('keydown keyup paste', this._onChange.bind(this))
    .on('focus', this._onFocus.bind(this))
    .on('blur', this._onBlur.bind(this))
    .attr('tabindex', '0');
  this.addField($editorContent);

  // demo data
  this.$field.appendDiv("", "Beispielstext");

  // command bar
  this.$commandBar = this.$fieldContainer.appendDiv('rich-text-bar');

  this.$commandFormat = this.$commandBar.appendDiv('rich-text-bar-group');
  this.$commandFormat.appendDiv('rich-text-command rich-text-bar-bold', 'a')
    .data('command', 'bold');
  this.$commandFormat.appendDiv('rich-text-command rich-text-bar-strike', 'a')
    .data('command', 'strikeThrough');
  this.$commandFormat.appendDiv('rich-text-command rich-text-bar-underline', 'a')
    .data('command', 'underline');

  this.$commandMark = this.$commandBar.appendDiv('rich-text-bar-group');
  this.$commandMark.appendDiv('rich-text-command rich-text-bar-white', '&nbsp')
    .data('command', 'BackColor');
  this.$commandMark.appendDiv('rich-text-command rich-text-bar-yellow', '&nbsp')
    .data('command', 'BackColor');
  this.$commandMark.appendDiv('rich-text-command rich-text-bar-green', '&nbsp')
    .data('command', 'BackColor');

  this.$commandList = this.$commandBar.appendDiv('rich-text-bar-group');
  this.$commandList.appendDiv('rich-text-command rich-text-bar-plain')
    .data('command', 'outdent');
  this.$commandList.appendDiv('rich-text-command rich-text-bar-bullet')
    .data('command', 'insertunorderedlist');
  this.$commandList.appendDiv('rich-text-command rich-text-bar-number')
    .data('command', 'insertorderedlist');

  $('.rich-text-command', this.$commandBar)
    .click((this._onCommandClick.bind(this)))
    .mousedown(function(event) {
      event.preventDefault();
    });

  this.addStatus();
};

scout.RichTextField.prototype._remove = function() {
  this.session.detachHelper.pushScrollable(this.$field);
};

scout.RichTextField.prototype._onFocus = function(event) {
  this.$field.addClass('focused');
};

scout.RichTextField.prototype._onBlur = function(event) {
  this.$field.removeClass('focused');
};

scout.RichTextField.prototype._onCommandClick = function(event) {
  var command = $(event.target).data('command'),
    attribute = '';

  if (command === 'BackColor') {
    attribute = $(event.target).css('background-color');
  }

  document.execCommand(command, false, attribute);

  if (command === 'BackColor') {
    var selelction = window.getSelection(),
      range = selelction.getRangeAt(0);

    range.collapse(false);
    selelction.removeAllRanges();
    selelction.addRange(range);

  }

  //this._onChange(event);
};

scout.RichTextField.prototype._onChange = function(event) {
  // update scrollbar
  scout.scrollbars.update(this.$field);
  // maybe necessary scroll to selection
  //  scout.scrollbars.scrollTo(this.$field, $divAtCursor);

  return;

  // store selection
  var selection = window.getSelection(),
    range, markerStart, markerEnd;

  if (selection.getRangeAt && selection.rangeCount) {
    range = selection.getRangeAt(0);

    markerStart = document.createElement('span');
    range.insertNode(markerStart);

    range.collapse(false);
    markerEnd = document.createElement('span');
    range.insertNode(markerEnd);
  }

  //cleanHTML(this.$field[0]);

  //scout.scrollbars.scrollTo(this.$field, $(markerStart));

  // restore selection
  selection.removeAllRanges();

  range = document.createRange();
  range.setStartBefore(markerStart);
  range.setEndBefore(markerEnd);
  selection.addRange(range);

  markerStart.parentElement.removeChild(markerStart);
  markerEnd.parentElement.removeChild(markerEnd);

  // backward selection?

  // recursive function

  function cleanHTML(element) {
    var content = element.childNodes,
      i, c, t;

    for (i = 0; i < content.length; i++) {
      c = content[i];
      $.l(c);

      // remove all styles but background-color
      /*if (c.removeAttribute) {
        c.removeAttribute('style');
      }*/

      // remove empty text nodes
      if (c.nodeType === 3 && c.length === 0) {
        c.parentElement.removeChild(c);
      }

      /*if (c.nodeType === 1 && c.nodeName === 'SPAN' && c !== markerStart && c !== markerEnd) {
        t = document.createTextNode(c.innerText);
        c.parentNode.replaceChild(t, c);
      }*/

      // recursive call
      if (c.childNodes.length > 0) {
        cleanHTML(c);
      }

    }

  }

};
