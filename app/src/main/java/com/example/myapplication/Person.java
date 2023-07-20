package com.example.myapplication;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "Person")
public class Person {
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    int id;

    @ColumnInfo(name = "name")
    String name;

    @ColumnInfo(name = "firstname")
    String firstname;

    @ColumnInfo(name = "IsFavorite")
    boolean IsFavorite;

    @Ignore
    public Person() {
    }

    public Person( String name, String firstname, boolean IsFavorite) {

        this.name = name;
        this.firstname = firstname;
        this.IsFavorite = IsFavorite;
    }


    public String getName() {
        return name;
    }
    public String getFirstname() {
        return firstname;

        }
    public boolean isPreferred() {
        return IsFavorite;
    }


    public void IsFavorite(boolean b) {
        this.IsFavorite = b;
    }

    public String getId() {
        return String.valueOf(id);
    }

    public int getIdInt() {
        return id;
    }
}
