package com.example.a301project;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

/**
 * This class creates a custom array list for ingredient objects
 * contains a constructor and a method that returns a view of the custom list
 */
public class CustomList extends ArrayAdapter<Ingredient> {
    // custom array list containing Ingredient
    private final ArrayList<Ingredient> ingredients;
    private final Context context;

    /**
     * Makes a Custom list from an array list of ingredients
     * @param context {@link Context} context to the array list
     * @param ingredients {@link ArrayList<Ingredient>} array list containing ingredients
     */
    public CustomList(Context context, ArrayList<Ingredient> ingredients) {
        // constructor
        super(context,0,ingredients);
        this.ingredients = ingredients;
        this.context = context;
    }

    /**
     * Method for creating a view that will appear in the ingredient adapter
     * @param position {@link Integer} the position of the current view
     * @param convertView {@link View} the reused view to be retrieved
     * @param parent {@link ViewGroup} the collection of views that contains current view
     * @return a view
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup
            parent) {
        View view = convertView;
        if(view == null){
            view = LayoutInflater.from(context).inflate(R.layout.ingredient_content, parent,false);
        }
        // list view to display attributes of each ingredient object
        // by setting the view text boxes to their ID
        Ingredient ingredient = ingredients.get(position);
        TextView ingredientName = view.findViewById(R.id.i_nameText);
        TextView locationName = view.findViewById(R.id.i_locationText);
        TextView bbdName = view.findViewById(R.id.i_bbdText);
        TextView amountName = view.findViewById(R.id.i_amountText);
        TextView unitName = view.findViewById(R.id.i_unitText);
        TextView categoryName = view.findViewById(R.id.i_categoryText);

        // set the texts of ingredients
        ingredientName.setText(ingredient.getName());
        locationName.setText(ingredient.getLocation());
        bbdName.setText("Expires: " + ingredient.getbbd());
        amountName.setText(ingredient.getAmount().toString());
        unitName.setText(ingredient.getUnit());
        categoryName.setText(ingredient.getCategory());
        return view;
    }
}