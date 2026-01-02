package tech.droi.saveas

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.core.net.toUri

@HiltViewModel
class SaveViewModel @Inject constructor(
    private val contentResolver: ContentResolver,
    private val contactDao: ContactDao
) : ViewModel() {

    private val _uiState = MutableStateFlow<List<ContactUi>>(emptyList())
    val uiState: StateFlow<List<ContactUi>> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val listApi = arrayListOf<ContactApi>()
            val cursor = contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                null,
                null,
                null,
                null
            )
            cursor?.use { cursor ->
                while (cursor.moveToNext()) {
                    val id = cursor
                        .getLong(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                    val primary = cursor
                        .getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY))
                    var index = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_ALTERNATIVE)
                    val alternative = if (index >= 0) cursor.getString(index) ?: "" else ""
                    index = cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI)
                    val photo = (if (index >= 0) cursor.getString(index) ?: "" else "").toUri()
                    val contactApi = ContactApi(id, primary, alternative, photo)
                    var projection: Array<String> = arrayOf(
                        ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
                        ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
                        ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME
                    )
                    val selection = ContactsContract.Data.CONTACT_ID + " = ?" + " AND " + ContactsContract.Data.MIMETYPE + " = ?"
                    var selectionArgs = arrayOf(id.toString(), ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    var nameCursor = contentResolver.query(
                        ContactsContract.Data.CONTENT_URI,
                        projection,
                        selection,
                        selectionArgs,
                        null
                    )
                    nameCursor?.use {
                        if (it.moveToFirst()) {
                            index = it.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME)
                            contactApi.display = if (index >= 0) it.getString(index) ?: "" else ""
                            index = it.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME)
                            contactApi.given = if (index >= 0) it.getString(index) ?: "" else ""
                            index = it.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME)
                            contactApi.family = if (index >= 0) it.getString(index) ?: "" else ""
                            index = it.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME)
                            contactApi.middle = if (index >= 0) it.getString(index) ?: "" else ""
                        }
                    }
                    projection = arrayOf(ContactsContract.CommonDataKinds.Nickname.NAME)
                    selectionArgs = arrayOf(id.toString(), ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE)
                    nameCursor = contentResolver.query(
                        ContactsContract.Data.CONTENT_URI,
                        projection,
                        selection,
                        selectionArgs,
                        null
                    )
                    nameCursor?.use {
                        if (it.moveToFirst()) {
                            index = it.getColumnIndex(ContactsContract.CommonDataKinds.Nickname.NAME)
                            contactApi.nickname = if (index >= 0) it.getString(index) ?: "" else ""
                        }
                    }
                    projection = arrayOf(ContactsContract.CommonDataKinds.Note.NOTE)
                    selectionArgs = arrayOf(id.toString(), ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE)
                    nameCursor = contentResolver.query(
                        ContactsContract.Data.CONTENT_URI,
                        projection,
                        selection,
                        selectionArgs,
                        null
                    )
                    nameCursor?.use {
                        if (it.moveToFirst()) {
                            index = it.getColumnIndex(ContactsContract.CommonDataKinds.Note.NOTE)
                            contactApi.note = if (index >= 0) it.getString(index) ?: "" else ""
                        }
                    }
                    listApi.add(contactApi)
                }
            }
            listApi.forEach {
                if (contactDao.getContact(it.id) == null)
                    contactDao.insert(ContactRoom(it.id, SaveAs.PRIMARY.value))
            }
            val listRoom = contactDao.getAll()
            listRoom.forEach { contactRoom ->
                if (listApi.find { it.id == contactRoom.contactId } == null)
                    contactDao.delete(contactRoom)
            }
            val listUi = arrayListOf<ContactUi>()
            listApi.forEach { contactApi ->
                val listRaw = arrayListOf<Pair<String, SaveAs>>()
                var string = contactApi.primary.trim()
                if (string.isNotBlank()) listRaw.add(Pair(string, SaveAs.PRIMARY))
                string = contactApi.alternative.trim()
                if (string.isNotBlank()) listRaw.add(Pair(string, SaveAs.ALTERNATIVE))
                string = contactApi.display.trim()
                if (string.isNotBlank()) listRaw.add(Pair(string, SaveAs.DISPLAY))
                string = contactApi.given.trim()
                if (string.isNotBlank()) listRaw.add(Pair(string, SaveAs.FIRST))
                string = contactApi.family.trim()
                if (string.isNotBlank()) listRaw.add(Pair(string, SaveAs.SURNAME))
                string = contactApi.middle.trim()
                if (string.isNotBlank()) listRaw.add(Pair(string, SaveAs.PATRONYMIC))
                string = contactApi.nickname.trim()
                if (string.isNotBlank()) listRaw.add(Pair(string, SaveAs.NICKNAME))
                string = contactApi.note.trim()
                if (string.isNotBlank()) listRaw.add(Pair(string, SaveAs.NOTE))
                val saveAs = SaveAs.fromInt(contactDao.getContact(contactApi.id)!!.saveAs)
                val list = arrayListOf<Pair<String, SaveAs>>()
                list.add(listRaw.find { it.second == saveAs } ?: listRaw.find { it.second == SaveAs.PRIMARY } ?: listRaw.first())   // TODO
                val stringDelete = listRaw.find { it.second == saveAs }!!.first
                listRaw.removeIf { it.first == stringDelete }
                list.addAll(listRaw.distinctBy { it.first })
                listUi.add(ContactUi(contactApi.id, saveAs, list, contactApi.photo))
            }
            _uiState.value = listUi
        }
    }

    suspend fun sendPageSelectedEvent(contactUi: ContactUi, saveAs: SaveAs) {
        contactDao.update(ContactRoom(contactUi.contactId!!, saveAs.value))
    }

    fun click(context: Context, contactUi: ContactUi) {
        val contactUri: Uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactUi.contactId!!)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(contactUri, ContactsContract.Contacts.CONTENT_ITEM_TYPE)
            putExtra("finishActivityOnSaveCompleted", true)
        }
        context.startActivity(intent)
    }
}
