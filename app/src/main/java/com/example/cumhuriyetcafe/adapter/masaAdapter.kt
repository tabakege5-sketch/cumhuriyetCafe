package com.example.cumhuriyetcafe.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.cumhuriyetcafe.R
import com.example.cumhuriyetcafe.dataClass.masa

class masaAdapter(
    private var masaListesi: List<masa>,
    private val onClick: (masa) -> Unit
) : RecyclerView.Adapter<masaAdapter.MasaViewHolder>() {
    class MasaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val masaAdi: TextView = view.findViewById(R.id.masaAdi)
        val cardView: CardView = view.findViewById(R.id.cardViewMasa)
    }
    fun updateData(yeniListe: List<masa>) {
        this.masaListesi = yeniListe
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MasaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_masa_recycler_view, parent, false)
        return MasaViewHolder(view)
    }
    override fun onBindViewHolder(holder: MasaViewHolder, position: Int) {
        val suAnkiMasa = masaListesi[position]
        holder.masaAdi.text = suAnkiMasa.isim
        val context = holder.itemView.context
        val isDolu = suAnkiMasa.durum == true || suAnkiMasa.durum.toString() == "true"

        val renkResId = if (isDolu) {
            R.color.masa_dolu_kırmızı
        } else {
            R.color.masada_musteri_yok
        }
        holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, renkResId))
        holder.itemView.setOnClickListener { onClick(suAnkiMasa) }
    }
    override fun getItemCount() = masaListesi.size
}