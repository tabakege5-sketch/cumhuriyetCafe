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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
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
        binding.gelirHesapla.setOnClickListener { verileriHesaplaVeGoster() }
        binding.ciroHesapla.setOnClickListener {
            verileriHesaplaVeGoster()
            if (isAdded) Toast.makeText(requireContext(), "Veriler güncellendi", Toast.LENGTH_SHORT).show()
        }
        verileriHesaplaVeGoster()
    }

    private fun verileriHesaplaVeGoster() {
        val sharedPref = requireActivity().getSharedPreferences("UygulamaAyarlari", Context.MODE_PRIVATE)
        val birim = sharedPref.getString("paraBirimi", "₺") ?: "₺"

        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (_binding == null) return

                ciroListesi.clear()
                var toplamGelir = 0.0
                var toplamGider = 0.0

                if (snapshot.exists()) {
                    for (item in snapshot.children) {
                        val kayit = item.getValue(ciroKayit::class.java)
                        kayit?.let {
                            ciroListesi.add(it)
                            toplamGelir += it.gelir
                            toplamGider += it.gider
                        }
                    }

                    val netCiro = toplamGelir - toplamGider
                    binding.textGunlukGelirHesaplamaView.text = "Günlük Gelir: ${String.format("%.2f", toplamGelir)} $birim"
                    binding.textGunlukGiderHesaplamaView.text = "Günlük Gider: ${String.format("%.2f", toplamGider)} $birim"
                    binding.textGunlukCiroHesaplamaView.text = "Günlük Ciro: ${String.format("%.2f", netCiro)} $birim"
                    binding.textToplamCiroHesaplamaView.text = "Toplam Ciro: ${String.format("%.2f", netCiro)} $birim"
                    binding.sonucTextView.text = "Net Kalan: ${String.format("%.2f", netCiro)} $birim"
                    val guncelListe = ArrayList(ciroListesi.reversed())
                    adapter.verileriGuncelle(guncelListe)

                } else {
                    if (isAdded) Toast.makeText(requireContext(), "Henüz kayıtlı veri bulunamadı", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                if (isAdded) Toast.makeText(requireContext(), "Hata: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}