#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
${rootArtifactId}.Desktop = function() {
  ${rootArtifactId}.Desktop.parent.call(this);
};
scout.inherits(${rootArtifactId}.Desktop, scout.Desktop);

${rootArtifactId}.Desktop.prototype._jsonModel = function() {
  return scout.models.getModel('${rootArtifactId}.Desktop');
};

${rootArtifactId}.Desktop.prototype._init = function(model) {
  ${rootArtifactId}.Desktop.parent.prototype._init.call(this, model);

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

${rootArtifactId}.Desktop.prototype._onDefaultThemeMenuAction = function(event) {
  this.setTheme('default');
};

${rootArtifactId}.Desktop.prototype._onDarkThemeMenuAction = function(event) {
  this.setTheme('dark');
};

${rootArtifactId}.Desktop.prototype._onAboutMenuAction = function(event) {
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
