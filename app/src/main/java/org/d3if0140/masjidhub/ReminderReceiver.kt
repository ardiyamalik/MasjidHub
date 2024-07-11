package org.d3if0140.masjidhub

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Tindakan yang dilakukan saat pengingat aktif
        Toast.makeText(context, "Saatnya membayar kas mingguan!", Toast.LENGTH_SHORT).show()

        // Tambahkan logika untuk mengirim notifikasi atau tindakan lain yang diperlukan
    }
}
