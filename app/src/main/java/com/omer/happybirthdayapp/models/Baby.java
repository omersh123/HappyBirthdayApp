package com.omer.happybirthdayapp.models;

import com.pixplicity.easyprefs.library.Prefs;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class Baby {

    private static final String TEST_INSTANCE_ID = "0";
    private static final Date NO_BIRTHDAY = new Date(0);

    private String name = "";
    private Date birthday = NO_BIRTHDAY;
    private String imageUrl = "";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isValid(){
        return !name.isEmpty() && !isBirthdayEmpty();
    }

    public boolean isBirthdayEmpty(){
        return birthday.equals(NO_BIRTHDAY);
    }

    public void load() throws JSONException {
        loadLocalInstanceModelFromLocalDisk(this,TEST_INSTANCE_ID);
    }

    public void save() throws JSONException {
        saveLocalInstanceModelFromLocalDisk(this,TEST_INSTANCE_ID);
    }

    private static final String NAME = "name";
    private static final String BIRTHDAY = "birthday";
    private static final String IMAGE_URL = "image_url";

    private static void loadLocalInstanceModelFromLocalDisk(Baby baby, String id) throws JSONException {
        String savedData = Prefs.getString(id,"");
        JSONObject jo = new JSONObject(savedData);
        if (jo.has(NAME) && !jo.isNull(NAME))
            baby.name = jo.getString(NAME);
        if (jo.has(BIRTHDAY) && !jo.isNull(BIRTHDAY))
            baby.birthday = new Date(jo.getLong(BIRTHDAY));
        if (jo.has(IMAGE_URL) && !jo.isNull(IMAGE_URL))
            baby.imageUrl = jo.getString(IMAGE_URL);

    }

    private static void saveLocalInstanceModelFromLocalDisk(Baby baby, String id) throws JSONException {
        JSONObject jo = new JSONObject();
        jo.put(NAME, baby.name);
        jo.put(BIRTHDAY, baby.birthday.getTime());
        jo.put(IMAGE_URL, baby.imageUrl);
        Prefs.putString(id, jo.toString());
    }
}
