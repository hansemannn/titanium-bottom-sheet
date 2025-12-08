package ti.bottomsheet

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.widget.NestedScrollView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
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
    Properties.DRAGGABLE,
    Properties.BORDER_RADIUS
])
class DialogProxy: KrollProxy(), KrollProxyListener {
    private val TAG = "BottomSheetDialog"
    private var bottomSheetDialog: BottomSheetDialog? = null
    private var nestedScrollView: NestedScrollView? = null
    private var titaniumView: TiUIView? = null
    private var dialogShown = false

    init {
        defaultValues[Properties.DRAGGABLE] = true
        defaultValues[Properties.CANCELABLE] = true
        defaultValues[Properties.CANCELED_ON_TOUCH_OUTSIDE] = true
        defaultValues[Properties.BACKGROUND_COLOR] = "#ffffff"

        val currentActivity = getActivity() as TiBaseActivity

        nestedScrollView = NestedScrollView(currentActivity).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundColor(Utils.getColor(this@DialogProxy.properties, Properties.BACKGROUND_COLOR))
        }

        bottomSheetDialog = BottomSheetDialog(currentActivity).apply {
            dismissWithAnimation = false

            window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
            setCancelable(Utils.getBoolean(this@DialogProxy.properties, Properties.CANCELABLE, true))
            setCanceledOnTouchOutside(Utils.getBoolean(this@DialogProxy.properties, Properties.CANCELED_ON_TOUCH_OUTSIDE, true))
            setOnShowListener {
                dialogShown = true
                applyCornerRadius()
                this@DialogProxy.fireEvent("open", null)
            }
            setOnDismissListener {
                // Clear the listener when the dialog is closed by user gesture.
                unregisterLifecycleListener()

                dialogShown = false
                titaniumView?.release()
                nestedScrollView?.removeAllViews()

                titaniumView = null
                nestedScrollView = null
                bottomSheetDialog = null

                this@DialogProxy.fireEvent("close", null)
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
            Properties.BACKGROUND_COLOR -> nestedScrollView?.setBackgroundColor(TiConvert.toColor(value as String, getActivity()))
            Properties.CANCELED_ON_TOUCH_OUTSIDE -> bottomSheetDialog?.setCanceledOnTouchOutside(TiConvert.toBoolean(value))
            Properties.CANCELABLE -> bottomSheetDialog?.setCancelable(TiConvert.toBoolean(value))
            Properties.PEEK_HEIGHT -> bottomSheetDialog?.behavior?.peekHeight = Utils.dpToPx(value).toInt()
            Properties.SHEET_STATE -> bottomSheetDialog?.behavior?.state = TiConvert.toInt(value)
            Properties.DRAGGABLE -> bottomSheetDialog?.behavior?.isDraggable = TiConvert.toBoolean(value, true)
            Properties.BORDER_RADIUS -> if (dialogShown) applyCornerRadius()
        }
    }

    private fun applyCornerRadius() {
        try {
            val sheet =
                bottomSheetDialog?.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
                    ?: return

            val radiusDp = Utils.getInt(properties, Properties.BORDER_RADIUS, 28)
            val radiusPx = Utils.dpToPx(radiusDp)
            val colorInt = Utils.getColor(this@DialogProxy.properties, Properties.BACKGROUND_COLOR)

            val shapeAppearanceBuilder = if (sheet.background is MaterialShapeDrawable) {
                (sheet.background as MaterialShapeDrawable).shapeAppearanceModel.toBuilder()
            } else {
                ShapeAppearanceModel.Builder()
            }

            val shapeAppearance = shapeAppearanceBuilder
                .setTopLeftCorner(CornerFamily.ROUNDED, radiusPx)
                .setTopRightCorner(CornerFamily.ROUNDED, radiusPx)
                .build()

            // Disable child view background to show radius on sheet.
            sheet.getChildAt(0)?.setBackgroundColor(Color.TRANSPARENT)

            sheet.clipToOutline = true
            sheet.background = MaterialShapeDrawable(shapeAppearance).apply {
                fillColor = ColorStateList.valueOf(colorInt)
                elevation = sheet.elevation
            }
        } catch (exc: Exception) {
            Log.d(TAG, "Could not apply borderRadius. Error: $exc")
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
