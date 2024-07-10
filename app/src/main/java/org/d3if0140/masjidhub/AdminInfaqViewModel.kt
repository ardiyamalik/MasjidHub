package org.d3if0140.masjidhub.viewmodel

import android.util.Log
import android.widget.Toast
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

    private val TAG = "AdminInfaqActivity"

    fun fetchInfaqData() {
        firestore.collection("infaq_masjid")
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { result ->
                val infaqList = result.mapNotNull { document ->
                    document.toObject(Infaq::class.java).copy(id = document.id)
                }
                _infaqList.value = infaqList
            }
            .addOnFailureListener { e ->
                Log.e("AdminInfaqViewModel", "Error fetching infaq data", e)
            }
    }



    fun approveInfaq(infaq: Infaq, callback: (Boolean) -> Unit) {
        firestore.collection("infaq_masjid")
            .document(infaq.id)
            .update("status", "approved")
            .addOnSuccessListener {
                val updatedList = _infaqList.value?.filterNot { it.id == infaq.id }
                _infaqList.value = updatedList
                callback(true)
            }
            .addOnFailureListener { e ->
                Log.e("AdminInfaqViewModel", "Error approving infaq", e)
                callback(false)
            }
    }


    fun rejectInfaq(infaq: Infaq, callback: (Boolean) -> Unit) {
        firestore.collection("infaq_masjid")
            .document(infaq.id)
            .delete()
            .addOnCompleteListener { task ->
                callback(task.isSuccessful)
                fetchInfaqData() // Refresh data after rejection
            }
    }
}
