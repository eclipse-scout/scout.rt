scout.ProposalChooser = function() {
  scout.ProposalChooser.parent.call(this);
  this._addAdapterProperties(['model']);
  this.$container;
  this.htmlComp;
};
scout.inherits(scout.ProposalChooser, scout.ModelAdapter);

scout.ProposalChooser.prototype.render = function($parent) {
  this.$container = $parent.appendDiv('proposal-chooser');
  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(new scout.ProposalChooserLayout(this));
  this.model.render(this.$container);

  // support for activeFilter
  this.activeFilter = 'TRUE'; // FIXME AWE: remove hardcoded
  if (this.activeFilter) {
    var $activeFilter = $.makeDiv('active-filter')
      .appendTo(this.$container);
    this._appendOption($activeFilter, 'UNDEFINED', 'Alle');
    this._appendOption($activeFilter, 'TRUE', 'Aktive');
    this._appendOption($activeFilter, 'FALSE', 'Inaktive');
  }
};

scout.ProposalChooser.prototype._appendOption = function($parent, value, text) {
  var $radio = $('<input>')
    .attr('type', 'radio')
    .attr('name', 'activeState')
    .attr('value', value)
    .change(this._onActiveFilterChanged.bind(this));
  if (this.activeFilter === value) {
    $radio.attr('checked', 'checked');
  }
  $parent
    .append($radio)
    .append($('<label>').text(text));
};

scout.ProposalChooser.prototype._onActiveFilterChanged = function(event) {
  var value = $(event.target).val();
  this.session.send(this.id, 'activeFilterChanged', {state: value});
};
