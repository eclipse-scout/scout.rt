scout.fields = function() {

};

scout.fields.new$TextField = function() {
  return $('<input>').
    attr('type', 'text').
    disableSpellcheck();
};
