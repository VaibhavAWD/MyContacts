package app.practice.mycontacts.data.source

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import app.practice.mycontacts.data.Contact
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests for implementation of [ContactsDao].
 */
@RunWith(AndroidJUnit4::class)
@SmallTest
class ContactsDaoTest {

    // SUT
    private lateinit var contactsDao: ContactsDao

    // Use to access the contacts dao
    private lateinit var database: ContactsDatabase

    // Use context for initializing the in memory database
    private val context = ApplicationProvider.getApplicationContext<Context>()

    // Fake contacts data
    private val contact1 = Contact("contact1", "1234567891")
    private val contact2 = Contact("contact2", "1234567892")
    private val newContact = Contact("newContact", "1234567893")
    private val invalidId = "invalidId"

    @Before
    fun initDb() {
        // We use in-memory database for testing which is cleared after the tests
        database = buildInMemoryDatabase()

        // initialize the contacts dao object here
        contactsDao = database.contactsDao()
    }

    @After
    fun cleanUp() {
        database.close()
    }

    @Test
    fun insertContact_contactInserted() = runBlocking {
        // WHEN - a contact is inserted
        contactsDao.insertContact(contact1)

        // THEN - verify that the new contact is inserted and has expected values
        val contacts = contactsDao.getAllContacts()
        assertThat(contacts[0].id).isEqualTo(contact1.id)
        assertThat(contacts[0].name).isEqualTo(contact1.name)
        assertThat(contacts[0].mobile).isEqualTo(contact1.mobile)
    }

    @Test
    fun getAllContacts_allContactsLoaded() = runBlocking {
        // GIVEN - two contacts are inserted
        contactsDao.insertContact(contact1)
        contactsDao.insertContact(contact2)

        // WHEN - getting all contacts from database
        val contacts = contactsDao.getAllContacts()

        // THEN - verify that all contacts are loaded and have expected values
        assertThat(contacts.size).isEqualTo(2)
        assertThat(contacts[0].id).isEqualTo(contact1.id)
        assertThat(contacts[0].name).isEqualTo(contact1.name)
        assertThat(contacts[0].mobile).isEqualTo(contact1.mobile)
        assertThat(contacts[1].id).isEqualTo(contact2.id)
        assertThat(contacts[1].name).isEqualTo(contact2.name)
        assertThat(contacts[1].mobile).isEqualTo(contact2.mobile)
    }

    @Test
    fun getAllContacts_noRecords_emptyListReturned() = runBlocking {
        // GIVEN - contacts table has no records

        // WHEN - getting all contacts
        val contacts = contactsDao.getAllContacts()

        // THEN - verify contacts is empty
        assertThat(contacts).isEmpty()
    }

    @Test
    fun getContactById_contactLoaded() = runBlocking {
        // GIVEN - a contact is inserted
        contactsDao.insertContact(contact1)

        // WHEN - getting contact by id
        val contact = contactsDao.getContactById(contact1.id)

        // THEN - verify that the contact is loaded and has expected values
        assertThat(contact).isNotNull()
        assertThat(contact!!.id).isEqualTo(contact1.id)
        assertThat(contact.name).isEqualTo(contact1.name)
        assertThat(contact.mobile).isEqualTo(contact1.mobile)
    }

    @Test
    fun getContactById_invalidId_contactNotRetrieved() = runBlocking {
        // GIVEN - a contact is inserted
        contactsDao.insertContact(contact1)

        // WHEN - getting contact by an invalid id
        val contact = contactsDao.getContactById(invalidId)

        // THEN - verify that the contact is not retrieved
        assertThat(contact).isNull()
    }

    @Test
    fun getContactById_emptyId_contactNotRetrieved() = runBlocking {
        // GIVEN - a contact is inserted
        contactsDao.insertContact(contact1)

        // WHEN - getting contact by empty id
        val contact = contactsDao.getContactById("")

        // THEN - verify that contact is not retrieved
        assertThat(contact).isNull()
    }

    @Test
    fun updateContact_contactUpdated() = runBlocking {
        // GIVEN - a contact is inserted
        contactsDao.insertContact(contact1)

        // WHEN - the contact is updated
        val newContact = Contact(newContact.name, newContact.mobile, contact1.id)
        contactsDao.updateContact(newContact)

        // THEN - verify that the contact is updated and has expected values
        val contact = contactsDao.getContactById(contact1.id)
        assertThat(contact).isNotNull()
        assertThat(contact!!.id).isEqualTo(newContact.id)
        assertThat(contact.name).isEqualTo(newContact.name)
        assertThat(contact.mobile).isEqualTo(newContact.mobile)
    }

    @Test
    fun updateContact_invalidId_contactNotUpdated() = runBlocking {
        // GIVEN - a contact is inserted
        contactsDao.insertContact(contact1)

        // WHEN - the contact is updated with invalid id
        val newContact = Contact(newContact.name, newContact.mobile, invalidId)
        contactsDao.updateContact(newContact)

        // THEN - verify that the contact was not updated and has expected values
        val contact = contactsDao.getContactById(contact1.id)
        assertThat(contact).isNotNull()
        assertThat(contact!!.id).isEqualTo(contact1.id)
        assertThat(contact.name).isEqualTo(contact1.name)
        assertThat(contact.mobile).isEqualTo(contact1.mobile)
    }

    @Test
    fun updateContact_emptyId_contactNotUpdated() = runBlocking {
        // GIVEN - a contact is inserted
        contactsDao.insertContact(contact1)

        // WHEN - the contact is updated with empty id
        val newContact = Contact(newContact.name, newContact.mobile, "")
        contactsDao.updateContact(newContact)

        // THEN - verify that the contact was not updated and has expected values
        val contact = contactsDao.getContactById(contact1.id)
        assertThat(contact).isNotNull()
        assertThat(contact!!.id).isEqualTo(contact1.id)
        assertThat(contact.name).isEqualTo(contact1.name)
        assertThat(contact.mobile).isEqualTo(contact1.mobile)
    }

    @Test
    fun deleteContactById_contactDeleted() = runBlocking {
        // GIVEN - two contacts are inserted
        contactsDao.insertContact(contact1)
        contactsDao.insertContact(contact2)

        // WHEN - deleting a contact from database
        contactsDao.deleteContactById(contact1.id)

        // THEN - verify that the contact is deleted
        val contacts = contactsDao.getAllContacts()
        assertThat(contacts.size).isEqualTo(1)
        assertThat(contacts[0].id).isEqualTo(contact2.id)
        assertThat(contacts[0].name).isEqualTo(contact2.name)
        assertThat(contacts[0].mobile).isEqualTo(contact2.mobile)
    }

    @Test
    fun deleteContactById_invalidId_contactNotDeleted() = runBlocking {
        // GIVEN - a contact is inserted
        contactsDao.insertContact(contact1)

        // WHEN - deleting a contact with invalid id
        contactsDao.deleteContactById(invalidId)

        // THEN - verify that the contact was not deleted
        val contacts = contactsDao.getAllContacts()
        assertThat(contacts.size).isEqualTo(1)
    }

    @Test
    fun deleteContactById_emptyId_contactNotDeleted() = runBlocking {
        // GIVEN - a contact is inserted
        contactsDao.insertContact(contact1)

        // WHEN - deleting contact with empty id
        contactsDao.deleteContactById("")

        // THEN - verify that the contact was not deleted
        val contacts = contactsDao.getAllContacts()
        assertThat(contacts.size).isEqualTo(1)
    }

    @Test
    fun deleteAllContacts_allContactsDeleted() = runBlocking {
        // GIVEN - two contacts are inserted
        contactsDao.insertContact(contact1)
        contactsDao.insertContact(contact2)

        // WHEN - deleting all contacts from database
        contactsDao.deleteAllContacts()

        // THEN - verify that all contacts were deleted
        val contacts = contactsDao.getAllContacts()
        assertThat(contacts).isEmpty()
    }

    /**
     * Helper method which created a new In-Memory Database for testing purpose.
     */
    private fun buildInMemoryDatabase(): ContactsDatabase {
        return Room.inMemoryDatabaseBuilder(context, ContactsDatabase::class.java).build()
    }
}