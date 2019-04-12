import { WidgetsApp } from './src/widgets/index';
import * as $ from 'jquery';

$(document).ready(() => {
  new WidgetsApp().init({
    bootstrap: {
      modelsUrl: 'models/widgetsapp.json'
    }
  });
});
