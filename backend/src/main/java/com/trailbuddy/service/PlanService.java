package com.trailbuddy.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trailbuddy.model.PlanRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.*;

@Service
public class PlanService {
    private final ObjectMapper mapper;
    private final RestClient ollamaClient;
    private final String model;

    public PlanService(ObjectMapper mapper,
                       @Value("${ollama.url}") String ollamaUrl,
                       @Value("${ollama.model}") String model) {
        this.mapper = mapper;
        this.ollamaClient = RestClient.builder().baseUrl(ollamaUrl).build();
        this.model = model;
    }

    public Map<String, Object> health() {
        boolean available = false;
        boolean modelAvailable = false;
        List<String> models = new ArrayList<>();
        try {
            Map<?, ?> response = ollamaClient.get().uri("/api/tags").retrieve().body(Map.class);
            if (response != null) {
                available = true;
                Object raw = response.get("models");
                if (raw instanceof List<?> list) {
                    for (Object entry : list) {
                        if (entry instanceof Map<?, ?> item && item.get("name") != null) {
                            String name = item.get("name").toString();
                            models.add(name);
                            if (name.equals(model) || name.startsWith(model.split(":")[0] + ":")) {
                                modelAvailable = true;
                            }
                        }
                    }
                }
            }
        } catch (RestClientException ignored) {
            // Local inference is optional. The built-in planner remains available.
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("ok", true);
        result.put("ollama", available);
        result.put("model", model);
        result.put("modelAvailable", modelAvailable);
        result.put("models", models);
        return result;
    }

    public Map<String, Object> createPlan(PlanRequest rawRequest) {
        PlanRequest request = normalize(rawRequest);
        try {
            String prompt = """
                You are TrailBuddy, an outdoors-first activity planner. Create a practical, safe plan that gets people off screens.
                Never invent live weather, trail status, opening hours, or exact local facts. Tell the user to verify conditions.
                Return ONLY valid JSON with keys: title (string), intro (string), duration (number),
                itinerary (array of objects with time,title,detail strings), packing (array of strings),
                missions (array of strings), safety (array of strings).
                Use 4-6 itinerary steps and 4-6 gentle nature missions.
                Do not suggest disturbing wildlife, trespassing, unsafe routes, or eating wild plants.
                User details: %s
                """.formatted(mapper.writeValueAsString(request));

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", model);
            body.put("prompt", prompt);
            body.put("stream", false);
            body.put("format", "json");
            body.put("options", Map.of("temperature", 0.6));

            Map<?, ?> result = ollamaClient.post().uri("/api/generate")
                    .body(body).retrieve().body(Map.class);
            if (result != null && result.get("response") != null) {
                Map<String, Object> plan = mapper.readValue(
                        result.get("response").toString(),
                        new TypeReference<Map<String, Object>>() {});
                if (plan.get("itinerary") instanceof List<?> && plan.get("packing") instanceof List<?>) {
                    plan.put("duration", request.getDuration());
                    plan.put("source", "ollama");
                    return plan;
                }
            }
        } catch (Exception ignored) {
            // Fall through to the local deterministic plan.
        }
        return offlinePlan(request);
    }

    private PlanRequest normalize(PlanRequest source) {
        PlanRequest r = source == null ? new PlanRequest() : source;
        r.setPlace(clean(r.getPlace(), "a nearby park or trail", 120));
        r.setActivity(clean(r.getActivity(), "hiking", 30).toLowerCase(Locale.ROOT));
        r.setLevel(clean(r.getLevel(), "beginner", 30));
        r.setInterests(clean(r.getInterests(), "nature and a screen-free break", 180));
        int duration = r.getDuration() == null ? 120 : r.getDuration();
        r.setDuration(Math.min(480, Math.max(30, duration)));
        return r;
    }

    private String clean(String value, String fallback, int max) {
        if (value == null || value.isBlank()) return fallback;
        return value.trim().substring(0, Math.min(value.trim().length(), max));
    }

    private Map<String, Object> offlinePlan(PlanRequest r) {
        Map<String, List<String>> suggestions = Map.of(
            "hiking", List.of(
                "Choose a marked route that matches your fitness and daylight.",
                "Pause at a safe viewpoint for a water break.",
                "Notice three different leaf shapes or tree types."),
            "walking", List.of(
                "Pick a loop through a park or quiet neighborhood.",
                "Walk for ten minutes without headphones and notice nearby sounds.",
                "Find a bench or green space for a mindful five-minute pause."),
            "gardening", List.of(
                "Check the season and sunlight before choosing plants.",
                "Prepare a small patch or container and remove weeds safely.",
                "Plant suitable seeds or seedlings and label what you planted."),
            "birding", List.of(
                "Visit a green space at a quiet time and keep a respectful distance.",
                "Listen before looking; note calls, colors, and behavior.",
                "Record observations without approaching nests or feeding wildlife."),
            "running", List.of(
                "Warm up with an easy walk and dynamic movement.",
                "Choose a familiar, well-lit route and keep the pace conversational.",
                "Cool down with a walk and gentle stretches.")
        );
        List<String> tips = suggestions.getOrDefault(r.getActivity(), suggestions.get("hiking"));
        List<Map<String, String>> itinerary = List.of(
            step("First 10%", "Arrive and get oriented", "At " + r.getPlace() + ", check weather, daylight, access rules, and your return plan."),
            step("Next 20%", "Ease into the activity", tips.get(0)),
            step("Middle 50%", "Explore with curiosity", tips.get(1) + " Focus on: " + r.getInterests() + "."),
            step("Next 15%", "Slow down and observe", tips.get(2)),
            step("Final 5%", "Wrap up and return", "Leave no trace, take all rubbish with you, and let someone know you are safely back.")
        );

        List<String> packing = new ArrayList<>(List.of(
            "Water bottle", "Weather-appropriate layers", "Charged phone and a small power bank",
            "Basic first-aid items", "Any personal medication", "Small bag for rubbish"));
        if (r.getActivity().equals("hiking") || r.getActivity().equals("birding")) {
            packing.add("Offline map or downloaded route");
            packing.add("Sun protection");
        } else if (r.getActivity().equals("gardening")) {
            packing.addAll(List.of("Gardening gloves", "Suitable seeds or seedlings", "Small hand trowel"));
        } else if (r.getActivity().equals("running")) {
            packing.addAll(List.of("Comfortable running shoes", "Reflective layer if visibility is low"));
        }

        Map<String, Object> plan = new LinkedHashMap<>();
        plan.put("title", Character.toUpperCase(r.getActivity().charAt(0)) + r.getActivity().substring(1) + " reset");
        plan.put("intro", "A " + r.getDuration() + "-minute, " + r.getLevel() + "-friendly plan for " + r.getPlace() + ". Built with the offline planner because local AI is not currently available.");
        plan.put("duration", r.getDuration());
        plan.put("itinerary", itinerary);
        plan.put("packing", packing);
        plan.put("missions", List.of(
            "Spot a color in nature you rarely notice.",
            "Listen quietly for 60 seconds and name three sounds.",
            "Find an interesting natural pattern without disturbing it.",
            "Take one photo, then put your phone away for 15 minutes.",
            "Leave the place cleaner than you found it, if safe to do so."
        ));
        plan.put("safety", List.of(
            "Check local weather, trail/park closures, and daylight before leaving.",
            "Tell someone your plan for remote outings.",
            "Stay on permitted paths and do not touch unknown plants or wildlife."
        ));
        plan.put("source", "offline");
        return plan;
    }

    private Map<String, String> step(String time, String title, String detail) {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("time", time);
        result.put("title", title);
        result.put("detail", detail);
        return result;
    }
}
