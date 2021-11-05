package dev.apes.pawmance.utils

import com.google.firebase.firestore.FirebaseFirestore

fun FirebaseFirestore.petCollection() = collection("users")