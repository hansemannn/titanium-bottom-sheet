
function onChange(e) {
    Ti.API.info('** picker changed');
}

function outputState(e) {
    Ti.API.info('** switch changed');
}

function updateLabel(e) {
    $.label.text = e.value.toFixed(1);
}
