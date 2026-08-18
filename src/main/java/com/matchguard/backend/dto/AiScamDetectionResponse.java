package com.matchguard.backend.dto;

public class AiScamDetectionResponse {

    private int trustScore;
    private String summary;
    private boolean verifiedSafe;

    public AiScamDetectionResponse(
            int trustScore,
            String summary,
            boolean verifiedSafe
    ) {
        this.trustScore = trustScore;
        this.summary = summary;
        this.verifiedSafe = verifiedSafe;
    }

    public int getTrustScore() {
        return trustScore;
    }

    public String getSummary() {
        return summary;
    }

    public boolean isVerifiedSafe() {
        return verifiedSafe;
    }
}