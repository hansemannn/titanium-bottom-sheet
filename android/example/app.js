import TiBottomSheet from 'ti.bottomsheet';

const window = Ti.UI.createWindow({
	backgroundColor: '#eee'
});

const button = Ti.UI.createButton({
	title: 'Open Sheet'
});
button.addEventListener('click', openSheet);

window.add(button);
window.open()

function openSheet() {
	const bottomSheet = TiBottomSheet.createDialog({
		view: Ti.UI.createView({ backgroundColor: 'red', height: 200 }),
		cancelable: true,
		sheetState: TiBottomSheet.SHEET_STATE_EXPANDED,
		backgroundColor: 'white',
		canceledOnTouchOutside: true
	});

	bottomSheet.addEventListener('open', () => alert('opened'));
	bottomSheet.addEventListener('close', () => alert('closed'));

	bottomSheet.show();
}
