scout.BreadCrumbsNavigation = function(model, session) {
  scout.BreadCrumbsNavigation.parent.call(this, model, session);
};
scout.inherits(scout.BreadCrumbsNavigation, scout.ModelAdapter);

scout.BreadCrumbsNavigation.prototype._render = function($parent) {
  this.$container = $parent.appendDiv(undefined, 'breadcrumbs');
  this.$scrollContainer= this.$container.appendDiv(undefined, 'scrollable-x');

  this._renderBreadCrumbs();
};

scout.BreadCrumbsNavigation.prototype._renderBreadCrumbs = function(removal) {
  var i, breadcrumb, form;
  for (i = 0; i < this.model.breadcrumbs.length; i++) {
    breadcrumb = this.model.breadcrumbs[i];
    form = this.session.widgetMap[breadcrumb.formId];
    this._addBreadCrumbForm(form);
  }
  form = this.session.widgetMap[this.model.currentFormId];
  this._addBreadCrumbForm(form);

  var scrollWidth = this.$scrollContainer[0].scrollWidth;
  var width = this.$scrollContainer.width();

  if (scrollWidth > width) {
    this.$scrollContainer.animate({
      scrollLeft: scrollWidth - width
    });
  }
};

scout.BreadCrumbsNavigation.prototype._addBreadCrumbForm = function(form) {
  var homeCrumb = false;
  if (this.model.breadcrumbs && this.model.breadcrumbs.length > 0) {
    homeCrumb = this.model.breadcrumbs[0].formId === form.model.id;
  } else {
    homeCrumb = this.model.currentFormId === form.model.id;
  }

  var $breadcrumb;
  var cssClasses = 'breadcrumb ';
  if (homeCrumb) {
    cssClasses += 'breadcrumb-home';
    $breadcrumb = this.$scrollContainer.beforeDiv(undefined, cssClasses, form.model.title);
    //necessary since scrollable is an inline element, see css
    this.$scrollContainer.width('calc(100% - '+$breadcrumb.outerWidth()+'px)');
  }
  else {
    $breadcrumb = this.$scrollContainer.appendDiv(undefined, cssClasses, form.model.title);
  }

  $breadcrumb
    .data('formId', form.model.id)
    .on('click', '', onClick);

  var that = this;

  function onClick() {
    var formId = $(this).data('formId');
    that.session.send('activate', that.model.id, {
      "formId": formId
    });
  }
};

scout.BreadCrumbsNavigation.prototype.onModelAction = function(event) {
  if (event.type_ == 'changed') {
    this.model.breadcrumbs = event.breadcrumbs;
    this.model.currentFormId = event.currentFormId;
    this.$container.find('.breadcrumb-home').remove();
    this.$scrollContainer.empty();
    this._renderBreadCrumbs();
  } else {
    $.log("Model event not handled. Widget: Form. Event: " + event.type_ + ".");
  }
};
