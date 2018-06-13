package com.kbaquri.gaze.First;

import com.kbaquri.gaze.R;

public final class CategoryList {

    private CategoryList(){

    }

    public static final Category[] CATEGORIES = {

            new Category("Restaurant", "restaurant", R.drawable.resta),
            new Category("Movie Theater", "movie_theater", R.drawable.movc),
            new Category("Mall", "shopping_mall", R.drawable.shopc),
            new Category("Hospital", "hospital", R.drawable.hospa),
            new Category("Gas Station", "gas_station", R.drawable.petb),
            new Category("Mosque", "mosque", R.drawable.mosa),
            new Category("ATM", "atm", R.drawable.atmc)
    };

}
