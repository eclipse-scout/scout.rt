// noinspection JSUnresolvedVariable

class Desktop extends scout.Desktop {

  constructor() {
    super();
  }

  _jsonModel() {
    return {
      objectType: 'Desktop',
      navigationHandleVisible: false,
      navigationVisible: false,
      headerVisible: false,
      views: [
        {
          objectType: 'Form',
          displayHint: 'view',
          modal: false,
          rootGroupBox: {
            objectType: 'GroupBox',
            borderDecoration: scout.GroupBox.BorderDecoration.EMPTY,
            gridColumnCount: 1,
            fields: [
              {
                objectType: 'ModeSelectorField',
                labelVisible: false,
                statusVisible: false,
                fieldStyle: 'default',
                modeSelector: {
                  id: 'ConfigSelector',
                  objectType: 'ModeSelector',
                  selectedMode: 'Pie',
                  modes: [{
                    id: 'Pie',
                    objectType: 'Mode',
                    text: 'Pie Chart'
                  }, {
                    id: 'Bar',
                    objectType: 'Mode',
                    text: 'Bar Chart'
                  }, {
                    id: 'CheckableBar',
                    objectType: 'Mode',
                    text: 'Checkable Bar Chart'
                  }]
                }
              },
              {
                id: 'ChartField',
                objectType: 'ChartField',
                gridDataHints: {
                  h: 3
                },
                labelVisible: false,
                statusVisible: false,
                chart: {
                  id: 'Chart',
                  objectType: 'Chart',
                  data: {
                    axes: [
                      [{label: 'Jan.'}, {label: 'Feb.'}, {label: 'Mar.'}, {label: 'Apr.'}, {label: 'May'}, {label: 'Jun.'}, {label: 'Jul.'}, {label: 'Aug.'}, {label: 'Sept.'}, {label: 'Oct.'}, {label: 'Nov.'}, {label: 'Dec.'}]
                    ],
                    chartValueGroups: [
                      {
                        groupName: 'Vanilla',
                        values: [0, 0, 0, 94, 162, 465, 759, 537, 312, 106, 0, 0]
                      },
                      {
                        groupName: 'Chocolate',
                        values: [0, 0, 0, 81, 132, 243, 498, 615, 445, 217, 0, 0]
                      },
                      {
                        groupName: 'Strawberry',
                        values: [0, 0, 0, 59, 182, 391, 415, 261, 75, 31, 0, 0]
                      }
                    ]
                  }

                }
              }
            ]
          }
        }
      ]
    };
  }

  _init(model) {
    super._init(model);
    this._updateConfig();
    this.widget('ConfigSelector').on('propertyChange:selectedMode', () => this._updateConfig());
  }

  _updateConfig() {
    let config = {};
    let lineConfig = {
      type: scout.Chart.Type.BAR,
      options: {
        colorScheme: scout.colorSchemes.ColorSchemeId.RAINBOW
      }
    };
    let selectedMode = this.widget('ConfigSelector').selectedMode.id;
    switch (selectedMode) {
      case 'Bar':
        config = lineConfig;
        break;
      case 'CheckableBar':
        config = lineConfig;
        config.options = $.extend(true, {}, config.options, {
          clickable: true,
          checkable: true,
          legend: {
            clickable: true
          }
        });
        break;

    }
    this.widget('Chart').setConfig(config);
  }
}

scout.addObjectFactories({
  'Desktop': () => new Desktop()
});

new scout.App().init({
  bootstrap: {
    textsUrl: ['../../../eclipse-scout-core/dist/texts.json', '../../dist/texts.json'],
    localesUrl: '../../../eclipse-scout-core/dist/locales.json'
  }
});

