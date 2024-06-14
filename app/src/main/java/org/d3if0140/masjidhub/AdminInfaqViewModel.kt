package org.d3if0140.masjidhub.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import org.d3if0140.masjidhub.model.Infaq

class AdminInfaqViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _infaqList = MutableLiveData<List<Infaq>>()
    val infaqList: LiveData<List<Infaq>> = _infaqList

    init {
        loadInfaqData()
    }

    fun loadInfaqData() {
        db.collection("infaq_masjid")
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Handle error
                    return@addSnapshotListener
                }
                snapshot?.let { querySnapshot ->
                    val infaqList = querySnapshot.documents.mapNotNull { doc ->
                        doc.toObject(Infaq::class.java)?.apply {
                            id = doc.id
                        }
                    }
                    _infaqList.value = infaqList
                }
            }
    }

    fun approveInfaq(infaq: Infaq, onComplete: (Boolean) -> Unit) {
        val infaqId = infaq.id
        val updatedData = hashMapOf<String, Any>("status" to "approved")

        db.collection("infaq_masjid").document(infaqId)
            .update(updatedData)
            .addOnSuccessListener {
                onComplete(true)
            }
            .addOnFailureListener { e ->
                onComplete(false)
            }
    }

    fun sendApprovalNotification(infaq: Infaq) {
        val notificationData = hashMapOf<String, Any>(
            "title" to "Infaq Diterima",
            "message" to "Infaq sebesar Rp ${infaq.jumlahInfaq} telah diterima oleh aplikasi.",
            "timestamp" to System.currentTimeMillis(),
            "userEmail" to infaq.userEmail
        )

        db.collection("notifikasi")
            .add(notificationData)
            .addOnSuccessListener {
                // Notification successfully sent
            }
            .addOnFailureListener { e ->
                // Handle notification sending failure
            }
    }
}
