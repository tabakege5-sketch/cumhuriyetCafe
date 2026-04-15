package com.example.cumhuriyetcafe.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cumhuriyetcafe.adapter.ciroAdapter
import com.example.cumhuriyetcafe.dataClass.ciroKayit
import com.example.cumhuriyetcafe.databinding.FragmentCiroRaporBinding
import com.google.firebase.database.*

class ciroRaporFragment : Fragment() {

    private var _binding: FragmentCiroRaporBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbRef: DatabaseReference
    private lateinit var adapter: ciroAdapter
    private var ciroListesi = ArrayList<ciroKayit>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCiroRaporBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val url = "https://cumhuriyetcafe-fb26c-default-rtdb.europe-west1.firebasedatabase.app"
        dbRef = FirebaseDatabase.getInstance(url).getReference("ciro_kayitlari")

        adapter = ciroAdapter(ciroListesi)
        binding.kalanRecyclerViewCiro.layoutManager = LinearLayoutManager(context)
        binding.kalanRecyclerViewCiro.adapter = adapter

        binding.gelirHesapla.setOnClickListener {
            verileriHesaplaVeGoster()
            if (bildirimIzniVarMi()) {
                Toast.makeText(requireContext(), "Gelirler güncellendi", Toast.LENGTH_SHORT).show()
            }
        }
        binding.ciroHesapla.setOnClickListener {
            verileriHesaplaVeGoster()
            if (bildirimIzniVarMi()) {
                Toast.makeText(requireContext(), "Ciro verileri güncellendi", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        verileriHesaplaVeGoster()
    }

    private fun bildirimIzniVarMi(): Boolean {
        val sharedPref =
            requireActivity().getSharedPreferences("UygulamaAyarlari", Context.MODE_PRIVATE)
        return sharedPref.getBoolean("bildirimIzni", true)
    }

    private fun verileriHesaplaVeGoster() {
        val sharedPref =
            requireActivity().getSharedPreferences("UygulamaAyarlari", Context.MODE_PRIVATE)
        val guncelBirim = sharedPref.getString("paraBirimi", "₺") ?: "₺"
        val kurlar = mapOf(
            "$" to 44.75,
            "€" to 52.77,
            "CH" to 57.31,
            "£" to 60.66 //İngiliz Sterlini
        )

        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (_binding == null) return

                ciroListesi.clear()
                var toplamGelirTL = 0.0
                var toplamGiderTL = 0.0

                if (snapshot.exists()) {
                    for (item in snapshot.children) {
                        val kayit = item.getValue(ciroKayit::class.java)
                        kayit?.let {
                            ciroListesi.add(it)
                            toplamGelirTL += it.gelir
                            toplamGiderTL += it.gider
                        }
                    }
                    val secilenKur = kurlar[guncelBirim] ?: 1.0
                    val hesaplanmisGelir = toplamGelirTL / secilenKur
                    val hesaplanmisGider = toplamGiderTL / secilenKur
                    val netCiro = hesaplanmisGelir - hesaplanmisGider
                    binding.textGunlukGelirHesaplamaView.text =
                        "Günlük Gelir: ${String.format("%.2f", hesaplanmisGelir)} $guncelBirim"
                    binding.textGunlukGiderHesaplamaView.text =
                        "Günlük Gider: ${String.format("%.2f", hesaplanmisGider)} $guncelBirim"
                    binding.textGunlukCiroHesaplamaView.text =
                        "Günlük Ciro: ${String.format("%.2f", netCiro)} $guncelBirim"
                    binding.textToplamCiroHesaplamaView.text =
                        "Toplam Ciro: ${String.format("%.2f", netCiro)} $guncelBirim"
                    binding.sonucTextView.text =
                        "Net Kalan: ${String.format("%.2f", netCiro)} $guncelBirim"
                    val guncelListe = ArrayList(ciroListesi.reversed())
                    adapter.verileriGuncelle(guncelListe)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                if (isAdded && bildirimIzniVarMi()) {
                    Toast.makeText(requireContext(), "Hata: ${error.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}