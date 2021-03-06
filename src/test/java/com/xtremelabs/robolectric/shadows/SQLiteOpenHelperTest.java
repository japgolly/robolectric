package com.xtremelabs.robolectric.shadows;


import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(WithTestDefaultsRunner.class)
public class SQLiteOpenHelperTest {

    private TestOpenHelper helper;

    @Before
    public void setUp() throws Exception {
    	ShadowSQLiteDatabase.resetInMemoryDatabases();
        helper = new TestOpenHelper(null, "path", null, 1);
    }

    @Test
    public void testInitialGetReadableDatabase() throws Exception {
        SQLiteDatabase database = helper.getReadableDatabase();
        assertInitialDB(database);
    }

    @Test
    public void testSubsequentGetReadableDatabase() throws Exception {
        SQLiteDatabase database = helper.getReadableDatabase();
        helper.reset();
        database = helper.getReadableDatabase();

        assertSubsequentDB(database);
    }

    @Test
    public void testSameDBInstanceSubsequentGetReadableDatabase() throws Exception {
        SQLiteDatabase db1 = helper.getReadableDatabase();
        SQLiteDatabase db2 = helper.getReadableDatabase();

        assertThat(db1, sameInstance(db2));
    }

    @Test
    public void testInitialGetWritableDatabase() throws Exception {
        SQLiteDatabase database = helper.getWritableDatabase();
        assertInitialDB(database);
    }

    @Test
    public void testSubsequentGetWritableDatabase() throws Exception {
        helper.getWritableDatabase();
        helper.reset();

        assertSubsequentDB(helper.getWritableDatabase());
    }

    @Test
    public void testSameDBInstanceSubsequentGetWritableDatabase() throws Exception {
        SQLiteDatabase db1 = helper.getWritableDatabase();
        SQLiteDatabase db2 = helper.getWritableDatabase();

        assertThat(db1, sameInstance(db2));
    }

    @Test
    public void testClose() throws Exception {
        SQLiteDatabase database = helper.getWritableDatabase();

        assertThat(database.isOpen(), equalTo(true));
        helper.close();
        assertThat(database.isOpen(), equalTo(false));
    }

    private void assertInitialDB(SQLiteDatabase database) {
        assertDatabaseOpened(database);
        assertThat(helper.onCreateCalled, equalTo(true));
    }

    private void assertSubsequentDB(SQLiteDatabase database) {
        assertDatabaseOpened(database);
        assertThat(helper.onCreateCalled, equalTo(false));
    }

    private void assertDatabaseOpened(SQLiteDatabase database) {
        assertThat(database, notNullValue());
        assertThat(database.isOpen(), equalTo(true));
        assertThat(helper.onOpenCalled, equalTo(true));
        assertThat(helper.onUpgradeCalled, equalTo(false));
    }

    private class TestOpenHelper extends SQLiteOpenHelper {

        public boolean onCreateCalled;
        public boolean onUpgradeCalled;
        public boolean onOpenCalled;

        public TestOpenHelper(Context context, String name,
                              CursorFactory factory, int version) {
            super(context, name, factory, version);
            reset();
        }

        @Override
        public void onCreate(SQLiteDatabase database) {
            onCreateCalled = true;
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
            onUpgradeCalled = true;
        }

        @Override
        public void onOpen(SQLiteDatabase database) {
            onOpenCalled = true;
        }

        public void reset() {
            onCreateCalled = false;
            onUpgradeCalled = false;
            onOpenCalled = false;
        }
    }
    
    @Test
    public void testDataRetained() throws Exception {
		final String createTableSql = "create table datarentiontest(id int primary key)";

		// Create table the first time - no worries
		SQLiteDatabase db = helper.getWritableDatabase();
		db.execSQL(createTableSql);
		db.close();

		// Create table the second time - should fail if the same database is in use
		db = helper.getWritableDatabase();
		try {
			db.execSQL(createTableSql);
			fail("Data isn't being retained. The same CREATE TABLE statement worked twice in a row,");
		} catch (SQLException e) {
		}
    }

	@Test
	public void testReset() throws Exception {
		final String createTableSql = "create table resettest(id int primary key)";

		// Create a table
		SQLiteDatabase db = helper.getWritableDatabase();
		db.execSQL(createTableSql);
		db.close();

		ShadowSQLiteDatabase.resetInMemoryDatabases();

		// Create table second time - should work if wiped properly
		db = helper.getWritableDatabase();
		db.execSQL(createTableSql);
		db.close();
	}
}
