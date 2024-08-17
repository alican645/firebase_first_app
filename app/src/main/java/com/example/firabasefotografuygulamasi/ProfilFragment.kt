package com.example.firabasefotografuygulamasi

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity.RESULT_CANCELED
import androidx.appcompat.app.AppCompatActivity.RESULT_OK
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.firabasefotografuygulamasi.databinding.FragmentProfilBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import java.io.IOException



class ProfilFragment : Fragment() {
    private var _binding: FragmentProfilBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseDb : FirebaseFirestore
    private lateinit var storage : FirebaseStorage

    private  lateinit var  permissionLauncher : ActivityResultLauncher<String>
    //galeriye gitmek için kullanılan yapı
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private var secilenGorselUri : Uri?=null
    private var secilenBitmap : Bitmap?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth
        firebaseDb = Firebase.firestore
        storage = Firebase.storage

        registerLauncher()
        setProfile()


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfilBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.ppImageView.setOnClickListener{
                selectImage(view)
        }


        binding.signOutEdtText.setOnClickListener{view->signOut(view)}

    }

    override fun onDestroy() {
        super.onDestroy()
    }

    // bu fonksiyon sayesinde isstenilen görsel storage'e kaydedilecek ve
    // ondan sonra firestore da ise bu storage'e kaydedilen görselin
    // url'i kaydedilecektir.
    private fun addPPUsersStorage(imageUri: Uri){
        val reference = storage.reference

        val gorselName = auth.currentUser!!.uid
        // görselin storage'e kaydedilmesini sağlayan satırlar
        val imageReference = reference.child("users_pp").child("${gorselName}.jpg")

        //uri kontorlü
        if(imageUri!=null){
            imageReference.putFile(imageUri).addOnSuccessListener {
                imageReference.getDownloadUrl().addOnSuccessListener { uriResult->
                    val url = uriResult.toString()
                    firebaseDb.collection("users")
                        .get()
                        .addOnSuccessListener { result ->
                            for (document in result) {
                                if(document.data.toMap().get("kullanici_id")== auth.currentUser!!.uid){
                                    var users = firebaseDb.collection("users").document(document.id)
                                    users.update("pp_url",url).addOnSuccessListener {  }.addOnFailureListener {  }
                                }
                            }
                        }


            }.addOnFailureListener{ exception->
                Toast.makeText(requireContext(),exception.localizedMessage,Toast.LENGTH_LONG).show()
            }
        }

    }

    }

    private fun signOut(view:View){
        auth.signOut()
        val intent = Intent(requireContext(),MainActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    private fun setProfile(){
        firebaseDb.collection("users")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    if(document.data.toMap().get("kullanici_id")== auth.currentUser!!.uid){
                        val userMap= document.data.toMap()
                        binding.pIsimTextView.setText(userMap["isim"].toString())
                        binding.pKullaniciAdiTextView.setText(userMap["kullanici_adi"].toString())
                        binding.pEmailTextView.setText(userMap["email"].toString())
                        Picasso.get().load(userMap["pp_url"].toString()).into(binding.ppImageView)

                        }
                }
            }
            .addOnFailureListener { exception ->

            }
    }



    private fun registerLauncher(){
        activityResultLauncher= registerForActivityResult(ActivityResultContracts.StartActivityForResult(),{
                result->
            if(result.resultCode== RESULT_OK){
                //kullanıcı görsel seçmiş olabilir.
                val intentFromResult =result.data
                if (intentFromResult != null) {
                    secilenGorselUri=intentFromResult?.data
                    try{
                        if (Build.VERSION.SDK_INT >= 28) {
                            val source =
                                ImageDecoder.createSource(requireActivity().contentResolver, secilenGorselUri!!)
                            secilenBitmap = ImageDecoder.decodeBitmap(source)
                            //image view etkinlikleri
                            binding.ppImageView.setImageBitmap(secilenBitmap)
                            secilenGorselUri?.let { addPPUsersStorage(it) }


                        } else {
                            secilenBitmap = MediaStore.Images.Media.getBitmap(
                                requireActivity().contentResolver,
                                secilenGorselUri
                            )
                            //image view etkinlikleri
                            binding.ppImageView.setImageBitmap(secilenBitmap)
                            secilenGorselUri?.let { addPPUsersStorage(it) }


                        }}catch (e : IOException){
                        e.printStackTrace()
                    }
                }

            }else if(result.resultCode== RESULT_CANCELED){
                // bu kısım galeriden bir şey seçilmiyorsa kullanılır
                // bu uygulamada eğer galeriden bir şey seçilmiyorsa galeri ekranından
                // direkt etkileşim ekranına döndürecek şekilde kullanılmıştır

            } })
        permissionLauncher= registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
            { result ->
                if(result){
                    //izin verildi
                    //galeriye gidebiliriz
                    val intentToGaleri= Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    activityResultLauncher.launch(intentToGaleri)
                }else{
                    //izin verilmedi
                    Toast.makeText(requireActivity(),"İzin verilmedi", Toast.LENGTH_LONG).show()
                } })
    }

    fun selectImage(view : View){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if(ContextCompat.checkSelfPermission(requireActivity(),android.Manifest.permission.READ_MEDIA_IMAGES)!= PackageManager.PERMISSION_GRANTED){
                //izin verilmemiş izin istenemsi gerekiyor
                if(ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),android.Manifest.permission.READ_MEDIA_IMAGES)){
                }else{
                    //izin isteyeceğiz
                    permissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
                }
            }else{
                //izin verilmiş , galeriye gidilebilir
                val intentToGaleri= Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGaleri)
            }
        }
        else{
            if(ContextCompat.checkSelfPermission(requireActivity(),android.Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                //izin verilmemiş izin istenemsi gerekiyor
                if(ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),android.Manifest.permission.READ_EXTERNAL_STORAGE)){
                    //snacbar göstermemiz gerekir. kullanıcıdan neden izin istediğimizi söyleyerek izin istememiz lazım
                    //ebeveyn görünümü tanıttık//           //gerekli mesajı verdik//          // snackbar uzunluğunu ekledik//
                    Snackbar.make(view,"Galeriye Ulaşıp Görsel Seçilmesi Gerekir", Snackbar.LENGTH_INDEFINITE).setAction("İzin Ver"){
                        permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    }.show()
                }else{
                    //izin isteyeceğiz
                    permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }else{
                //izin verilmiş , galeriye gidilebilir
                val intentToGaleri= Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGaleri)
            }}}
}