package taylor.gerard.hw5;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

// an activty to edit a todo item
public class EditActivity extends AppCompatActivity {
    // edit texts to hold the item's values
    private EditText nameText;
    private EditText descriptionText;
    private EditText priorityText;

    private TodoItem item;

    public static final Long TEN_SECONDS_IN_MILLI = 10000L;

    // set up the user interface when the activity has been created
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // find the edit texts
        nameText = (EditText) findViewById(R.id.name);
        descriptionText = (EditText) findViewById(R.id.description);
        priorityText = (EditText) findViewById(R.id.priority);

        // get the id of the item to edit (or -1 if not set)
        long itemId = getIntent().getLongExtra("itemId", -1L);

        // if there's an id, lookup the item
        if (itemId != -1) {
            item = Util.findTodo(this, itemId);

        // if no id, create a new item
        } else {
            item = new TodoItem();
        }

        // fill in the initial values for the UI
        nameText.setText(item.name.get());
        descriptionText.setText(item.description.get());
        priorityText.setText(""+item.priority.get());
    }

    // set up the action bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if(this.item.status.get() == Status.Done){
            getMenuInflater().inflate(R.menu.menu_edit2, menu);
            return true;
        }
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    // handle action bar items pressed
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id) {
            // if "done" was pressed, save the data in a new item and return it
            case R.id.save:
                // set up an item to return
                this.item.name.set(nameText.getText().toString());
                this.item.description.set(descriptionText.getText().toString());
                this.item.priority.set(Integer.valueOf(priorityText.getText().toString()));
                this.item.updateDueDate(System.currentTimeMillis()+TEN_SECONDS_IN_MILLI, this);
                finish();
                return true;
            case R.id.done:
                this.item.status.set(Status.Done);
                Util.updateTodo(this, this.item);
                finish();
                return true;
            case R.id.snooze:
                this.item.updateDueDate(System.currentTimeMillis() + TEN_SECONDS_IN_MILLI, this);
                finish();
                return true;

            // if "cancel" was pressed, just return "canceled" without an item
            case R.id.cancel:
                // flag that we want to remove this activity from the stack and go back
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
