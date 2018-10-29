#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
${simpleArtifactName}.Desktop = function() {
  ${simpleArtifactName}.Desktop.parent.call(this);
};
scout.inherits(${simpleArtifactName}.Desktop, scout.Desktop);

${simpleArtifactName}.Desktop.prototype._jsonModel = function() {
  return scout.models.getModel('${simpleArtifactName}.Desktop');
};

${simpleArtifactName}.Desktop.prototype._init = function(model) {
  ${simpleArtifactName}.Desktop.parent.prototype._init.call(this, model);

  var aboutMenu = this.widget('AboutMenu');
  aboutMenu.on('action', this._onAboutMenuAction.bind(this));

  var defaultThemeMenu = this.widget('DefaultThemeMenu');
  defaultThemeMenu.on('action', this._onDefaultThemeMenuAction.bind(this));

  var darkThemeMenu = this.widget('DarkThemeMenu');
  darkThemeMenu.on('action', this._onDarkThemeMenuAction.bind(this));

  if (this.theme === 'dark') {
    darkThemeMenu.setIconId(scout.icons.CHECKED_BOLD);
  } else {
    defaultThemeMenu.setIconId(scout.icons.CHECKED_BOLD);
  }
};

${simpleArtifactName}.Desktop.prototype._onDefaultThemeMenuAction = function(event) {
  this.setTheme('default');
};

${simpleArtifactName}.Desktop.prototype._onDarkThemeMenuAction = function(event) {
  this.setTheme('dark');
};

${simpleArtifactName}.Desktop.prototype._onAboutMenuAction = function(event) {
  var form = scout.create('Form', {
    parent: this,
    resizable: false,
    title: this.session.text('ApplicationTitle'),
    rootGroupBox: {
      objectType: 'GroupBox',
      borderDecoration: 'empty',
      fields: [{
        objectType: 'LabelField',
        value: this.session.text('AboutText', this.session.text('ApplicationTitle'), scout.app.version),
        labelVisible: false,
        wrapText: true,
        htmlEnabled: true,
        cssClass: 'about-info',
        statusVisible: false,
        gridDataHints: {
          h: 3
        }
      }]
    }
  });
  form.open();
};
