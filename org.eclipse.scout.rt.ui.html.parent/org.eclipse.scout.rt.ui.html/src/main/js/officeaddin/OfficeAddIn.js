scout.OfficeAddIn = function() {
  scout.OfficeAddIn.parent.call(this);
};
scout.inherits(scout.OfficeAddIn, scout.ModelAdapter);

scout.OfficeAddIn.prototype._render = function($parent) {
  var that = this;
  this.$parent = $parent;
  this.$mscomSend = $('#_js2mscom');
  this.$mscomRecv = $('#_mscom2js');
  this.$mscomRecv.click(function() {
    that._mscomRecv();
  });
  this._notImplementedMessage = JSON.stringify({
    'op' : '0',
    'status' : 'error',
    'message' : 'ERR-9000 Office is not attached'
  });
};

scout.OfficeAddIn.prototype._mscomSend = function(serialJson) {
  this.$mscomSend.html(serialJson);
  this.$mscomRecv.html(this._notImplementedMessage);
  this.$mscomSend.trigger('click');
  var response = JSON.parse(this.$mscomRecv.html());
  this.$mscomSend.html(' ');
  this.$mscomRecv.html(' ');
  return response;
};

scout.OfficeAddIn.prototype._mscomRecv = function() {
  var data = JSON.parse(this.$mscomRecv.html());
  this.$mscomRecv.html(' ');
  this.session.send('inbound', this.id, data);
};

/* event handling */

scout.OfficeAddIn.prototype.onModelAction = function(event) {
  if (event.type === 'outbound') {
    if (event.status === 'request') {
      var data = this._mscomSend(JSON.stringify(event));
      data.ref = event.ref;
      // return ref, status=success|error+message|timeout
      this.session.send('inbound', this.id, data);
    }
    else {
      this.$mscomSend.html(JSON.stringify(event));
    }
  }
  else {
    scout.OfficeAddIn.parent.prototype.onModelAction.call(this, event);
  }
};

/* registration in object factory */

scout.defaultObjectFactories = scout.defaultObjectFactories.concat([ {
  objectType : 'OfficeAddIn',
  create : function() {
    return new scout.OfficeAddIn();
  }
} ]);
