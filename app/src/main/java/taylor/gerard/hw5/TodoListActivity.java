package taylor.gerard.hw5;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

public class TodoListActivity extends AppCompatActivity {
    // define an id for the loader we'll use to manage a cursor and stick its data in the list
    private static final int TODO_LOADER = 1;
    private static final int NOTIFICATION_ID = 1991;

    private TodoAdapter adapter;
    private NotificationManager notificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setContentView(R.layout.activity_todo_list);
        setSupportActionBar(toolbar);


        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        // use a linear layout manager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        assert recyclerView != null;
        recyclerView.setLayoutManager(layoutManager);

        adapter = new TodoAdapter(this,
                new TodoAdapter.OnItemClickedListener() {
                    @Override public void onItemClicked(long id) {
                        // start activity to edit the item
                        // we're creating a new item; just pass -1 as the id
                        Intent intent = new Intent(TodoListActivity.this, EditActivity.class);
                        intent.putExtra("itemId", id);
                        startActivity(intent);
                    }});
        recyclerView.setAdapter(adapter);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // start activity to edit the item
                // we're creating a new item; just pass -1 as the id
                Intent intent = new Intent(TodoListActivity.this, EditActivity.class);
                intent.putExtra("itemId", -1L);
                startActivity(intent);
            }
        });

        // start asynchronous loading of the cursor
        getSupportLoaderManager().initLoader(TODO_LOADER, null, loaderCallbacks);
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                checkIntentAction();
                statusThread.start();
            }
        }).start();

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    private void checkIntentAction() {
        Intent intent = getIntent();
        if(intent.getExtras() == null) return;
        if(intent.getStringExtra("ACTION").equals("DONE")){
            markAllAsDone(this);
        }else if(intent.getStringExtra("ACTION").equals("SNOOZE")){
            TodoWidget.snoozeAll(this);
        }
    }

    private void snoozeAll(Context context) {
        ArrayList<TodoItem> items = Util.findAll(context);
        for(TodoItem item : items){
            item.updateDueDate(System.currentTimeMillis() + 10000, context);
        }
    }

    private void markAllAsDone(Context context) {
        ArrayList<TodoItem> items = Util.findAll(context);
        for(TodoItem item : items){
            item.status.set(Status.Done);
            Util.updateTodo(context, item);
        }
    }

    // define a loader manager that will asynchronously retrieve data and when finished,
    //   update the list's adapter with the changes
    private LoaderManager.LoaderCallbacks<Cursor> loaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {
        // when the loader is created, setup the projection to retrieve from the database
        //   and create a cursorloader to request a cursor from a content provider (by URI)
        @Override
        public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
            String[] projection = {
                    TodoProvider.ID,
                    TodoProvider.NAME,
                    TodoProvider.DESCRIPTION,
                    TodoProvider.PRIORITY,
                    TodoProvider.STATUS,
                    TodoProvider.DUETIME
            };
            return new CursorLoader(
                    TodoListActivity.this,
                    TodoProvider.CONTENT_URI, // note: this will register for changes
                    projection,
                    null, null, // groupby, having
                    TodoProvider.NAME + " asc");
        }

        // when the data has been loaded from the content provider, update the list adapter
        //   with the new cursor
        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            adapter.swapCursor(cursor); // set the data
        }

        // if the loader has been reset, kill the cursor in the adapter to remove the data from the list
        @Override
        public void onLoaderReset(Loader<Cursor> cursor) {
            adapter.swapCursor(null); // clear the data
        }
    };
    public volatile TodoItem[] dueItems = new TodoItem[5];
    public Thread statusThread = new Thread(new Runnable() {
        @Override
        public void run() {
            while(true){
                ArrayList<TodoItem> items = Util.findAll(getApplicationContext());
                if(items == null) continue;
                dueItems = new TodoItem[5];
                int count = 0;
                for(TodoItem item : items){
                    if(item.checkAndUpdateStatus(System.currentTimeMillis(), getApplicationContext())){
                        if(count < dueItems.length) {
                            dueItems[count] = item;
                        }
                        count++;
                    }
                }
                if(count == 0){
                    notificationManager.cancelAll();
                }else{
                    createNotification(NOTIFICATION_ID, count);
                }
                Intent intent = new Intent("ItemCount");
                intent.putExtra("count", count);
                getApplicationContext().sendBroadcast(intent);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    });

    private PendingIntent createPending(int id, String info){
        Intent intent = new Intent(this, TodoListActivity.class);
        intent.putExtra("ACTION", info);

        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this);
        taskStackBuilder.addParentStack(TodoListActivity.class);
        taskStackBuilder.addNextIntent(intent);
        return taskStackBuilder.getPendingIntent(id, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void createNotification(int id, int count){

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle()
                .setBigContentTitle("Todo Items")
                .setSummaryText("Tasks that are due.");

        int i = 0;
        while(i < dueItems.length && dueItems[i] != null){
            inboxStyle.addLine(dueItems[i].name.get());
            i++;
        }

        PendingIntent doneAction = createPending(id+1, "DONE");
        PendingIntent snoozeAction = createPending(id+2, "SNOOZE");
        PendingIntent mainAction = createPending(id+3, "MAIN");

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("Todo Items")
                .setContentText("Total Number of Items Due: ")
                .setContentIntent(mainAction)
                .setSmallIcon(R.drawable.ic_notifications_active_white_24dp)
                .addAction(R.drawable.ic_check_white_32dp, "Mark all as Done", doneAction)
                .addAction(R.drawable.ic_forward_10_white_32dp, "Snooze All",snoozeAction)
                .setStyle(inboxStyle)
                .setNumber(count)
                .setAutoCancel(true)
                .build();
        notificationManager.notify(id, notification);
    }

}
