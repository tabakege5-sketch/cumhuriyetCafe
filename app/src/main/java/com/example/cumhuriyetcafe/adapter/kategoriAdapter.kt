package com.example.cumhuriyetcafe.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cumhuriyetcafe.dataClass.kategori
import com.example.cumhuriyetcafe.databinding.KategoriRecyclerViewBinding

class kategoriAdapter(
    private var kategoriListesi: List<kategori>,
    private val kategoriTiklandi: (kategori) -> Unit
) : RecyclerView.Adapter<kategoriAdapter.kategoriViewHolder>() {
    private var selectedPosition = 0

    class kategoriViewHolder(val binding: KategoriRecyclerViewBinding) : RecyclerView.ViewHolder(binding.root)

    fun setSelected(position: Int) {
        val previous = selectedPosition
        selectedPosition = position
        notifyItemChanged(previous)
        notifyItemChanged(selectedPosition)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): kategoriViewHolder {
        val binding = KategoriRecyclerViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return kategoriViewHolder(binding)
    }

    override fun onBindViewHolder(holder: kategoriViewHolder, position: Int) {
        val item = kategoriListesi[position]

        holder.binding.apply {
            kategoriAdiTextView.text = item.isim

            Glide.with(root.context)
                .load(item.gorselUrl)
                .into(kategoriResmi)

            root.setOnClickListener {
                val currentPosition = holder.adapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    setSelected(currentPosition)
                    kategoriTiklandi(item)
                }
            }
        }
    }

    override fun getItemCount() = kategoriListesi.size
    fun updateList(yeniListe: List<kategori>) {
        this.kategoriListesi = yeniListe
        notifyDataSetChanged()
    }
}