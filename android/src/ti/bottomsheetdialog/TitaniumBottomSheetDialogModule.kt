package ti.bottomsheetdialog

import com.google.android.material.bottomsheet.BottomSheetDialog
import org.appcelerator.kroll.KrollDict
import org.appcelerator.kroll.KrollModule
import org.appcelerator.kroll.annotations.Kroll.*
import org.appcelerator.titanium.TiApplication
import org.appcelerator.titanium.proxy.TiViewProxy

@module(name = "TitaniumBottomSheetDialog", id = "ti.bottomsheetdialog")
class TitaniumBottomSheetDialogModule: KrollModule() {

    @method
    fun show(params: KrollDict) {
        val view = params["view"] as TiViewProxy
        val currentActivity = TiApplication.getAppCurrentActivity()
        view.activity = currentActivity

        BottomSheetDialog(currentActivity).also {
            it.setContentView(view.orCreateView.outerView, view.orCreateView.layoutParams)
            it.setCancelable(true)
            it.setCanceledOnTouchOutside(true)

            it.show()
        }
    }
}