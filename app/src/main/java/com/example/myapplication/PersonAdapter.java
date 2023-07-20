package com.example.myapplication;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.ui.home.HomeFragment;
import com.example.myapplication.ui.notifications.NotificationsFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class PersonAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;


    private static final int TYPE_FOOTER = 2;
    private HomeFragment homeFragment;
    private NotificationsFragment notificationsFragment;
    private int total ;

    private Map<String, List<Person>> groupedPeople;
    private List<String> sections;

    public Person getPerson(int position) {
        int count = 0;
        for (String section : sections) {
            if (position < count + groupedPeople.get(section).size() + 1) {
                return groupedPeople.get(section).get(position - count - 1);
            }
            count += groupedPeople.get(section).size() + 1;
        }
        return null;
    }

    public void removePerson(int position) {
        int count = 0;
        for (String section : sections) {
            if (position < count + groupedPeople.get(section).size() + 1) {
                groupedPeople.get(section).remove(position - count - 1);
                if (groupedPeople.get(section).isEmpty()) {
                    groupedPeople.remove(section);
                    sections.remove(section);
                }
                notifyDataSetChanged();
                return;
            }
            count += groupedPeople.get(section).size() + 1;
        }
    }


    public PersonAdapter(Map<String, List<Person>> groupedPeople, int total, HomeFragment homeFragment) {
        this.groupedPeople = groupedPeople;
        this.homeFragment = homeFragment;

        this.sections = new ArrayList<>(groupedPeople.keySet());
        Collections.sort(sections, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                if (s1.equals("#")) {
                    return 1;
                } else if (s2.equals("#")) {
                    return -1;
                } else {
                    return s1.compareTo(s2);
                }
            }
        });

        this.total = total;
        Log.d("PersonAdapter", "Total number:" + total);
        Log.d("PersonAdapter", "PersonAdapter: " + groupedPeople);
    }
    public PersonAdapter(Map<String, List<Person>> groupedPeople, int total, NotificationsFragment NotificationsFragment) {
        this.groupedPeople = groupedPeople;
        this.notificationsFragment = NotificationsFragment;

        this.sections = new ArrayList<>(groupedPeople.keySet());
        Collections.sort(sections, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                if (s1.equals("#")) {
                    return 1;
                } else if (s2.equals("#")) {
                    return -1;
                } else {
                    return s1.compareTo(s2);
                }
            }
        });

        this.total = total;
        Log.d("PersonAdapter", "Total number:" + total);
        Log.d("PersonAdapter", "PersonAdapter: " + groupedPeople);
    }

    public void updateData(Map<String, List<Person>> groupedPeople, int total) {
        this.groupedPeople = groupedPeople;
        this.sections = new ArrayList<>(groupedPeople.keySet());
        Collections.sort(sections);
        this.total = total;
        Log.d("PersonAdapter", "Total number:" + total);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
    if (position == getItemCount()-1) {
            return TYPE_FOOTER;
        }

        int count = 0;

        for (String section : sections) {
            if (position == count) {
                return TYPE_HEADER;
            } else if (position < count + groupedPeople.get(section).size() + 1) {
                return TYPE_ITEM;
            }
            count += groupedPeople.get(section).size() + 1;
        }
        return super.getItemViewType(position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_FOOTER){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.footer_item, parent, false);
            return new FooterViewHolder(view);
        }
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.letter_item, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.person_item, parent, false);
            return new PersonViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof PersonViewHolder) {
            PersonViewHolder personViewHolder = (PersonViewHolder) holder;
            final Person person = getPerson(position);
            personViewHolder.favBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("PersonAdapter", "onClick: " + person.getFirstname() + " " + person.isPreferred());
                    person.IsFavorite(true);
                    if (homeFragment != null) {
                        homeFragment.updateState(person);
                    } else if (notificationsFragment != null) {
                        notificationsFragment.updateState(person);
                    }
                    notifyDataSetChanged();
                }
            });
        }
        if (holder instanceof FooterViewHolder){

            ((FooterViewHolder) holder).bind(total);
        }else{
        int count = 0;
        for (String section : sections) {
            if (position == count) {
                ((HeaderViewHolder) holder).bind(section);
                return;
            } else if (position < count + groupedPeople.get(section).size() + 1) {
                ((PersonViewHolder) holder).bind(groupedPeople.get(section).get(position - count - 1));
                return;
            }
            count += groupedPeople.get(section).size() + 1;
        }
    }}

    @Override
    public int getItemCount() {
        int count = 0;
        for (List<Person> people : groupedPeople.values()) {
            count += people.size() + 1;
        }
        return count + 1;
    }

    private static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private TextView headerTextView;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            headerTextView = itemView.findViewById(R.id.letter_text_view);
        }

        public void bind(String headerText) {
            headerTextView.setText(headerText);
        }
    }

    private static class PersonViewHolder extends RecyclerView.ViewHolder {
        private TextView nameTextView;
        private TextView firstnameTextView;

        private ToggleButton favBtn;
        public PersonViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            firstnameTextView = itemView.findViewById(R.id.firstnameTextView);
            favBtn = itemView.findViewById(R.id.toggleButton);
        }

        public void bind(Person person) {

            String name = person.getName();
            nameTextView.setText(name);
            String firstname = person.getFirstname();
            firstnameTextView.setText(firstname);
            favBtn.setChecked(person.isPreferred());
        }
    }

    private static class FooterViewHolder extends RecyclerView.ViewHolder{

        private TextView textViewFooter;
        public FooterViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewFooter = itemView.findViewById(R.id.textViewFooter);
        }
        public void bind(int total){

            Log.d("FooterViewHolder", "bind: " + total);
            if (total == 0)
                textViewFooter.setText("No contacts added yet.");
            else if (total == 1)
                textViewFooter.setText(total + " contact");
            else
                textViewFooter.setText(total + " contacts");
        }
    }
}
