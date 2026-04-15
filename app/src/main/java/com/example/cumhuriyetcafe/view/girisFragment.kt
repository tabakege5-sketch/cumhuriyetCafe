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
import coil.load
import coil.transform.CircleCropTransformation
import com.example.cumhuriyetcafe.R
import com.example.cumhuriyetcafe.databinding.FragmentGirisBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
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
                    val user = auth.currentUser
                    user?.let { guncelProfilFotografi(it) }
                    girisiKaydet()
                    masalaraGit()
                }
            }
        } catch (e: Exception) {
            if (isAdded) Toast.makeText(requireContext(), "Google Giriş Hatası", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGirisBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        val sharedPref = requireActivity().getSharedPreferences("TemaAyari", Context.MODE_PRIVATE)
        if (auth.currentUser != null && sharedPref.getBoolean("isLoggedIn", false)) {
            masalaraGit()
            return
        }

        auth.currentUser?.let { guncelProfilFotografi(it) } ?: googleSimgesiYap()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("50692335386-kber6ueb97j8dh8si0kltfna11r4pj2u.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        binding.girisLinearLayout.setOnClickListener {
            googleSignInLauncher.launch(googleSignInClient.signInIntent)
        }

        binding.girisButton.setOnClickListener {
            val email = binding.eMailText.text.toString().trim()
            val sifre = binding.editTextSifre.text.toString().trim()
            if (email.isNotEmpty() && sifre.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, sifre).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        girisiKaydet()
                        masalaraGit()
                    }
                }
            }
        }
    }

    private fun guncelProfilFotografi(user: FirebaseUser) {
        val photoUrl = user.photoUrl
        if (photoUrl != null) {
            binding.imageView.load(photoUrl) {
                crossfade(true)
                transformations(CircleCropTransformation())
                listener(onSuccess = { _, _ ->
                    binding.imageView.setPadding(0, 0, 0, 0)
                    binding.imageView.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
                }, onError = { _, _ ->
                    googleSimgesiYap()
                })
            }
        } else {
            googleSimgesiYap()
        }
    }
    private fun googleSimgesiYap() {
        binding.imageView.apply {
            setPadding(15, 15, 15, 15)
            scaleType = android.widget.ImageView.ScaleType.CENTER_INSIDE
        }
    }

    private fun girisiKaydet() {
        val sharedPref = requireActivity().getSharedPreferences("TemaAyari", Context.MODE_PRIVATE)
        sharedPref.edit().putBoolean("isLoggedIn", true).apply()
    }

    private fun masalaraGit() {
        if (isAdded) {
            val navController = findNavController()
            if (navController.currentDestination?.id == R.id.girisFragment) {
                navController.navigate(R.id.action_girisFragment_to_masalarFragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}