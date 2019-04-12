import HtmlComponent from './HtmlComponent';
import AbstractLayout from './AbstractLayout';

export default class NullLayout extends AbstractLayout {

    constructor() {
        super();
    }

    layout($container) {
        $container.children().each(function() {
            var htmlComp = HtmlComponent.optGet($(this));
            if (htmlComp) {
                htmlComp.revalidateLayout();
            }
        });
    };
}
