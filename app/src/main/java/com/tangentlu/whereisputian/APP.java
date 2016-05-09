package com.tangentlu.whereisputian;

import android.app.Application;
import android.content.Context;

import com.litesuits.orm.db.utils.DataUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import im.fir.sdk.FIR;

/**
 * Created by ChiEr on 16/5/4.
 */
public class APP extends Application {
    static Context context ;

    public void onCreate() {
        super.onCreate();
        FIR.init(this);
        context = getApplicationContext();
        String DATABASES_DIR = "/data/data/" + getPackageName() + "/databases/";
        copyFilesFromAssets(context, "data", DATABASES_DIR);
        DBUtil.initDBUtil();
//        createDB();
    }

    private void createDB() {
        if (DBUtil.getAllItem(HospitalItem.class).size()==0){
            String s=DataUtils.s;
            String[] s2=s.split("#");
            List<HospitalItem> hl = new ArrayList<>();
            for (int i=0;i<s2.length;i++) {
                String[] s3 = s2[i].split(",");
                String[] s4 = s3[1].split("&&");
                for(int i2=0;i2<s4.length;i2++) {
                    HospitalItem h=new HospitalItem(s4[i2]);
                    h.city = s3[0];
                    hl.add(h);
                }
            }
            DBUtil.saveItem(hl);
        }
    }

    public static Context getAppContext() {
        return context;
    }

    public static void copyFilesFromAssets(Context context, String oldPath, String newPath) {
        try {

            // 获取assets目录下的所有文件及目录名
            String fileNames[] = context.getAssets().list(oldPath);

            // 如果是目录名，则将重复调用方法递归地将所有文件
            if (fileNames.length > 0) {
                File file = new File(newPath);
                file.mkdirs();
                for (String fileName : fileNames) {
                    copyFilesFromAssets(context, oldPath + "/" + fileName, newPath + "/" + fileName);
                }
            }
            // 如果是文件，则循环从输入流读取字节写入
            else {
                InputStream is = context.getAssets().open(oldPath);
                FileOutputStream fos = new FileOutputStream(new File(newPath));
                byte[] buffer = new byte[1024];
                int byteCount = 0;
                while ((byteCount = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, byteCount);
                }
                fos.flush();
                is.close();
                fos.close();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


}
