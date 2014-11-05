scout.OfficeAddIn = function() {
  scout.OfficeAddIn.parent.call(this);
};
scout.inherits(scout.OfficeAddIn, scout.ModelAdapter);

//override
scout.OfficeAddIn.prototype.init = function(model, session) {
  scout.OfficeAddIn.parent.prototype.init.call(this, model,session);
  //call render directly since this model is not part of any other container such as desktop or form
  this.render(this.session.$entryPoint);
};

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
    var data={};
    data.ref=event.ref;
    data.status='success';
    if(event.op==20){
      data.docx=this.$mscom.html();
    }
    if(data.ref || data.docx){
      //return ref, status=success|error+message|timeout
      this.session.send('invokeResult', this.id,data);
    }
  } else {
    scout.OfficeAddIn.parent.prototype.onModelAction.call(this, event);
  }
};

/* registration in object factory */

scout.defaultObjectFactories = scout.defaultObjectFactories.concat( [ {
  objectType : 'OfficeAddIn',
  create : function() {
    return new scout.OfficeAddIn();
  }
}]);
