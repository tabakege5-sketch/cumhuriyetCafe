package com.example.cumhuriyetcafe.view

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
import com.example.cumhuriyetcafe.adapter.kategoriAdapter
import com.example.cumhuriyetcafe.adapter.menuAdapter
import com.example.cumhuriyetcafe.dataClass.*
import com.example.cumhuriyetcafe.databinding.FragmentMenulerBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.database.FirebaseDatabase
import java.util.*

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMenulerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        aktifMasaAdi = arguments?.getString("secilenMasaId") ?: "Masa1"
        binding.textViewMasaAdi.text = "Masa: $aktifMasaAdi"

        setupRecyclerViews()
        verileriYukle()
        siparislerVeToplamTakibi()

        binding.buttonGeri.setOnClickListener { findNavController().navigateUp() }

        // SİPARİŞİ KAYDET
        binding.siparisiKaydetButton.setOnClickListener {
            db.collection("AktifSiparisler").document(aktifMasaAdi).collection("Siparisler").get().addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    FirebaseDatabase.getInstance().reference.child("Masalar").child(aktifMasaAdi).setValue(true)
                    Toast.makeText(requireContext(), "Sipariş Kaydedildi!", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                } else {
                    Toast.makeText(requireContext(), "Lütfen önce ürün ekleyin!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // ÖDEME AL (Açık kapatılmış ve görselleştirilmiş hali)
        binding.buttonOdemeYap.setOnClickListener {
            // 1. Ödeme yöntemi seçili mi?
            if (!binding.Nakit.isChecked && !binding.Kart.isChecked) {
                Toast.makeText(requireContext(), "Lütfen ödeme türü seçiniz!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 2. Masada gerçekten ürün var mı?
            db.collection("AktifSiparisler").document(aktifMasaAdi).collection("Siparisler").get().addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    Toast.makeText(requireContext(), "Ödenecek ürün bulunamadı!", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                // Masada ürün varsa temizleme işlemine başla
                val batch = db.batch()
                snapshot.forEach { batch.delete(it.reference) }

                batch.commit().addOnSuccessListener {
                    // Masayı boşalt (Yeşil yap)
                    FirebaseDatabase.getInstance().reference.child("Masalar").child(aktifMasaAdi).removeValue()

                    // Görsel "Başarı" Katmanını Göster
                    binding.basariliEkran.visibility = View.VISIBLE

                    // Arkadaki her şeyi gizle (Yeni sayfa efekti)
                    binding.topBar.visibility = View.GONE
                    binding.kontrolPaneli.visibility = View.GONE
                    binding.Menu.visibility = View.GONE
                    binding.kategoriler.visibility = View.GONE
                    binding.siparisiKaydetButton.visibility = View.GONE

                    // 2 saniye sonra Masalar ekranına dön
                    Handler(Looper.getMainLooper()).postDelayed({
                        if (_binding != null) {
                            findNavController().popBackStack()
                        }
                    }, 2000)
                }
            }
        }
    }

    private fun setupRecyclerViews() {
        katAdapter = kategoriAdapter(kategoriListesi) { secilen -> urunleriFiltrele(secilen.isim) }
        binding.kategoriler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
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
            benzersizKategoriler.forEach { kategoriListesi.add(kategori(isim = it, gorselUrl = "")) }
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
                var toplam = 0.0
                snapshot?.forEach { doc ->
                    val ad = doc.getString("urunAdi") ?: ""
                    val f = doc.getDouble("fiyat") ?: 0.0
                    adetMap[ad] = (adetMap[ad] ?: 0) + 1
                    toplam += f
                }
                binding.ToplamTutar.text = String.format(Locale.US, "TOPLAM: %.2f TL", toplam)
                mAdapter.updateAdetler(adetMap)
            }
    }

    private fun siparisEkle(u: urunler) {
        val veri = hashMapOf("urunAdi" to u.urunAdi, "fiyat" to u.fiyat, "tarih" to com.google.firebase.Timestamp.now())
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