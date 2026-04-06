package com.example.cumhuriyetcafe.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.cumhuriyetcafe.dataClass.ciroKayit
import com.example.cumhuriyetcafe.databinding.CiroRecyclerViewBinding

class ciroAdapter(private var ciroListesi: List<ciroKayit>) :
    RecyclerView.Adapter<ciroAdapter.CiroViewHolder>() {

    class CiroViewHolder(val binding: CiroRecyclerViewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CiroViewHolder {
        val binding = CiroRecyclerViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CiroViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CiroViewHolder, position: Int) {
        val veri = ciroListesi[position]
        val detayliMetin = "${veri.tarih}\n${veri.aciklama}"

        holder.binding.textViewTarih.text = detayliMetin
        holder.binding.textViewMiktar.text = "${veri.gunlukCiro} TL"
    }

    override fun getItemCount(): Int = ciroListesi.size

    fun verileriGuncelle(yeniListe: List<ciroKayit>) {
        this.ciroListesi = yeniListe
        notifyDataSetChanged()
    }
}