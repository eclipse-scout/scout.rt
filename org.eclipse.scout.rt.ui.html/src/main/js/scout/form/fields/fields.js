scout.fields = {

  new$TextField: function() {
    return $('<input>')
      .attr('type', 'text')
      .attr('autocomplete', 'false') /* use false instead of off, off is currently ignored in chrome, false should work with all major browsers*/
      .disableSpellcheck();
  },

  new$Icon: function() {
    return $('<span>')
      .addClass('icon');
  },

  initTouch: function(field, model) {
    field.embedded = scout.helpers.nvl(model.embedded, false);
    // when 'touch' is not set explicitly, check the device
    field.touch = scout.helpers.nvl(model.touch, scout.device.supportsTouch());
  },

  /**
   * Calls JQuery $.text() for touch-devices and $.val() for all other devices, used together with inputOrDiv.
   */
  valOrText: function(field, $field, text) {
    if (field.touch) {
      $field.text(text);
    } else {
      $field.val(text);
    }
  },

  /**
   * Creates a DIV element for touch-devices and an INPUT element for all other devices.
   */
  inputOrDiv: function(field) {
    if (field.touch) {
      return $.makeDiv('input-field');
    } else {
      return scout.fields.new$TextField();
    }
  },

  // note: the INPUT element does not process the click event when the field is disabled
  // however, the DIV element used in touch-mode does process the event anyway, that's
  // why this check is required.
  handleOnClick: function(field) {
    return field.enabled && !field.embedded && !field._popup.isOpen();
  }

};
