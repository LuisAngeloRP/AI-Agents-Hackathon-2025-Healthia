package com.healthia.java.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthia.java.models.UserData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class MealPlanGeneratorService {

    private static final Logger log = LoggerFactory.getLogger(MealPlanGeneratorService.class);
    private static final String MEALS_RESOURCE_PATH = "data/meals.js";

    private List<Map<String, Object>> mealsData = Collections.emptyList();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void loadMealsData() {
        try {
            ClassPathResource resource = new ClassPathResource(MEALS_RESOURCE_PATH);
            if (!resource.exists()) {
                log.error("Meals data file not found at: {}", MEALS_RESOURCE_PATH);
                return;
            }
            InputStream inputStream = resource.getInputStream();
            byte[] bdata = FileCopyUtils.copyToByteArray(inputStream);
            String content = new String(bdata, StandardCharsets.UTF_8);

            // Extract JSON array from JS file content
            int startIndex = content.indexOf('[');
            int endIndex = content.lastIndexOf(']');
            if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
                String jsonContent = content.substring(startIndex, endIndex + 1);
                mealsData = objectMapper.readValue(jsonContent, new TypeReference<List<Map<String, Object>>>() {});
                log.info("Successfully loaded {} meals from {}", mealsData.size(), MEALS_RESOURCE_PATH);
            } else {
                log.error("Could not find JSON array structure in {}", MEALS_RESOURCE_PATH);
            }
        } catch (IOException e) {
            log.error("Error loading or parsing meals data from {}", MEALS_RESOURCE_PATH, e);
        }
    }

    public String generateMealPlanJson(UserData userData) {
        Map<String, Object> mealPlan = generateMealPlan(userData);
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(mealPlan);
        } catch (IOException e) {
            log.error("Error serializing meal plan to JSON", e);
            return "{\"error\": \"Error generating JSON plan\"}";
        }
    }

    public Map<String, Object> generateMealPlan(UserData userData) {
        if (mealsData.isEmpty()) {
            log.warn("Meals data is empty, cannot generate plan.");
            return Map.of("error", "No se pudieron cargar los datos de comidas");
        }

        int dailyCalories = calculateCaloricNeeds(userData);
        Map<String, Double> macrosDistribution = determineMacroDistribution(userData);
        Map<String, Object> weeklyPlan = createWeeklyPlan(userData, dailyCalories, macrosDistribution);

        Map<String, Object> plan = new LinkedHashMap<>(); // Use LinkedHashMap to preserve insertion order
        Map<String, Object> usuarioInfo = new LinkedHashMap<>();
        Map<String, Object> datosPersonales = new LinkedHashMap<>();

        datosPersonales.put("edad", userData.getEdad());
        datosPersonales.put("peso", userData.getPeso());
        datosPersonales.put("altura", userData.getAltura());
        datosPersonales.put("genero", userData.getGenero());
        datosPersonales.put("nivel_actividad", userData.getNivelActividad());
        datosPersonales.put("objetivos", userData.getObjetivos());
        datosPersonales.put("restricciones_alimentarias", userData.getRestriccionesAlimentarias());
        datosPersonales.put("alergias", userData.getAlergias());

        usuarioInfo.put("nombre", userData.getNombre());
        usuarioInfo.put("datos_personales", datosPersonales);

        Map<String, Object> recomendaciones = new LinkedHashMap<>();
        recomendaciones.put("calorias_diarias", dailyCalories);
        recomendaciones.put("distribucion_macronutrientes", macrosDistribution);
        recomendaciones.put("justificacion", generatePlanJustification(userData, dailyCalories, macrosDistribution));

        plan.put("usuario", usuarioInfo);
        plan.put("recomendaciones_nutricionales", recomendaciones);
        plan.put("plan_semanal", weeklyPlan);

        return plan;
    }

    private int calculateCaloricNeeds(UserData userData) {
        double peso = Optional.ofNullable(userData.getPeso()).orElse(70.0);
        double altura = Optional.ofNullable(userData.getAltura()).orElse(170.0);
        int edad = Optional.ofNullable(userData.getEdad()).orElse(30);
        String genero = Optional.ofNullable(userData.getGenero()).orElse("hombre").toLowerCase();
        String nivelActividad = Optional.ofNullable(userData.getNivelActividad()).orElse("moderado").toLowerCase();
        List<String> objetivos = Optional.ofNullable(userData.getObjetivos()).orElse(Collections.emptyList())
                                       .stream().map(String::toLowerCase).toList();

        double tmb;
        if (genero.equals("hombre") || genero.equals("masculino")) {
            tmb = 10 * peso + 6.25 * altura - 5 * edad + 5;
        } else { // mujer or femenino
            tmb = 10 * peso + 6.25 * altura - 5 * edad - 161;
        }

        Map<String, Double> multiplicadores = Map.of(
                "sedentario", 1.2,
                "ligero", 1.375,
                "moderado", 1.55,
                "activo", 1.725,
                "muy activo", 1.9
        );
        double factorActividad = multiplicadores.getOrDefault(nivelActividad, 1.55);
        double calories = tmb * factorActividad;

        if (objetivos.contains("perder peso")) {
            calories *= 0.85;
        } else if (objetivos.contains("ganar masa muscular")) {
            calories *= 1.1;
        }

        return (int) calories;
    }

    private Map<String, Double> determineMacroDistribution(UserData userData) {
        List<String> objetivos = Optional.ofNullable(userData.getObjetivos()).orElse(Collections.emptyList())
                                       .stream().map(String::toLowerCase).toList();

        double proteinPct = 0.25;
        double carbsPct = 0.50;
        double fatPct = 0.25;

        if (objetivos.contains("perder peso")) {
            proteinPct = 0.30; carbsPct = 0.40; fatPct = 0.30;
        } else if (objetivos.contains("ganar masa muscular")) {
            proteinPct = 0.35; carbsPct = 0.45; fatPct = 0.20;
        } else if (objetivos.contains("rendimiento deportivo")) {
            proteinPct = 0.25; carbsPct = 0.55; fatPct = 0.20;
        }

        return Map.of("proteinas", proteinPct, "carbohidratos", carbsPct, "grasas", fatPct);
    }

    private Map<String, Object> createWeeklyPlan(UserData userData, int dailyCalories, Map<String, Double> macrosDistribution) {
        String[] diasSemana = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
        Map<String, List<Map<String, Object>>> comidasPorTipo = categorizeMeals();

        List<String> restricciones = Optional.ofNullable(userData.getRestriccionesAlimentarias()).orElse(Collections.emptyList())
                                              .stream().map(String::toLowerCase).toList();
        List<String> alergias = Optional.ofNullable(userData.getAlergias()).orElse(Collections.emptyList())
                                      .stream().map(String::toLowerCase).toList();

        Map<String, Object> planSemanal = new LinkedHashMap<>();
        Random random = new Random();

        for (String dia : diasSemana) {
            Map<String, Object> comidasDia = new LinkedHashMap<>();
            comidasDia.put("desayuno", selectMeal(comidasPorTipo.getOrDefault("Breakfast", Collections.emptyList()), restricciones, alergias, dia, random));
            comidasDia.put("almuerzo", selectMeal(comidasPorTipo.getOrDefault("Lunch", Collections.emptyList()), restricciones, alergias, dia, random));
            comidasDia.put("merienda", selectMeal(comidasPorTipo.getOrDefault("Snack", Collections.emptyList()), restricciones, alergias, dia, random));
            comidasDia.put("cena", selectMeal(comidasPorTipo.getOrDefault("Dinner", Collections.emptyList()), restricciones, alergias, dia, random));

            Map<String, Double> totalesDia = calculateDayTotals(comidasDia);
            Map<String, Object> diaPlan = new LinkedHashMap<>();
            diaPlan.put("comidas", comidasDia);
            diaPlan.put("totales_nutricionales", totalesDia);
            diaPlan.put("adecuacion_calorica", calculateCaloricAdequacy(totalesDia.getOrDefault("energia", 0.0), dailyCalories));

            planSemanal.put(dia, diaPlan);
        }
        return planSemanal;
    }

    private Map<String, List<Map<String, Object>>> categorizeMeals() {
        Map<String, List<Map<String, Object>>> categories = new HashMap<>();
        for (Map<String, Object> meal : mealsData) {
            String category = (String) meal.get("mealCategory");
            if (category != null) {
                categories.computeIfAbsent(category, k -> new ArrayList<>()).add(meal);
            }
        }
        return categories;
    }

    private Map<String, Object> selectMeal(List<Map<String, Object>> meals, List<String> restricciones, List<String> alergias, String diaSemana, Random random) {
        // Simple selection for now - doesn't filter by restrictions/allergies yet
        // Needs detailed ingredient data in meals.js for proper filtering
        if (meals.isEmpty()) {
            return Map.of("nombre", "No se encontró comida adecuada", "energia", 0.0, "proteinas", 0.0, "carbohidratos", 0.0, "grasas", 0.0);
        }

        // Prioritize day-specific meals if available
        String diaEng = translateDayToEnglish(diaSemana);
        List<Map<String, Object>> daySpecificMeals = meals.stream()
            .filter(m -> diaEng.equalsIgnoreCase((String) m.get("day")))
            .toList();

        Map<String, Object> selectedMealSource = daySpecificMeals.isEmpty() ?
             meals.get(random.nextInt(meals.size())) : daySpecificMeals.get(0);


        Map<String, Object> mealInfo = new LinkedHashMap<>();
        mealInfo.put("nombre", selectedMealSource.getOrDefault("name", ""));
        // Ensure numeric values are parsed correctly
        mealInfo.put("energia", ((Number)selectedMealSource.getOrDefault("energy", 0)).doubleValue());
        mealInfo.put("proteinas", ((Number)selectedMealSource.getOrDefault("protein", 0)).doubleValue());
        mealInfo.put("carbohidratos", ((Number)selectedMealSource.getOrDefault("carbs", 0)).doubleValue());
        mealInfo.put("grasas", ((Number)selectedMealSource.getOrDefault("fat", 0)).doubleValue());
        mealInfo.put("tiempo_preparacion", selectedMealSource.getOrDefault("time", ""));
        mealInfo.put("imagen", selectedMealSource.getOrDefault("image", ""));
        mealInfo.put("justificacion", generateMealJustification(selectedMealSource));

        return mealInfo;
    }

    private String translateDayToEnglish(String dia) {
        return Map.of(
                "Lunes", "Monday", "Martes", "Tuesday", "Miércoles", "Wednesday",
                "Jueves", "Thursday", "Viernes", "Friday", "Sábado", "Saturday", "Domingo", "Sunday"
        ).getOrDefault(dia, dia);
    }

    private Map<String, Double> calculateDayTotals(Map<String, Object> comidasDia) {
        double totalEnergia = 0, totalProteinas = 0, totalCarbohidratos = 0, totalGrasas = 0;
        for (Object comidaObj : comidasDia.values()) {
            if (comidaObj instanceof Map) {
                 @SuppressWarnings("unchecked")
                 Map<String, Object> comida = (Map<String, Object>) comidaObj;
                 totalEnergia += ((Number)comida.getOrDefault("energia", 0)).doubleValue();
                 totalProteinas += ((Number)comida.getOrDefault("proteinas", 0)).doubleValue();
                 totalCarbohidratos += ((Number)comida.getOrDefault("carbohidratos", 0)).doubleValue();
                 totalGrasas += ((Number)comida.getOrDefault("grasas", 0)).doubleValue();
            }
        }
        return Map.of("energia", totalEnergia, "proteinas", totalProteinas, "carbohidratos", totalCarbohidratos, "grasas", totalGrasas);
    }

    private Map<String, Object> calculateCaloricAdequacy(double totalEnergia, double recomendacion) {
        double porcentaje = (recomendacion > 0) ? (totalEnergia / recomendacion) * 100 : 0;
        double diferencia = totalEnergia - recomendacion;
        String evaluacion = "adecuada";
        if (porcentaje < 90) evaluacion = "deficiente";
        else if (porcentaje > 110) evaluacion = "excesiva";

        Map<String, Object> adequacy = new LinkedHashMap<>();
        adequacy.put("porcentaje_adecuacion", Math.round(porcentaje * 10.0) / 10.0);
        adequacy.put("diferencia_calorica", (int) diferencia);
        adequacy.put("evaluacion", evaluacion);
        return adequacy;
    }

    private String generateMealJustification(Map<String, Object> meal) {
        String nombre = (String) meal.getOrDefault("name", "");
        double energia = ((Number) meal.getOrDefault("energy", 0)).doubleValue();
        double proteinas = ((Number) meal.getOrDefault("protein", 0)).doubleValue();
        double carbohidratos = ((Number) meal.getOrDefault("carbs", 0)).doubleValue();
        double grasas = ((Number) meal.getOrDefault("fat", 0)).doubleValue();

        String beneficio;
        if (proteinas > 20) beneficio = String.format("alto contenido proteico (%.0fg)", proteinas);
        else if (carbohidratos > 40) beneficio = String.format("buena fuente de energía (%.0fg de carbohidratos)", carbohidratos);
        else if (grasas < 10) beneficio = String.format("bajo contenido en grasas (%.0fg)", grasas);
        else beneficio = "perfil nutricional equilibrado";

        return String.format("Se seleccionó %s por su %s y aporte de %.0f kcal.", nombre, beneficio, energia);
    }

    private String generatePlanJustification(UserData userData, int dailyCalories, Map<String, Double> macrosDistribution) {
        List<String> objetivos = Optional.ofNullable(userData.getObjetivos()).orElse(Collections.emptyList());
        String nivelActividad = Optional.ofNullable(userData.getNivelActividad()).orElse("moderado");

        String justificacion = String.format("Este plan proporciona aproximadamente %d calorías diarias, ", dailyCalories);
        if (!objetivos.isEmpty()) {
            justificacion += String.format("diseñado para apoyar tus objetivos de %s, ", String.join(", ", objetivos));
        }
        justificacion += String.format("considerando tu nivel de actividad %s. ", nivelActividad);
        justificacion += String.format("La distribución de macronutrientes es: %.0f%% proteínas, %.0f%% carbohidratos, %.0f%% grasas, para maximizar los resultados según tus necesidades específicas.",
                macrosDistribution.getOrDefault("proteinas", 0.0) * 100,
                macrosDistribution.getOrDefault("carbohidratos", 0.0) * 100,
                macrosDistribution.getOrDefault("grasas", 0.0) * 100);
        return justificacion;
    }
} 