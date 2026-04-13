package com.example.cumhuriyetcafe.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.cumhuriyetcafe.dataClass.urunler
import com.example.cumhuriyetcafe.databinding.MenuRecyclerViewBinding
class menuAdapter(
    private var urunListesi: ArrayList<urunler>,
    private val onUrunDegisti: (urun: urunler, isArtis: Boolean) -> Unit
) : RecyclerView.Adapter<menuAdapter.MenuViewHolder>() {

    private var urunAdetleri = mutableMapOf<String, Int>()

    class MenuViewHolder(val binding: MenuRecyclerViewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val binding = MenuRecyclerViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MenuViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val currentUrun = urunListesi[position]
        val urunAdi = currentUrun.urunAdi ?: "Bilinmeyen Ürün"
        val adet = urunAdetleri[urunAdi] ?: 0
        val sharedPref = holder.itemView.context.getSharedPreferences("UygulamaAyarlari", Context.MODE_PRIVATE)
        val birim = sharedPref.getString("paraBirimi", "₺") ?: "₺"
        holder.binding.apply {
            urununIsmi.text = urunAdi
            urununFiyati.text = String.format("%.2f %s", currentUrun.fiyat, birim)
            urununAdedi.text = adet.toString()

            buttonArti.setOnClickListener { onUrunDegisti(currentUrun, true) }
            buttonEksi.setOnClickListener {
                if (adet > 0) onUrunDegisti(currentUrun, false)
            }
        }
    }

    override fun getItemCount() = urunListesi.size

    fun updateList(yeniListe: List<urunler>) {
        this.urunListesi.clear()
        this.urunListesi.addAll(yeniListe)
        notifyDataSetChanged()
    }

    fun updateAdetler(yeniAdetler: Map<String, Int>) {
        this.urunAdetleri.clear()
        this.urunAdetleri.putAll(yeniAdetler)
        notifyDataSetChanged()
    }
}