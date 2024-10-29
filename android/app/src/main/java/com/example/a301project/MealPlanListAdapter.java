package com.example.a301project;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Class for an ArrayAdapter that renders MealPlan objects for use
 * by a ListView in MealPlanFragment
 */
public class MealPlanListAdapter extends ArrayAdapter<MealPlan> {
    private final ArrayList<MealPlan> mealPlans;
    private final Context context;

    /**
     * Constructor for the adapter, takes context and ArrayList of MealPlan
     * @param context {@link Context} context of the layout to render
     * @param mealPlans {@link ArrayList} array of MealPlan to render
     */
    public MealPlanListAdapter(@NonNull Context context, ArrayList<MealPlan> mealPlans) {
        super(context, 0, mealPlans);
        this.mealPlans = mealPlans;
        this.context = context;
    }

    /**
     * Method for creating a view that will appear in the MealPlan adapter
     * @param position {@link Integer} the position of the current view
     * @param convertView {@link View} the reused view to be retrieved
     * @param parent {@link ViewGroup} the collection of views that contains current view
     * @return a view
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        MealPlan mp = mealPlans.get(position);

        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.meal_plan_layout, parent, false);
        }

        // Find text views and list views for meal plan
        TextView name = view.findViewById(R.id.mp_nameText);
        TextView startDate = view.findViewById(R.id.startDateTextView);
        TextView endDate = view.findViewById(R.id.endDateTextView);
        TextView nIngredients = view.findViewById(R.id.nIngredients);
        TextView nRecipes = view.findViewById(R.id.nRecipes);

//        ListView ingrientList = view.findViewById(R.id.mp_ingredientList);
//        ListView recipeList = view.findViewById(R.id.mp_recipeList);

        // set the text to each field
        name.setText(mp.getName());
        startDate.setText(mp.getStartDate());
        endDate.setText(mp.getEndDate());

        int ingredientSize =  mp.getIngredients().size();
        nIngredients.setText(ingredientSize + " Ingredient" + (ingredientSize > 1 ? "s" : ""));
        int recipeSize = mp.getRecipes().size();
        nRecipes.setText(recipeSize + " Recipe" + (recipeSize > 1 ? "s" : ""));

        return view;
    }
}
