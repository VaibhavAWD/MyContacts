package app.practice.mycontacts.data

/**
 * Abstract class to work with local and remote data sources.
 */
interface ContactsDataSource {

    suspend fun getContacts(): Result<List<Contact>>

    suspend fun getContact(contactId: String): Result<Contact>

    suspend fun saveContact(contact: Contact)

    suspend fun updateContact(contact: Contact)

    suspend fun deleteContact(contactId: String)

    suspend fun deleteAllContacts()

}