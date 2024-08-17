package com.example.firabasefotografuygulamasi

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.firabasefotografuygulamasi.databinding.FragmentKayitBinding
import com.google.firebase.appcheck.internal.util.Logger.TAG
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class KayitFragment : Fragment() {
    private var _binding: FragmentKayitBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseDb : FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize Firebase Auth
        auth = Firebase.auth
        firebaseDb = Firebase.firestore

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentKayitBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.kayitBtn.setOnClickListener { view->signupBtn(view) }


    }
    override fun onDestroy() {
        super.onDestroy()
    }

    private fun signupBtn(view : View){
        val email = binding.emailKayitEdt.text.toString()
        val password1 = binding.sifreKayitEdt.text.toString()
        val password2 = binding.sifreKayitEdt2.text.toString()
        val username = binding.kullaniciadiKayitEdt.text.toString()
        val name = binding.isimKayitEdt.text.toString()
        val surname = binding.soyisimKayitEdt.text.toString()

        if(password1.equals(password2)){
            //kullanıcı oluşturulduysa , oluşturulan kullanıcıyı kullanıcı veri tabanına eklenir.
            auth.createUserWithEmailAndPassword(email, password1)
                .addOnCompleteListener { task ->
                    if(task.isSuccessful){

                        val user = hashMapOf<String, Any>()
                        user.put("isim", name)
                        user.put("soyisim", surname)
                        user.put("kullanici_adi", username)
                        user.put("email", auth.currentUser?.email.toString())
                        user.put("pp_url", "profil fotoğrafı daha seçilmedi")
                        auth.currentUser?.let { user.put("kullanici_id", it.uid) }

                        firebaseDb.collection("users")
                            .add(user)
                            .addOnSuccessListener { documentReference ->
                                val intent = Intent(requireContext(),NavBarActivity::class.java,)
                                startActivity(intent)
                                requireActivity().finish()
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(
                                    requireContext(),
                                    exception.localizedMessage,
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                        //kullanıcı Oluşturuldu.


                    }
                }.addOnFailureListener { exception->
                    Toast.makeText(requireContext(),exception.localizedMessage,Toast.LENGTH_LONG).show()
                }
        }else{
            Toast.makeText(requireContext(),"Lütfen Şifrenizi Tekrar Giriniz",Toast.LENGTH_LONG).show()
        }

    }

}