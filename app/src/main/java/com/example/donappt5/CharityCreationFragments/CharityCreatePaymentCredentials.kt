package com.example.donappt5.CharityCreationFragments

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.DialogInterface.BUTTON_POSITIVE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.example.donappt5.R
import com.example.donappt5.databinding.FragmentCharityCreatePaymentCredentialsBinding
import kotlinx.android.synthetic.main.fragment_charity_create_payment_credentials.view.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CharityCreatePaymentCredentials.newInstance] factory method to
 * create an instance of this fragment.
 */
class CharityCreatePaymentCredentials : Fragment() {

    private var _binding: FragmentCharityCreatePaymentCredentialsBinding? = null
    private val binding get() = _binding!!
    private var qiwiEditText: EditText? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding =
            FragmentCharityCreatePaymentCredentialsBinding.inflate(inflater, container, false)
        val view = binding.root
        setupView(view)
        return view
    }

    private fun setupView(view: View) {
        qiwiEditText = view.qiwiPaymentUrlEditText
        view.qiwiPaymentInfoButton.setOnClickListener {
            showInfoAlertDialog()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        qiwiEditText = requireView().findViewById<View>(R.id.qiwiPaymentUrlEditText) as EditText
    }

    private fun showInfoAlertDialog() {
        val ad = AlertDialog.Builder(requireActivity())
        ad.setTitle(resources.getString(R.string.qiwi_getting_url_hint_title))
        ad.setMessage(resources.getString(R.string.qiwi_getting_url_hint))
        ad.setPositiveButton(resources.getText(R.string.ok)) {dialog, which -> }
        ad.show()
    }

    fun getText() : String {
        return qiwiEditText?.text.toString()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CharityCreatePaymentCredentials.
         */
        @JvmStatic
        fun newInstance() =
            CharityCreatePaymentCredentials().apply {
                arguments = Bundle().apply {
//                    putString(ARG_PARAM1, param1)
//                    putString(ARG_PARAM2, param2)
                }
            }
    }
}