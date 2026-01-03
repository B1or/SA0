package tech.droi.saveas

import kotlin.collections.first

enum class SaveAs(val value: Int, val resource: Int) {
    PRIMARY(0, R.string.primary),
    ALTERNATIVE(1, R.string.alternative),
    DISPLAY(2, R.string.display),
    FIRST(3, R.string.first),
    SURNAME(4, R.string.surname),
    PATRONYMIC(5, R.string.patronymic),
    NICKNAME(6, R.string.nickname),
    NOTE(7, R.string.note),
    ORGANIZATION(8, R.string.organization);

    companion object {
        fun fromInt(value: Int) = entries.first { it.value == value }
    }
}
