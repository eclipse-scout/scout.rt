// SCOUT GUI 0.2
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

// my log function!
var log = console.log.bind(console);

//
// extend jQuery, with all helpers 
//

(function ($) {
	// server communication
	$.syncAjax = function (url, data) {
		var ret;
		$.ajax({
			async : false,
			dataType : "json",
			cache : false,
			url : url,
			data : data,
			success : function (message) {ret = message; }
		});
		return ret;
	};
	
	// scout uses only divs...
    $.makeDiv = function (i, c, h) {
		i = i ? ' id="' + i + '"' : '';
		c = c ? ' class="' + c + '"' : '';
		h = h || '';
		return $('<div ' + i + c + '>' + h + '</div>');
    };

	// prepend - and return new div for chaining
    $.fn.prependDiv = function (i, c, h) {
		return $.makeDiv(i, c, h).prependTo(this);
    };
	
	// append - and return new div for chaining
    $.fn.appendDiv = function (i, c, h) {
		return $.makeDiv(i, c, h).appendTo(this);
    };

	// insert after - and return new div for chaining
    $.fn.afterDiv = function (i, c, h) {
		return $.makeDiv(i, c, h).insertAfter(this);
    };
	
	// insert before - and return new div for chaining
    $.fn.beforeDiv = function (i, c, h) {
		return $.makeDiv(i, c, h).insertBefore(this);
    };

	// select one and deselect siblings
	$.fn.selectOne = function () {
		this.siblings().removeClass('selected');
		this.addClass('selected');
		return this;
	};

	// most used animate
	$.fn.animateAVCSD = function (attr, value, complete, step, duration) {
		var properties = {},
			options = {};
		
		properties[attr] = value;
		if (complete) options.complete = complete; 
		if (step) options.step = step; 
		if (duration) options.duration = duration;
		
		this.animate(properties, options);
		return this;
	};
	
	// used by some animate functions
	$.removeThis = function () { $(this).remove(); };	
	
}(jQuery));

//
// scout
//

Scout = function ($entryPoint) {
	// initiate scout 
	var allWidgets = [];

	// create all widgets for entry point
	var model = $.syncAjax('model', {id : $entryPoint.attr('id')});
	for (var i = 0; i < model.length; i++) {
		var w = createWidget($entryPoint, model[i]);	
		allWidgets.push(w);
	}
			
	// create single widget based on a model object
	function createWidget ($parent, widget) {
		if (widget.type == "desktop") {
			return new Scout.Desktop($parent, widget);
		}
	}
};

//
// desktop: contains views, main_tree, (empty) bench, tools
//

Scout.Desktop = function ($parent, widget) {
	// create all 4 containers
	var view = new Scout.Desktop.View($parent, widget.views);
	var tool = new Scout.Desktop.Tool($parent, widget.tools);
	var tree = new Scout.Desktop.Tree($parent);
	var bench = new Scout.Desktop.Bench($parent);

	// show node
	var nodes = $.syncAjax('drilldown', {id : widget.start});
	tree.addNodes(nodes);
};

//
// view namespace and container
//

Scout.Desktop.View = function ($desktop, views) {
	//  create container
	var $desktopView = $desktop.appendDiv('DesktopViews');

	//  add view-item, all before #viewAdd
	for (var i = 0; i < views.length; i++) {
		var state = views[i].state || '';
		
		var $view = $desktopView.appendDiv(views[i].id, 'view-item ' + state, views[i].label)
						.on('click', '', clickView);
	}

	//  create logo and plus sign	
	$desktopView.appendDiv('ViewAdd').on('click', '', addOwnView);
	$desktopView.appendDiv('ViewLogo').delay(1000).animateAVCSD('width', 55, null, null, 1000);

	// set this for later usage
	this.$div = $desktopView;
	
	// named event funktions
	function clickView (event) {
		$(this).selectOne();
	}

	function addOwnView (event) {
		var name = $desktopView.children('.selected').text().split('('),
			c = name.length > 1 ? parseInt(name[1], 0) + 1 : 2;
		
		var state = 'view-own',
			label = name[0] + ' (' + c + ')';
			
		var $view = $('#ViewAdd').beforeDiv('', 'view-item ' + state, label)
						.on('click', '', clickView)
						.selectOne();
		
		$view.appendDiv('', 'view-remove').on('click', '', removeOwnView);
		
		var w = $view.width();
		$view.css('width', 0).animateAVCSD('width', w);	
	}	
	
	function removeOwnView (event) {
		$(this).parent()
			.animateAVCSD('width', 0, $.removeThis)
			.prev().click();
		
		return false;
	}
};

//
// tool namespace and container
//

Scout.Desktop.Tool = function ($desktop, tools) {
	// create container
	var $desktopTools = $desktop.appendDiv('DesktopTools');	

	// create tool-items
	for (var i = 0; i < tools.length; i++) {
		var state = tools[i].state || '',
			icon = tools[i].icon || '',
			shortcut = tools[i].shortcut || '';

		var $tool = $desktopTools
			.appendDiv(tools[i].id, 'tool-item ' + state, tools[i].label)
			.attr('data-icon', icon).attr('data-shortcut', shortcut);

		if (!$tool.hasClass('disabled')) {
			$tool.on('click', '', clickTool);
		}		
	}

	// create container for dialogs
	$desktopTools.appendDiv('DesktopDialogs');
	
	// set this for later usage
	this.$div = $desktopTools;	
	
	// named event funktions
	function clickTool (event) {
		var $clicked = $(this);
	
		$('.tool-open').animateAVCSD('width', 0, $.removeThis, null, 500);

		if ($clicked.hasClass("selected")) {
			$clicked.removeClass("selected");
		} else {
			$clicked.selectOne();
			$('#DesktopTools').beforeDiv('', 'tool-open')
				.animateAVCSD('width', 300, null, null, 500);
		}
	}
};

// 
// tree
//

Scout.Desktop.Tree = function ($desktop) {
	// create container
	var $desktopTree = $desktop.appendDiv('DesktopTree');
	var $desktopTreeScroll = $desktopTree.appendDiv('DesktopTreeScroll');
	var scrollbar = new Scout.Scrollbar($desktopTreeScroll, 'y', true);
	$desktopTree.appendDiv('DesktopTreeResize')
		.on('mousedown', '', resizeTree);
	
	// set this for later usage
	this.$div = $desktopTree;
	this.addNodes = addNodes;
	
	// named  funktions
	function resizeTree (event) {
		$('body').addClass('col-resize')
			.on('mousemove', '', resizeMove)
			.one('mouseup', '', resizeEnd);	
		
		function resizeMove(event){
			var w = event.pageX + 11;
			$desktopTree.width(w);
			$desktopTree.next().width('calc(100% - ' + (w + 80) + 'px)')
				.css('left', w);
		}
		
		function resizeEnd(event){
			$('body').off('mousemove')
				.removeClass('col-resize');
		}
		
		return false;
	}
	
	function addNodes (nodes, $parent) {
		var $allNodes = $('');
	
		for (var i =  nodes.length - 1; i >= 0; i--) {
			// create node
			var node = nodes[i],
				state = node.state || '',
				level = $parent ? $parent.data('level') + 1 : 0;
			
			var $node = $.makeDiv(node.id, 'tree-item ' + state, node.label)
							.on('click', '', clickNode)
							.data('bench', node.bench)
							.attr('data-level', level)
							.css('margin-left', level * 20)
							.css('width', 'calc(100% - ' + (level * 20 + 30) + 'px)');
			
			// decorate with (close) control
			var $control = $node.appendDiv('', 'tree-item-control')
				.on('click', '', clickNodeControl);

			// rotate control if expanded
			if ($node.hasClass('expanded')) {
				$control.css('transform', 'rotate(90deg)');
			}

			// decorate with menu
			$node.appendDiv('', 'tree-item-menu')
				.on('click', '', clickNodeMenu);
			
			// append first node and successors
			if ($parent) {
				$node.insertAfter($parent);
			} else {
				$node.appendTo($desktopTreeScroll);
			}
			
			// collect all nodes for later retur
			$allNodes = $allNodes.add($node);
			
			// if model demands children, create them
			if (node.children) {
				var $n = addNodes(node.children, $node);
				$allNodes = $allNodes.add($n);
			}
		}

		// return all created nodes
		return $allNodes;
	}

	function clickNode (event) {
		var $clicked = $(this),
			bench = $clicked.data('bench');
		
		// selected the one
		$clicked.selectOne();
		
		// show bench
		if (bench.type == 'table') {
			new Scout.Desktop.Table($('#DesktopBench'));
		} else{
			$('#DesktopBench').text(JSON.stringify(bench));
		}	
		
		// open node
		if ($clicked.hasClass('can-expand') && !$clicked.hasClass('expanded')) {
			// load model and draw nodes
			var nodes = $.syncAjax('drilldown', {id : $clicked.attr('id')});
			var $newNodes = addNodes(nodes, $clicked);
			
			if ($newNodes.length) {
				// animated opening ;)
				$newNodes.wrapAll('<div id="TreeItemAnimate"></div>)');
				var h = $newNodes.height() * $newNodes.length,
					removeContainer = function () {$(this).replaceWith($(this).contents());};
					
				$('#TreeItemAnimate').css('height', 0) 
					.animateAVCSD('height', h, removeContainer, scrollbar.initThumb);

				// animated control, at the end: parent is expanded
				var $control = $clicked.children('.tree-item-control'),
					rotateControl = function (now, fx) {
						$control.css('transform', 'rotate(' + now + 'deg)'); },
					addExpanded = function () {
						$clicked.addClass('expanded');};
					
				$control.css('borderSpacing', 0)
					.animateAVCSD('borderSpacing', 90, addExpanded, rotateControl);
			}
		}
	}	
	
	function clickNodeControl (event) {
		var $close = $(this).parent(),
			level = $close.attr('data-level');

		// only go further (and return false) if expanded
		if (!$close.hasClass('expanded')) return true;

		// click always select, even if closed
		$close.selectOne().removeClass('expanded');

		// animated closing ;)
		$close.nextUntil(function() {return $(this).attr("data-level") <= level;})
			.wrapAll('<div id="TreeItemAnimate"></div>)');
		$('#TreeItemAnimate').animateAVCSD('height', 0, $.removeThis, scrollbar.initThumb);
					
		// animated control
		var $control = $close.children('.tree-item-control'),
			rotateControl = function(now, fx){$control.css('transform', 'rotate(' + now + 'deg)');};
		$control.css('borderSpacing', 90) 
			.animateAVCSD('borderSpacing', 0, null, rotateControl);

		// prevent immediately reopening
		return false;
	}
	
	function clickNodeMenu (event) {
		var $clicked = $(this),
			id = $clicked.parent().attr('id'),
			x = $clicked.offset().left,
			y = $clicked.offset().top;
		
		new Scout.Menu(id, x, y);
	}
};

//
// bench
//

Scout.Desktop.Bench = function ($desktop) {
	//create container
	var $desktopBench = $desktop.appendDiv('DesktopBench');
	
	// set this for later usage
	this.$div = $desktopBench;	
};

//
// desktop table namespace and element
//

Scout.Desktop.Table = function ($bench) {
	//create container
	var $desktopTable = $bench.appendDiv('DesktopTable');
	$desktopTable.appendDiv('DesktopTableHeader');
	$desktopTable.appendDiv('DesktopTableData');
	$desktopTable.appendDiv('DesktopTableFooter');

	var $tableControl = $desktopTable.appendDiv('DesktopTableControl');
	$tableControl.appendDiv('ControlResizeTop');
	$tableControl.appendDiv('ControlResizeBottom');
	$tableControl.appendDiv('ControlGraph');
	$tableControl.appendDiv('ControlChart');
	$tableControl.appendDiv('ControlMap');
	$tableControl.appendDiv('ControlOrg');
	$tableControl.appendDiv('ControlLabel');
	
	var $tableControlInfo = $tableControl.appendDiv('ControlInfo');	
	$tableControlInfo.appendDiv('ControlInfoSelect');
	$tableControlInfo.appendDiv('ControlInfoFilter');
	$tableControlInfo.appendDiv('ControlInfoMore');
	$tableControlInfo.appendDiv('ControlInfoLoad');
	
	// set this for later usage
	this.$div = $desktopTable;	
};

//
// menu namespace and element
//

Scout.Menu = function (id, x, y) {
	// remove (without animate) old menu
	$('#MenuSelect, #MenuControl').remove();
	
	// load model		
	var menu = $.syncAjax('drilldown_menu', {id : id});
	
	// withou model, nothing to do
	if (menu.length === 0) return;
	
	// create 2 container, animate do not allow overflow
	var $menuSelect = $('body').appendDiv('MenuSelect')
					.css('left', x + 28).css('top', y - 3);
	var $menuControl = $('body').appendDiv('MenuControl')
					.css('left', x - 7).css('top', y - 3);
					
	// create menu-item and menu-button
	for (var i = 0; i < menu.length; i++) {
		if (menu[i].icon) {
			$menuSelect.appendDiv('', 'menu-button')
				.attr('data-icon', menu[i].icon)
				.attr('data-label', menu[i].label)
				.hover( function() {$('#MenuButtonsLabel').text($(this).data('label'));}, 
						function() {$('#MenuButtonsLabel').text('');});
		} else {
			$menuSelect.appendDiv('', 'menu-item', menu[i].label);
		}
	}
	
	// wrap menu-buttons and add one div for label
	$('.menu-button').wrapAll('<div id="MenuButtons"></div>');
	$('#MenuButtons').appendDiv('MenuButtonsLabel');

	// show menu on top
	var menuTop = $menuSelect.offset().top; 
		menuHeight = $menuSelect.height(),
		windowHeight = $(window).height(); 

	if (menuTop + menuHeight > windowHeight) {
		$menuSelect.css('top', menuTop - menuHeight + 27);
	}

	// animated opening
	var w = $menuSelect.css('width');		
	$menuSelect.css('width', 0).animateAVCSD('width', w);		
		
	// every user action will close menu
	$('*').one('mousedown keydown mousewheel', removeMenu);
	function removeMenu (event) {
		$menuSelect.animateAVCSD('width', 0, 
				function() {$menuControl.remove(); $menuSelect.remove(); });
		return true;
	}
};

//
// scrollbar namespace and element
//

Scout.Scrollbar = function ($container, axis, trackResize) {
	var dir = (axis === "x" ? "left" : "top"),
		dim = (axis === "x" ? "Width" : "Height"),
		begin = 0, beginDefault = 0,
		setThumb;

	// create scrollbar
	var $scrollbar = $container.beforeDiv('', 'scrollbar'),
		$thumb = $scrollbar.appendDiv('', 'scrollbar-thumb');

	//event handling
	$container.parent().on('DOMMouseScroll mousewheel', '', scrollWheel);
	$thumb.on('mousedown', '', scrollStart);
	$scrollbar.on('mousedown', scrollEnd);
	if (trackResize) $(window).on('load resize', initThumb);
		
	// set this for later usage
	this.initThumb = initThumb;

	// use this function (from outside) if size of tree content changes
	function initThumb () {
		var offset = $container[0]["offset" + dim],
			scroll = $container[0]["scroll" + dim],
			margin = parseInt($scrollbar.css('margin-top'), 0),
			topContainer = parseInt($container.css(dir), 0);

		// when needed: move container to right position
		if (offset - topContainer >= scroll){
			topContainer = Math.min(0, - scroll + offset);
			$container.stop().animateAVCSD(dir, topContainer); 
		}
		
		// calc size and range of thumb
		var thumbSize = Math.max(offset * offset / scroll - margin * 2, 30),
			thumbRange = offset - thumbSize - margin * 2;		
		
		// set size of thumb
		$thumb.css(dim.toLowerCase(), thumbSize);
		beginDefault = thumbSize / 2;

		// set location of thumb
		$thumb.css(dir, topContainer / (offset - scroll) * thumbRange);
		
		// show scrollbar
		if (offset >= scroll) {
			$scrollbar.css('visibility', 'hidden');
		} 
		else {
			$scrollbar.css('visibility', 'visible');
		}

		// prepare function (with colsure) for later usage
		setThumb = function (posDiff) {
			var posOld = $thumb.offset()[dir] - $scrollbar.offset()[dir],
				posNew = Math.min(thumbRange, Math.max(0, posOld + posDiff));
			
			$container.css(dir, (offset - scroll) / thumbRange * posNew );
			$thumb.css(dir, posNew);
		};	
	}
	
	function scrollWheel (event) {
		event = event.originalEvent || window.event.originalEvent;
		var w = event.wheelDelta ? - event.wheelDelta / 4 : event.detail * 30;
		setThumb(w);
		return false;
	}

	function scrollStart (event) {
		begin = (axis === "x" ? event.pageX : event.pageY) - $thumb.offset()[dir];
		$thumb.addClass('scrollbar-thumb-move');
		$(document).on('mousemove', scrollEnd)
			.one('mouseup', scrollExit);
		return false;
	}
	
	function scrollEnd (event) {
		begin = begin === 0 ? beginDefault : begin;
		var end = (axis === "x" ? event.pageX : event.pageY) - $thumb.offset()[dir];
		setThumb(end - begin);
	}

	function scrollExit() {
		$thumb.removeClass('scrollbar-thumb-move');
		$(document).off("mousemove");
		return false;
	}		
};

//
// start all scouts after loading 
//

$(document).ready(function () {
	$('.scout').each(function () {new Scout($(this));});
});

// old
