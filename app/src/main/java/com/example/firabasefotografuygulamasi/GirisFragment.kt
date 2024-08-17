package com.example.firabasefotografuygulamasi

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.example.firabasefotografuygulamasi.databinding.FragmentGirisBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class GirisFragment : Fragment() {
    private var _binding: FragmentGirisBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase Auth
        auth = Firebase.auth


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGirisBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.girisBtn.setOnClickListener{view->giris(view)}
        binding.signupText.setOnClickListener{view->toKayitFragment(view)}

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(requireContext(),NavBarActivity::class.java,)
            startActivity(intent)
            requireActivity().finish()
        }

    }
    override fun onDestroy() {
        super.onDestroy()
    }

    private fun giris(view:View){

        val email = binding.emailEdt.text.toString()
        val password = binding.sifreEdt.text.toString()
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val intent = Intent(requireContext(),NavBarActivity::class.java,)
                    startActivity(intent)
                    requireActivity().finish()
                } else {

                }
            }.addOnFailureListener { exception ->
                Toast.makeText(requireContext(),exception.localizedMessage, Toast.LENGTH_LONG).show()
            }


    }

    private fun toKayitFragment(view: View)
    {
        val action = GirisFragmentDirections.actionGirisFragmentToKayitFragment()
        Navigation.findNavController(view).navigate(action)

    }


}