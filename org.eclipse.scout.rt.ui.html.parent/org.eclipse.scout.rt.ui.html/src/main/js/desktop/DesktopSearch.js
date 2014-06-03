scout.DesktopQuery = function($controlContainer) {
  $controlContainer.empty().text('query');
};

scout.DesktopAnalyze = function($controlContainer) {
  $controlContainer.empty();

  // command container
  var $commandContainer = $controlContainer.appendDiv('', 'command-container');

  $commandContainer.appendDiv('', 'command search', 'Daten anzeigen');
  $commandContainer.appendDiv('', 'separator', '');
  $commandContainer.appendDiv('', 'command new', 'Neues Kriterium').click(addCriteria);
  $commandContainer.appendDiv('', 'command delete', 'Kriterium verwerfen').click(removeCriteria);
  $commandContainer.appendDiv('', 'separator', '');
  $commandContainer.appendDiv('', 'command union', 'Vereinigungsmenge');
  $commandContainer.appendDiv('', 'command distinct', 'Schnittmenge');
  $commandContainer.appendDiv('', 'separator', 'eigene Auswahl');
  $commandContainer.appendDiv('', 'command union', 'Simulator').click(simulateServer);

  var MID_X = 250, MID_Y = 165, MIN_R = 20, MAX_R = 120;

  // svg container
  var $vennContainer = $controlContainer
      .appendSVG('svg', '', 'venn-container')
      .attrSVG('viewBox', '0 0 500 340')
      .attrSVG('preserveAspectRatio', 'xMinYMin');

  $vennContainer.appendSVG('rect', '', 'venn-all')
    .attr('x', 5).attr('y', 15)
    .attr('width', 490).attr('height', 300)
    .attr('rx', 10).attr('ry', 10);

  var $totalCount = $vennContainer.appendSVG('text', '', 'venn-all-text')
    .attr('x', 490).attr('y', 28);

  var $criteria = [],
    count;

  count = {};
  count.total = undefined;

  addCriteria();

 function addCriteria () {
   count = {};
   count.total = undefined;

   if ($criteria.length < 3) {
     var $div = $vennContainer.appendSVG('circle', '', 'venn-circle')
       .attr('cx', MID_X)
       .attr('cy', MID_Y)
       .attr('r', 0)
       .click(selectCriteria);
     $criteria.push($div);

     drawCriteria();
     selectCriteria.call($div);
   }
 }

 function removeCriteria () {
   count = {};
   count.total = undefined;

   var $selected = $('.selected', $vennContainer);
   for (var c = 0; c < $criteria.length; c++) {
     if ($criteria[c][0] == $selected[0]) {
       $criteria[c].animateSVG('r', 0, $.removeThis);
       $criteria.splice(c, 1);
       if ($criteria.length) {
          selectCriteria.call($criteria[$criteria.length - 1]);
       drawCriteria();
       return;
       }
     }
   }
 }

 function selectCriteria () {
   $(this).siblings().removeClassSVG('selected');
   $(this).addClassSVG('selected');
 }

 function drawCriteria () {
   if (count.total) {
     $totalCount.text(count.total + ' Datensätze ');
   } else {
     $totalCount.text('');
   }

   $('.venn-circle-text', $vennContainer)
     .animateSVG('opacity', 0, 100, $.removeThis);

   var x0, x1, x2, y0, y1, y2, r0, r1, r2;
   var ret, d, d01, d02, d12;
   var alpha, beta;
   $.log(count);

   if ($criteria.length === 1) {
     r0 = calcR(count['0'], MAX_R);
     x0 = 0;
     y0 = 0;

     moveCircle($criteria[0], r0, x0, y0);
     drawText(count['0'], x0, y0);
   } else if ($criteria.length === 2) {
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
     y0 = 0;

     // move and resize circle
     moveCircle($criteria[0], r0, x0, y0);
     moveCircle($criteria[1], r1, x1, y0);

     // draw text
     if (count['0'] == count['01']) {
       drawText(count['0'], x0, y0);
       drawText(count['1'], x1 - (d - r1 - r0) / 2, y0);
     } else if (count['1'] == count['01']) {
       drawText(count['0'], x0 + (d - r0 - r1) / 2, y0);
       drawText(count['1'], x1, y0);
     } else if (count['01'] === 0) {
       drawText(count['0'], x0, y0);
       drawText(count['1'], x1, y0);
     } else {
       drawText(count['0'], x0 + (d - r0 - r1) / 2, y0);
       drawText(count['1'], x1 - (d - r0 - r1) / 2, y0);
       drawText(count['01'], x0 + r0 + (d - r0 - r1) / 2, y0);
     }


   } else if ($criteria.length === 3) {
     // calculate size of circles
     r0 = calcR(count['0'], MAX_R - 20);
     r1 = calcR(count['1'], MAX_R - 20);
     r2 = calcR(count['2'], MAX_R - 20);

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

     // find balance, start with 0 and 1
     x1 = d01 / ((r1*r1) / (r0*r0) + 1);
     y1 = 0;

     x0 = x1 - d01;
     y0 = 0;

     // fit 0 and 2
     x2 = x0;
     y2 = y0 + d02;

     //  TODO cru: turn 2 to fit with 1
     if (d01 > d02) {
       x2 = x0 + d02;
       y2 = y0;
     } else {
     }

     //alpha = Math.acos((d02*d02 + d01*d01 - d12*d12) / (2 * d02 * d01));
     //beta = Math.acos((d12*d12 + d01*d01 - d02*d02) / (2 * d12 * d01));

     //x2 = x0 + d02 * Math.cos(alpha);
     //y2 = y0 + d02 * Math.sin(alpha);

     // find center
     var cx = (r0 * x0 + r1 * x1 + r2 * x2 ) / (r0 + r1 + r2) ;
     var cy = (r0 * y0 + r1 * y1 + r2 * y2 ) / (r0 + r1 + r2) ;

     // move and resize circles
     moveCircle($criteria[0], r0, x0 - cx, y0 - cy);
     moveCircle($criteria[1], r1, x1 - cx, y1 - cy);
     moveCircle($criteria[2], r2, x2 - cx, y2 - cy);

     // draw text
     drawText(count['0'], x0 - cx, y0 - cy);
     drawText(count['1'], x1 - cx, y1 - cy);
     drawText(count['2'], x2 - cx, y2 - cy);


   }

   function calcR (size, limit) {
     if (count.total) {
       return Math.max(limit * Math.sqrt(size / count.total), MIN_R);
     } else {
       return limit;
     }
   }

   function findD (ca, cb, cab, ra, rb) {
     var ret = {};

     if (ca == cab) {
       ret.ra = ra - 5;
       ret.rb = rb + 5;
       ret.d = rb - ra;
     } else if (cb == cab) {
       ret.ra = ra + 5;
       ret.rb = rb - 5;
       ret.d = ra - rb;
     } else if (cab === 0) {
       ret.ra = ra;
       ret.rb = rb;
       ret.d = ra + rb + 10;
     } else {
       ret.ra = ra;
       ret.rb = rb;
       ret.d = (1 - 2 * cab / (ca + cb)) * (ra + rb - 10);
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
       $vennContainer.appendSVG('text', '', 'venn-circle-text', text)
       .attr('x', MID_X + dx)
       .attr('y', MID_Y + dy)
       .attr('opacity', 0)
       .animateSVG('opacity', 1);
     }
   }
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

scout.DesktopCategory = function($controlContainer) {
  $controlContainer.empty().text('category');
};
