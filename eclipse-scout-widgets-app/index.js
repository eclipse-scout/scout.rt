import { WidgetsApp } from './src/widgets/index';
import * as $ from 'jquery';

$(document).ready(() => {
  new WidgetsApp().init({
    bootstrap: {
      modelsUrl: 'models/widgetsapp.json' // FIXME [awe] toolstack: das gibt es nicht mehr, oder?
    }
  });
});
