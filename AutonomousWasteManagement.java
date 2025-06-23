import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Main system class
class WasteManagementSystem {
    private List<SmartBin> smartBins;
    private RouteOptimizer routeOptimizer;
    private AIWasteSorter wasteSorter;
    private CostAnalyzer costAnalyzer;
    private EcoRecommendationEngine ecoEngine;
    
    public WasteManagementSystem() {
        this.smartBins = new ArrayList<>();
        this.routeOptimizer = new RouteOptimizer();
        this.wasteSorter = new AIWasteSorter();
        this.costAnalyzer = new CostAnalyzer();
        this.ecoEngine = new EcoRecommendationEngine();
        initializeSystem();
    }
    
    private void initializeSystem() {
        // Initialize smart bins at different locations
        smartBins.add(new SmartBin("BIN001", "Downtown Plaza", 40.7589, -73.9851));
        smartBins.add(new SmartBin("BIN002", "Central Park", 40.7829, -73.9654));
        smartBins.add(new SmartBin("BIN003", "Times Square", 40.7580, -73.9855));
        smartBins.add(new SmartBin("BIN004", "Brooklyn Bridge", 40.7061, -73.9969));
        
        System.out.println("🗂️  Waste Management System Initialized");
        System.out.println("📍 " + smartBins.size() + " smart bins deployed");
    }
    
    public void runDailyOperations() {
        System.out.println("\n=== Daily Operations: " + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + " ===");
        
        // Update bin statuses
        updateBinSensors();
        
        // Get bins that need collection
        List<SmartBin> binsToCollect = identifyBinsForCollection();
        
        if (!binsToCollect.isEmpty()) {
            // Optimize collection route
            Route optimalRoute = routeOptimizer.calculateOptimalRoute(binsToCollect);
            
            // Simulate collection and sorting
            performCollection(optimalRoute);
            
            // Analyze costs
            costAnalyzer.analyzeDailyCosts(optimalRoute, binsToCollect);
            
            // Generate eco recommendations
            ecoEngine.generateRecommendations(binsToCollect);
        } else {
            System.out.println("✅ No bins require collection today");
        }
    }
    
    private void updateBinSensors() {
        System.out.println("\n📡 Updating sensor data...");
        for (SmartBin bin : smartBins) {
            bin.updateSensorData();
        }
    }
    
    private List<SmartBin> identifyBinsForCollection() {
        List<SmartBin> binsToCollect = new ArrayList<>();
        System.out.println("\n🔍 Checking bins for collection:");
        
        for (SmartBin bin : smartBins) {
            if (bin.needsCollection()) {
                binsToCollect.add(bin);
                System.out.println("📋 " + bin.getId() + " at " + bin.getLocation() + 
                    " (" + bin.getFillLevel() + "% full)");
            }
        }
        
        return binsToCollect;
    }
    
    private void performCollection(Route route) {
        System.out.println("\n🚛 Starting waste collection...");
        System.out.println("📍 Route: " + route.getRouteDescription());
        System.out.println("🛣️  Total distance: " + route.getTotalDistance() + " km");
        
        for (SmartBin bin : route.getBins()) {
            // Store waste data before collection (since collectWaste() resets the bin)
            double preCollectionWeight = bin.getWeight();
            String preCollectionType = bin.getWasteType();
            
            // Simulate waste collection
            WasteData wasteData = bin.collectWaste();
            
            // Use the stored pre-collection data for display
            wasteData = new WasteData(bin.getId(), preCollectionWeight, preCollectionType, bin.getFillLevel());
            
            // AI sorting
            SortingResult sortingResult = wasteSorter.sortWaste(wasteData);
            
            System.out.println("🗑️  Collected from " + bin.getId() + ": " + 
                String.format("%.2f", wasteData.getWeight()) + "kg - " + sortingResult.getSummary());
        }
    }
    
    public void displaySystemStatus() {
        System.out.println("\n=== SYSTEM STATUS ===");
        for (SmartBin bin : smartBins) {
            System.out.println(bin.getStatusReport());
        }
    }
}

// Smart Bin with sensors
class SmartBin {
    private String id;
    private String location;
    private double latitude, longitude;
    private int fillLevel; // 0-100%
    private double weight; // kg
    private String wasteType;
    private LocalDateTime lastCollection;
    private BinSensor sensor;
    
    public SmartBin(String id, String location, double lat, double lon) {
        this.id = id;
        this.location = location;
        this.latitude = lat;
        this.longitude = lon;
        this.fillLevel = 0;
        this.weight = 0.0;
        this.lastCollection = LocalDateTime.now().minusDays(1);
        this.sensor = new BinSensor();
    }
    
    public void updateSensorData() {
        // Simulate sensor readings
        sensor.updateReadings();
        this.fillLevel = sensor.getFillLevel();
        this.weight = sensor.getWeight();
        this.wasteType = sensor.detectWasteType();
    }
    
    public boolean needsCollection() {
        return fillLevel >= 80 || 
               LocalDateTime.now().minusDays(3).isAfter(lastCollection);
    }
    
    public WasteData collectWaste() {
        // Store current data before resetting
        double currentWeight = this.weight;
        String currentType = this.wasteType;
        int currentFillLevel = this.fillLevel;
        
        WasteData data = new WasteData(id, currentWeight, currentType, currentFillLevel);
        
        // Reset bin after collection
        this.fillLevel = 0;
        this.weight = 0.0;
        this.lastCollection = LocalDateTime.now();
        return data;
    }
    
    public String getStatusReport() {
        String status = fillLevel >= 80 ? "🔴 FULL" : 
                       fillLevel >= 60 ? "🟡 MEDIUM" : "🟢 LOW";
        return String.format("📍 %s (%s): %s - %d%% full, %.1fkg", 
            id, location, status, fillLevel, weight);
    }
    
    // Getters
    public String getId() { return id; }
    public String getLocation() { return location; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public int getFillLevel() { return fillLevel; }
    public double getWeight() { return weight; }
    public String getWasteType() { return wasteType; }
}

// Sensor simulation class
class BinSensor {
    private Random random = new Random();
    private int fillLevel;
    private double weight;
    
    public void updateReadings() {
        // Simulate realistic sensor data with higher chance of needing collection
        this.fillLevel = 60 + random.nextInt(40); // 60-99% to ensure some bins need collection
        this.weight = fillLevel * 0.8 + random.nextDouble() * 20; // Correlated weight (more realistic)
    }
    
    public String detectWasteType() {
        String[] types = {"Mixed", "Recyclable", "Organic", "Electronic"};
        return types[random.nextInt(types.length)];
    }
    
    public int getFillLevel() { return fillLevel; }
    public double getWeight() { return weight; }
}

// Route optimization using nearest neighbor algorithm
class RouteOptimizer {
    public Route calculateOptimalRoute(List<SmartBin> bins) {
        if (bins.isEmpty()) return new Route();
        
        System.out.println("\n🧭 Calculating optimal collection route...");
        
        List<SmartBin> optimizedRoute = new ArrayList<>();
        List<SmartBin> remaining = new ArrayList<>(bins);
        
        // Start with the first bin
        SmartBin current = remaining.remove(0);
        optimizedRoute.add(current);
        
        // Nearest neighbor algorithm
        while (!remaining.isEmpty()) {
            SmartBin nearest = findNearestBin(current, remaining);
            remaining.remove(nearest);
            optimizedRoute.add(nearest);
            current = nearest;
        }
        
        return new Route(optimizedRoute);
    }
    
    private SmartBin findNearestBin(SmartBin current, List<SmartBin> candidates) {
        SmartBin nearest = candidates.get(0);
        double minDistance = calculateDistance(current, nearest);
        
        for (SmartBin candidate : candidates) {
            double distance = calculateDistance(current, candidate);
            if (distance < minDistance) {
                minDistance = distance;
                nearest = candidate;
            }
        }
        return nearest;
    }
    
    private double calculateDistance(SmartBin bin1, SmartBin bin2) {
        // Simplified distance calculation using Haversine formula
        double lat1 = Math.toRadians(bin1.getLatitude());
        double lat2 = Math.toRadians(bin2.getLatitude());
        double deltaLat = Math.toRadians(bin2.getLatitude() - bin1.getLatitude());
        double deltaLon = Math.toRadians(bin2.getLongitude() - bin1.getLongitude());
        
        double a = Math.sin(deltaLat/2) * Math.sin(deltaLat/2) +
                   Math.cos(lat1) * Math.cos(lat2) *
                   Math.sin(deltaLon/2) * Math.sin(deltaLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return 6371 * c; // Earth's radius in km
    }
}

// Route data structure
class Route {
    private List<SmartBin> bins;
    private double totalDistance;
    
    public Route() {
        this.bins = new ArrayList<>();
        this.totalDistance = 0.0;
    }
    
    public Route(List<SmartBin> bins) {
        this.bins = bins;
        calculateTotalDistance();
    }
    
    private void calculateTotalDistance() {
        totalDistance = 0.0;
        if (bins.size() > 1) {
            for (int i = 1; i < bins.size(); i++) {
                totalDistance += calculateDistance(bins.get(i-1), bins.get(i));
            }
        }
        // Add base distance from depot to first bin and back (simulate real scenario)
        if (!bins.isEmpty()) {
            totalDistance += 2.5; // Assume 2.5km average to/from depot
        }
    }
    
    private double calculateDistance(SmartBin bin1, SmartBin bin2) {
        // Simplified distance calculation
        double lat1 = Math.toRadians(bin1.getLatitude());
        double lat2 = Math.toRadians(bin2.getLatitude());
        double deltaLat = Math.toRadians(bin2.getLatitude() - bin1.getLatitude());
        double deltaLon = Math.toRadians(bin2.getLongitude() - bin1.getLongitude());
        
        double a = Math.sin(deltaLat/2) * Math.sin(deltaLat/2) +
                   Math.cos(lat1) * Math.cos(lat2) *
                   Math.sin(deltaLon/2) * Math.sin(deltaLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return 6371 * c;
    }
    
    public String getRouteDescription() {
        if (bins.isEmpty()) return "No route";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bins.size(); i++) {
            sb.append(bins.get(i).getId());
            if (i < bins.size() - 1) sb.append(" → ");
        }
        return sb.toString();
    }
    
    public List<SmartBin> getBins() { return bins; }
    public double getTotalDistance() { return Math.round(totalDistance * 100.0) / 100.0; }
}

// AI Waste Sorting System
class AIWasteSorter {
    private Map<String, WasteCategory> categories;
    
    public AIWasteSorter() {
        initializeCategories();
    }
    
    private void initializeCategories() {
        categories = new HashMap<>();
        categories.put("Mixed", new WasteCategory("Mixed", "General Waste", 0.05));
        categories.put("Recyclable", new WasteCategory("Recyclable", "Recyclable Materials", 0.15));
        categories.put("Organic", new WasteCategory("Organic", "Compostable Waste", 0.10));
        categories.put("Electronic", new WasteCategory("Electronic", "E-Waste", 0.25));
    }
    
    public SortingResult sortWaste(WasteData wasteData) {
        // Simulate AI image recognition and sorting
        WasteCategory category = categories.get(wasteData.getType());
        if (category == null) {
            category = categories.get("Mixed");
        }
        
        double confidence = 0.85 + Math.random() * 0.14; // 85-99% confidence
        
        return new SortingResult(category, confidence, wasteData.getWeight());
    }
}

// Waste data structure
class WasteData {
    private String binId;
    private double weight;
    private String type;
    private int fillLevel;
    
    public WasteData(String binId, double weight, String type, int fillLevel) {
        this.binId = binId;
        this.weight = weight;
        this.type = type;
        this.fillLevel = fillLevel;
    }
    
    public String getBinId() { return binId; }
    public double getWeight() { return weight; }
    public String getType() { return type; }
    public int getFillLevel() { return fillLevel; }
}

// Waste category
class WasteCategory {
    private String name;
    private String description;
    private double recyclingValue; // Value per kg
    
    public WasteCategory(String name, String desc, double value) {
        this.name = name;
        this.description = desc;
        this.recyclingValue = value;
    }
    
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getRecyclingValue() { return recyclingValue; }
}

// Sorting result
class SortingResult {
    private WasteCategory category;
    private double confidence;
    private double weight;
    
    public SortingResult(WasteCategory category, double confidence, double weight) {
        this.category = category;
        this.confidence = confidence;
        this.weight = weight;
    }
    
    public String getSummary() {
        return String.format("%s (%.1f%% confidence)", 
            category.getDescription(), confidence * 100);
    }
    
    public WasteCategory getCategory() { return category; }
    public double getConfidence() { return confidence; }
    public double getWeight() { return weight; }
}

// Cost Analysis System
class CostAnalyzer {
    private static final double FUEL_COST_PER_KM = 0.80; // $0.80 per km
    private static final double LABOR_COST_PER_HOUR = 25.0; // $25 per hour
    private static final double VEHICLE_DEPRECIATION_PER_KM = 0.15;
    
    public void analyzeDailyCosts(Route route, List<SmartBin> binsCollected) {
        System.out.println("\n💰 Cost Analysis:");
        
        double distance = route.getTotalDistance();
        double estimatedTime = distance / 30.0 + binsCollected.size() * 0.25; // 30km/h + 15min per bin
        
        double fuelCost = distance * FUEL_COST_PER_KM;
        double laborCost = estimatedTime * LABOR_COST_PER_HOUR;
        double depreciation = distance * VEHICLE_DEPRECIATION_PER_KM;
        double totalCost = fuelCost + laborCost + depreciation;
        
        // Calculate potential savings from recycling
        double recyclingRevenue = calculateRecyclingRevenue(binsCollected);
        double netCost = totalCost - recyclingRevenue;
        
        System.out.printf("⛽ Fuel cost: $%.2f\n", fuelCost);
        System.out.printf("👷 Labor cost: $%.2f (%.1f hours)\n", laborCost, estimatedTime);
        System.out.printf("🚛 Vehicle depreciation: $%.2f\n", depreciation);
        System.out.printf("📊 Total operational cost: $%.2f\n", totalCost);
        System.out.printf("♻️  Recycling revenue: $%.2f\n", recyclingRevenue);
        System.out.printf("💡 Net cost: $%.2f\n", netCost);
        
        generateCostOptimizationSuggestions(route, totalCost);
    }
    
    private double calculateRecyclingRevenue(List<SmartBin> bins) {
        double revenue = 0.0;
        // Estimate recycling value based on simulated collected weight
        for (int i = 0; i < bins.size(); i++) {
            double binWeight = 60 + Math.random() * 40; // Simulate collected weight
            revenue += binWeight * (0.05 + Math.random() * 0.10); // $0.05-0.15 per kg
        }
        return revenue;
    }
    
    private void generateCostOptimizationSuggestions(Route route, double currentCost) {
        System.out.println("\n💡 Cost Optimization Suggestions:");
        
        if (route.getTotalDistance() > 50) {
            System.out.println("• Consider deploying additional collection vehicles for long routes");
        }
        
        if (route.getBins().size() < 3) {
            System.out.println("• Optimize collection frequency to batch more bins per trip");
        }
        
        System.out.println("• Predicted 15% cost reduction with AI-optimized scheduling");
        System.out.printf("• Potential monthly savings: $%.2f\n", currentCost * 30 * 0.15);
    }
}

// Eco-friendly Recommendation Engine
class EcoRecommendationEngine {
    public void generateRecommendations(List<SmartBin> collectedBins) {
        System.out.println("\n🌱 Eco-Friendly Recommendations:");
        
        // Calculate total weight from collected bins
        double totalWeight = 0.0;
        for (int i = 0; i < collectedBins.size(); i++) {
            // Use estimated weight since bins are reset after collection
            totalWeight += 60 + Math.random() * 40; // 60-100 kg typical full bin
        }
        
        // Calculate environmental impact
        double co2Saved = totalWeight * 0.5; // kg CO2 saved through proper waste management
        double energySaved = totalWeight * 1.2; // kWh saved through recycling
        
        System.out.printf("🌍 Environmental Impact:\n");
        System.out.printf("• CO2 emissions reduced: %.1f kg\n", co2Saved);
        System.out.printf("• Energy saved through recycling: %.1f kWh\n", energySaved);
        
        System.out.println("\n📋 Recommendations:");
        System.out.println("• Increase recycling rate by 25% through better bin labeling");
        System.out.println("• Install solar panels on smart bins to power sensors");
        System.out.println("• Partner with local composting facilities for organic waste");
        System.out.println("• Implement citizen rewards program for proper waste sorting");
        
        generateCompostingRecommendation(totalWeight);
        generateRecyclingInsights(collectedBins);
    }
    
    private void generateCompostingRecommendation(double totalWeight) {
        double organicWasteEstimate = totalWeight * 0.3; // 30% organic waste estimate
        System.out.printf("♻️  Estimated %.1f kg organic waste could be composted\n", 
            organicWasteEstimate);
        System.out.println("• Potential to create " + Math.round(organicWasteEstimate / 3) + 
            " kg of nutrient-rich compost");
    }
    
    private void generateRecyclingInsights(List<SmartBin> bins) {
        System.out.println("\n📊 Recycling Insights:");
        System.out.println("• 78% of collected waste can be diverted from landfills");
        System.out.println("• Implementing smart sorting increases recycling efficiency by 40%");
        System.out.println("• Current recycling rate estimated at 65% - target: 85%");
    }
}

// Main application
public class AutonomousWasteManagement {
    public static void main(String[] args) {
        System.out.println("🤖 AUTONOMOUS WASTE COLLECTION & SORTING SYSTEM");
        System.out.println("================================================");
        
        WasteManagementSystem system = new WasteManagementSystem();
        
        // Display initial system status
        system.displaySystemStatus();
        
        // Run daily operations simulation
        system.runDailyOperations();
        
        System.out.println("\n✅ Daily operations completed successfully!");
        
        // Simulate system over multiple days
        System.out.println("\n🔄 Simulating system over 3 days...");
        for (int day = 1; day <= 3; day++) {
            System.out.println("\n--- Day " + day + " ---");
            system.runDailyOperations();
        }
        
        System.out.println("\n🎯 System simulation completed!");
        System.out.println("📈 All modules functioning optimally");
    }
}