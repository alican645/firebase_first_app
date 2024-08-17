package com.example.firabasefotografuygulamasi

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firabasefotografuygulamasi.databinding.FragmentEtkilesimBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage


class EtkilesimFragment : Fragment() {
    private var _binding: FragmentEtkilesimBinding? = null
    private val binding get() = _binding!!
    private lateinit var postList: ArrayList<PostModel>


    private lateinit var storage : FirebaseStorage
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseDb : FirebaseFirestore

    private var adapter : PostListAdapter ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        firebaseDb = Firebase.firestore
        postList = arrayListOf()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEtkilesimBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getDataFromFireStore()

        adapter=PostListAdapter(postList)

        binding.etkilesimReVi.layoutManager=LinearLayoutManager(requireActivity())
        binding.etkilesimReVi.adapter=adapter

    }
    override fun onDestroy() {
        super.onDestroy()

    }

    private fun getDataFromFireStore(){
        firebaseDb.collection("posts").addSnapshotListener{ value,error ->
            if(error!=null){
                //Toast.makeText(requireContext(),"", Toast.LENGTH_LONG).show()
            }else{
                if(value!=null && !value.isEmpty){
                    val documents = value.documents
                    postList.clear()
                    for(document in documents){
                        val gorsel_url= document.get("gorsel_url") as String
                        val yorum = document.get("yorum") as String
                        val email = document.get("email") as String
                        postList.add(PostModel(
                            gorsel_url = gorsel_url,
                            yorum = yorum,
                            email = email))
                    }
                    //her yeni veri geldikçe adapter baştan oluşturulur ve etkileşim ekreni yenilenmiş olur.
                    adapter?.notifyDataSetChanged()

                }
            }
        }
    }

}