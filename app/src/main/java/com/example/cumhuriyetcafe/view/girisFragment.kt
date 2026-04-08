package com.example.cumhuriyetcafe.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.cumhuriyetcafe.R
import com.example.cumhuriyetcafe.databinding.FragmentGirisBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class girisFragment : Fragment() {

    private var _binding: FragmentGirisBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)
            auth.signInWithCredential(credential).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    saveLoginStatus()
                    masalaraGit()
                } else {
                    Toast.makeText(requireContext(), "Firebase Bağlantı Hatası: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: ApiException) {
            Toast.makeText(requireContext(), "Google Hatası Kod: ${e.statusCode}", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Beklenmedik Hata: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGirisBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("50692335386-kber6ueb97j8dh8si0kltfna11r4pj2u.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        binding.girisButton.setOnClickListener {
            val email = binding.eMailText.text.toString().trim()
            val sifre = binding.editTextSifre.text.toString().trim()

            if (email.isNotEmpty() && sifre.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, sifre)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            saveLoginStatus()
                            masalaraGit()
                        } else {
                            kayitOl(email, sifre)
                        }
                    }
            } else {
                Toast.makeText(requireContext(), "Lütfen tüm alanları doldurun!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.imageView.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }
    }

    private fun kayitOl(email: String, sifre: String) {
        auth.createUserWithEmailAndPassword(email, sifre).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                saveLoginStatus()
                masalaraGit()
            } else {
                Toast.makeText(requireContext(), "Hata: ${task.exception?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    private fun saveLoginStatus() {
        val sharedPref = requireActivity().getSharedPreferences("TemaAyari", Context.MODE_PRIVATE)
        sharedPref.edit().putBoolean("isLoggedIn", true).apply()
    }

    private fun masalaraGit() {
        findNavController().navigate(R.id.action_girisFragment_to_masalarFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
//giriş yaparken çift katmanlıdır 1 email şifre
// 2 google üzerinen