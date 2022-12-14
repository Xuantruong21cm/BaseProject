package com.base.common.base

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.fragment.app.FragmentManager
import androidx.viewbinding.ViewBinding
import com.base.common.R
import com.base.common.extensions.findFragment
import com.base.common.extensions.inflateViewBinding
import com.base.common.utils.listeners.SimpleBottomSheetCallbackListener
import com.base.common.utils.registerEventBusBy
import com.base.common.utils.unRegisterEventBus
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel

abstract class BaseBottomSheetDialogFragment<VB : ViewBinding> : BottomSheetDialogFragment(),
    BaseAndroidComponent<VB> {


    override var safetyScope: CoroutineScope? = null

    override lateinit var binding: VB

    override val needToSubscribeEventBus = false

    override fun getTheme() = R.style.SheetDialog

    protected open fun getDefaultDialogHeight(): Int {
        return 0
    }

    protected open fun isDraggable() = true

    override fun onStart() {
        super.onStart()
        safetyScope = CoroutineScope(Dispatchers.IO)
        registerEventBusBy(needToSubscribeEventBus)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = getViewBinding(container)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        init(savedInstanceState)
    }

    override fun VB.initListener() {}

    override fun VB.initView(savedInstanceState: Bundle?) {}

    override fun VB.setupViewModel() {}

    override fun getViewBinding(parent: ViewGroup?): VB {
        return inflateViewBinding(layoutInflater, parent)
    }

    @CallSuper
    open fun show(manager: FragmentManager) {
        val bsdf = manager.findFragment(this) ?: this
        if (bsdf is BottomSheetDialogFragment && !bsdf.isAdded) {
            super.show(manager, javaClass.simpleName)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return (super.onCreateDialog(savedInstanceState) as BottomSheetDialog).apply {
            val defaultHeight = getDefaultDialogHeight()
            this.setOnShowListener {
                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                val frameLayout =
                    this.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                        ?: return@setOnShowListener
                if (defaultHeight > 0) {
                    frameLayout.layoutParams.apply {
                        height = defaultHeight
                        frameLayout.layoutParams = this
                        frameLayout.requestLayout()
                    }
                }
                val behavior = BottomSheetBehavior.from(frameLayout)
                behavior.isDraggable = isDraggable()
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.peekHeight = defaultHeight
                behavior.isHideable = true
                behavior.addBottomSheetCallback(object : SimpleBottomSheetCallbackListener() {
                    @SuppressLint("SwitchIntDef")
                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        when (newState) {
                            BottomSheetBehavior.STATE_COLLAPSED -> behavior.state =
                                BottomSheetBehavior.STATE_EXPANDED
                            BottomSheetBehavior.STATE_DRAGGING -> if (!isDraggable()) behavior.state =
                                BottomSheetBehavior.STATE_EXPANDED
                            BottomSheetBehavior.STATE_HIDDEN -> dismiss()
                        }
                    }
                })
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        safetyScope?.cancel()
        unRegisterEventBus()
    }

}