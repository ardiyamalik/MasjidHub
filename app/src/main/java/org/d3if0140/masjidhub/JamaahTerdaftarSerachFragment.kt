package org.d3if0140.masjidhub

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import org.d3if0140.masjidhub.databinding.FragmentJamaahTerdaftarSearchBinding

class JamaahTerdaftarSerachFragment : Fragment() {

    private var _binding: FragmentJamaahTerdaftarSearchBinding? = null
    private val binding get() = _binding!!
    private val jamaahList = mutableListOf<Jamaah>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentJamaahTerdaftarSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerViewJamaah.layoutManager = LinearLayoutManager(context)

        binding.backButton.setOnClickListener {
            // Handle back button action
            requireActivity().supportFragmentManager.popBackStack()
        }

        val masjidName = arguments?.getString("nama")?.trim()
        Log.d("JamaahTerdaftarFragment", "Masjid Name: $masjidName")
        masjidName?.let {
            fetchJamaahList(it)
        } ?: Log.d("JamaahTerdaftarFragment", "Masjid name is null or empty")
    }

    private fun fetchJamaahList(currentMasjidName: String) {
        Log.d("JamaahTerdaftarFragment", "Fetching Jamaah list for Masjid: $currentMasjidName")
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("user")
            .whereEqualTo("role", "jamaah")
            .whereEqualTo("dkm", currentMasjidName)
            .get()
            .addOnSuccessListener { documents ->
                Log.d("JamaahTerdaftarFragment", "Successfully fetched documents")
                jamaahList.clear()
                for (document in documents) {
                    val jamaah = document.toObject(Jamaah::class.java)
                    jamaahList.add(jamaah)
                    Log.d("JamaahTerdaftarFragment", "Added Jamaah: $jamaah")
                }
                setupAdapter()
                Log.d("JamaahTerdaftarFragment", "Jamaah list size: ${jamaahList.size}")

                if (jamaahList.isEmpty()) {
                    Log.d("JamaahTerdaftarFragment", "Jamaah list is empty")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("JamaahTerdaftarFragment", "Error getting documents: ", exception)
            }
    }

    private fun setupAdapter() {
        val adapter = JamaahAdapter(jamaahList) { jamaah ->
            // Handle item click here
            Log.d("JamaahTerdaftarFragment", "Clicked on: ${jamaah.nama}")
        }
        binding.recyclerViewJamaah.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
