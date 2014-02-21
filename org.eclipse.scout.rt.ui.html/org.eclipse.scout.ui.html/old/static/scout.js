// SCOUT GUI 0.2
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

// my log and profiler functions!
var log = console.log.bind(console);

var profile = {
	time: null,
	start: function () { this.time = new Date().getTime(); },
	end: function () { log('exec time: ' + (new Date().getTime() - this.time)); }
};

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
	
	// append svg 
	$.fn.appendSVG = function (t, i, c, h) {
		var $svgElement = $(document.createElementNS("http://www.w3.org/2000/svg", t));
		if (i) $svgElement.attr('id', i);
		if (c) $svgElement.attr('class', c);
		if (h) $svgElement.html(h);
		return $svgElement.appendTo(this);
	};
	
	// attr and class handling for svg 
	$.fn.attrSVG = function (a, v) {
		return this.each(function () {
			this.setAttribute(a, v);
		});
	};

	$.fn.attrXLINK = function (a, v) {
		return this.each(function () {
			this.setAttributeNS('http://www.w3.org/1999/xlink', 'xlink:' + a, v);
		});
	};

	$.fn.addClassSVG = function (c) {
		return this.each(function () {
			if (!$(this).hasClassSVG(c)) {
				var old = this.getAttribute('class');
				this.setAttribute('class', old + ' ' + c);
			}
		});
	};

	$.fn.removeClassSVG = function (c) {
		return this.each(function () {
			var old = ' ' + this.getAttribute('class') + ' ';
			this.setAttribute('class',  old.replace(' ' + c + ' ', ' '));
		});
	};
	
	$.fn.hasClassSVG = function (c) {
		var old = ' ' + this.attr('class') + ' ';
		return old.indexOf(' ' + c + ' ') > -1;
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

	// SVG animate, array contains attr, endValue + startValue
	$.fn.animateSVG = function (attr, endValue, duration, complete) {			
		return this.each(function () {
			var startValue = parseFloat($(this).attr(attr));
				
			$(this).animate({tabIndex: 0},
				{step: function (now, fx) {this.setAttribute(attr, startValue + (endValue - startValue) * fx.pos); },
				duration: duration,
				complete: complete,
				queue: false});
		});
	};

	
	// over engineered animate
	$.fn.widthToContent = function () {
		var oldW = this.outerWidth(),
			newW = this.css('width', 'auto').outerWidth(),
			finalWidth = this.data('finalWidth');	
		
		if (newW != oldW) {
			this.css('width', oldW);
		}

		if (newW != finalWidth) {
			this.stop().animateAVCSD('width', newW, null, function () { $(this).data('finalWidth', null); });
			this.data('finalWidth', newW);
		}
		
		return this;
	};
	
	// used by some animate functions
	$.removeThis = function () { $(this).remove(); };	
	
	// converter functions constants
	// todo: holen aus kleinem Array
	// todo: verschiebene aller locale dinge nach Scout.Locale
	
	$.DEC = '.';
	$.GROUP = "'";
	$.DATE = ['dd', 'mm', 'yyyy'];
	$.DATE_SEP = '.';
	$.WEEKDAY = ['Mo', 'Di', 'Mi', 'Do', 'Fr', 'Sa', 'So'];
	$.WEEKDAY_LONG = ['Montag', 'Dienstag', 'Mittwoch', 'Donnerstag', 'Freitag', 'Samstag', 'Sonntag'];
	$.MONTH = ['Jan', 'Feb', 'Mär', 'Apr', 'Mai', 'Jun', 'Jul', 'Aug', 'Sep', 'Okt', 'Nov', 'Dez'];
	$.MONTH_LONG = ['Januar', 'Februar', 'März', 'April', 'Mai', 'Juni', 'Juli', 'August', 'September', 'Oktober', 'November', 'Dezember'];
	
	// converter functions
	$.numberToString = function (number, round) {
		var string = String(number.toFixed(round));

		// replace decimal point
		split = string.split('.').join($.DEC);

		// find start and end position of main number
		var start = string.indexOf($.DEC);
		var end = string.indexOf('-');
		
		start = (start == -1 ? string.length : start);
		end = (end == -1 ? 0 : 1);
		
		// group digits
		for (var i = start - 3; i > end; i -= 3) {
			string = string.substr(0, i) + $.GROUP + string.substr(i);
		}
		
		return string;
	};
	
	$.stringToDate = function (string) {
		var splitter = string.split($.DATE_SEP);

		var d = parseInt(splitter[$.DATE.indexOf('dd')], 10);
		var m = parseInt(splitter[$.DATE.indexOf('mm')], 10);
		var y = parseInt(splitter[$.DATE.indexOf('yyyy')], 10);
			
		return new Date((y < 100 ? y + 2000 : y), m - 1, d);
	};

	$.dateToString = function (date) {
		var d = date.getDate(),
			m = date.getMonth() + 1,
			y = date.getFullYear();		
		
		var string = $.DATE.join($.DATE_SEP);
		
		return string.replace('dd', (d <= 9 ? '0' + d : d)).replace('mm', (m <= 9 ? '0' + m : m)).replace('yyyy', y); 
	};
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

	// alt and f1-help
	$(window).keydown(function (event)  {
		if (event.which == 18) {
			removeKeyBox();
			drawKeyBox();
		}
	});

	$(window).keyup(function (event)  {
		if (event.which == 18) {
			removeKeyBox();
			return false;
		}
	});
	
	$(window).blur(function (event)  {
		removeKeyBox();
	});
	
	
	// key handling
	var fKeys = {};
	$('.tool-item', tool.$div).each(function (i, e) {
		var shortcut = parseInt($(e).attr('data-shortcut').replace('F', '')) + 111;
		fKeys[shortcut] = e;
	}); 
	
	$(window).keydown(function (event)  {
		// numbers: views
		if (event.which >= 49 && event.which <= 57){
			$('.view-item', view.$div).eq(event.which - 49).click();
		}
		
		// function keys: tools
		if (fKeys[event.which]){
			$(fKeys[event.which]).click();
			return false;
		}

		// left: up in tree
		if (event.which == 37){
			$('.selected', tree.$div).prev().click();
			removeKeyBox();
			return false;
		}		
		
		// right: down in tree
		if (event.which == 39){
			$('.selected', tree.$div).next().click();
			removeKeyBox();
			return false;
		}
		
		// +/-: open and close tree
		if (event.which == 109 || event.which == 107){
			$('.selected', tree.$div).children('.tree-item-control').click();
			removeKeyBox();
			return false;
		}
		
		//todo: make clicked row visible 
		if ([38, 40, 36, 35, 33, 34].indexOf(event.which) > -1){
			// up: up in table
			if (event.which == 38){
				var $row = $('.row-selected', bench.$div).first();
				if ($row.length > 0) { 
					$row.prev().trigger('mousedown').trigger('mouseup');
				} else {
					$('.table-row', bench.$div).last().trigger('mousedown').trigger('mouseup');
				}
			}
	
			// up: down in table
			if (event.which == 40){
				var $row = $('.row-selected', bench.$div).last();
				if ($row.length > 0) { 
					$row.next().trigger('mousedown').trigger('mouseup');
				} else {
					$('.table-row', bench.$div).first().trigger('mousedown').trigger('mouseup');
				}
			}
	
			// home: down in table
			if (event.which == 36){
				var $row = $('.table-row', bench.$div).first();
				$row.trigger('mousedown');
			}
	
			// end: down in table
			if (event.which == 35){
				var $row = $('.table-row', bench.$div).last();
				$row.trigger('mousedown');
			}
	
			// pgup: move up
			if (event.which == 33){
				var $row = $('.row-selected', bench.$div).first();
				if ($row.length > 0) { 
					$('.table-row', bench.$div).first().trigger('mousedown');
					$row.prevAll().eq(10).trigger('mousedown');
				} else {
					$('.table-row', bench.$div).last().trigger('mousedown');
				}
			}
	
			// pgdn: move down
			if (event.which == 34){
				var $row = $('.row-selected', bench.$div).last();
				if ($row.length > 0) {
					$('.table-row', bench.$div).last().trigger('mousedown');
					$row.nextAll().eq(10).trigger('mousedown');
				} else {
					$('.table-row', bench.$div).first().trigger('mousedown');
				}
			}
		}
		
	});
	
	function removeKeyBox () {
		$('.key-box').remove();
		$('.tree-item-control').show();
	}
	
	function drawKeyBox () {
		// keys for views
		$('.view-item', view.$div).each(function (i, e) {
				if (i < 9)  $(e).appendDiv('', 'key-box', i + 1);
			}); 
		
		// keys for tools
		$('.tool-item', tool.$div).each(function (i, e) {
			$(e).appendDiv('', 'key-box', $(e).attr('data-shortcut'));
		});
					
		// keys for tree
		var node = $('.selected', tree.$div),
			prev = node.prev(), 
			next = node.next();
		
		if (node.hasClass('can-expand')) {
			if (node.hasClass('expanded')) {
				node.appendDiv('', 'key-box large', '-');
			} else {
				node.appendDiv('', 'key-box large', '+');
			}
			node.children('.tree-item-control').hide();
		}
		
		if (prev) {
			prev.appendDiv('', 'key-box', '←');
			prev.children('.tree-item-control').hide();
		}
		
		if (next) {
			next.appendDiv('', 'key-box', '→');
			next.children('.tree-item-control').hide();
		}
		
		// keys for table
		
		var node = $('#TableData', bench.$div);
		if (node) {
			node.appendDiv('', 'key-box top3', 'Home');
			node.appendDiv('', 'key-box top2', 'PgUp');
			node.appendDiv('', 'key-box top1', '↑');
			node.appendDiv('', 'key-box bottom1', '↓');
			node.appendDiv('', 'key-box bottom2', 'PgDn');
			node.appendDiv('', 'key-box bottom3', 'End');
		} 
	}

	
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
			c = name.length > 1 ? parseInt(name[1], 10) + 1 : 2;
		
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
	var scrollbar = new Scout.Scrollbar($desktopTreeScroll, 'y');
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
							.css('width', 'calc(100% - ' + (level * 20 + 20) + 'px)');
			
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
				
		// open node
		if ($clicked.hasClass('can-expand') && !$clicked.hasClass('expanded')) {
			// load model and draw nodes
			var nodes = $.syncAjax('drilldown', {id : $clicked.attr('id')});
			var $newNodes = addNodes(nodes, $clicked);
			
			if ($newNodes.length) {
				// animated opening ;)
				$newNodes.wrapAll('<div id="TreeItemAnimate"></div>)');
				var h = $newNodes.height() * $newNodes.length,
					removeContainer = function () {$(this).replaceWith($(this).contents()); scrollbar.initThumb();};
					
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
		
		// show bench
		$('#DesktopBench').html('');
		if (bench.type == 'table') {
			new Scout.Desktop.Table($('#DesktopBench'), bench);
		} else{
			$('#DesktopBench').text(JSON.stringify(bench));
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

Scout.Desktop.Table = function ($bench, model) {
	var textOrg = 'Spaltenverwaltung',
		textClose = 'schliessen';

	//create container
	var $desktopTable = $bench.appendDiv('DesktopTable'),
		$tableHeader = $desktopTable.appendDiv('TableHeader'),
		$tableData = $desktopTable.appendDiv('TableData'),
		$tableFooter = $desktopTable.appendDiv('TableFooter'),
		$tableControl = $desktopTable.appendDiv('TableControl');
		
	var $tableDataScroll = $tableData.appendDiv('TableDataScroll');
		scrollbar = new Scout.Scrollbar($tableDataScroll, 'y');
	
	var $controlResizeTop = $tableControl.appendDiv('ControlResizeTop'),
		$controlResizeBottom = $tableControl.appendDiv('ControlResizeBottom');
	
	var	$controlChart = $tableControl.appendDiv('ControlChart'),
		$controlGraph = $tableControl.appendDiv('ControlGraph'),
		$controlMap = $tableControl.appendDiv('ControlMap'),
		$controlOrg = $tableControl.appendDiv('ControlOrg'),
		$controlLabel = $tableControl.appendDiv('ControlLabel');

	var $controlContainer = $tableControl.appendDiv('ControlContainer'); 
	
	var $infoSelect = $tableControl.appendDiv('InfoSelect').on('click', '', toggleSelect),
		$infoFilter = $tableControl.appendDiv('InfoFilter').on('click', '', resetFilter),
		$infoMore = $tableControl.appendDiv('InfoMore'),
		$infoLoad = $tableControl.appendDiv('InfoLoad').on('click', '', loadData);
		
	// control buttons has mouse over effects
	$("body").on("mouseenter", "#control_graph, #control_chart, #control_map, #control_organise",
		function() {
			$('#control_label').text($(this).data('label'));
		});

	$("body").on("mouseleave", "#control_graph, #control_chart, #control_map, #control_organise",
		function() {
			$('#control_label').text('');
		});
	
	// data and a row-pointer is used by many functions, may be very large
	var table;
	
	// create header
	var tableHeader = new Scout.Desktop.Table.Header($tableHeader, model.columns);
		
	// load data and create rows
	loadData();
	
	// update chart button
	$controlChart.data('label', model.chart)
		.hover(controlIn, controlOut)
		.click(controlClick)
		.click(controlChart);
	
	// update or disable graph button 
	if (model.graph) {
		$controlGraph.data('label', model.graph)
			.hover(controlIn, controlOut)
			.click(controlClick)
			.click(controlGraph);
	} else {
		$controlGraph.addClass('disabled');
	}

	// update or disable map button 
	if (model.map) {
		$controlMap.data('label', model.map)
			.hover(controlIn, controlOut)
			.click(controlClick)
			.click(controlMap);
	} else {
		$controlMap.addClass('disabled');
	}
	
	// organize button
	$controlOrg.data('label', textOrg)
		.hover(controlIn, controlOut)
		.click(controlClick)
		.click(controlOrg);
	
	// named funktions
	
	function controlIn (event) {
		var close = $(event.target).hasClass('selected') ? ' ' + textClose : '';
		$controlLabel.text($(event.target).data('label') + close);
	}
	
	function controlOut (event) {
		$controlLabel.text('');
	}
	
	function controlClick (event) {
		var $clicked = $(this);
	
		// reset handling resize 
		$controlResizeTop.off('mousedown');
		$controlResizeBottom.off('mousedown');
	
		if ($clicked.hasClass('selected')) {
			// classes: unselect and stop resizing 
			$clicked.removeClass('selected');
			$clicked.parent().removeClass('resize-on');		

			//adjust table
			$tableData.animateAVCSD('height',
				parseFloat($desktopTable.css('height')) - 80,
				function () {$(this).css('height', 'calc(100% - 80px'); }, 
				scrollbar.initThumb, 
				500);
			
			// visual: reset label and close control
			controlOut(event);
			$tableControl.animateAVCSD('height', 50, null, null, 500);

			// do not handel the click
			event.stopImmediatePropagation();
		} else {
			// classes: select and allow resizing 
			$clicked.selectOne();
			$clicked.parent().addClass('resize-on');

			//adjust table
			$tableData.animateAVCSD('height',
				parseFloat($desktopTable.css('height')) - 430,
				function () {$(this).css('height', 'calc(100% - 430px'); }, 
				scrollbar.initThumb, 
				500);
			
			// visual: update label, size container and control
			controlIn(event);
			$controlContainer.height(340);
			$tableControl.animateAVCSD('height', 400, null, null, 500);

			// set events for resizing
			$controlResizeTop.on('mousedown', '', resizeControl);
			$controlResizeBottom.on('mousedown', '', resizeControl);
		}
	}

	function controlChart (event) {
		new Scout.Desktop.Table.Chart($controlContainer, model.columns, table, filterCallback);
	}	

	function controlGraph (event) {
		new Scout.Desktop.Table.Graph($controlContainer, model.id);
	}	

	function controlMap (event) {
		new Scout.Desktop.Table.Map($controlContainer, model.id, model.columns, table, filterCallback);
	}

	function controlOrg (event) {
		new Scout.Desktop.Table.Org($controlContainer,  model.columns, table);
	}
	
	function resizeControl (event) {
		$('body').addClass('row-resize')
			.on('mousemove', '', resizeMove)
			.one('mouseup', '', resizeEnd);	
		
		var offset = (this.id == 'ControlResizeTop')  ? 58 : 108;
		
		function resizeMove(event){
			var h = $bench.outerHeight() - event.pageY + offset;
			$tableControl.height(h);
			$tableData.height('calc(100% - ' + (h + 30) + 'px)');
			$controlContainer.height(h - 60);
			scrollbar.initThumb();
		}
		
		function resizeEnd(event){
			$('body').off('mousemove')
				.removeClass('row-resize');
		}
		
		return false;
	}
	
	function loadData () {
		table = $.syncAjax('drilldown_data', {'id': model.id});
		$('.table-row').remove();
		drawData(0);
		setInfoSelect(0, false);
	}	
	
	function drawData (startRow) {
		// this function hast to be fast
		var rowString = '';
		
		for (var r = startRow; r < Math.min(table.length, startRow + 100); r++) {
			var row = table[r];
			
			rowString += '<div class="table-row">';
			for (var c = 0; c < row.length; c++) {
				var width = model.columns[c].width,
					style = (width === 0) ? 'display: none' : 'width: ' + width + 'px';					
				rowString += '<div style = "' + style + ';">' + row[c] + '</div>';
			}
			
			rowString += '</div>';
		}
			
		// append block of rows
		$(rowString)
			.appendTo($tableDataScroll)
			.on('mousedown', '', clickData)
			.width(tableHeader.totalWidth + 4);
		
		// update info and scrollbar
		setInfoLoad(r); 
		scrollbar.initThumb();
		
		// repaint and append next block
		if (r < table.length) {
			setTimeout(function() { drawData(startRow + 100); }, 0); 
		}
	}
	
	function clickData (event) {
		var $row = $(event.delegateTarget),
			add = true;

		// click without ctrl always starts new selection, with ctrl toggle		
		if (event.ctrlKey) {
			add = !$row.hasClass('row-selected');
		} else {
			$('.row-selected').removeClass('row-selected');	
		}		
		
		// just a click...
		selectData(event);
		
		// ...or movement with held mouse button
		$(".table-row").one("mousemove", function(event){
			selectData(event);
		});

		// remove all events
		$(".table-row").one("mouseup", function(event){
			$(".table-row").unbind("mousemove");
		});
		
		// action for all affected rows
		function selectData (event) {
			// affected rows between $row and Target
			var firstIndex = $row.index(),
				lastIndex = $(event.delegateTarget).index();

			var startIndex = Math.min(firstIndex, lastIndex),
				endIndex = Math.max(firstIndex, lastIndex) + 1;
				
			var $actionRow = $('.table-row', $tableData).slice(startIndex, endIndex);
			
			// set/remove selection
			if (add) {
				$actionRow.addClass('row-selected');
			} else {
				$actionRow.removeClass('row-selected');
			}
			
			// draw nice border
			selectionBorder();
			
			// open and animate menu
			selectionMenu(event.pageX, event.pageY);
		}
	}
	
	function selectionBorder () {
		// remove nice border 
			$('.select-middle, .select-top, .select-bottom, .select-single')
				.removeClass('select-middle select-top select-bottom select-single');

		// draw nice border
		$rowSelected = $('.row-selected');
		$rowSelected.each(function (i) {
			var hasPrev = $(this).prevAll(':visible:first').hasClass('row-selected'),
				hasNext = $(this).nextAll(':visible:first').hasClass('row-selected');
			
			if (hasPrev && hasNext) $(this).addClass('select-middle');
			if (!hasPrev && hasNext) $(this).addClass('select-top');
			if (hasPrev && !hasNext) $(this).addClass('select-bottom');
			if (!hasPrev && !hasNext) $(this).addClass('select-single');
		});
		
		// show count
		setInfoSelect($rowSelected.length, $rowSelected.length == table.length);
	}
	
	function selectionMenu (x, y) {
		// selection 
		$rowSelected = $('.row-selected');
	
		// make menu - if not already there
		var $menuRow = $('#MenuRow');
		if ($menuRow.length === 0) {
			$menuRow = $('body').appendDiv('MenuRow')
				.on('click', '', clickRowMenu);
		}
		// place menu top-left
		$menuRow.css('left', $rowSelected.first().offset().left - 13)
			.css('top', $rowSelected.first().offset().top - 13);
		
		// move to the mouse pointer
		var moveMenu = function (event) {
			var top = $rowSelected.first().offset().top,
				bottom = $rowSelected.last().offset().top + 32;
				
			var toTop = Math.abs(top - y) < Math.abs(bottom - y) ? top - 13: bottom - 13,
				toLeft = x - 13;
			
			$menuRow.stop().animate({'top': toTop}, 
					{complete: function() {$menuRow.animate({'left': toLeft}, 500); }},
					500);
		};
		
		// start movement
		moveMenu(event);
	}
	
	function clickRowMenu (event) {
		var $clicked = $(this),
			id = model.id,
			x = $clicked.offset().left,
			y = $clicked.offset().top;
		
		new Scout.Menu(id, x, y);
	}	
	
	function toggleSelect () {
		var $rowSelected = $('.row-selected', $tableData);
		
		if ($rowSelected.length == table.length) {
			$rowSelected.removeClass('row-selected');
		} else {
			$('.table-row', $tableData).addClass('row-selected');
		}
		
		selectionBorder();
	}

	function filterCallback (testFunc) {
		var rowCount = 0,
			$rowSelected = $('.row-selected', $tableData),
			$allRows = $('.table-row', $tableDataScroll);
		
		$rowSelected.removeClass('row-selected');
		selectionBorder();

		$allRows.detach();
		$allRows.each(function (i) {
			var $row = $(this),
				show = testFunc($row);
	
			if (show){
				showRow($row);
				rowCount++;
			} else {
				hideRow($row);
			}
			scrollbar.initThumb();
		});
		
		setInfoFilter(rowCount);
		$allRows.appendTo($tableDataScroll);		
		scrollbar.initThumb();
	}
	
	function resetFilter (event) {
		$('.table-row', $tableData).each(function (i) { showRow($(this)); });
		$infoFilter.animateAVCSD('width', 0, function () {$(this).hide(); });
		$('.main-chart.selected').removeClassSVG('selected');
	}

	function showRow ($row) {
			$row.show()
				.animate({'height': '34', 'padding-top': '2', 'padding-bottom': '2'});
	}

	function hideRow ($row) {
			$row.hide()
				.animate({'height': '0', 'padding-top': '0', 'padding-bottom': '0'},
					{complete: function() {$(this).hide();}});
	}
	
	function setInfoLoad (count) {
		$infoLoad.html(findInfo(count) + ' geladen</br>Daten neu laden');		
		$infoLoad.show().widthToContent();
	}
	
	function setInfoMore (count) {
	}
	
	function setInfoFilter (count) {
		$infoFilter.html(findInfo(count) + ' gefiltert</br>Filter entfernen');		
		$infoFilter.show().widthToContent();
	}
	
	function setInfoSelect (count, all) {
		var allText = all ? 'Keine' : 'Alle';
		$infoSelect.html(findInfo(count) + ' selektiert</br>' + (all ? 'Keine' : 'Alle') + ' selektieren');
		$infoSelect.show().widthToContent();	
	}

	function findInfo (n) {
		if (n === 0 ) {
			return 'Keine Zeile';
		} else if (n == 1) {
			return 'Eine Zeile';
		} else {
			return n + ' Zeilen';
		}
	}
	
};

//
// table header namespace and element
//

Scout.Desktop.Table.Header = function ($tableHeader, columns) {
	var	totalWidth = 0;
	
	// create header based on model
	for (var i = 0; i < columns.length; i++) {
		var $header = $tableHeader.appendDiv('', 'header-item', columns[i].label)
			.data('type', columns[i].type)
			.css('width', columns[i].width);
		
		if (columns[i].width === 0) $header.hide();
		
		totalWidth += columns[i].width;
			
		$header.appendDiv('', 'header-control', '')
			.on('click', '', clickHeaderMenu);
		
		$header.appendDiv('', 'header-resize', '')
			.on('mousedown', '', resizeHeader);
	}
	
	this.totalWidth = totalWidth;
	
	function resizeHeader (event) {
		var startX = event.pageX - 8,
			$header = $(this).parent(),
			colNum = $header.index() + 1,
			headerWidth = $header.width(),
			totalWidth = $('.table-row').first().width();
	
		$('body').addClass('col-resize')
			.on('mousemove', '', resizeMove)
			.one('mouseup', '', resizeEnd);	
			
		return false;
		
		function resizeMove (event) {
			var diff = event.pageX - startX;

			if (headerWidth + diff > 80) {
				$header.css('width', headerWidth + diff);
				$('.table-row > div:nth-of-type(' + colNum + ' )').css('width', headerWidth + diff);	
				$('.table-row').width(totalWidth + diff);

			}
		}
		
		function resizeEnd (event){
			$('body').off('mousemove')
				.removeClass('col-resize');
		}
	}
	
	function clickHeaderMenu (event) {
		var $clicked = $(this),
			x = $clicked.offset().left,
			y = $clicked.offset().top;
		
		new Scout.Menu.Header(x, y);
	}
};

//
// graph namespace and element
//

Scout.Desktop.Table.Chart = function ($controlContainer, columns, table, filterCallback) {
	// group functions for dates
	// todo
	var dateDesc = ['jedes Datum anzeigen', 'gruppiert nach Wochentag',
			 'gruppiert nach Monat', 'gruppiert nach Jahr'],
		countDesc = 'Anzahl';
		
	var removeChart = null,
		xAxis,
		yAxis;
	
	// create container 
	var $chartSelect = $controlContainer.empty().appendDiv('ChartSelect');
	
	// create chart types for selection
	addSelectBar($chartSelect);
	addSelectStacked($chartSelect);
	addSelectLine($chartSelect);
	addSelectPie($chartSelect);
	addSelectScatter($chartSelect);
	
	// add addition rectangle for hover and event handling
	$('svg.select-chart')
		.appendSVG('rect', '', 'select-events')
		.attr('width', 75)
		.attr('height', 60)
		.attr('fill', 'none')
		.attr('pointer-events', 'all')
		.click(function (event) { chartSelect($(this).parent()); })
		.click(drawChart);
		
	// first chart type is preselected
	$('svg.select-chart').first().addClassSVG('selected');
					
	// create container for x/y-axis
	var $xAxisSelect = $controlContainer.appendDiv('XAxisSelect'),
		$yAxisSelect = $controlContainer.appendDiv('YAxisSelect');
	
	// all x/y-axis for selection
	for (var c1 = 0; c1 < columns.length; c1++) {
		var column1 = columns[c1];
		
		if (column1.type == 'key') continue;
		
		var $div = $.makeDiv('', 'select-axis', column1.label)
			.data('column', c1);
		
		if (column1.type == 'date') {
			$div.appendDiv('', 'select-axis-group', dateDesc[0]);
			$div.data('group', 0);				
		}
		
		$xAxisSelect.append($div);
		$yAxisSelect.append($div.clone(true));
	}
	
	// click handling for data
	$('.select-axis')
		.click(axisSelect)
		.click(drawChart);	
	
	// find best x and y axis
	// todo with data/matrix
	$xAxisSelect.children().eq(0).addClass('selected');
	$yAxisSelect.children().eq(1).addClass('selected');
	
	// create container for data 
	var $dataSelect = $controlContainer.appendDiv('DataSelect');
	$dataSelect.appendDiv('', 'select-data data-count', countDesc)
		.data('column', -1);
	
	// all data for selection
	for (var c2 = 0; c2 < columns.length; c2++) {
		var column2 = columns[c2];
		
		if ((column2.type == 'float') || (column2.type == 'int')) {
			$dataSelect.appendDiv('', 'select-data data-sum', column2.label)
				.data('column', c2);
		}
	}
	
	// click handling for data
	$('.select-data')
		.click(dataSelect)
		.click(drawChart);
	
	// first data type is preselected
	$('.select-data').first().addClass('selected');
		
	// draw first chart
	var $chartMain = $controlContainer.appendSVG('svg', 'ChartMain')
			.attrSVG('viewBox', '0 0 1000 320')
			.attr('preserveAspectRatio', 'xMinYMin');
	drawChart();

	function addSelectBar ($Container) {
		var $svg = $Container.appendSVG('svg', 'ChartBar', 'select-chart');
			show = [2, 4, 3, 3.5, 5];
			
		for (var s = 0; s < show.length; s++) {
			$svg.appendSVG('rect', '', 'select-fill')
				.attr('x', s * 14)
				.attr('y', 50 - show[s] * 9)
				.attr('width', 12)
				.attr('height',  show[s] * 9);
		}
	}	
	
	function addSelectStacked ($Container) {
		var $svg = $Container.appendSVG('svg', 'ChartStacked', 'select-chart'),
			show = [2, 4, 3.5, 5];

		for (var s = 0; s < show.length; s++) {
			$svg.appendSVG('rect', '', 'select-fill')
				.attr('x', 0)
				.attr('y', 16 + s * 9)
				.attr('width', show[s] * 14)
				.attr('height',  7);
		}
	}

	function addSelectLine ($Container) {
		var $svg = $Container.appendSVG('svg', 'ChartLine', 'select-chart'),
			show = [0, 1.7, 1, 2, 1.5, 3],
			pathPoints = [];

		for (var s = 0; s < show.length; s++) {
			pathPoints.push(2 + (s * 14) + ',' + (45 - show[s]  * 11));
		}
		
		$svg.appendSVG('path', '', 'select-fill-line').
			attr('d', 'M' + pathPoints.join('L'));
	}		
	
	function addSelectPie ($Container) {
		var $svg = $Container.appendSVG('svg', 'ChartPie', 'select-chart'),
			show = [[0, 0.1], [0.1, 0.25], [0.25, 1]];

		for (var s = 0; s < show.length; s++) {
			$svg.appendSVG('path', '', 'select-fill-pie').
				attr('d', pathSegment(37, 30, 24, show[s][0], show[s][1]));
		}		
	}
	
	function addSelectScatter ($Container) {
		var $svg = $Container.appendSVG('svg', 'ChartScatter', 'select-chart');
		
		$svg.appendSVG('line', '', 'select-fill-line')
			.attr('x1', 3).attr('y1', 53)
			.attr('x2', 70).attr('y2', 53);

		$svg.appendSVG('line', '', 'select-fill-line')
			.attr('x1', 8).attr('y1', 12)
			.attr('x2', 8).attr('y2', 58);
			
		$svg.appendSVG('circle', '', 'select-fill')
			.attr('cx', 22 ).attr('cy', 40)
			.attr('r', 5 );
			
		$svg.appendSVG('circle', '', 'select-fill')
			.attr('cx', 50 ).attr('cy', 26)
			.attr('r', 11 );	
	}
	
	function chartSelect ($chart) {
		var chart = $chart.attr('id');
		
		$chart.siblings().removeClassSVG('selected');
		$chart.addClassSVG('selected');
		
		if (chart == 'ChartScatter') {
			$yAxisSelect.animateAVCSD('width', 175);
		} else {
			$yAxisSelect.animateAVCSD('width', 0);
		}
	}

	function axisSelect (event) {
		var $axis = $(this),
			group = $axis.data('group');
		
		$axis.siblings().animateAVCSD('height', 26);
		
		if (group >= 0) {
			$axis.animateAVCSD('height', 42);
				
			if ($axis.hasClass('selected')) {
				var newGroup = (group + 1) % dateDesc.length;
				$axis.data('group', newGroup)
					.children('.select-axis-group').text(dateDesc[newGroup]);
			}
		}
		
		$axis.selectOne('selected');

	}

	function dataSelect (event) {
		var $data = $(this);
		
		if ($data.hasClass('selected')) {
			if ($data.hasClass('data-sum')) {
				$data.removeClass('data-sum').addClass('data-median');
			} else if ($data.hasClass('data-median')) {
				$data.removeClass('data-median').addClass('data-sum');
			}
		}

		$data.selectOne('selected');
	}
	
	function drawChart () {
		var chart = $('.selected', $chartSelect).attr('id');
			
		// remove axis and chart
		$chartMain.children('.main-axis, .main-axis-x, .main-axis-y')
			.animateSVG('opacity', 0, 200, $.removeThis);
		if (removeChart) removeChart();

		// find xAxis and dataAxis
		var axis = $('.selected', $xAxisSelect).data('column'),
			axisGroup = $('.selected', $xAxisSelect).data('group');
			
		var data = $('.selected', $dataSelect).data('column'),
			dataCount = $('.selected', $dataSelect).hasClass('data-count'),
			dataSum = $('.selected', $dataSelect).hasClass('data-sum');
		
		// build matrix
		var matrix = new Scout.Desktop.Matrix(columns, table),
			dataAxis = matrix.addData(data, dataCount ? -1 : (dataSum ? 1 : 2));
			
			xAxis = matrix.addAxis(axis, axisGroup);
									
		// in case of scatter
		if (chart == 'ChartScatter') {
			var axis2 = $('.selected', $yAxisSelect).data('column'),
				axis2Group = $('.selected', $yAxisSelect).data('group');
			
			yAxis = matrix.addAxis(axis2, axis2Group);
		}
				
		// calculate matrix
		cube = matrix.calculateCube();

		// based on chart type: set class and draw chart
		if (chart == 'ChartBar') {
			$('.select-axis', $xAxisSelect).removeClass('axis-ver axis-around').addClass('axis-hor');
			drawBar(xAxis, dataAxis, cube);
		} else if (chart == 'ChartStacked') {
			$('.select-axis', $xAxisSelect).removeClass('axis-hor axis-around').addClass('axis-ver');			
			drawStacked(xAxis, dataAxis, cube);
		} else if (chart == 'ChartLine') {
			$('.select-axis', $xAxisSelect).removeClass('axis-ver axis-around').addClass('axis-hor');
			drawLine(xAxis, dataAxis, cube);
		} else if (chart == 'ChartPie') {
			$('.select-axis', $xAxisSelect).removeClass('axis-ver axis-hor').addClass('axis-around');
			drawPie(xAxis, dataAxis, cube);
		} else if (chart == 'ChartScatter') {
			$('.select-axis', $xAxisSelect).removeClass('axis-ver axis-around').addClass('axis-hor');
			$('.select-axis', $yAxisSelect).addClass('axis-up');
			drawScatter(xAxis, yAxis, dataAxis, cube);
		}
		return false;
	}
		
	function drawBar(xAxis, dataAxis, cube) {
		// dimension functions
		var width = Math.min(800 / (xAxis.max - xAxis.min), 60),
			x = function (i) { return 100 + (i - xAxis.min) * width; },
			y = function (i) { return 280 - i / (dataAxis.max - 0) * 240; };
		
		// draw data-axis
		var labels = [0, dataAxis.max / 4, dataAxis.max / 2, dataAxis.max / 4 * 3, dataAxis.max];
		for (var l = 0; l < labels.length; l++) {
			var label = labels[l],
				text = dataAxis.format(label);
			
			drawAxisLine(x(xAxis.min) - 10, y(label), x(xAxis.max + 1) + 7,  y(label));
			drawAxisText(x(xAxis.min) - 20, y(label), 'y', text);
		}

		// draw x-axis and values
		for (var a = 0; a < xAxis.length; a++) {
			var key = xAxis[a],
				mark = xAxis.format(key),
				value = cube.getValue([key])[0];
				
			drawAxisText(x(key) + width / 2 - 1.5, y(0) + 14, 'x', mark);
			
			$chartMain.appendSVG('rect', '', 'main-chart')
				.attr('x', x(key)).attr('y', y(0))
				.attr('width',  Math.max(2, width - 3)).attr('height', 0)
				.delay(200)
				.animateSVG('height', 280 - y(value), 600)
				.animateSVG('y', y(value), 600)
				.attr('data-xAxis', key)
				.click(chartClick);		
		}

		// function for later remove
		removeChart = function() {
			$chartMain.children('.main-chart')
				.animateSVG('height', 0, 200)
				.animateSVG('y', y(0), 200, $.removeThis);
		};
	}

	function drawStacked(xAxis, dataAxis, cube) {
		// dimension functions
		var height = Math.min(240 / (xAxis.max - xAxis.min), 30),
			x = function (i) { return 100 + i / dataAxis.max * 800; },
			y = function (i) { return 50 + (i - xAxis.min) * height ; };
			
		// draw data-axis
		var labels = [0,dataAxis.max / 4, dataAxis.max / 2, dataAxis.max / 4 * 3, dataAxis.max];
		for (var l = 0; l < labels.length; l++) {
			var label = labels[l],
				text = dataAxis.format(label);
			
			drawAxisLine(x(label), y(xAxis.min) - 10, x(label),  y(xAxis.max + 1) + 7);
			drawAxisText(x(label), y(xAxis.min) - 20, 'x', text);
		}

		// draw x-axis and values
		for (var a = 0; a < xAxis.length; a++) {
			var key = xAxis[a],
				mark = xAxis.format(key),
				value = cube.getValue([key])[0];
				
			drawAxisText(x(0) - 15, y(key) + height / 2, 'y', mark);
			
			$chartMain.appendSVG('rect', '', 'main-chart')
				.attr('x', x(0)).attr('y', y(key))
				.attr('width', 0).attr('height', Math.max(2, height - 3))
				.delay(200)
				.animateSVG('width', x(value) - 100)
				.attr('data-xAxis', key)
				.click(chartClick);		
	
		}

		// function for later remove
		removeChart = function() {
			$chartMain.children('.main-chart')
				.animateSVG('width', 0, 200, $.removeThis);
		};
	}
	
	function drawLine(xAxis, dataAxis, cube) {
		// dimension functions
		var x = function (i) { return 100 + (i - xAxis.min) / (xAxis.max - xAxis.min) * 800; },
			y = function (i) { return 280 - i / (dataAxis.max - 0) * 240; };
		
		// draw data-axis
		var labels = [0, dataAxis.max / 4, dataAxis.max / 2, dataAxis.max / 4 * 3, dataAxis.max];
		for (var l = 0; l < labels.length; l++) {
			var label = labels[l],
				text = dataAxis.format(label);
			
			drawAxisLine(x(xAxis.min) - 10, y(label), x(xAxis.max) + 10,  y(label));
			drawAxisText(x(xAxis.min) - 20, y(label), 'y', text);
		}
		
		// draw x-axis
		var delta = xAxis.max - xAxis.min,
			labelsX;
		if (xAxis.length > 14) {
			labelsX = [xAxis.min, xAxis.min + delta / 4, xAxis.min + delta / 2, 
							xAxis.min + delta / 4 * 3, xAxis.max];
		} else {
			labelsX = xAxis;
		}
		
		for (var k = 0; k < labelsX.length; k++) {
			var labelX = labelsX[k],
				textX = xAxis.format(labelX);
			
			drawAxisLine(x(labelX), y(0) - 3, x(labelX), y(0) + 3);
			drawAxisText(x(labelX), y(0) + 14, 'x', textX);
		}
							
		// draw values
		for (var a = 0; a < xAxis.length; a++) {
			if (a === 0) continue;
		
			var key1 = xAxis[a - 1],
				key2 = xAxis[a],
				value1 = cube.getValue([key1])[0];	
				value2 = cube.getValue([key2])[0];		

			$chartMain.appendSVG('line', '', 'main-chart')
				.attr('x1', x(key1)).attr('y1', y(0))
				.attr('x2', x(key2)).attr('y2', y(0))
				.delay(200)
				.animateSVG('y1', y(value1), 600)		
				.animateSVG('y2', y(value2), 600);		
		}

		// function for later remove
		removeChart = function() {
			$chartMain.children('.main-chart')
				.animateSVG('y1', y(0), 200)
				.animateSVG('y2', y(0), 200, $.removeThis);
		};
	}
	
	function drawPie(xAxis, dataAxis, cube) {
	// circle for surrounding text, hehe: svg ;)
		$chartMain.appendSVG('path', 'ArcAxis', 'main-axis')
			.attr('fill', 'none')
			.attr("d", "M 450 160 m 0, -110 a 110,110 0 1, 1 0,220 a 110,110 0 1, 1 0,-220");
			
		var startAngle = 0,
			endAngle;
			
		for (var a = 0; a < xAxis.length; a++) {
			var key = xAxis[a],
				mark = xAxis.format(key),
				value = cube.getValue([key])[0];	
			
			endAngle = startAngle + value / dataAxis.total;

			// segment
			
			tweenIn = function (now, fx) {
							var start = this.getAttribute('data-start'),
								end = this.getAttribute('data-end');
							this.setAttribute('d', pathSegment(450, 160, 105, start * fx.pos, end * fx.pos));
						};
						
			tweenOut = function (now, fx) {
							var start = this.getAttribute('data-start'),
								end = this.getAttribute('data-end');
							this.setAttribute('d', pathSegment(450, 160, 105, start * (1 - fx.pos), end * (1 - fx.pos)));
						};
			
			// arc segement
			$chartMain.appendSVG('path', '', 'main-chart')
				.attr('data-start', startAngle)
				.attr('data-end', endAngle)
				.delay(200)
				.animate({tabIndex: 0}, {step: tweenIn, duration: 600})
				.attr('data-xAxis', key)
				.click(chartClick);		

			
			// axis around the circle
			$chartMain.appendSVG('text', '', 'main-axis-x')
				.appendSVG('textPath')
					.attrSVG('startOffset', (startAngle + endAngle) / 2 * 100 + '%')
					.attrXLINK('href', '#ArcAxis')
					.text(mark)
					.attr('opacity', 0)
					.delay(400).animateSVG('opacity', 1, 400);	
			
			// data inside the arc
			var midPoint = (startAngle + (endAngle - startAngle) / 2) * 2 * Math.PI;

			$chartMain.appendSVG('text', '', 'main-axis')
				.attr('x', 450 + 70 * Math.sin(midPoint))
				.attr('y', 160 - 70 * Math.cos(midPoint))
				.attr('fill', '#fff')
				.text(Math.round(value / dataAxis.total * 100) + '%')
				.attr('opacity', 0)
				.delay(600).animateSVG('opacity', 1, 300);	
				
			startAngle = endAngle;
		}
		
		// function for later remove
		removeChart = function() {
			$chartMain.children('.main-chart')
				.animate({tabIndex: 0}, {step: tweenOut, complete: $.removeThis, duration: 200});
		};
	}

	function drawScatter(xAxis, yAxis, dataAxis, cube) {
		// dimension functions
		var x = function (i) { return 100 + (i - xAxis.min) / (xAxis.max - xAxis.min) * 700; },
			y = function (i) { return 280 - (i - yAxis.min) / (yAxis.max - yAxis.min) * 240; };
		
		// draw x-axis
		var deltaX = xAxis.max - xAxis.min,
			labelsX;
			
		if (xAxis.length > 14) {
			labelsX = [xAxis.min, xAxis.min + deltaX / 4, xAxis.min + deltaX / 2, xAxis.min + deltaX / 4 * 3, xAxis.max];
		} else {
			labelsX = xAxis;
		}
		
		for (var k = 0; k < labelsX.length; k++) {
			var labelX = labelsX[k],
				textX = xAxis.format(labelX);
			
			drawAxisLine(x(labelX), y(yAxis.max) - 7, x(labelX), y(yAxis.min) + 3);
			drawAxisText(x(labelX), y(yAxis.min) + 14, 'x', textX);
		}

		// draw y-axis
		var deltaY = yAxis.max - yAxis.min,
			labelsY;
			
		if (yAxis.length > 14) {
			labelsY = [yAxis.min, yAxis.min + deltaY / 4, yAxis.min + deltaY / 2, yAxis.min + deltaY / 4 * 3, yAxis.max];
		} else {
			labelsY = yAxis;
		}
		
		for (var l = 0; l < labelsY.length; l++) {
			var labelY = labelsY[l],
				textY = yAxis.format(labelY);
			
			drawAxisLine(x(xAxis.min) - 10, y(labelY), x(xAxis.max), y(labelY));
			drawAxisText(x(xAxis.min) - 20, y(labelY), 'y', textY);
		}
		// draw values
		for (var a1 = 0; a1 < xAxis.length; a1++) {
			for (var a2 = 0; a2 < yAxis.length; a2++) {
				var key1 = xAxis[a1],
					key2 = yAxis[a2],
					testValue = cube.getValue([key1, key2]);

				if (testValue) {
					var value = testValue[0],
						r = Math.max(Math.sqrt((value - dataAxis.min) / (dataAxis.max - dataAxis.min)) * 40, 5);		

					$chartMain.appendSVG('circle', '', 'main-chart')
						.attr('cx', x(key1))
						.attr('cy', y(key2))
						.attr('r', 0)
						.delay(200)
						.animateSVG('r', r, 600)
					.attr('data-xAxis', key1)
					.attr('data-yAxis', key2)
					.click(chartClick);		
				}
			}
		}

		// function for later remove
		removeChart = function() {
			$chartMain.children('.main-chart')
				.animateSVG('r', 0, 200, $.removeThis);
		};
	}
	
	function drawAxisLine (x1, y1, x2, y2) {
		$chartMain.appendSVG('line', '', 'main-axis')
			.attr('x1', x1).attr('y1', y1)
			.attr('x2', x2).attr('y2', y2)
			.attr('opacity', 0)
			.delay(200).animateSVG('opacity', 1, 600);
	}
				
	function drawAxisText (x, y, c, t) {
		$chartMain.appendSVG('text', '', 'main-axis-' + c)
			.attr('x', x).attr('y', y)
			.text(t)
			.attr('opacity', 0)
			.delay(200).animateSVG('opacity', 1, 600);	
	}
	
	function pathSegment (mx, my, r, start, end) {
		var s = start * 2 * Math.PI,
			e = end * 2 * Math.PI,
			pathString = '';
			
		pathString += 'M' + (mx + r * Math.sin(s)) + ',' + (my - r * Math.cos(s));
		pathString += 'A' + r + ', ' + r;
		pathString += (end - start < 0.5) ? ' 0 0,1 ' : ' 0 1,1 ';
		pathString += (mx + r * Math.sin(e)) + ',' + (my - r * Math.cos(e));
		pathString += 'L' + mx + ',' + my + 'Z';
		
		return pathString;
	}
	
	function chartClick (event) {
		var $clicked = $(this);
		
		// change state
		if (event.ctrlKey) {
			if ($clicked.hasClassSVG('selected')) {
				$clicked.removeClassSVG('selected');;
			} else {
				$clicked.addClassSVG('selected');
			}
		} else {
			$clicked.addClassSVG('selected');
			$clicked.siblings('.main-chart').removeClassSVG('selected');
		}

		//  prepare filter
		var filters = [],
			oneDim = $('.selected', $chartSelect).attr('id') != 'ChartScatter';

		//  find all filter
		$('.main-chart.selected').each( function () {
			var dX = parseFloat($(this).attr('data-xAxis'));
			
			if (oneDim) {
				filters.push(dX);
			} else {				
				dY = parseFloat($(this).attr('data-yAxis'));
				filters.push(JSON.stringify([dX, dY]));
			}
		});		

		//  filter function 
		var testFunc = function ($row) { 
			var textX = $row.children().eq(xAxis.column).text(),
				nX = xAxis.norm(textX);
			
			if (oneDim) {			
				return (filters.indexOf(nX) > -1);
			} else {
				var textY = $row.children().eq(yAxis.column).text(),
					nY = yAxis.norm(textY);
				return (filters.indexOf(JSON.stringify([nX, nY])) > -1);
			}
		}; 

		// callback to table
		filterCallback(testFunc);
	}
};

//
// matrix namespace and element
//

Scout.Desktop.Matrix = function (columns, table) {
	var allData = [],
		allAxis = [];
	
	// public functions
	this.addData = addData;
	this.addAxis = addAxis;
	this.calculateCube = calculateCube;
	
	// return (empty) matrix
	return this;
	
	function addData (data, dataGroup) {
		var dataAxis = [];
		
		allData.push(dataAxis);
		dataAxis.column = data;
		
		dataAxis.format = function (n) {return $.numberToString(n, 0); };

		if (dataGroup == -1) {
			dataAxis.norm = function (f) {return 1; };
			dataAxis.group = function (array) {return array.length; };
		} else if (dataGroup == 1) {
			dataAxis.norm = function (f) {return parseFloat(f); };
			dataAxis.group = function (array) {return array.reduce(function(a, b) {return a + b; }); };
		} else if (dataGroup == 2) {
			dataAxis.norm = function (f) {return parseFloat(f); };
			dataAxis.group = function (array) {return array.reduce(function(a, b) {return a + b; }) / array.length; };
		}
		
		return dataAxis;
	}
	
	function addAxis (axis, axisGroup) {
		var keyAxis = [];
		
		allAxis.push(keyAxis);
		keyAxis.column = axis;
		keyAxis.normTable = [];
		
		keyAxis.add = function (k) { if (keyAxis.indexOf(k) == -1) keyAxis.push(k); };
		keyAxis.reorder = function () { keyAxis.sort(); };

		if (columns[axis].type == 'date') {
			if (axisGroup === 0) {
				keyAxis.norm = function (f) {return $.stringToDate(f).getTime(); };
				keyAxis.format = function (n) {return $.dateToString(new Date(n)); };
			} else if (axisGroup === 1) {
				keyAxis.norm = function (f) {return ($.stringToDate(f).getDay() + 6) % 7; };
				keyAxis.format = function (n) {return $.WEEKDAY_LONG[n]; };
			} else if (axisGroup === 2) {
				keyAxis.norm = function (f) {return $.stringToDate(f).getMonth(); };
				keyAxis.format = function (n) {return $.MONTH_LONG[n]; };
			} else if (axisGroup === 3) {
				keyAxis.norm = function (f) {return $.stringToDate(f).getFullYear(); };
				keyAxis.format = function (n) {return String(n); };
			}
		} else if (columns[axis].type == 'int'){
			keyAxis.norm = function (f) {return parseInt(f, 10); };
			keyAxis.format = function (n) {return $.numberToString(n, 0); };
		} else if (columns[axis].type == 'float'){
			keyAxis.norm = function (f) {return parseFloat(f); };
			keyAxis.format = function (n) {return $.numberToString(n, 0); };
		} else {
			keyAxis.norm = function (f) {var index =  keyAxis.normTable.indexOf(f);
										if (index == -1) {
											return  keyAxis.normTable.push(f) - 1;
										} else {
											return index;
										} };
			keyAxis.format = function (n) { return keyAxis.normTable[n]; };
			keyAxis.reorder = function () { log('TODO');};

		}
		
		return keyAxis;
	}
	
	function calculateCube () {
		var cube = {},
			r, v, k, data;
		
		// collect data from table
		for (r = 0; r < table.length; r++) {
			var keys = [];
			for (k = 0; k < allAxis.length; k++) {
				key = table[r][allAxis[k].column];
				normKey = allAxis[k].norm(key);
				
				allAxis[k].add(normKey);
				keys.push(normKey);
			}
			keys = JSON.stringify(keys);
			
			var values = [];
			for (v = 0; v < allData.length; v++) {
				data = table[r][allData[v].column];
				normData = allData[v].norm(data);
				
				values.push(normData);
			}	

			if (cube[keys]) {
				cube[keys].push(values);
			} else {
				cube[keys] = [values];
			}
		}

		// group data		
		for (v = 0; v < allData.length; v++) {
			data = allData[v];
			
			data.total = 0;
			data.min = null;
			data.max = null;

			for (k in cube) {
				if (cube.hasOwnProperty(k)) {
					var allCell = cube[k],
						subCell = [];

					for (i = 0; i < allCell.length; i++) {
						subCell.push(allCell[i][v]);
					}
					
					var newValue = allData[v].group(subCell);
					cube[k][v] = newValue;
					data.total += newValue;
					
					if (newValue < data.min || data.min === null) data.min = newValue; 
					if (newValue > data.max || data.min === null) data.max = newValue; 
				}
			}
			
			var f = Math.ceil(Math.log(data.max) / Math.LN10) - 1;
			
			data.max = Math.ceil(data.max / Math.pow(10, f)) * Math.pow(10, f);
			data.max = Math.ceil(data.max / 4) * 4;
		}

		// find dimensions and sort
		for (k = 0; k < allAxis.length; k++) {
			key = allAxis[k];
			
			key.min = Math.min.apply(null, key);
			key.max = Math.max.apply(null, key);
			
			key.reorder();
		}

		// acces function
		cube.getValue = function (keys) {
			keys = JSON.stringify(keys);

			if (cube.hasOwnProperty(keys)) {
				return cube[keys];
			} else {
				return null;
			}
		};
		
		return cube;
	}
};


//
// graph namespace and element
//

Scout.Desktop.Table.Graph = function ($controlContainer, id) {
	// create container 
	$graphContainer = $controlContainer.empty()
		.appendSVG('svg', 'GraphContainer');

	// some basics
	var wBox = 120,
		hBox = 60,
		kelvin = 1000,
		wContainer = $graphContainer.width(),
		hContainer = $graphContainer.height();
		
	// create container 
	var graph = $.syncAjax('drilldown_graph', {'id': id});

	// create all links with label
	for (var l = 0; l < graph.links.length; l++) {
		var link = graph.links[l];
		
		link.$div = $graphContainer.appendSVG('line', null, 'graph-link');
		link.$divText = $graphContainer.appendSVG('text', null, 'graph-link-text', link.label)
			.attr('dy', -5);
	}

	// create nodes with text and place them randomly
	for (var n = 0; n < graph.nodes.length; n++) {
		var node = graph.nodes[n];
	
		node.$div = $graphContainer.appendSVG('rect', null, 'graph-node ' + node.type)
			.attr('width', wBox).attr('height', hBox)
			.attr('x', 0).attr('y', 0)
			.on('mousedown', moveNode);
			
		node.$divText = $graphContainer.appendSVG('text', null, 'graph-node-text', node.name)
			.on('mousedown', moveNode);

		setNode(node,  Math.random() * (wContainer - wBox),  Math.random() * (hContainer - hBox));
	}
	
	// start optimization
	doPhysics();

	// moving nodes and links by dx and dy
	function setNode (node, dx, dy) {
		var x = getPos(node, 'x'),
			y = getPos(node, 'y');

		node.$div
			.attr('x', x + dx)
			.attr('y', y + dy);
			
		node.$divText
			.attr('x', x + dx + wBox / 2)
			.attr('y', y + dy + hBox / 2);
		
		for (var l = 0; l < graph.links.length; l++) {
			var link = graph.links[l];
			
			if (link.source == node.id) {
				link.$div
					.attr('x1', x + dx + wBox / 2)
					.attr('y1', y + dy + hBox / 2);
				setLabel(link);
			} else if (link.target == node.id) {
				link.$div
					.attr('x2', x + dx + wBox / 2)
					.attr('y2', y + dy + hBox / 2);
				setLabel(link);
			}
		}
	}
	
	// set label of a link
	function setLabel (link) {
		var x1 = getPos(link, 'x1'),
			y1 = getPos(link, 'y1'),
			x2 = getPos(link, 'x2'),
			y2 = getPos(link, 'y2');
		
		link.$divText
			.attr('x', (x1 + x2) / 2)
			.attr('y', (y1 + y2) / 2)
			.attr('transform', 'rotate( ' + (Math.atan((y2 - y1) / (x2 - x1)) / Math.PI * 180) + 
									', ' + ((x1 + x2) / 2) + ', ' + ((y1 + y2) / 2) + ')');	
	}	
	
	// force the nodes to their place
	function doPhysics () {
		var totalDiff = 0;

		for (var n = 0; n < graph.nodes.length; n++) {
			var node = graph.nodes[n],
				x = getPos(node, 'x'),
				y = getPos(node, 'y');
				dx = 0, dy = 0;
			
			// move center to the middle
			if (node.type == 'center') {
				dx += ((wContainer - wBox) / 2 - x) / 40;
				dy += ((hContainer - hBox) / 2 - y) / 40;
			}
			// move from outside
			dx -= (Math.min(x, 5) - 5) / 2;
			dy -= (Math.min(y, 5) - 5) / 2;
			dx += (Math.min(wContainer - wBox - x, 10) - 10) / 4;
			dy += (Math.min(hContainer - hBox - y, 10) - 10) / 4;
			
			// gravity
			dx += ((wContainer - wBox) / 2 - x) / 500;
			dy += ((hContainer - hBox) / 2 - y) / 500;

			// repulsion force
			for (var o = 0; o < graph.nodes.length; o++) {
				var otherNode = graph.nodes[o];
				if (o != n) {
					var	oX = getPos(otherNode, 'x'),
						oY = getPos(otherNode, 'y'),
						repForce = 100 / (Math.pow(x - oX, 2) + Math.pow(y - oY, 2));
	
					dx += (x - oX) * repForce;
					dy += (y - oY) * repForce;
						
				}
			}
			
			// spring force
			for (var l = 0; l < graph.links.length; l++) {
				var link = graph.links[l],
					oppositeNode = null;

				if (link.source === node.id){
					oppositeNode = graph.nodes[link.target];
				} else if (link.target === node.id){
					oppositeNode = graph.nodes[link.source];
				}
				
				if (oppositeNode) {
					otherX = getPos(oppositeNode, 'x');
					otherY = getPos(oppositeNode, 'y');
						
					var dist =  Math.sqrt(Math.pow(x - otherX, 2) + Math.pow(y - otherY, 2)),
						springForce = Math.log(dist / 260) / 10;

					dx -= (x - otherX) * springForce;
					dy -= (y - otherY) * springForce;					
				}
			}

			// adjust position
			setNode(node, dx, dy);
			totalDiff += Math.abs(dx)  + Math.abs(dy);
		}

		// cool down, heat up
		if (kelvin-- > 0) setTimeout(doPhysics, 0);
	}
	
	// move node by mouse
	function moveNode (event) {
		var startX = event.pageX,
			startY = event.pageY,
			clickedNode;
		
		for (var n = 0; n < graph.nodes.length; n++) {
			var node = graph.nodes[n];
			if ($(this).is(node.$div) || $(this).is(node.$divText)) clickedNode = graph.nodes[n];
		}
		
		$('body').on('mousemove', '', nodeMove)
			.one('mouseup', '', nodeEnd);	
		return false;
		
		function nodeMove (event) {
			setNode(clickedNode, event.pageX - startX, event.pageY - startY);
			startX = event.pageX;
			startY = event.pageY;
			kelvin = 0;
		}
		
		function nodeEnd (event){
			$('body').off('mousemove');
			kelvin = 200;
			doPhysics();
		}
	}
	
	function getPos(e, d) { return parseFloat(e.$div.attr(d)); }
	
};

//
// map namespace and element
//

Scout.Desktop.Table.Map = function ($controlContainer, id, columns, table, filterCallback) {
	// create container 
	$mapContainer = $controlContainer.empty()
		.appendSVG('svg', 'MapContainer')
		.attrSVG('viewBox', '5000 -100000 200000 83000')
		.attrSVG("preserveAspectRatio", "xMidYMid");
	
	// create container 
	var map = $.syncAjax('drilldown_map', {'id': id}),
		countries = map.objects.countries.geometries;
		
	// find all countires in table
	var tableCountries = [];
	for (var i = 0; i < columns.length; i++) {
		if (columns[i].type == 'geo') {
			for (var r = 0; r < table.length; r++) {
				var value = table[r][i];
				if ( tableCountries.indexOf(value) == -1) tableCountries.push(value);
			}
		}
	}
	
	// loop all countries and draw path
	for (var c = 0; c < countries.length; c++) {
		var borders = countries[c].arcs,
			pathString = '';
		
		// per country: loop boundaries
		for (var b = 0; b < borders.length; b++) {
			// inconsistent: if ony more than one boundary exists, hidden in sub array
			var border = (typeof borders[b][0] != 'number') ? borders[b][0] : borders[b],
				mainArray = [];
			
			// build arcs of every boundary
			for (var a = 0; a < border.length; a++) {
				// negativ arc-numbers are in reverse order
				var reverse = (border[a] < 0),
					arc = reverse ? ~border[a] : border[a],
					localArray = [],
					x, y;
				
				// loop all points of arc 
				for (var s = 0; s < map.arcs[arc].length; s++) {
					var line = map.arcs[arc][s];
					
					// first point is absolute, all other delta
					if (s === 0) {
						// todo: alaska and russia have overlap
						if ((countries[c].id == 'Russland') && (line[0] < 3000)) line[0] += 100000;
						x = line[0];
						y = line[1];
					} else {
						// todo: some pacific islands
						if (Math.abs(line[0]) > 8000) line[0] = 0;
						x += line[0];
						y += line[1];
					}
					
					// transform coordinates
					localArray.push((x * 2) + ',' + (-y));
				}
				
				// append array 
				localArray = reverse ? localArray.reverse() : localArray;				
				mainArray = $.merge(mainArray, localArray);
			}
			
			// build path per boundary
			pathString += 'M' + mainArray.join('L') + 'Z';
		}
		
		// finally: append country as svg path
		var $country = $mapContainer.appendSVG('path', countries[c].id, 'map-item')
			.attr('d', pathString)
			.click(clickMap);
			
		if (tableCountries.indexOf(countries[c].id) > -1) $country.addClassSVG('has-data');
	}

	function clickMap (event) {
		var $clicked = $(this);
		
		if (event.ctrlKey) {
			if ($clicked.hasClassSVG('selected')) {
				$clicked.removeClassSVG('selected');
			} else {
				$clicked.addClassSVG('selected');
			}
		} else {
			$clicked.addClassSVG('selected');
			$clicked.siblings('.selected').removeClassSVG('selected');
		}
		
		// find filter values
		var countries = [];
		$('.map-item.selected').each( function () {
			countries.push($(this).attr('id')); 
		});	
				
		//  filter function
		var testFunc = function ($row) { 
			for (var c = 0; c < columns.length; c++) {
				var text = $row.children().eq(c).text();
				if (countries.indexOf(text) > -1) return true;
			}
			return false;
		};
		
		// callback to table
		filterCallback(testFunc);
		
		
	}
};

//
// organize table namespace and element
//

Scout.Desktop.Table.Org = function ($controlContainer, columns, data) {
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

Scout.Menu.Header = function (x, y) {
	// create container
	var $menuHeader = $('body').appendDiv('MenuHeader')
				.css('left', x - 6).css('top', y - 10);

	// create buttons inside
	$menuHeader.appendDiv('MenuHeaderControl');
	$menuHeader.appendDiv('MenuHeaderSortUp');
	$menuHeader.appendDiv('MenuHeaderSortDown');
	$menuHeader.appendDiv('MenuHeaderFilter');
	$menuHeader.appendDiv('MenuHeaderAdd');
	$menuHeader.appendDiv('MenuHeaderMore');

	// animated opening
	var h = $menuHeader.css('height');		
	$menuHeader.css('height', 32)
		.animateAVCSD('height', h);
	
	// every user action will close menu
	$('*').one('mousedown keydown mousewheel', removeMenu);
	function removeMenu (event) {
		$menuHeader.animateAVCSD('height', 32, $.removeThis);
		return true;
	}
};

//
// scrollbar namespace and element
//

Scout.Scrollbar = function ($container, axis) {
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
	$(window).on('load resize', initThumb);
		
	// set this for later usage
	this.initThumb = initThumb;

	// use this function (from outside) if size of tree content changes
	function initThumb () {
		var offset = $container[0]["offset" + dim],
			scroll = $container[0]["scroll" + dim],
			margin = parseFloat($scrollbar.css('margin-top')),
			topContainer = parseFloat($container.css(dir));
			
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
