package com.example.cumhuriyetcafe.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.cumhuriyetcafe.R
import com.example.cumhuriyetcafe.databinding.FragmentAyarlarBinding

class ayarlarFragment : Fragment() {

    private var _binding: FragmentAyarlarBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ayarlar, container, false)
    }

}