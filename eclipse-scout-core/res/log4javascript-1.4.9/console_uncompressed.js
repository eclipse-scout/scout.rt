// <BSI change>

// Our CSP rule prohibits inline scripts. That's why we refactored the original console_uncompressed.html
// from the log4javascript distribution, so it loads a regular script file. Because of this we also changed
// the IE and "old IE" detection used by log4javascript. We simply ask Scout what browser is used.
var isIe = window.opener.scout.Device.get().isInternetExplorer();
var isIePre7 = false; // not supported anymore, flag is always false

// Inline event handlers are not allowed -> attach them here
$('switch_TRACE').addEventListener('click', function() {
  applyFilters(); checkAllLevels();
});
$('switch_DEBUG').addEventListener('click', function() {
  applyFilters(); checkAllLevels();
});
$('switch_INFO').addEventListener('click', function() {
  applyFilters(); checkAllLevels();
});
$('switch_WARN').addEventListener('click', function() {
  applyFilters(); checkAllLevels();
});
$('switch_ERROR').addEventListener('click', function() {
  applyFilters(); checkAllLevels();
});
$('switch_FATAL').addEventListener('click', function() {
  applyFilters(); checkAllLevels();
});
$('switch_ALL').addEventListener('click', function() {
  toggleAllLevels(); applyFilters();
});

$('searchBox').addEventListener('click', function() {
  toggleSearchEnabled(true); applyFilters();
});
$('searchBox').addEventListener('keyup', scheduleSearch);
$('searchReset').addEventListener('click', clearSearch);
$('searchRegex').addEventListener('click', doSearch);
$('searchCaseSensitive').addEventListener('click', doSearch);
$('searchDisable').addEventListener('click', toggleSearchEnabled);

$('searchNext').addEventListener('click', searchNext);
$('searchPrevious').addEventListener('click', searchPrevious);
$('searchFilter').addEventListener('click', searchFilter);
$('searchHighlight').addEventListener('click', toggleSearchHighlight);

$('enableLogging').addEventListener('click', toggleLoggingEnabled);
$('wrap').addEventListener('click', toggleWrap);
$('newestAtTop').addEventListener('click', toggleNewestAtTop);
$('scrollToLatest').addEventListener('click', toggleScrollToLatest);
$('clearButton').addEventListener('click', clearLog);
$('hideButton').addEventListener('click', hide);
$('closeButton').addEventListener('click', closeWindow);
$('evaluateButton').addEventListener('click', evalCommandLine);

// </BSI change>

var loggingEnabled = true;
var logQueuedEventsTimer = null;
var logEntries = [];
var logEntriesAndSeparators = [];
var logItems = [];
var renderDelay = 100;
var unrenderedLogItemsExist = false;
var rootGroup, currentGroup = null;
var loaded = false;
var currentLogItem = null;
var logMainContainer;

function copyProperties(obj, props) {
  for (var i in props) {
    obj[i] = props[i];
  }
}

/*----------------------------------------------------------------*/

function LogItem() {
}

LogItem.prototype = {
  mainContainer: null,
  wrappedContainer: null,
  unwrappedContainer: null,
  group: null,

  appendToLog: function() {
    for (var i = 0, len = this.elementContainers.length; i < len; i++) {
      this.elementContainers[i].appendToLog();
    }
    this.group.update();
  },

  doRemove: function(doUpdate, removeFromGroup) {
    if (this.rendered) {
      for (var i = 0, len = this.elementContainers.length; i < len; i++) {
        this.elementContainers[i].remove();
      }
      this.unwrappedElementContainer = null;
      this.wrappedElementContainer = null;
      this.mainElementContainer = null;
    }
    if (this.group && removeFromGroup) {
      this.group.removeChild(this, doUpdate);
    }
    if (this === currentLogItem) {
      currentLogItem = null;
    }
  },

  remove: function(doUpdate, removeFromGroup) {
    this.doRemove(doUpdate, removeFromGroup);
  },

  render: function() {},

  accept: function(visitor) {
    visitor.visit(this);
  },

  getUnwrappedDomContainer: function() {
    return this.group.unwrappedElementContainer.contentDiv;
  },

  getWrappedDomContainer: function() {
    return this.group.wrappedElementContainer.contentDiv;
  },

  getMainDomContainer: function() {
    return this.group.mainElementContainer.contentDiv;
  }
};

LogItem.serializedItemKeys = {LOG_ENTRY: 0, GROUP_START: 1, GROUP_END: 2};

/*----------------------------------------------------------------*/

function LogItemContainerElement() {
}

LogItemContainerElement.prototype = {
  appendToLog: function() {
    var insertBeforeFirst = (newestAtTop && this.containerDomNode.hasChildNodes());
    if (insertBeforeFirst) {
      this.containerDomNode.insertBefore(this.mainDiv, this.containerDomNode.firstChild);
    } else {
      this.containerDomNode.appendChild(this.mainDiv);
    }
  }
};

/*----------------------------------------------------------------*/

function SeparatorElementContainer(containerDomNode) {
  this.containerDomNode = containerDomNode;
  this.mainDiv = document.createElement("div");
  this.mainDiv.className = "separator";
  this.mainDiv.innerHTML = "&nbsp;";
}

SeparatorElementContainer.prototype = new LogItemContainerElement();

SeparatorElementContainer.prototype.remove = function() {
  this.mainDiv.parentNode.removeChild(this.mainDiv);
  this.mainDiv = null;
};

/*----------------------------------------------------------------*/

function Separator() {
  this.rendered = false;
}

Separator.prototype = new LogItem();

copyProperties(Separator.prototype, {
  render: function() {
    var containerDomNode = this.group.contentDiv;
    if (isIe) {
      this.unwrappedElementContainer = new SeparatorElementContainer(this.getUnwrappedDomContainer());
      this.wrappedElementContainer = new SeparatorElementContainer(this.getWrappedDomContainer());
      this.elementContainers = [this.unwrappedElementContainer, this.wrappedElementContainer];
    } else {
      this.mainElementContainer = new SeparatorElementContainer(this.getMainDomContainer());
      this.elementContainers = [this.mainElementContainer];
    }
    this.content = this.formattedMessage;
    this.rendered = true;
  }
});

/*----------------------------------------------------------------*/

function GroupElementContainer(group, containerDomNode, isRoot, isWrapped) {
  this.group = group;
  this.containerDomNode = containerDomNode;
  this.isRoot = isRoot;
  this.isWrapped = isWrapped;
  this.expandable = false;

  if (this.isRoot) {
    if (isIe) {
      this.contentDiv = logMainContainer.appendChild(document.createElement("div"));
      this.contentDiv.id = this.isWrapped ? "log_wrapped" : "log_unwrapped";
    } else {
      this.contentDiv = logMainContainer;
    }
  } else {
    var groupElementContainer = this;

    this.mainDiv = document.createElement("div");
    this.mainDiv.className = "group";

    this.headingDiv = this.mainDiv.appendChild(document.createElement("div"));
    this.headingDiv.className = "groupheading";

    this.expander = this.headingDiv.appendChild(document.createElement("span"));
    this.expander.className = "expander unselectable greyedout";
    this.expander.unselectable = true;
    var expanderText = this.group.expanded ? "-" : "+";
    this.expanderTextNode = this.expander.appendChild(document.createTextNode(expanderText));

    this.headingDiv.appendChild(document.createTextNode(" " + this.group.name));

    this.contentDiv = this.mainDiv.appendChild(document.createElement("div"));
    var contentCssClass = this.group.expanded ? "expanded" : "collapsed";
    this.contentDiv.className = "groupcontent " + contentCssClass;

    this.expander.onclick = function() {
      if (groupElementContainer.group.expandable) {
        groupElementContainer.group.toggleExpanded();
      }
    };
  }
}

GroupElementContainer.prototype = new LogItemContainerElement();

copyProperties(GroupElementContainer.prototype, {
  toggleExpanded: function() {
    if (!this.isRoot) {
      var oldCssClass, newCssClass, expanderText;
      if (this.group.expanded) {
        newCssClass = "expanded";
        oldCssClass = "collapsed";
        expanderText = "-";
      } else {
        newCssClass = "collapsed";
        oldCssClass = "expanded";
        expanderText = "+";
      }
      replaceClass(this.contentDiv, newCssClass, oldCssClass);
      this.expanderTextNode.nodeValue = expanderText;
    }
  },

  remove: function() {
    if (!this.isRoot) {
      this.headingDiv = null;
      this.expander.onclick = null;
      this.expander = null;
      this.expanderTextNode = null;
      this.contentDiv = null;
      this.containerDomNode = null;
      this.mainDiv.parentNode.removeChild(this.mainDiv);
      this.mainDiv = null;
    }
  },

  reverseChildren: function() {
    // Invert the order of the log entries
    var node = null;

    // Remove all the log container nodes
    var childDomNodes = [];
    while ((node = this.contentDiv.firstChild)) {
      this.contentDiv.removeChild(node);
      childDomNodes.push(node);
    }

    // Put them all back in reverse order
    while ((node = childDomNodes.pop())) {
      this.contentDiv.appendChild(node);
    }
  },

  update: function() {
    if (!this.isRoot) {
      if (this.group.expandable) {
        removeClass(this.expander, "greyedout");
      } else {
        addClass(this.expander, "greyedout");
      }
    }
  },

  clear: function() {
    if (this.isRoot) {
      this.contentDiv.innerHTML = "";
    }
  }
});

/*----------------------------------------------------------------*/

function Group(name, isRoot, initiallyExpanded) {
  this.name = name;
  this.group = null;
  this.isRoot = isRoot;
  this.initiallyExpanded = initiallyExpanded;
  this.elementContainers = [];
  this.children = [];
  this.expanded = initiallyExpanded;
  this.rendered = false;
  this.expandable = false;
}

Group.prototype = new LogItem();

copyProperties(Group.prototype, {
  addChild: function(logItem) {
    this.children.push(logItem);
    logItem.group = this;
  },

  render: function() {
    if (isIe) {
      var unwrappedDomContainer, wrappedDomContainer;
      if (this.isRoot) {
        unwrappedDomContainer = logMainContainer;
        wrappedDomContainer = logMainContainer;
      } else {
        unwrappedDomContainer = this.getUnwrappedDomContainer();
        wrappedDomContainer = this.getWrappedDomContainer();
      }
      this.unwrappedElementContainer = new GroupElementContainer(this, unwrappedDomContainer, this.isRoot, false);
      this.wrappedElementContainer = new GroupElementContainer(this, wrappedDomContainer, this.isRoot, true);
      this.elementContainers = [this.unwrappedElementContainer, this.wrappedElementContainer];
    } else {
      var mainDomContainer = this.isRoot ? logMainContainer : this.getMainDomContainer();
      this.mainElementContainer = new GroupElementContainer(this, mainDomContainer, this.isRoot, false);
      this.elementContainers = [this.mainElementContainer];
    }
    this.rendered = true;
  },

  toggleExpanded: function() {
    this.expanded = !this.expanded;
    for (var i = 0, len = this.elementContainers.length; i < len; i++) {
      this.elementContainers[i].toggleExpanded();
    }
  },

  expand: function() {
    if (!this.expanded) {
      this.toggleExpanded();
    }
  },

  accept: function(visitor) {
    visitor.visitGroup(this);
  },

  reverseChildren: function() {
    if (this.rendered) {
      for (var i = 0, len = this.elementContainers.length; i < len; i++) {
        this.elementContainers[i].reverseChildren();
      }
    }
  },

  update: function() {
    var previouslyExpandable = this.expandable;
    this.expandable = (this.children.length !== 0);
    if (this.expandable !== previouslyExpandable) {
      for (var i = 0, len = this.elementContainers.length; i < len; i++) {
        this.elementContainers[i].update();
      }
    }
  },

  flatten: function() {
    var visitor = new GroupFlattener();
    this.accept(visitor);
    return visitor.logEntriesAndSeparators;
  },

  removeChild: function(child, doUpdate) {
    array_remove(this.children, child);
    child.group = null;
    if (doUpdate) {
      this.update();
    }
  },

  remove: function(doUpdate, removeFromGroup) {
    for (var i = 0, len = this.children.length; i < len; i++) {
      this.children[i].remove(false, false);
    }
    this.children = [];
    this.update();
    if (this === currentGroup) {
      currentGroup = this.group;
    }
    this.doRemove(doUpdate, removeFromGroup);
  },

  serialize: function(items) {
    items.push([LogItem.serializedItemKeys.GROUP_START, this.name]);
    for (var i = 0, len = this.children.length; i < len; i++) {
      this.children[i].serialize(items);
    }
    if (this !== currentGroup) {
      items.push([LogItem.serializedItemKeys.GROUP_END]);
    }
  },

  clear: function() {
    for (var i = 0, len = this.elementContainers.length; i < len; i++) {
      this.elementContainers[i].clear();
    }
  }
});

/*----------------------------------------------------------------*/

function LogEntryElementContainer() {
}

LogEntryElementContainer.prototype = new LogItemContainerElement();

copyProperties(LogEntryElementContainer.prototype, {
  remove: function() {
    this.doRemove();
  },

  doRemove: function() {
    this.mainDiv.parentNode.removeChild(this.mainDiv);
    this.mainDiv = null;
    this.contentElement = null;
    this.containerDomNode = null;
  },

  setContent: function(content, wrappedContent) {
    if (content === this.formattedMessage) {
      this.contentElement.innerHTML = "";
      this.contentElement.appendChild(document.createTextNode(this.formattedMessage));
    } else {
      this.contentElement.innerHTML = content;
    }
  },

  setSearchMatch: function(isMatch) {
    var oldCssClass = isMatch ? "searchnonmatch" : "searchmatch";
    var newCssClass = isMatch ? "searchmatch" : "searchnonmatch";
    replaceClass(this.mainDiv, newCssClass, oldCssClass);
  },

  clearSearch: function() {
    removeClass(this.mainDiv, "searchmatch");
    removeClass(this.mainDiv, "searchnonmatch");
  }
});

/*----------------------------------------------------------------*/

function LogEntryWrappedElementContainer(logEntry, containerDomNode) {
  this.logEntry = logEntry;
  this.containerDomNode = containerDomNode;
  this.mainDiv = document.createElement("div");
  this.mainDiv.appendChild(document.createTextNode(this.logEntry.formattedMessage));
  this.mainDiv.className = "logentry wrapped " + this.logEntry.level;
  this.contentElement = this.mainDiv;
}

LogEntryWrappedElementContainer.prototype = new LogEntryElementContainer();

LogEntryWrappedElementContainer.prototype.setContent = function(content, wrappedContent) {
  if (content === this.formattedMessage) {
    this.contentElement.innerHTML = "";
    this.contentElement.appendChild(document.createTextNode(this.formattedMessage));
  } else {
    this.contentElement.innerHTML = wrappedContent;
  }
};

/*----------------------------------------------------------------*/

function LogEntryUnwrappedElementContainer(logEntry, containerDomNode) {
  this.logEntry = logEntry;
  this.containerDomNode = containerDomNode;
  this.mainDiv = document.createElement("div");
  this.mainDiv.className = "logentry unwrapped " + this.logEntry.level;
  this.pre = this.mainDiv.appendChild(document.createElement("pre"));
  this.pre.appendChild(document.createTextNode(this.logEntry.formattedMessage));
  this.pre.className = "unwrapped";
  this.contentElement = this.pre;
}

LogEntryUnwrappedElementContainer.prototype = new LogEntryElementContainer();

LogEntryUnwrappedElementContainer.prototype.remove = function() {
  this.doRemove();
  this.pre = null;
};

/*----------------------------------------------------------------*/

function LogEntryMainElementContainer(logEntry, containerDomNode) {
  this.logEntry = logEntry;
  this.containerDomNode = containerDomNode;
  this.mainDiv = document.createElement("div");
  this.mainDiv.className = "logentry nonielogentry " + this.logEntry.level;
  this.contentElement = this.mainDiv.appendChild(document.createElement("span"));
  this.contentElement.appendChild(document.createTextNode(this.logEntry.formattedMessage));
}

LogEntryMainElementContainer.prototype = new LogEntryElementContainer();

/*----------------------------------------------------------------*/

function LogEntry(level, formattedMessage) {
  this.level = level;
  this.formattedMessage = formattedMessage;
  this.rendered = false;
}

LogEntry.prototype = new LogItem();

copyProperties(LogEntry.prototype, {
  render: function() {
    var logEntry = this;
    var containerDomNode = this.group.contentDiv;

    // Support for the CSS attribute white-space in IE for Windows is
    // non-existent pre version 6 and slightly odd in 6, so instead
    // use two different HTML elements
    if (isIe) {
      this.formattedMessage = this.formattedMessage.replace(/\r\n/g, "\r"); // Workaround for IE's treatment of white space
      this.unwrappedElementContainer = new LogEntryUnwrappedElementContainer(this, this.getUnwrappedDomContainer());
      this.wrappedElementContainer = new LogEntryWrappedElementContainer(this, this.getWrappedDomContainer());
      this.elementContainers = [this.unwrappedElementContainer, this.wrappedElementContainer];
    } else {
      this.mainElementContainer = new LogEntryMainElementContainer(this, this.getMainDomContainer());
      this.elementContainers = [this.mainElementContainer];
    }
    this.content = this.formattedMessage;
    this.rendered = true;
  },

  setContent: function(content, wrappedContent) {
    if (content != this.content) {
      if (isIe && (content !== this.formattedMessage)) {
        content = content.replace(/\r\n/g, "\r"); // Workaround for IE's treatment of white space
      }
      for (var i = 0, len = this.elementContainers.length; i < len; i++) {
        this.elementContainers[i].setContent(content, wrappedContent);
      }
      this.content = content;
    }
  },

  getSearchMatches: function() {
    var matches = [];
    var i, len;
    if (isIe) {
      var unwrappedEls = getElementsByClass(this.unwrappedElementContainer.mainDiv, "searchterm", "span");
      var wrappedEls = getElementsByClass(this.wrappedElementContainer.mainDiv, "searchterm", "span");
      for (i = 0, len = unwrappedEls.length; i < len; i++) {
        matches[i] = new Match(this.level, null, unwrappedEls[i], wrappedEls[i]);
      }
    } else {
      var els = getElementsByClass(this.mainElementContainer.mainDiv, "searchterm", "span");
      for (i = 0, len = els.length; i < len; i++) {
        matches[i] = new Match(this.level, els[i]);
      }
    }
    return matches;
  },

  setSearchMatch: function(isMatch) {
    for (var i = 0, len = this.elementContainers.length; i < len; i++) {
      this.elementContainers[i].setSearchMatch(isMatch);
    }
  },

  clearSearch: function() {
    for (var i = 0, len = this.elementContainers.length; i < len; i++) {
      this.elementContainers[i].clearSearch();
    }
  },

  accept: function(visitor) {
    visitor.visitLogEntry(this);
  },

  serialize: function(items) {
    items.push([LogItem.serializedItemKeys.LOG_ENTRY, this.level, this.formattedMessage]);
  }
});

/*----------------------------------------------------------------*/

function LogItemVisitor() {
}

LogItemVisitor.prototype = {
  visit: function(logItem) {
  },

  visitParent: function(logItem) {
    if (logItem.group) {
      logItem.group.accept(this);
    }
  },

  visitChildren: function(logItem) {
    for (var i = 0, len = logItem.children.length; i < len; i++) {
      logItem.children[i].accept(this);
    }
  },

  visitLogEntry: function(logEntry) {
    this.visit(logEntry);
  },

  visitSeparator: function(separator) {
    this.visit(separator);
  },

  visitGroup: function(group) {
    this.visit(group);
  }
};

/*----------------------------------------------------------------*/

function GroupFlattener() {
  this.logEntriesAndSeparators = [];
}

GroupFlattener.prototype = new LogItemVisitor();

GroupFlattener.prototype.visitGroup = function(group) {
  this.visitChildren(group);
};

GroupFlattener.prototype.visitLogEntry = function(logEntry) {
  this.logEntriesAndSeparators.push(logEntry);
};

GroupFlattener.prototype.visitSeparator = function(separator) {
  this.logEntriesAndSeparators.push(separator);
};

/*----------------------------------------------------------------*/

window.onload = function() {
  // Sort out document.domain
  if (location.search) {
    var queryBits = unescape(location.search).substr(1).split("&"), nameValueBits;
    for (var i = 0, len = queryBits.length; i < len; i++) {
      nameValueBits = queryBits[i].split("=");
      if (nameValueBits[0] == "log4javascript_domain") {
        document.domain = nameValueBits[1];
        break;
      }
    }
  }

  // Create DOM objects
  logMainContainer = $("log");
  if (isIePre7) {
    addClass(logMainContainer, "oldIe");
  }

  rootGroup = new Group("root", true);
  rootGroup.render();
  currentGroup = rootGroup;

  setCommandInputWidth();
  setLogContainerHeight();
  toggleLoggingEnabled();
  toggleSearchEnabled();
  toggleSearchFilter();
  toggleSearchHighlight();
  applyFilters();
  checkAllLevels();
  toggleWrap();
  toggleNewestAtTop();
  toggleScrollToLatest();
  renderQueuedLogItems();
  loaded = true;
  $("command").value = "";
  $("command").autocomplete = "off";
  $("command").onkeydown = function(evt) {
    evt = getEvent(evt);
    if (evt.keyCode == 10 || evt.keyCode == 13) { // Return/Enter
      evalCommandLine();
      stopPropagation(evt);
    } else if (evt.keyCode == 27) { // Escape
      this.value = "";
      this.focus();
    } else if (evt.keyCode == 38 && commandHistory.length > 0) { // Up
      currentCommandIndex = Math.max(0, currentCommandIndex - 1);
      this.value = commandHistory[currentCommandIndex];
      moveCaretToEnd(this);
    } else if (evt.keyCode == 40 && commandHistory.length > 0) { // Down
      currentCommandIndex = Math.min(commandHistory.length - 1, currentCommandIndex + 1);
      this.value = commandHistory[currentCommandIndex];
      moveCaretToEnd(this);
    }
  };

  // Prevent the keypress moving the caret in Firefox
  $("command").onkeypress = function(evt) {
    evt = getEvent(evt);
    if (evt.keyCode == 38 && commandHistory.length > 0 && evt.preventDefault) { // Up
      evt.preventDefault();
    }
  };

  // Prevent the keyup event blurring the input in Opera
  $("command").onkeyup = function(evt) {
    evt = getEvent(evt);
    if (evt.keyCode == 27 && evt.preventDefault) { // Up
      evt.preventDefault();
      this.focus();
    }
  };

  // Add document keyboard shortcuts
  document.onkeydown = function keyEventHandler(evt) {
    evt = getEvent(evt);
    switch (evt.keyCode) {
      case 69: // Ctrl + shift + E: re-execute last command
        if (evt.shiftKey && (evt.ctrlKey || evt.metaKey)) {
          evalLastCommand();
          cancelKeyEvent(evt);
          return false;
        }
        break;
      case 75: // Ctrl + shift + K: focus search
        if (evt.shiftKey && (evt.ctrlKey || evt.metaKey)) {
          focusSearch();
          cancelKeyEvent(evt);
          return false;
        }
        break;
      case 40: // Ctrl + shift + down arrow: focus command line
      case 76: // Ctrl + shift + L: focus command line
        if (evt.shiftKey && (evt.ctrlKey || evt.metaKey)) {
          focusCommandLine();
          cancelKeyEvent(evt);
          return false;
        }
        break;
    }
  };

  // Workaround to make sure log div starts at the correct size
  setTimeout(setLogContainerHeight, 20);

  setShowCommandLine(showCommandLine);
  doSearch();
};

window.onunload = function() {
  if (mainWindowExists()) {
    appender.unload();
  }
  appender = null;
};

/*----------------------------------------------------------------*/

function toggleLoggingEnabled() {
  setLoggingEnabled($("enableLogging").checked);
}

function setLoggingEnabled(enable) {
  loggingEnabled = enable;
}

var appender = null;

function setAppender(appenderParam) {
  appender = appenderParam;
}

function setShowCloseButton(showCloseButton) {
  $("closeButton").style.display = showCloseButton ? "inline" : "none";
}

function setShowHideButton(showHideButton) {
  $("hideButton").style.display = showHideButton ? "inline" : "none";
}

var newestAtTop = false;

/*----------------------------------------------------------------*/

function LogItemContentReverser() {
}

LogItemContentReverser.prototype = new LogItemVisitor();

LogItemContentReverser.prototype.visitGroup = function(group) {
  group.reverseChildren();
  this.visitChildren(group);
};

/*----------------------------------------------------------------*/

function setNewestAtTop(isNewestAtTop) {
  var oldNewestAtTop = newestAtTop;
  var i, iLen, j, jLen;
  newestAtTop = Boolean(isNewestAtTop);
  if (oldNewestAtTop != newestAtTop) {
    var visitor = new LogItemContentReverser();
    rootGroup.accept(visitor);

    // Reassemble the matches array
    if (currentSearch) {
      var currentMatch = currentSearch.matches[currentMatchIndex];
      var matchIndex = 0;
      var matches = [];
      var actOnLogEntry = function(logEntry) {
        var logEntryMatches = logEntry.getSearchMatches();
        for (j = 0, jLen = logEntryMatches.length; j < jLen; j++) {
          matches[matchIndex] = logEntryMatches[j];
          if (currentMatch && logEntryMatches[j].equals(currentMatch)) {
            currentMatchIndex = matchIndex;
          }
          matchIndex++;
        }
      };
      if (newestAtTop) {
        for (i = logEntries.length - 1; i >= 0; i--) {
          actOnLogEntry(logEntries[i]);
        }
      } else {
        for (i = 0, iLen = logEntries.length; i < iLen; i++) {
          actOnLogEntry(logEntries[i]);
        }
      }
      currentSearch.matches = matches;
      if (currentMatch) {
        currentMatch.setCurrent();
      }
    } else if (scrollToLatest) {
      doScrollToLatest();
    }
  }
  $("newestAtTop").checked = isNewestAtTop;
}

function toggleNewestAtTop() {
  var isNewestAtTop = $("newestAtTop").checked;
  setNewestAtTop(isNewestAtTop);
}

var scrollToLatest = true;

function setScrollToLatest(isScrollToLatest) {
  scrollToLatest = isScrollToLatest;
  if (scrollToLatest) {
    doScrollToLatest();
  }
  $("scrollToLatest").checked = isScrollToLatest;
}

function toggleScrollToLatest() {
  var isScrollToLatest = $("scrollToLatest").checked;
  setScrollToLatest(isScrollToLatest);
}

function doScrollToLatest() {
  var l = logMainContainer;
  if (typeof l.scrollTop != "undefined") {
    if (newestAtTop) {
      l.scrollTop = 0;
    } else {
      var latestLogEntry = l.lastChild;
      if (latestLogEntry) {
        l.scrollTop = l.scrollHeight;
      }
    }
  }
}

var closeIfOpenerCloses = true;

function setCloseIfOpenerCloses(isCloseIfOpenerCloses) {
  closeIfOpenerCloses = isCloseIfOpenerCloses;
}

var maxMessages = null;

function setMaxMessages(max) {
  maxMessages = max;
  pruneLogEntries();
}

var showCommandLine = false;

function setShowCommandLine(isShowCommandLine) {
  showCommandLine = isShowCommandLine;
  if (loaded) {
    $("commandLine").style.display = showCommandLine ? "block" : "none";
    setCommandInputWidth();
    setLogContainerHeight();
  }
}

function focusCommandLine() {
  if (loaded) {
    $("command").focus();
  }
}

function focusSearch() {
  if (loaded) {
    $("searchBox").focus();
  }
}

function getLogItems() {
  var items = [];
  for (var i = 0, len = logItems.length; i < len; i++) {
    logItems[i].serialize(items);
  }
  return items;
}

function setLogItems(items) {
  var loggingReallyEnabled = loggingEnabled;
  // Temporarily turn logging on
  loggingEnabled = true;
  for (var i = 0, len = items.length; i < len; i++) {
    switch (items[i][0]) {
      case LogItem.serializedItemKeys.LOG_ENTRY:
        log(items[i][1], items[i][2]);
        break;
      case LogItem.serializedItemKeys.GROUP_START:
        group(items[i][1]);
        break;
      case LogItem.serializedItemKeys.GROUP_END:
        groupEnd();
        break;
    }
  }
  loggingEnabled = loggingReallyEnabled;
}

function log(logLevel, formattedMessage) {
  if (loggingEnabled) {
    var logEntry = new LogEntry(logLevel, formattedMessage);
    logEntries.push(logEntry);
    logEntriesAndSeparators.push(logEntry);
    logItems.push(logEntry);
    currentGroup.addChild(logEntry);
    if (loaded) {
      if (logQueuedEventsTimer !== null) {
        clearTimeout(logQueuedEventsTimer);
      }
      logQueuedEventsTimer = setTimeout(renderQueuedLogItems, renderDelay);
      unrenderedLogItemsExist = true;
    }
  }
}

function renderQueuedLogItems() {
  logQueuedEventsTimer = null;
  var pruned = pruneLogEntries();

  // Render any unrendered log entries and apply the current search to them
  var initiallyHasMatches = currentSearch ? currentSearch.hasMatches() : false;
  for (var i = 0, len = logItems.length; i < len; i++) {
    if (!logItems[i].rendered) {
      logItems[i].render();
      logItems[i].appendToLog();
      if (currentSearch && (logItems[i] instanceof LogEntry)) {
        currentSearch.applyTo(logItems[i]);
      }
    }
  }
  if (currentSearch) {
    if (pruned) {
      if (currentSearch.hasVisibleMatches()) {
        if (currentMatchIndex === null) {
          setCurrentMatchIndex(0);
        }
        displayMatches();
      } else {
        displayNoMatches();
      }
    } else if (!initiallyHasMatches && currentSearch.hasVisibleMatches()) {
      setCurrentMatchIndex(0);
      displayMatches();
    }
  }
  if (scrollToLatest) {
    doScrollToLatest();
  }
  unrenderedLogItemsExist = false;
}

function pruneLogEntries() {
  if ((maxMessages !== null) && (logEntriesAndSeparators.length > maxMessages)) {
    var numberToDelete = logEntriesAndSeparators.length - maxMessages;
    var prunedLogEntries = logEntriesAndSeparators.slice(0, numberToDelete);
    if (currentSearch) {
      currentSearch.removeMatches(prunedLogEntries);
    }
    var group;
    for (var i = 0; i < numberToDelete; i++) {
      group = logEntriesAndSeparators[i].group;
      array_remove(logItems, logEntriesAndSeparators[i]);
      array_remove(logEntries, logEntriesAndSeparators[i]);
      logEntriesAndSeparators[i].remove(true, true);
      if (group.children.length === 0 && group !== currentGroup && group !== rootGroup) {
        array_remove(logItems, group);
        group.remove(true, true);
      }
    }
    logEntriesAndSeparators = array_removeFromStart(logEntriesAndSeparators, numberToDelete);
    return true;
  }
  return false;
}

function group(name, startExpanded) {
  if (loggingEnabled) {
    initiallyExpanded = (typeof startExpanded === "undefined") ? true : Boolean(startExpanded);
    var newGroup = new Group(name, false, initiallyExpanded);
    currentGroup.addChild(newGroup);
    currentGroup = newGroup;
    logItems.push(newGroup);
    if (loaded) {
      if (logQueuedEventsTimer !== null) {
        clearTimeout(logQueuedEventsTimer);
      }
      logQueuedEventsTimer = setTimeout(renderQueuedLogItems, renderDelay);
      unrenderedLogItemsExist = true;
    }
  }
}

function groupEnd() {
  currentGroup = (currentGroup === rootGroup) ? rootGroup : currentGroup.group;
}

function mainPageReloaded() {
  currentGroup = rootGroup;
  var separator = new Separator();
  logEntriesAndSeparators.push(separator);
  logItems.push(separator);
  currentGroup.addChild(separator);
}

function closeWindow() {
  if (appender && mainWindowExists()) {
    appender.close(true);
  } else {
    window.close();
  }
}

function hide() {
  if (appender && mainWindowExists()) {
    appender.hide();
  }
}

var mainWindow = window;
var windowId = "log4javascriptConsoleWindow_" + new Date().getTime() + "_" + ("" + Math.random()).substr(2);

function setMainWindow(win) {
  mainWindow = win;
  mainWindow[windowId] = window;
  // If this is a pop-up, poll the opener to see if it's closed
  if (opener && closeIfOpenerCloses) {
    pollOpener();
  }
}

function pollOpener() {
  if (closeIfOpenerCloses) {
    if (mainWindowExists()) {
      setTimeout(pollOpener, 500);
    } else {
      closeWindow();
    }
  }
}

function mainWindowExists() {
  try {
    return (mainWindow && !mainWindow.closed &&
      mainWindow[windowId] == window);
  } catch (ex) {}
  return false;
}

var logLevels = ["TRACE", "DEBUG", "INFO", "WARN", "ERROR", "FATAL"];

function getCheckBox(logLevel) {
  return $("switch_" + logLevel);
}

function getIeWrappedLogContainer() {
  return $("log_wrapped");
}

function getIeUnwrappedLogContainer() {
  return $("log_unwrapped");
}

function applyFilters() {
  for (var i = 0; i < logLevels.length; i++) {
    if (getCheckBox(logLevels[i]).checked) {
      addClass(logMainContainer, logLevels[i]);
    } else {
      removeClass(logMainContainer, logLevels[i]);
    }
  }
  updateSearchFromFilters();
}

function toggleAllLevels() {
  var turnOn = $("switch_ALL").checked;
  for (var i = 0; i < logLevels.length; i++) {
    getCheckBox(logLevels[i]).checked = turnOn;
    if (turnOn) {
      addClass(logMainContainer, logLevels[i]);
    } else {
      removeClass(logMainContainer, logLevels[i]);
    }
  }
}

function checkAllLevels() {
  for (var i = 0; i < logLevels.length; i++) {
    if (!getCheckBox(logLevels[i]).checked) {
      getCheckBox("ALL").checked = false;
      return;
    }
  }
  getCheckBox("ALL").checked = true;
}

function clearLog() {
  rootGroup.clear();
  currentGroup = rootGroup;
  logEntries = [];
  logItems = [];
  logEntriesAndSeparators = [];
  doSearch();
}

function toggleWrap() {
  var enable = $("wrap").checked;
  if (enable) {
    addClass(logMainContainer, "wrap");
  } else {
    removeClass(logMainContainer, "wrap");
  }
  refreshCurrentMatch();
}

/* ------------------------------------------------------------------- */

// Search

var searchTimer = null;

function scheduleSearch() {
  try {
    clearTimeout(searchTimer);
  } catch (ex) {
    // Do nothing
  }
  searchTimer = setTimeout(doSearch, 500);
}

function Search(searchTerm, isRegex, searchRegex, isCaseSensitive) {
  this.searchTerm = searchTerm;
  this.isRegex = isRegex;
  this.searchRegex = searchRegex;
  this.isCaseSensitive = isCaseSensitive;
  this.matches = [];
}

Search.prototype = {
  hasMatches: function() {
    return this.matches.length > 0;
  },

  hasVisibleMatches: function() {
    if (this.hasMatches()) {
      for (var i = 0; i < this.matches.length; i++) {
        if (this.matches[i].isVisible()) {
          return true;
        }
      }
    }
    return false;
  },

  match: function(logEntry) {
    var entryText = String(logEntry.formattedMessage);
    var matchesSearch = false;
    if (this.isRegex) {
      matchesSearch = this.searchRegex.test(entryText);
    } else if (this.isCaseSensitive) {
      matchesSearch = (entryText.indexOf(this.searchTerm) > -1);
    } else {
      matchesSearch = (entryText.toLowerCase().indexOf(this.searchTerm.toLowerCase()) > -1);
    }
    return matchesSearch;
  },

  getNextVisibleMatchIndex: function() {
    for (var i = currentMatchIndex + 1; i < this.matches.length; i++) {
      if (this.matches[i].isVisible()) {
        return i;
      }
    }
    // Start again from the first match
    for (i = 0; i <= currentMatchIndex; i++) {
      if (this.matches[i].isVisible()) {
        return i;
      }
    }
    return -1;
  },

  getPreviousVisibleMatchIndex: function() {
    for (var i = currentMatchIndex - 1; i >= 0; i--) {
      if (this.matches[i].isVisible()) {
        return i;
      }
    }
    // Start again from the last match
    for (var i = this.matches.length - 1; i >= currentMatchIndex; i--) {
      if (this.matches[i].isVisible()) {
        return i;
      }
    }
    return -1;
  },

  applyTo: function(logEntry) {
    var doesMatch = this.match(logEntry);
    if (doesMatch) {
      logEntry.group.expand();
      logEntry.setSearchMatch(true);
      var logEntryContent;
      var wrappedLogEntryContent;
      var searchTermReplacementStartTag = "<span class=\"searchterm\">";
      var searchTermReplacementEndTag = "<" + "/span>";
      var preTagName = isIe ? "pre" : "span";
      var preStartTag = "<" + preTagName + " class=\"pre\">";
      var preEndTag = "<" + "/" + preTagName + ">";
      var startIndex = 0;
      var searchIndex, matchedText, textBeforeMatch;
      if (this.isRegex) {
        var flags = this.isCaseSensitive ? "g" : "gi";
        var capturingRegex = new RegExp("(" + this.searchRegex.source + ")", flags);

        // Replace the search term with temporary tokens for the start and end tags
        var rnd = ("" + Math.random()).substr(2);
        var startToken = "%%s" + rnd + "%%";
        var endToken = "%%e" + rnd + "%%";
        logEntryContent = logEntry.formattedMessage.replace(capturingRegex, startToken + "$1" + endToken);

        // Escape the HTML to get rid of angle brackets
        logEntryContent = escapeHtml(logEntryContent);

        // Substitute the proper HTML back in for the search match
        var result;
        var searchString = logEntryContent;
        logEntryContent = "";
        wrappedLogEntryContent = "";
        while ((searchIndex = searchString.indexOf(startToken, startIndex)) > -1) {
          var endTokenIndex = searchString.indexOf(endToken, searchIndex);
          matchedText = searchString.substring(searchIndex + startToken.length, endTokenIndex);
          textBeforeMatch = searchString.substring(startIndex, searchIndex);
          logEntryContent += preStartTag + textBeforeMatch + preEndTag;
          logEntryContent += searchTermReplacementStartTag + preStartTag + matchedText +
            preEndTag + searchTermReplacementEndTag;
          if (isIe) {
            wrappedLogEntryContent += textBeforeMatch + searchTermReplacementStartTag +
              matchedText + searchTermReplacementEndTag;
          }
          startIndex = endTokenIndex + endToken.length;
        }
        logEntryContent += preStartTag + searchString.substr(startIndex) + preEndTag;
        if (isIe) {
          wrappedLogEntryContent += searchString.substr(startIndex);
        }
      } else {
        logEntryContent = "";
        wrappedLogEntryContent = "";
        var searchTermReplacementLength = searchTermReplacementStartTag.length +
          this.searchTerm.length + searchTermReplacementEndTag.length;
        var searchTermLength = this.searchTerm.length;
        var searchTermLowerCase = this.searchTerm.toLowerCase();
        var logTextLowerCase = logEntry.formattedMessage.toLowerCase();
        while ((searchIndex = logTextLowerCase.indexOf(searchTermLowerCase, startIndex)) > -1) {
          matchedText = escapeHtml(logEntry.formattedMessage.substr(searchIndex, this.searchTerm.length));
          textBeforeMatch = escapeHtml(logEntry.formattedMessage.substring(startIndex, searchIndex));
          var searchTermReplacement = searchTermReplacementStartTag +
            preStartTag + matchedText + preEndTag + searchTermReplacementEndTag;
          logEntryContent += preStartTag + textBeforeMatch + preEndTag + searchTermReplacement;
          if (isIe) {
            wrappedLogEntryContent += textBeforeMatch + searchTermReplacementStartTag +
              matchedText + searchTermReplacementEndTag;
          }
          startIndex = searchIndex + searchTermLength;
        }
        var textAfterLastMatch = escapeHtml(logEntry.formattedMessage.substr(startIndex));
        logEntryContent += preStartTag + textAfterLastMatch + preEndTag;
        if (isIe) {
          wrappedLogEntryContent += textAfterLastMatch;
        }
      }
      logEntry.setContent(logEntryContent, wrappedLogEntryContent);
      var logEntryMatches = logEntry.getSearchMatches();
      this.matches = this.matches.concat(logEntryMatches);
    } else {
      logEntry.setSearchMatch(false);
      logEntry.setContent(logEntry.formattedMessage, logEntry.formattedMessage);
    }
    return doesMatch;
  },

  removeMatches: function(logEntries) {
    var matchesToRemoveCount = 0;
    var currentMatchRemoved = false;
    var matchesToRemove = [];
    var i, iLen, j, jLen;

    // Establish the list of matches to be removed
    for (i = 0, iLen = this.matches.length; i < iLen; i++) {
      for (j = 0, jLen = logEntries.length; j < jLen; j++) {
        if (this.matches[i].belongsTo(logEntries[j])) {
          matchesToRemove.push(this.matches[i]);
          if (i === currentMatchIndex) {
            currentMatchRemoved = true;
          }
        }
      }
    }

    // Set the new current match index if the current match has been deleted
    // This will be the first match that appears after the first log entry being
    // deleted, if one exists; otherwise, it's the first match overall
    var newMatch = currentMatchRemoved ? null : this.matches[currentMatchIndex];
    if (currentMatchRemoved) {
      for (i = currentMatchIndex, iLen = this.matches.length; i < iLen; i++) {
        if (this.matches[i].isVisible() && !array_contains(matchesToRemove, this.matches[i])) {
          newMatch = this.matches[i];
          break;
        }
      }
    }

    // Remove the matches
    for (i = 0, iLen = matchesToRemove.length; i < iLen; i++) {
      array_remove(this.matches, matchesToRemove[i]);
      matchesToRemove[i].remove();
    }

    // Set the new match, if one exists
    if (this.hasVisibleMatches()) {
      if (newMatch === null) {
        setCurrentMatchIndex(0);
      } else {
        // Get the index of the new match
        var newMatchIndex = 0;
        for (i = 0, iLen = this.matches.length; i < iLen; i++) {
          if (newMatch === this.matches[i]) {
            newMatchIndex = i;
            break;
          }
        }
        setCurrentMatchIndex(newMatchIndex);
      }
    } else {
      currentMatchIndex = null;
      displayNoMatches();
    }
  }
};

function getPageOffsetTop(el, container) {
  var currentEl = el;
  var y = 0;
  while (currentEl && currentEl != container) {
    y += currentEl.offsetTop;
    currentEl = currentEl.offsetParent;
  }
  return y;
}

function scrollIntoView(el) {
  var logContainer = logMainContainer;
  // Check if the whole width of the element is visible and centre if not
  if (!$("wrap").checked) {
    var logContainerLeft = logContainer.scrollLeft;
    var logContainerRight = logContainerLeft  + logContainer.offsetWidth;
    var elLeft = el.offsetLeft;
    var elRight = elLeft + el.offsetWidth;
    if (elLeft < logContainerLeft || elRight > logContainerRight) {
      logContainer.scrollLeft = elLeft - (logContainer.offsetWidth - el.offsetWidth) / 2;
    }
  }
  // Check if the whole height of the element is visible and centre if not
  var logContainerTop = logContainer.scrollTop;
  var logContainerBottom = logContainerTop  + logContainer.offsetHeight;
  var elTop = getPageOffsetTop(el) - getToolBarsHeight();
  var elBottom = elTop + el.offsetHeight;
  if (elTop < logContainerTop || elBottom > logContainerBottom) {
    logContainer.scrollTop = elTop - (logContainer.offsetHeight - el.offsetHeight) / 2;
  }
}

function Match(logEntryLevel, spanInMainDiv, spanInUnwrappedPre, spanInWrappedDiv) {
  this.logEntryLevel = logEntryLevel;
  this.spanInMainDiv = spanInMainDiv;
  if (isIe) {
    this.spanInUnwrappedPre = spanInUnwrappedPre;
    this.spanInWrappedDiv = spanInWrappedDiv;
  }
  this.mainSpan = isIe ? spanInUnwrappedPre : spanInMainDiv;
}

Match.prototype = {
  equals: function(match) {
    return this.mainSpan === match.mainSpan;
  },

  setCurrent: function() {
    if (isIe) {
      addClass(this.spanInUnwrappedPre, "currentmatch");
      addClass(this.spanInWrappedDiv, "currentmatch");
      // Scroll the visible one into view
      var elementToScroll = $("wrap").checked ? this.spanInWrappedDiv : this.spanInUnwrappedPre;
      scrollIntoView(elementToScroll);
    } else {
      addClass(this.spanInMainDiv, "currentmatch");
      scrollIntoView(this.spanInMainDiv);
    }
  },

  belongsTo: function(logEntry) {
    if (isIe) {
      return isDescendant(this.spanInUnwrappedPre, logEntry.unwrappedPre);
    } else {
      return isDescendant(this.spanInMainDiv, logEntry.mainDiv);
    }
  },

  setNotCurrent: function() {
    if (isIe) {
      removeClass(this.spanInUnwrappedPre, "currentmatch");
      removeClass(this.spanInWrappedDiv, "currentmatch");
    } else {
      removeClass(this.spanInMainDiv, "currentmatch");
    }
  },

  isOrphan: function() {
    return isOrphan(this.mainSpan);
  },

  isVisible: function() {
    return getCheckBox(this.logEntryLevel).checked;
  },

  remove: function() {
    if (isIe) {
      this.spanInUnwrappedPre = null;
      this.spanInWrappedDiv = null;
    } else {
      this.spanInMainDiv = null;
    }
  }
};

var currentSearch = null;
var currentMatchIndex = null;

function doSearch() {
  var searchBox = $("searchBox");
  var searchTerm = searchBox.value;
  var isRegex = $("searchRegex").checked;
  var isCaseSensitive = $("searchCaseSensitive").checked;
  var i;

  if (searchTerm === "") {
    $("searchReset").disabled = true;
    $("searchNav").style.display = "none";
    removeClass(document.body, "searching");
    removeClass(searchBox, "hasmatches");
    removeClass(searchBox, "nomatches");
    for (i = 0; i < logEntries.length; i++) {
      logEntries[i].clearSearch();
      logEntries[i].setContent(logEntries[i].formattedMessage, logEntries[i].formattedMessage);
    }
    currentSearch = null;
    setLogContainerHeight();
  } else {
    $("searchReset").disabled = false;
    $("searchNav").style.display = "block";
    var searchRegex;
    var regexValid;
    if (isRegex) {
      try {
        searchRegex = isCaseSensitive ? new RegExp(searchTerm, "g") : new RegExp(searchTerm, "gi");
        regexValid = true;
        replaceClass(searchBox, "validregex", "invalidregex");
        searchBox.title = "Valid regex";
      } catch (ex) {
        regexValid = false;
        replaceClass(searchBox, "invalidregex", "validregex");
        searchBox.title = "Invalid regex: " + (ex.message ? ex.message : (ex.description ? ex.description : "unknown error"));
        return;
      }
    } else {
      searchBox.title = "";
      removeClass(searchBox, "validregex");
      removeClass(searchBox, "invalidregex");
    }
    addClass(document.body, "searching");
    currentSearch = new Search(searchTerm, isRegex, searchRegex, isCaseSensitive);
    for (i = 0; i < logEntries.length; i++) {
      currentSearch.applyTo(logEntries[i]);
    }
    setLogContainerHeight();

    // Highlight the first search match
    if (currentSearch.hasVisibleMatches()) {
      setCurrentMatchIndex(0);
      displayMatches();
    } else {
      displayNoMatches();
    }
  }
}

function updateSearchFromFilters() {
  if (currentSearch) {
    if (currentSearch.hasMatches()) {
      if (currentMatchIndex === null) {
        currentMatchIndex = 0;
      }
      var currentMatch = currentSearch.matches[currentMatchIndex];
      if (currentMatch.isVisible()) {
        displayMatches();
        setCurrentMatchIndex(currentMatchIndex);
      } else {
        currentMatch.setNotCurrent();
        // Find the next visible match, if one exists
        var nextVisibleMatchIndex = currentSearch.getNextVisibleMatchIndex();
        if (nextVisibleMatchIndex > -1) {
          setCurrentMatchIndex(nextVisibleMatchIndex);
          displayMatches();
        } else {
          displayNoMatches();
        }
      }
    } else {
      displayNoMatches();
    }
  }
}

function refreshCurrentMatch() {
  if (currentSearch && currentSearch.hasVisibleMatches()) {
    setCurrentMatchIndex(currentMatchIndex);
  }
}

function displayMatches() {
  replaceClass($("searchBox"), "hasmatches", "nomatches");
  $("searchBox").title = "" + currentSearch.matches.length + " matches found";
  $("searchNav").style.display = "block";
  setLogContainerHeight();
}

function displayNoMatches() {
  replaceClass($("searchBox"), "nomatches", "hasmatches");
  $("searchBox").title = "No matches found";
  $("searchNav").style.display = "none";
  setLogContainerHeight();
}

function toggleSearchEnabled(enable) {
  enable = (typeof enable == "undefined") ? !$("searchDisable").checked : enable;
  $("searchBox").disabled = !enable;
  $("searchReset").disabled = !enable;
  $("searchRegex").disabled = !enable;
  $("searchNext").disabled = !enable;
  $("searchPrevious").disabled = !enable;
  $("searchCaseSensitive").disabled = !enable;
  $("searchNav").style.display = (enable && ($("searchBox").value !== "") &&
      currentSearch && currentSearch.hasVisibleMatches()) ?
    "block" : "none";
  if (enable) {
    removeClass($("search"), "greyedout");
    addClass(document.body, "searching");
    if ($("searchHighlight").checked) {
      addClass(logMainContainer, "searchhighlight");
    } else {
      removeClass(logMainContainer, "searchhighlight");
    }
    if ($("searchFilter").checked) {
      addClass(logMainContainer, "searchfilter");
    } else {
      removeClass(logMainContainer, "searchfilter");
    }
    $("searchDisable").checked = !enable;
  } else {
    addClass($("search"), "greyedout");
    removeClass(document.body, "searching");
    removeClass(logMainContainer, "searchhighlight");
    removeClass(logMainContainer, "searchfilter");
  }
  setLogContainerHeight();
}

function toggleSearchFilter() {
  var enable = $("searchFilter").checked;
  if (enable) {
    addClass(logMainContainer, "searchfilter");
  } else {
    removeClass(logMainContainer, "searchfilter");
  }
  refreshCurrentMatch();
}

function toggleSearchHighlight() {
  var enable = $("searchHighlight").checked;
  if (enable) {
    addClass(logMainContainer, "searchhighlight");
  } else {
    removeClass(logMainContainer, "searchhighlight");
  }
}

function clearSearch() {
  $("searchBox").value = "";
  doSearch();
}

function searchNext() {
  if (currentSearch !== null && currentMatchIndex !== null) {
    currentSearch.matches[currentMatchIndex].setNotCurrent();
    var nextMatchIndex = currentSearch.getNextVisibleMatchIndex();
    if (nextMatchIndex > currentMatchIndex || confirm("Reached the end of the page. Start from the top?")) {
      setCurrentMatchIndex(nextMatchIndex);
    }
  }
}

function searchPrevious() {
  if (currentSearch !== null && currentMatchIndex !== null) {
    currentSearch.matches[currentMatchIndex].setNotCurrent();
    var previousMatchIndex = currentSearch.getPreviousVisibleMatchIndex();
    if (previousMatchIndex < currentMatchIndex || confirm("Reached the start of the page. Continue from the bottom?")) {
      setCurrentMatchIndex(previousMatchIndex);
    }
  }
}

function setCurrentMatchIndex(index) {
  currentMatchIndex = index;
  currentSearch.matches[currentMatchIndex].setCurrent();
}

/* ------------------------------------------------------------------------- */

// CSS Utilities

function addClass(el, cssClass) {
  if (!hasClass(el, cssClass)) {
    if (el.className) {
      el.className += " " + cssClass;
    } else {
      el.className = cssClass;
    }
  }
}

function hasClass(el, cssClass) {
  if (el.className) {
    var classNames = el.className.split(" ");
    return array_contains(classNames, cssClass);
  }
  return false;
}

function removeClass(el, cssClass) {
  if (hasClass(el, cssClass)) {
    // Rebuild the className property
    var existingClasses = el.className.split(" ");
    var newClasses = [];
    for (var i = 0, len = existingClasses.length; i < len; i++) {
      if (existingClasses[i] != cssClass) {
        newClasses[newClasses.length] = existingClasses[i];
      }
    }
    el.className = newClasses.join(" ");
  }
}

function replaceClass(el, newCssClass, oldCssClass) {
  removeClass(el, oldCssClass);
  addClass(el, newCssClass);
}

/* ------------------------------------------------------------------------- */

// Other utility functions

function getElementsByClass(el, cssClass, tagName) {
  var elements = el.getElementsByTagName(tagName);
  var matches = [];
  for (var i = 0, len = elements.length; i < len; i++) {
    if (hasClass(elements[i], cssClass)) {
      matches.push(elements[i]);
    }
  }
  return matches;
}

// Syntax borrowed from Prototype library
function $(id) {
  return document.getElementById(id);
}

function isDescendant(node, ancestorNode) {
  while (node != null) {
    if (node === ancestorNode) {
      return true;
    }
    node = node.parentNode;
  }
  return false;
}

function isOrphan(node) {
  var currentNode = node;
  while (currentNode) {
    if (currentNode == document.body) {
      return false;
    }
    currentNode = currentNode.parentNode;
  }
  return true;
}

function escapeHtml(str) {
  return str.replace(/&/g, "&amp;").replace(/[<]/g, "&lt;").replace(/>/g, "&gt;");
}

function getWindowWidth() {
  if (window.innerWidth) {
    return window.innerWidth;
  } else if (document.documentElement && document.documentElement.clientWidth) {
    return document.documentElement.clientWidth;
  } else if (document.body) {
    return document.body.clientWidth;
  }
  return 0;
}

function getWindowHeight() {
  if (window.innerHeight) {
    return window.innerHeight;
  } else if (document.documentElement && document.documentElement.clientHeight) {
    return document.documentElement.clientHeight;
  } else if (document.body) {
    return document.body.clientHeight;
  }
  return 0;
}

function getToolBarsHeight() {
  return $("switches").offsetHeight;
}

function getChromeHeight() {
  var height = getToolBarsHeight();
  if (showCommandLine) {
    height += $("commandLine").offsetHeight;
  }
  return height;
}

function setLogContainerHeight() {
  if (logMainContainer) {
    var windowHeight = getWindowHeight();
    $("body").style.height = getWindowHeight() + "px";
    logMainContainer.style.height = "" +
      Math.max(0, windowHeight - getChromeHeight()) + "px";
  }
}

function setCommandInputWidth() {
  if (showCommandLine) {
    $("command").style.width = "" + Math.max(0, $("commandLineContainer").offsetWidth -
      ($("evaluateButton").offsetWidth + 13)) + "px";
  }
}

window.onresize = function() {
  setCommandInputWidth();
  setLogContainerHeight();
};

if (!Array.prototype.push) {
  Array.prototype.push = function() {
    for (var i = 0, len = arguments.length; i < len; i++){
      this[this.length] = arguments[i];
    }
    return this.length;
  };
}

if (!Array.prototype.pop) {
  Array.prototype.pop = function() {
    if (this.length > 0) {
      var val = this[this.length - 1];
      this.length = this.length - 1;
      return val;
    }
  };
}

if (!Array.prototype.shift) {
  Array.prototype.shift = function() {
    if (this.length > 0) {
      var firstItem = this[0];
      for (var i = 0, len = this.length - 1; i < len; i++) {
        this[i] = this[i + 1];
      }
      this.length = this.length - 1;
      return firstItem;
    }
  };
}

if (!Array.prototype.splice) {
  Array.prototype.splice = function(startIndex, deleteCount) {
    var itemsAfterDeleted = this.slice(startIndex + deleteCount);
    var itemsDeleted = this.slice(startIndex, startIndex + deleteCount);
    this.length = startIndex;
    // Copy the arguments into a proper Array object
    var argumentsArray = [];
    for (var i = 0, len = arguments.length; i < len; i++) {
      argumentsArray[i] = arguments[i];
    }
    var itemsToAppend = (argumentsArray.length > 2) ?
      itemsAfterDeleted = argumentsArray.slice(2).concat(itemsAfterDeleted) : itemsAfterDeleted;
    for (i = 0, len = itemsToAppend.length; i < len; i++) {
      this.push(itemsToAppend[i]);
    }
    return itemsDeleted;
  };
}

function array_remove(arr, val) {
  var index = -1;
  for (var i = 0, len = arr.length; i < len; i++) {
    if (arr[i] === val) {
      index = i;
      break;
    }
  }
  if (index >= 0) {
    arr.splice(index, 1);
    return index;
  } else {
    return false;
  }
}

function array_removeFromStart(array, numberToRemove) {
  if (Array.prototype.splice) {
    array.splice(0, numberToRemove);
  } else {
    for (var i = numberToRemove, len = array.length; i < len; i++) {
      array[i - numberToRemove] = array[i];
    }
    array.length = array.length - numberToRemove;
  }
  return array;
}

function array_contains(arr, val) {
  for (var i = 0, len = arr.length; i < len; i++) {
    if (arr[i] == val) {
      return true;
    }
  }
  return false;
}

function getErrorMessage(ex) {
  if (ex.message) {
    return ex.message;
  } else if (ex.description) {
    return ex.description;
  }
  return "" + ex;
}

function moveCaretToEnd(input) {
  if (input.setSelectionRange) {
    input.focus();
    var length = input.value.length;
    input.setSelectionRange(length, length);
  } else if (input.createTextRange) {
    var range = input.createTextRange();
    range.collapse(false);
    range.select();
  }
  input.focus();
}

function stopPropagation(evt) {
  if (evt.stopPropagation) {
    evt.stopPropagation();
  } else if (typeof evt.cancelBubble != "undefined") {
    evt.cancelBubble = true;
  }
}

function getEvent(evt) {
  return evt ? evt : event;
}

function getTarget(evt) {
  return evt.target ? evt.target : evt.srcElement;
}

function getRelatedTarget(evt) {
  if (evt.relatedTarget) {
    return evt.relatedTarget;
  } else if (evt.srcElement) {
    switch(evt.type) {
      case "mouseover":
        return evt.fromElement;
      case "mouseout":
        return evt.toElement;
      default:
        return evt.srcElement;
    }
  }
}

function cancelKeyEvent(evt) {
  evt.returnValue = false;
  stopPropagation(evt);
}

function evalCommandLine() {
  var expr = $("command").value;
  evalCommand(expr);
  $("command").value = "";
}

function evalLastCommand() {
  if (lastCommand != null) {
    evalCommand(lastCommand);
  }
}

var lastCommand = null;
var commandHistory = [];
var currentCommandIndex = 0;

function evalCommand(expr) {
  if (appender) {
    appender.evalCommandAndAppend(expr);
  } else {
    var prefix = ">>> " + expr + "\r\n";
    try {
      log("INFO", prefix + eval(expr));
    } catch (ex) {
      log("ERROR", prefix + "Error: " + getErrorMessage(ex));
    }
  }
  // Update command history
  if (expr != commandHistory[commandHistory.length - 1]) {
    commandHistory.push(expr);
    // Update the appender
    if (appender) {
      appender.storeCommandHistory(commandHistory);
    }
  }
  currentCommandIndex = (expr == commandHistory[currentCommandIndex]) ? currentCommandIndex + 1 : commandHistory.length;
  lastCommand = expr;
}
