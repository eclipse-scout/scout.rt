scout.BreadCrumbNavigation = function() {
  scout.BreadCrumbNavigation.parent.call(this);
};
scout.inherits(scout.BreadCrumbNavigation, scout.ModelAdapter);

scout.BreadCrumbNavigation.prototype._render = function($parent) {
  this.$container = $parent.appendDiv(undefined, 'breadcrumbs');
  this.$scrollContainer= this.$container.appendDiv(undefined, 'scrollable-x');

  this._renderBreadCrumbs();
};

scout.BreadCrumbNavigation.prototype._renderBreadCrumbs = function(removal) {
  var i, breadcrumb, form;
  for (i = 0; i < this.breadcrumbs.length; i++) {
    breadcrumb = this.breadcrumbs[i];
    form = this.session.getModelAdapter(breadcrumb.formId);
    this._addBreadCrumbForm(form);
  }
  form = this.session.getModelAdapter(this.currentFormId);
  this._addBreadCrumbForm(form);

  var scrollWidth = this.$scrollContainer[0].scrollWidth;
  var width = this.$scrollContainer.width();

  if (scrollWidth > width) {
    this.$scrollContainer.animate({
      scrollLeft: scrollWidth - width
    });
  }
};

scout.BreadCrumbNavigation.prototype._addBreadCrumbForm = function(form) {
  var homeCrumb = false;
  if (this.breadcrumbs && this.breadcrumbs.length > 0) {
    homeCrumb = this.breadcrumbs[0].formId === form.id;
  } else {
    homeCrumb = this.currentFormId === form.id;
  }

  var $breadcrumb;
  var cssClasses = 'breadcrumb ';
  if (homeCrumb) {
    cssClasses += 'breadcrumb-home';
    $breadcrumb = this.$scrollContainer.beforeDiv(undefined, cssClasses, form.title);
    //necessary since scrollable is an inline element, see css
    this.$scrollContainer.width('calc(100% - '+$breadcrumb.outerWidth()+'px)');
  }
  else {
    $breadcrumb = this.$scrollContainer.appendDiv(undefined, cssClasses, form.title);
  }

  $breadcrumb
    .data('formId', form.id)
    .on('click', '', onClick);

  var that = this;

  function onClick() {
    var formId = $(this).data('formId');
    that.session.send('activate', that.id, {
      'formId': formId
    });
  }
};

scout.BreadCrumbNavigation.prototype.onModelAction = function(event) {
  if (event.type_ == 'changed') {
    this.breadcrumbs = event.breadcrumbs;
    this.currentFormId = event.currentFormId;
    this.$container.find('.breadcrumb-home').remove();
    this.$scrollContainer.empty();
    this._renderBreadCrumbs();
  } else {
    $.log('Model event not handled. Widget: Form. Event: ' + event.type_ + '.');
  }
};
