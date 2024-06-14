package org.d3if0140.masjidhub

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import org.d3if0140.masjidhub.adapter.InfaqAdapter
import org.d3if0140.masjidhub.databinding.ActivityAdminInfaqBinding
import org.d3if0140.masjidhub.model.Infaq
import org.d3if0140.masjidhub.viewmodel.AdminInfaqViewModel

class AdminInfaqActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminInfaqBinding
    private val viewModel: AdminInfaqViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminInfaqBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = InfaqAdapter { infaq -> approveInfaq(infaq) }
        binding.recyclerViewInfaq.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewInfaq.adapter = adapter

        viewModel.infaqList.observe(this) { infaqList ->
            adapter.submitList(infaqList)
        }
    }

    private fun approveInfaq(infaq: Infaq) {
        viewModel.approveInfaq(infaq) { success ->
            if (success) {
                viewModel.sendApprovalNotification(infaq)
                Toast.makeText(this, "Infaq telah disetujui", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Gagal menyetujui infaq", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
