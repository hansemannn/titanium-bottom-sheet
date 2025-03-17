package ti.bottomsheet

import android.app.Activity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.core.widget.NestedScrollView
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.appcelerator.kroll.KrollDict
import org.appcelerator.kroll.KrollPropertyChange
import org.appcelerator.kroll.KrollProxy
import org.appcelerator.kroll.KrollProxyListener
import org.appcelerator.kroll.annotations.Kroll
import org.appcelerator.kroll.common.Log
import org.appcelerator.titanium.TiBaseActivity
import org.appcelerator.titanium.proxy.TiViewProxy
import org.appcelerator.titanium.util.TiConvert
import org.appcelerator.titanium.view.TiUIView

@Kroll.proxy(creatableInModule = TiBottomsheetModule::class, propertyAccessors = [
    Properties.CANCELABLE,
    Properties.CANCELED_ON_TOUCH_OUTSIDE,
    Properties.BACKGROUND_COLOR,
    Properties.DRAGGABLE
])
class DialogProxy: KrollProxy(), KrollProxyListener {
    private val TAG = "BottomSheetDialog"
    private var bottomSheetDialog: BottomSheetDialog? = null
    private var nestedScrollView: NestedScrollView? = null
    private var titaniumView: TiUIView? = null

    init {
        defaultValues[Properties.DRAGGABLE] = true
        defaultValues[Properties.CANCELABLE] = true
        defaultValues[Properties.CANCELED_ON_TOUCH_OUTSIDE] = true
        defaultValues[Properties.BACKGROUND_COLOR] = "#ffffff"

        val currentActivity = getActivity() as TiBaseActivity

        nestedScrollView = NestedScrollView(currentActivity).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundColor(Utils.getColor(this@DialogProxy.properties, "backgroundColor"))
        }

        bottomSheetDialog = BottomSheetDialog(currentActivity).apply {
            dismissWithAnimation = false

            window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            setCancelable(Utils.getBoolean(this@DialogProxy.properties, "cancelable", true))
            setCanceledOnTouchOutside(Utils.getBoolean(this@DialogProxy.properties, "canceledOnTouchOutside", true))
            setOnShowListener {
                this@DialogProxy.fireEvent("open", null)
            }
            setOnDismissListener {
                // Clear the listener when the dialog is closed by user gesture.
                unregisterLifecycleListener()

                this@DialogProxy.fireEvent("close", null)
                nestedScrollView?.removeAllViews()
                bottomSheetDialog = null
                nestedScrollView = null
                titaniumView = null
            }
        }

        setModelListener(this)
    }

    override fun propertiesChanged(krollPropertyChangeList: MutableList<KrollPropertyChange>?, krollProxy: KrollProxy?) {}
    override fun listenerAdded(p0: String?, p1: Int, p2: KrollProxy?) {}
    override fun listenerRemoved(p0: String?, p1: Int, p2: KrollProxy?) {}

    override fun processProperties(krollDict: KrollDict?) {
        if (krollDict == null || krollDict.keys.isEmpty()) {
            return
        }

        for (key in krollDict.keys) {
            handleProperty(key, krollDict[key])
        }
    }

    override fun propertyChanged(name: String?, oldValue: Any?, newValue: Any?, krollProxy: KrollProxy?) {
        if (name != null && newValue != null) {
            handleProperty(name, newValue)
        }
    }

    private fun handleProperty(key: String, value: Any?) {
        when(key) {
            Properties.VIEW -> {
                if (nestedScrollView != null && bottomSheetDialog != null) {
                    titaniumView = (value as TiViewProxy).orCreateView
                    nestedScrollView!!.addView(titaniumView!!.outerView)
                    nestedScrollView!!.requestLayout()
                    nestedScrollView!!.invalidate()
                    bottomSheetDialog!!.setContentView(nestedScrollView!!)
                }
            }
            Properties.BACKGROUND_COLOR -> nestedScrollView?.setBackgroundColor(TiConvert.toColor(value as String))
            Properties.CANCELED_ON_TOUCH_OUTSIDE -> bottomSheetDialog?.setCanceledOnTouchOutside(TiConvert.toBoolean(value))
            Properties.CANCELABLE -> bottomSheetDialog?.setCancelable(TiConvert.toBoolean(value))
            Properties.PEEK_HEIGHT -> bottomSheetDialog?.behavior?.peekHeight = TiConvert.toInt(value)
            Properties.SHEET_STATE -> bottomSheetDialog?.behavior?.state = TiConvert.toInt(value)
            Properties.DRAGGABLE -> bottomSheetDialog?.behavior?.isDraggable = TiConvert.toBoolean(value, true)
        }
    }

    override fun onDestroy(activity: Activity?) {
        hide()
        super.onDestroy(activity)
    }

    @Kroll.method
    fun show() {
        bottomSheetDialog?.let {
            if (!it.isShowing) {
                it.show()

                // Register lifecycle listener only when the dialog is shown.
                registerLifecycleListener()
            }
        }
    }

    @Kroll.method
    fun hide() {
        try {
            bottomSheetDialog?.dismiss()
        } catch (iae: IllegalArgumentException) {
            Log.d(TAG, "IllegalArgumentException error in hiding the bottom-sheet dialog : $iae")
        } catch (e: Exception) {
            Log.d(TAG, "Unknown error in hiding the bottom-sheet dialog : $e")
        }
    }

    // Receive activity lifecycle events to close the dialog, and to avoid its activity memory leak.
    private fun registerLifecycleListener() {
        // Try to unregister any previously added listener first.
        unregisterLifecycleListener()

        (getActivity() as? TiBaseActivity)?.addOnLifecycleEventListener(this)
    }

    /**
     * Clean up the lifecycle listener in following cases:
     * 1. When the dialog is closed programmatically by calling its hide() method.
     * 2. When the dialog's activity is destroyed programmatically, or forcefully by the Android OS.
     * 3. When the dialog is closed by user gesture.
     * It requires a fix in the SDK (not available until 12.6.3.GA at least) to remove an already registered listener.
     * Though without this fix, this call will silently fail and won't cause memory leaks.
     */
    private fun unregisterLifecycleListener() {
        (activity?.get() as? TiBaseActivity)?.removeOnLifecycleEventListener(this)
    }
}
