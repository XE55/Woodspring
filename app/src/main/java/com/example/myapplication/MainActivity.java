package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import com.example.myapplication.ui.home.HomeFragment;
import com.example.myapplication.ui.notifications.ErrorDialogFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;

import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.example.myapplication.databinding.ActivityMainBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity {

    public static final int PICK_CSV_FILE = 1;


    private static final String TAG = "MainActivity";

    FloatingActionButton fabBtn;
    private ActivityMainBinding binding;

    RecyclerView recyclerView;
    public static PersonDatabase personDB;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);

        recyclerView = findViewById(R.id.recyclerView);




        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(navView, navController);

        //DB implementation
        RoomDatabase.Callback callback = new RoomDatabase.Callback() {
            @Override
            public void onCreate(androidx.sqlite.db.SupportSQLiteDatabase db) {
                super.onCreate(db);
            }
            @Override
            public void onOpen(androidx.sqlite.db.SupportSQLiteDatabase db) {
                super.onOpen(db);
            }
        };

        personDB = Room.databaseBuilder(getApplicationContext(), PersonDatabase.class, "personDB").addCallback(callback).build();

        //Logic for basic implementation
        /*firstNameEdit = findViewById(R.id.firstNameEdit);
        nameEdit = findViewById(R.id.nameEdit);
        isPreferred = findViewById(R.id.isPreferred);
        saveBtn = findViewById(R.id.saveBtn);
        getBtn = findViewById(R.id.getBtn);*/

/*
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String firstName = firstNameEdit.getText().toString();
                String name = nameEdit.getText().toString();
                boolean preferred = isPreferred.isChecked();

                Person person = new Person(firstName, name, preferred);
               addPersonInBackground(person);
                getPersonInBackground();

            }
        });

        getBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                getPersonInBackground();
            }
        });


*/
        fabBtn = findViewById(R.id.fab);
        fabBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("text/comma-separated-values");


                startActivityForResult(intent, PICK_CSV_FILE);
            }
        });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_CSV_FILE && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                Reader reader = new InputStreamReader(inputStream);
                CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader());
                ExecutorService executorService = Executors.newSingleThreadExecutor();
                executorService.execute(() -> {
                    int count_before = personDB.personDAO().getTotal();
                    for (CSVRecord record : parser) {
                        String firstName = record.get("firstname");
                        String lastName = record.get("lastname");

                        // Check if the person already exists in the database
                        if (!personDB.personExists(firstName, lastName)) {
                            Person person = new Person(lastName, firstName, false);
                            personDB.personDAO().insert(person);
                            //get total number of persons added


                        }

                    }
                    NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_main);
                    if (navHostFragment != null) {
                        List<Fragment> fragments = navHostFragment.getChildFragmentManager().getFragments();
                        for (Fragment fragment : fragments) {
                            if (fragment instanceof HomeFragment) {
                                HomeFragment homeFragment = (HomeFragment) fragment;
                                homeFragment.getPersonInBackground();
                                break;
                            }
                        }
                    }


                    int count_after = personDB.personDAO().getTotal();
                    int count_added = count_after - count_before;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (count_added == 0){
                                Toast.makeText(MainActivity.this, "No person added", Toast.LENGTH_SHORT).show();}else{
                            if (count_added == 1 ){
                                Toast.makeText(MainActivity.this, count_added + " person added", Toast.LENGTH_SHORT).show();
                            }else {
                            Toast.makeText(MainActivity.this, count_added + " persons added", Toast.LENGTH_SHORT).show();
                        }}}
                    });

                });
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                showErrorDialog("File not found");
            } catch (IOException e) {
                e.printStackTrace();
                showErrorDialog("Error reading file");
            }
        }
    }


    private void showErrorDialog(String message) {
        ErrorDialogFragment errorDialog = ErrorDialogFragment.newInstance(message);
        errorDialog.show(getSupportFragmentManager(), "error_dialog");
    }
    public void addPersonInBackground(Person person){
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(getMainLooper());
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                personDB.personDAO().insert(person);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Person added", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    //todo:add a button to delete all persons
    //todo: add a search bar to search for a person
    //todo:add a setting page to change name of columns and shit
    //todo:add error handling
    //todo:add an image to show when there is no contact

}