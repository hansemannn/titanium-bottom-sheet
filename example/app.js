import TiBottomSheetDialog from 'ti.bottomsheetdialog';

var win = Ti.UI.createWindow({
    backgroundColor: '#fff'
});

var btn = Ti.UI.createButton({
    title: 'Show dialog'
});

btn.addEventListener('click', event => {
    TiBottomSheetDialog.show({
        view: Ti.UI.createView({ width: 300, height: 300, backgroundColor: 'red' })
    });
});

win.add(btn);
win.open();