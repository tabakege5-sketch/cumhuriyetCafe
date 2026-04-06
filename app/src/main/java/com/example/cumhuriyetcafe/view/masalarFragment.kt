package com.example.cumhuriyetcafe.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.cumhuriyetcafe.R
import com.example.cumhuriyetcafe.adapter.masaAdapter
import com.example.cumhuriyetcafe.dataClass.masa
import com.example.cumhuriyetcafe.databinding.FragmentMasalarBinding
import com.google.firebase.database.*

class masalarFragment : Fragment() {

    private var _binding: FragmentMasalarBinding? = null
    private val binding get() = _binding!!
    private lateinit var mAdapter: masaAdapter
    private lateinit var dbRef: DatabaseReference

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMasalarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbRef = FirebaseDatabase.getInstance().reference.child("Masalar")

        setupRecyclerView()
        masalariAnlikDinle()
    }

    private fun setupRecyclerView() {
        mAdapter = masaAdapter(emptyList()) { tiklananMasa ->
            val mIdStr = tiklananMasa.isim.replace(" ", "")
            val bundle = Bundle().apply {
                putString("secilenMasaId", mIdStr)
            }
            findNavController().navigate(R.id.action_masalarFragment_to_menulerFragment, bundle)
        }

        binding.masalarRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = mAdapter
        }
    }

    private fun masalariAnlikDinle() {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val yeniMasaListesi = mutableListOf<masa>()

                for (i in 1..20) {
                    val mKey = "Masa$i"
                    val isDolu = snapshot.child(mKey).value.toString() == "true"
                    val durumDegeri = if (isDolu) "true" else "false"

                    yeniMasaListesi.add(masa(
                        id = i.toString(),
                        isim = "Masa $i",
                        durum = durumDegeri
                    ))
                }

                if (_binding != null) {
                    mAdapter.updateData(yeniMasaListesi)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Hata: ${error.message}")
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}