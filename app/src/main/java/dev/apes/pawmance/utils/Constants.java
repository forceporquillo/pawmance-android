package dev.apes.pawmance.utils;

import java.util.HashMap;

public class Constants {

  public static final String[] BREEDS = new String[] {
      "German Shepherd", "Golden Retriever",
      "American Bully", "Bulldog", "Husky"
  };

  public static final String[] MONTHS = new String[] {
      "January", "February", "March", "April", "May", "June",
      "July", "August", "September", "October", "November", "December"
  };

  public static String[]  PREFERENCES  = new String[] {
      "With Papers", "Without Papers"
  };

  public static String[] GENDER = new String[] {
      "Male", "Female"
  };

  public static final HashMap<Integer, String> MONTHS_MAP =
      new HashMap<Integer, String>() {
        {
          put(1, "January");
          put(2, "February");
          put(3, "March");
          put(4, "April");
          put(5, "May");
          put(6, "June");
          put(7, "July");
          put(8, "August");
          put(9, "September");
          put(10, "October");
          put(11, "November");
          put(12, "December");
        }
      };

  public static final String[] YEARS = new String[] {
      "2012", "2013", "2014", "2015", "2016", "2017", "2018", "2019", "2020", "2021"
  };
}
