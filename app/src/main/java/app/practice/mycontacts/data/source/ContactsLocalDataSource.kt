package app.practice.mycontacts.data.source

import app.practice.mycontacts.data.Contact
import app.practice.mycontacts.data.ContactsDataSource
import app.practice.mycontacts.data.Result
import app.practice.mycontacts.data.Result.Error
import app.practice.mycontacts.data.Result.Success
import kotlinx.coroutines.*

/**
 * This class deals with the local database.
 */
class ContactsLocalDataSource(
    private val contactsDao: ContactsDao?,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ContactsDataSource {

    /**
     * Returns list of all contacts from the database.
     * If no records are found then empty list of contacts is returned.
     */
    override suspend fun getContacts(): Result<List<Contact>> = withContext(ioDispatcher) {
        return@withContext try {
            Success(contactsDao!!.getAllContacts())
        } catch (e: Exception) {
            Error(e)
        }
    }

    /**
     * Returns single contact w.r.t given contact id if found in the
     * database, returns [Error] otherwise.
     */
    override suspend fun getContact(contactId: String): Result<Contact> =
        withContext(ioDispatcher) {
            return@withContext try {
                val contact = contactsDao?.getContactById(contactId)
                if (contact != null) {
                    return@withContext Success(contact)
                } else {
                    return@withContext Error(Exception("Contact not found"))
                }
            } catch (e: Exception) {
                Error(e)
            }
        }

    /**
     * Saves contact in the database.
     */
    override suspend fun saveContact(contact: Contact) = withContext(ioDispatcher) {
        contactsDao!!.insertContact(contact)
    }

    /**
     * Updates contact in the database w.r.t given contact id.
     */
    override suspend fun updateContact(contact: Contact) = withContext(ioDispatcher) {
        contactsDao!!.updateContact(contact)
    }

    /**
     * Deletes single contact from the database w.r.t given contact id.
     */
    override suspend fun deleteContact(contactId: String) = withContext(ioDispatcher) {
        contactsDao!!.deleteContactById(contactId)
    }

    /**
     * Deletes all contacts from the database.
     */
    override suspend fun deleteAllContacts() = withContext(ioDispatcher) {
        contactsDao!!.deleteAllContacts()
    }

}