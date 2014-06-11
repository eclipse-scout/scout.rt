scout.AnalysisTableControl = function() {
  scout.AnalysisTableControl.parent.call(this);
};

scout.inherits(scout.AnalysisTableControl, scout.TableControl);

scout.AnalysisTableControl.prototype._render = function($parent) {
  this.$container = $parent.appendDiv(); //FIXME CGU maybe not necessary

//command container and commands
  var $commandContainer = this.$container.appendDiv('', 'command-container');

  $commandContainer.appendDiv('', 'command search', 'Daten anzeigen');
  $commandContainer.appendDiv('', 'separator', '');
  $commandContainer.appendDiv('', 'command new', 'Neues Kriterium').click(addCriteria);
  $commandContainer.appendDiv('', 'command delete', 'Kriterium verwerfen').click(removeCriteria);
  $commandContainer.appendDiv('', 'separator', '');
  $commandContainer.appendDiv('', 'command union', 'Ansicht wechseln').click(switchShow);
  $commandContainer.appendDiv('', 'command union', 'Vereinigungsmenge').click(setUnion);
  $commandContainer.appendDiv('', 'command distinct', 'Schnittmenge').click(setIntersect);
  $commandContainer.appendDiv('', 'separator', 'Durch klicken mit der rechten Maustaste...');
  $commandContainer.appendDiv('', 'command union', 'Simulator').click(simulateServer);

  // svg container
  var $vennContainer = this.$container
      .appendSVG('svg', '', 'venn-container')
      .attrSVG('viewBox', '0 0 500 340')
      .attrSVG('preserveAspectRatio', 'xMinYMin')
      .on('click', clickCriteria)
      .on('contextmenu', clickSet);

  var $vennDefs = $vennContainer.appendSVG('defs', '', 'venn-defs');

  $vennContainer.appendSVG('rect', '', 'venn-all')
    .attr('x', 5).attr('y', 15)
    .attr('width', 490).attr('height', 300)
    .attr('rx', 10).attr('ry', 10);

  // $criteria = circle per criteria; count calculated by server
  var $criteria = [],
    show = false,
    count = {};
  count.total = undefined;

  // constants for venn diagram
  var MID_X = 250, MID_Y = 165, DIST_R = 5, MIN_R = 20, MAX_R = 120;

  // auto add forst criteria
  addCriteria();

 function addCriteria () {
   // reset count
   count = {};
   count.total = undefined;

   // draw circle
   if ($criteria.length < 3) {
     var $div = $vennContainer.appendSVG('circle', '', 'venn-circle')
       .attr('cx', MID_X)
       .attr('cy', MID_Y)
       .attr('r', 0)
       .click(selectCriteria);

     $criteria.push($div);

     updateCriteria();
     selectCriteria.call($div);
     drawCriteria();
   }
 }

 function removeCriteria () {
   // reset count
   count = {};
   count.total = undefined;

   // remove circle
   var $selected = $('.selected', $vennContainer);
   for (var c = 0; c < $criteria.length; c++) {
     if ($criteria[c][0] == $selected[0]) {
       $criteria[c].animateSVG('r', 0, 300, $.removeThis);
       $criteria.splice(c, 1);
       break;
     }
   }
   // select next criteria
   if ($criteria.length) {
     updateCriteria();
     selectCriteria.call($criteria[$criteria.length - 1]);
     drawCriteria();
   }

 }

 function updateCriteria () {
   for (var c = 0; c < $criteria.length; c++) {
     $criteria[c][0].id = c;
   }
 }

 function selectCriteria () {
   // remove and add classes
   $(this).siblings().removeClassSVG('selected');
   $(this).addClassSVG('selected');
 }

 function switchShow () {
   show = !show;
   drawCriteria();
 }

 function drawCriteria () {
   // remove all text
   $('text', $vennContainer)
     .animateSVG('opacity', 0, 100, $.removeThis);

   // remove intersect elements
   $('.venn-set', $vennContainer).remove();

   // show count all data
   if (count.total) {
     $vennContainer.appendSVG('text', '', 'venn-all-text', count.total + ' Datensätze')
       .attr('x', 490).attr('y', 28);
   }

   // init variables
   var x, x0, x1, x2, y, y0, y1, y2, r0, r1, r2;
   var ret, d, d01, d02, d12;
   var alpha, beta;
   var intersec = [];

   // move circle, draw text and set
   if ($criteria.length === 1) {
     // that is easy...
     if (count.total && !show) {
       r0 = calcR(count['0'], MAX_R);
     } else {
       r0 = MAX_R * 0.9;
     }

     x0 = 0;
     y0 = 0;

     moveCircle($criteria[0], r0, x0, y0);
     drawText(count['0'], x0, y0);
     drawSetOuter(r0, x0, y0);
     drawSetMain('0', r0, x0, y0);
   } else if ($criteria.length === 2) {
     if (count.total && !show) {
       // calculate size of circles
       r0 = calcR(count['0'], MAX_R);
       r1 = calcR(count['1'], MAX_R);

       // find distance
       ret = findD(count['0'], count['1'], count['01'], r0, r1);
       r0 = ret.ra;
       r1 = ret.rb;
       d = ret.d;

       // find balance
       x1 = d / ((r1*r1) / (r0*r0) + 1);
       x0 = x1 - d;
     } else {
       r0 = MAX_R * 0.9;
       r1 = MAX_R * 0.9;

       x0 = -MAX_R * 0.6;
       x1 = MAX_R * 0.6;
     }

     y0 = 0;
     y1 = 0;

     // move and resize circle
     moveCircle($criteria[0], r0, x0, y0);
     moveCircle($criteria[1], r1, x1, y1);

     // draw text
     if (count.total && !show) {
       if (count['0'] == count['01']) {
         drawText(count['0'], x0, y0);
         drawText(count['1'], x1 - (d - r1 - r0) / 2, y1);
       } else if (count['1'] == count['01']) {
         drawText(count['0'], x0 + (d - r0 - r1) / 2, y0);
         drawText(count['1'], x1, y1);
       } else if (count['01'] === 0) {
         drawText(count['0'], x0, y0);
         drawText(count['1'], x1, y1);
       } else {
         drawText(count['0'], x0 + (d - r0 - r1) / 2, y0);
         drawText(count['1'], x1 - (d - r0 - r1) / 2, y1);
         drawText(count['01'], x0 + r0 + (d - r0 - r1) / 2, y0);
       }
     } else {
       drawText(count['0'], -MAX_R * 0.8, 0);
       drawText(count['1'], +MAX_R * 0.8, 0);
       drawText(count['01'], 0, 0);
     }

     drawSetOuter(r0, x0, y0, r1, x1, y1);
     drawSetMain('0', r0, x0, y0, r1, x1, y1);
     drawSetMain('1', r1, x1, y1, r0, x0, y0);
     drawSetIntersect('01', r0, x0, y0, r1, x1, y1);
   } else if ($criteria.length === 3) {
     if (count.total && !show) {

       // calculate size of circles
       r0 = calcR(count['0'], MAX_R * 0.9);
       r1 = calcR(count['1'], MAX_R * 0.9);
       r2 = calcR(count['2'], MAX_R * 0.9);

       // find distance
       ret = findD(count['0'], count['1'], count['01'], r0, r1);
       r0 = ret.ra;
       r1 = ret.rb;
       d01 = ret.d;

       ret = findD(count['0'], count['2'], count['02'], r0, r2);
       r0 = ret.ra;
       r2 = ret.rb;
       d02 = ret.d;

       ret = findD(count['1'], count['2'], count['12'], r1, r2);
       r1 = ret.ra;
       r2 = ret.rb;
       d12 = ret.d;

       // find balance, start with 1
       x1 = d01 / ((r1*r1) / (r0*r0) + 1);
       y1 = 0;

       // 0 is simple ;)
       x0 = x1 - d01;
       y0 = 0;

       //  fit 2 with 1
       if (d12 > d01 + d02 || count['12'] === 0) {
         x2 = x0 - d02;
         y2 = y0;
       } else  if (d12 < d01 - d02 || d12 < d02 - d01) {
         x2 = x0 + d02;
         y2 = y0;
       } else  {
         alpha = Math.acos((d02*d02 + d01*d01 - d12*d12) / (2 * d02 * d01));
         beta = Math.acos((d12*d12 + d01*d01 - d02*d02) / (2 * d12 * d01));

         x2 = x0 + d02 * Math.cos(alpha);
         y2 = y0 + d02 * Math.sin(alpha);
       }

       // find center
       var cx = (r0 * x0 + r1 * x1 + r2 * x2 ) / (r0 + r1 + r2) ;
       var cy = (r0 * y0 + r1 * y1 + r2 * y2 ) / (r0 + r1 + r2) ;

       x0 = x0 - cx;
       x1 = x1 - cx;
       x2 = x2 - cx;

       y0 = y0 - cy;
       y1 = y1 - cy;
       y2 = y2 - cy;
     } else {
       r0 = MAX_R * 0.7;
       r1 = MAX_R * 0.7;
       r2 = MAX_R * 0.7;

       x0 = -MAX_R * 0.47;
       x1 = MAX_R * 0.47;
       x2 = 0;

       y0 = -MAX_R * 0.4;
       y1 = -MAX_R * 0.4;
       y2 = MAX_R * 0.4;
     }

     // move and resize circles
     moveCircle($criteria[0], r0, x0, y0);
     moveCircle($criteria[1], r1, x1, y1);
     moveCircle($criteria[2], r2, x2, y2);

     if (!count.total || show) {
       drawText(count['0'], -MAX_R * 0.7, -MAX_R * 0.5);
       drawText(count['1'], +MAX_R * 0.7, -MAX_R * 0.5);
       drawText(count['2'], 0, MAX_R * 0.6);

       drawText(count['01'], 0, -MAX_R * 0.5);
       drawText(count['02'], -MAX_R * 0.35, MAX_R * 0.1);
       drawText(count['12'], +MAX_R * 0.35, MAX_R * 0.1);

       drawText(count['012'], 0,  -MAX_R * 0.1);
     }

     drawSetOuter(r0, x0, y0, r1, x1, y1, r2, x2, y2);
     drawSetMain('0', r0, x0, y0, r1, x1, y1, r2, x2, y2);
     drawSetMain('1', r1, x1, y1, r0, x0, y0, r2, x2, y2);
     drawSetMain('2', r2, x2, y2, r1, x1, y1, r0, x0, y0);
     drawSetIntersect('01', r0, x0, y0, r1, x1, y1, r2, x2, y2);
     drawSetIntersect('02', r0, x0, y0, r2, x2, y2, r1, x1, y1);
     drawSetIntersect('12', r2, x2, y2, r1, x1, y1, r0, x0, y0);

     //TODO cru: one element can not be clicked
     drawSetTriple('012', r2, x2, y2, r1, x1, y1, r0, x0, y0);
   }
   function calcR (size, limit) {
     return Math.max(limit * Math.sqrt(size / count.total), MIN_R);
   }

   function findD (ca, cb, cab, ra, rb) {
     var ret = {};

     if (ca == cab) {
       ret.ra = ra - DIST_R;
       ret.rb = rb + DIST_R;
       ret.d = rb - ra;
     } else if (cb == cab) {
       ret.ra = ra + DIST_R;
       ret.rb = rb - DIST_R;
       ret.d = ra - rb;
     } else if (cab === 0) {
       ret.ra = ra;
       ret.rb = rb;
       ret.d = ra + rb + DIST_R * 2;
     } else {
       ret.ra = ra;
       ret.rb = rb;
       ret.d = (1 - 2 * cab / (ca + cb)) * (ra + rb - DIST_R * 2);
     }

     return ret;
   }

   function moveCircle ($div, r, dx, dy) {
     $div
       .animateSVG('cx', MID_X + dx)
       .animateSVG('cy', MID_Y + dy)
       .animateSVG('r', r);
   }

   function drawText (text, dx, dy) {
     if (count.total) {
       $vennContainer.appendSVG('text', '', 'venn-circle-text', text || '0')
       .attr('x', MID_X + dx)
       .attr('y', MID_Y + dy)
       .attr('opacity', 0)
       .animateSVG('opacity', 1);
     }
   }

   function drawSetOuter (ra, dxa, dya, rb, dxb, dyb, rc, dxc, dyc) {
     // TODO cru: clone existing object instead of creating new ones
     // TODO cru: collect all functions
     var $mask = $vennDefs.empty().appendSVG('mask', 'maskAll', '');
     appendRect($mask, '', 'white');
     if (ra) appendCircle($mask, dxa, dya, ra);
     if (rb) appendCircle($mask, dxb, dyb, rb);
     if (rc) appendCircle($mask, dxc, dyc, rc);

     appendRect($vennContainer, 'venn-set')
       .attr('mask', 'url(#maskAll)')
       .attr('id', 'all');
   }

   function drawSetMain (id, ra, dxa, dya, rb, dxb, dyb, rc, dxc, dyc) {
     var $mask = $vennDefs.appendSVG('mask', 'set' + id, '');
     appendCircle($mask, dxa, dya, ra, '', 'white');
     if (rb) appendCircle($mask, dxb, dyb, rb);
     if (rc) appendCircle($mask, dxc, dyc, rc);

     appendCircle($vennContainer, dxa, dya, ra, 'venn-set')
       .attr('mask', 'url(#set' + id + ')')
       .attr('id', id);
   }

   function drawSetIntersect (id, ra, dxa, dya, rb, dxb, dyb, rc, dxc, dyc) {
     var $clip = $vennDefs.appendSVG('clipPath', 'set-clip' + id, '');
     appendCircle($clip, dxb, dyb, rb);

     if (rc) {
       var $mask = $vennDefs.appendSVG('mask', 'set-mask' + id, '');
       appendCircle($mask, dxa, dya, ra, '', 'white');
       appendCircle($mask, dxc, dyc, rc);
     }

     appendCircle($vennContainer, dxa, dya, ra, 'venn-set')
       .attr('clip-path', 'url(#set-clip' + id + ')')
       .attr('mask', 'url(#set-mask' + id + ')')
       .attr('id', id);
   }

 function drawSetTriple (id, ra, dxa, dya, rb, dxb, dyb, rc, dxc, dyc) {
   var $clip = $vennDefs.appendSVG('clipPath', 'set-clip' + id, '');
   appendCircle($clip, dxb, dyb, rb);

   var $mask = $vennDefs.appendSVG('mask', 'set-mask' + id, '');
   appendCircle($mask, dxa, dya, ra);
   appendCircle($mask, dxc, dyc, rc, '', 'white');

   appendCircle($vennContainer, dxa, dya, ra, 'venn-set')
     .attr('clip-path', 'url(#set-clip' + id + ')')
     .attr('mask', 'url(#set-mask' + id + ')')
     .attr('id', id);
   }
 }

 function clickCriteria (event) {
   var candidate = findSet(event.clientX, event.clientY),
     select = $('.selected', $vennContainer)[0].id,
     next;

   if (candidate.length === 0) return;

   candidate.push(candidate[0]);
   next = candidate[candidate.indexOf(select) + 1];

   $.log(candidate, select, next);
   $('.venn-circle', $vennContainer).removeClassSVG('selected');
   $('#' + next, $vennContainer).addClassSVG('selected');
 }

 function clickSet (event) {
   var candidate = findSet(event.clientX, event.clientY),
     $clicked;
   $.log(candidate);
   if (candidate.length === 0) {
     $clicked = $('#all.venn-set',  $vennContainer);
   } else {
     $clicked = $('#' + candidate.sort().join('') + '.venn-set',  $vennContainer);
   }
   $.log($clicked, candidate.sort().join(''));
   if ($clicked.hasClassSVG('selected')) {
     $clicked.removeClassSVG('selected');
   } else {
     $clicked.addClassSVG('selected');
   }
   event.preventDefault();
 }

 function appendRect ($def, claz, fill) {
   var $rect = $def.appendSVG('rect', '', '')
     .attr('x', 5).attr('y', 15)
     .attr('width', 490).attr('height', 300)
     .attr('rx', 10).attr('ry', 10);

   if (claz) $rect.attr('class', claz);
   if (fill) $rect.attr('fill', fill);

   return $rect;
 }

 function appendCircle ($def, dx, dy, r, claz, fill) {
   var $circle = $def.appendSVG('circle', '', '')
     .attr('cx', MID_X + dx)
     .attr('cy', MID_Y + dy)
     .attr('r', r);

   if (claz) $circle.attr('class', claz);
   if (fill) $circle.attr('fill', fill);

   return $circle;
 }

 function findSet (x, y) {
   var root = $vennContainer[0],
     rpos = root.createSVGRect();

   rpos.x = event.clientX - $vennContainer.offset().left;
   rpos.y = event.clientY - $vennContainer.offset().top;
   $.log(rpos.x,rpos.y);
   rpos.width = rpos.height = 1;
   rpos.fill = 'red';

   var list = root.getIntersectionList(rpos, null),
     ret = [];

   for(var i = 0; i < list.length; i++) {
     var item = list[i].id;
     for(var j = 0; j < item.length; j++) {
       // workaround: do not use triple set to detect all sets
       if (ret.indexOf(item[j]) == -1 && item.length != 3) ret.push(item[j]);
     }
   }

   return ret;
 }

 function setUnion () {
 }

 function setIntersect () {
 }

 function simulateServer () {
   count.total = Math.ceil(Math.random() * 100000);

   var r0 = Math.ceil(count.total * Math.random()),
     r1 = Math.ceil(count.total * Math.random()),
     r2 = Math.ceil(count.total * Math.random());

   count['0'] = r0;
   count['1'] = r1;
   count['2'] = r2;

   if ($criteria.length === 2) {
     if (Math.random() < 0.2 && (r0 + r1  < count.total)) {
       count['01'] = 0;
     } else if (Math.random() < 0.4) {
       count['01'] = Math.ceil(Math.min(r0, r1));
     } else {
       count['01'] = Math.max(Math.ceil(Math.min(r0, r1) * Math.random()), r0 + r1 - count.total);
     }
   } else if ($criteria.length === 3) {
     if (Math.random() < 0.2 && (r0 + r1 + r2 < count.total)) {
       count['01'] = 0;
     } else if (Math.random() < 0.4) {
       count['01'] = Math.ceil(Math.min(r0, r1));
     } else {
       count['01'] = Math.max(Math.ceil(Math.min(r0, r1) * Math.random()), r0 + r1 - count.total);
     }

     if (Math.random() < 0.2 && (r0 + r1 + r2  < count.total)) {
       count['02'] = 0;
     } else if (Math.random() < 0.4) {
       count['02'] = Math.ceil(Math.min(r0, r2));
     } else {
       count['02'] = Math.max(Math.ceil(Math.min(r0, r2) * Math.random()), r0 + r2 - count.total);
     }

     if (Math.random() < 0.2 && (r0 + r1 + r2  < count.total)) {
       count['12'] = 0;
     } else if (Math.random() < 0.4) {
       count['12'] = Math.ceil(Math.min(r1, r2));
     } else {
       count['12'] = Math.max(Math.ceil(Math.min(r1, r2) * Math.random()), r1 + r2 - count.total);
     }

     if (Math.random() < 0.2 && (r0 + r1 + r2  < count.total)) {
       count['012'] = 0;
     } else if (Math.random() < 0.4) {
       count['012'] = Math.ceil(Math.min(count['01'], count['02'], count['12']));
     } else {
       count['012'] = Math.ceil(Math.min(count['01'], count['02'], count['12']) * Math.random());
     }
   }

   drawCriteria();
 }

};

scout.AnalysisTableControl.prototype._setDataModel = function(dataModel) {
  // NOP
};
