/* global $ */
/* global jQuery */
/* global GOVUK */
/* Taken from https://github.com/alphagov/govuk_elements/blob/master/assets/javascripts/application.js */

$(document).ready(function () {

    $('#back-link[href="#"]').on('click', function(e){
        e.preventDefault();
        window.history.back();
    });
    // Turn off jQuery animation
    jQuery.fx.off = true;
    //Add keypress event on every link where exist role="button"
    $(document).on('keypress', 'a[role="button"]', null, onKeypressButton, false);

    $('input[type=submit], button[type=submit]').data('ignore-double-submit', 'true');
})

$(window).on('load', function () {
    // If there is an error summary, set focus to the summary
    if ($('.error-summary').length) {
        $('.error-summary').focus();
    }
})

function initAutocompleteSelects() {
  // Find all <select> elements using our autocomplete module
  document.querySelectorAll("select[data-module='hmrc-accessible-autocomplete']").forEach(select => {
    const name = select.name;
    const inputId = name;                      // input id will match the field name
    const selectId = `${name}-select`;         // hidden select id
    const label = document.querySelector(`label[for='${select.id || selectId}']`);
    if (label) label.setAttribute("for", inputId); // attach label to input
    select.id = selectId;
    // Build options array including synonyms for search
    const options = Array.from(select.options).map(o => ({
      value: o.value,
      label: o.text,
      searchText: ((o.dataset.search || "") + " " + o.text).toLowerCase()
    }));
    // Helper: find exact match by label or synonyms
    const findMatch = text => options.find(o =>
      o.label.toLowerCase() === text.toLowerCase().trim() ||
      o.searchText.split(/\s+/).some(s => s === text.toLowerCase().trim())
    );
    select.style.display = "none";           // hide the original <select>
    // Create container for autocomplete input
    const container = document.createElement("div");
    select.parentNode.insertBefore(container, select);
    // Remove any previously rendered autocomplete elements
    select.parentElement.querySelectorAll(".hmrc-accessible-autocomplete, .autocomplete__wrapper")
      .forEach(el => { if (!container.contains(el)) el.remove(); });
    // Initialize HMRC Accessible Autocomplete
    window.HMRCAccessibleAutocomplete({
      element: container,
      id: inputId,                           // input id
      name: "",                              // no name to prevent duplicate submission
      defaultValue: options.find(o => o.value === select.value)?.label || "",
      showAllValues: false,
      autoselect: false,
      source: (query, populate) =>
        populate(options.filter(o => o.searchText.includes((query || "").toLowerCase())).map(o => o.label)),
      onConfirm: label => {
        const match = label && findMatch(label);
        select.value = match ? match.value : "";  // update hidden select
        const input = document.getElementById(inputId);
        if (match && input) input.value = match.label; // show canonical label
      }
    });
    // Ensure autocomplete input has no name to avoid duplicate submission
    const input = document.getElementById(inputId);
    if (input) input.removeAttribute("name");
    // On form submit: enforce exact match, clear if invalid
    const form = select.closest("form");
    if (form) form.addEventListener("submit", () =>
      select.value = findMatch(input?.value || "")?.value || ""
    );
  });
}


function onKeypressButton(e) {
    // Need both, 'keyCode' and 'which' to work in all browsers.
    var code = e.keyCode || e.which,
        spaceKey = 32;
    //If user press space key:
    if (code == spaceKey) {
        // Do same thing as onclick:
        $(e.currentTarget)[0].click();
        e.preventDefault();
        e.stopImmediatePropagation();
    }
}


/**
 * detect IE
 * returns version of IE or false, if browser is not Internet Explorer
 * https://codepen.io/gapcode/pen/vEJNZN
 */
function detectIE() {
    var ua = window.navigator.userAgent;

    var trident = ua.indexOf('Trident/');
    if (trident > 0) {
        // IE 11 => return version number
        var rv = ua.indexOf('rv:');
        return parseInt(ua.substring(rv + 3, ua.indexOf('.', rv)), 10);
    }

    // other browser
    return false;
}