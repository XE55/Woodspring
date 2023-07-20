package com.example.myapplication.ui.home;

import static android.app.Activity.RESULT_OK;
import static android.os.Looper.getMainLooper;

import static com.example.myapplication.MainActivity.PICK_CSV_FILE;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.myapplication.PersonAdapter;
import com.example.myapplication.MainActivity;
import com.example.myapplication.Person;
import com.example.myapplication.PersonDAO;
import com.example.myapplication.PersonDatabase;
import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentHomeBinding;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    RecyclerView recyclerView;
    ImageButton buttonMenu;

    Button favBtn;
    LinkedHashMap<String, Person> personList;
    PersonAdapter personAdapter;

    PersonDatabase personDB;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        recyclerView = binding.recyclerView;
        buttonMenu = binding.buttonMenu;
        buttonMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(v);
            }
        });
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

        personDB = Room.databaseBuilder(getContext(), PersonDatabase.class, "personDB")
                .allowMainThreadQueries()
                .addCallback(callback)
                .build();


        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            private final Drawable deleteIcon = ContextCompat.getDrawable(getContext(), R.drawable.delete_icon);
            private final int iconMargin = (int) getResources().getDimension(R.dimen.icon_margin);
            private final ColorDrawable background = new ColorDrawable(Color.RED);

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                // Get the person to be deleted
                Person person = personAdapter.getPerson(position);
                // Remove the person from the database
                deletePersonInBackground(person);
                // Remove the person from the adapter
                personAdapter.removePerson(position);
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                // Limit swipe distance to half of the screen width
                if (dX < -recyclerView.getWidth() / 2) {
                    dX = -recyclerView.getWidth() / 2;
                }

                // Change the background color and draw the delete icon
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    View itemView = viewHolder.itemView;
                    int iconTop = itemView.getTop() + (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;
                    int iconBottom = iconTop + deleteIcon.getIntrinsicHeight();
                    int iconRight = itemView.getRight() - iconMargin;
                    int iconLeft = iconRight - deleteIcon.getIntrinsicWidth();

                    background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                    background.draw(c);

                    deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                    deleteIcon.draw(c);
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);



        return root;
    }

    private void showPopupMenu(View view) {

        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        popupMenu.inflate(R.menu.popup_menu);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {if (item.getItemId() == R.id.action_option1) {
                // Handle Option 1 click
                List<Person> personList = personDB.personDAO().getAllPerson();


// Create a FileWriter for the output file
                try {
                    File downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    File outputFile = new File(downloadsFolder, "Contacts.txt");

                    int count = 1;
                    while (outputFile.exists()) {
                        // Create a new file with a different name
                        outputFile = new File(downloadsFolder, "Contacts(" + count + ").txt");
                        count++;
                    }
                    FileWriter fileWriter = new FileWriter(outputFile);
                    fileWriter.write("firstname,lastname\n");
                    for (Person person : personList) {
                        fileWriter.write(person.getFirstname() + "," + person.getName() + "\n");
                        Log.d("TAG", "onMenuItemClick: " + person.getFirstname() + "," + person.getName() + "\n");
                    }

                    Log.d("TAG", "onMenuItemClick: " + outputFile.getAbsolutePath());
                    fileWriter.close();
                    } catch (IOException e) {
                    throw new RuntimeException(e);
                }


                return true;
            } else if (item.getItemId() == R.id.action_option2) {
                // Handle Option 2 click and delete all
                personDB.personDAO().deleteAll();
                getPersonInBackground();
                return true;
            } else {
                return false;
            }

            }
        });
        popupMenu.show();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        personList = new LinkedHashMap<>();

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        getPersonInBackground();
        EditText searchEditText = view.findViewById(R.id.searchEditText);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().toLowerCase();
                Map<String, List<Person>> filteredPeople = personList.values().stream()
                        .filter(p -> p.getFirstname().toLowerCase().contains(query) || p.getName().toLowerCase().contains(query))
                        .collect(Collectors.groupingBy(p -> {
                            String firstLetter = p.getName().substring(0, 1);
                            if (firstLetter.matches("[a-zA-Z]")) {
                                Log.d("TAG", "onTextChanged: " + firstLetter);
                                return firstLetter;
                            } else {
                                return "#";
                            }
                        }));
                int total = personDB.personDAO().getTotal();
                personAdapter.updateData(filteredPeople,total);
            }



            @Override
            public void afterTextChanged(Editable s) {}

            //swipe func

        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }




    public void getPersonInBackground(){
        Log.d("TAG", "getPersonInBackground: ");
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(getMainLooper());
        HomeFragment homeFragment = this;
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                List<Person> tempList = personDB.personDAO().getAllPerson();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        personList.clear();
                        for (Person person : tempList) {
                            personList.put(person.getId(), person);
                        }
                        Map<String, List<Person>> groupedPeople = personList.values().stream()
                                .collect(Collectors.groupingBy(p -> {
                                    String firstLetter = p.getName().substring(0, 1);
                                    if (firstLetter.matches("[a-zA-Z]")) {
                                        return firstLetter;
                                    } else {
                                        return "#";
                                    }
                                }));
                        int total = personDB.personDAO().getTotal();
                        personAdapter = new PersonAdapter(groupedPeople,total,homeFragment);
                        recyclerView.setAdapter(personAdapter);
                    }
                });
            }
        });
    }

    public void deletePersonInBackground(Person person){
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(getMainLooper());
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                personDB.personDAO().delete(person);
                getPersonInBackground();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), "Person deleted", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
    public void updatePersonInBackground(Person person){
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(getMainLooper());
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                personDB.personDAO().update(person);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), "Person updated", Toast.LENGTH_LONG).show();

                    }
                });
            }
        });
    }
    public void updateState(Person person){
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(getMainLooper());
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                personDB.personDAO().toggleFavorite(person.getIdInt());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), "Person updated", Toast.LENGTH_LONG).show();

                    }
                });
            }
        });
    }

    public void reloadFragment() {

    }
    //get data from csv file and log them in the console




}

