package com.example.firabasefotografuygulamasi

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.firabasefotografuygulamasi.databinding.RecyclerViewItemBinding
import com.squareup.picasso.Picasso

class PostListAdapter(val postList : ArrayList<PostModel>) :
    RecyclerView.Adapter<PostListAdapter.PostListViewHolder>() {
    class PostListViewHolder (val binding:RecyclerViewItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, ViewType: Int) : PostListViewHolder {
        val binding = RecyclerViewItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return PostListViewHolder(binding)
    }
    override fun getItemCount(): Int {
        return postList.size
    }
    override fun onBindViewHolder(holder: PostListViewHolder, position: Int) {
        Picasso.get().load(postList[position].gorsel_url).into(holder.binding.postImageView)
        holder.binding.kullaniciadiTextView.setText(postList[position].email)
        holder.binding.commentTextView.setText(postList[position].yorum)
    }

}