package com.example.myapplication;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao

public interface PersonDAO {
    @Insert
    public void insert(Person person);

    @Update
    public void update(Person person);

    @Delete
    public void delete(Person person);

    //delete all
    @Query("delete from Person")
    public void deleteAll();

    @Query("select * from Person order by name, firstname")
    public List<Person> getAllPerson();

    //select only where IsFavorite is true
    @Query("select * from Person where IsFavorite = 1 order by name, firstname")
    public List<Person> getAllFavoritePerson();

    //Select all but display IsFavorite first
    @Query("select * from Person order by IsFavorite desc")
    public List<Person> getAllPersonByFavorite();

    //get total number
    @Query("select count(*) from Person")
    public int getTotal();

    //get total number of favorite
    @Query("select count(*) from Person where IsFavorite = 1")
    public int getTotalFavorite();

    //getpersonbyname
    @Query("select * from Person where firstname = :firstname and name = :name")
    public Person getPersonByName(String firstname, String name);

    //set state of IsFavorite to !IsFavorite
    @Query("update Person set IsFavorite = not IsFavorite where id = :id")
    public void toggleFavorite(int id);

    //delete only favorites
    @Query("delete from Person where IsFavorite = 1")
    public void deleteAllFavorite();

    //unfavorite all
    @Query("update Person set IsFavorite = 0")
    public void unFavoriteAll();

}
