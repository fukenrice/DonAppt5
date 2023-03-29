package com.example.donappt5.views.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.donappt5.databinding.PopupCheckboxesBinding
import kotlinx.android.synthetic.main.popup_checkboxes.*
import kotlinx.android.synthetic.main.popup_checkboxes.view.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ART = "art"
private const val KIDS = "kids"
private const val POVERTY = "poverty"
private const val SCIENCE_AND_RESEARCH = "science&research"
private const val HEALTHCARE = "healthcare"
private const val EDUCATION = "education"

/**
 * A simple [Fragment] subclass.
 * Use the [TagsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TagsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private lateinit var binding: PopupCheckboxesBinding
    private val state = mutableMapOf(
        "art" to false,
        "kids" to false,
        "poverty" to false,
        "science&research" to false,
        "healthcare" to false,
        "education" to false
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            state[ART] = it.getBoolean(ART)
            state[KIDS] = it.getBoolean(KIDS)
            state[POVERTY] = it.getBoolean(POVERTY)
            state[SCIENCE_AND_RESEARCH] = it.getBoolean(SCIENCE_AND_RESEARCH)
            state[HEALTHCARE] = it.getBoolean(HEALTHCARE)
            state[EDUCATION] = it.getBoolean(EDUCATION)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = PopupCheckboxesBinding.inflate(inflater, container, false)
        val view = binding.root
        setupView(view)
        return view
    }

    private fun setupView(view: View) {
        view.apply {
            btnSetTags.setVisibility(GONE)

            kidscb.isChecked = state[KIDS] == true
            povcb.isChecked = state[POVERTY] == true
            rescb.isChecked = state[SCIENCE_AND_RESEARCH] == true
            artcb.isChecked = state[ART] == true
            helcb.isChecked = state[HEALTHCARE] == true
            educb.isChecked = state[EDUCATION] == true
        }
    }

    public fun getData(): Map<String, Boolean> = mapOf(
        KIDS to binding.kidscb.isChecked,
        POVERTY to binding.povcb.isChecked,
        SCIENCE_AND_RESEARCH to binding.rescb.isChecked,
        ART to binding.artcb.isChecked,
        HEALTHCARE to binding.helcb.isChecked,
        EDUCATION to binding.educb.isChecked
    )

    public fun addData(
        art: Boolean?,
        kids: Boolean?,
        poverty: Boolean?,
        scienceAndResearch: Boolean?,
        healthcare: Boolean?,
        education: Boolean?
    ) {
        kidscb.isChecked = kids == true
        povcb.isChecked = poverty == true
        rescb.isChecked = scienceAndResearch == true
        artcb.isChecked = art == true
        helcb.isChecked = healthcare == true
        educb.isChecked = education == true
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TagsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(
            art: Boolean = false,
            kids: Boolean = false,
            poverty: Boolean = false,
            scienceAndResearch: Boolean = false,
            healthcare: Boolean = false,
            education: Boolean = false
        ) =
            TagsFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(ART, art)
                    putBoolean(KIDS, kids)
                    putBoolean(POVERTY, poverty)
                    putBoolean(SCIENCE_AND_RESEARCH, scienceAndResearch)
                    putBoolean(HEALTHCARE, healthcare)
                    putBoolean(EDUCATION, education)
                }
            }
    }
}