package taylor.gerard.hw5;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.ArrayList;

/**
 * Implementation of App Widget functionality.
 */
public class TodoWidget extends AppWidgetProvider {

    private Context context;
    private volatile int itemCount = 0;
    @Override
    public void onReceive(Context context, Intent intent) {
        if("ItemCount".equals(intent.getAction())){
            int count = intent.getIntExtra("count", -1);
            itemCount = count;
            ComponentName componentName = new ComponentName(context, TodoWidget.class);
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(componentName);
            for(int i = 0; i < appWidgetIds.length; i++) {
                updateWidget(context, count, componentName, appWidgetManager);
            }
        }else if("SNOOZEALL".equals(intent.getAction())){
            snoozeAll(context);
        } else {
            super.onReceive(context, intent);
        }
    }

    public static void snoozeAll(Context context) {
        ArrayList<TodoItem> items = Util.findAll(context);
        for(TodoItem item : items){
            item.updateDueDate(System.currentTimeMillis()+10000, context);
        }
    }

    private void updateWidget(Context context, int count, ComponentName componentName, AppWidgetManager appWidgetManager) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.todo_widget);
        remoteViews.setTextViewText(R.id.appwidget_text,getQuantityString(context, count));

        //when clicked show list

        Intent showList = new Intent(context, TodoListActivity.class);
        PendingIntent pendingIntent2 = PendingIntent.getActivity(context, 0, showList, 0);

        //add button

        Intent addItem = new Intent(context, EditActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, addItem, 0);

        //snooze

        Intent snoozeAll = new Intent(context, getClass());
        snoozeAll.setAction("SNOOZEALL");
        PendingIntent pendingIntent1 = PendingIntent.getBroadcast(context, 0, snoozeAll, 0);


        remoteViews.setOnClickPendingIntent(R.id.add, pendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.snooze, pendingIntent1);
        remoteViews.setOnClickPendingIntent(R.id.widget, pendingIntent2);

        appWidgetManager.updateAppWidget(componentName, remoteViews);
    }

    @NonNull
    private String getQuantityString(Context context, int count) {
        Resources res = context.getResources();
        return res.getQuantityString(R.plurals.items, count, count);
    }

    private void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {

    }


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
    }

    @Override
    public void onDisabled(Context context) {
    }




}

