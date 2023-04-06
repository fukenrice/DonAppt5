package com.example.donappt5.views.charitylist

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import com.example.donappt5.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class SearchDialogFragment(var radius: Int) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.search_bottom_sheet, container, false)
        val rootLayout = view.findViewById<ConstraintLayout>(R.id.rootLayout)
        val rectangle = rootLayout.background as GradientDrawable
        rectangle.cornerRadius = radius.toFloat()

        // Set window flags to remove dimming effect and make dialog appear on top of activity
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.CustomBottomSheetDialog);
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        dialog?.window?.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        dialog?.window?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        return view
    }
}