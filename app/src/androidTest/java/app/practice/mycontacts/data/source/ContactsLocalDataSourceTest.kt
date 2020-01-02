package app.practice.mycontacts.data.source

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import app.practice.mycontacts.data.Contact
import app.practice.mycontacts.data.Result.Error
import app.practice.mycontacts.data.Result.Success
import app.practice.mycontacts.data.succeeded
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests for implementation of [ContactsLocalDataSource].
 */
@RunWith(AndroidJUnit4::class)
@SmallTest
class ContactsLocalDataSourceTest {

    // SUT
    private lateinit var contactsLocalDataSource: ContactsLocalDataSource

    // Use to access contacts dao and work with data
    private lateinit var database: ContactsDatabase

    // Use context to initialize in-memory database
    private val context = ApplicationProvider.getApplicationContext<Context>()

    // Fake contact data
    private val contact1 = Contact("contact1", "1234567891")
    private val contact2 = Contact("contact2", "1234567892")
    private val newContact = Contact("newContact", "1234567893")
    private val invalidId = "invalid_id"

    @Before
    fun setUp() {
        // We use in-memory database because it clears off after tests
        database = buildInMemoryDatabase()

        // get contacts dao to be injected
        val contactsDao = database.contactsDao()

        // initialize contacts local data source
        contactsLocalDataSource = ContactsLocalDataSource(contactsDao)
    }

    @After
    fun cleanUp() {
        database.close()
    }

    @Test
    fun saveContact_contactSaved() = runBlocking {
        // WHEN - a contact is saved
        contactsLocalDataSource.saveContact(contact1)

        // THEN - verify that the contact was saved and has expected values
        val result = contactsLocalDataSource.getContacts()
        assertThat(result.succeeded).isTrue()
        val contacts = (result as Success).data
        assertThat(contacts.size).isEqualTo(1)
        assertThat(contacts[0].id).isEqualTo(contact1.id)
        assertThat(contacts[0].name).isEqualTo(contact1.name)
        assertThat(contacts[0].mobile).isEqualTo(contact1.mobile)
    }

    @Test
    fun saveContact_sameContact_contactReplaced() = runBlocking {
        // GIVEN - a contact is saved
        contactsLocalDataSource.saveContact(contact1)

        // WHEN - same contact is saved
        contactsLocalDataSource.saveContact(contact1)

        // THEN - verify that the contact was replaced and has expected values
        val result = contactsLocalDataSource.getContacts()
        assertThat(result.succeeded).isTrue()
        val contacts = (result as Success).data
        assertThat(contacts.size).isEqualTo(1)
        assertThat(contacts[0].id).isEqualTo(contact1.id)
        assertThat(contacts[0].name).isEqualTo(contact1.name)
        assertThat(contacts[0].mobile).isEqualTo(contact1.mobile)
    }

    @Test
    fun getContacts_allContactsRetrieved() = runBlocking {
        // GIVEN - two contacts are saved
        contactsLocalDataSource.saveContact(contact1)
        contactsLocalDataSource.saveContact(contact2)

        // WHEN - getting all contacts
        val result = contactsLocalDataSource.getContacts()

        // THEN - verify that all contacts were retrieved and have expected values
        assertThat(result.succeeded).isTrue()
        val contacts = (result as Success).data
        assertThat(contacts.size).isEqualTo(2)
        assertThat(contacts[0].id).isEqualTo(contact1.id)
        assertThat(contacts[0].name).isEqualTo(contact1.name)
        assertThat(contacts[0].mobile).isEqualTo(contact1.mobile)
        assertThat(contacts[1].id).isEqualTo(contact2.id)
        assertThat(contacts[1].name).isEqualTo(contact2.name)
        assertThat(contacts[1].mobile).isEqualTo(contact2.mobile)
    }

    @Test
    fun getContacts_noRecords_emptyListRetrieved() = runBlocking {
        // GIVEN - there are contact no records in the database

        // WHEN - getting all contacts
        val result = contactsLocalDataSource.getContacts()

        // THEN - verify that the contacts list is empty
        assertThat(result.succeeded).isTrue()
        val contacts = (result as Success).data
        assertThat(contacts).isEmpty()
    }

    @Test
    fun getContact_contactRetrieved() = runBlocking {
        // GIVEN - a contact is saved
        contactsLocalDataSource.saveContact(contact1)

        // WHEN - getting contact by id
        val result = contactsLocalDataSource.getContact(contact1.id)

        // THEN - verify that contact is retrieved and has expected values
        assertThat(result.succeeded).isTrue()
        val contact = (result as Success).data
        assertThat(contact.id).isEqualTo(contact1.id)
        assertThat(contact.name).isEqualTo(contact1.name)
        assertThat(contact.mobile).isEqualTo(contact1.mobile)
    }

    @Test
    fun getContact_invalidId_errorReturned() = runBlocking {
        // GIVEN - a contact is saved
        contactsLocalDataSource.saveContact(contact1)

        // WHEN - getting contact by invalid id
        val result = contactsLocalDataSource.getContact(invalidId)

        // THEN - verify that the result is instance of error
        assertThat(result).isInstanceOf(Error::class.java)
    }

    @Test
    fun getContact_emptyId_errorReturned() = runBlocking {
        // GIVEN - a contact is saved
        contactsLocalDataSource.saveContact(contact1)

        // WHEN - getting contact by empty id
        val result = contactsLocalDataSource.getContact("")

        // THEN - verify that the result is instance of error
        assertThat(result).isInstanceOf(Error::class.java)
    }

    @Test
    fun updateContact_contactUpdated() = runBlocking {
        // GIVEN - a contact is saved
        contactsLocalDataSource.saveContact(contact1)

        // WHEN - the contact is updated
        val newContact = Contact(newContact.name, newContact.mobile, contact1.id)
        contactsLocalDataSource.updateContact(newContact)

        // THEN - the contact is updated and has expected values
        val result = contactsLocalDataSource.getContact(contact1.id)
        assertThat(result.succeeded).isTrue()
        val updatedContact = (result as Success).data
        assertThat(updatedContact.id).isEqualTo(newContact.id)
        assertThat(updatedContact.name).isEqualTo(newContact.name)
        assertThat(updatedContact.mobile).isEqualTo(newContact.mobile)
    }

    @Test
    fun updateContact_invalidId_contactNotUpdated() = runBlocking {
        // GIVEN - a contact is saved
        contactsLocalDataSource.saveContact(contact1)

        // WHEN - the contact is updated with invalid id
        val newContact = Contact(newContact.name, newContact.mobile, invalidId)
        contactsLocalDataSource.updateContact(newContact)

        // THEN - verify that the contact was not updated
        val result = contactsLocalDataSource.getContact(contact1.id)
        assertThat(result.succeeded).isTrue()
        val contact = (result as Success).data
        assertThat(contact.id).isEqualTo(contact1.id)
        assertThat(contact.name).isEqualTo(contact1.name)
        assertThat(contact.mobile).isEqualTo(contact1.mobile)
    }

    @Test
    fun updateContact_emptyId_contactNotUpdated() = runBlocking {
        // GIVEN - a contact is saved
        contactsLocalDataSource.saveContact(contact1)

        // WHEN - the contact is updated with empty id
        val newContact = Contact(newContact.name, newContact.mobile, "")
        contactsLocalDataSource.updateContact(newContact)

        // THEN - verify that the contact was not updated
        val result = contactsLocalDataSource.getContact(contact1.id)
        assertThat(result.succeeded).isTrue()
        val contact = (result as Success).data
        assertThat(contact.id).isEqualTo(contact1.id)
        assertThat(contact.name).isEqualTo(contact1.name)
        assertThat(contact.mobile).isEqualTo(contact1.mobile)
    }

    @Test
    fun deleteContact_contactDeleted() = runBlocking {
        // GIVEN - two contacts are saved
        contactsLocalDataSource.saveContact(contact1)
        contactsLocalDataSource.saveContact(contact2)

        // WHEN - a contact is deleted by id
        contactsLocalDataSource.deleteContact(contact1.id)

        // THEN - verify that the contact was deleted
        val result = contactsLocalDataSource.getContacts()
        assertThat(result.succeeded).isTrue()
        val contacts = (result as Success).data
        assertThat(contacts.size).isEqualTo(1)
        assertThat(contacts[0].id).isEqualTo(contact2.id)
        assertThat(contacts[0].name).isEqualTo(contact2.name)
        assertThat(contacts[0].mobile).isEqualTo(contact2.mobile)
    }

    @Test
    fun deleteContact_invalidId_contactNotDeleted() = runBlocking {
        // GIVEN - a contact is saved
        contactsLocalDataSource.saveContact(contact1)

        // WHEN - the contact is deleted by invalid id
        contactsLocalDataSource.deleteContact(invalidId)

        // THEN - verify that the contact was not deleted
        val result = contactsLocalDataSource.getContacts()
        assertThat(result.succeeded).isTrue()
        val contacts = (result as Success).data
        assertThat(contacts.size).isEqualTo(1)
    }

    @Test
    fun deleteContact_emptyId_contactNotDeleted() = runBlocking {
        // GIVEN - a contact is saved
        contactsLocalDataSource.saveContact(contact1)

        // WHEN - the contact is deleted by empty id
        contactsLocalDataSource.deleteContact("")

        // THEN - verify that the contact was not deleted
        val result = contactsLocalDataSource.getContacts()
        assertThat(result.succeeded).isTrue()
        val contacts = (result as Success).data
        assertThat(contacts.size).isEqualTo(1)
    }

    @Test
    fun deleteAllContacts_allContactsDeleted() = runBlocking {
        // GIVEN - two contacts are saved
        contactsLocalDataSource.saveContact(contact1)
        contactsLocalDataSource.saveContact(contact2)

        // WHEN - all contacts are deleted
        contactsLocalDataSource.deleteAllContacts()

        // THEN - verify that all contacts were deleted
        val result = contactsLocalDataSource.getContacts()
        assertThat(result.succeeded).isTrue()
        val contacts = (result as Success).data
        assertThat(contacts).isEmpty()
    }

    /**
     * Helper method to create In-Memory Database.
     */
    private fun buildInMemoryDatabase(): ContactsDatabase {
        return Room.inMemoryDatabaseBuilder(
            context,
            ContactsDatabase::class.java
        ).build()
    }
}