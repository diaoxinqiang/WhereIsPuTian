package com.tangentlu.whereisputian;

import com.litesuits.orm.db.annotation.Column;
import com.litesuits.orm.db.annotation.PrimaryKey;
import com.litesuits.orm.db.annotation.Table;
import com.litesuits.orm.db.annotation.Unique;
import com.litesuits.orm.db.enums.AssignType;

/**
 * Created by ChiEr on 16/5/5.
 */
@Table("HospitalItem")
public class HospitalItem {
    public static final String ID = "_id";
    public static final String HOSPITAL_NAME = "_hospital_name";
    public static final String QUERY_HOSPITAL_NAME = "_query_hospital_name";
    public static final String CITY = "_city";
    public static final String LOCATION_X = "_location_x";
    public static final String LOCATION_Y = "_location_y";
    public static final String ADDRESS = "_address";

    @PrimaryKey(AssignType.AUTO_INCREMENT)
    @Column(ID)
    private int DB_id;
    @Unique
    @Column(HOSPITAL_NAME)
    public String hospitalName;
    @Column(QUERY_HOSPITAL_NAME)
    public String queryHospitalName;
    @Column(CITY)
    public String city;
    @Column(LOCATION_X)
    public double location_x;
    @Column(LOCATION_Y)
    public double location_y;
    @Column(ADDRESS)
    public String address;

    public HospitalItem(String hospitalName) {
        this.hospitalName = hospitalName;
    }
}
