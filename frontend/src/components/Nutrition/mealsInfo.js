// mealsInfo.js

const mealsInfo = [
    // ===== BREAKFAST =====
    {
      name: 'Oatmeal with Quinoa and Banana',
      mealCategory: 'Breakfast',
      day: 'Monday',
      time: '15 mins',
      energy: 300,   // kcal per serve
      protein: 10,   // g
      carbs: 50,     // g
      fat: 5,        // g
      favorite: false,
      ingredients: [
        { baseAmount: 50, unit: 'g', name: 'Oats', price: 0.80 },
        { baseAmount: 30, unit: 'g', name: 'Quinoa', price: 1.20 },
        { baseAmount: 1, unit: 'unit', name: 'Banana (sliced)', price: 0.70 },
        { baseAmount: 200, unit: 'ml', name: 'Almond milk', price: 2.50 },
        { baseAmount: 10, unit: 'g', name: 'Honey', price: 0.50 }
      ],
      image: 'https://fodmap-publicsite-us-east-2.s3.amazonaws.com/production/media/images/Quinoa_Porridge_with_Banana_Yoghur.2e16d0ba.fill-720x400.png',
      instructions_step1: "Combine oats, quinoa, and almond milk in a saucepan.",
      ingredients_step1: [
        { baseAmount: 50, unit: 'g', name: 'Oats', price: 0.80 },
        { baseAmount: 30, unit: 'g', name: 'Quinoa', price: 1.20 },
        { baseAmount: 200, unit: 'ml', name: 'Almond milk', price: 2.50 }
      ],
      instructions_step2: "Bring the mixture to a simmer and cook until thickened (about 5 minutes).",
      ingredients_step2: [],
      instructions_step3: "Add the sliced banana to the porridge.",
      ingredients_step3: [
        { baseAmount: 1, unit: 'unit', name: 'Banana (sliced)', price: 0.70 }
      ],
      instructions_step4: "Drizzle with honey and serve warm.",
      ingredients_step4: [
        { baseAmount: 10, unit: 'g', name: 'Honey', price: 0.50 }
      ],
      videoLink: ""
    },
    {
      name: 'Avocado Toast with Poached Egg',
      mealCategory: 'Breakfast',
      day: 'Tuesday',
      time: '10 mins',
      energy: 350,
      protein: 15,
      carbs: 30,
      fat: 18,
      favorite: false,
      ingredients: [
        { baseAmount: 2, unit: 'slices', name: 'Whole grain bread', price: 1.50 },
        { baseAmount: 1, unit: 'unit', name: 'Avocado', price: 3.00 },
        { baseAmount: 1, unit: 'unit', name: 'Egg', price: 0.80 },
        { baseAmount: 5, unit: 'g', name: 'Lemon juice', price: 0.30 },
        { baseAmount: 2, unit: 'g', name: 'Chili flakes', price: 0.20 }
      ],
      image: 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTn7d-BBT7XiRDdAEamksYWQKxfzPKrrvCjOQ&s',
      instructions_step1: "Toast the whole grain bread slices until golden.",
      ingredients_step1: [
        { baseAmount: 2, unit: 'slices', name: 'Whole grain bread', price: 1.50 }
      ],
      instructions_step2: "In a bowl, mash the avocado with lemon juice and chili flakes.",
      ingredients_step2: [
        { baseAmount: 1, unit: 'unit', name: 'Avocado', price: 3.00 },
        { baseAmount: 5, unit: 'g', name: 'Lemon juice', price: 0.30 },
        { baseAmount: 2, unit: 'g', name: 'Chili flakes', price: 0.20 }
      ],
      instructions_step3: "Poach the egg until the white is set and yolk remains runny.",
      ingredients_step3: [
        { baseAmount: 1, unit: 'unit', name: 'Egg', price: 0.80 }
      ],
      instructions_step4: "Spread the avocado mash on the toast and top with the poached egg.",
      ingredients_step4: [],
      videoLink: ""
    },
    {
      name: 'Fruit Salad with Yogurt and Granola',
      mealCategory: 'Breakfast',
      day: 'Wednesday',
      time: '10 mins',
      energy: 250,
      protein: 8,
      carbs: 40,
      fat: 7,
      favorite: false,
      ingredients: [
        { baseAmount: 100, unit: 'g', name: 'Mixed fruits', price: 2.00 },
        { baseAmount: 150, unit: 'g', name: 'Plain yogurt', price: 1.50 },
        { baseAmount: 30, unit: 'g', name: 'Granola', price: 1.00 },
        { baseAmount: 10, unit: 'g', name: 'Honey', price: 0.50 }
      ],
      image: 'https://realfood.tesco.com/media/images/MothersGranolaLGH-e914ddf2-750c-458b-ba8b-6695bb46a96d-0-1400x919.jpg',
      instructions_step1: "Chop or dice the mixed fruits into bite-sized pieces.",
      ingredients_step1: [
        { baseAmount: 100, unit: 'g', name: 'Mixed fruits', price: 2.00 }
      ],
      instructions_step2: "Mix the fruits with plain yogurt in a bowl.",
      ingredients_step2: [
        { baseAmount: 150, unit: 'g', name: 'Plain yogurt', price: 1.50 }
      ],
      instructions_step3: "Sprinkle granola evenly over the fruit and yogurt mixture.",
      ingredients_step3: [
        { baseAmount: 30, unit: 'g', name: 'Granola', price: 1.00 }
      ],
      instructions_step4: "Drizzle with honey if desired, then serve immediately.",
      ingredients_step4: [
        { baseAmount: 10, unit: 'g', name: 'Honey', price: 0.50 }
      ],
      videoLink: ""
    },
    {
      name: 'Amaranth Porridge',
      mealCategory: 'Breakfast',
      day: 'Thursday',
      time: '20 mins',
      energy: 280,
      protein: 9,
      carbs: 45,
      fat: 6,
      favorite: false,
      ingredients: [
        { baseAmount: 50, unit: 'g', name: 'Amaranth', price: 1.00 },
        { baseAmount: 200, unit: 'ml', name: 'Milk', price: 1.50 },
        { baseAmount: 1, unit: 'unit', name: 'Apple (diced)', price: 1.20 },
        { baseAmount: 5, unit: 'g', name: 'Cinnamon', price: 0.30 },
        { baseAmount: 5, unit: 'g', name: 'Honey', price: 0.30 }
      ],
      image: 'https://eating-made-easy.com/wp-content/uploads/2014/06/15.jpg',
      instructions_step1: "In a pot, combine amaranth and milk.",
      ingredients_step1: [
        { baseAmount: 50, unit: 'g', name: 'Amaranth', price: 1.00 },
        { baseAmount: 200, unit: 'ml', name: 'Milk', price: 1.50 }
      ],
      instructions_step2: "Bring the mixture to a simmer and cook for about 10 minutes until it thickens.",
      ingredients_step2: [],
      instructions_step3: "Stir in the diced apple.",
      ingredients_step3: [
        { baseAmount: 1, unit: 'unit', name: 'Apple (diced)', price: 1.20 }
      ],
      instructions_step4: "Add cinnamon and honey, then stir well and serve warm.",
      ingredients_step4: [
        { baseAmount: 5, unit: 'g', name: 'Cinnamon', price: 0.30 },
        { baseAmount: 5, unit: 'g', name: 'Honey', price: 0.30 }
      ],
      videoLink: ""
    },
    {
      name: 'Chia Pudding with Mango',
      mealCategory: 'Breakfast',
      day: 'Friday',
      time: 'Overnight',
      energy: 300,
      protein: 8,
      carbs: 40,
      fat: 10,
      favorite: false,
      ingredients: [
        { baseAmount: 30, unit: 'g', name: 'Chia seeds', price: 1.20 },
        { baseAmount: 200, unit: 'ml', name: 'Coconut milk', price: 2.00 },
        { baseAmount: 1, unit: 'unit', name: 'Mango (diced)', price: 2.50 },
        { baseAmount: 5, unit: 'g', name: 'Honey', price: 0.30 }
      ],
      image: 'https://c.ndtvimg.com/2020-06/a0ct6hi_pudding_625x300_26_June_20.jpg',
      instructions_step1: "Mix chia seeds with coconut milk in a bowl.",
      ingredients_step1: [
        { baseAmount: 30, unit: 'g', name: 'Chia seeds', price: 1.20 },
        { baseAmount: 200, unit: 'ml', name: 'Coconut milk', price: 2.00 }
      ],
      instructions_step2: "Cover and refrigerate overnight for the pudding to set.",
      ingredients_step2: [],
      instructions_step3: "Dice the mango into small cubes.",
      ingredients_step3: [
        { baseAmount: 1, unit: 'unit', name: 'Mango (diced)', price: 2.50 }
      ],
      instructions_step4: "Top the chia pudding with the diced mango and drizzle with honey.",
      ingredients_step4: [
        { baseAmount: 5, unit: 'g', name: 'Honey', price: 0.30 }
      ],
      videoLink: ""
    },
    {
      name: 'Cornmeal Pancakes with Honey',
      mealCategory: 'Breakfast',
      day: 'Saturday',
      time: '15 mins',
      energy: 350,
      protein: 10,
      carbs: 55,
      fat: 8,
      favorite: false,
      ingredients: [
        { baseAmount: 60, unit: 'g', name: 'Cornmeal', price: 1.00 },
        { baseAmount: 40, unit: 'g', name: 'Whole wheat flour', price: 0.80 },
        { baseAmount: 1, unit: 'unit', name: 'Egg', price: 0.80 },
        { baseAmount: 150, unit: 'ml', name: 'Milk', price: 1.20 },
        { baseAmount: 10, unit: 'g', name: 'Honey', price: 0.50 }
      ],
      image: 'https://dinnerthendessert.com/wp-content/uploads/2015/05/Honey-Cornmeal-Pancakes.jpg',
      instructions_step1: "In a bowl, mix cornmeal, whole wheat flour, egg, and milk until smooth.",
      ingredients_step1: [
        { baseAmount: 60, unit: 'g', name: 'Cornmeal', price: 1.00 },
        { baseAmount: 40, unit: 'g', name: 'Whole wheat flour', price: 0.80 },
        { baseAmount: 1, unit: 'unit', name: 'Egg', price: 0.80 },
        { baseAmount: 150, unit: 'ml', name: 'Milk', price: 1.20 }
      ],
      instructions_step2: "Whisk the batter thoroughly until it is smooth.",
      ingredients_step2: [],
      instructions_step3: "Pour small portions of the batter onto a hot non-stick pan and cook until golden on both sides.",
      ingredients_step3: [],
      instructions_step4: "Serve the pancakes with a drizzle of honey on top.",
      ingredients_step4: [
        { baseAmount: 10, unit: 'g', name: 'Honey', price: 0.50 }
      ],
      videoLink: ""
    },
    {
      name: 'Papaya Smoothie Bowl',
      mealCategory: 'Breakfast',
      day: 'Sunday',
      time: '10 mins',
      energy: 320,
      protein: 9,
      carbs: 50,
      fat: 7,
      favorite: false,
      ingredients: [
        { baseAmount: 150, unit: 'g', name: 'Papaya', price: 1.50 },
        { baseAmount: 100, unit: 'g', name: 'Banana', price: 0.70 },
        { baseAmount: 100, unit: 'ml', name: 'Coconut water', price: 1.00 },
        { baseAmount: 30, unit: 'g', name: 'Granola', price: 1.00 },
        { baseAmount: 10, unit: 'g', name: 'Chia seeds', price: 0.40 }
      ],
      image: 'https://cdn.foodaciously.com/static/recipes/dada739f-363c-4f0b-8b93-6f87a65648b6/papaya-smoothie-bowl-ca17acf95da46587c08daefeb2a740fe-2560.jpg',
      instructions_step1: "In a blender, combine papaya, banana, and coconut water until smooth.",
      ingredients_step1: [
        { baseAmount: 150, unit: 'g', name: 'Papaya', price: 1.50 },
        { baseAmount: 100, unit: 'g', name: 'Banana', price: 0.70 },
        { baseAmount: 100, unit: 'ml', name: 'Coconut water', price: 1.00 }
      ],
      instructions_step2: "Pour the smoothie mixture into a bowl.",
      ingredients_step2: [],
      instructions_step3: "Sprinkle granola evenly over the smoothie bowl.",
      ingredients_step3: [
        { baseAmount: 30, unit: 'g', name: 'Granola', price: 1.00 }
      ],
      instructions_step4: "Garnish with a sprinkle of chia seeds and serve immediately.",
      ingredients_step4: [
        { baseAmount: 10, unit: 'g', name: 'Chia seeds', price: 0.40 }
      ],
      videoLink: ""
    },
    {
      name: 'Quinoa Salad with Chicken',
      mealCategory: 'Lunch',
      day: 'Monday',
      time: '20 mins',
      energy: 400,
      protein: 30,
      carbs: 50,
      fat: 14,
      favorite: false,
      ingredients: [
        { baseAmount: 100, unit: 'g', name: 'Quinoa', price: 2.50 },
        { baseAmount: 150, unit: 'g', name: 'Chicken breast', price: 4.00 },
        { baseAmount: 50, unit: 'g', name: 'Bell pepper', price: 0.80 },
        { baseAmount: 30, unit: 'g', name: 'Onion', price: 0.30 },
        { baseAmount: 20, unit: 'ml', name: 'Olive oil', price: 1.00 },
        { baseAmount: 5, unit: 'g', name: 'Salt', price: 0.10 },
        { baseAmount: 5, unit: 'g', name: 'Pepper', price: 0.20 },
        { baseAmount: 50, unit: 'g', name: 'Spinach leaves', price: 1.00 }
      ],
      image: 'https://sunkissedkitchen.com/wp-content/uploads/2017/11/moroccan-chicken-quinoa-salad-feature.jpg',
      instructions_step1: "Cook the quinoa according to package instructions and let it cool.",
      ingredients_step1: [
        { baseAmount: 100, unit: 'g', name: 'Quinoa', price: 2.50 }
      ],
      instructions_step2: "Grill or pan-fry the chicken breast until cooked and then slice it.",
      ingredients_step2: [
        { baseAmount: 150, unit: 'g', name: 'Chicken breast', price: 4.00 }
      ],
      instructions_step3: "Chop the bell pepper and onion, and toss them with spinach leaves.",
      ingredients_step3: [
        { baseAmount: 50, unit: 'g', name: 'Bell pepper', price: 0.80 },
        { baseAmount: 30, unit: 'g', name: 'Onion', price: 0.30 },
        { baseAmount: 50, unit: 'g', name: 'Spinach leaves', price: 1.00 }
      ],
      instructions_step4: "Drizzle with olive oil, season with salt and pepper, and mix with the quinoa and chicken.",
      ingredients_step4: [
        { baseAmount: 20, unit: 'ml', name: 'Olive oil', price: 1.00 },
        { baseAmount: 5, unit: 'g', name: 'Salt', price: 0.10 },
        { baseAmount: 5, unit: 'g', name: 'Pepper', price: 0.20 }
      ],
      videoLink: ""
    },
    {
      name: 'Lomo Saltado',
      mealCategory: 'Lunch',
      day: 'Tuesday',
      time: '25 mins',
      energy: 170,
      protein: 20,
      carbs: 25,
      fat: 8,
      favorite: false,
      ingredients: [
        { baseAmount: 300, unit: 'g', name: 'Beef sirloin', price: 15.00 },
        { baseAmount: 150, unit: 'g', name: 'Onion', price: 1.20 },
        { baseAmount: 150, unit: 'g', name: 'Tomato', price: 1.50 },
        { baseAmount: 30, unit: 'ml', name: 'Soy sauce', price: 0.80 },
        { baseAmount: 10, unit: 'ml', name: 'Vinegar', price: 0.40 },
        { baseAmount: 200, unit: 'g', name: 'Potato (for fries)', price: 2.50 },
        { baseAmount: 150, unit: 'g', name: 'Rice (cooked)', price: 1.50 },
        { baseAmount: 10, unit: 'g', name: 'Salt', price: 0.10 },
        { baseAmount: 5, unit: 'g', name: 'Pepper', price: 0.20 }
      ],
      image: 'https://i.ytimg.com/vi/sWXRJbGi6yQ/hq720.jpg?sqp=-oaymwEhCK4FEIIDSFryq4qpAxMIARUAAAAAGAElAADIQj0AgKJD&rs=AOn4CLAmzgGJ6J0eLasWLgYE-TcZgyiSJQ',
      instructions_step1: "beef sirloin into strips and slice the onion and tomato.",
      ingredients_step1: [
        { baseAmount: 300, unit: 'g', name: 'Beef sirloin', price: 15.00 },
        { baseAmount: 150, unit: 'g', name: 'Onion', price: 1.20 },
        { baseAmount: 150, unit: 'g', name: 'Tomato', price: 1.50 }
      ],
      instructions_step2: "Stir-fry the beef and vegetables in a hot pan with soy sauce and vinegar.",
      ingredients_step2: [
        { baseAmount: 30, unit: 'ml', name: 'Soy sauce', price: 0.80 },
        { baseAmount: 10, unit: 'ml', name: 'Vinegar', price: 0.40 }
      ],
      instructions_step3: "Fry the potatoes separately until crispy.",
      ingredients_step3: [
        { baseAmount: 200, unit: 'g', name: 'Potato (for fries)', price: 2.50 }
      ],
      instructions_step4: "Serve the stir-fry with a side of rice and season with salt and pepper.",
      ingredients_step4: [
        { baseAmount: 150, unit: 'g', name: 'Rice (cooked)', price: 1.50 },
        { baseAmount: 10, unit: 'g', name: 'Salt', price: 0.10 },
        { baseAmount: 5, unit: 'g', name: 'Pepper', price: 0.20 }
      ],
      videoLink: ""
    },
    {
      name: 'Rice And Chicken',
      mealCategory: 'Lunch',
      day: 'Wednesday',
      time: '35 mins',
      energy: 320,
      protein: 25,
      carbs: 50,
      fat: 10,
      favorite: false,
      ingredients: [
        { baseAmount: 150, unit: 'g', name: 'Chicken breast', price: 4.00 },
        { baseAmount: 200, unit: 'g', name: 'Rice', price: 2.00 },
        { baseAmount: 50, unit: 'g', name: 'Carrot', price: 0.50 },
        { baseAmount: 50, unit: 'g', name: 'Peas', price: 0.50 },
        { baseAmount: 20, unit: 'ml', name: 'Oil', price: 0.70 },
        { baseAmount: 5, unit: 'g', name: 'Salt', price: 0.10 },
        { baseAmount: 5, unit: 'g', name: 'Pepper', price: 0.20 },
        { baseAmount: 50, unit: 'ml', name: 'Chicken broth', price: 0.60 }
      ],
      image: 'https://www.simplyrecipes.com/thmb/KU-Q1xCeSjcMHLfGVvYUiQoBknA=/1500x0/filters:no_upscale():max_bytes(150000):strip_icc()/__opt__aboutcom__coeus__resources__content_migration__simply_recipes__uploads__2014__06__Chicken-Rice-Casserole-LEAD-2-01230ff397ac4b35958e8ffe9857fca6.jpg',
      instructions_step1: "Dice the chicken breast into small cubes.",
      ingredients_step1: [
        { baseAmount: 150, unit: 'g', name: 'Chicken breast', price: 4.00 }
      ],
      instructions_step2: "Cook the rice according to package instructions and let it cool.",
      ingredients_step2: [
        { baseAmount: 200, unit: 'g', name: 'Rice', price: 2.00 }
      ],
      instructions_step3: "Sauté diced carrot and peas in oil until tender.",
      ingredients_step3: [
        { baseAmount: 50, unit: 'g', name: 'Carrot', price: 0.50 },
        { baseAmount: 50, unit: 'g', name: 'Peas', price: 0.50 },
        { baseAmount: 20, unit: 'ml', name: 'Oil', price: 0.70 }
      ],
      instructions_step4: "Add chicken broth and the cooked chicken to the vegetables, season with salt and pepper, then combine with the rice.",
      ingredients_step4: [
        { baseAmount: 5, unit: 'g', name: 'Salt', price: 0.10 },
        { baseAmount: 5, unit: 'g', name: 'Pepper', price: 0.20 },
        { baseAmount: 50, unit: 'ml', name: 'Chicken broth', price: 0.60 }
      ],
      videoLink: ""
    },
    {
      name: 'Aji de Gallina',
      mealCategory: 'Lunch',
      day: 'Thursday',
      time: '40 mins',
      energy: 350,
      protein: 28,
      carbs: 40,
      fat: 15,
      favorite: false,
      ingredients: [
        { baseAmount: 200, unit: 'g', name: 'Chicken (cooked & shredded)', price: 5.00 },
        { baseAmount: 100, unit: 'g', name: 'Bread', price: 1.50 },
        { baseAmount: 50, unit: 'g', name: 'Walnuts', price: 3.00 },
        { baseAmount: 100, unit: 'ml', name: 'Milk', price: 1.00 },
        { baseAmount: 50, unit: 'g', name: 'Onion', price: 0.50 },
        { baseAmount: 2, unit: 'unit', name: 'Yellow chili peppers', price: 0.80 },
        { baseAmount: 10, unit: 'g', name: 'Salt', price: 0.10 },
        { baseAmount: 5, unit: 'g', name: 'Pepper', price: 0.20 }
      ],
      image: 'https://recetas.comohacer.cl/wp-content/uploads/2023/03/aji-de-gallina-con-arroz.jpg',
      instructions_step1: "Shred the cooked chicken and soak the bread in milk.",
      ingredients_step1: [
        { baseAmount: 200, unit: 'g', name: 'Chicken (cooked & shredded)', price: 5.00 },
        { baseAmount: 100, unit: 'g', name: 'Bread', price: 1.50 },
        { baseAmount: 100, unit: 'ml', name: 'Milk', price: 1.00 }
      ],
      instructions_step2: "Finely chop the onion and yellow chili peppers.",
      ingredients_step2: [
        { baseAmount: 50, unit: 'g', name: 'Onion', price: 0.50 },
        { baseAmount: 2, unit: 'unit', name: 'Yellow chili peppers', price: 0.80 }
      ],
      instructions_step3: "Blend the soaked bread with walnuts to create a creamy base.",
      ingredients_step3: [
        { baseAmount: 50, unit: 'g', name: 'Walnuts', price: 3.00 }
      ],
      instructions_step4: "Combine the shredded chicken, chopped vegetables, and the creamy base; season with salt and pepper.",
      ingredients_step4: [
        { baseAmount: 10, unit: 'g', name: 'Salt', price: 0.10 },
        { baseAmount: 5, unit: 'g', name: 'Pepper', price: 0.20 }
      ],
      videoLink: ""
    },
    {
      name: 'Ceviche',
      mealCategory: 'Lunch',
      day: 'Friday',
      time: '30 mins',
      energy: 140,
      protein: 22,
      carbs: 14,
      fat: 3,
      favorite: false,
      ingredients: [
        { baseAmount: 400, unit: 'g', name: 'Fresh fish (sea bass)', price: 12.00 },
        { baseAmount: 120, unit: 'ml', name: 'Lime juice', price: 1.50 },
        { baseAmount: 80, unit: 'g', name: 'Red onion', price: 0.80 },
        { baseAmount: 2, unit: 'unit', name: 'Fresh chili (ají limo)', price: 0.50 },
        { baseAmount: 200, unit: 'g', name: 'Sweet potato', price: 2.00 },
        { baseAmount: 100, unit: 'g', name: 'Corn (cooked)', price: 1.00 },
        { baseAmount: 10, unit: 'g', name: 'Cilantro', price: 0.30 },
        { baseAmount: 10, unit: 'g', name: 'Salt', price: 0.10 },
        { baseAmount: 5, unit: 'g', name: 'Garlic', price: 0.20 },
        { baseAmount: 10, unit: 'ml', name: 'Vegetable oil', price: 0.50 }
      ],
      image: 'https://cdn0.recetasgratis.net/es/posts/7/4/1/ceviche_peruano_18147_orig.jpg',
      instructions_step1: "Dice the fresh fish and thinly slice the red onion.",
      ingredients_step1: [
        { baseAmount: 400, unit: 'g', name: 'Fresh fish (sea bass)', price: 12.00 },
        { baseAmount: 80, unit: 'g', name: 'Red onion', price: 0.80 }
      ],
      instructions_step2: "In a bowl, mix the fish with lime juice, minced garlic, and sliced fresh chili.",
      ingredients_step2: [
        { baseAmount: 120, unit: 'ml', name: 'Lime juice', price: 1.50 },
        { baseAmount: 5, unit: 'g', name: 'Garlic', price: 0.20 },
        { baseAmount: 2, unit: 'unit', name: 'Fresh chili (ají limo)', price: 0.50 }
      ],
      instructions_step3: "Add the cooked sweet potato and corn to the marinated fish, stirring gently.",
      ingredients_step3: [
        { baseAmount: 200, unit: 'g', name: 'Sweet potato', price: 2.00 },
        { baseAmount: 100, unit: 'g', name: 'Corn (cooked)', price: 1.00 }
      ],
      instructions_step4: "Garnish with chopped cilantro, drizzle a little vegetable oil, and season with salt.",
      ingredients_step4: [
        { baseAmount: 10, unit: 'g', name: 'Cilantro', price: 0.30 },
        { baseAmount: 10, unit: 'g', name: 'Salt', price: 0.10 },
        { baseAmount: 10, unit: 'ml', name: 'Vegetable oil', price: 0.50 }
      ],
      videoLink: ""
    },
    {
      name: 'Fish Cracklings (Baked)',
      mealCategory: 'Lunch',
      day: 'Saturday',
      time: '25 mins',
      energy: 400,
      protein: 25,
      carbs: 30,
      fat: 20,
      favorite: false,
      ingredients: [
        { baseAmount: 250, unit: 'g', name: 'Fish fillets', price: 10.00 },
        { baseAmount: 60, unit: 'g', name: 'Flour', price: 0.80 },
        { baseAmount: 10, unit: 'g', name: 'Baking powder', price: 0.50 },
        { baseAmount: 20, unit: 'ml', name: 'Oil', price: 0.70 },
        { baseAmount: 10, unit: 'g', name: 'Salt', price: 0.10 },
        { baseAmount: 5, unit: 'g', name: 'Pepper', price: 0.20 },
        { baseAmount: 50, unit: 'g', name: 'Breadcrumbs', price: 1.00 }
      ],
      image: 'https://cocina-casera.com/en/wp-content/uploads/2023/05/fish-cracklings.jpg',
      instructions_step1: "Cut the fish fillets into bite-sized pieces.",
      ingredients_step1: [
        { baseAmount: 250, unit: 'g', name: 'Fish fillets', price: 10.00 }
      ],
      instructions_step2: "In a bowl, mix the flour with baking powder and lightly season with salt and pepper.",
      ingredients_step2: [
        { baseAmount: 60, unit: 'g', name: 'Flour', price: 0.80 },
        { baseAmount: 10, unit: 'g', name: 'Baking powder', price: 0.50 },
        { baseAmount: 10, unit: 'g', name: 'Salt', price: 0.10 },
        { baseAmount: 5, unit: 'g', name: 'Pepper', price: 0.20 }
      ],
      instructions_step3: "Dredge the fish pieces in the seasoned flour mixture.",
      ingredients_step3: [],
      instructions_step4: "Bake the fish in a preheated oven with a light coating of oil, then toss with breadcrumbs until crispy.",
      ingredients_step4: [
        { baseAmount: 20, unit: 'ml', name: 'Oil', price: 0.70 },
        { baseAmount: 50, unit: 'g', name: 'Breadcrumbs', price: 1.00 }
      ],
      videoLink: ""
    },
    {
      name: 'Shrimp Chowder',
      mealCategory: 'Lunch',
      day: 'Sunday',
      time: '45 mins',
      energy: 500,
      protein: 35,
      carbs: 35,
      fat: 25,
      favorite: false,
      ingredients: [
        { baseAmount: 250, unit: 'g', name: 'Shrimp (peeled)', price: 12.00 },
        { baseAmount: 150, unit: 'g', name: 'Potato', price: 1.50 },
        { baseAmount: 60, unit: 'g', name: 'Onion', price: 0.60 },
        { baseAmount: 150, unit: 'ml', name: 'Cream', price: 2.00 },
        { baseAmount: 20, unit: 'g', name: 'Butter', price: 0.80 },
        { baseAmount: 400, unit: 'ml', name: 'Fish or seafood stock', price: 3.00 },
        { baseAmount: 10, unit: 'g', name: 'Salt', price: 0.10 },
        { baseAmount: 5, unit: 'g', name: 'Pepper', price: 0.20 },
        { baseAmount: 10, unit: 'g', name: 'Parsley', price: 0.30 }
      ],
      image: 'https://assets.wholefoodsmarket.com/recipes/4004/2048/1536/4004-5.jpg',
      instructions_step1: "In a large pot, sauté chopped onion and diced potato in butter until softened.",
      ingredients_step1: [
        { baseAmount: 150, unit: 'g', name: 'Potato', price: 1.50 },
        { baseAmount: 60, unit: 'g', name: 'Onion', price: 0.60 },
        { baseAmount: 20, unit: 'g', name: 'Butter', price: 0.80 }
      ],
      instructions_step2: "Add the seafood stock and simmer until the potatoes are tender.",
      ingredients_step2: [
        { baseAmount: 400, unit: 'ml', name: 'Fish or seafood stock', price: 3.00 }
      ],
      instructions_step3: "Stir in the shrimp and cream, cooking until the shrimp turn pink.",
      ingredients_step3: [
        { baseAmount: 250, unit: 'g', name: 'Shrimp (peeled)', price: 12.00 },
        { baseAmount: 150, unit: 'ml', name: 'Cream', price: 2.00 }
      ],
      instructions_step4: "Season with salt and pepper, garnish with chopped parsley, and serve hot.",
      ingredients_step4: [
        { baseAmount: 10, unit: 'g', name: 'Salt', price: 0.10 },
        { baseAmount: 5, unit: 'g', name: 'Pepper', price: 0.20 },
        { baseAmount: 10, unit: 'g', name: 'Parsley', price: 0.30 }
      ],
      videoLink: ""
    },
        // ===== SNACK =====
    {
      name: 'Plantain Chips with Guacamole',
      mealCategory: 'Snack',
      day: 'Monday',
      time: '10 mins',
      energy: 220,
      protein: 4,
      carbs: 30,
      fat: 10,
      favorite: false,
      ingredients: [
        { baseAmount: 150, unit: 'g', name: 'Green plantains', price: 1.50 },
        { baseAmount: 1, unit: 'unit', name: 'Avocado', price: 3.00 },
        { baseAmount: 5, unit: 'g', name: 'Lime juice', price: 0.30 },
        { baseAmount: 10, unit: 'g', name: 'Cilantro', price: 0.30 },
        { baseAmount: 2, unit: 'g', name: 'Salt', price: 0.05 }
      ],
      image: 'https://siriuslyhungry.com/wp-content/uploads/2021/07/guac-and-plantain-chips-4-720x405.jpg',
      instructions_step1: "Peel and slice the green plantains into chips.",
      ingredients_step1: [
        { baseAmount: 150, unit: 'g', name: 'Green plantains', price: 1.50 }
      ],
      instructions_step2: "Fry or bake the plantain slices until crisp.",
      ingredients_step2: [],
      instructions_step3: "Prepare the guacamole by mashing the avocado with lime juice and cilantro.",
      ingredients_step3: [
        { baseAmount: 1, unit: 'unit', name: 'Avocado', price: 3.00 },
        { baseAmount: 5, unit: 'g', name: 'Lime juice', price: 0.30 },
        { baseAmount: 10, unit: 'g', name: 'Cilantro', price: 0.30 }
      ],
      instructions_step4: "Serve the plantain chips with the guacamole and season with salt.",
      ingredients_step4: [
        { baseAmount: 2, unit: 'g', name: 'Salt', price: 0.05 }
      ],
      videoLink: ""
    },
    {
      name: 'Tostones with Lime',
      mealCategory: 'Snack',
      day: 'Tuesday',
      time: '10 mins',
      energy: 200,
      protein: 3,
      carbs: 28,
      fat: 8,
      favorite: false,
      ingredients: [
        { baseAmount: 150, unit: 'g', name: 'Green plantains', price: 1.50 },
        { baseAmount: 5, unit: 'g', name: 'Lime juice', price: 0.30 },
        { baseAmount: 2, unit: 'g', name: 'Salt', price: 0.05 },
        { baseAmount: 5, unit: 'g', name: 'Garlic powder', price: 0.20 }
      ],
      image: 'https://goodiegodmother.com/wp-content/uploads/2021/06/horizonal-image-cuban-fried-plantains-scaled.jpg',
      instructions_step1: "Peel and slice the green plantains.",
      ingredients_step1: [
        { baseAmount: 150, unit: 'g', name: 'Green plantains', price: 1.50 }
      ],
      instructions_step2: "Fry the slices until golden, then flatten them to form tostones.",
      ingredients_step2: [],
      instructions_step3: "Drizzle lime juice over the tostones.",
      ingredients_step3: [
        { baseAmount: 5, unit: 'g', name: 'Lime juice', price: 0.30 }
      ],
      instructions_step4: "Sprinkle with salt and garlic powder, then serve.",
      ingredients_step4: [
        { baseAmount: 2, unit: 'g', name: 'Salt', price: 0.05 },
        { baseAmount: 5, unit: 'g', name: 'Garlic powder', price: 0.20 }
      ],
      videoLink: ""
    },
    {
      name: 'Quinoa & Corn Salad',
      mealCategory: 'Snack',
      day: 'Wednesday',
      time: '15 mins',
      energy: 180,
      protein: 5,
      carbs: 25,
      fat: 6,
      favorite: false,
      ingredients: [
        { baseAmount: 50, unit: 'g', name: 'Quinoa', price: 1.25 },
        { baseAmount: 50, unit: 'g', name: 'Corn kernels', price: 0.80 },
        { baseAmount: 30, unit: 'g', name: 'Red bell pepper', price: 0.50 },
        { baseAmount: 20, unit: 'g', name: 'Red onion', price: 0.30 },
        { baseAmount: 5, unit: 'g', name: 'Lime juice', price: 0.30 },
        { baseAmount: 2, unit: 'g', name: 'Salt', price: 0.05 }
      ],
      image: 'https://www.grocery.coop/sites/default/files/wp-content/uploads/2012/03/Quinoa_Kale_Salad_with_Corn1_0.jpg',
      instructions_step1: "Cook the quinoa according to the package instructions and let it cool.",
      ingredients_step1: [
        { baseAmount: 50, unit: 'g', name: 'Quinoa', price: 1.25 }
      ],
      instructions_step2: "In a bowl, combine corn kernels, diced red bell pepper, and chopped red onion.",
      ingredients_step2: [
        { baseAmount: 50, unit: 'g', name: 'Corn kernels', price: 0.80 },
        { baseAmount: 30, unit: 'g', name: 'Red bell pepper', price: 0.50 },
        { baseAmount: 20, unit: 'g', name: 'Red onion', price: 0.30 }
      ],
      instructions_step3: "Add lime juice and season with salt.",
      ingredients_step3: [
        { baseAmount: 5, unit: 'g', name: 'Lime juice', price: 0.30 },
        { baseAmount: 2, unit: 'g', name: 'Salt', price: 0.05 }
      ],
      instructions_step4: "Toss all ingredients together and serve chilled.",
      ingredients_step4: [],
      videoLink: ""
    },
    {
      name: 'Mixed Nuts & Dried Fruits',
      mealCategory: 'Snack',
      day: 'Thursday',
      time: '5 mins',
      energy: 250,
      protein: 6,
      carbs: 20,
      fat: 15,
      favorite: false,
      ingredients: [
        { baseAmount: 30, unit: 'g', name: 'Almonds', price: 1.50 },
        { baseAmount: 30, unit: 'g', name: 'Walnuts', price: 1.80 },
        { baseAmount: 20, unit: 'g', name: 'Raisins', price: 0.80 },
        { baseAmount: 20, unit: 'g', name: 'Dried cranberries', price: 1.00 }
      ],
      image: 'https://www.nuttydelights.ie/pub/media/mageplaza/blog/post/u/n/untitled_design.jpg',
      instructions_step1: "Measure out the almonds, walnuts, raisins, and dried cranberries.",
      ingredients_step1: [
        { baseAmount: 30, unit: 'g', name: 'Almonds', price: 1.50 },
        { baseAmount: 30, unit: 'g', name: 'Walnuts', price: 1.80 }
      ],
      instructions_step2: "Coarsely chop the nuts for a varied texture.",
      ingredients_step2: [],
      instructions_step3: "Combine the chopped nuts with the dried fruits in a bowl.",
      ingredients_step3: [
        { baseAmount: 20, unit: 'g', name: 'Raisins', price: 0.80 },
        { baseAmount: 20, unit: 'g', name: 'Dried cranberries', price: 1.00 }
      ],
      instructions_step4: "Mix well and serve as a quick snack.",
      ingredients_step4: [],
      videoLink: ""
    },
    {
      name: 'Mini Ceviche Cups',
      mealCategory: 'Snack',
      day: 'Friday',
      time: '15 mins',
      energy: 210,
      protein: 12,
      carbs: 10,
      fat: 8,
      favorite: false,
      ingredients: [
        { baseAmount: 100, unit: 'g', name: 'Fresh fish', price: 3.00 },
        { baseAmount: 40, unit: 'ml', name: 'Lime juice', price: 0.50 },
        { baseAmount: 20, unit: 'g', name: 'Red onion', price: 0.30 },
        { baseAmount: 1, unit: 'unit', name: 'Fresh chili', price: 0.25 },
        { baseAmount: 5, unit: 'g', name: 'Cilantro', price: 0.20 },
        { baseAmount: 5, unit: 'g', name: 'Salt', price: 0.10 }
      ],
      image: 'https://www.pamperedchef.ca/iceberg/com/recipe/936267-lg.jpg',
      instructions_step1: "Dice the fresh fish into small cubes.",
      ingredients_step1: [
        { baseAmount: 100, unit: 'g', name: 'Fresh fish', price: 3.00 }
      ],
      instructions_step2: "Mix the fish with lime juice and finely chopped red onion.",
      ingredients_step2: [
        { baseAmount: 40, unit: 'ml', name: 'Lime juice', price: 0.50 },
        { baseAmount: 20, unit: 'g', name: 'Red onion', price: 0.30 }
      ],
      instructions_step3: "Add chopped fresh chili and cilantro.",
      ingredients_step3: [
        { baseAmount: 1, unit: 'unit', name: 'Fresh chili', price: 0.25 },
        { baseAmount: 5, unit: 'g', name: 'Cilantro', price: 0.20 }
      ],
      instructions_step4: "Season with salt and serve in small cups.",
      ingredients_step4: [
        { baseAmount: 5, unit: 'g', name: 'Salt', price: 0.10 }
      ],
      videoLink: ""
    },
    {
      name: 'Peruvian Granola Bars',
      mealCategory: 'Snack',
      day: 'Saturday',
      time: '20 mins',
      energy: 230,
      protein: 7,
      carbs: 30,
      fat: 9,
      favorite: false,
      ingredients: [
        { baseAmount: 50, unit: 'g', name: 'Oats', price: 0.80 },
        { baseAmount: 20, unit: 'g', name: 'Honey', price: 0.60 },
        { baseAmount: 20, unit: 'g', name: 'Almonds', price: 1.00 },
        { baseAmount: 10, unit: 'g', name: 'Dried cranberries', price: 0.50 },
        { baseAmount: 5, unit: 'g', name: 'Chia seeds', price: 0.30 }
      ],
      image: 'https://m.media-amazon.com/images/I/91Tbf+EPRNS._SL1500_.jpg',
      instructions_step1: "Preheat the oven to 180°C (350°F).",
      ingredients_step1: [],
      instructions_step2: "In a bowl, mix oats, honey, and almonds until well combined.",
      ingredients_step2: [
        { baseAmount: 50, unit: 'g', name: 'Oats', price: 0.80 },
        { baseAmount: 20, unit: 'g', name: 'Honey', price: 0.60 },
        { baseAmount: 20, unit: 'g', name: 'Almonds', price: 1.00 }
      ],
      instructions_step3: "Stir in the dried cranberries and chia seeds.",
      ingredients_step3: [
        { baseAmount: 10, unit: 'g', name: 'Dried cranberries', price: 0.50 },
        { baseAmount: 5, unit: 'g', name: 'Chia seeds', price: 0.30 }
      ],
      instructions_step4: "Press the mixture into a baking tray, bake for 15-20 mins, then cool and cut into bars.",
      ingredients_step4: [],
      videoLink: ""
    },
    {
      name: 'Fresh Fruit Smoothie',
      mealCategory: 'Snack',
      day: 'Sunday',
      time: '10 mins',
      energy: 180,
      protein: 4,
      carbs: 35,
      fat: 4,
      favorite: false,
      ingredients: [
        { baseAmount: 150, unit: 'g', name: 'Mixed fruits', price: 2.00 },
        { baseAmount: 100, unit: 'ml', name: 'Coconut water', price: 1.00 },
        { baseAmount: 50, unit: 'g', name: 'Spinach', price: 0.50 },
        { baseAmount: 5, unit: 'g', name: 'Chia seeds', price: 0.30 }
      ],
      image: 'https://lilluna.com/wp-content/uploads/2022/10/fruit-smoothie-resize-14.jpg',
      instructions_step1: "Combine mixed fruits, coconut water, and spinach in a blender.",
      ingredients_step1: [
        { baseAmount: 150, unit: 'g', name: 'Mixed fruits', price: 2.00 },
        { baseAmount: 100, unit: 'ml', name: 'Coconut water', price: 1.00 },
        { baseAmount: 50, unit: 'g', name: 'Spinach', price: 0.50 }
      ],
      instructions_step2: "Blend until smooth.",
      ingredients_step2: [],
      instructions_step3: "Pour the smoothie into a serving bowl or glass.",
      ingredients_step3: [],
      instructions_step4: "Sprinkle chia seeds on top and serve immediately.",
      ingredients_step4: [
        { baseAmount: 5, unit: 'g', name: 'Chia seeds', price: 0.30 }
      ],
      videoLink: ""
    },
        // ===== DINNER =====
    {
      name: 'Grilled Trout with Herbs',
      mealCategory: 'Dinner',
      day: 'Monday',
      time: '30 mins',
      energy: 420,
      protein: 35,
      carbs: 25,
      fat: 15,
      favorite: false,
      ingredients: [
        { baseAmount: 250, unit: 'g', name: 'Trout', price: 12.00 },
        { baseAmount: 10, unit: 'g', name: 'Fresh herbs', price: 0.80 },
        { baseAmount: 5, unit: 'g', name: 'Lemon zest', price: 0.30 },
        { baseAmount: 5, unit: 'ml', name: 'Olive oil', price: 0.50 },
        { baseAmount: 2, unit: 'g', name: 'Salt', price: 0.05 }
      ],
      image: 'https://i.ytimg.com/vi/KK9PabT4jGw/hq720.jpg?sqp=-oaymwEhCK4FEIIDSFryq4qpAxMIARUAAAAAGAElAADIQj0AgKJD&rs=AOn4CLAB3QWjvBYyPvauByubcdmB-EekgA',
      instructions_step1: "Season the trout with salt, olive oil, and lemon zest.",
      ingredients_step1: [
        { baseAmount: 250, unit: 'g', name: 'Trout', price: 12.00 },
        { baseAmount: 5, unit: 'ml', name: 'Olive oil', price: 0.50 },
        { baseAmount: 5, unit: 'g', name: 'Lemon zest', price: 0.30 },
        { baseAmount: 2, unit: 'g', name: 'Salt', price: 0.05 }
      ],
      instructions_step2: "Preheat the grill to medium-high heat.",
      ingredients_step2: [],
      instructions_step3: "Grill the trout for about 10-12 minutes until cooked through.",
      ingredients_step3: [],
      instructions_step4: "Garnish with fresh herbs and serve immediately.",
      ingredients_step4: [
        { baseAmount: 10, unit: 'g', name: 'Fresh herbs', price: 0.80 }
      ],
      videoLink: ""
    },
    {
      name: 'Chicken Escabeche',
      mealCategory: 'Dinner',
      day: 'Tuesday',
      time: '35 mins',
      energy: 400,
      protein: 32,
      carbs: 30,
      fat: 12,
      favorite: false,
      ingredients: [
        { baseAmount: 200, unit: 'g', name: 'Chicken thigh', price: 6.00 },
        { baseAmount: 100, unit: 'g', name: 'Onion', price: 1.00 },
        { baseAmount: 50, unit: 'ml', name: 'Vinegar', price: 1.00 },
        { baseAmount: 20, unit: 'ml', name: 'Olive oil', price: 1.00 },
        { baseAmount: 10, unit: 'g', name: 'Oregano', price: 0.50 },
        { baseAmount: 5, unit: 'g', name: 'Salt', price: 0.10 }
      ],
      image: 'https://www.allrecipes.com/thmb/CYH6Xm_lYpXaMsemp56Zr2KUK5k=/1500x0/filters:no_upscale():max_bytes(150000):strip_icc()/274330_Chicken-Escabech_Buckwheat-Queen_6754461_original-2x1-1-97265d0402704a05bd63da17fc2ca698.jpg',
      instructions_step1: "Cut the chicken thigh into pieces and slice the onion.",
      ingredients_step1: [
        { baseAmount: 200, unit: 'g', name: 'Chicken thigh', price: 6.00 },
        { baseAmount: 100, unit: 'g', name: 'Onion', price: 1.00 }
      ],
      instructions_step2: "Sauté the chicken and onion in olive oil until lightly browned.",
      ingredients_step2: [
        { baseAmount: 20, unit: 'ml', name: 'Olive oil', price: 1.00 }
      ],
      instructions_step3: "Add vinegar and oregano; let it simmer for 10-15 minutes.",
      ingredients_step3: [
        { baseAmount: 50, unit: 'ml', name: 'Vinegar', price: 1.00 },
        { baseAmount: 10, unit: 'g', name: 'Oregano', price: 0.50 }
      ],
      instructions_step4: "Season with salt, serve hot, and enjoy the tangy flavor.",
      ingredients_step4: [
        { baseAmount: 5, unit: 'g', name: 'Salt', price: 0.10 }
      ],
      videoLink: ""
    },
    {
      name: 'Quinoa Soup with Vegetables',
      mealCategory: 'Dinner',
      day: 'Wednesday',
      time: '40 mins',
      energy: 380,
      protein: 15,
      carbs: 50,
      fat: 8,
      favorite: false,
      ingredients: [
        { baseAmount: 50, unit: 'g', name: 'Quinoa', price: 1.00 },
        { baseAmount: 200, unit: 'ml', name: 'Vegetable broth', price: 1.50 },
        { baseAmount: 100, unit: 'g', name: 'Mixed vegetables', price: 1.00 },
        { baseAmount: 30, unit: 'g', name: 'Tomato', price: 0.40 },
        { baseAmount: 5, unit: 'g', name: 'Salt', price: 0.10 }
      ],
      image: 'https://assets.wholefoodsmarket.com/recipes/4004/2048/1536/4004-5.jpg',
      instructions_step1: "Rinse and cook quinoa in vegetable broth until soft.",
      ingredients_step1: [
        { baseAmount: 50, unit: 'g', name: 'Quinoa', price: 1.00 },
        { baseAmount: 200, unit: 'ml', name: 'Vegetable broth', price: 1.50 }
      ],
      instructions_step2: "Dice mixed vegetables and tomato.",
      ingredients_step2: [
        { baseAmount: 100, unit: 'g', name: 'Mixed vegetables', price: 1.00 },
        { baseAmount: 30, unit: 'g', name: 'Tomato', price: 0.40 }
      ],
      instructions_step3: "Combine the cooked quinoa with the vegetables in a pot.",
      ingredients_step3: [],
      instructions_step4: "Season with salt and simmer for a few more minutes before serving.",
      ingredients_step4: [
        { baseAmount: 5, unit: 'g', name: 'Salt', price: 0.10 }
      ],
      videoLink: ""
    },
    {
      name: 'Steamed Fish with Ginger',
      mealCategory: 'Dinner',
      day: 'Thursday',
      time: '30 mins',
      energy: 360,
      protein: 30,
      carbs: 20,
      fat: 10,
      favorite: false,
      ingredients: [
        { baseAmount: 200, unit: 'g', name: 'White fish fillet', price: 10.00 },
        { baseAmount: 5, unit: 'g', name: 'Fresh ginger', price: 0.50 },
        { baseAmount: 5, unit: 'g', name: 'Garlic', price: 0.20 },
        { baseAmount: 5, unit: 'ml', name: 'Soy sauce', price: 0.30 },
        { baseAmount: 5, unit: 'ml', name: 'Sesame oil', price: 0.40 },
        { baseAmount: 2, unit: 'g', name: 'Salt', price: 0.05 }
      ],
      image: 'https://media-cldnry.s-nbcnews.com/image/upload/newscms/2021_38/1777805/ginger-scallion-fish-main-jc-210921.jpg',
      instructions_step1: "Rinse the fish fillet and pat dry.",
      ingredients_step1: [
        { baseAmount: 200, unit: 'g', name: 'White fish fillet', price: 10.00 }
      ],
      instructions_step2: "Prepare a marinade with grated ginger, garlic, soy sauce, sesame oil, and salt.",
      ingredients_step2: [
        { baseAmount: 5, unit: 'g', name: 'Fresh ginger', price: 0.50 },
        { baseAmount: 5, unit: 'g', name: 'Garlic', price: 0.20 },
        { baseAmount: 5, unit: 'ml', name: 'Soy sauce', price: 0.30 },
        { baseAmount: 5, unit: 'ml', name: 'Sesame oil', price: 0.40 },
        { baseAmount: 2, unit: 'g', name: 'Salt', price: 0.05 }
      ],
      instructions_step3: "Marinate the fish for 10 minutes and then steam it until tender.",
      ingredients_step3: [],
      instructions_step4: "Serve the steamed fish with a drizzle of the marinade and extra fresh ginger if desired.",
      ingredients_step4: [],
      videoLink: ""
    },
    {
      name: 'Vegetable Causa',
      mealCategory: 'Dinner',
      day: 'Friday',
      time: '35 mins',
      energy: 340,
      protein: 10,
      carbs: 55,
      fat: 12,
      favorite: false,
      ingredients: [
        { baseAmount: 150, unit: 'g', name: 'Yellow potato', price: 2.00 },
        { baseAmount: 50, unit: 'g', name: 'Avocado', price: 1.50 },
        { baseAmount: 30, unit: 'g', name: 'Red bell pepper', price: 0.50 },
        { baseAmount: 20, unit: 'g', name: 'Lime juice', price: 0.40 },
        { baseAmount: 5, unit: 'g', name: 'Salt', price: 0.10 }
      ],
      image: 'https://mojo.generalmills.com/api/public/content/gAjkbxNXv0e5knTWfopHPg_gmi_hi_res_jpeg.jpeg?v=58e15536&t=16e3ce250f244648bef28c5949fb99ff',
      instructions_step1: "Boil the yellow potatoes until tender, then mash them.",
      ingredients_step1: [
        { baseAmount: 150, unit: 'g', name: 'Yellow potato', price: 2.00 }
      ],
      instructions_step2: "In a separate bowl, blend the avocado with lime juice and a pinch of salt.",
      ingredients_step2: [
        { baseAmount: 50, unit: 'g', name: 'Avocado', price: 1.50 },
        { baseAmount: 20, unit: 'g', name: 'Lime juice', price: 0.40 },
        { baseAmount: 5, unit: 'g', name: 'Salt', price: 0.10 }
      ],
      instructions_step3: "Mix the mashed potatoes with the avocado mixture and finely diced red bell pepper.",
      ingredients_step3: [
        { baseAmount: 30, unit: 'g', name: 'Red bell pepper', price: 0.50 }
      ],
      instructions_step4: "Adjust the seasoning and serve chilled as a refreshing dish.",
      ingredients_step4: [],
      videoLink: ""
    },
    {
      name: 'Shrimp Stir-fry with Quinoa',
      mealCategory: 'Dinner',
      day: 'Saturday',
      time: '40 mins',
      energy: 450,
      protein: 35,
      carbs: 40,
      fat: 15,
      favorite: false,
      ingredients: [
        { baseAmount: 200, unit: 'g', name: 'Shrimp', price: 10.00 },
        { baseAmount: 100, unit: 'g', name: 'Quinoa', price: 2.50 },
        { baseAmount: 50, unit: 'g', name: 'Broccoli', price: 1.00 },
        { baseAmount: 30, unit: 'g', name: 'Carrot', price: 0.50 },
        { baseAmount: 10, unit: 'ml', name: 'Soy sauce', price: 0.40 },
        { baseAmount: 5, unit: 'g', name: 'Salt', price: 0.10 }
      ],
      image: 'https://uk.lkk.com/images/recipes/main/recipe-quinoa-stir-fry-with-shrimp-and-vegetables-with-oyster-sauce.jpg',
      instructions_step1: "Rinse the quinoa and cook it according to package instructions.",
      ingredients_step1: [
        { baseAmount: 100, unit: 'g', name: 'Quinoa', price: 2.50 }
      ],
      instructions_step2: "Sauté the shrimp in a pan until pink, then set aside.",
      ingredients_step2: [
        { baseAmount: 200, unit: 'g', name: 'Shrimp', price: 10.00 }
      ],
      instructions_step3: "Stir-fry chopped broccoli and carrot in a little oil; add soy sauce and salt.",
      ingredients_step3: [
        { baseAmount: 50, unit: 'g', name: 'Broccoli', price: 1.00 },
        { baseAmount: 30, unit: 'g', name: 'Carrot', price: 0.50 },
        { baseAmount: 10, unit: 'ml', name: 'Soy sauce', price: 0.40 },
        { baseAmount: 5, unit: 'g', name: 'Salt', price: 0.10 }
      ],
      instructions_step4: "Combine the stir-fried vegetables with the quinoa and shrimp; toss well and serve hot.",
      ingredients_step4: [],
      videoLink: ""
    },
    {
      name: 'Light Vegetable Soup',
      mealCategory: 'Dinner',
      day: 'Sunday',
      time: '30 mins',
      energy: 300,
      protein: 8,
      carbs: 40,
      fat: 7,
      favorite: false,
      ingredients: [
        { baseAmount: 100, unit: 'g', name: 'Mixed vegetables', price: 1.00 },
        { baseAmount: 200, unit: 'ml', name: 'Vegetable broth', price: 1.50 },
        { baseAmount: 30, unit: 'g', name: 'Tomato', price: 0.40 },
        { baseAmount: 5, unit: 'g', name: 'Salt', price: 0.10 },
        { baseAmount: 5, unit: 'g', name: 'Pepper', price: 0.20 }
      ],
      image: 'https://www.inspiredtaste.net/wp-content/uploads/2018/10/Homemade-Vegetable-Soup-Recipe-2-1200.jpg',
      instructions_step1: "Chop all mixed vegetables into uniform pieces.",
      ingredients_step1: [
        { baseAmount: 100, unit: 'g', name: 'Mixed vegetables', price: 1.00 }
      ],
      instructions_step2: "Bring the vegetable broth to a boil in a large pot.",
      ingredients_step2: [
        { baseAmount: 200, unit: 'ml', name: 'Vegetable broth', price: 1.50 }
      ],
      instructions_step3: "Add the vegetables and diced tomato; simmer until tender.",
      ingredients_step3: [
        { baseAmount: 30, unit: 'g', name: 'Tomato', price: 0.40 }
      ],
      instructions_step4: "Season with salt and pepper, then serve hot.",
      ingredients_step4: [
        { baseAmount: 5, unit: 'g', name: 'Salt', price: 0.10 },
        { baseAmount: 5, unit: 'g', name: 'Pepper', price: 0.20 }
      ],
      videoLink: ""
    }
  ];
  
  export default mealsInfo;
  