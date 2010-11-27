package com.xtremelabs.robolectric.shadows;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class SQLiteDatabaseTest {
    private SQLiteDatabase database;

    @Before
    public void setUp() throws Exception {
        database = SQLiteDatabase.openDatabase("path", null, 0);
    }
    
    @After
    public void tearDown() throws Exception {
    	database.close();
    }

    @Test
    public void testInsertAndQuery() throws Exception {

        String stringColumnValue = "column_value";
        byte[] byteColumnValue = new byte[]{1, 2, 3};

        ContentValues values = new ContentValues();

        values.put("first_column", stringColumnValue);
        values.put("second_column", byteColumnValue);

        database.insert("table_name", null, values);

        Cursor cursor = database.query("table_name", new String[]{"second_column", "first_column"}, null, null, null, null, null);

        assertThat(cursor.moveToFirst(), equalTo(true));

        byte[] byteValueFromDatabase = cursor.getBlob(0);
        String stringValueFromDatabase = cursor.getString(1);

        assertThat(stringValueFromDatabase, equalTo(stringColumnValue));
        assertThat(byteValueFromDatabase, equalTo(byteColumnValue));
    }

    @Test
    public void testEmptyTable() throws Exception {
        Cursor cursor = database.query("table_name", new String[]{"second_column", "first_column"}, null, null, null, null, null);

        assertThat(cursor.moveToFirst(), equalTo(false));
    }
    
    @Test
    public void testUpdate() throws Exception {    	
    	addChuck();	
    	
    	assertThat( updateName( 1234L, "Buster" ), equalTo(1) );
    	
    	Cursor cursor = database.query("table_name", new String[]{"id", "name"}, null, null, null, null, null);
    	assertThat(cursor.moveToFirst(), equalTo(true));
    	assertThat(cursor.getCount(), equalTo(1));
 
    	assertIdAndName( cursor, 1234L, "Buster" );
    }
    
    @Test
    public void testUpdateNoMatch() throws Exception {
    	addChuck();
    	
    	assertThat(updateName( 5678L, "Buster" ), equalTo(0));
    	    	
    	Cursor cursor = database.query("table_name", new String[]{"id", "name"}, null, null, null, null, null);
    	assertThat(cursor.moveToFirst(), equalTo(true));
    	assertThat(cursor.getCount(), equalTo(1));
    	
    	assertIdAndName( cursor, 1234L, "Chuck" );
    }
    
    @Test
    public void testUpdateAll() throws Exception {
    	addChuck();
    	addJulie();
    	
    	assertThat(updateName( "Belvedere" ), equalTo(2));
    	
    	Cursor cursor = database.query("table_name", new String[]{"id", "name"}, null, null, null, null, null);
    	assertThat(cursor.moveToFirst(), equalTo(true));
    	assertThat(cursor.getCount(), equalTo(2));
    	
    	assertIdAndName( cursor, 1234L, "Belvedere" );
    	assertThat(cursor.moveToNext(), equalTo(true));

    	assertIdAndName( cursor, 1235L, "Belvedere" );
    	assertThat(cursor.moveToNext(), equalTo(false));
    }
    
    @Test
    public void testDelete() throws Exception {
    	addChuck();
    	
    	int deleted = database.delete("table_name", "id=1234", null);
    	assertThat( deleted, equalTo(1) );
    	
    	assertEmptyDatabase();
    }
    
    @Test
    public void testDeleteNoMatch() throws Exception {
    	addChuck();
    	
    	int deleted = database.delete("table_name", "id=5678", null);
    	assertThat( deleted, equalTo(0) );
    	
    	assertNonEmptyDatabase();
    }
    
    @Test
    public void testDeleteAll() throws Exception {
    	addChuck();
    	addJulie();
    	
    	int deleted = database.delete("table_name", "1", null);
    	assertThat( deleted, equalTo(2) );
    	
    	assertEmptyDatabase();
    }
    
    @Test
    public void testExecSQL() throws Exception {
    	Statement statement;
    	ResultSet resultSet;
    	
    	database.execSQL("CREATE TABLE table_name(id INT PRIMARY KEY, name VARCHAR(255));");
    	database.execSQL("INSERT INTO table_name (id, name) VALUES(1234, 'Chuck');");
    	
    	Connection conn = ((ShadowSQLiteDatabase) Robolectric.shadowOf(database)).getConnection();

    	statement = conn.createStatement();
    	resultSet = statement.executeQuery("SELECT COUNT(*) FROM table_name");
    	assertThat( resultSet.first(), equalTo(true) );
    	assertThat( resultSet.getInt(1), equalTo(1));
    	
    	statement = conn.createStatement();
    	resultSet = statement.executeQuery("SELECT * FROM table_name");
    	assertThat( resultSet.first(), equalTo(true) );
    	assertThat( resultSet.getInt(1), equalTo(1234));
    	assertThat( resultSet.getString(2), equalTo("Chuck"));
    }
    
    @Test(expected=IllegalStateException.class)
    public void testClose() throws Exception {
    	database.execSQL("CREATE TABLE table_name(id INT PRIMARY KEY, name VARCHAR(255));");
    	database.close();
    	
    	database.execSQL("INSERT INTO table_name (id, name) VALUES(1234, 'Chuck');");
    }
    
    @Test
    public void testIsOpen() throws Exception {
    	assertThat(database.isOpen(), equalTo(true));
    	database.close();
    	assertThat(database.isOpen(), equalTo(false));
    }
    
    private void addChuck() {
    	ContentValues values = new ContentValues();
    	values.put("id", 1234L);
    	values.put("name", "Chuck");	
    	database.insert("table_name", null, values);    	
    }
    
    private void addJulie() {
    	ContentValues values = new ContentValues();
    	values.put("id", 1235L);
    	values.put("name", "Julie");
    	database.insert("table_name", null, values);
    }

    private int updateName( long id, String name ) {
    	ContentValues values = new ContentValues();
		values.put("name", name);
		return database.update("table_name", values, "id=" + id, null);
    }
    
    private int updateName( String name ) {
    	ContentValues values = new ContentValues();
		values.put("name", name);
		return database.update("table_name", values, null, null);
    }
    
    private void assertIdAndName( Cursor cursor, long id, String name ) {
    	long idValueFromDatabase;
    	String stringValueFromDatabase;

    	idValueFromDatabase = cursor.getLong(0);
    	stringValueFromDatabase = cursor.getString(1);
    	assertThat(idValueFromDatabase, equalTo(id));
    	assertThat(stringValueFromDatabase, equalTo(name));
    }
    
    private void assertEmptyDatabase() {
    	Cursor cursor = database.query("table_name", new String[]{"id", "name"}, null, null, null, null, null);
    	assertThat(cursor.moveToFirst(), equalTo(false));
    	assertThat(cursor.getCount(), equalTo(0));
    }
    
    private void assertNonEmptyDatabase() {
    	Cursor cursor = database.query("table_name", new String[]{"id", "name"}, null, null, null, null, null);
    	assertThat(cursor.moveToFirst(), equalTo(true));
    	assertThat(cursor.getCount(), greaterThan(0));
    }
}
