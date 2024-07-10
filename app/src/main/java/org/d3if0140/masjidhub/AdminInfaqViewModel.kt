package org.d3if0140.masjidhub.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import org.d3if0140.masjidhub.model.Infaq

class AdminInfaqViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val _infaqList = MutableLiveData<List<Infaq>>()
    val infaqList: LiveData<List<Infaq>> get() = _infaqList

    init {
        fetchInfaqData()
    }

    private fun fetchInfaqData() {
        firestore.collection("infaq_masjid")
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { result ->
                val infaqList = result.mapNotNull { document ->
                    document.toObject(Infaq::class.java).copy(id = document.id)
                }
                _infaqList.value = infaqList
            }
    }

    fun approveInfaq(infaq: Infaq, callback: (Boolean) -> Unit) {
        firestore.collection("infaq_masjid").document(infaq.id)
            .update("status", "approved")
            .addOnSuccessListener {
                fetchInfaqData() // Refresh data
                callback(true)
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    fun rejectInfaq(infaq: Infaq, callback: (Boolean) -> Unit) {
        firestore.collection("infaq_masjid").document(infaq.id)
            .delete()
            .addOnSuccessListener {
                fetchInfaqData() // Refresh data
                callback(true)
            }
            .addOnFailureListener {
                callback(false)
            }
    }
}
