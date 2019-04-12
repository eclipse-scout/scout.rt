import Widget from '../Widget/Widget';
import HtmlComponent from '../Layout/HtmlComponent';
import SimpleTabAreaLayout from './SimpleTabAreaLayout';

//require('./SimpleTabArea.less');

export default class SimpleTabArea extends Widget {

    constructor() {
        super();
        this.tabs = [];
    }

    _init(model) {
        super._init(model);
        this._selectedViewTab;
        this._tabClickHandler = this._onTabClick.bind(this);
    };

    _render() {
        this.$container = this.$parent.appendDiv('simple-tab-area');
        this.htmlComp = HtmlComponent.install(this.$container, this.session);
        this.htmlComp.setLayout(new SimpleTabAreaLayout(this));
    };

    _renderProperties() {
        super._renderProperties();
        this._renderTabs();
    };

    _renderTabs() {
        // reverse since tab.renderAfter() called without sibling=true argument (see _renderTab)
        // will _prepend_ themselves into the container.
        this.tabs.slice().reverse()
            .forEach(function(tab) {
                this._renderTab(tab);
            }.bind(this));
    };

    _renderTab(tab) {
        tab.renderAfter(this.$container);
    };

    _renderVisible() {
        if (this.visible && this.tabs.length > 0) {
            this.attach();
        } else {
            this.detach();
        }
        this.invalidateLayoutTree();
    };

    _attach() {
        this.$parent.prepend(this.$container);
        this.session.detachHelper.afterAttach(this.$container);
        // If the parent was resized while this view was detached, the view has a wrong size.
        this.invalidateLayoutTree(false);
        super._attach();
    };

    /**
     * @override Widget.js
     */
    _detach() {
        this.session.detachHelper.beforeDetach(this.$container);
        this.$container.detach();
        super._detach();
        this.invalidateLayoutTree(false);
    };

    _onTabClick(event) {
        this.selectTab(event.source);
    };

    getTabs() {
        return this.tabs;
    };

    selectTab(viewTab) {
        if (this._selectedViewTab === viewTab) {
            return;
        }
        this.deselectTab(this._selectedViewTab);
        this._selectedViewTab = viewTab;
        if (viewTab) {
            // Select the new view tab.
            viewTab.select();
        }
        this.trigger('tabSelect', {
            viewTab: viewTab
        });
        if (viewTab && viewTab.rendered && !viewTab.$container.isVisible()) {
            this.invalidateLayoutTree();
        }
    };

    deselectTab(viewTab) {
        if (!viewTab) {
            return;
        }
        if (this._selectedViewTab !== viewTab) {
            return;
        }
        this._selectedViewTab.deselect();
    };

    getSelectedTab() {
        return this._selectedViewTab;
    };

    addTab(tab, sibling) {
        var insertPosition = -1;
        if (sibling) {
            insertPosition = this.tabs.indexOf(sibling);
        }
        this.tabs.splice(insertPosition + 1, 0, tab);
        tab.on('click', this._tabClickHandler);
        if (this.rendered) {
            this._renderVisible();
            tab.renderAfter(this.$container, sibling);
            this.invalidateLayoutTree();
        }
    };

    destroyTab(tab) {
        var index = this.tabs.indexOf(tab);
        if (index > -1) {
            this.tabs.splice(index, 1);
            tab.destroy();
            tab.off('click', this._tabClickHandler);
            this._renderVisible();
            this.invalidateLayoutTree();
        }
    };

}
