package com.michielo.antivpn.api;

public class APIResult {

    /*
        Fields
     */

    private VPNResult result;
    private String location = null;

    /*
        Constructors
        - supports 3 different options to init; 1 with just a VPNResult, 1 with VPNResult & location
            and finally a constructor that takes a serialized string which then deserializes
     */

    public APIResult(VPNResult result, String location) {
        this.result = result;
        this.location = location;
    }

    public APIResult(String serialized) throws IllegalArgumentException {
        deserialize(serialized);
    }

    public APIResult(VPNResult result) {
        this.result = result;
    }

    /*
        Getters
     */

    public VPNResult getResult() {
        return this.result;
    }

    public String getLocation() {
        return this.location;
    }

    /*
        String serialization
     */

    public String serialize() {
        String resultStr = (result != null) ? result.name() : "null";
        String locationStr = (location != null) ? location : "null";
        return resultStr + "|" + locationStr;
    }

    public void deserialize(String serialized) throws IllegalArgumentException {
        // Split the string assuming '|' as the delimiter
        String[] parts = serialized.split("\\|");

        // Check for the minimum required parts (1 for result, 1 for location)
        // This can be incremented in the future to accommodate more fields & invalidating older records
        if (parts.length < 2) {
            throw new IllegalArgumentException("Serialized string does not contain enough information.");
        }

        try {
            // Deserialize the first part into the result field
            this.result = VPNResult.valueOf(parts[0]); // Assuming VPNResult has a valueOf method
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Deserialization failed for VPNResult: " + e.getMessage());
        }

        // Handle the location field, allowing it to be null
        this.location = "null".equals(parts[1]) ? null : parts[1];
    }

}
