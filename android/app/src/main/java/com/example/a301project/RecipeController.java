package com.example.a301project;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * This {@link RecipeController} class allows the {@link RecipeFragment} to communicate with
 * the Firestore database backend. This class contains methods to add or remove {@link Recipe} objects to the
 * database, as well as edit functionality.
 *
 * This class should be used exclusively by the {@link RecipeFragment} class to handle database communication.
 */
@SuppressWarnings("unchecked")
public class RecipeController {
    private final FirebaseFirestore db;
    private final CollectionReference collectionReference;

    /**
     * The constructor for the {@link RecipeController}. Sets up the {@link #db} and {@link #collectionReference}
     */
    public RecipeController() {
        this.db = FirebaseFirestore.getInstance();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user.getEmail() != null;
        String collectionName = "Recipe";
        collectionReference = db.collection("User").document(user.getEmail()).collection(collectionName);

    }

    /**
     * Constructor for injecting a db for testing purposes
     * @param db
     */
    public RecipeController(FirebaseFirestore db) {
        this.db = db;
        this.collectionReference = db.collection("Recipe");
    }

    /**
     * Method to add an {@link Recipe} to the Firebase database
     * @param recipe This is the {@link Recipe} to be added to Firebase
     */
    public void addRecipe(Recipe recipe) {
        // get all required values
        String title = recipe.getTitle();
        String category = recipe.getCategory();
        String comments = recipe.getComments();
        Long prepTime = recipe.getPrepTime();
        Long servings = recipe.getServings();
        String photo = recipe.getPhoto();
        ArrayList<Ingredient> ingredients = recipe.getIngredients();

        // put all the values into hashmap
        HashMap<String, Object> data = new HashMap<>();
        data.put("Title", title);
        data.put("Category", category);
        data.put("Comments", comments);
        data.put("Ingredients", ingredients);
        data.put("Photo", photo);
        data.put("PrepTime", prepTime);
        data.put("Servings", servings);

        // collection reference
        collectionReference
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    /**
                     * Method invoked when successfully added to database
                     * @param documentReference {@link DocumentReference} reference to document
                     */
                    public void onSuccess(DocumentReference documentReference) {
                        String id = documentReference.getId();
                        Log.d("Added", "Added document with ID: "+ id);
                        recipe.setId(id);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    /**
                     * Method invoked when failed to add to database
                     * @param e {@link Exception} the error that occured
                     */
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("ERROR", "Error adding document", e);
                    }
                });
    }
    /**
     * Method to remove an {@link Recipe} from Firebase using its ID
     * @param recipe This is the {@link Recipe} to be removed from Firebase
     */
    public void removeRecipe(Recipe recipe) {
        String id = recipe.getId();
        collectionReference.document(id)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("ContentValues", "Successfully deleted recipe with ID: " + id);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("ContentValues", "Could not delete document with ID: " + id, e);
                    }
                });
    }

    /**
     * Gets all recipes from Firebase
     *
     * @param s successHandler function to be called on success with
     *          the ArrayList of Recipes
     */
    public void getRecipes(successHandler s) {
        collectionReference.get().addOnSuccessListener(queryDocumentSnapshots -> {
            ArrayList<Recipe> res = new ArrayList<>();

            queryDocumentSnapshots.forEach(doc -> {
                ArrayList<Ingredient> ingredients = new ArrayList<>();

                ArrayList<Map<String, Object>> objects = (ArrayList<Map<String, Object>>) doc.get("Ingredients");
                objects.forEach(o -> {
                    ingredients.add(new Ingredient((String) o.get("name"), ((Number) o.get("amount")).doubleValue()));
                });

                Recipe r = new Recipe(
                        doc.getString("Title"),
                        doc.getString("Category"),
                        doc.getString("Comments"),
                        doc.getString("Photo"),
                        doc.getLong("PrepTime"),
                        doc.getLong("Servings"),
                        ingredients
                );
                r.setId(doc.getId());
                res.add(r);
            });
            s.f(res);
        });
    }

    public interface successHandler {
        void f(ArrayList<Recipe> r);
    }

    /**
     * Notifies the Firestore database of an update to an recipe. The database then updates the
     * recipe's values with the provided {@link Recipe} object
     * @param recipe The {@link Recipe} object to update in the database
     */
    public void notifyUpdate(Recipe recipe) {
        Map<String,Object> userMap = new HashMap<>();
        userMap.put("Title",recipe.getTitle());
        userMap.put("Category",recipe.getCategory());
        userMap.put("Comments",recipe.getComments());
        userMap.put("Ingredients",recipe.getIngredients());
        userMap.put("Photo", recipe.getPhoto());
        userMap.put("Servings", recipe.getServings());
        userMap.put("PrepTime", recipe.getPrepTime());
        String id = recipe.getId();
        collectionReference
            .document(id)
            .update(userMap);
    }
}
