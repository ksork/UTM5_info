package com.example.UTM5_info;

/**
 * Created by k on 25.07.14.
 */
public class Day {
    public static String getNumEnding(String num){
        String ending;
        String[] endings = {"день", "дня", "дней"};
        int inum = Integer.parseInt(num);

        int i = inum % 100;
        if(i>10 && i<20 ){
            ending = endings[2];
        }else {
            i = inum % 10;
            switch (i){
                case 1:
                    ending = endings[0];
                    break;
                case 4:
                    ending = endings[1];
                    break;
                default:
                    ending = endings[2];
            }
        }

        return ending;
    }
}
