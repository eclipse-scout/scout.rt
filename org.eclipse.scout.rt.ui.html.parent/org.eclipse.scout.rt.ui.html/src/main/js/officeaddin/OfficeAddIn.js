scout.OfficeAddIn = function() {
  scout.OfficeAddIn.parent.call(this);
};
scout.inherits(scout.OfficeAddIn, scout.ModelAdapter);

scout.OfficeAddIn.prototype._render = function($parent) {
  this.$parent = $parent;
  this.$mscom = $parent.appendDIV('officeaddin');
  this.$mscom.attr('id','div_mscom');
};


/* event handling */

scout.OfficeAddIn.prototype.onModelAction = function(event) {
  var form;

  if (event.type === 'invoke') {
    alert('EVENT '+event.op+' '+event.ref);//op, ref, args
    if(event.ref){
      //return ref, status=success|error+message|timeout
      this.session.send('invokeResult', this.id,{'ref':event.ref, 'status':'success'});
    }
  } else {
    scout.parent.prototype.onModelAction.call(this, event);
  }
};

/* registration in object factory */

scout.defaultObjectFactories = scout.defaultObjectFactories.concat( [ {
  objectType : 'OfficeAddIn',
  create : function() {
    return new scout.OfficeAddIn();
  }
}]);
