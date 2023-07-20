package com.example.myapplication;

import androidx.room.Database;
import androidx.room.RoomDatabase;


@Database(entities = {Person.class}, version = 1)
public abstract class PersonDatabase extends RoomDatabase {

    public abstract PersonDAO personDAO();
    public boolean personExists(String firstName, String lastName) {
        PersonDAO personDAO = personDAO(); // Get the PersonDAO instance

        // Check if a person with the given first name and last name exists in the database
        return personDAO.getPersonByName(firstName, lastName) != null;
    }
}
