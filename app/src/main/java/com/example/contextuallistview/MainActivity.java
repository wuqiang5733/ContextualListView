package com.example.contextuallistview;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.HashSet;

public class MainActivity extends AppCompatActivity {
    private ListView listView;
    private HashSet<Customer> selectedCustomers;
    private CustomerAdapter adapter;
    private ActionMode actionMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        selectedCustomers = new HashSet<>();
        adapter = new CustomerAdapter();
        for (int i = 0; i < 40; i++) {
            adapter.add(new Customer("Customer_" + Integer.toString(i + 1)));
        }

        listView = (ListView) findViewById(R.id.activity_main_listView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Customer customer = adapter.getItem(position);
                if (actionMode != null) {
                    toggleCustomerSelection(customer);
                } else {
                    showCustomer(customer);
                }
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
                Customer customer = adapter.getItem(position);
                toggleCustomerSelection(customer);
                return true;
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_main_addCustomer) {
            adapter.insert(new Customer("Instered Customer " + Integer.toString(adapter.getCount())), 0);
        }

        return super.onOptionsItemSelected(item);
    }

    private void showCustomer(Customer customer) {
        Toast.makeText(this, "Show customer " + customer.getName(), Toast.LENGTH_SHORT).show();

    }

    private void deleteCustomer(Iterable<Customer> customers) {
        adapter.setNotifyOnChange(false);
        for (Customer customer : customers) {
            adapter.remove(customer);
        }

        adapter.setNotifyOnChange(true);
        adapter.notifyDataSetChanged();
    }

    private void toggleCustomerSelection(Customer customer) {
        if (selectedCustomers.contains(customer)) {
            selectedCustomers.remove(customer);
        } else {
            selectedCustomers.add(customer);
        }
        if (selectedCustomers.size() == 0 && actionMode != null) {
            actionMode.finish();
            return;
        }
        if (actionMode == null) {
            actionMode = startSupportActionMode(new CustomerActionModeCallback());
        } else {
            actionMode.invalidate();
        }
        adapter.notifyDataSetChanged();
    }

    private class CustomerAdapter extends ArrayAdapter<Customer> {
        public CustomerAdapter() {
            super(MainActivity.this, R.layout.list_item_customer);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            Customer customer = adapter.getItem(position);

            if (selectedCustomers.contains(customer)) {
                view.setBackgroundColor(Color.parseColor("#B2EBF2"));
            } else {
                view.setBackground(null);
            }
            return view;
//            return super.getView(position, convertView, parent);
        }
    }

    private class CustomerActionModeCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            getMenuInflater().inflate(R.menu.menu_main_customs, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            if (selectedCustomers.size() == 1) {
                menu.setGroupVisible(R.id.menu_customers_singleOnlyGroup, true);
            } else {
                menu.setGroupVisible(R.id.menu_customers_singleOnlyGroup, false);
            }
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int id = item.getItemId();
            if (id == R.id.menu_main_customer_delete) {
                deleteCustomer(selectedCustomers);
                actionMode.finish();
                return true;
            }

            if (id == R.id.menu_main_customers_show) {
                if (selectedCustomers.size() != 1) {
                    throw new RuntimeException("The show button on the CAB only be pressed if one customer is selected");
                }
                Customer customer = selectedCustomers.iterator().next();
                showCustomer(customer);
                actionMode.finish();
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            MainActivity.this.actionMode = null;
            selectedCustomers.clear();
            adapter.notifyDataSetChanged();
        }
    }
}
