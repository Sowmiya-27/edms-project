package com.edms.edms.dto;

import java.util.List;

public class GenAIResponse {

    private String answer;
    private double confidence;
    private List<String> sources;

    public GenAIResponse() {}

    public GenAIResponse(String answer, double confidence, List<String> sources) {
        this.answer = answer;
        this.confidence = confidence;
        this.sources = sources;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public List<String> getSources() {
        return sources;
    }

    public void setSources(List<String> sources) {
        this.sources = sources;
    }
}
