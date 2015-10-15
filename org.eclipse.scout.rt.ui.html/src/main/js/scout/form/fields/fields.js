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

  initMobile: function(field, model) {
    field.embedded = scout.helpers.nvl(model.embedded, false);
    // when 'mobile' is not set explicitly, check the device
    field.mobile = scout.helpers.nvl(model.mobile, scout.device.type !== scout.Device.Type.DESKTOP);
  },

  valOrText: function(field, $field, text) {
    if (field.mobile) {
      $field.text(text);
    } else {
      $field.val(text);
    }
  }

};
