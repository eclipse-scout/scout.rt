scout.fields = {

  new$TextField: function() {
    return $('<input>').
      attr('type', 'text').
      disableSpellcheck();
  },

  imageUrl: function(adapter, url) {
    return url + '?sessionId=' + adapter.session.jsonSessionId;
  }

};
