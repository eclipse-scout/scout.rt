import { Scout, OutlineViewButton } from 'eclipse-scout';
import * as $ from 'jquery';
import MyOutlineViewButton from './outline/MyOutlineViewButton';


// FIXME [awe] toolstack: better use an API function from Scout, instead of plain jQuery/Object
Scout.objectFactories = $.extend(Scout.objectFactories, {
  OutlineViewButton: function() {
    return new MyOutlineViewButton();
  }
});
