import TiBottomSheet from 'ti.bottomsheet';
let dialog;

(function () {
    $.index.open();
}());

function showDialog(e) {
    const contentView = Alloy.createController('/dialog_view_scrolling');
    contentView.closeView.addEventListener('click', closeDialog);

    dialog = TiBottomSheet.createDialog({
        view: contentView.getView(),    // mandatory
        cancelable: true,               // optional: default true
        backgroundColor: 'transparent', // optional: default `white` || for rounded-corners -> set backgroundColor to `transparent` + apply `bottomSheetDialogTheme`
        canceledOnTouchOutside: true,   // optional: default true
        sheetState: TiBottomSheet.SHEET_STATE_EXPANDED,
        // peekHeight: Ti.Platform.displayCaps.logicalDensityFactor * 100   // optional: default auto handled by library…… but pass pixels
    });
    dialog.addEventListener('open', function (e) {
        Ti.UI.createNotification({
            message: 'dialog opened',
            duration: Ti.UI.NOTIFICATION_DURATION_SHORT
        }).show();
    });
    dialog.addEventListener('close', function (e) {
        Ti.UI.createNotification({
            message: 'dialog closed',
            duration: Ti.UI.NOTIFICATION_DURATION_SHORT
        }).show();
    });
    dialog.show();
}

function closeDialog() {
    dialog.hide();
}
