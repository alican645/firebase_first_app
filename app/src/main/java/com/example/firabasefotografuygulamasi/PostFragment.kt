package com.example.firabasefotografuygulamasi

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.firabasefotografuygulamasi.databinding.ActivityNavBarBinding
import com.example.firabasefotografuygulamasi.databinding.FragmentPostBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.util.UUID


class PostFragment(val bitmap: Bitmap,val imageUri: Uri, val activityBinding: ActivityNavBarBinding) : Fragment() {
    private var _binding: FragmentPostBinding? = null
    private val binding get() = _binding!!

    private lateinit var storage : FirebaseStorage
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseDb : FirebaseFirestore





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth
        storage = Firebase.storage
        firebaseDb = Firebase.firestore

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPostBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.postImageView.setImageBitmap(bitmap)
        binding.paylasBtn.setOnClickListener { paylas(imageUri = imageUri) }

    }
    override fun onDestroy() {
        super.onDestroy()


    }

    // bu fonksiyon sayesinde isstenilen görsel storage'e kaydedilecek ve
    // ondan sonra firestore da ise bu storage'e kaydedilen görselin
    // url'i kaydedilecektir.
    private fun paylas(imageUri: Uri){
        val reference = storage.reference
        val gorselName = UUID.randomUUID()
        // görselin storage'e kaydedilmesini sağlayan satırlar
        val imageReference = reference.child("images").child("${gorselName}.jpg")

        //uri kontorlü
        if(imageUri!=null){

//          addOnSuccessListener: Bir işlemin yalnızca başarılı sonucunu işlemeniz gerektiğinde
//          ve başarısızlıkları açıkça işlemeniz gerekmediğinde.
//          addOnCompleteListener: Hem başarı hem de başarısızlık durumlarını işlemeniz gerektiğinde
//          veya sonuca bakılmaksızın eylemler gerçekleştirmeniz gerektiğinde
//          (örneğin, UI öğelerini güncelleme).

            //görseli storage'a kaydeden listener
            imageReference.putFile(imageUri).addOnSuccessListener {
                imageReference.getDownloadUrl().addOnSuccessListener { uriResult->

                    if(auth.currentUser!=null){
                        val url = uriResult.toString()
                        // görselin url'i sorunsuz bir şekilde çekildiyse çalışacak satırlar.
                        // yani burada url firestore'a kaydedilir.

                        val post = hashMapOf<String, Any>()
                        post.put("email", auth.currentUser?.email.toString())
                        post.put("gorsel_name", gorselName.toString())
                        post.put("gorsel_url", url)
                        post.put("yorum", binding.yorumEdt.text.toString())

                        firebaseDb.collection("posts")
                            .add(post)
                            .addOnSuccessListener { documentReference ->
                                //post ekleme işlemi başarı ile tamamlandığında çalışacak fonksiyon blokları
                                fragmentToFragment(EtkilesimFragment())
                                activityBinding.bottomNavigationView.selectedItemId =
                                    R.id.etkilesimNavBarItem
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(
                                    requireContext(),
                                    exception.localizedMessage,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }

                }.addOnFailureListener { exception->
                    Toast.makeText(requireContext(),exception.localizedMessage,Toast.LENGTH_LONG).show()
                }

                fragmentToFragment(EtkilesimFragment())
                activityBinding.bottomNavigationView.selectedItemId=R.id.etkilesimNavBarItem
            }.addOnFailureListener{ exception->
                Toast.makeText(requireContext(),exception.localizedMessage,Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun fragmentToFragment(fragment: Fragment){
        val fragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameLayout, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

}