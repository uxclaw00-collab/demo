package com.example.util

import com.example.model.IngredientSubstitution
import com.example.model.Recipe
import com.example.model.SubstitutionOption

object SubstitutionProvider {

    val allSubstitutions: List<IngredientSubstitution> = listOf(
        IngredientSubstitution(
            originalIngredient = "Butter",
            category = "Dairy & Fats",
            defaultSubstitutes = listOf(
                SubstitutionOption(
                    substituteName = "Extra Virgin Olive Oil",
                    ratio = "3/4 cup oil per 1 cup butter (3:4 ratio)",
                    note = "Ideal for sautéing, roasting, and savory pan cooking; adds healthy heart monounsaturated fats.",
                    dietaryTag = "Vegan & Dairy-Free"
                ),
                SubstitutionOption(
                    substituteName = "Coconut Oil (Solid/Melted)",
                    ratio = "1:1 ratio",
                    note = "Great for baking and high-heat searing with mild tropical sweetness.",
                    dietaryTag = "Vegan"
                ),
                SubstitutionOption(
                    substituteName = "Plain Greek Yogurt",
                    ratio = "1:1 ratio in baking/sauces",
                    note = "Cuts fat while boosting protein and moisture in baked goods and creamy dressings.",
                    dietaryTag = "High-Protein"
                ),
                SubstitutionOption(
                    substituteName = "Ghee (Clarified Butter)",
                    ratio = "1:1 ratio",
                    note = "Lactose-free, nutty aroma, extremely high smoke point (485°F).",
                    dietaryTag = "Keto"
                )
            )
        ),
        IngredientSubstitution(
            originalIngredient = "Sour Cream",
            category = "Dairy & Eggs",
            defaultSubstitutes = listOf(
                SubstitutionOption(
                    substituteName = "Plain Greek Yogurt",
                    ratio = "1:1 ratio",
                    note = "Virtually identical texture and tang with double the protein and significantly less fat.",
                    dietaryTag = "High-Protein"
                ),
                SubstitutionOption(
                    substituteName = "Coconut Cream + 1 tsp Lemon Juice",
                    ratio = "1:1 ratio",
                    note = "Rich, plant-based alternative that mimics the luscious thickness.",
                    dietaryTag = "Vegan & Dairy-Free"
                ),
                SubstitutionOption(
                    substituteName = "Blended Cottage Cheese + Lemon",
                    ratio = "1:1 ratio",
                    note = "Smooth velvet texture with high casein protein.",
                    dietaryTag = "Keto & High-Protein"
                )
            )
        ),
        IngredientSubstitution(
            originalIngredient = "Heavy Whipping Cream",
            category = "Dairy & Eggs",
            defaultSubstitutes = listOf(
                SubstitutionOption(
                    substituteName = "Coconut Cream (Chilled Canned)",
                    ratio = "1:1 ratio",
                    note = "Thick, dairy-free rich fat content ideal for curries, soups, and whipped toppings.",
                    dietaryTag = "Vegan & Dairy-Free"
                ),
                SubstitutionOption(
                    substituteName = "Whole Milk + 2 tbsp Melted Butter",
                    ratio = "3/4 cup milk + 1/4 cup melted butter per 1 cup cream",
                    note = "Recreates the fat percentage and silky body in savory sauces.",
                    dietaryTag = "Keto"
                ),
                SubstitutionOption(
                    substituteName = "Silken Tofu (Blended smooth)",
                    ratio = "1:1 ratio",
                    note = "Neutral flavor, creamy body, zero dairy.",
                    dietaryTag = "Vegan"
                )
            )
        ),
        IngredientSubstitution(
            originalIngredient = "Eggs",
            category = "Dairy & Eggs",
            defaultSubstitutes = listOf(
                SubstitutionOption(
                    substituteName = "Flaxseed Meal Egg (Flax Egg)",
                    ratio = "1 tbsp ground flax + 3 tbsp warm water (let sit 5 min)",
                    note = "Binds baked goods and pancakes while delivering dietary fiber and Omega-3s.",
                    dietaryTag = "Vegan"
                ),
                SubstitutionOption(
                    substituteName = "Unsweetened Applesauce",
                    ratio = "1/4 cup per egg",
                    note = "Provides natural moisture and binding for muffins, cakes, and quick breads.",
                    dietaryTag = "Low-Fat"
                ),
                SubstitutionOption(
                    substituteName = "Mashed Ripe Banana",
                    ratio = "1/2 medium banana per egg",
                    note = "Adds moisture and delicate sweetness in pancakes and breakfast bakes.",
                    dietaryTag = "Vegan"
                ),
                SubstitutionOption(
                    substituteName = "Aquafaba (Chickpea liquid)",
                    ratio = "3 tbsp per whole egg (or 2 tbsp per egg white)",
                    note = "Whips into peaks like real egg whites for meringues and batters.",
                    dietaryTag = "Vegan"
                )
            )
        ),
        IngredientSubstitution(
            originalIngredient = "Whole Milk",
            category = "Dairy & Eggs",
            defaultSubstitutes = listOf(
                SubstitutionOption(
                    substituteName = "Unsweetened Almond Milk",
                    ratio = "1:1 ratio",
                    note = "Light, nutty, low in calories; works across savory and sweet recipes.",
                    dietaryTag = "Dairy-Free & Low-Calorie"
                ),
                SubstitutionOption(
                    substituteName = "Oat Milk (Barista / Full-Body)",
                    ratio = "1:1 ratio",
                    note = "Creamiest plant milk, foams well and mimics whole milk mouthfeel.",
                    dietaryTag = "Vegan & Nut-Free"
                ),
                SubstitutionOption(
                    substituteName = "Soy Milk",
                    ratio = "1:1 ratio",
                    note = "High in complete plant protein with balanced consistency.",
                    dietaryTag = "High-Protein"
                )
            )
        ),
        IngredientSubstitution(
            originalIngredient = "Buttermilk",
            category = "Dairy & Eggs",
            defaultSubstitutes = listOf(
                SubstitutionOption(
                    substituteName = "Milk + 1 tbsp Fresh Lemon Juice or Vinegar",
                    ratio = "1 cup milk + 1 tbsp acid (rest 5 min to curdle)",
                    note = "Acid activates baking soda for fluffy pancakes and tender meat marinades.",
                    dietaryTag = "Quick Pantry Fix"
                ),
                SubstitutionOption(
                    substituteName = "Plain Yogurt thinned with Milk",
                    ratio = "3/4 cup plain yogurt + 1/4 cup milk",
                    note = "Provides identical tangy acidity and thick texture.",
                    dietaryTag = "Probiotic"
                )
            )
        ),
        IngredientSubstitution(
            originalIngredient = "Aged Sharp Cheddar",
            category = "Dairy & Eggs",
            defaultSubstitutes = listOf(
                SubstitutionOption(
                    substituteName = "Nutritional Yeast Flakes",
                    ratio = "2 tbsp per 1/4 cup grated cheese",
                    note = "Rich cheesy, nutty umami flavor loaded with B-vitamins.",
                    dietaryTag = "Vegan & Dairy-Free"
                ),
                SubstitutionOption(
                    substituteName = "Pecorino Romano or Parmesan",
                    ratio = "3/4 amount of cheddar",
                    note = "Slightly sharper and saltier; reduces needed sodium in dishes.",
                    dietaryTag = "Keto"
                ),
                SubstitutionOption(
                    substituteName = "Plant-Based Cheddar Shreds",
                    ratio = "1:1 ratio",
                    note = "Melts smoothly over frittatas and casseroles without dairy.",
                    dietaryTag = "Vegan"
                )
            )
        ),
        IngredientSubstitution(
            originalIngredient = "Soy Sauce",
            category = "Condiments & Sauces",
            defaultSubstitutes = listOf(
                SubstitutionOption(
                    substituteName = "Tamari Sauce",
                    ratio = "1:1 ratio",
                    note = "Rich, dark, brewed from 100% soybeans with no wheat.",
                    dietaryTag = "Gluten-Free"
                ),
                SubstitutionOption(
                    substituteName = "Coconut Aminos",
                    ratio = "1:1 ratio (or + 1/4 tsp salt)",
                    note = "Naturally sweet, soy-free, contains ~70% less sodium than regular soy sauce.",
                    dietaryTag = "Keto & Paleo"
                ),
                SubstitutionOption(
                    substituteName = "Worcestershire Sauce + Water",
                    ratio = "1:1 mixture with water",
                    note = "Adds deep savory depth and complex anchovy-tamarind tang.",
                    dietaryTag = "Savory Umami"
                )
            )
        ),
        IngredientSubstitution(
            originalIngredient = "Olive Oil",
            category = "Oils & Vinegars",
            defaultSubstitutes = listOf(
                SubstitutionOption(
                    substituteName = "Avocado Oil",
                    ratio = "1:1 ratio",
                    note = "Neutral flavor profile with an exceptional 520°F smoke point for searing.",
                    dietaryTag = "High Heat"
                ),
                SubstitutionOption(
                    substituteName = "Sesame Oil (Toasted)",
                    ratio = "1/2 amount (dilute with neutral oil)",
                    note = "Intensely aromatic nutty flavor for stir-fries and dressings.",
                    dietaryTag = "Aromatic"
                ),
                SubstitutionOption(
                    substituteName = "Butter or Ghee",
                    ratio = "1:1 ratio",
                    note = "Adds golden browning and French bistro richness.",
                    dietaryTag = "Keto"
                )
            )
        ),
        IngredientSubstitution(
            originalIngredient = "Garlic Cloves",
            category = "Produce & Aromatics",
            defaultSubstitutes = listOf(
                SubstitutionOption(
                    substituteName = "Garlic Powder",
                    ratio = "1/8 tsp per 1 fresh clove",
                    note = "Distributes evenly throughout sauces, marinades, and dry rubs.",
                    dietaryTag = "Pantry Staple"
                ),
                SubstitutionOption(
                    substituteName = "Minced Shallots + Black Pepper",
                    ratio = "1 tbsp minced shallot per clove",
                    note = "Milder, sweeter allium flavor with delicate onion-garlic notes.",
                    dietaryTag = "Gourmet"
                ),
                SubstitutionOption(
                    substituteName = "Asafoetida (Hing Powder)",
                    ratio = "A tiny pinch (1/16 tsp)",
                    note = "Ayurvedic allium-free substitute with authentic pungent savoriness.",
                    dietaryTag = "Allium-Free"
                )
            )
        ),
        IngredientSubstitution(
            originalIngredient = "Fresh Lemons",
            category = "Produce",
            defaultSubstitutes = listOf(
                SubstitutionOption(
                    substituteName = "Fresh Lime Juice",
                    ratio = "1:1 ratio",
                    note = "Equal citric acidity with a bright zesty punch.",
                    dietaryTag = "Citrus"
                ),
                SubstitutionOption(
                    substituteName = "White Wine Vinegar or Apple Cider Vinegar",
                    ratio = "1/2 tbsp vinegar per 1 tbsp lemon juice",
                    note = "Provides crisp tartness in dressings, pan deglazing, and soups.",
                    dietaryTag = "Pantry Acid"
                )
            )
        ),
        IngredientSubstitution(
            originalIngredient = "Chicken Breast",
            category = "Meat & Seafood",
            defaultSubstitutes = listOf(
                SubstitutionOption(
                    substituteName = "Organic Firm Tofu (Pressed & Cubed)",
                    ratio = "1:1 weight ratio",
                    note = "Absorbs marinades and glazes beautifully with a crispy sear.",
                    dietaryTag = "Vegan & High-Protein"
                ),
                SubstitutionOption(
                    substituteName = "Boneless Turkey Cutlets",
                    ratio = "1:1 ratio",
                    note = "Lean poultry meat that cooks quickly with comparable macros.",
                    dietaryTag = "High-Protein"
                ),
                SubstitutionOption(
                    substituteName = "Chickpeas (Garbanzo Beans)",
                    ratio = "1 can rinsed chickpeas per 2 chicken breasts",
                    note = "Hearty plant-protein base with creamy texture and fiber.",
                    dietaryTag = "Vegetarian"
                )
            )
        ),
        IngredientSubstitution(
            originalIngredient = "Thick-Cut Smoked Bacon",
            category = "Meat & Seafood",
            defaultSubstitutes = listOf(
                SubstitutionOption(
                    substituteName = "Smoked Paprika Crispy Tempeh",
                    ratio = "1:1 ratio (sliced thin and pan-seared)",
                    note = "Fermented soy strips seasoned with liquid smoke and maple.",
                    dietaryTag = "Vegan"
                ),
                SubstitutionOption(
                    substituteName = "Turkey Bacon",
                    ratio = "1:1 ratio",
                    note = "Lower in saturated fat and calories with classic smoky crunch.",
                    dietaryTag = "Lean Protein"
                ),
                SubstitutionOption(
                    substituteName = "Shiitake Mushroom 'Bacon'",
                    ratio = "Thinly sliced shiitakes roasted with tamari & smoked paprika",
                    note = "Earthy, crispy, plant-based bacon alternative.",
                    dietaryTag = "Vegan"
                )
            )
        ),
        IngredientSubstitution(
            originalIngredient = "Organic Firm Tofu",
            category = "Plant Protein",
            defaultSubstitutes = listOf(
                SubstitutionOption(
                    substituteName = "Tempeh (Steamed & Cubed)",
                    ratio = "1:1 ratio",
                    note = "Nutty whole-bean texture with higher fiber and protein density.",
                    dietaryTag = "High-Protein"
                ),
                SubstitutionOption(
                    substituteName = "Chicken Breast",
                    ratio = "1:1 ratio",
                    note = "Lean protein alternative for omnivore cooking.",
                    dietaryTag = "High-Protein"
                ),
                SubstitutionOption(
                    substituteName = "Cooked Edamame / Chickpeas",
                    ratio = "1:1 volume ratio",
                    note = "Quick toss-in legume protein requiring no pressing.",
                    dietaryTag = "Vegan"
                )
            )
        ),
        IngredientSubstitution(
            originalIngredient = "White Rice",
            category = "Grains & Starches",
            defaultSubstitutes = listOf(
                SubstitutionOption(
                    substituteName = "Cauliflower Rice",
                    ratio = "1:1 ratio (sauté 3-4 min)",
                    note = "Cuts net carbs by 90% while adding vitamin C and fiber.",
                    dietaryTag = "Keto & Low-Carb"
                ),
                SubstitutionOption(
                    substituteName = "Organic Quinoa",
                    ratio = "1:1 ratio",
                    note = "Contains all 9 essential amino acids and complex carbs.",
                    dietaryTag = "Gluten-Free & High-Protein"
                )
            )
        ),
        IngredientSubstitution(
            originalIngredient = "All-Purpose Flour",
            category = "Baking & Grains",
            defaultSubstitutes = listOf(
                SubstitutionOption(
                    substituteName = "1:1 Gluten-Free Baking Flour Blend",
                    ratio = "1:1 ratio",
                    note = "Pre-blended with xanthan gum to mimic wheat flour stretch.",
                    dietaryTag = "Gluten-Free"
                ),
                SubstitutionOption(
                    substituteName = "Blanched Almond Flour",
                    ratio = "1:1 ratio (or + 1 egg for binding)",
                    note = "Nutty, moist crumb with virtually zero net carbs.",
                    dietaryTag = "Keto & Grain-Free"
                ),
                SubstitutionOption(
                    substituteName = "Oat Flour (Blended Rolled Oats)",
                    ratio = "1:1 ratio",
                    note = "Gentle wholesome flavor high in beta-glucan soluble fiber.",
                    dietaryTag = "Whole Grain"
                )
            )
        ),
        IngredientSubstitution(
            originalIngredient = "Cornstarch",
            category = "Baking & Grains",
            defaultSubstitutes = listOf(
                SubstitutionOption(
                    substituteName = "Arrowroot Starch / Powder",
                    ratio = "1:1 ratio",
                    note = "Glossy thickening agent that freezes well and holds in acidic sauces.",
                    dietaryTag = "Paleo & Grain-Free"
                ),
                SubstitutionOption(
                    substituteName = "Tapioca Starch",
                    ratio = "2 tsp tapioca per 1 tsp cornstarch",
                    note = "Creates smooth gravies and stir-fry glazes.",
                    dietaryTag = "Gluten-Free"
                )
            )
        )
    )

    fun findSubstitutionsForIngredient(name: String): IngredientSubstitution? {
        val query = name.lowercase().trim()
        return allSubstitutions.firstOrNull { sub ->
            val orig = sub.originalIngredient.lowercase()
            query.contains(orig) || orig.contains(query) ||
                    (orig == "butter" && query.contains("butter")) ||
                    (orig == "sour cream" && query.contains("sour cream")) ||
                    (orig == "heavy whipping cream" && (query.contains("cream") || query.contains("heavy cream"))) ||
                    (orig == "eggs" && (query.contains("egg") || query.contains("eggs"))) ||
                    (orig == "whole milk" && query.contains("milk")) ||
                    (orig == "aged sharp cheddar" && (query.contains("cheddar") || query.contains("cheese"))) ||
                    (orig == "garlic cloves" && query.contains("garlic")) ||
                    (orig == "chicken breast" && (query.contains("chicken") || query.contains("poultry"))) ||
                    (orig == "thick-cut smoked bacon" && query.contains("bacon")) ||
                    (orig == "organic firm tofu" && query.contains("tofu")) ||
                    (orig == "fresh lemons" && query.contains("lemon")) ||
                    (orig == "olive oil" && query.contains("oil")) ||
                    (orig == "soy sauce" && query.contains("soy sauce"))
        }
    }

    fun findSubstitutionsForRecipe(recipe: Recipe): List<Pair<String, List<SubstitutionOption>>> {
        val results = mutableListOf<Pair<String, List<SubstitutionOption>>>()
        val allRecipeIngredients = (recipe.matchedIngredients + recipe.missingIngredients).distinct()

        for (ingredient in allRecipeIngredients) {
            val sub = findSubstitutionsForIngredient(ingredient)
            if (sub != null) {
                results.add(Pair(ingredient, sub.defaultSubstitutes))
            }
        }
        return results
    }
}
