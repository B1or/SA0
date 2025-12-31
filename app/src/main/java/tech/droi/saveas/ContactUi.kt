package tech.droi.saveas

import android.net.Uri

data class ContactUi(
    val contactId: Long? = null,
    val saveAs: SaveAs? = null,
    val names: List<Pair<String, SaveAs>> = emptyList(),
    val photo: Uri? = null
)
