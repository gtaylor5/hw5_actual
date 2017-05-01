package taylor.gerard.hw5;

import android.content.Context;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.databinding.ObservableLong;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class TodoItem implements Comparator<TodoItem>{



    public final ObservableLong id = new ObservableLong();
    public final ObservableLong dueTime = new ObservableLong();
    public final ObservableField<String> dueDate = new ObservableField<>();
    public final ObservableField<String> name = new ObservableField<>();
    public final ObservableField<String> description = new ObservableField<>();
    public final ObservableInt priority = new ObservableInt();
    public final ObservableField<Status> status = new ObservableField<>();


    public TodoItem() {
        this.id.set(-1);
        this.name.set("");
        this.description.set("");
        this.priority.set(1);
        this.status.set(Status.Pending);
    }

    public TodoItem(long id, String name, String description, int priority, String status, String dueTime) {
        this.id.set(id);
        this.dueTime.set(Long.parseLong(dueTime));
        Date date = new Date(this.dueTime.get());
        DateFormat format = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.US);
        this.dueDate.set(format.format(date));
        this.name.set(name);
        this.description.set(description);
        this.priority.set(priority);
        this.status.set(Status.valueOf(status));
    }

    public void updateDueDate(long timeInMilli, Context context){
        this.dueTime.set(timeInMilli);
        Date date = new Date(dueTime.get());
        DateFormat format = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.US);
        this.dueDate.set(format.format(date));
        this.status.set(Status.Pending);
        Util.updateTodo(context, this);
    }

    public boolean checkAndUpdateStatus(long timeInMilli, Context context){
        if(timeInMilli >= this.dueTime.get() && this.status.get() != Status.Done){
            this.status.set(Status.Due);
            Util.updateTodo(context, this);
            return true;
        }
        return false;
    }

    @Override
    public int compare(TodoItem o1, TodoItem o2) {
        if(o1.dueTime.get() < o2.dueTime.get()){
            return -1;
        }
        if(o1.dueTime.get() > o2.dueTime.get()){
            return 1;
        }
        return 0;
    }

    @Override
    public String toString() {
        return "TodoItem {"+id.get()+", "+name.get()+", "+description.get()+", "+dueDate.get()+", "+dueTime.get()+", "+status.get();
    }
}
