package com.example.cumhuriyetcafe.view

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cumhuriyetcafe.R
import com.example.cumhuriyetcafe.adapter.kategoriAdapter
import com.example.cumhuriyetcafe.adapter.menuAdapter
import com.example.cumhuriyetcafe.dataClass.*
import com.example.cumhuriyetcafe.databinding.FragmentMenulerBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class menulerFragment : Fragment() {
    private var _binding: FragmentMenulerBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: FirebaseFirestore
    private var kategoriListesi = ArrayList<kategori>()
    private var tumUrunlerYedek = ArrayList<urunler>()
    private var aktifMasaAdi: String = ""
    private var siparisAdetListener: ListenerRegistration? = null
    private lateinit var katAdapter: kategoriAdapter
    private lateinit var mAdapter: menuAdapter
    private val rtDbUrl =
        "https://cumhuriyetcafe-fb26c-default-rtdb.europe-west1.firebasedatabase.app"

    private val kurlar = mapOf(
        "$" to 44.93,
        "€" to 52.79,
        "CH" to 57.59,
        "£" to 60.76
    )

    private fun bildirimIzniVarMi(): Boolean {
        val sharedPref =
            requireActivity().getSharedPreferences("UygulamaAyarlari", Context.MODE_PRIVATE)
        return sharedPref.getBoolean("bildirimIzni", true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMenulerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseFirestore.getInstance()
        aktifMasaAdi = arguments?.getString("secilenMasaId") ?: "Masa1"
        binding.textViewMasaAdi.text = aktifMasaAdi

        recyclerViewKur()
        verileriYukle()
        siparislerVeToplamTakibi()

        binding.buttonGeri.setOnClickListener { findNavController().navigateUp() }

        binding.siparisiKaydetButton.setOnClickListener {
            db.collection("AktifSiparisler").document(aktifMasaAdi).collection("Siparisler").get()
                .addOnSuccessListener { snapshot ->
                    if (!snapshot.isEmpty) {
                        FirebaseDatabase.getInstance(rtDbUrl).reference
                            .child("Masalar")
                            .child(aktifMasaAdi)
                            .setValue(true)
                            .addOnSuccessListener {
                                if (bildirimIzniVarMi()) {
                                    Toast.makeText(
                                        requireContext(),
                                        "Sipariş Kaydedildi!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                findNavController().popBackStack()
                            }
                    } else {
                        if (bildirimIzniVarMi()) {
                            Toast.makeText(
                                requireContext(),
                                "Lütfen önce ürün ekleyin!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
        }

        binding.buttonOdemeYap.setOnClickListener {
            if (!binding.Nakit.isChecked && !binding.Kart.isChecked) {
                if (bildirimIzniVarMi()) {
                    Toast.makeText(requireContext(), "Lütfen ödeme türü seç", Toast.LENGTH_SHORT)
                        .show()
                }
                return@setOnClickListener
            }

            db.collection("AktifSiparisler").document(aktifMasaAdi).collection("Siparisler").get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.isEmpty) {
                        if (bildirimIzniVarMi()) {
                            Toast.makeText(
                                requireContext(),
                                "Ödenecek ürün bulunamadı!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        return@addOnSuccessListener
                    }

                    var toplamTutarTl = 0.0
                    val urunIsimListesi = mutableListOf<String>()
                    snapshot.forEach { doc ->
                        val fiyat = doc.getDouble("fiyat") ?: 0.0
                        val ad = doc.getString("urunAdi") ?: "Bilinmeyen"
                        toplamTutarTl += fiyat
                        urunIsimListesi.add(ad)
                    }

                    val sharedPref = requireActivity().getSharedPreferences(
                        "UygulamaAyarlari",
                        Context.MODE_PRIVATE
                    )
                    val birim = sharedPref.getString("paraBirimi", "₺") ?: "₺"
                    val kur = kurlar[birim] ?: 1.0
                    val hesaplananTutar = toplamTutarTl / kur
                    val gruplanmisMap = urunIsimListesi.groupingBy { it }.eachCount()
                    val urunOzetMetni =
                        gruplanmisMap.entries.joinToString(", ") { "${it.value}x ${it.key}" }
                    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    val tarih = sdf.format(Date())
                    val odemeYontemi = if (binding.Nakit.isChecked) "Nakit" else "Kart"

                    val ciroRef =
                        FirebaseDatabase.getInstance(rtDbUrl).getReference("ciro_kayitlari").push()
                    val yeniKayit = ciroKayit(
                        firebaseId = ciroRef.key ?: "",
                        tarih = tarih,
                        gelir = toplamTutarTl,
                        gider = 0.0,
                        gunlukCiro = toplamTutarTl,
                        aciklama = "$aktifMasaAdi - $odemeYontemi | $urunOzetMetni | Tahsilat: ${
                            String.format(
                                "%.2f",
                                hesaplananTutar
                            )
                        } $birim"
                    )

                    ciroRef.setValue(yeniKayit).addOnSuccessListener {
                        val batch = db.batch()
                        snapshot.forEach { batch.delete(it.reference) }
                        batch.commit().addOnSuccessListener {
                            FirebaseDatabase.getInstance(rtDbUrl).reference
                                .child("Masalar")
                                .child(aktifMasaAdi)
                                .removeValue()

                            gizleAnaArayuz()
                            binding.basariliEkran.visibility = View.VISIBLE
                            if (bildirimIzniVarMi()) {
                                Toast.makeText(
                                    requireContext(),
                                    "Ödeme İşlemi Başarılı",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            Handler(Looper.getMainLooper()).postDelayed({
                                if (_binding != null) findNavController().popBackStack()
                            }, 2100)
                        }
                    }.addOnFailureListener {
                        if (bildirimIzniVarMi()) {
                            Toast.makeText(
                                requireContext(),
                                "Hata: ${it.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
        }
    }

    private fun gizleAnaArayuz() {
        binding.topBar.visibility = View.GONE
        binding.kontrolPaneli.visibility = View.GONE
        binding.Menu.visibility = View.GONE
        binding.kategoriler.visibility = View.GONE
        binding.siparisiKaydetButton.visibility = View.GONE
    }

    private fun recyclerViewKur() {
        katAdapter = kategoriAdapter(kategoriListesi) { secilen -> urunleriFiltrele(secilen.isim) }
        binding.kategoriler.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.kategoriler.adapter = katAdapter

        mAdapter = menuAdapter(ArrayList(), { urun, artis ->
            if (artis) siparisEkle(urun) else siparisSil(urun)
        })
        binding.Menu.layoutManager = LinearLayoutManager(requireContext())
        binding.Menu.adapter = mAdapter
    }

    private fun verileriYukle() {
        db.collection("urunler").get().addOnSuccessListener { snapshot ->
            tumUrunlerYedek.clear()
            val benzersizKategoriler = mutableSetOf<String>()
            snapshot.forEach { doc ->
                val urun = doc.toObject(urunler::class.java)
                tumUrunlerYedek.add(urun)
                urun.kategori?.let { benzersizKategoriler.add(it) }
            }
            kategoriListesi.clear()
            benzersizKategoriler.forEach {
                kategoriListesi.add(
                    kategori(
                        isim = it,
                        gorselUrl = ""
                    )
                )
            }
            katAdapter.notifyDataSetChanged()
            if (kategoriListesi.isNotEmpty()) urunleriFiltrele(kategoriListesi[0].isim)
            else mAdapter.updateList(tumUrunlerYedek)
        }
    }

    private fun urunleriFiltrele(kategoriAdi: String?) {
        if (kategoriAdi == null) return
        val trLocale = Locale("tr", "TR")
        val filtrelenmis = tumUrunlerYedek.filter {
            it.kategori?.trim()?.lowercase(trLocale) == kategoriAdi.trim().lowercase(trLocale)
        }
        mAdapter.updateList(filtrelenmis)
    }

    private fun siparislerVeToplamTakibi() {
        siparisAdetListener = db.collection("AktifSiparisler").document(aktifMasaAdi)
            .collection("Siparisler").addSnapshotListener { snapshot, _ ->
                if (_binding == null) return@addSnapshotListener

                val adetMap = mutableMapOf<String, Int>()
                var toplamTl = 0.0

                val sharedPref =
                    requireActivity().getSharedPreferences("UygulamaAyarlari", Context.MODE_PRIVATE)
                val birim = sharedPref.getString("paraBirimi", "₺") ?: "₺"
                val kur = kurlar[birim] ?: 1.0

                snapshot?.forEach { doc ->
                    val ad = doc.getString("urunAdi") ?: ""
                    val f = doc.getDouble("fiyat") ?: 0.0
                    adetMap[ad] = (adetMap[ad] ?: 0) + 1
                    toplamTl += f
                }
                val donusturulmusToplam = toplamTl / kur
                binding.ToplamTutar.text = getString(
                    R.string.toplam_tutar,
                    String.format("%.2f", donusturulmusToplam),
                    birim
                )

                mAdapter.updateAdetler(adetMap)
            }
    }

    private fun siparisEkle(u: urunler) {
        val veri = hashMapOf(
            "urunAdi" to u.urunAdi,
            "fiyat" to u.fiyat,
            "tarih" to com.google.firebase.Timestamp.now()
        )
        db.collection("AktifSiparisler").document(aktifMasaAdi).collection("Siparisler").add(veri)
    }

    private fun siparisSil(u: urunler) {
        db.collection("AktifSiparisler").document(aktifMasaAdi).collection("Siparisler")
            .whereEqualTo("urunAdi", u.urunAdi).limit(1).get().addOnSuccessListener { s ->
                if (!s.isEmpty) s.documents[0].reference.delete()
            }
    }

    override fun onDestroyView() {
        siparisAdetListener?.remove()
        _binding = null
        super.onDestroyView()
    }
}