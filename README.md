# Titanium Bottom Sheet / Page Sheet

Use swipeable bottom sheets (aka page sheets) in Titanium. Currently Android-only, but with strong ambitions to be cross-platform soon.

## Example

```js
import TiBottomSheet from 'ti.bottomsheet';

const bottomSheet = TiBottomSheet.createDialog({
    view: Ti.UI.createView({ backgroundColor: 'red', height: 200 }),
    cancelable: true,
    sheetState: TiBottomSheet.SHEET_STATE_EXPANDED,
    backgroundColor: 'white',
    canceledOnTouchOutside: true
});

bottomSheet.show();
```

## Author

Prashant Saini, Hans Knöchel

## License

MIT
