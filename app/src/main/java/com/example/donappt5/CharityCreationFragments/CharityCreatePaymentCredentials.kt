package com.example.donappt5.CharityCreationFragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.example.donappt5.R
import com.example.donappt5.databinding.FragmentCharityCreatePaymentCredentialsBinding
import kotlinx.android.synthetic.main.fragment_charity_create_payment_credentials.view.*


/**
 * A simple [Fragment] subclass.
 * Use the [CharityCreatePaymentCredentials.newInstance] factory method to
 * create an instance of this fragment.
 */
class CharityCreatePaymentCredentials : Fragment() {

    private var _binding: FragmentCharityCreatePaymentCredentialsBinding? = null
    private val binding get() = _binding!!
    private var qiwiEditText: EditText? = null
    private var url: String? = null

    companion object {
        const val ARG_URL = "url"


        fun newInstance(name: String = ""): CharityCreatePaymentCredentials {
            val fragment = CharityCreatePaymentCredentials()

            val bundle = Bundle().apply {
                putString(ARG_URL, name)
            }

            fragment.arguments = bundle

            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding =
            FragmentCharityCreatePaymentCredentialsBinding.inflate(inflater, container, false)
        val view = binding.root
        url = arguments?.getString(ARG_URL)
        setupView(view)
        return view
    }

    private fun setupView(view: View) {
        qiwiEditText = view.qiwiPaymentUrlEditText
        qiwiEditText?.setText(url)
        view.qiwiPaymentInfoButton.setOnClickListener {
            showInfoAlertDialog()
        }
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


}