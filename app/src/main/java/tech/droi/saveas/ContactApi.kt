package tech.droi.saveas

import android.net.Uri

data class ContactApi(
    val id: Long,                   // идентификатор
    val primary: String,            // первичное
    val alternative: String,        // альтернативное
    val photo: Uri,                 // фото
    var display: String = "",       // показываемое
    var given: String = "",         // имя
    var family: String = "",        // фамилия
    var middle: String = "",        // отчество
    var nickname: String = "",      // прозвище
    var note: String = ""           // примечание
)
