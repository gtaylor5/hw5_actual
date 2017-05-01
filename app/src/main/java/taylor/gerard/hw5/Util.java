package taylor.gerard.hw5;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.PriorityQueue;

public class Util {

    // helper method to find an item
    public static TodoItem findTodo(Context context, long id) {
        // set up a URI that represents the specific item
        Uri uri = Uri.withAppendedPath(TodoProvider.CONTENT_URI, "" + id);

        // set up a projection to show which columns we want to retrieve
        String[] projection = {
                TodoProvider.ID,
                TodoProvider.NAME,
                TodoProvider.DESCRIPTION,
                TodoProvider.PRIORITY,
                TodoProvider.STATUS,
                TodoProvider.DUETIME,
                TodoProvider.DUEDATE
        };

        // declare a cursor outside the try so we can close it in a finally
        Cursor cursor = null;
        try {
            // ask the content resolver to find the data for the URI
            cursor = context.getContentResolver().
                    query(uri, projection, null, null, null);

            // if nothing found, return null
            if (cursor == null || !cursor.moveToFirst())
                return null;

            // otherwise return the located item
            return todoItemFromCursor(cursor);
        } finally {
            // BE SURE TO CLOSE THE CURSOR!!!
            if (cursor != null)
                cursor.close();
        }
    }

    public static ArrayList<TodoItem> findAll(Context context){
        Uri uri = Uri.withAppendedPath(TodoProvider.CONTENT_URI,"");

        // set up a projection to show which columns we want to retrieve
        String[] projection = {
                TodoProvider.ID,
                TodoProvider.NAME,
                TodoProvider.DESCRIPTION,
                TodoProvider.PRIORITY,
                TodoProvider.STATUS,
                TodoProvider.DUETIME,
                TodoProvider.DUEDATE
        };


        Cursor cursor = null;
        ArrayList<TodoItem> items = new ArrayList<>();
        try {
            // ask the content resolver to find the data for the URI
            cursor = context.getContentResolver().
                    query(uri, projection, null, null, null);

            // if nothing found, return null
            if (cursor == null || !cursor.moveToFirst())
                return null;
            int i = 0;
            while(i < cursor.getCount()){
                items.add(todoItemFromCursor(cursor));
                cursor.moveToNext();
                i++;
            }
            return items;
        } finally {
            // BE SURE TO CLOSE THE CURSOR!!!
            if (cursor != null)
                cursor.close();
        }

    }

    // helper method to update or insert an item
    public static void updateTodo(Context context, TodoItem todo) {
        // set up the data to store or update
        ContentValues values = new ContentValues();
        values.put(TodoProvider.NAME, todo.name.get());
        values.put(TodoProvider.DESCRIPTION, todo.description.get());
        values.put(TodoProvider.PRIORITY, todo.priority.get());
        values.put(TodoProvider.STATUS, todo.status.get().toString());
        values.put(TodoProvider.DUETIME, todo.dueTime.get());
        values.put(TodoProvider.DUEDATE, todo.dueDate.get());

        // if the item didn't yet have an id, insert it and set the id on the object
        if (todo.id.get() == -1) {
            Uri uri = TodoProvider.CONTENT_URI;
            Uri insertedUri = context.getContentResolver().insert(uri, values);
            String idString = insertedUri.getLastPathSegment();
            long id = Long.parseLong(idString);
            todo.id.set(id);
        // otherwise, update the item with that id
        } else {
            // create a URI that represents the item
            Uri uri = Uri.withAppendedPath(TodoProvider.CONTENT_URI, "" + todo.id.get());
            context.getContentResolver().update(uri, values, TodoProvider.ID + "=" + todo.id.get(), null);
        }
    }


    public static TodoItem todoItemFromCursor(Cursor cursor) {
        TodoItem todoItem = new TodoItem(cursor.getLong(0), cursor.getString(1), cursor.getString(2), cursor.getInt(3), cursor.getString(4), cursor.getString(5));
        return todoItem;
    }
}
