// Note: all other forms of attaching a 'load' listener
// to either the window or the document are not reliable.

// When window is opened by main-window the popupWindow instance is always set
let url, eventData = {
  window: window
};
if (window.popupWindow) {
  eventData.popupWindow = window.popupWindow;
} else {
  url = new window.opener.scout.URL(document.location);
  eventData.formId = url.getParameter('formId');
}
window.opener.$(window.opener.document).trigger('popupWindowReady', eventData);
