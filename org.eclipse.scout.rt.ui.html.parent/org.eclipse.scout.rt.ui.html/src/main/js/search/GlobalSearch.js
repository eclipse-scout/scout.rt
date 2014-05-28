scout.GlobalSearch = function ($parent, session) {
  this._render($parent);
};

scout.CheckBoxField.prototype._render = function($parent) {
  // input field
  this._$globalSearchInput = $('<input type="text" class="global-search-input" />');
  this._$globalSearchInput
    .appendTo($parent)
    .on('change keyup paste', onInput);

  // output field
  this._$globalSearchOutput = $.makeDiv('', 'global-search-output');
  this._$globalSearchOutput
    .appendTo($parent);

  //event handling
  function onInput (event) {
    this._$globalSearchOutput.empty();
    var proposal = this._simulator(this._$globalSearchInput.text());

    for (var i;  i < proposal.length; i++) {
      this._$globalSearchOutput.appendDiv('', 'global-search-output-item', proposal[i]);
    }
  }
};


scout.GlobalSearch.prototype._simulator = function (text) {
  var data = {};

  data.company = ['ABB', 'ROCHE', 'SBB', 'BSI', 'POST', 'IBM', 'NOVARTIS', 'LUFTHANSA', 'ETAVIS', 'SYNGENTA', 'SANITAS', 'SIKA'];
  data.business = ['CTMS ROCHE', 'BSI CRM SIKA', 'SANITAS CALL CENTER', 'KUDI POST', 'LUFTHANSA HR MANAGER', 'WARTUNG IBM 2014', 'NOVARTIS CTMS MAIN', 'LUFTHANSA OVERSEAS OUTSOURCING', 'ETAVIS NEW BUSINESS', 'SYNGENTA BSI CRM'];
  data.person = ['Hans Müller', 'Rainer Rilke', 'Jens Habermas', 'Niko Leicht', 'Karl Richter', 'Sandra Schaad', 'Sigrid Lichtermann', 'Monika Jost'];

  data.newCommand = ['anlegen', 'neu'];
  data.schowCommand = ['anzeigen', 'zeigen'];
  data.changeCommand = ['bearbeiten', 'öffnen', 'ändern'];

  return ['test 1 ' + text, 'test 2 ' + text];
};


