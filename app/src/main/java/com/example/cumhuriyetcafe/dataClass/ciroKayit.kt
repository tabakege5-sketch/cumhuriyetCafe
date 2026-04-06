package com.example.cumhuriyetcafe.dataClass

data class ciroKayit(
    val firebaseId: String = "",
    val tarih: String = "",
    val gelir: Double = 0.0,
    val gider: Double = 0.0,
    val gunlukCiro: Double = 0.0,
    val aciklama: String = ""
)