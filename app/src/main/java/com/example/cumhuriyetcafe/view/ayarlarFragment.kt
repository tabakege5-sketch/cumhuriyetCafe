package com.example.cumhuriyetcafe.view

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.cumhuriyetcafe.view.MainActivity
import com.example.cumhuriyetcafe.R
import com.example.cumhuriyetcafe.databinding.FragmentAyarlarBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale

class ayarlarFragment : Fragment() {
    private var _binding: FragmentAyarlarBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAyarlarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreferences = requireActivity().getSharedPreferences("UygulamaAyarlari", Context.MODE_PRIVATE)

        binding.acKapat.isChecked = sharedPreferences.getBoolean("isDarkMode", false)
        binding.bildirimlerSwitchMaterial.isChecked = sharedPreferences.getBoolean("bildirimIzni", true)
        guncelKullanimSuresi()

        binding.ayarlarSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                val arama = newText?.lowercase() ?: ""
                binding.hareketlerBaslikLayout.visibility =
                    if (arama.isEmpty() || "hareketlerin".contains(arama) || "usage".contains(arama)) View.VISIBLE else View.GONE
                binding.dilSecimiLayout.visibility =
                    if (arama.isEmpty() || "dil".contains(arama) || "language".contains(arama) || "para".contains(arama)) View.VISIBLE else View.GONE
                val temaLayout = binding.acKapat.parent as View
                temaLayout.visibility =
                    if (arama.isEmpty() || "koyu".contains(arama) || "dark".contains(arama) || "tema".contains(arama)) View.VISIBLE else View.GONE
                return true
            }
        })

        binding.dilSecimiLayout.setOnClickListener {
            dilleriGoster()
        }

        binding.hareketlerBaslikLayout.setOnClickListener {
            if (binding.cardHareketlerDetaylari.visibility == View.GONE) {
                oturumuKaydet()
                guncelKullanimSuresi()
                binding.cardHareketlerDetaylari.visibility = View.VISIBLE
            } else {
                binding.cardHareketlerDetaylari.visibility = View.GONE
            }
        }

        binding.acKapat.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("isDarkMode", isChecked).apply()
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        binding.bildirimlerSwitchMaterial.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("bildirimIzni", isChecked).apply()
            Toast.makeText(requireContext(), if (isChecked) "Bildirimler Açık" else "Bildirimler Kapalı", Toast.LENGTH_SHORT).show()
        }

        binding.buttonCikisYap.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Çıkış")
                .setMessage("Hesabınızdan çıkış yapmak istediğinize emin misiniz?")
                .setPositiveButton("Evet") { _, _ ->
                    oturumuKaydet()
                    FirebaseAuth.getInstance().signOut()
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
                    GoogleSignIn.getClient(requireActivity(), gso).signOut()
                    val loginPref = requireActivity().getSharedPreferences("GirisBilgileri", Context.MODE_PRIVATE)
                    loginPref.edit().putBoolean("isLoggedIn", false).apply()
                    findNavController().navigate(R.id.action_ayarlarFragment_to_girisFragment)
                }
                .setNegativeButton("İptal", null)
                .show()
        }
    }
    private fun dilleriGoster() {
        val diller = arrayOf("Türkçe (₺)", "English ($)", "Germany (€)", "Switzerland (CH)")
        AlertDialog.Builder(requireContext())
            .setTitle("Dil ve Para Birimi Seçin")
            .setItems(diller) { _, hangiDil ->
                when (hangiDil) {
                    0 -> diliVeParayiDegistir("tr", "₺")
                    1 -> diliVeParayiDegistir("en", "$")
                    2 -> diliVeParayiDegistir("de", "€")
                    3 -> diliVeParayiDegistir("fr", "CH")
                }
            }.show()
    }
    private fun diliVeParayiDegistir(dilKodu: String, paraSembolu: String) {
        sharedPreferences.edit().putString("paraBirimi", paraSembolu).apply()
        sharedPreferences.edit().putString("seciliDil", dilKodu).apply()
        val locale = Locale(dilKodu)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        requireActivity().resources.updateConfiguration(config, requireActivity().resources.displayMetrics)
        requireActivity().recreate()
    }

    private fun guncelKullanimSuresi() {
        val eskiToplamMs = sharedPreferences.getLong("toplamKullanimMs", 0L)
        val buOturumMs = System.currentTimeMillis() - MainActivity.baslangicZamani
        val toplamMs = eskiToplamMs + buOturumMs
        val saat = (toplamMs / (1000 * 60 * 60)).toInt()
        val dakika = ((toplamMs / (1000 * 60)) % 60).toInt()
        binding.kullanimSuresiView.text = "Bugünkü Kullanım: $saat sa $dakika dk"
    }

    private fun oturumuKaydet() {
        val buOturumMs = System.currentTimeMillis() - MainActivity.baslangicZamani
        val eskiToplamMs = sharedPreferences.getLong("toplamKullanimMs", 0L)
        sharedPreferences.edit().putLong("toplamKullanimMs", eskiToplamMs + buOturumMs).apply()
        MainActivity.baslangicZamani = System.currentTimeMillis()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}