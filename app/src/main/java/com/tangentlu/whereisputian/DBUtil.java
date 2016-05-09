package com.tangentlu.whereisputian;

import com.litesuits.orm.LiteOrm;
import com.litesuits.orm.db.assit.QueryBuilder;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by ChiEr on 16/5/5.
 */
public class DBUtil {
    static LiteOrm liteOrm;

    public static void initDBUtil() {
        String DB_NAME = "hospitalList";
        liteOrm = LiteOrm.newSingleInstance(APP.getAppContext(), DB_NAME + ".db");
    }

    public static <W> void save(W w) {
        liteOrm.save(w);
    }
    public static <W> void delete(W w) {
        liteOrm.delete(w);
    }

    public static <W> void saveItem(Collection<W> W) {
        liteOrm.save(W);
    }
    public static <W> void delete(Collection<W> W) {
        liteOrm.delete(W);
    }

    public static <T> ArrayList<T> getItemsByCity(Class<T> tClass, String city) {
        QueryBuilder qb = new QueryBuilder(tClass).where(HospitalItem.CITY + " = ? ", new String[]{city});
        return liteOrm.query(qb);
    }

//    public static <T> ArrayList<T> getItemsByHospitalName(Class<T> tClass, String hospitalName) {
//        QueryBuilder qb = new QueryBuilder(tClass).where(HospitalItem.HOSPITAL_NAME + " = ? ", new String[]{hospitalName});
//        return liteOrm.query(qb);
//    }
    public static <T> ArrayList<T> getItemsByHospitalName(Class<T> tClass, String hospitalName) {
        QueryBuilder qb = new QueryBuilder(tClass).where(HospitalItem.HOSPITAL_NAME + " LIKE ? ", new String[]{hospitalName});
        return liteOrm.query(qb);
    }

    public static <T> ArrayList<T> getAllItem(Class<T> tClass) {
        return liteOrm.query(tClass);
    }
}
