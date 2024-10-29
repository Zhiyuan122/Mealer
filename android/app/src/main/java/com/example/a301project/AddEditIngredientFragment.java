package com.example.a301project;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * A class for a fragment that handles adding and editing ingredients
 * Fragment is activated when user clicks certain buttons
 */
@SuppressWarnings({"SpellCheckingInspection", "UnusedAssignment", "unchecked"})
public class AddEditIngredientFragment extends DialogFragment {
    // fragment used for adding and editing an ingredient
    private EditText ingredientName;
    private EditText amountName;
    private Spinner locationName;
    private Spinner unitName;
    private EditText bbdName;
    private Spinner categoryName;
    private OnFragmentInteractionListener listener;
    private DatePickerDialog.OnDateSetListener setListener;
    private Ingredient currentIngredient;
    private boolean createNewIngredient;
    private ArrayList<CharSequence> unitOptions;
    private ArrayList<CharSequence> categoryOptions;
    private ArrayList<CharSequence> locationOptions;
    private AddEditIngredientController addEditIngredientController;
    private Resources res;
    private String TAG;

    /**
     * Method that responds when the fragment has been interacted with
     * OnConfirmPressed either creates a new Ingredient or updates an existing one based on boolean createNewIngredient
     */
    public interface OnFragmentInteractionListener {
        void onConfirmPressed(Ingredient currentIngredient, boolean createNewIngredient);
    }

    /**
     * Called on start to set make the dialog fullscreen
     */
    @Override
    public void onStart()
    {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null)
        {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    /**
     * Method to set the fragment attributes
     * Sets the information of current ingredient if the tag is EDIT
     * Sets empty EditText views if the tag is ADD, and hides delete button
     * Creates new ingredient or resets information of current ingredient based on the tag
     * @param savedInstanceState {@link Bundle} the last saved instance state of fragment, NULL if
     *                                         fragment is newly created
     * @return dialog fragment with the appropriate fields
     */
    @Override
    @NonNull
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.add_edit_ingredientlayout, null);

        // get the current date as the default in date picker
        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);

        Bundle bundle = getArguments();
        if (bundle != null) {
            currentIngredient = (Ingredient) bundle.get("ingredient");
            createNewIngredient = (boolean) bundle.get("createNew");
        }

        // set variables of EditText fields
        ingredientName = view.findViewById(R.id.edit_name);
        bbdName = view.findViewById(R.id.edit_bbd);
        locationName = view.findViewById(R.id.edit_location);
        amountName = view.findViewById(R.id.edit_amount);
        unitName = view.findViewById(R.id.edit_unit);
        categoryName = view.findViewById(R.id.edit_category);
        Button deleteButton = view.findViewById(R.id.delete_ingredient_button);

        unitOptions = new ArrayList<>();
        categoryOptions = new ArrayList<>();
        locationOptions = new ArrayList<>();
        res = getActivity().getResources();

        addEditIngredientController = new AddEditIngredientController();
        DocumentReference documentReference = addEditIngredientController.getDocumentReference();

        // max length of 10 characters
        InputFilter[] filterArray = new InputFilter[2];
        filterArray[0] = new InputFilter.LengthFilter(10);
        // only a-zA-Z and spaces
        filterArray[1] = (source, start, end, dest, dstart, dend) -> {
            if (source.equals("")) {
                return source;
            }
            if (source.toString().matches("[a-zA-Z ]+")) {
                return source;
            }
            return source.subSequence(start, end-1);
        };

        TAG = this.getTag();

        // sets title of the fragment depending on whether the tag is ADD or EDIT or SHOPPING
        String title = "";
        if (TAG.equalsIgnoreCase("ADD")) {
            title = "Add Entry";
            deleteButton.setVisibility(View.GONE);
        }
        else if (TAG.equalsIgnoreCase("EDIT")) {
            title = "Edit Entry";
        } else if (TAG.equalsIgnoreCase("SHOPPING")) {
            // if adding an ingredient from the shoppping list
            title = "Purchase";
            ingredientName.setEnabled(false);

            // if the values are already set -> then don't allow editing
            if (currentIngredient.getUnit() != "null") {
                unitName.setEnabled(false);
            }
            if (currentIngredient.getCategory() != "null") {
                categoryName.setEnabled(false);
            }

            // set prompts for bbd and location
            bbdName.setHint("Select a date");
            locationName.setPrompt("Select a location");

            // since this is adding an ingredient -> remove the delete button
            deleteButton.setVisibility(View.GONE);
        }


        deleteButton.setOnClickListener(new View.OnClickListener() {
            /**
             * Method for when Delete button is clicked
             * Another fragment pops up to confirm whether user meant to delete
             * @param view {@link View} the view of the fragment that was clicked
             */
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("Are you sure you want to delete this ingredient?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                IngredientController controller = new IngredientController();
                                controller.removeIngredient(currentIngredient);

                                Fragment frag = getParentFragmentManager().findFragmentByTag("EDIT");
                                getParentFragmentManager().beginTransaction().remove(frag).commit();
                                Toast.makeText(getContext(), "Ingredient Delete Successful", Toast.LENGTH_LONG).show();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            /**
                             * Method for when negative button is clicked in delete fragment
                             * @param dialog {@link DialogInterface} the interface of this pop up fragment
                             * @param id {@link Integer} ID of the recipe to be deleted
                             */
                            public void onClick(DialogInterface dialog, int id) {
                                // if No is pressed, return to Edit fragment
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        // Category spinner and create a new category adapter for the spinner
        ArrayAdapter<CharSequence> categoryAdapter = new ArrayAdapter<>(this.getContext(), com.google.android.material.R.layout.support_simple_spinner_dropdown_item, categoryOptions);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categoryName.setAdapter(categoryAdapter);
        categoryName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            /**
             * Method invoked when a category in this view has been selected
             * @param adapterView {@link AdapterView} the AdapterView where the selection happened
             * @param view {@link View} the view that was clicked
             * @param i {@link Integer} position of the view in the adapter
             * @param l {@link Long} the row ID of the item that was selected
             */
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (categoryAdapter.getItem(i).equals("Add Category")) {

                    // create the edit text and set constraints
                    EditText customCategory = new EditText(getContext());
                    customCategory.setHint("Only letters allowed");
                    customCategory.setFilters(filterArray);

                    // build the alert dialog -> which will prompt the user to enter a new category
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setView(customCategory);
                    builder.setMessage("Enter custom category")
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();

                                    // set the category back to the what it was before
                                    categoryName.setSelection(categoryAdapter.getPosition(currentIngredient.getCategory()));
                                }
                            })
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                        /**
                         * When the 'OK' button is clicked on the Alert Dialog which prompts user to enter a new category
                         * @param v: The {@link View} - which is the 'OK' button
                         */
                        @Override
                        public void onClick(View v) {
                            // get the user input -> and check that it is not empty
                            String newCategory = customCategory.getText().toString().trim();
                            if (newCategory.isEmpty()) {
                                customCategory.setError("Can't be empty");
                                return;
                            }

                            // if the category is not empty -> check if it already exist
                            Iterator<CharSequence> listIterator = categoryOptions.iterator();
                            boolean exists = false;
                            while (listIterator.hasNext()) {
                                String nextValue = listIterator.next().toString();
                                if (nextValue.equalsIgnoreCase(newCategory)) {
                                    exists = true;
                                    newCategory = nextValue;
                                }
                            }

                            // if the category doesn't already exist -> add the data
                            if (!exists) {
                                int size = categoryOptions.size();
                                categoryOptions.add(size-1, newCategory);
                                categoryAdapter.notifyDataSetChanged();

                                // add the data to firebase
                                addEditIngredientController.addIngredientCategory(newCategory);
                            }

                            // select the spinner value
                            int j = categoryAdapter.getPosition(newCategory);
                            categoryName.setSelection(j);
                            currentIngredient.setCategory(newCategory);

                            // close the dialog
                            dialog.dismiss();
                        }
                    });

                }
                else {
                    // user didn't select the add custom option
                    currentIngredient.setCategory(categoryAdapter.getItem(i).toString());
                }
            }

            /**
             * Method invoked when nothing is selected
             * selection disappears from the view
             * @param adapterView {@link AdapterView} the AdapterView that contains no selected item
             */
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // nothing happens
            }
        });

        // Location spinner
        ArrayAdapter<CharSequence> locationAdapter = new ArrayAdapter<>(this.getContext(), com.google.android.material.R.layout.support_simple_spinner_dropdown_item, locationOptions);
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationName.setAdapter(locationAdapter);
        locationName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            /**
             * Method invoked when a location in this view has been selected
             * @param adapterView {@link AdapterView} the AdapterView where the selection happened
             * @param view {@link View} the view that was clicked
             * @param i {@link Integer} position of the view in the adapter
             * @param l {@link Long} the row ID of the item that was selected
             */
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (locationAdapter.getItem(i).equals("Add Location")) {

                    // create the edit text and set constraints
                    EditText customLocation = new EditText(getContext());
                    customLocation.setHint("Only letters allowed");
                    customLocation.setFilters(filterArray);

                    // build the alert dialog -> which will prompt the user to enter a new location
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setView(customLocation);
                    builder.setMessage("Enter custom location")
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();

                                    // set the location back to the what it was before
                                    locationName.setSelection(locationAdapter.getPosition(currentIngredient.getLocation()));
                                }
                            })
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                        /**
                         * When the 'OK' button is clicked on the Alert Dialog which prompts user to enter a new location
                         * @param v: The {@link View} - which is the 'OK' button
                         */
                        @Override
                        public void onClick(View v) {
                            // get the user input -> and check that it is not empty
                            String newLocation = customLocation.getText().toString().trim();
                            if (newLocation.isEmpty()) {
                                customLocation.setError("Can't be empty");
                                return;
                            }

                            // if the location is not empty -> check if it already exist
                            Iterator<CharSequence> listIterator = locationOptions.iterator();
                            boolean exists = false;
                            while (listIterator.hasNext()) {
                                String nextValue = listIterator.next().toString();
                                if (nextValue.equalsIgnoreCase(newLocation)) {
                                    exists = true;
                                    newLocation = nextValue;
                                }
                            }

                            // if the location doesn't already exist -> add the data
                            if (!exists) {
                                int size = locationOptions.size();
                                locationOptions.add(size-1, newLocation);
                                locationAdapter.notifyDataSetChanged();

                                // add the data to firebase
                                addEditIngredientController.addIngredientLocation(newLocation);
                            }

                            // select the spinner value
                            int j = locationAdapter.getPosition(newLocation);
                            locationName.setSelection(j);
                            currentIngredient.setLocation(newLocation);

                            // close the dialog
                            dialog.dismiss();
                        }
                    });

                }
                else {
                    // user didn't select the add custom option
                    currentIngredient.setLocation(locationAdapter.getItem(i).toString());
                }
            }

            /**
             * Method invoked when nothing is selected
             * selection disappears from the view
             * @param adapterView {@link AdapterView} the AdapterView that contains no selected item
             */
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // nothing happens
            }
        });


        // Unit spinner
        ArrayAdapter<CharSequence> unitAdapter = new ArrayAdapter<>(this.getContext(), com.google.android.material.R.layout.support_simple_spinner_dropdown_item, unitOptions);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        unitName.setAdapter(unitAdapter);
        unitName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            /**
             * Method invoked when a unit in this view has been selected
             * @param adapterView {@link AdapterView} the AdapterView where the selection happened
             * @param view {@link View} the view that was clicked
             * @param i {@link Integer} position of the view in the adapter
             * @param l {@link Long} the row ID of the item that was selected
             */
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (unitAdapter.getItem(i).equals("Add Unit")) {

                    // create the edit text and set constraints
                    EditText customUnit = new EditText(getContext());
                    customUnit.setHint("Only letters allowed");
                    customUnit.setFilters(filterArray);

                    // build the alert dialog -> which will prompt the user to enter a new unit
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setView(customUnit);
                    builder.setMessage("Enter custom unit")
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();

                                    // set the category back to the what it was before
                                    unitName.setSelection(unitAdapter.getPosition(currentIngredient.getUnit()));
                                }
                            })
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                        /**
                         * When the 'OK' button is clicked on the Alert Dialog which prompts user to enter a new location
                         * @param v: The {@link View} - which is the 'OK' button
                         */
                        @Override
                        public void onClick(View v) {
                            // get the user input -> and check that it is not empty
                            String newUnit = customUnit.getText().toString().trim();
                            if (newUnit.isEmpty()) {
                                customUnit.setError("Can't be empty");
                                return;
                            }

                            // if the unit is not empty -> check if it already exist
                            Iterator<CharSequence> listIterator = unitOptions.iterator();
                            boolean exists = false;
                            while (listIterator.hasNext()) {
                                String nextValue = listIterator.next().toString();
                                if (nextValue.equalsIgnoreCase(newUnit)) {
                                    exists = true;
                                    newUnit = nextValue;
                                }
                            }

                            // if the unit doesn't already exist -> add the data
                            if (!exists) {
                                int size = unitOptions.size();
                                unitOptions.add(size-1, newUnit);
                                unitAdapter.notifyDataSetChanged();

                                // add the data to firebase
                                addEditIngredientController.addIngredientUnit(newUnit);
                            }

                            // select the spinner value
                            int j = unitAdapter.getPosition(newUnit);
                            unitName.setSelection(j);
                            currentIngredient.setUnit(newUnit);

                            // close the dialog
                            dialog.dismiss();
                        }
                    });

                }
                else {
                    // user didn't select the add custom option
                    currentIngredient.setUnit(unitAdapter.getItem(i).toString());
                }
            }

            /**
             * Method invoked when nothing is selected
             * selection disappears from the view
             * @param adapterView {@link AdapterView} the AdapterView that contains no selected item
             */
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // nothing happens
            }
        });

        // gets the spinner values from FireBase
        Task<DocumentSnapshot> documentSnapshot = documentReference.get();
        documentSnapshot.addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Map<String, Object> result = task.getResult().getData();

                    // add the default spinner values and get the custom categories from firebase (if they exist)
                    List<CharSequence> defaultCategories = List.of(res.getStringArray(R.array.category_array));
                    categoryOptions.addAll(defaultCategories);
                    if (result != null && result.containsKey("IngredientCategories")) {
                        categoryOptions.addAll(categoryOptions.size()-1,(ArrayList<CharSequence>) result.get("IngredientCategories"));
                    }
                    categoryAdapter.notifyDataSetChanged();

                    // add the default spinner values and get the custom locations from firebase (if they exist)
                    List<CharSequence> defaultLocations = List.of(res.getStringArray(R.array.location_array));
                    locationOptions.addAll(defaultLocations);
                    if (result != null && result.containsKey("IngredientLocations")) {
                        locationOptions.addAll(locationOptions.size()-1, (ArrayList<CharSequence>) result.get("IngredientLocations"));
                    }
                    locationAdapter.notifyDataSetChanged();

                    // add the default spinner values and get the custom units from firebase (if they exist)
                    List<CharSequence> defaultUnits = List.of(res.getStringArray(R.array.units_array));
                    unitOptions.addAll(defaultUnits);
                    if (result != null && result.containsKey("IngredientUnits")) {
                        unitOptions.addAll(unitOptions.size()-1, ((ArrayList<CharSequence>) result.get("IngredientUnits")));
                    }
                    unitAdapter.notifyDataSetChanged();

                    // set the spinners at the correct value for the current ingredient
                    locationName.setSelection(locationAdapter.getPosition(currentIngredient.getLocation()));
                    unitName.setSelection(unitAdapter.getPosition(currentIngredient.getUnit()));
                    categoryName.setSelection(categoryAdapter.getPosition(currentIngredient.getCategory()));
                }
            }
        });

        
        bbdName.setOnClickListener(new View.OnClickListener() {
            /**
             * Method invoked when the view is clicked
             * shows date picker
             * @param view {@link View} the view that contains the selected date
             */
            @Override
            public void onClick(View view) {
                DatePickerDialog datePicker = new DatePickerDialog(
                        getActivity(), android.R.style.Theme_Holo_Light_Dialog_MinWidth, setListener, year, month, day);
                datePicker.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                datePicker.show();
            }
        });

        setListener = new DatePickerDialog.OnDateSetListener() {
            /**
             * Method invoked a date is selected
             * sets the selected date as the best before date for this ingredient
             * @param datePicker {@link DatePicker} the date picker in view
             * @param year {@link Integer}  the year selected
             * @param month {@link Integer} the month selected
             * @param dayOfMonth {@link Integer} the day selected
             */
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                month = month + 1;
                @SuppressLint("DefaultLocale") String date = year + "-" + String.format("%02d", month) + "-" + String.format("%02d", dayOfMonth);
                bbdName.setText(date);
            }
        };

        // set EditText boxes to the specific fields of the current selected Food
        ingredientName.setText(currentIngredient.getName());
        bbdName.setText(currentIngredient.getbbd());
        amountName.setText(currentIngredient.getAmount().toString());
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder
                .setView(view)
                .setTitle(title)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    /**
                     * Method for getting and setting attributes of current ingredient
                     * @param dialogInterface {@link DialogInterface} the dialog interface of this fragment
                     * @param i {@link Integer} ID of the selected item
                     */
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                AlertDialog alertDialog = (AlertDialog) dialog;
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // retrieve text from the text boxes
                        String ingredientName = AddEditIngredientFragment.this.ingredientName.getText().toString();
                        @SuppressWarnings("SpellCheckingInspection") String bestbefore = AddEditIngredientFragment.this.bbdName.getText().toString();
                        String amount = AddEditIngredientFragment.this.amountName.getText().toString();
                        double doubleAmount = 0.0;

                        // check if any field is empty
                        // if empty, reject add - reject add and show error message
                        boolean hasEmpty = ingredientName.isEmpty() || bestbefore.isEmpty() || amount.isEmpty();
                        if (ingredientName.isEmpty()) {
                            AddEditIngredientFragment.this.ingredientName.setError("Can't be empty");
                        }
                        if (bestbefore.isEmpty()) {
                            AddEditIngredientFragment.this.bbdName.setError("Can't be empty");
                        }
                        if (amount.isEmpty()) {
                            AddEditIngredientFragment.this.amountName.setError("Can't be empty");
                        }

                        if (hasEmpty) {
                            return;
                        } else {
                            amount = String.format("%.1f", Double.parseDouble(amount));
                            doubleAmount = Double.parseDouble(amount);
                        }

                        // set the name of the current food as the edited fields
                        currentIngredient.setName(ingredientName);
                        currentIngredient.setBbd(bestbefore);
                        currentIngredient.setAmount(doubleAmount);

                        listener.onConfirmPressed(currentIngredient, createNewIngredient);

                        // close the dialog
                        dialog.dismiss();
                    }
                });
                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // if the user presses cancel button when adding from Shopping List
                        if (TAG.equalsIgnoreCase("SHOPPING")) {
                            // build the alert dialog -> this will warn user that if they cancel -> it won't be purchased
                            TextView alertMessage = new TextView(getContext());
                            alertMessage.setTextSize(18);
                            alertMessage.setPadding(10, 0, 10, 0);
                            alertMessage.setText("You are cancelling the purchase. Press 'CANCEL' to go back and continue the purchase");
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setView(alertMessage);
                            builder.setMessage("WARNING")
                                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.cancel();
                                        }
                                    })
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialog.dismiss();
                                        }
                                    });
                            AlertDialog alertDialog1 = builder.create();
                            alertDialog1.show();
                        } else {
                            dialog.dismiss();
                        }
                    }
                });
            }
        });
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        return dialog;
    }



    /**
     * Method to create a new AddEditRecipe fragment
     * @param ingredient {@link Ingredient} the current ingredient
     * @param createNew {@link boolean} variable that indicates whether to create a new ingredient
     * @return An Add/Edit Ingredient fragment
     */
    static AddEditIngredientFragment newInstance(Ingredient ingredient, boolean createNew, OnFragmentInteractionListener listener) {
        Bundle args = new Bundle();
        args.putSerializable("ingredient",ingredient);
        args.putSerializable("createNew", createNew);

        AddEditIngredientFragment fragment = new AddEditIngredientFragment();
        fragment.setArguments(args);

        fragment.listener = listener;
        return fragment;
    }
}

