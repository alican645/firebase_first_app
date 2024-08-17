package com.example.firabasefotografuygulamasi

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.firabasefotografuygulamasi.databinding.ActivityMainBinding
import com.example.firabasefotografuygulamasi.databinding.ActivityNavBarBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.io.IOException

class NavBarActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNavBarBinding
    private lateinit var bottomNavigationView: BottomNavigationView

    private lateinit var storage : FirebaseStorage

    private  lateinit var  permissionLauncher : ActivityResultLauncher<String>
    //galeriye gitmek için kullanılan yapı
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private var secilenGorselUri : Uri?=null
    private var secilenBitmap : Bitmap?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNavBarBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        registerLauncher()

        bottomNavigationView=findViewById(R.id.bottomNavigationView)

        //fragmentlari sayfa içerisinde değiştiren fonksiyondur.
        setCurrFragment(ProfilFragment())
        bottomNavigationView.selectedItemId=R.id.profilNavBarItem

        bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.etkilesimNavBarItem -> {
                    setCurrFragment(EtkilesimFragment())
                }
                R.id.profilNavBarItem -> {
                    setCurrFragment(ProfilFragment())
                }
                R.id.postNavBarItem -> {
                    selectImage(view)
                }
            };true
        }

        storage = Firebase.storage

    }

    private fun setCurrFragment(fragment : Fragment){
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.frameLayout,fragment)
            commit()
        }

    }

    private fun registerLauncher(){
        activityResultLauncher= registerForActivityResult(ActivityResultContracts.StartActivityForResult(),{
                result->
            if(result.resultCode==RESULT_OK){
                //kullanıcı görsel seçmiş olabilir.
                val intentFromResult =result.data
                if (intentFromResult != null) {
                    secilenGorselUri=intentFromResult?.data
                    try{
                        if (Build.VERSION.SDK_INT >= 28) {
                            val source =
                                ImageDecoder.createSource(this.contentResolver, secilenGorselUri!!)
                            secilenBitmap = ImageDecoder.decodeBitmap(source)
                            setCurrFragment(PostFragment(bitmap = secilenBitmap!!, imageUri = secilenGorselUri!!, activityBinding = binding))

                        } else {
                            secilenBitmap = MediaStore.Images.Media.getBitmap(
                                this.contentResolver,
                                secilenGorselUri
                            )
                            setCurrFragment(PostFragment(secilenBitmap!!,imageUri = secilenGorselUri!!, activityBinding = binding))


                        }}catch (e : IOException){
                        e.printStackTrace()
                    }
                }

            }else if(result.resultCode== RESULT_CANCELED){
                // bu kısım galeriden bir şey seçilmiyorsa kullanılır
                // bu uygulamada eğer galeriden bir şey seçilmiyorsa galeri ekranından
                // direkt etkileşim ekranına döndürecek şekilde kullanılmıştır
                setCurrFragment(EtkilesimFragment())
                bottomNavigationView.selectedItemId=R.id.etkilesimNavBarItem
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
                    Toast.makeText(this,"İzin verilmedi", Toast.LENGTH_LONG).show()
                } })
    }

    fun selectImage(view : View){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_MEDIA_IMAGES)!= PackageManager.PERMISSION_GRANTED){
                //izin verilmemiş izin istenemsi gerekiyor
                if(ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.READ_MEDIA_IMAGES)){
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
            if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                //izin verilmemiş izin istenemsi gerekiyor
                if(ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.READ_EXTERNAL_STORAGE)){
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